# Implementation Plan: get_sp_loads Coroutine Migration

## Overview

Migrate the `POST /get_sp_loads` API flow from RxJava to pure Kotlin coroutines across Service, Repository, and ViewModel layers. The migration replaces `Single<BaseResponse<T>>` with `suspend fun`, `compositeDisposable` with `viewModelScope.launch`, and `Single.zip()` with `coroutineScope { async {} }`. All existing behavior (loading states, pagination, fallback, tab counts, LiveData contracts) is preserved. The UI layer requires no changes.

## Tasks

- [x] 1. Add test dependencies to build configuration
  - Add Kotest property testing, MockK, coroutines-test, and core-testing dependencies to `app/build.gradle`
  - Verify the project compiles with `./gradlew assembleDevelopmentDebug`
  - _Requirements: 13.1, 13.2, 13.3, 13.4_

- [x] 2. Migrate Service Layer to suspend functions
  - [x] 2.1 Replace RecommendationService methods with suspend functions
    - Replace `fun recommendationTransactions(@Body request: ReccomdationRequest): Single<BaseResponse<TransactionsResponse>>` with `suspend fun recommendationTransactions(@Body request: ReccomdationRequest): BaseResponse<TransactionsResponse>`
    - Replace `fun recommendationIntracityTransactions(@Body request: ReccomdationRequest): Single<BaseResponse<TransactionsResponse>>` with `suspend fun recommendationIntracityTransactions(@Body request: ReccomdationRequest): BaseResponse<TransactionsResponse>`
    - These are replaced in-place since only `HomeLoadsViewModel` consumes them
    - _Requirements: 1.1, 1.2, 1.6, 14.1_

  - [x] 2.2 Add suspend variants to BidService
    - Add `suspend fun bidsForLoadsSuspend(...)` returning `BaseResponse<TransactionBidsResponseBody>` directly
    - Add `suspend fun bulkLowestBidsForTransactionsSuspend(...)` returning `BaseResponse<List<LowestBidResponse>>` directly
    - Keep existing RxJava methods for other callers (rename if needed to avoid overload conflicts)
    - _Requirements: 1.3, 1.4, 1.6_

  - [x] 2.3 Add suspend variant to TransactionService
    - Add `suspend fun spotMarketplaceTransactionsSuspend(...)` returning `BaseResponse<SpotMarketplaceLoadsData>` directly
    - Keep existing RxJava method for callers outside this migration scope
    - _Requirements: 1.5, 1.6_

- [x] 3. Migrate Repository Layer to safeApiCall pattern
  - [x] 3.1 Replace TransactionsRepository methods with suspend equivalents
    - Replace `fetchRecommTransactions()` with `suspend fun` using `safeApiCall`, checking `BaseResponse.isSuccess` and throwing `toHttpException()` on failure
    - Replace `fetchIntracityRecommTransactions()` with `suspend fun` using `safeApiCall`
    - Replace `fetchSpotMarketplaceTransactions()` with `suspend fun` using `safeApiCall`
    - Handle null `responseData` by throwing `Exception("Null response data")` so `safeApiCall` maps to `Resource.Failure(ApiError.Unknown)`
    - _Requirements: 2.1, 2.2, 2.3, 2.6, 2.7, 2.8, 14.2_

  - [x] 3.2 Replace BidsRepository methods with suspend equivalents
    - Replace `bidsForLoads()` with `suspend fun` using `safeApiCall`, returning `Resource<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>>`
    - Replace `bulkLowestBidsForLoads()` with `suspend fun` using `safeApiCall`, returning `Resource<Pair<List<HomeBidsRequestItemData>, BulkLowestBidsResponse>>`
    - Rename old RxJava methods (e.g., `bidsForLoadsRx()`) and update callers outside migration scope
    - _Requirements: 2.4, 2.5, 2.6, 2.7, 2.8, 14.3_

  - [ ]* 3.3 Write property test: Repository success unwrapping (Property 1)
    - **Property 1: Repository success unwrapping**
    - Generate random `BaseResponse(isSuccess=true, data=randomTransactionsResponse)` using Kotest `Arb`, verify `fetchRecommTransactions` returns `Resource.Success(data)`
    - Create `app/src/test/java/com/delhivery/axle/api/repository/TransactionsRepositoryTest.kt`
    - **Validates: Requirements 2.6**

  - [ ]* 3.4 Write property test: Repository failure mapping via toHttpException (Property 2)
    - **Property 2: Repository failure mapping via toHttpException**
    - Generate random `BaseResponse(isSuccess=false, errorBody=randomErrorBody)` with varying error codes, verify `Resource.Failure` with correct `ApiError` mapping (401→Unauthorized, 403→AccessDenied, 404→NotFound, other→Unknown)
    - **Validates: Requirements 2.7, 11.5**

  - [ ]* 3.5 Write unit tests for repository edge cases
    - Test null `responseData` with `isSuccess=true` returns `Resource.Failure(ApiError.Unknown)`
    - Test `IOException` returns `Resource.Failure(ApiError.Network)`
    - Test `SocketTimeoutException` returns `Resource.Failure(ApiError.Timeout)`
    - Create `app/src/test/java/com/delhivery/axle/api/repository/BidsRepositoryTest.kt` with equivalent tests for bids methods
    - _Requirements: 13.1, 13.2, 13.3, 13.4_

