package com.dfd.delfin.utils.extensions

import com.dfd.delfin.api.repository.ApiError
import com.dfd.delfin.api.repository.Resource
import com.dfd.delfin.api.response.BaseResponse
import com.dfd.delfin.api.response.toResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Executes a suspend API call and wraps the result in Flow<Resource<T>>.
 * This utility provides consistent error handling and Loading state emission
 * for all API calls in the application.
 *
 * Flow emission sequence:
 * 1. Resource.Loading - emitted immediately before the API call
 * 2. Resource.Success or Resource.Failure - emitted when API call completes
 *
 * Exception handling:
 * - CancellationException: Rethrown to respect coroutine cancellation
 * - SocketTimeoutException: Mapped to ApiError.Timeout
 * - IOException: Mapped to ApiError.Network
 * - HttpException: Mapped to appropriate ApiError based on HTTP status code
 * - Other exceptions: Mapped to ApiError.Unknown
 *
 * The function executes on Dispatchers.IO to avoid blocking the main thread.
 *
 * @param T The type of data expected in the API response
 * @param apiCall Suspend function that makes the API call and returns BaseResponse<T>
 * @return Flow that emits Resource.Loading, then Resource.Success or Resource.Failure
 *
 * Usage example:
 * ```
 * fun searchTripsFlow(request: JsonObject): Flow<Resource<SearchTripsResponse>> {
 *     return safeApiCallFlow { loadsService.searchTrips(request) }
 * }
 * ```
 */
fun <T : Any> safeApiCallFlowExtension(apiCall: suspend () -> BaseResponse<T>): Flow<Resource<T>> = flow {
    // Emit Loading state immediately
    emit(Resource.Loading)
    
    try {
        // Execute the suspend API call
        val response = apiCall()
        
        // Use existing toResource() extension to unwrap BaseResponse
        // This maintains consistency with existing error handling logic
        val data = response.toResource()
        emit(Resource.Success(data))
        
    } catch (e: CancellationException) {
        // Respect coroutine cancellation - rethrow to propagate cancellation
        // This ensures proper cleanup when the coroutine is cancelled
        throw e
        
    } catch (e: SocketTimeoutException) {
        // Request timed out - network is slow or server is not responding
        emit(Resource.Failure(
            isNetworkError = true,
            errorCode = null,
            apiError = ApiError.Timeout
        ))
        
    } catch (e: IOException) {
        // Network error - no internet connection or network unavailable
        emit(Resource.Failure(
            isNetworkError = true,
            errorCode = null,
            apiError = ApiError.Network
        ))
        
    } catch (e: HttpException) {
        // HTTP error - server returned an error status code
        emit(Resource.Failure(
            isNetworkError = false,
            errorCode = e.code(),
            apiError = mapHttpCodeToApiError(e.code())
        ))
        
    } catch (e: Exception) {
        // Unknown error - catch-all for unexpected exceptions
        emit(Resource.Failure(
            isNetworkError = false,
            errorCode = null,
            apiError = ApiError.Unknown
        ))
    }
}.flowOn(Dispatchers.IO) // Execute on IO dispatcher to avoid blocking main thread

/**
 * Maps HTTP status codes to ApiError enum values.
 * Reuses the same mapping logic from BaseRepository for consistency.
 *
 * @param code HTTP status code from the server response
 * @return Corresponding ApiError enum value
 */
private fun mapHttpCodeToApiError(code: Int): ApiError = when (code) {
    401 -> ApiError.Unauthorized      // Authentication required or session expired
    403 -> ApiError.AccessDenied      // User doesn't have permission
    404 -> ApiError.NotFound          // Resource not found
    503 -> ApiError.ServiceUnavailable // Server temporarily unavailable
    else -> ApiError.Unknown          // Other HTTP errors
}
