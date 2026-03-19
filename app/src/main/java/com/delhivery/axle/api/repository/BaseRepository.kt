package com.delhivery.axle.api.repository

import android.util.Log
import com.delhivery.axle.utils.ErrorLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Abstract base repository providing safe API call wrappers with comprehensive error handling.
 * All repository classes should extend this to inherit consistent exception-to-ApiError mapping.
 *
 * @param errorLogger Injectable error logger for recording exceptions
 */
abstract class BaseRepository(
    private val errorLogger: ErrorLogger
) {

    /**
     * Wraps an API call with comprehensive exception handling and maps exceptions to Resource.
     * This function ensures consistent error handling across all API calls in the application.
     *
     * Exception handling:
     * - CancellationException: Rethrown to respect coroutine cancellation
     * - SocketTimeoutException: Mapped to ApiError.Timeout
     * - IOException: Mapped to ApiError.Network
     * - HttpException: Mapped to appropriate ApiError based on HTTP status code
     * - Other exceptions: Mapped to ApiError.Unknown
     *
     * @param T The type of data returned on success
     * @param apiCall Suspend lambda containing the API call to execute
     * @return Resource.Success with data on success, Resource.Failure with ApiError on failure
     */
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Resource<T> {
        return try {
            Resource.Success(apiCall())
        } catch (e: CancellationException) {
            // Respect coroutine cancellation - rethrow
            throw e
        } catch (e: SocketTimeoutException) {
            errorLogger.log(e)
            Log.e("BaseRepository", "SocketTimeoutException in safeApiCall", e)
            Resource.Failure(
                isNetworkError = true,
                errorCode = null,
                apiError = ApiError.Timeout
            )
        } catch (e: IOException) {
            errorLogger.log(e)
            Log.e("BaseRepository", "IOException in safeApiCall", e)
            Resource.Failure(
                isNetworkError = true,
                errorCode = null,
                apiError = ApiError.Network
            )
        } catch (e: HttpException) {
            errorLogger.log(e)
            Log.e("BaseRepository", "HttpException in safeApiCall: code=${e.code()}, message=${e.message()}", e)
            Resource.Failure(
                isNetworkError = false,
                errorCode = e.code(),
                apiError = mapHttpCodeToApiError(e.code())
            )
        } catch (e: Exception) {
            errorLogger.log(e)
            Log.e("BaseRepository", "Unknown exception in safeApiCall: ${e.javaClass.simpleName} - ${e.printStackTrace()}", e)
            Resource.Failure(
                isNetworkError = false,
                errorCode = null,
                apiError = ApiError.Unknown
            )
        }
    }

    /**
     * Wraps an API call in a Flow that emits Loading, then Success or Failure.
     * This function enables reactive data handling with proper error handling and dispatcher management.
     *
     * Flow emission sequence:
     * 1. Resource.Loading - emitted before API call starts
     * 2. Resource.Success or Resource.Failure - emitted based on API call result
     *
     * Exception handling:
     * - CancellationException: Rethrown to respect coroutine cancellation
     * - SocketTimeoutException: Mapped to ApiError.Timeout
     * - IOException: Mapped to ApiError.Network
     * - HttpException: Mapped to appropriate ApiError based on HTTP status code
     * - Other exceptions: Mapped to ApiError.Unknown
     *
     * The Flow uses flowOn(Dispatchers.IO) to execute network operations on the IO dispatcher.
     *
     * @param T The type of data returned on success
     * @param apiCall Suspend lambda containing the API call to execute
     * @return Flow<Resource<T>> that emits Loading, then Success or Failure
     */
    fun <T> safeApiCallFlow(
        apiCall: suspend () -> T
    ): Flow<Resource<T>> = flow {
        emit(Resource.Loading)
        try {
            emit(Resource.Success(apiCall()))
        } catch (e: CancellationException) {
            // Respect coroutine cancellation - rethrow
            throw e
        } catch (e: SocketTimeoutException) {
            errorLogger.log(e)
            emit(Resource.Failure(
                isNetworkError = true,
                errorCode = null,
                apiError = ApiError.Timeout
            ))
        } catch (e: IOException) {
            errorLogger.log(e)
            emit(Resource.Failure(
                isNetworkError = true,
                errorCode = null,
                apiError = ApiError.Network
            ))
        } catch (e: HttpException) {
            errorLogger.log(e)
            emit(Resource.Failure(
                isNetworkError = false,
                errorCode = e.code(),
                apiError = mapHttpCodeToApiError(e.code())
            ))
        } catch (e: Exception) {
            errorLogger.log(e)
            emit(Resource.Failure(
                isNetworkError = false,
                errorCode = null,
                apiError = ApiError.Unknown
            ))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Maps HTTP status codes to appropriate ApiError enum values.
     *
     * @param code HTTP status code from HttpException
     * @return Corresponding ApiError enum value
     */
    private fun mapHttpCodeToApiError(code: Int): ApiError = when (code) {
        401 -> ApiError.Unauthorized
        403 -> ApiError.AccessDenied
        404 -> ApiError.NotFound
        503 -> ApiError.ServiceUnavailable
        else -> ApiError.Unknown
    }

    /**
     * Executes two API calls in parallel and combines their results using a transform function.
     * Both calls execute concurrently using structured concurrency (coroutineScope + async).
     *
     * If any call fails, the exception is caught and mapped to Resource.Failure.
     * All child coroutines are cancelled if one fails (structured concurrency).
     *
     * @param T1 Type of first API call result
     * @param T2 Type of second API call result
     * @param R Type of transformed result
     * @param call1 First suspend lambda to execute
     * @param call2 Second suspend lambda to execute
     * @param transform Function to combine both results into final result
     * @return Resource.Success with transformed data on success, Resource.Failure on any failure
     */
    suspend fun <T1, T2, R> parallelApiCall2(
        call1: suspend () -> T1,
        call2: suspend () -> T2,
        transform: (T1, T2) -> R
    ): Resource<R> {
        return try {
            coroutineScope {
                val deferred1 = async { call1() }
                val deferred2 = async { call2() }
                Resource.Success(transform(deferred1.await(), deferred2.await()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SocketTimeoutException) {
            errorLogger.log(e)
            Resource.Failure(
                isNetworkError = true,
                errorCode = null,
                apiError = ApiError.Timeout
            )
        } catch (e: IOException) {
            errorLogger.log(e)
            Resource.Failure(
                isNetworkError = true,
                errorCode = null,
                apiError = ApiError.Network
            )
        } catch (e: HttpException) {
            errorLogger.log(e)
            Resource.Failure(
                isNetworkError = false,
                errorCode = e.code(),
                apiError = mapHttpCodeToApiError(e.code())
            )
        } catch (e: Exception) {
            errorLogger.log(e)
            Resource.Failure(
                isNetworkError = false,
                errorCode = null,
                apiError = ApiError.Unknown
            )
        }
    }

    /**
     * Executes three API calls in parallel and combines their results using a transform function.
     * All three calls execute concurrently using structured concurrency (coroutineScope + async).
     *
     * If any call fails, the exception is caught and mapped to Resource.Failure.
     * All child coroutines are cancelled if one fails (structured concurrency).
     *
     * @param T1 Type of first API call result
     * @param T2 Type of second API call result
     * @param T3 Type of third API call result
     * @param R Type of transformed result
     * @param call1 First suspend lambda to execute
     * @param call2 Second suspend lambda to execute
     * @param call3 Third suspend lambda to execute
     * @param transform Function to combine all three results into final result
     * @return Resource.Success with transformed data on success, Resource.Failure on any failure
     */
    suspend fun <T1, T2, T3, R> parallelApiCall3(
        call1: suspend () -> T1,
        call2: suspend () -> T2,
        call3: suspend () -> T3,
        transform: (T1, T2, T3) -> R
    ): Resource<R> {
        return try {
            coroutineScope {
                val d1 = async { call1() }
                val d2 = async { call2() }
                val d3 = async { call3() }
                Resource.Success(transform(d1.await(), d2.await(), d3.await()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SocketTimeoutException) {
            errorLogger.log(e)
            Resource.Failure(
                isNetworkError = true,
                errorCode = null,
                apiError = ApiError.Timeout
            )
        } catch (e: IOException) {
            errorLogger.log(e)
            Resource.Failure(
                isNetworkError = true,
                errorCode = null,
                apiError = ApiError.Network
            )
        } catch (e: HttpException) {
            errorLogger.log(e)
            Resource.Failure(
                isNetworkError = false,
                errorCode = e.code(),
                apiError = mapHttpCodeToApiError(e.code())
            )
        } catch (e: Exception) {
            errorLogger.log(e)
            Resource.Failure(
                isNetworkError = false,
                errorCode = null,
                apiError = ApiError.Unknown
            )
        }
    }
}
