package com.dfd.delfin.ui.home.fragments.pod

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
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.common.UiEvent
import com.dfd.delfin.ui.common.UiState
import com.dfd.delfin.ui.common.UserIntent
import com.dfd.delfin.utils.DatePatterns.OrionDateFormat
import com.dfd.delfin.utils.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for PendingPodTab — clean MVI with a single state container.
 *
 * All observable UI state lives in [ViewState].
 * Loading context is expressed via explicit boolean flags instead of a sealed Loading sub-state,
 * so the Fragment always knows exactly _how_ things are loading without any guesswork.
 */
class PendingPodViewModelFlow @Inject constructor(
    private val loadCycleRepository: LoadCycleRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    // ────────────────────────── Pod type enum ──────────────────────────

    enum class PodType { EPOD, HPOD }

    // ────────────────────────── ViewState ──────────────────────────────

    /**
     * Single nested state container for this screen.
     *
     * Nested inside the ViewModel so it is co-located with the code that manages it.
     * Not marked private so the Fragment can collect [state] and access its fields.
     *
     * Field semantics:
     * [isLoading]     — true while a full-screen shimmer should be shown (initial load / retry /
     *                   tab switch). [uiState] stays [UiState.Idle] during this time.
     * [isRefreshing]  — true while the pull-to-refresh spinner should be shown. The existing
     *                   [uiState] (typically [UiState.Success]) is kept visible underneath.
     * [isLoadingMore] — true while the pagination footer spinner should be shown. The existing
     *                   list stays visible.
     * [uiState]       — the actual data state. Only ever Idle / Success / Empty / Error here;
     *                   the three boolean flags above replace UiState.Loading entirely.
     * [podCounts]     — badge counts from the last successful API response.
     * [currentPodType]— currently selected tab (EPOD or HPOD).
     */
    data class ViewState(
        val uiState: UiState<List<HomeTripsItemData>> = UiState.Idle,
        val podCounts: PodCounts? = null,
        val currentPodType: PodType = PodType.EPOD,
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false
    )

    // ────────────────────────── Public flows ───────────────────────────

    private val _state = MutableStateFlow(ViewState())

    /** Single source of truth — Fragment collects this and calls per-field handlers. */
    val state: StateFlow<ViewState> = _state.asStateFlow()

    /**
     * One-time events (snackbars, navigation).
     * Kept separate from [state] because events must NOT survive screen rotation —
     * a snackbar should not re-appear when the user rotates the device.
     */
    private val _events = MutableSharedFlow<UiEvent>(replay = 0)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // ────────────────────────── Pagination (internal bookkeeping) ──────

    // Not in ViewState: pure fetch-loop counters, never observed by Fragment.
    private var currentOffset = 0
    private var hasMore = true
    private val pageSize = UserSearchLimit

    // ────────────────────────── Intent handler ─────────────────────────

    /**
     * Single entry point. Each intent maps 1-to-1 to an unambiguous action.
     */
    fun processIntent(intent: UserIntent) {
        when (intent) {
            is UserIntent.InitialLoad    -> fetchTrips(paginate = false, isRefreshing = false)
            is UserIntent.SwipeToRefresh -> fetchTrips(paginate = false, isRefreshing = true)
            is UserIntent.Retry          -> fetchTrips(paginate = false, isRefreshing = false)
            is UserIntent.LoadMore       -> fetchTrips(paginate = true, isRefreshing = false) // loadMore() // no more used as pagingtion and "!hasMore" check is present in "fetchTrips" function
            is UserIntent.Search         -> Unit   // not used on this screen
            is UserIntent.Refresh        -> fetchTrips(paginate = false, isRefreshing = false)
        }
    }

    // ────────────────────────── Pod type switching ─────────────────────

    fun switchPodType(podType: PodType) {
        if (_state.value.currentPodType == podType) return
        //V.IMP.
        //ALWAYS use named argument for updating any state to stateFlow
        _state.update { it.copy(currentPodType = podType) }
        //
        resetPagination()
        //
        fetchTrips(paginate = false, isRefreshing = false)
    }

    // ────────────────────────── Core fetch logic ───────────────────────
    //This function is no more required as "fetchTrips()" is capable of handling the pagination request as well.
    private fun loadMore() {
        //check if no next page exists. return control flow
        if (!hasMore) return
        //
        fetchTrips(paginate = true, isRefreshing = false)
    }

    private fun resetPagination() {
        currentOffset = 0
        hasMore = true
    }

    /**
     * The only function that mutates loading flags.
     * It sets the correct flag(s) exactly once, before the network call.
     * [handleResource] never touches loading flags on [Resource.Loading] — that emission
     * is ignored so the flag values set here survive through to the Fragment.
     *
     * Loading flag logic:
     * - Fresh load (not paginating, not refreshing) → [isLoading] = true
     * - Pull-to-refresh → [isRefreshing] = true
     * - Pagination → [isLoadingMore] = true
     * The other two flags are always cleared to false when one is set.
     */
    private fun fetchTrips(paginate: Boolean, isRefreshing: Boolean) {
        //check if this is a pagination request and if no next page exists ==>> return control flow
        if (paginate && !hasMore) return

        viewModelScope.launch {
            if (paginate) {
                // Pagination — keep existing list, only show footer spinner.
                _state.update { it.copy(isLoadingMore = true, isLoading = false, isRefreshing = false) }
            } else {
                resetPagination()
                _state.update {
                    it.copy(
                        isLoading     = true,          // shimmer for all fresh loads including swipe-to-refresh
                        isRefreshing  = false,
                        isLoadingMore = false,
                        uiState       = UiState.Idle
                    )
                }
            }
        }
    }

    /**
     * Handles Success and Failure results.
     * Loading flags are always cleared here along with the final data state,
     * so the Fragment sees a single atomic update: "done loading + here's the data".
     */
    private suspend fun handleResource(resource: Resource<SearchTripsResponse>, paginate: Boolean) {
        when (resource) {
            // Ignored: flags were set in fetchTrips() and must not be overwritten.
            is Resource.Loading -> Unit

            is Resource.Success -> {
                val newTrips = filterTripsByPodType(resource.data?.trips ?: emptyList())
                val freshCounts = resource.data?.podCounts

                if (paginate) {
                    // Append new page to the existing successful list.
                    val current = _state.value.uiState
                    if (current is UiState.Success) {
                        val combined = current.data + newTrips
                        currentOffset = combined.size
                        hasMore = resource.data?.hasNext ?: false
                        _state.update {
                            it.copy(
                                isLoading     = false,
                                isRefreshing  = false,
                                isLoadingMore = false,
                                podCounts     = freshCounts ?: it.podCounts,
                                uiState       = UiState.Success(data = combined, hasMore = hasMore)
                            )
                        }
                    }
                } else {
                    // Fresh load or pull-to-refresh.
                    currentOffset = newTrips.size
                    hasMore = resource.data?.hasNext ?: false
                    _state.update {
                        it.copy(
                            isLoading     = false,
                            isRefreshing  = false,
                            isLoadingMore = false,
                            podCounts     = freshCounts ?: it.podCounts,
                            uiState       = if (newTrips.isEmpty())
                                UiState.Empty("No pending POD items found")
                            else
                                UiState.Success(data = newTrips, hasMore = hasMore)
                        )
                    }
                }
            }

            is Resource.Failure -> {
                val current = _state.value.uiState
                if (current is UiState.Success && paginate) {
                    // Preserve the current list; just hide the footer spinner.
                    _state.update { it.copy(isLoadingMore = false) }
                    _events.emit(
                        UiEvent.ShowSnackbar(
                            message = "Failed to load more: ${getErrorMessage(resource.apiError)}",
                            action = "Retry",
                            onActionClick = { processIntent(UserIntent.LoadMore) }
                        )
                    )
                } else {
                    _state.update {
                        it.copy(
                            isLoading     = false,
                            isRefreshing  = false,
                            isLoadingMore = false,
                            uiState       = UiState.Error(
                                apiError      = resource.apiError,
                                message       = getErrorMessage(resource.apiError),
                                isNetworkError = resource.isNetworkError
                            )
                        )
                    }
                }
            }
        }
    }

    // ────────────────────────── Filtering ──────────────────────────────

    private fun filterTripsByPodType(trips: List<HomeTripsItemData>): List<HomeTripsItemData> =
        when (_state.value.currentPodType) {
            PodType.EPOD -> trips.filter {
                it.tripStatus == TruckUnloaded.statusKey && !it.hasPODTracking()
            }
            PodType.HPOD -> trips.filter {
                !it.hasPODTracking() &&
                (it.tripStatus == EPodUploaded.statusKey || it.tripStatus == TruckUnloaded.statusKey)
            }
        }

    // ────────────────────────── Request builder ─────────────────────────

    private fun buildRequest(offset: Int): com.google.gson.JsonObject {
        val request = SearchRequest()
        request.offset   = offset
        request.limit    = pageSize
        request.vendorId = userRepository.userId()

        when (_state.value.currentPodType) {
            PodType.EPOD -> {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DATE, -14)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                request.tripStatus = TruckUnloaded.statusKey
                request.value      = DateUtils.formatDate(cal.time, OrionDateFormat)
            }
            PodType.HPOD -> {
                request.tripStatus = "${EPodUploaded.statusKey},${TruckUnloaded.statusKey}"
                request.value      = null
            }
        }

        return request.getRequest()
    }

    // ────────────────────────── Error mapping ───────────────────────────

    private fun getErrorMessage(apiError: ApiError): String = when (apiError) {
        ApiError.Network            -> "No internet connection. Please check your network."
        ApiError.Timeout            -> "Request timed out. Please try again."
        ApiError.Unauthorized       -> "Session expired. Please log in again."
        ApiError.AccessDenied       -> "You don't have permission to access this resource."
        ApiError.NotFound           -> "Resource not found."
        ApiError.ServiceUnavailable -> "Service temporarily unavailable. Please try again later."
        ApiError.Unknown            -> "An unexpected error occurred. Please try again."
    }
}
