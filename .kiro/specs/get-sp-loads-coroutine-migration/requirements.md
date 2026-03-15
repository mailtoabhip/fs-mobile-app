# Requirements Document

## Introduction

Migrate the `POST /get_sp_loads` API endpoint flow from the legacy RxJava `Single<BaseResponse<TransactionsResponse>>` pattern to a pure coroutines-based architecture using `suspend fun`, `safeApiCall`, and `Resource<T>`. This is a complete RxJava removal from the `get_sp_loads` flow — no bridge libraries, no `Single.await()`, no `kotlinx-coroutines-rx2`.

All service methods involved in the `get_sp_loads` ViewModel flow must become suspend functions: `recommendationTransactions`, `bidsForLoads`, `bulkLowestBidsForLoads`, `fetchIntracityRecommTransactions`, and `fetchSpotMarketplaceTransactions`. The existing RxJava methods are replaced in-place (not kept alongside) since the migration is scoped to the `get_sp_loads` flow and these parallel calls are only consumed by `HomeLoadsViewModel`.

The migration must preserve all existing behavior: parallel API calls via `coroutineScope { async {} }`, pagination via `searchAfter`, fallback to supplier loads on error, tab count aggregation, loading/error states, and lifecycle-aware cancellation.

## Glossary

- **RecommendationService**: Retrofit service interface defining the `POST /get_sp_loads` endpoint (`recommendationTransactions`) and `POST /get_sp_intracity_loads` endpoint (`recommendationIntracityTransactions`)
- **BidService**: Retrofit service interface defining bid-related endpoints (`bidsForLoads`, `bulkLowestBidsForTransactions`)
- **TransactionService**: Retrofit service interface defining the `spotMarketplaceTransactions` endpoint
- **TransactionsRepository**: Repository class that wraps `RecommendationService` and `TransactionService` calls and exposes them to ViewModels
- **BidsRepository**: Repository class that wraps `BidService` calls for bid fetching and exposes them to ViewModels
- **HomeLoadsViewModel**: ViewModel managing load data fetching for the Home Loads screen, containing 4 call sites that invoke `fetchRecommTransactions`
- **HomeLoadsFragment**: UI Fragment observing LiveData streams (`userLoadsData`, `userLoadsDataFetch`, `dataLoadingLiveData`, `fullLoadsCountLiveData`) to render load lists
- **safeApiCall**: Suspend function in `BaseRepository` that wraps API calls with comprehensive exception-to-`Resource` mapping
- **Resource**: Sealed class with `Loading`, `Success(data)`, and `Failure(isNetworkError, errorCode, apiError)` states
- **ApiError**: Enum categorizing errors: `Timeout`, `Network`, `Unauthorized`, `AccessDenied`, `NotFound`, `ServiceUnavailable`, `Unknown`
- **convertResponse()**: RxJava extension on `Single<BaseResponse<T>>` that unwraps `BaseResponse`, checking `isSuccess` and throwing `HttpException` on failure — not used in the migrated flow
- **BaseResponse**: API response wrapper with `isSuccess: Boolean`, `responseData: T?`, and `errorBody: BaseErrorResponse?`
- **compositeDisposable**: RxJava subscription manager in `BaseViewModel`, cleared on `onCleared()` for lifecycle safety — not used in the migrated flow
- **viewModelScope**: Coroutine scope tied to ViewModel lifecycle, auto-cancelled on `onCleared()`
- **searchAfter**: Pagination cursor object containing `creationTime`, `transactionId`, `requiredOn` for cursor-based pagination
- **splitViewCount**: Boolean flag on `ReccomdationRequest` that triggers a count-only response with `loadCounts` breakdown by type

## Requirements

### Requirement 1: Replace Service Layer Methods with Suspend Functions

**User Story:** As a developer, I want the Retrofit service methods used in the `get_sp_loads` flow replaced with suspend functions, so that the entire flow uses pure coroutines without RxJava.

#### Acceptance Criteria