- [x] 4. Checkpoint - Verify service and repository layers
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Migrate ViewModel fetch methods to coroutines
  - [x] 5.1 Add currentFetchJob pattern for rapid tab switching
    - Add `private var currentFetchJob: Job? = null` to `HomeLoadsViewModel`
    - Cancel previous job before launching new fetch in `fetchUserTransactions()` and `fetchSpotMarketplaceLoads()`
    - _Requirements: 10.1, 10.2, 16.7_

  - [x] 5.2 Migrate fetchLoadsData() non-intracity branch
    - Replace RxJava `compositeDisposable += ...flatMap { Single.zip(...) }.subscribe {}` with `viewModelScope.launch`
    - Call `transactionsRepository.fetchRecommTransactions()` sequentially for primary data
    - Launch 5 parallel calls using `coroutineScope { async {} }`: `bidsForLoads`, `bulkLowestBidsForLoads`, `fetchIntracityRecommTransactions`, `fetchRecommTransactions(splitViewCount=true)`, `fetchSpotMarketplaceTransactions(onlyCount=true)`
    - Combine parallel results identically to current `Single.zip()` combiner
    - On `Resource.Failure` from primary call, fall back to `fetchSupplierTransactions()`
    - Post `dataLoadingLiveData.postValue(true)` before API calls, `false` after completion
    - Update `searchAfter`, `hasMoreData`, `offset`, `total` from response
    - _Requirements: 3.1, 3.3, 3.5, 3.6, 4.1, 4.2, 5.1, 6.1, 6.2, 6.3, 7.1, 7.2, 7.3, 14.4_

  - [x] 5.3 Migrate fetchLoadsData() intracity branch
    - Call `transactionsRepository.fetchIntracityRecommTransactions()` sequentially for primary data
    - Launch parallel calls for split counts and marketplace count using `coroutineScope { async {} }`
    - On `Resource.Failure`, post error state to UI (no supplier fallback for intracity)
    - Update pagination state from response
    - _Requirements: 3.2, 4.1, 4.2, 5.2, 6.1, 6.2, 6.3_

  - [x] 5.4 Migrate fetchMarketplaceLoadsData()
    - Replace RxJava chain with `viewModelScope.launch` + sequential suspend calls + `coroutineScope { async {} }` for bids parallel calls
    - Fetch cross-tab intercity counts using suspend `fetchRecommTransactions`
    - _Requirements: 3.4, 3.5_

  - [x] 5.5 Wire entry points to coroutine methods
    - Update `fetchUserTransactions()` to use `currentFetchJob` pattern and call coroutine-based `fetchLoadsData()`
    - Update `fetchSpotMarketplaceLoads()` to call coroutine-based `fetchMarketplaceLoadsData()`
    - Preserve user-data-fetch-first pattern (check `user == null` before proceeding)
    - Remove all `compositeDisposable` additions from migrated fetch methods
    - _Requirements: 12.1, 12.2, 12.3, 14.4, 14.5_

  - [x] 5.6 Implement tab count aggregation in coroutine flow
    - Extract `INTERCITY` and `NON_DELHIVERY` counts from `loadCounts.all` (default to 0 when null)
    - Post sum of intracity + intercity + non-delhivery + marketplace to `fullLoadsCountLiveData`
    - Post per-filter count to `loadsCountLiveData`
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

  - [x] 5.7 Implement pagination progress item for coroutine flow
    - When `paginate=true`, post `HomeLoadsProgressItem` with `AddUpdate` operation to `userLoadsData` before API call
    - Return early without API call when `hasMoreData == false`
    - _Requirements: 4.3, 6.4_

