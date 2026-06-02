package com.delhivery.axle.ui.home.fragments.pod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.ApiError
import com.delhivery.axle.api.repository.LoadCycleRepository
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.api.repository.UserRepository
import com.delhivery.axle.api.repository.UserSearchLimit
import com.delhivery.axle.api.request.SearchRequest
import com.delhivery.axle.api.response.SearchTripsResponse
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.PodCounts
import com.delhivery.axle.ui.common.UiEvent
import com.delhivery.axle.ui.common.UiState
import com.delhivery.axle.ui.common.UserIntent

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Flow-based ViewModel for HomePods screen using MVI architecture.
 * 
 * This ViewModel manages UI state using StateFlow and processes user intents
 * through explicit intent handler functions. It follows the MVI pattern for
 * predictable state management and unidirectional data flow.
 * 
 * Key responsibilities:
 * - Process user intents (search, refresh, loadMore, retry)
 * - Manage UI state using StateFlow (single source of truth)
 * - Handle pagination state (offset, hasMore, isLoadingMore)
 * - Emit one-time events for navigation and messages
 * 
 * Migration note: This is the Flow-based replacement for HomePodViewModel.
 * The existing RxJava-based ViewModel remains for backward compatibility.
 */
class HomePodViewModelFlow @Inject constructor(
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

    // Current search parameters (for pagination and retry)
    private var currentSearchRequest: SearchRequest? = null

    // Active search job — cancelled when a new search starts
    private var searchJob: Job? = null

    /**
     * Single entry point for processing all user intents.
     * 
     * This is the core of MVI architecture - all user actions flow through this method,
     * making the system predictable and easy to test.
     * 
     * @param intent The user intent to process
     */
    fun processIntent(intent: UserIntent) {
        when (intent) {
            is UserIntent.Refresh -> refresh()
            is UserIntent.LoadMore -> loadMore()
            is UserIntent.Retry -> retry()
            else -> {}
        }
    }

    /**
     * Intent: User initiates a new search for trips.
     * 
     * This resets pagination state and fetches the first page of results.
     * 
     * @param request SearchRequest containing search parameters
     */
    fun searchTrips(request: SearchRequest) {
        currentSearchRequest = request
        currentOffset = 0
        hasMore = true

        // Cancel any in-flight search so stale results don't overwrite the new state
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = UiState.Loading(isRefreshing = false)
        }
    }

    /**
     * Intent: User pulls to refresh the trip list.
     * 
     * This resets pagination and replaces existing data with fresh data.
     */
    fun refresh() {
        val request = currentSearchRequest ?: return
        currentOffset = 0
        hasMore = true

        viewModelScope.launch {
            // Set refreshing flag
            _uiState.value = UiState.Loading(isRefreshing = true)

            val paginatedRequest = buildRequest(request, 0)
        }
    }

    /**
     * Intent: User scrolls to bottom and triggers load more.
     * 
     * This appends new data to the existing list without replacing it.
     */
    fun loadMore() {
        if (!hasMore) return

        val request = currentSearchRequest ?: return
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return

        viewModelScope.launch {
            // Set loading more flag
            _uiState.value = currentState.copy(isLoadingMore = true)
        }
    }

    /**
     * Intent: User taps retry button after an error.
     * 
     * This triggers the same search request that caused the error.
     */
    fun retry() {
        val request = currentSearchRequest ?: return
        searchTrips(request)
    }

    /**
     * Maps Resource to UiState.
     * 
     * This function handles the conversion from repository Resource states
     * to UI-specific UiState, including updating pagination state.
     */
    private fun mapResourceToUiState(
        resource: Resource<SearchTripsResponse>,
        isInitialLoad: Boolean
    ): UiState<List<HomeTripsItemData>> {
        return when (resource) {
            is Resource.Loading -> UiState.Loading(isRefreshing = false)

            is Resource.Success -> {
                val trips = resource.data?.trips ?: emptyList()
                
                // Update pod counts
                resource.data?.podCounts?.let { _podCounts.value = it }

                if (trips.isEmpty()) {
                    UiState.Empty("No trips found")
                } else {
                    currentOffset = trips.size
                    hasMore = resource.data?.hasNext ?: false
                    UiState.Success(
                        data = trips,
                        hasMore = hasMore
                    )
                }
            }
            is Resource.Failure -> UiState.Error(
                apiError = resource.apiError,
                message = getErrorMessage(resource.apiError),
                isNetworkError = resource.isNetworkError
            )
        }
    }

    /**
     * Builds request with pagination parameters.
     * 
     * @param baseRequest The base search request
     * @param offset The pagination offset
     * @return JsonObject with pagination parameters added
     */
    private fun buildRequest(baseRequest: SearchRequest, offset: Int): com.google.gson.JsonObject {
        baseRequest.offset = offset
        baseRequest.limit = pageSize
        baseRequest.vendorId = userRepository.userId()
        return baseRequest.getRequest()
    }

    /**
     * Maps ApiError to user-friendly message.
     * 
     * This provides consistent error messaging across the app.
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
