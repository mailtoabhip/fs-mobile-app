package com.dfd.delfin.utils.extensions

import android.content.Context
import com.dfd.delfin.R
import com.dfd.delfin.api.repository.ApiError
import com.dfd.delfin.api.repository.Resource

/**
 * Extension functions for Resource sealed class to simplify UI handling.
 * These utilities help with exhaustive when expressions and error message mapping.
 */

/**
 * Maps ApiError to user-friendly error message resource ID.
 * Use this to display appropriate error messages in the UI.
 *
 * Example usage:
 * ```
 * when (resource) {
 *     is Resource.Failure -> {
 *         val messageResId = resource.apiError.toErrorMessageResId()
 *         showError(getString(messageResId))
 *     }
 * }
 * ```
 *
 * @return String resource ID for the error message
 */
fun ApiError.toErrorMessageResId(): Int = when (this) {
    ApiError.Timeout -> R.string.error_timeout
    ApiError.Network -> R.string.error_network
    ApiError.Unauthorized -> R.string.error_unauthorized
    ApiError.AccessDenied -> R.string.error_access_denied
    ApiError.NotFound -> R.string.error_not_found
    ApiError.ServiceUnavailable -> R.string.error_service_unavailable
    ApiError.Unknown -> R.string.error_unknown
}

/**
 * Gets user-friendly error message string from ApiError.
 * Convenience method that combines toErrorMessageResId() with getString().
 *
 * Example usage:
 * ```
 * when (resource) {
 *     is Resource.Failure -> {
 *         val message = resource.apiError.toErrorMessage(context)
 *         showError(message)
 *     }
 * }
 * ```
 *
 * @param context Android context for string resource access
 * @return Localized error message string
 */
fun ApiError.toErrorMessage(context: Context): String {
    return context.getString(toErrorMessageResId())
}

/**
 * Executes the given block if Resource is Success.
 * Provides a cleaner syntax for handling success cases.
 *
 * Example usage:
 * ```
 * resource.onSuccess { data ->
 *     updateUI(data)
 * }
 * ```
 */
inline fun <T> Resource<T>.onSuccess(block: (T?) -> Unit): Resource<T> {
    if (this is Resource.Success) {
        block(data)
    }
    return this
}

/**
 * Executes the given block if Resource is Failure.
 * Provides a cleaner syntax for handling error cases.
 *
 * Example usage:
 * ```
 * resource.onFailure { apiError, isNetworkError ->
 *     if (isNetworkError) showNetworkError()
 *     else showError(apiError.toErrorMessage(context))
 * }
 * ```
 */
inline fun <T> Resource<T>.onFailure(block: (ApiError, Boolean) -> Unit): Resource<T> {
    if (this is Resource.Failure) {
        block(apiError, isNetworkError)
    }
    return this
}

/**
 * Chains success and failure handlers for fluent API.
 *
 * Example usage:
 * ```
 * resource
 *     .onSuccess { data -> updateUI(data) }
 *     .onFailure { error, _ -> showError(error.toErrorMessage(context)) }
 * ```
 */