1. THE RecommendationService SHALL replace `fun recommendationTransactions(@Body request: ReccomdationRequest): Single<BaseResponse<TransactionsResponse>>` with `suspend fun recommendationTransactions(@Body request: ReccomdationRequest): BaseResponse<TransactionsResponse>` annotated with `@POST("/get_sp_loads")`
2. THE RecommendationService SHALL replace `fun recommendationIntracityTransactions(@Body request: ReccomdationRequest): Single<BaseResponse<TransactionsResponse>>` with `suspend fun recommendationIntracityTransactions(@Body request: ReccomdationRequest): BaseResponse<TransactionsResponse>` annotated with `@POST("/get_sp_intracity_loads")`
3. THE BidService SHALL add a suspend method `suspend fun bidsForLoadsSuspend(...)` returning the response directly without `Single<>` wrapping, for use by the migrated flow
4. THE BidService SHALL add a suspend method `suspend fun bulkLowestBidsForTransactionsSuspend(...)` returning the response directly without `Single<>` wrapping, for use by the migrated flow
5. THE TransactionService SHALL add a suspend method `suspend fun spotMarketplaceTransactionsSuspend(...)` returning the response directly without `Single<>` wrapping, for use by the migrated flow
6. WHEN any suspend method is called with valid parameters, THE service SHALL return the response object directly without RxJava wrapping

### Requirement 2: Replace Repository Methods with Coroutine Implementations

**User Story:** As a developer, I want the repository methods used in the `get_sp_loads` flow replaced with suspend functions using `safeApiCall`, so that errors are mapped to `Resource.Failure` consistently.

#### Acceptance Criteria

1. THE TransactionsRepository SHALL replace `fetchRecommTransactions()` with `suspend fun fetchRecommTransactions(offset: Int, demand_type: String, vehicle_type: String?, excludeTruckTypes: String?, filterVehicleType: Boolean?, biddingGoingOn: Boolean, splitViewCount: Boolean?, searchAfter: SearchAfter?): Resource<TransactionsResponse>` using `safeApiCall`
2. THE TransactionsRepository SHALL replace `fetchIntracityRecommTransactions()` with `suspend fun fetchIntracityRecommTransactions(offset: Int, demand_type: String?, vehicle_type: String?, excludeTruckTypes: String?, filterVehicleType: Boolean?, biddingGoingOn: Boolean, onlyCount: Boolean?, searchAfter: SearchAfter?): Resource<TransactionsResponse>` using `safeApiCall`
3. THE TransactionsRepository SHALL replace `fetchSpotMarketplaceTransactions()` with `suspend fun fetchSpotMarketplaceTransactions(onlyCount: Boolean, limit: Int, offset: Int): Resource<SpotMarketplaceLoadsData>` using `safeApiCall`
4. THE BidsRepository SHALL replace `bidsForLoads()` with `suspend fun bidsForLoads(transactions: List<HomeBidsRequestItemData>?, contractBids: Boolean?): Resource<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>>` using `safeApiCall`
5. THE BidsRepository SHALL replace `bulkLowestBidsForLoads()` with `suspend fun bulkLowestBidsForLoads(transactions: List<HomeBidsRequestItemData>?): Resource<Pair<List<HomeBidsRequestItemData>, BulkLowestBidsResponse>>` using `safeApiCall`
6. WHEN the API returns `BaseResponse.isSuccess == true`, THE repository method SHALL return `Resource.Success(responseData)`
7. WHEN the API returns `BaseResponse.isSuccess == false`, THE repository method SHALL throw the `HttpException` produced by `BaseResponse.toHttpException()` so that `safeApiCall` maps it to `Resource.Failure` with the correct `ApiError`
8. WHEN `BaseResponse.responseData` is null despite `isSuccess == true`, THE repository method SHALL throw an `Exception` so that `safeApiCall` maps it to `Resource.Failure(ApiError.Unknown)`

### Requirement 3: Migrate ViewModel Fetch Methods to Pure Coroutines

**User Story:** As a developer, I want the ViewModel's load-fetching methods rewritten using pure coroutines with `coroutineScope { async {} }`, so that the complex `Single.zip()` and `.flatMap{}` chains are fully replaced.

#### Acceptance Criteria

