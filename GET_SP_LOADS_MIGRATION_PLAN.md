# Migration Plan: `get_sp_loads` → Coroutines-Based API Architecture

**Date**: March 2026
**Endpoint**: `POST /get_sp_loads`
**Status**: PLAN ONLY — No code changes

---

## 1. Architecture Summary

### Current Architecture (RxJava)
```
RecommendationService.recommendationTransactions()     [Retrofit → Single<BaseResponse<TransactionsResponse>>]
        ↓
    .convertResponse()                                 [Extension: unwraps BaseResponse → Single<TransactionsResponse>]
        ↓
TransactionsRepository.fetchRecommTransactions()       [Returns Single<TransactionsResponse>]
        ↓
HomeLoadsViewModel                                     [compositeDisposable += repo.fetch...onBackground().subscribe{}]
        ↓
HomeLoadsFragment                                      [Observes MutableLiveData via reobserve()]
```

### Target Architecture (Coroutines)
```
RecommendationService.recommendationTransactions()     [Retrofit → suspend fun: BaseResponse<TransactionsResponse>]
        ↓
TransactionsRepository.fetchRecommTransactions()       [suspend fun using safeApiCall → Resource<TransactionsResponse>]
        ↓
HomeLoadsViewModel                                     [viewModelScope.launch { _state.value = Resource.Loading; ... }]
        ↓
HomeLoadsFragment                                      [Observes LiveData<Resource<T>> with exhaustive when]
```

---

## 2. Endpoint Discovery — Full Call Chain

### Service Layer
- **File**: `app/src/main/java/com/delhivery/axle/api/service/RecommendationService.kt`
- **Method**: `recommendationTransactions(@Body request: ReccomdationRequest): Single<BaseResponse<TransactionsResponse>>`
- **HTTP**: `@POST("/get_sp_loads")`

### Repository Layer
- **File**: `app/src/main/java/com/delhivery/axle/api/repository/TransactionsRepository.kt`
- **Method**: `fetchRecommTransactions(offset, demand_type, vehicle_type, excludeTruckTypes, filterVehicleType, biddingGoingOn, splitViewCount, searchAfter)`
- **Returns**: `Single<TransactionsResponse>` (after `.convertResponse()` unwraps `BaseResponse`)
- **Dependencies**: `RecommendationService`, `UserPrefs` (for `parentId`)

### ViewModel Layer
- **File**: `app/src/main/java/com/delhivery/axle/ui/home/fragments/loads/HomeLoadsViewModel.kt`
- **Consumer Methods** (4 distinct call sites):
  1. **`fetchLoadsData()`** (line ~285) — Inside `Single.zip()` for intercity count fetching when `selectedFilter == DemandType.Intracity.type`
  2. **`fetchLoadsData()`** (line ~412) — Primary intercity/non-delhivery load fetch, chained with `.flatMap{}` and `Single.zip()` with 5 parallel calls
  3. **`fetchLoadsData()`** (line ~464) — Inside the 5-way `Single.zip()` as the 4th parallel call (with `splitViewCount = true`)
  4. **`fetchMarketplaceLoadsData()`** (line ~976) — Inside `Single.zip()` for fetching intercity counts while on marketplace tab

### UI Layer
- **File**: `app/src/main/java/com/delhivery/axle/ui/home/fragments/loads/HomeLoadsFragment.kt`
- **Observers**:
  - `viewModel.userLoadsData.reobserve()` — Main loads list
  - `viewModel.userLoadsDataFetch.reobserve()` — Supplier/fallback loads list
  - `viewModel.dataLoadingLiveData` — Loading state (Boolean)
  - `viewModel.loadsCountLiveData` — Tab count
  - `viewModel.fullLoadsCountLiveData` — Total count across all tabs

---

## 3. Usage Mapping — All Locations

### Direct Callers of `fetchRecommTransactions()`

