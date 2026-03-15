package com.delhivery.axle.api.repository

import com.delhivery.axle.injection.qualifier.IoDispatcher
import com.delhivery.axle.utils.ErrorLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Example repository demonstrating the coroutine-based API pattern with BaseRepository.
 * This serves as a reference implementation for migrating existing repositories from RxJava to coroutines.
 *
 * Key patterns demonstrated:
 * 1. Extending BaseRepository with ErrorLogger injection
 * 2. Injecting IO dispatcher for API calls
 * 3. Using safeApiCall for single API operations
 * 4. Using parallelApiCall2 for concurrent API operations
 * 5. Using withContext to switch to IO dispatcher
 *
 * Migration guide:
 * - Replace RxJava Single/Observable with suspend functions
 * - Wrap API calls with safeApiCall() for automatic error handling
 * - Use parallelApiCall2/3 for concurrent operations instead of zip/combineLatest
 * - Return Resource<T> instead of Single<T> or Observable<T>
 */
@Singleton
class ExampleRepository @Inject constructor(
    // Inject the service interface (Retrofit)
    // private val exampleService: ExampleService,
    
    // Inject ErrorLogger for BaseRepository
    errorLogger: ErrorLogger,
    
    // Inject IO dispatcher for API calls
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseRepository(errorLogger) {

    /**
     * Example: Single API call with safeApiCall wrapper.
     *
     * Pattern:
     * 1. Use withContext(ioDispatcher) to switch to IO thread
     * 2. Wrap the API call with safeApiCall { }
     * 3. Return Resource<T> which will be Success or Failure
     *
     * Usage in ViewModel:
     * ```
     * viewModelScope.launch {
     *     val result = repository.fetchUserData()
     *     when (result) {
     *         is Resource.Success -> handleSuccess(result.data)
     *         is Resource.Failure -> handleError(result.apiError)
     *     }
     * }
     * ```
     */
    suspend fun fetchUserData(): Resource<ExampleUserData> = withContext(ioDispatcher) {
        safeApiCall {
            // Replace with actual service call
            // exampleService.getUser()
            
            // Mock data for demonstration
            ExampleUserData(id = "123", name = "John Doe")
        }
    }

    /**
     * Example: Parallel API calls with parallelApiCall2.
     *
     * Pattern:
     * 1. Use withContext(ioDispatcher) to switch to IO thread
     * 2. Use parallelApiCall2 with two suspend lambdas
     * 3. Provide a transform function to combine results
     * 4. Both calls execute concurrently (async/await internally)
     *
     * Usage in ViewModel:
     * ```
     * viewModelScope.launch {
     *     val result = repository.fetchCombinedData()
     *     when (result) {
     *         is Resource.Success -> {
     *             val combined = result.data
     *             // Access combined.user and combined.wallet
     *         }
     *         is Resource.Failure -> handleError(result.apiError)
     *     }
     * }
     * ```
     */
    suspend fun fetchCombinedData(): Resource<ExampleCombinedData> = withContext(ioDispatcher) {
        parallelApiCall2(
            call1 = {
                // Replace with actual service call
                // exampleService.getUser()
                ExampleUserData(id = "123", name = "John Doe")
            },
            call2 = {
                // Replace with actual service call
                // exampleService.getWallet()
                ExampleWalletData(balance = 1000.0, currency = "INR")
            }
        ) { user, wallet ->
            // Transform function combines both results
            ExampleCombinedData(user = user, wallet = wallet)
        }
    }

    /**
     * Example: Three parallel API calls with parallelApiCall3.
     *
     * Pattern similar to parallelApiCall2 but with three concurrent calls.
     * Useful for loading dashboard data from multiple endpoints.
     */
    suspend fun fetchDashboardData(): Resource<ExampleDashboardData> = withContext(ioDispatcher) {
        parallelApiCall3(
            call1 = {
                // exampleService.getUser()
                ExampleUserData(id = "123", name = "John Doe")
            },
            call2 = {
                // exampleService.getWallet()
                ExampleWalletData(balance = 1000.0, currency = "INR")
            },
            call3 = {
                // exampleService.getOrders()
                listOf(
                    ExampleOrderData(orderId = "ORD001", status = "Active"),
                    ExampleOrderData(orderId = "ORD002", status = "Completed")
                )
            }
        ) { user, wallet, orders ->
            ExampleDashboardData(user = user, wallet = wallet, orders = orders)
        }
    }
}

// Example data models for demonstration
data class ExampleUserData(val id: String, val name: String)
data class ExampleWalletData(val balance: Double, val currency: String)
data class ExampleOrderData(val orderId: String, val status: String)
data class ExampleCombinedData(val user: ExampleUserData, val wallet: ExampleWalletData)
data class ExampleDashboardData(
    val user: ExampleUserData,
    val wallet: ExampleWalletData,
    val orders: List<ExampleOrderData>
)