1. THE HomeLoadsViewModel SHALL replace the RxJava-based `fetchLoadsData()` with a coroutine-based implementation using `viewModelScope.launch`
2. WHEN `selectedFilter == DemandType.Intracity.type`, THE HomeLoadsViewModel SHALL call the suspend `fetchIntracityRecommTransactions` for the primary data fetch, then execute the split-view-count `fetchRecommTransactions` calls in parallel using `coroutineScope { async {} }`
3. WHEN `selectedFilter != DemandType.Intracity.type`, THE HomeLoadsViewModel SHALL call the suspend `fetchRecommTransactions` for the primary data fetch, then execute `bidsForLoads`, `bulkLowestBidsForLoads`, `fetchIntracityRecommTransactions`, the split-view-count `fetchRecommTransactions`, and `fetchSpotMarketplaceTransactions` in parallel using `coroutineScope { async {} }`
4. THE HomeLoadsViewModel SHALL replace the RxJava-based `fetchMarketplaceLoadsData()` with a coroutine-based implementation using the suspend `fetchRecommTransactions` for cross-tab intercity counts
5. THE HomeLoadsViewModel SHALL use `viewModelScope.launch` as the coroutine entry point for all fetch methods
6. THE HomeLoadsViewModel SHALL remove all `compositeDisposable` usage from the migrated fetch methods

### Requirement 4: Preserve Loading State Emission

**User Story:** As a developer, I want loading states emitted before API calls begin, so that the UI shows loading indicators consistently.

#### Acceptance Criteria

1. WHEN a coroutine fetch method starts, THE HomeLoadsViewModel SHALL post `true` to `dataLoadingLiveData` before any API call
2. WHEN a coroutine fetch method completes (success or failure), THE HomeLoadsViewModel SHALL post `false` to `dataLoadingLiveData`
3. WHEN pagination is triggered, THE HomeLoadsViewModel SHALL post a `HomeLoadsProgressItem` with `AddUpdate` operation to `userLoadsData` before the API call

### Requirement 5: Preserve Fallback to Supplier Loads on Error

**User Story:** As a developer, I want the fallback to `fetchSupplierTransactions()` preserved when the recommendation API fails on the intercity/non-delhivery tab, so that users still see loads.

#### Acceptance Criteria

1. WHEN `fetchRecommTransactions` returns `Resource.Failure` in the non-intracity branch, THE HomeLoadsViewModel SHALL call `fetchSupplierTransactions()` with the same parameters as the current RxJava error handler
2. WHEN `fetchRecommTransactions` returns `Resource.Failure` in the intracity branch, THE HomeLoadsViewModel SHALL post the error state to the UI without calling the supplier fallback

### Requirement 6: Preserve Pagination State Management

**User Story:** As a developer, I want pagination via `searchAfter` cursor and `hasMoreData` flag to work identically after migration, so that infinite scroll continues to function.

#### Acceptance Criteria

1. WHEN `fetchRecommTransactions` returns `Resource.Success`, THE HomeLoadsViewModel SHALL update `searchAfter` from `TransactionsResponse.searchAfter`
2. WHEN `TransactionsResponse.searchAfter` is null, THE HomeLoadsViewModel SHALL set `hasMoreData` to `false`
3. WHEN `TransactionsResponse.transactions` is empty, THE HomeLoadsViewModel SHALL set `searchAfter` to null and `hasMoreData` to `false`
4. WHEN `hasMoreData` is `false` and pagination is requested, THE HomeLoadsViewModel SHALL return early without making an API call

### Requirement 7: Preserve Parallel Call Behavior with Structured Concurrency

**User Story:** As a developer, I want parallel API calls to execute concurrently and cancel siblings on failure, so that the behavior matches the current `Single.zip()` semantics.

#### Acceptance Criteria

1. WHEN the non-intracity branch executes parallel calls, THE HomeLoadsViewModel SHALL use `coroutineScope { async {} }` to launch all parallel suspend calls concurrently
2. WHEN any parallel call throws an exception, THE HomeLoadsViewModel SHALL cancel all sibling coroutines via structured concurrency
3. WHEN all parallel calls succeed, THE HomeLoadsViewModel SHALL combine results identically to the current `Single.zip()` combiner function

