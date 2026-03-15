# Retrofit Coroutines Configuration Fix

## Problem
The `get_sp_loads` and `get_sp_intracity_loads` API calls were failing with the error:
```
IllegalArgumentException: Unable to create call adapter for class java.lang.Object
for method RecommendationService.recommendationIntracityTransactions
```

## Root Cause
The `RecommendationService` interface contains `suspend` functions for coroutines-based API calls:
- `recommendationTransactions()` - for intercity loads
- `recommendationIntracityTransactions()` - for intracity loads

However, the Retrofit instance was configured with `RxJava2CallAdapterFactory`, which doesn't support Kotlin coroutines. When Retrofit tried to create a proxy for the suspend functions, it couldn't find an appropriate call adapter.

## Solution
Created a separate Retrofit builder method for coroutines-based services that doesn't include any call adapter factory (Retrofit 2.6+ has built-in coroutines support).

### Changes Made

#### 1. NetworkModule.kt - Added Coroutines Retrofit Builder
```kotlin
/**
 * Get retrofit instance for Coroutines-based services (suspend functions)
 * 
 * Note: No call adapter factory is added. Retrofit 2.6+ has built-in support 
 * for Kotlin coroutines (suspend functions).
 */
private fun getRetrofitForCoroutines(
  gson: Gson,
  okHttpClient: OkHttpClient,
  urlConfig: UrlConfig
) = Retrofit.Builder()
    .baseUrl(urlConfig.url())
    .addConverterFactory(GsonConverterFactory.create(gson))
    .client(okHttpClient)
    .build()
```

#### 2. Updated RecommendationService Provider
Changed from `getRetrofit()` to `getRetrofitForCoroutines()`:
```kotlin
@Provides
@Singleton
fun provideRecommendationService(
  gson: Gson,
  okHttpClient: OkHttpClient
) = getRetrofitForCoroutines(gson, okHttpClient, UrlConfig.RecommendationService).create(
  RecommendationService::class.java
)
```

#### 3. TransactionService - Kept Using RxJava Retrofit
`TransactionService` has BOTH suspend functions AND RxJava methods, so it must use the RxJava retrofit. The `spotMarketplaceTransactions()` method is called via RxJava with a `runBlocking` bridge in the repository:

```kotlin
suspend fun fetchSpotMarketplaceTransactions(...): Resource<SpotMarketplaceLoadsData> = safeApiCall {
  kotlinx.coroutines.runBlocking {
    val response = transactionService.spotMarketplaceTransactions(...).blockingGet()
    if (response.isSuccess) {
      response.responseData ?: throw Exception("Null response data")
    } else {
      throw response.toHttpException()
    }
  }
}
```

## Why This Works
- **Retrofit 2.6+** has built-in support for Kotlin coroutines (suspend functions)
- When no call adapter factory is added, Retrofit uses its default adapter which supports suspend functions
- The `RxJava2CallAdapterFactory` is only needed for services that return `Single`, `Observable`, etc.
- By creating separate Retrofit instances, we can support both RxJava and coroutines in the same app
- Services with mixed RxJava/coroutines methods use RxJava retrofit with `runBlocking` bridges

## Impact
- ✅ Fixes `get_sp_loads` API call (intercity loads)
- ✅ Fixes `get_sp_intracity_loads` API call (intracity loads)
- ✅ Fixes spot marketplace loads API call
- ✅ Maintains backward compatibility with existing RxJava-based services
- ✅ No changes needed to existing RxJava service methods
- ✅ Contracts and other tabs work correctly

## Testing
After this fix:
1. Build succeeds without errors
2. API calls work correctly for all tabs (Loads, Contracts, Bids, Trips)
3. Coroutines-based repository methods receive proper responses
4. Error handling in `BaseRepository.safeApiCall()` works as expected

## Related Files
- `app/src/main/java/com/delhivery/axle/injection/module/NetworkModule.kt` - Retrofit configuration
- `app/src/main/java/com/delhivery/axle/api/service/RecommendationService.kt` - Service with suspend functions
- `app/src/main/java/com/delhivery/axle/api/service/TransactionService.kt` - Service with mixed RxJava/suspend functions
- `app/src/main/java/com/delhivery/axle/api/repository/TransactionsRepository.kt` - Repository using suspend functions
- `app/src/main/java/com/delhivery/axle/api/repository/BaseRepository.kt` - Error handling for coroutines

## Future Considerations
- When adding new services with **only** suspend functions, use `getRetrofitForCoroutines()` in the provider method
- When adding services with **only** RxJava methods, use `getRetrofit()` in the provider method
- When adding services with **mixed** RxJava and suspend methods, use `getRetrofit()` and bridge suspend calls via `runBlocking`

