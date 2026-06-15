package com.dfd.delfin.ui.common

import com.dfd.delfin.api.repository.ApiError

/**
 * Sealed class representing all possible UI states for API-driven screens.
 * This provides type-safe exhaustive handling of all states in the UI layer.
 *
 * The sealed class enforces exhaustive when expressions, ensuring all states
 * are handled at compile-time. This prevents runtime errors from missing state cases.
 *
 * @param T The type of data being displayed
 *
 * Usage example:
 * ```
 * when (state) {
 *     is UiState.Idle -> // Handle initial state
 *     is UiState.Loading -> // Show loading indicator
 *     is UiState.Success -> // Display data
 *     is UiState.Empty -> // Show empty state
 *     is UiState.Error -> // Show error message
 * }
 * ```
 */
sealed class UiState<out T> {
    
    /**
     * Initial state before any data is loaded.
     * Use this as the default state when the screen is first created.
     */
    object Idle : UiState<Nothing>()
    
    /**
     * Loading state indicating an API operation is in progress.
     * 
     * @param isRefreshing True if this is a pull-to-refresh operation.
     *                     When true, the UI should show the refresh indicator
     *                     instead of a full-screen loading state.
     */
    data class Loading(val isRefreshing: Boolean = false) : UiState<Nothing>()
    
    /**
     * Successful state with data.
     * 
     * @param data The loaded data to be displayed
     * @param isLoadingMore True if loading additional paginated data.
     *                      When true, the UI should show a footer loading indicator
     *                      while preserving the existing data.
     * @param hasMore True if more data is available for pagination.
     *                When false, the UI should not trigger additional load more requests.
     */
    data class Success<T>(
        val data: T,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = false
    ) : UiState<T>()
    
    /**
     * Empty state when API returns successfully but with no data.
     * Use this to show a user-friendly empty state message instead of an empty list.
     * 
     * @param message Optional message to display to the user.
     *                Defaults to "No data available" if not specified.
     */
    data class Empty(val message: String = "No data available") : UiState<Nothing>()
    
    /**
     * Error state when API call fails.
     * 
     * @param apiError The categorized error type (Network, Timeout, Unauthorized, etc.)
     * @param message User-friendly error message to display
     * @param isNetworkError True if the error is network-related (no connection, timeout).
     *                       This can be used to show network-specific UI or suggestions.
     */
    data class Error(
        val apiError: ApiError,
        val message: String,
        val isNetworkError: Boolean
    ) : UiState<Nothing>()
}