### Requirement 8: Preserve Tab Count Aggregation

**User Story:** As a developer, I want tab badge counts (intracity, intercity, non-delhivery, marketplace) computed and posted identically, so that the filter UI displays correct counts.

#### Acceptance Criteria

1. WHEN the split-view-count call returns `loadCounts`, THE HomeLoadsViewModel SHALL extract `INTERCITY` and `NON_DELHIVERY` counts from `loadCounts.all`
2. WHEN `loadCounts` is null, THE HomeLoadsViewModel SHALL default intercity and non-delhivery counts to 0
3. THE HomeLoadsViewModel SHALL post the sum of all tab counts (intracity + intercity + non-delhivery + marketplace) to `fullLoadsCountLiveData`
4. THE HomeLoadsViewModel SHALL post the per-filter count to `loadsCountLiveData`

### Requirement 9: Preserve LiveData Contract for UI Layer

**User Story:** As a developer, I want the existing LiveData types and posting behavior unchanged, so that `HomeLoadsFragment` requires minimal or no changes.

#### Acceptance Criteria

1. THE HomeLoadsViewModel SHALL continue posting to `userLoadsData: MutableLiveData<List<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>` with the same item types and operations
2. THE HomeLoadsViewModel SHALL continue posting to `userLoadsDataFetch`, `dataLoadingLiveData`, `loadsCountLiveData`, and `fullLoadsCountLiveData` with the same data types
3. THE HomeLoadsFragment SHALL continue observing the same LiveData streams with `reobserve(viewLifecycleOwner)` without changes to observer logic

### Requirement 10: Handle Coroutine Cancellation on ViewModel Lifecycle

**User Story:** As a developer, I want coroutines automatically cancelled when the ViewModel is cleared, so that there are no leaked API calls or crashes from posting to destroyed LiveData.

#### Acceptance Criteria

1. WHEN the ViewModel's `onCleared()` is called, THE viewModelScope SHALL cancel all running coroutines automatically
2. THE HomeLoadsViewModel SHALL use `viewModelScope.launch` (not `GlobalScope` or custom scopes) for all coroutine entry points
3. WHEN a `CancellationException` is thrown during an API call, THE safeApiCall SHALL rethrow it without mapping to `Resource.Failure`

### Requirement 11: Granular Error Categorization

**User Story:** As a developer, I want API errors categorized into specific `ApiError` types, so that the UI can display targeted error messages instead of generic errors.

#### Acceptance Criteria

1. WHEN a `SocketTimeoutException` occurs, THE safeApiCall SHALL return `Resource.Failure(isNetworkError=true, errorCode=null, apiError=ApiError.Timeout)`
2. WHEN an `IOException` occurs, THE safeApiCall SHALL return `Resource.Failure(isNetworkError=true, errorCode=null, apiError=ApiError.Network)`
3. WHEN an `HttpException` with code 401 occurs, THE safeApiCall SHALL return `Resource.Failure(isNetworkError=false, errorCode=401, apiError=ApiError.Unauthorized)`
4. WHEN an `HttpException` with code 403 occurs, THE safeApiCall SHALL return `Resource.Failure(isNetworkError=false, errorCode=403, apiError=ApiError.AccessDenied)`
5. WHEN `BaseResponse.isSuccess == false` triggers `toHttpException()`, THE safeApiCall SHALL map the resulting `HttpException` to the appropriate `ApiError` based on the error code from `BaseErrorResponse`

### Requirement 12: Switch Callers to Coroutine Methods

**User Story:** As a developer, I want the entry-point methods (`fetchUserTransactions`, `fetchSpotMarketplaceLoads`) to call the coroutine-based methods, so that the migration takes effect.

#### Acceptance Criteria

1. WHEN `fetchUserTransactions()` is called, THE HomeLoadsViewModel SHALL invoke the coroutine-based fetch logic directly (replacing the old RxJava `fetchLoadsData()` call)
2. WHEN `fetchSpotMarketplaceLoads()` is called, THE HomeLoadsViewModel SHALL invoke the coroutine-based marketplace fetch logic directly (replacing the old RxJava `fetchMarketplaceLoadsData()` call)
3. THE HomeLoadsViewModel SHALL preserve the user-data-fetch-first pattern (checking `user == null` and fetching user before proceeding)