- [x] 6. Checkpoint - Verify ViewModel migration compiles
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. Write ViewModel property-based tests
  - [ ]* 7.1 Write property test: Loading state round trip (Property 3)
    - **Property 3: Loading state round trip**
    - Generate random fetch parameters, verify `dataLoadingLiveData` transitions `true` → `false` regardless of success/failure
    - Create `app/src/test/java/com/delhivery/axle/ui/home/fragments/loads/HomeLoadsViewModelTest.kt`
    - **Validates: Requirements 4.1, 4.2**

  - [ ]* 7.2 Write property test: Pagination state consistency (Property 4)
    - **Property 4: Pagination state consistency**
    - Generate random `TransactionsResponse` with varying `searchAfter` and `transactions`, verify `hasMoreData == (searchAfter != null && transactions.isNotEmpty())`
    - Verify `fetchUserTransactions(paginate=true)` returns early when `hasMoreData == false`
    - **Validates: Requirements 6.1, 6.2, 6.4**

  - [ ]* 7.3 Write property test: Tab count aggregation (Property 5)
    - **Property 5: Tab count aggregation**
    - Generate random `loadCounts` with varying `INTERCITY`/`NON_DELHIVERY` values, verify `fullLoadsCountLiveData` equals sum of all tab counts, with null `loadCounts` defaulting to 0
    - **Validates: Requirements 8.1, 8.3, 8.4**

  - [ ]* 7.4 Write property test: Parallel call failure cancels siblings (Property 6)
    - **Property 6: Parallel call failure cancels siblings**
    - Generate random parallel call configurations where one call throws, verify no partial results posted to LiveData
    - **Validates: Requirements 7.2**

  - [ ]* 7.5 Write property test: Parallel call success produces combined result (Property 7)
    - **Property 7: Parallel call success produces combined result**
    - Generate random successful parallel results (bids, lowestBids, intracity, splitCount, marketplace), verify combined output matches expected structure
    - **Validates: Requirements 7.3**

  - [ ]* 7.6 Write property test: CancellationException propagation (Property 8)
    - **Property 8: CancellationException propagation**
    - Verify `CancellationException` thrown during `safeApiCall` is rethrown, not mapped to `Resource.Failure`
    - **Validates: Requirements 10.3**

- [ ] 8. Write ViewModel unit tests for branches and edge cases
  - [ ]* 8.1 Write unit tests for ViewModel branch logic
    - Test intracity branch success: mock intracity response, verify `userLoadsData` posted with correct items
    - Test non-intracity branch success: mock all 5 parallel responses, verify combined `userLoadsData`
    - Test non-intracity fallback on error: mock primary call failure, verify `fetchSupplierTransactions` called
    - Test intracity no fallback on error: mock intracity failure, verify no supplier fallback
    - _Requirements: 3.2, 3.3, 5.1, 5.2_

  - [ ]* 8.2 Write unit tests for ViewModel edge cases
    - Test pagination progress item: call with `paginate=true`, verify `HomeLoadsProgressItem` posted
    - Test null `loadCounts`: mock response with `loadCounts=null`, verify counts default to 0
    - Test empty transactions: mock response with empty transactions, verify `hasMoreData=false` and `searchAfter=null`
    - Test rapid tab switching: launch two fetches rapidly, verify first is cancelled via `currentFetchJob`
    - Test `onCleared` cancellation: start fetch, call `onCleared()`, verify no LiveData updates after
    - _Requirements: 4.3, 6.2, 6.3, 8.2, 10.1, 16.7, 16.8_

- [x] 9. Clean up legacy RxJava code from migrated flow
  - Remove old RxJava `fetchLoadsData()` and `fetchMarketplaceLoadsData()` method bodies (now replaced by coroutine implementations)
  - Remove all `compositeDisposable` additions from the migrated fetch methods
  - Verify no dead RxJava imports remain in migrated files
  - Verify `convertResponse()` is NOT used in the migrated flow (kept for other RxJava callers)
  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_

- [x] 10. Final checkpoint - Full validation
  - Ensure all tests pass, ask the user if questions arise.
  - Verify `./gradlew assembleDevelopmentDebug` compiles without errors
  - Verify the migration can be committed as a single atomic commit for rollback safety
  - _Requirements: 15.1, 15.3, 16.1_

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document (Properties 1-8)
- Unit tests validate specific examples, edge cases, and branch logic
- The UI layer (HomeLoadsFragment) requires no changes since the LiveData contract is preserved
- BidService and TransactionService keep existing RxJava methods for callers outside this migration scope
- RecommendationService methods are replaced in-place since only HomeLoadsViewModel consumes them
