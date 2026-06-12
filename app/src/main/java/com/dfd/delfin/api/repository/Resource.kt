package com.dfd.delfin.api.repository

/**
 * Represents the result of an API operation with type-safe loading, success, and failure states.
 * This sealed class ensures exhaustive handling of all possible API call outcomes.
 *
 * @param T The type of data returned on success
 */
sealed class Resource<out T> {
    /**
     * Loading state indicating an API operation is in progress.
     * Emit this state before starting an API call to show loading UI.
     */
    object Loading : Resource<Nothing>()

    /**
     * Successful API response containing the result data.
     *
     * @param data The response data (nullable for empty body responses or operations that return no data)
     */
    data class Success<out T>(val data: T?) : Resource<T>()

    /**
     * Failed API response with detailed error information.
     *
     * @param isNetworkError True if the failure was due to network connectivity issues (IOException)
     * @param errorCode HTTP status code if available (null for network errors or non-HTTP exceptions)
     * @param apiError Categorized error type for consistent error handling in the UI
     * @param errorMessage Server error message if available
     */
    data class Failure(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val apiError: ApiError,
        val errorMessage: String? = null
    ) : Resource<Nothing>()
}