| # | File | Method | Context |
|---|------|--------|---------|
| 1 | `HomeLoadsViewModel.kt` | `fetchLoadsData()` — Intracity branch | Inside `Single.zip()` to get intercity split counts |
| 2 | `HomeLoadsViewModel.kt` | `fetchLoadsData()` — Else branch | Primary data fetch, chained via `.flatMap{}` |
| 3 | `HomeLoadsViewModel.kt` | `fetchLoadsData()` — Else branch | 4th call in 5-way `Single.zip()` (splitViewCount=true) |
| 4 | `HomeLoadsViewModel.kt` | `fetchMarketplaceLoadsData()` | 2nd call in `Single.zip()` for cross-tab counts |

### Indirect Consumers (observe LiveData set by above methods)

| # | File | LiveData Observed |
|---|------|-------------------|
| 1 | `HomeLoadsFragment.kt` | `userLoadsData` — Main loads list |
| 2 | `HomeLoadsFragment.kt` | `userLoadsDataFetch` — Supplier loads fallback |
| 3 | `HomeLoadsFragment.kt` | `dataLoadingLiveData` — Loading indicator |
| 4 | `HomeLoadsFragment.kt` | `fullLoadsCountLiveData` — Tab badge count |
| 5 | `HomeLoadsFragment.kt` | `loadsCountLiveData` — Per-filter count |

### NOT Used In (Confirmed)
- No background workers (`MyWorker.kt`)
- No other ViewModels besides `HomeLoadsViewModel`
- No other Fragments/Activities
- `SearchResultsViewModel` uses `searchTransactions()`, NOT `fetchRecommTransactions()`

---

## 4. Current Architecture Review

### Execution Model
- **Reactive Framework**: RxJava 2 (`Single<T>`)
- **Threading**: `.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())` via `.onBackground()`
- **Lifecycle Management**: `compositeDisposable` in `BaseViewModel`, cleared in `onCleared()`
- **Chaining**: Heavy use of `.flatMap{}` and `Single.zip()` for sequential and parallel calls

### Data Models

| Model | Package | Role |
|-------|---------|------|
| `ReccomdationRequest` | `api.request` | Request body with sp_id, limit, offset, demand_types, truck_types, etc. |
| `BaseResponse<TransactionsResponse>` | `api.response` | Wrapper with `isSuccess`, `responseData`, `errorBody` |
| `TransactionsResponse` | `api.response` | Contains `transactions`, `total`, `offset`, `hasNext`, `searchAfter`, `loadCounts`, etc. |
| `HomeBidsRequestItemData` | `data.home.bids` | Individual load item (used in RecyclerView) |
| `SearchAfter` | `api.response` | Pagination cursor (creation_time, transaction_id, required_on) |
| `LoadCounts` | `api.response` | Split counts by load type (INTERCITY, NON_DELHIVERY) |

### Current Error Handling
1. **`convertResponse()`**: Checks `BaseResponse.isSuccess` → throws `HttpException` if false
2. **RxJava `.subscribe{}`**: `{ result, error -> if (!error && result != null) { ... } else { error.handle() } }`
3. **`Throwable.handle()`**: Posts to `exceptionLiveData` in `BaseViewModel`
4. **Fallback**: On error in intercity fetch, calls `fetchSupplierTransactions()` as fallback
5. **No granular error categorization** — all errors treated the same way in UI

### Parallel Call Patterns
The ViewModel uses complex `Single.zip()` patterns combining `fetchRecommTransactions` with:
- `bidsRepository.bidsForLoads()`
- `bidsRepository.bulkLowestBidsForLoads()`
- `transactionsRepository.fetchIntracityRecommTransactions()`
- `transactionsRepository.fetchSpotMarketplaceTransactions()`

---

## 5. Impact Analysis — Files That Must Change

### Must Change