### Requirement 13: Unit Test Coverage for Repository Migration

**User Story:** As a developer, I want unit tests verifying the new suspend repository methods, so that the `BaseResponse.isSuccess` check and error mapping are validated.

#### Acceptance Criteria

1. WHEN `recommendationTransactions` returns `BaseResponse(isSuccess=true, responseData=data)`, THE test SHALL verify `fetchRecommTransactions` returns `Resource.Success(data)`
2. WHEN `recommendationTransactions` returns `BaseResponse(isSuccess=false, errorBody=error)`, THE test SHALL verify `fetchRecommTransactions` returns `Resource.Failure` with the correct `ApiError`
3. WHEN `recommendationTransactions` throws `IOException`, THE test SHALL verify `fetchRecommTransactions` returns `Resource.Failure(ApiError.Network)`
4. WHEN `recommendationTransactions` throws `SocketTimeoutException`, THE test SHALL verify `fetchRecommTransactions` returns `Resource.Failure(ApiError.Timeout)`

### Requirement 14: Cleanup of Legacy RxJava Code from Migrated Flow

**User Story:** As a developer, I want the old RxJava method signatures replaced in-place with suspend functions, so that no dead RxJava code remains in the migrated flow.

#### Acceptance Criteria

1. THE RecommendationService SHALL have the RxJava `Single<>` return types replaced with direct return types on the migrated methods (`recommendationTransactions`, `recommendationIntracityTransactions`)
2. THE TransactionsRepository SHALL have the RxJava-based `fetchRecommTransactions()`, `fetchIntracityRecommTransactions()`, and `fetchSpotMarketplaceTransactions()` replaced with suspend equivalents
3. THE BidsRepository SHALL have the RxJava-based `bidsForLoads()` and `bulkLowestBidsForLoads()` replaced with suspend equivalents
4. THE HomeLoadsViewModel SHALL have the old `fetchLoadsData()` and `fetchMarketplaceLoadsData()` RxJava methods replaced with coroutine implementations
5. THE HomeLoadsViewModel SHALL remove all `compositeDisposable` additions from the migrated fetch methods

### Requirement 15: Rollback Safety via Git

**User Story:** As a developer, I want a clean git-based rollback path, so that the migration can be reverted quickly if issues are found in production.

#### Acceptance Criteria

1. THE migration changes SHALL be committed as a single atomic git commit (or a small series of commits) so that `git revert <commit-hash>` restores the full RxJava implementation
2. IF a build failure or runtime crash occurs after the migration, THEN THE developer SHALL revert the migration commit(s) using `git revert` to restore the previous RxJava-based implementation
3. THE migration commit message SHALL clearly describe the scope of changes (service, repository, ViewModel layers) for easy identification during rollback

### Requirement 16: Build and Runtime Validation

**User Story:** As a developer, I want a validation checklist ensuring the migration does not introduce regressions, so that all load tabs, pagination, error states, and counts work correctly.

#### Acceptance Criteria

1. WHEN `./gradlew assembleDevelopmentDebug` is executed, THE build SHALL complete without errors on all modified files
2. WHEN the Intracity tab is selected, THE HomeLoadsFragment SHALL display intracity loads with correct counts and pagination
3. WHEN the Intercity tab is selected, THE HomeLoadsFragment SHALL display intercity loads with correct counts, bid data, and pagination
4. WHEN the Marketplace tab is selected, THE HomeLoadsFragment SHALL display marketplace loads with correct cross-tab counts
5. WHEN a network error occurs during load fetch, THE HomeLoadsFragment SHALL display the appropriate error state
6. WHEN the recommendation API fails on the intercity tab, THE HomeLoadsViewModel SHALL fall back to supplier loads
7. WHEN the user rapidly switches tabs, THE HomeLoadsViewModel SHALL cancel the previous fetch coroutine and start a new one without crashes
8. WHEN the screen rotates during a load fetch, THE HomeLoadsViewModel SHALL not crash or leak coroutines
