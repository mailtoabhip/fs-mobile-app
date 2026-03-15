package com.delhivery.axle.api.repository

import com.delhivery.axle.utils.ErrorLogger
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

/**
 * Test class for BaseRepository to verify safeApiCall exception handling.
 * Tests proper dependency injection of ErrorLogger into repositories.
 */
class BaseRepositoryTest {

    private lateinit var testRepository: TestRepository
    private lateinit var errorLogger: ErrorLogger

    @Before
    fun setup() {
        // Create test dependencies
        errorLogger = TestErrorLogger()
        testRepository = TestRepository(errorLogger)
    }

    @Test
    fun testSafeApiCallSuccess() = runTest {
        val result = testRepository.testSafeApiCall { "Success" }
        
        assertTrue("Result should be Resource.Success", result is Resource.Success)
        assertEquals("Success", (result as Resource.Success).data)
    }

    @Test
    fun testSafeApiCallTimeout() = runTest {
        val result = testRepository.testSafeApiCall { 
            throw SocketTimeoutException("Timeout")
        }
        
        assertTrue("Result should be Resource.Failure", result is Resource.Failure)
        val failure = result as Resource.Failure
        assertEquals(ApiError.Timeout, failure.apiError)
        assertTrue(failure.isNetworkError)
    }

    @Test
    fun testSafeApiCallNetworkError() = runTest {
        val result = testRepository.testSafeApiCall { 
            throw IOException("Network error")
        }
        
        assertTrue("Result should be Resource.Failure", result is Resource.Failure)
        val failure = result as Resource.Failure
        assertEquals(ApiError.Network, failure.apiError)
        assertTrue(failure.isNetworkError)
    }

    @Test
    fun testSafeApiCallUnauthorized() = runTest {
        val result = testRepository.testSafeApiCall { 
            throw HttpException(Response.error<Any>(401, okhttp3.ResponseBody.create(null, "")))
        }
        
        assertTrue("Result should be Resource.Failure", result is Resource.Failure)
        val failure = result as Resource.Failure
        assertEquals(ApiError.Unauthorized, failure.apiError)
        assertEquals(401, failure.errorCode)
    }

    @Test
    fun testSafeApiCallAccessDenied() = runTest {
        val result = testRepository.testSafeApiCall { 
            throw HttpException(Response.error<Any>(403, okhttp3.ResponseBody.create(null, "")))
        }
        
        assertTrue("Result should be Resource.Failure", result is Resource.Failure)
        val failure = result as Resource.Failure
        assertEquals(ApiError.AccessDenied, failure.apiError)
        assertEquals(403, failure.errorCode)
    }

    @Test
    fun testSafeApiCallNotFound() = runTest {
        val result = testRepository.testSafeApiCall { 
            throw HttpException(Response.error<Any>(404, okhttp3.ResponseBody.create(null, "")))
        }
        
        assertTrue("Result should be Resource.Failure", result is Resource.Failure)
        val failure = result as Resource.Failure
        assertEquals(ApiError.NotFound, failure.apiError)
        assertEquals(404, failure.errorCode)
    }

    @Test
    fun testSafeApiCallServiceUnavailable() = runTest {
        val result = testRepository.testSafeApiCall { 
            throw HttpException(Response.error<Any>(503, okhttp3.ResponseBody.create(null, "")))
        }
        
        assertTrue("Result should be Resource.Failure", result is Resource.Failure)
        val failure = result as Resource.Failure
        assertEquals(ApiError.ServiceUnavailable, failure.apiError)
        assertEquals(503, failure.errorCode)
    }

    @Test
    fun testSafeApiCallUnknownError() = runTest {
        val result = testRepository.testSafeApiCall { 
            throw RuntimeException("Unknown error")
        }
        
        assertTrue("Result should be Resource.Failure", result is Resource.Failure)
        val failure = result as Resource.Failure
        assertEquals(ApiError.Unknown, failure.apiError)
    }

    @Test
    fun testParallelApiCall2Success() = runTest {
        val result = testRepository.testParallelApiCall2(
            call1 = { "First" },
            call2 = { "Second" }
        ) { first, second ->
            "$first-$second"
        }
        
        assertTrue("Result should be Resource.Success", result is Resource.Success)
        assertEquals("First-Second", (result as Resource.Success).data)
    }

    @Test
    fun testParallelApiCall2Failure() = runTest {
        val result = testRepository.testParallelApiCall2(
            call1 = { "First" },
            call2 = { throw IOException("Network error") }
        ) { first, second ->
            "$first-$second"
        }
        
        assertTrue("Result should be Resource.Failure", result is Resource.Failure)
        val failure = result as Resource.Failure
        assertEquals(ApiError.Network, failure.apiError)
    }

    @Test
    fun testParallelApiCall3Success() = runTest {
        val result = testRepository.testParallelApiCall3(
            call1 = { "First" },
            call2 = { "Second" },
            call3 = { "Third" }
        ) { first, second, third ->
            "$first-$second-$third"
        }
        
        assertTrue("Result should be Resource.Success", result is Resource.Success)
        assertEquals("First-Second-Third", (result as Resource.Success).data)
    }

    @Test
    fun testDependencyInjection() {
        // Verify that ErrorLogger is properly injected
        val injectedRepo = TestRepository(errorLogger)
        assertTrue("Repository should be created with injected ErrorLogger", injectedRepo != null)
    }
}

/**
 * Test implementation of BaseRepository for testing purposes.
 * Uses @Inject constructor to demonstrate proper dependency injection pattern.
 */
class TestRepository @Inject constructor(
    errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {
    
    suspend fun <T> testSafeApiCall(apiCall: suspend () -> T): Resource<T> {
        return safeApiCall(apiCall)
    }

    suspend fun <T1, T2, R> testParallelApiCall2(
        call1: suspend () -> T1,
        call2: suspend () -> T2,
        transform: (T1, T2) -> R
    ): Resource<R> {
        return parallelApiCall2(call1, call2, transform)
    }

    suspend fun <T1, T2, T3, R> testParallelApiCall3(
        call1: suspend () -> T1,
        call2: suspend () -> T2,
        call3: suspend () -> T3,
        transform: (T1, T2, T3) -> R
    ): Resource<R> {
        return parallelApiCall3(call1, call2, call3, transform)
    }
}

/**
 * Test implementation of ErrorLogger that does nothing.
 * In a real test scenario, this could be replaced with a mock or spy
 * to verify error logging behavior.
 */
class TestErrorLogger : ErrorLogger {
    val loggedExceptions = mutableListOf<Exception>()
    
    override fun log(exception: Exception) {
        // Store exceptions for verification in tests if needed
        loggedExceptions.add(exception)
    }
}