| # | File | Layer | Change Required |
|---|------|-------|-----------------|
| 1 | `RecommendationService.kt` | Service | Change `fun` to `suspend fun`, return type `BaseResponse<TransactionsResponse>` (drop `Single<>`) |
| 2 | `TransactionsRepository.kt` | Repository | Add new `suspend fun fetchRecommTransactionsSafe()` using `safeApiCall` |
| 3 | `HomeLoadsViewModel.kt` | ViewModel | Replace RxJava chains with `viewModelScope.launch` + coroutines |
| 4 | `HomeLoadsFragment.kt` | UI | Update observers to handle `Resource<T>` with Loading/Success/Failure |

### May Need Change (Shared Dependencies)

| # | File | Reason |
|---|------|--------|
| 5 | `DelhiveryExtensions.kt` | `convertResponse()` won't be needed for coroutine path (keep for other RxJava callers) |
| 6 | `BaseViewModel.kt` | May need coroutine support alongside existing RxJava `compositeDisposable` |
| 7 | `NetworkModule.kt` | No change needed — Retrofit supports both `Single` and `suspend` on same interface |

### No Change Required

| File | Reason |
|------|--------|
| `ReccomdationRequest.kt` | Data class, unchanged |
| `TransactionsResponse.kt` | Data class, unchanged |
| `BaseResponse.kt` | Still used by other endpoints |
| `BaseRepository.kt` | Already has `safeApiCall` — ready to use |
| `Resource.kt` | Already has Loading/Success/Failure |
| `ApiError.kt` | Already has all error types |
| `CoroutineModule.kt` | Already provides dispatchers |
| `AppComponent.kt` | Already includes CoroutineModule |

---

## 6. Edge Cases Checklist

| # | Edge Case | Current Handling | Migration Handling |
|---|-----------|-----------------|-------------------|
| 1 | Network failure (no connectivity) | `IOException` → `error.handle()` → generic error | `IOException` → `Resource.Failure(ApiError.Network)` → specific error message |
| 2 | Timeout | `SocketTimeoutException` → `error.handle()` | `Resource.Failure(ApiError.Timeout)` → specific timeout message |
| 3 | HTTP 401 Unauthorized | `HttpException(401)` → `error.handle()` | `Resource.Failure(ApiError.Unauthorized)` → navigate to login |
| 4 | HTTP 403 Access Denied | `HttpException(403)` → `error.handle()` | `Resource.Failure(ApiError.AccessDenied)` → access denied message |
| 5 | API returns `success: false` | `convertResponse()` throws `HttpException` | Must manually check `isSuccess` in `safeApiCall` block |
| 6 | Null/empty `transactions` list | `_res.transactions ?: emptyList()` | Same null-safe handling must be preserved |
| 7 | Null `searchAfter` | `hasMoreData = searchAfter != null` | Same logic preserved |
| 8 | Null `loadCounts` | `if(_tRes.fifth.loadCounts != null)` with try/catch | Same null checks preserved |
| 9 | Loading state | `dataLoadingLiveData.postValue(true/false)` | `Resource.Loading` emitted before API call |
| 10 | Pagination (hasMoreData) | `hasMoreData` flag + `searchAfter` cursor | Same pagination logic preserved |
| 11 | Fallback to supplier loads | On error → calls `fetchSupplierTransactions()` | Must preserve this fallback in coroutine error handling |
| 12 | Cancellation on ViewModel clear | `compositeDisposable.disposeAndClear()` | `viewModelScope` auto-cancels on `onCleared()` |
| 13 | Parallel call failure | `Single.zip()` fails if any call fails | `coroutineScope` + `async` — structured concurrency cancels siblings |
| 14 | Fragment lifecycle | `reobserve(viewLifecycleOwner)` | Same lifecycle-aware observation |
| 15 | `splitViewCount = true` variant | Separate call for count-only data | Must preserve as separate coroutine call |

### CRITICAL Edge Case: `BaseResponse.isSuccess` Check

The current `convertResponse()` extension checks `BaseResponse.isSuccess` and throws `HttpException` if false. In the coroutine path, the `safeApiCall` wrapper catches `HttpException` but does NOT automatically unwrap `BaseResponse`. 

