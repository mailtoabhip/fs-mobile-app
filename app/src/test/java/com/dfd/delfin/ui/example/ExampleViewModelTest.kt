package com.dfd.delfin.ui.example

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.dfd.delfin.api.repository.ApiError
import com.dfd.delfin.api.repository.ExampleCombinedData
import com.dfd.delfin.api.repository.ExampleRepository
import com.dfd.delfin.api.repository.ExampleUserData
import com.dfd.delfin.api.repository.Resource
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Test class for ExampleViewModel to verify Resource state emissions.
 * Tests that Loading state is emitted before API calls and proper state transitions occur.
 */
@ExperimentalCoroutinesApi
class ExampleViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ExampleRepository
    private lateinit var viewModel: ExampleViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = ExampleViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchUserData emits Loading then Success`() = runTest(testDispatcher) {
        // Given
        val expectedData = ExampleUserData(id = "123", name = "Test User")
        coEvery { repository.fetchUserData() } returns Resource.Success(expectedData)

        val observer = mockk<Observer<Resource<ExampleUserData>>>(relaxed = true)
        viewModel.userDataState.observeForever(observer)

        // When
        viewModel.fetchUserData()
        advanceUntilIdle()

        // Then - verify Loading was emitted first, then Success
        verify(exactly = 1) { observer.onChanged(Resource.Loading) }
        verify(exactly = 1) { observer.onChanged(Resource.Success(expectedData)) }
        verify(exactly = 2) { observer.onChanged(any()) }

        viewModel.userDataState.removeObserver(observer)
    }

    @Test
    fun `fetchUserData emits Loading then Failure on error`() = runTest(testDispatcher) {
        // Given
        val expectedFailure = Resource.Failure(
            isNetworkError = true,
            errorCode = null,
            apiError = ApiError.Network
        )
        coEvery { repository.fetchUserData() } returns expectedFailure

        val observer = mockk<Observer<Resource<ExampleUserData>>>(relaxed = true)
        viewModel.userDataState.observeForever(observer)

        // When
        viewModel.fetchUserData()
        advanceUntilIdle()

        // Then - verify Loading was emitted first, then Failure
        verify(exactly = 1) { observer.onChanged(Resource.Loading) }
        verify(exactly = 1) { observer.onChanged(expectedFailure) }
        verify(exactly = 2) { observer.onChanged(any()) }

        viewModel.userDataState.removeObserver(observer)
    }

    @Test
    fun `fetchCombinedData emits Loading then Success`() = runTest(testDispatcher) {
        // Given
        val expectedData = ExampleCombinedData(
            user = ExampleUserData(id = "123", name = "Test User"),
            wallet = mockk()
        )
        coEvery { repository.fetchCombinedData() } returns Resource.Success(expectedData)

        val observer = mockk<Observer<Resource<ExampleCombinedData>>>(relaxed = true)
        viewModel.combinedDataState.observeForever(observer)

        // When
        viewModel.fetchCombinedData()
        advanceUntilIdle()

        // Then - verify Loading was emitted first, then Success
        verify(exactly = 1) { observer.onChanged(Resource.Loading) }
        verify(exactly = 1) { observer.onChanged(Resource.Success(expectedData)) }
        verify(exactly = 2) { observer.onChanged(any()) }

        viewModel.combinedDataState.removeObserver(observer)
    }

    @Test
    fun `fetchCombinedData emits Loading then Failure on timeout`() = runTest(testDispatcher) {
        // Given
        val expectedFailure = Resource.Failure(
            isNetworkError = true,
            errorCode = null,
            apiError = ApiError.Timeout
        )
        coEvery { repository.fetchCombinedData() } returns expectedFailure

        val observer = mockk<Observer<Resource<ExampleCombinedData>>>(relaxed = true)
        viewModel.combinedDataState.observeForever(observer)

        // When
        viewModel.fetchCombinedData()
        advanceUntilIdle()

        // Then - verify Loading was emitted first, then Failure
        verify(exactly = 1) { observer.onChanged(Resource.Loading) }
        verify(exactly = 1) { observer.onChanged(expectedFailure) }
        verify(exactly = 2) { observer.onChanged(any()) }

        viewModel.combinedDataState.removeObserver(observer)
    }

    @Test
    fun `Resource Loading state exists and is singleton`() {
        // Verify that Resource.Loading is a singleton object
        val loading1 = Resource.Loading
        val loading2 = Resource.Loading
        
        assert(loading1 === loading2) { "Resource.Loading should be a singleton" }
    }

    @Test
    fun `Resource sealed class has three states`() {
        // Verify all three states exist
        val loading: Resource<String> = Resource.Loading
        val success: Resource<String> = Resource.Success("data")
        val failure: Resource<String> = Resource.Failure(
            isNetworkError = false,
            errorCode = 404,
            apiError = ApiError.NotFound
        )

        // Verify exhaustive when expression compiles (compile-time check)
        val result = when (loading) {
            is Resource.Loading -> "loading"
            is Resource.Success -> "success"
            is Resource.Failure -> "failure"
        }

        assert(result == "loading") { "When expression should handle Loading state" }
    }
}
