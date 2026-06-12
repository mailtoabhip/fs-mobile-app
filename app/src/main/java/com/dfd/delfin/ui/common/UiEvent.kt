package com.dfd.delfin.ui.common

import android.os.Bundle

/**
 * Sealed class for one-time UI events that should be consumed only once.
 * 
 * These events are emitted via SharedFlow with replay=0, ensuring they are:
 * - Consumed only once per emission
 * - Not replayed after configuration changes (e.g., screen rotation)
 * - Lost if no collector is active when emitted
 * 
 * This prevents duplicate toasts, navigation actions, or snackbars after
 * configuration changes, which is a common issue with LiveData or StateFlow.
 * 
 * Usage in ViewModel:
 * ```
 * private val _events = MutableSharedFlow<UiEvent>(replay = 0)
 * val events: SharedFlow<UiEvent> = _events.asSharedFlow()
 * 
 * suspend fun showMessage(message: String) {
 *     _events.emit(UiEvent.ShowToast(message))
 * }
 * ```
 * 
 * Usage in Fragment:
 * ```
 * viewLifecycleOwner.lifecycleScope.launch {
 *     viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
 *         viewModel.events.collect { event ->
 *             when (event) {
 *                 is UiEvent.ShowToast -> // Show toast
 *                 is UiEvent.ShowSnackbar -> // Show snackbar
 *                 is UiEvent.Navigate -> // Navigate
 *             }
 *         }
 *     }
 * }
 * ```
 */
sealed class UiEvent {
    
    /**
     * Event to show a short toast message.
     * 
     * @param message The message to display in the toast
     * 
     * Example:
     * ```
     * UiEvent.ShowToast("Operation completed successfully")
     * ```
     */
    data class ShowToast(val message: String) : UiEvent()
    
    /**
     * Event to show a snackbar with an optional action button.
     * 
     * @param message The message to display in the snackbar
     * @param action Optional action button text (e.g., "Retry", "Undo")
     * @param onActionClick Optional callback to invoke when action button is clicked
     * 
     * Example:
     * ```
     * UiEvent.ShowSnackbar(
     *     message = "Failed to load data",
     *     action = "Retry",
     *     onActionClick = { viewModel.retry() }
     * )
     * ```
     */
    data class ShowSnackbar(
        val message: String,
        val action: String? = null,
        val onActionClick: (() -> Unit)? = null
    ) : UiEvent()
    
    /**
     * Event to navigate to another screen.
     * 
     * @param destination The navigation destination (route, action ID, or screen identifier)
     * @param args Optional bundle of arguments to pass to the destination
     * 
     * Example:
     * ```
     * UiEvent.Navigate(
     *     destination = "trip_details",
     *     args = Bundle().apply {
     *         putString("trip_id", tripId)
     *     }
     * )
     * ```
     */
    data class Navigate(
        val destination: String,
        val args: Bundle? = null
    ) : UiEvent()
}