**The migration MUST handle this by either:**
- (A) Checking `isSuccess` inside the `safeApiCall` lambda and throwing if false, OR
- (B) Creating a coroutine-specific `convertResponse` that unwraps `BaseResponse` inside the suspend call

**Recommended approach**: Option (A) — check inside `safeApiCall` lambda:
```kotlin
// Inside repository
suspend fun fetchRecommTransactionsSafe(...): Resource<TransactionsResponse> = safeApiCall {
    val response = recommendationService.recommendationTransactions(request)
    if (response.isSuccess) {
        response.responseData ?: throw Exception("Null response data")
    } else {
        throw response.toHttpException()
    }
}
```

---

## 7. Step-by-Step Migration Plan

### Phase 1: Service Layer (Additive — Zero Risk)

**Step 1.1**: Add suspend variant to `RecommendationService.kt`
- Add new method alongside existing RxJava method
- `suspend fun recommendationTransactionsSuspend(@Body request: ReccomdationRequest): BaseResponse<TransactionsResponse>`
- Same `@POST("/get_sp_loads")` annotation
- **Risk**: None — additive change, existing code untouched
- **Rollback**: Delete the new method

### Phase 2: Repository Layer (Additive — Zero Risk)

**Step 2.1**: Add suspend method to `TransactionsRepository.kt`
- Add new `suspend fun fetchRecommTransactionsSafe(...)` method
- Uses `safeApiCall` wrapper from `BaseRepository`
- Handles `BaseResponse.isSuccess` check inside the lambda
- Keep existing `fetchRecommTransactions()` RxJava method intact
- **Risk**: None — additive change
- **Rollback**: Delete the new method

### Phase 3: ViewModel Layer (High Complexity — Highest Risk)

This is the most complex phase due to the deeply nested `Single.zip()` and `.flatMap{}` chains.

**Step 3.1**: Add coroutine-based fetch methods to `HomeLoadsViewModel.kt`
- Add new methods alongside existing RxJava methods:
  - `fetchLoadsDataCoroutine()` — replaces `fetchLoadsData()`
  - `fetchMarketplaceLoadsDataCoroutine()` — replaces marketplace count fetch
- Use `viewModelScope.launch` with `Resource.Loading` emission
- Replace `Single.zip()` with `coroutineScope { async {} }` for parallel calls
- Replace `.flatMap{}` with sequential suspend calls
- **Risk**: Medium — complex logic translation
- **Temporary compatibility**: Both old and new methods coexist

**Step 3.2**: Add new LiveData for Resource-based state
- Add `val loadsResource: LiveData<Resource<List<...>>>` alongside existing `userLoadsData`
- **Risk**: Low — additive

**Step 3.3**: Switch callers to use new coroutine methods
- Update `fetchUserTransactions()` to call `fetchLoadsDataCoroutine()` instead of `fetchLoadsData()`
- Update `fetchSpotMarketplaceLoads()` to use coroutine variant for count fetch
- **Risk**: High — this is the switch-over point

**Step 3.4**: Remove old RxJava methods
- Only after validation confirms coroutine methods work correctly
- **Risk**: Medium — point of no return

### Phase 4: UI Layer (Low Risk)

**Step 4.1**: Update `HomeLoadsFragment.kt` observers
- If ViewModel exposes new `Resource<T>` LiveData, add observers with `when(resource)` handling
- If ViewModel continues to post to existing `userLoadsData` (recommended for minimal UI change), no Fragment changes needed
- **Risk**: Low
- **Recommended approach**: Keep existing LiveData contract, handle Resource internally in ViewModel

### Phase 5: Cleanup

**Step 5.1**: Remove old RxJava method from `RecommendationService.kt`
- Only after all callers migrated
- **Risk**: Low — but verify no other callers exist

**Step 5.2**: Remove old `fetchRecommTransactions()` from `TransactionsRepository.kt`
- Only after all ViewModel callers migrated
- **Risk**: Low

---

## 8. Dependency Map

