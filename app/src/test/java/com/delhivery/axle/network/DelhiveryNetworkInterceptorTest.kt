package com.delhivery.axle.network

import android.util.Log
import com.delhivery.axle.testutils.relaxedMock
import com.delhivery.axle.utils.prefs.UserPrefs
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Unit tests for DelhiveryNetworkInterceptor using Kotest and MockK.
 *
 * Covers:
 * - Authorization header injection
 * - X-User-Id header injection
 * - Accept header presence
 * - JWT token update behavior
 * - No internet connection handling
 */
class DelhiveryNetworkInterceptorTest : FunSpec({

    lateinit var userPrefs: UserPrefs
    lateinit var connectionLiveData: ConnectionLiveData
    lateinit var interceptor: DelhiveryNetworkInterceptor

    beforeSpec {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    afterSpec {
        unmockkStatic(Log::class)
    }

    beforeEach {
        userPrefs = relaxedMock()
        connectionLiveData = relaxedMock()
        every { connectionLiveData.isConnected() } returns true
        every { userPrefs.jwtToken } returns "test_jwt_token"
        every { userPrefs.userId() } returns "USER123"
        interceptor = DelhiveryNetworkInterceptor(userPrefs, connectionLiveData)
    }

    /**
     * Helper to create a mock chain that captures the request and returns a dummy response.
     */
    fun createMockChain(url: String = "https://api.example.com/test"): Pair<Interceptor.Chain, () -> Request> {
        var capturedRequest: Request? = null
        val chain = mockk<Interceptor.Chain> {
            every { request() } returns Request.Builder().url(url).build()
            every { proceed(any()) } answers {
                capturedRequest = firstArg()
                Response.Builder()
                    .request(capturedRequest!!)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .build()
            }
        }
        return Pair(chain) { capturedRequest!! }
    }

    // ==================== Authorization Header Tests ====================

    context("authorization header") {

        test("adds bearer token when jwt is present") {
            interceptor.updateJWT("my_jwt_token")
            val (chain, getRequest) = createMockChain()

            interceptor.intercept(chain)

            getRequest().header("Authorization") shouldBe "Bearer my_jwt_token"
        }

        test("does not add authorization header when jwt is null") {
            interceptor.updateJWT(null)
            val (chain, getRequest) = createMockChain()

            interceptor.intercept(chain)

            getRequest().header("Authorization").shouldBeNull()
        }

        test("does not add authorization header when jwt is empty") {
            every { userPrefs.jwtToken } returns ""
            val freshInterceptor = DelhiveryNetworkInterceptor(userPrefs, connectionLiveData)
            freshInterceptor.updateJWT("")
            val (chain, getRequest) = createMockChain()

            freshInterceptor.intercept(chain)

            getRequest().header("Authorization").shouldBeNull()
        }
    }

    // ==================== X-User-Id Header Tests ====================

    context("X-User-Id header") {

        test("adds X-User-Id when userId is present") {
            every { userPrefs.userId() } returns "USER456"
            val (chain, getRequest) = createMockChain()

            interceptor.intercept(chain)

            getRequest().header("X-User-Id") shouldBe "USER456"
        }

        test("does not add X-User-Id when userId is empty") {
            every { userPrefs.userId() } returns ""
            val (chain, getRequest) = createMockChain()

            interceptor.intercept(chain)

            getRequest().header("X-User-Id").shouldBeNull()
        }
    }

    // ==================== Accept Header Tests ====================

    context("accept header") {

        test("always adds Accept application/json header") {
            val (chain, getRequest) = createMockChain()

            interceptor.intercept(chain)

            getRequest().header("Accept") shouldBe "application/json"
        }
    }

    // ==================== JWT Update Tests ====================

    context("updateJWT") {

        test("updates token used in subsequent requests") {
            interceptor.updateJWT("new_token")
            val (chain, getRequest) = createMockChain()

            interceptor.intercept(chain)

            getRequest().header("Authorization") shouldBe "Bearer new_token"
        }

        test("clearing token removes authorization header") {
            interceptor.updateJWT("some_token")
            interceptor.updateJWT(null)
            val (chain, getRequest) = createMockChain()

            interceptor.intercept(chain)

            getRequest().header("Authorization").shouldBeNull()
        }
    }

    // ==================== No Internet Connection Tests ====================

    context("no internet connection") {

        test("throws IOException when not connected") {
            every { connectionLiveData.isConnected() } returns false
            val (chain, _) = createMockChain()

            val result = runCatching { interceptor.intercept(chain) }

            result.isFailure shouldBe true
            (result.exceptionOrNull() is IOException) shouldBe true
        }

        test("IOException message indicates no internet") {
            every { connectionLiveData.isConnected() } returns false
            val (chain, _) = createMockChain()

            val result = runCatching { interceptor.intercept(chain) }

            (result.exceptionOrNull() as IOException).message shouldContain "No internet"
        }
    }
})
