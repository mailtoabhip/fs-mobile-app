package com.dfd.delfin.ui.home.fragments.pod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dfd.delfin.api.repository.ApiError
import com.dfd.delfin.api.repository.LoadCycleRepository
import com.dfd.delfin.api.repository.Resource
import com.dfd.delfin.api.repository.UserRepository
import com.dfd.delfin.api.repository.UserSearchLimit
import com.dfd.delfin.api.request.SearchRequest
import com.dfd.delfin.api.response.SearchTripsResponse
import com.dfd.delfin.data.home.trips.HomeTripsItemData
import com.dfd.delfin.data.home.trips.PodCounts
import com.dfd.delfin.data.home.trips.TripStatus.EPodUploaded
import com.dfd.delfin.data.home.trips.TripStatus.TruckUnloaded
import com.dfd.delfin.ui.common.UiEvent
import com.dfd.delfin.ui.common.UiState
import com.dfd.delfin.ui.common.UserIntent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Flow-based ViewModel for SubmittedPodTab screen using MVI architecture.
 * 
 * This ViewModel manages UI state for submitted/dispatched POD items.
 * It follows the MVI pattern for predictable state management.
 * 
 * Key responsibilities:
 * - Process user intents (search, refresh, loadMore, retry)
 * - Manage UI state using StateFlow
 * - Filter for dispatched items (items with POD tracking)
 * - Handle pagination state
 * - Emit one-time events for navigation and messages
 */
class SubmittedPodViewModelFlow @Inject constructor(
    private val loadCycleRepository: LoadCycleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // UI State - Single source of truth for UI
    private val _uiState = MutableStateFlow<UiState<List<HomeTripsItemData>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<HomeTripsItemData>>> = _uiState.asStateFlow()

    // One-time events (navigation, toasts, snackbars)
    private val _events = MutableSharedFlow<UiEvent>(replay = 0)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // Pod counts state
    private val _podCounts = MutableStateFlow<PodCounts?>(null)
    val podCounts: StateFlow<PodCounts?> = _podCounts.asStateFlow()

    // Pagination state
    private var currentOffset = 0
    private var hasMore = true
    private val pageSize = UserSearchLimit

    /**
     * Single entry point for processing all user intents.
     */
    fun processIntent(intent: UserIntent) {
        when (intent) {
            is UserIntent.Search -> {
                // Not used for SubmittedPod - use Refresh instead
            }
            is UserIntent.Refresh -> refresh()
            is UserIntent.LoadMore -> loadMore()
            is UserIntent.Retry -> retry()
            else -> {}
        }
    }
    
    /**
     * Initial load - same as refresh.
     */
    fun initialLoad() {
        refresh()
    }

    /**
     * Fetch trips for submitted POD.
     */
    private fun fetchTrips(paginate: Boolean) {
        if (paginate && !hasMore) return

        viewModelScope.launch {
            if (!paginate) {
                _uiState.value = UiState.Loading(isRefreshing = false)
                currentOffset = 0
                hasMore = true
            } else {
                val currentState = _uiState.value
                if (currentState is UiState.Success) {
                    _uiState.value = currentState.copy(isLoadingMore = true)
                }
            }

            val request = buildRequest(currentOffset)
        }
    }

    /**
     * Refresh the trip list.
     */
    private fun refresh() {
        currentOffset = 0
        hasMore = true
        fetchTrips(paginate = false)
    }

    /**
     * Load more trips (pagination).
     */
    private fun loadMore() {
        if (!hasMore) return
        fetchTrips(paginate = true)
    }

    /**
     * Retry after error.
     */
    private fun retry() {
        fetchTrips(paginate = false)
    }

    /**
     * Handle resource response from repository.
     */
    private suspend fun handleResource(
        resource: Resource<SearchTripsResponse>,
        paginate: Boolean
    ) {
        when (resource) {
            is Resource.Loading -> {
                // State already set before API call
            }
            is Resource.Success -> {
                val allTrips = resource.data?.trips ?: emptyList()
                
                // Update pod counts
                resource.data?.podCounts?.let { _podCounts.value = it }

                // Filter for submitted items (items with POD tracking)
                val filteredTrips = allTrips.filter { trip ->
                    trip.hasPODTracking()
                }

                if (paginate) {
                    val currentState = _uiState.value
                    if (currentState is UiState.Success) {
                        val combinedTrips = currentState.data + filteredTrips
                        currentOffset = combinedTrips.size
                        hasMore = resource.data?.hasNext ?: false
                        _uiState.value = UiState.Success(
                            data = combinedTrips,
                            isLoadingMore = false,
                            hasMore = hasMore
                        )
                    }
                } else {
                    if (filteredTrips.isEmpty()) {
                        _uiState.value = UiState.Empty("No submitted POD items found")
                    } else {
                        currentOffset = filteredTrips.size
                        hasMore = resource.data?.hasNext ?: false
                        _uiState.value = UiState.Success(
                            data = filteredTrips,
                            hasMore = hasMore
                        )
                    }
                }
            }
            is Resource.Failure -> {
                val currentState = _uiState.value
                if (currentState is UiState.Success && paginate) {
                    // Preserve existing data, show error in snackbar
                    _uiState.value = currentState.copy(isLoadingMore = false)
                    _events.emit(UiEvent.ShowSnackbar(
                        message = "Failed to load more: ${getErrorMessage(resource.apiError)}",
                        action = "Retry"
                    ))
                } else {
                    _uiState.value = UiState.Error(
                        apiError = resource.apiError,
                        message = getErrorMessage(resource.apiError),
                        isNetworkError = resource.isNetworkError
                    )
                }
            }
        }
    }

    /**
     * Build request with pagination parameters.
     */
    private fun buildRequest(offset: Int): com.google.gson.JsonObject {
        val request = SearchRequest()
        request.offset = offset
        request.limit = pageSize
        request.vendorId = userRepository.userId()
        
        // For submitted POD, we always use EPodUploaded + TruckUnloaded status
        request.tripStatus = EPodUploaded.statusKey + "," + TruckUnloaded.statusKey
        request.value = null
        
        return request.getRequest()
    }

    /**
     * Maps ApiError to user-friendly message.
     */
    private fun getErrorMessage(apiError: ApiError): String {
        return when (apiError) {
            ApiError.Network -> "No internet connection. Please check your network."
            ApiError.Timeout -> "Request timed out. Please try again."
            ApiError.Unauthorized -> "Session expired. Please log in again."
            ApiError.AccessDenied -> "You don't have permission to access this resource."
            ApiError.NotFound -> "Resource not found."
            ApiError.ServiceUnavailable -> "Service temporarily unavailable. Please try again later."
            ApiError.Unknown -> "An unexpected error occurred. Please try again."
        }
    }
}
