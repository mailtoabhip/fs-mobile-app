package com.dfd.delfin.ui.example

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dfd.delfin.api.repository.ExampleCombinedData
import com.dfd.delfin.api.repository.ExampleRepository
import com.dfd.delfin.api.repository.ExampleUserData
import com.dfd.delfin.api.repository.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Example ViewModel demonstrating the coroutine-based API pattern with Resource states.
 * This serves as a reference implementation for migrating existing ViewModels from RxJava to coroutines.
 *
 * Key patterns demonstrated:
 * 1. Exposing LiveData of type Resource<T> for API operations
 * 2. Using viewModelScope.launch for coroutine launching
 * 3. Emitting Resource.Loading before API call
 * 4. Emitting Resource result (Success/Failure) after API call completes
 *
 * Migration guide:
 * - Replace RxJava disposables with viewModelScope (auto-cancelled on ViewModel clear)
 * - Replace CompositeDisposable with viewModelScope.launch
 * - Observe Resource<T> instead of handling onSuccess/onError callbacks
 * - Use when expression in UI to handle Resource.Loading, Resource.Success, and Resource.Failure
 */
class ExampleViewModel @Inject constructor(
    private val repository: ExampleRepository
) : ViewModel() {

    // LiveData for user data API operation
    private val _userDataState = MutableLiveData<Resource<ExampleUserData>>()
    val userDataState: LiveData<Resource<ExampleUserData>> = _userDataState

    // LiveData for combined data API operation
    private val _combinedDataState = MutableLiveData<Resource<ExampleCombinedData>>()
    val combinedDataState: LiveData<Resource<ExampleCombinedData>> = _combinedDataState


    /**
     * Example: Fetch user data with Resource.Loading state.
     *
     * Pattern:
     * 1. Emit Resource.Loading before API call
     * 2. Launch coroutine in viewModelScope
     * 3. Call repository suspend function
     * 4. Post result (Success or Failure) to LiveData
     *
     * Usage in Activity/Fragment:
     * ```
     * viewModel.userDataState.observe(viewLifecycleOwner) { resource ->
     *     when (resource) {
     *         is Resource.Loading -> {
     *             // Show loading indicator
     *             progressBar.visibility = View.VISIBLE
     *         }
     *         is Resource.Success -> {
     *             // Hide loading and handle success
     *             progressBar.visibility = View.GONE
     *             val userData = resource.data
     *         }
     *         is Resource.Failure -> {
     *             // Hide loading and handle error
     *             progressBar.visibility = View.GONE
     *             handleError(resource.apiError)
     *         }
     *     }
     * }
     *
     * // Trigger the API call
     * viewModel.fetchUserData()
     * ```
     */
    fun fetchUserData() {
        viewModelScope.launch {
            _userDataState.value = Resource.Loading
            val result = repository.fetchUserData()
            _userDataState.value = result
        }
    }

    /**
     * Example: Fetch combined data from parallel API calls.
     *
     * Pattern is the same - emit Loading, then emit result.
     * The repository handles parallelization internally.
     */
    fun fetchCombinedData() {
        viewModelScope.launch {
            _combinedDataState.value = Resource.Loading
            val result = repository.fetchCombinedData()
            _combinedDataState.value = result
        }
    }
}