```
ReccomdationRequest (unchanged)
        ↓ [used by]
RecommendationService.recommendationTransactionsSuspend() (NEW)
        ↓ [called by]
TransactionsRepository.fetchRecommTransactionsSafe() (NEW)
        ↓ [called by]
HomeLoadsViewModel.fetchLoadsDataCoroutine() (NEW)
        ↓ [posts to]
userLoadsData: MutableLiveData (EXISTING — same contract)
        ↓ [observed by]
HomeLoadsFragment (MINIMAL or NO changes)
```

### Parallel Dependencies in ViewModel
The `fetchLoadsData()` method zips `fetchRecommTransactions` with:
- `bidsRepository.bidsForLoads()` — Still RxJava (not migrated yet)
- `bidsRepository.bulkLowestBidsForLoads()` — Still RxJava
- `transactionsRepository.fetchIntracityRecommTransactions()` — Still RxJava
- `transactionsRepository.fetchSpotMarketplaceTransactions()` — Still RxJava

**CRITICAL DECISION**: Since the parallel calls are still RxJava, the ViewModel migration has two options:

**Option A — Full coroutine migration of the entire fetch chain**:
- Migrate ALL parallel calls to suspend functions simultaneously
- Cleaner result but much larger scope and risk

**Option B — Bridge pattern (RECOMMENDED)**:
- Convert only `fetchRecommTransactions` to suspend
- Use `kotlinx-coroutines-rx2` bridge (`Single.await()`) for remaining RxJava calls
- Migrate other calls incrementally later
- Add dependency: `implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-rx2:1.6.4'`

---

## 9. Validation Strategy

### Build Validation
- [ ] `./gradlew assembleDevelopmentDebug` compiles without errors
- [ ] `./gradlew lint` passes with no new warnings
- [ ] Zero diagnostics errors on all modified files

### Unit Test Validation
- [ ] Existing `BaseRepositoryTest.kt` passes (12 tests)
- [ ] New test: `TransactionsRepositoryTest.kt` — test `fetchRecommTransactionsSafe()` with mock service
- [ ] New test: Verify `safeApiCall` handles `BaseResponse.isSuccess = false` correctly
- [ ] New test: Verify parallel call failure cancels siblings

### Runtime Behavior Validation
- [ ] Intracity tab loads correctly with counts
- [ ] Intercity (Delhivery) tab loads correctly with counts
- [ ] Non-Delhivery tab loads correctly with counts
- [ ] Marketplace tab shows correct cross-tab counts
- [ ] Pagination works (scroll to load more)
- [ ] Pull-to-refresh works
- [ ] Loading indicator shows/hides correctly
- [ ] Error state shows on network failure
- [ ] Fallback to supplier loads works on recommendation API failure
- [ ] Tab badge counts are accurate
- [ ] `searchAfter` pagination cursor works correctly
- [ ] `splitViewCount` count-only call returns correct data
- [ ] App doesn't crash on rapid tab switching
- [ ] App doesn't crash on screen rotation during load

### UI Validation
- [ ] Loading spinner appears before data loads
- [ ] Error messages display correctly for each error type
- [ ] Empty state shows when no loads available
- [ ] List items render correctly with bid data
- [ ] Filter counts match across all tabs

---

## 10. Risk Mitigation

| # | Risk | Likelihood | Impact | Mitigation |
|---|------|-----------|--------|------------|
| 1 | Complex `Single.zip()` translation breaks parallel behavior | High | High | Use bridge pattern (`Single.await()`) to minimize translation scope |
| 2 | `BaseResponse.isSuccess` check missed in coroutine path | Medium | High | Explicit check in `safeApiCall` lambda + unit test |
| 3 | Fallback to `fetchSupplierTransactions()` broken | Medium | High | Preserve exact same error handling logic in coroutine catch block |
| 4 | Pagination state (`searchAfter`, `hasMoreData`) corrupted | Medium | Medium | Keep same mutable state management, test pagination thoroughly |
| 5 | Race condition on rapid tab switching | Low | Medium | `viewModelScope` auto-cancels previous coroutine on new launch |
| 6 | Other RxJava callers of same service method break | Low | High | Keep RxJava method alongside suspend method during transition |
| 7 | `convertResponse()` behavior difference | Medium | High | Unit test both paths return identical results for same input |
| 8 | LiveData contract change breaks Fragment | Low | Medium | Keep same LiveData types, handle Resource internally in ViewModel |

