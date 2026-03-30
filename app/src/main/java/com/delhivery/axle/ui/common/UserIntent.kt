package com.delhivery.axle.ui.common

import com.delhivery.axle.api.request.SearchRequest

/**
 * Sealed class representing all possible user intents/actions in the application.
 * 
 * In MVI architecture, user intents are explicit representations of user actions
 * that trigger state changes. This approach provides:
 * - Clear contract of what actions are possible
 * - Type-safe intent handling
 * - Easy testing and debugging
 * - Single entry point for all user actions
 * 
 * Usage:
 * ```
 * // In Fragment/Activity:
 * viewModel.processIntent(UserIntent.Refresh)
 * 
 * // In ViewModel:
 * fun processIntent(intent: UserIntent) {
 *     when (intent) {
 *         is UserIntent.Refresh -> handleRefresh()
 *         is UserIntent.LoadMore -> handleLoadMore()
 *         // ...
 *     }
 * }
 * ```
 */
sealed class UserIntent {
    
    /**
     * User initiates a new search with specific parameters.
     * This is typically triggered on initial load or when search criteria change.
     */
    data class Search(val request: SearchRequest) : UserIntent()
    
    /**
     * User pulls down to refresh the current data.
     * This resets pagination and fetches fresh data.
     */
    object Refresh : UserIntent()
    
    /**
     * User scrolls to the bottom and triggers load more.
     * This appends new data to the existing list.
     */
    object LoadMore : UserIntent()
    
    /**
     * User taps retry button after an error.
     * This re-executes the last failed request.
     */
    object Retry : UserIntent()

    /**
     * Fragment Just Opened
     */
    object InitialLoad : UserIntent()

    /**
     * User swipes to refresh
     */
    object SwipeToRefresh : UserIntent()
}