---

## 11. Rollback Strategy

### Rollback Levels

**Level 1 — Method-level rollback (Safest)**:
- Switch `fetchUserTransactions()` back to calling `fetchLoadsData()` instead of `fetchLoadsDataCoroutine()`
- One-line change in ViewModel
- All old methods still exist

**Level 2 — Full rollback**:
- Revert all changes in `HomeLoadsViewModel.kt`
- Revert `TransactionsRepository.kt` (remove suspend method)
- Revert `RecommendationService.kt` (remove suspend method)
- No Fragment changes to revert (if recommended approach followed)

**Level 3 — Git revert**:
- `git revert <commit-hash>` for the migration commit
- Clean rollback if changes were in a single commit

### Rollback Triggers
- Build failure after migration
- Crash on any load tab
- Incorrect data displayed
- Pagination broken
- Loading state stuck
- Performance regression (API calls taking longer)

---

## 12. Impacted Files Summary

### Files to Modify

| # | File | Change Type | Risk |
|---|------|-------------|------|
| 1 | `RecommendationService.kt` | Add suspend method | Low |
| 2 | `TransactionsRepository.kt` | Add suspend method | Low |
| 3 | `HomeLoadsViewModel.kt` | Major refactor of fetch methods | High |
| 4 | `HomeLoadsFragment.kt` | Minimal or none (if LiveData contract preserved) | Low |

### Files to Create

| # | File | Purpose |
|---|------|---------|
| 1 | `TransactionsRepositoryTest.kt` | Unit tests for new suspend method |

### Files to Potentially Modify

| # | File | Condition |
|---|------|-----------|
| 1 | `app/build.gradle` | If `kotlinx-coroutines-rx2` bridge needed |
| 2 | `BaseViewModel.kt` | If coroutine scope support needed alongside RxJava |

### Files NOT Modified

| File | Reason |
|------|--------|
| `ReccomdationRequest.kt` | Data class unchanged |
| `TransactionsResponse.kt` | Data class unchanged |
| `BaseResponse.kt` | Still used by other endpoints |
| `BaseRepository.kt` | Already has safeApiCall |
| `Resource.kt` | Already has Loading/Success/Failure |
| `ApiError.kt` | Already has all error types |
| `NetworkModule.kt` | Retrofit handles both Single and suspend |
| `DelhiveryExtensions.kt` | convertResponse() still used by other RxJava callers |

---

## 13. Recommended Migration Order

```
1. RecommendationService.kt          [Add suspend method — LOW RISK]
2. TransactionsRepository.kt         [Add suspend method — LOW RISK]  
3. app/build.gradle                   [Add coroutines-rx2 bridge if needed — LOW RISK]
4. TransactionsRepositoryTest.kt      [Create tests — NO RISK]
5. HomeLoadsViewModel.kt              [Add coroutine methods — HIGH RISK, keep old methods]
6. HomeLoadsViewModel.kt              [Switch callers to new methods — HIGH RISK]
7. HomeLoadsFragment.kt               [Update observers if needed — LOW RISK]
8. Validate all scenarios
9. HomeLoadsViewModel.kt              [Remove old RxJava methods — MEDIUM RISK]
10. RecommendationService.kt          [Remove old RxJava method — LOW RISK]
11. TransactionsRepository.kt         [Remove old RxJava method — LOW RISK]
```

**Total estimated files changed**: 3-5
**Highest risk area**: `HomeLoadsViewModel.kt` — complex `Single.zip()` and `.flatMap{}` chains with 5 parallel API calls
