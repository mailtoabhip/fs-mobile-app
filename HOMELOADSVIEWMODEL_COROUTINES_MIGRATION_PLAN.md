# HomeLoadsViewModel Coroutines Migration Plan

## Executive Summary

This document provides a complete migration plan for converting `HomeLoadsViewModel` from RxJava to Kotlin Coroutines. The migration affects the `POST /get_sp_loads` API flow across Service, Repository, and ViewModel layers.

**Scope:**
- **Service Layer**: 5 methods across 3 services (RecommendationService, BidService, TransactionService)
- **Repository Layer**: 5 methods across 2 repositories (TransactionsRepository, BidsRepository)
- **ViewModel Layer**: 4 fetch methods in HomeLoadsViewModel
- **UI Layer**: No changes required (LiveData contracts preserved)

**Key Migration Patterns:**
- `Single<BaseResponse<T>>` → `suspend fun` returning `BaseResponse<T>`
- `compositeDisposable += ...subscribe{}` → `viewModelScope.launch { when(Resource) {} }`
- `Single.zip()` → `coroutineScope { async {} }`
- `.flatMap{}` → Sequential suspend calls
- Error handling via `safeApiCall` and `Resource<T>` sealed class

**Critical Behaviors Preserved:**
- Pagination state (searchAfter, hasMoreData, offset, total)
- Tab count aggregation (INTERCITY, NON_DELHIVERY, marketplace)
- Loading states (dataLoadingLiveData true→false)
- Error fallback (non-intracity → fetchSupplierTransactions)
- User data population (populatePaymentFields)
- LiveData contracts unchanged

---

## 1. RxJava → Coroutines Translation Patterns

### Pattern 1: User Data Fetch (Entry Point)

**Current RxJava Pattern:**
```kotlin
fun fetchUserTransactions(...) {
    dataLoadingLiveData.postValue(true)
    
    if (user == null) {
        compositeDisposable += userRepository.getUser(false)
            .onBackground()
            .subscribe { userModel, error ->
                if (!error && userModel != null) {
                    this.user = userModel
                    fetchLoadsData(...)
                } else {
                    error?.handle()
                    dataLoadingLiveData.postValue(false)
                }
            }
    } else {
        fetchLoadsData(...)
    }
}
```


**Migrated Coroutine Pattern:**
```kotlin
private var currentFetchJob: Job? = null

fun fetchUserTransactions(...) {
    dataLoadingLiveData.postValue(true)
    
    // Cancel previous fetch for rapid tab switching
    currentFetchJob?.cancel()
    currentFetchJob = viewModelScope.launch {
        try {
            if (user == null) {
                // Keep RxJava getUser() call (outside migration scope)
                // OR migrate to suspend if desired
            }
            fetchLoadsData(...)
        } catch (e: Exception) {
            if (e !is CancellationException) {
                e.handle()
            }
        } finally {
            dataLoadingLiveData.postValue(false)
        }
    }
}
```

**Key Changes:**
- Add `currentFetchJob: Job?` field to track and cancel previous fetches
- Wrap in `viewModelScope.launch` for lifecycle-aware cancellation
- Use try/catch/finally for error handling and loading state cleanup
- Keep `getUser()` as RxJava (outside migration scope) or migrate separately

---

### Pattern 2: Sequential API Call with flatMap

**Current RxJava Pattern:**
```kotlin
compositeDisposable += transactionsRepository.fetchRecommTransactions(...)
    .flatMap { _res ->
        // Process response
        searchAfter = _res.searchAfter
        hasMoreData = searchAfter != null
        
        // Chain next call
        Single.zip(...)
    }
    .onBackground()
    .subscribe { result, error ->
        if (!error && result != null) {
            // Handle success
        } else {
            // Handle error
        }
    }
```

**Migrated Coroutine Pattern:**
```kotlin
viewModelScope.launch {
    val primaryResult = transactionsRepository.fetchRecommTransactions(...)
    
    when (primaryResult) {
        is Resource.Success -> {
            val _res = primaryResult.data!!
            // Process response
            searchAfter = _res.searchAfter
            hasMoreData = searchAfter != null
            
            // Continue with parallel calls
            val parallelResults = coroutineScope { async {} }
            // Handle success
        }
        is Resource.Failure -> {
            // Handle error or fallback
        }
        is Resource.Loading -> { /* not expected */ }
    }
}
```


**Key Changes:**
- Replace `.flatMap{}` with sequential `when(Resource)` branches
- `Resource.Success` branch contains the data processing logic
- `Resource.Failure` branch handles errors or triggers fallback
- No need for `onBackground()` — coroutines handle threading

---

### Pattern 3: Parallel API Calls with Single.zip()

**Current RxJava Pattern (5 parallel calls):**
```kotlin
Single.zip(
    bidsRepository.bidsForLoads(_res.transactions).subscribeOn(Schedulers.io()),
    bidsRepository.bulkLowestBidsForLoads(_res.transactions).subscribeOn(Schedulers.io()),
    transactionsRepository.fetchIntracityRecommTransactions(...),
    transactionsRepository.fetchRecommTransactions(..., splitViewCount = true),
    marketplaceSingle,
    { t1, t2, t3, t4, t5 ->
        SixTuple(t1.first, t1.second, t2.second, t3, t4, t5)
    }
)
```

**Migrated Coroutine Pattern:**
```kotlin
data class ParallelResults(
    val bids: Resource<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>>,
    val lowestBids: Resource<Pair<List<HomeBidsRequestItemData>, BulkLowestBidsResponse>>,
    val intracity: Resource<TransactionsResponse>,
    val splitCount: Resource<TransactionsResponse>,
    val marketplace: Resource<SpotMarketplaceLoadsData>
)

val parallelResults = coroutineScope {
    val bidsDeferred = async { bidsRepository.bidsForLoads(_res.transactions) }
    val lowestBidsDeferred = async { bidsRepository.bulkLowestBidsForLoads(_res.transactions) }
    val intracityDeferred = async { transactionsRepository.fetchIntracityRecommTransactions(...) }
    val splitCountDeferred = async { transactionsRepository.fetchRecommTransactions(..., splitViewCount = true) }
    val marketplaceDeferred = async {
        if (hasMarketplaceAccess) {
            transactionsRepository.fetchSpotMarketplaceTransactions(onlyCount = true, limit = 100, offset = 0)
        } else {
            Resource.Success(SpotMarketplaceLoadsData(totalCount = 0, limit = 0, offset = 0, hasNext = false, transactions = emptyList()))
        }
    }
    
    ParallelResults(
        bidsDeferred.await(),
        lowestBidsDeferred.await(),
        intracityDeferred.await(),
        splitCountDeferred.await(),
        marketplaceDeferred.await()
    )
}

// All results are now available in parallelResults
```

**Key Changes:**
- Replace `Single.zip()` with `coroutineScope { async {} }`
- Each `async` block launches a parallel coroutine
- Use `await()` to collect results
- Define a data class to hold combined results (cleaner than tuples)
- No need for `subscribeOn(Schedulers.io())` — repositories handle threading
- If any `async` fails, all siblings are cancelled (structured concurrency)

---
### Pattern 4: Error Handling with Fallback

**Current RxJava Pattern:**
```kotlin
.subscribe { result, error ->
    if (!error && result != null) {
        // Handle success
        userLoadsData.postValue(items)
    } else {
        // Fallback to supplier loads
        fetchSupplierTransactions(...)
    }
    dataLoadingLiveData.postValue(false)
}
```

**Migrated Coroutine Pattern:**
```kotlin
when (primaryResult) {
    is Resource.Success -> {
        // Handle success
        userLoadsData.postValue(items)
    }
    is Resource.Failure -> {
        // Fallback to supplier loads
        fetchSupplierTransactions(
            totalFetchTitle > 0,
            selectedFilter,
            demandType,
            infoSearch,
            excludeTruckTypes
        )
    }
    is Resource.Loading -> { /* not expected */ }
}
```

**Key Changes:**
- `Resource.Failure` branch replaces the `error` check
- Fallback logic moves into the `Failure` branch
- No need for null checks — `Resource.Success` guarantees data
- Intracity branch does NOT have fallback — posts error directly to UI

---

### Pattern 5: Loading State Management

**Current RxJava Pattern:**
```kotlin
dataLoadingLiveData.postValue(true)
compositeDisposable += repository.fetch()
    .subscribe { result, error ->
        // Handle result
        dataLoadingLiveData.postValue(false)
    }
```

**Migrated Coroutine Pattern:**
```kotlin
dataLoadingLiveData.postValue(true)
viewModelScope.launch {
    try {
        val result = repository.fetch()
        when (result) {
            is Resource.Success -> { /* handle */ }
            is Resource.Failure -> { /* handle */ }
        }
    } finally {
        dataLoadingLiveData.postValue(false)
    }
}
```

**Key Changes:**
- Use `try/finally` to ensure loading state is always reset
- `finally` block executes even if coroutine is cancelled
- No need for separate error handling for loading state

---

### Pattern 6: Pagination State Updates

**Current RxJava Pattern:**
```kotlin
.flatMap { _res ->
    searchAfter = _res.searchAfter
    hasMoreData = searchAfter != null
    offset = _res.offset
    total = _res.transactions?.size ?: 0
    if (total == 0) {
        searchAfter = null
        hasMoreData = false
    }
    // Continue with next call
}
```

**Migrated Coroutine Pattern:**
```kotlin
when (primaryResult) {
    is Resource.Success -> {
        val _res = primaryResult.data!!
        searchAfter = _res.searchAfter
        hasMoreData = searchAfter != null
        offset = _res.offset
        total = _res.transactions?.size ?: 0
        if (total == 0) {
            searchAfter = null
            hasMoreData = false
        }
        // Continue with parallel calls
    }
}
```

**Key Changes:**
- Pagination state updates move into `Resource.Success` branch
- Same logic, different structure
- State updates happen before parallel calls

---
## 2. Method-by-Method Migration Guide

### 2.1 fetchLoadsData() - Intracity Branch

**Current Implementation:**
```kotlin
if (selectedFilter == DemandType.Intracity.type) {
    compositeDisposable += transactionsRepository.fetchIntracityRecommTransactions(...)
        .flatMap { _res ->
            // Update pagination state
            offset = _res.offset ?: 0
            total = _res.transactions?.size ?: 0
            searchAfter = _res.searchAfter
            
            // 2 parallel calls
            Single.zip(
                transactionsRepository.fetchRecommTransactions(..., splitViewCount = true),
                marketplaceSingle,
                BiFunction { recommTrans, marketplaceTrans ->
                    Triple(_res.transactions, _res.total, Pair(recommTrans, marketplaceTrans))
                }
            )
        }
        .onBackground()
        .subscribe { _tRes, error ->
            if (!error && _tRes != null) {
                // Build UI items
                // Extract tab counts
                // Post to LiveData
            }
            dataLoadingLiveData.postValue(false)
        }
}
```

**Migrated Implementation:**
```kotlin
if (selectedFilter == DemandType.Intracity.type) {
    viewModelScope.launch {
        try {
            // Primary call: Fetch intracity transactions
            val primaryResult = transactionsRepository.fetchIntracityRecommTransactions(
                offset, demandType, vehicleTypes, excludeTruckTypes,
                filterVehicleType, true, null, searchAfter
            )
            
            when (primaryResult) {
                is Resource.Success -> {
                    val _res = primaryResult.data!!
                    
                    // Update pagination state
                    offset = _res.offset ?: 0
                    total = _res.transactions?.size ?: 0
                    searchAfter = _res.searchAfter
                    if (total == 0) {
                        searchAfter = null
                        hasMoreData = false
                    }
                    
                    loadsCountLiveData.postValue(total)
                    
                    // 2 parallel calls for split counts + marketplace
                    val parallelResults = coroutineScope {
                        val splitCountDeferred = async {
                            transactionsRepository.fetchRecommTransactions(
                                offset,
                                filterDemandTypeForRecommendations(userPrefs.demandType),
                                vehicleTypes, excludeTruckTypes, filterVehicleType,
                                true, splitViewCount = true, searchAfter = null
                            )
                        }
                        val marketplaceDeferred = async {
                            if (hasMarketplaceAccess) {
                                transactionsRepository.fetchSpotMarketplaceTransactions(
                                    onlyCount = true, limit = 100, offset = 0
                                )
                            } else {
                                Resource.Success(SpotMarketplaceLoadsData(
                                    totalCount = 0, limit = 0, offset = 0,
                                    hasNext = false, transactions = emptyList()
                                ))
                            }
                        }
                        Pair(splitCountDeferred.await(), marketplaceDeferred.await())
                    }
                    
                    // Extract tab counts
                    var intercityCount = 0
                    var nonDlvCount = 0
                    var marketplaceCount = 0
                    
                    when (val splitResult = parallelResults.first) {
                        is Resource.Success -> {
                            splitResult.data?.loadCounts?.all?.forEach { item ->
                                when (item.key) {
                                    "INTERCITY" -> intercityCount = item.count ?: 0
                                    "NON_DELHIVERY" -> nonDlvCount = item.count ?: 0
                                }
                            }
                        }
                        else -> { /* counts remain 0 */ }
                    }
                    
                    when (val marketplaceResult = parallelResults.second) {
                        is Resource.Success -> {
                            marketplaceCount = marketplaceResult.data?.totalCount ?: 0
                            cachedMarketplaceCount = marketplaceCount
                        }
                        else -> { /* count remains 0 */ }
                    }
                    
                    // Build UI items list
                    mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        add(Pair(HomeLoadsProgressItem(), Remove))
                        
                        val loads = _res.transactions ?: emptyList()
                        
                        add(Pair(HomeLoadsSearchItem(HomeLoadsSearchItemData(vehicleTypes)), AddUpdate))
                        
                        val count = total + intercityCount + nonDlvCount + marketplaceCount
                        fullLoadsCountLiveData.postValue(count)
                        
                        add(Pair(
                            HomeLoadsFilterItem(HomeLoadsFilterItemData(
                                selectedFilter, total, intercityCount, nonDlvCount,
                                marketplaceCount, userPrefs.demandType
                            )), AddUpdate
                        ))
                        
                        if (!paginate && userPrefs.verificationStatus.equals("failed")) {
                            add(Pair(HomeLoadsKycPendingItem(), AddUpdate))
                        }
                        
                        if (!paginate) {
                            add(Pair(HomeLoadsTruckPriorityAccessItem(), AddUpdate))
                        }
                        
                        if (total == 0 && !paginate) {
                            add(Pair(HomeLoadsWarningItem_NoLoads, AddUpdate))
                        }
                        
                        for ((index, load) in loads.toMutableList().withIndex()) {
                            load.transactionId?.let { txnIds.add(it) }
                            populatePaymentFields(load)
                            add(Pair(HomeLoadsRequestItem(load), Add))
                        }
                        
                        if (!hasMoreData && !hasOrionLoadOnce && more_default_loads) {
                            add(Pair(HomeLoadsInfoItem(), Remove))
                            add(Pair(HomeLoadsInfoItem(), AddUpdate))
                        }
                        add(Pair(HomeLoadsMoreInfoItem(), Remove))
                        add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
                    }.let {
                        userLoadsData.postValue(it)
                        if (_res.transactions?.isNotEmpty() == true && paginateCount == 0) {
                            intracityListShownTracked.postValue(true)
                        }
                    }
                }
                
                is Resource.Failure -> {
                    // Post error state to UI (NO fallback for intracity)
                    primaryResult.apiError?.handle()
                }
                
                is Resource.Loading -> { /* not expected */ }
            }
        } finally {
            dataLoadingLiveData.postValue(false)
        }
    }
}
```

**Key Points:**
- **2 parallel calls**: split count + marketplace count
- **NO fallback** on error (unlike non-intracity branch)
- Tab count extraction from `loadCounts.all`
- Pagination state updates in `Resource.Success` branch

---
### 2.2 fetchLoadsData() - Non-Intracity Branch

**Current Implementation:**
```kotlin
else {
    compositeDisposable += transactionsRepository.fetchRecommTransactions(...)
        .flatMap { _res ->
            // Update pagination state
            searchAfter = _res.searchAfter
            hasMoreData = searchAfter != null
            
            // 5 parallel calls
            Single.zip(
                bidsRepository.bidsForLoads(_res.transactions),
                bidsRepository.bulkLowestBidsForLoads(_res.transactions),
                transactionsRepository.fetchIntracityRecommTransactions(...),
                transactionsRepository.fetchRecommTransactions(..., splitViewCount = true),
                marketplaceSingle,
                { t1, t2, t3, t4, t5 -> SixTuple(...) }
            )
        }
        .onBackground()
        .subscribe { _tRes, error ->
            if (!error && _tRes != null) {
                // Build UI items
            } else {
                // FALLBACK to supplier loads
                fetchSupplierTransactions(...)
            }
            dataLoadingLiveData.postValue(false)
        }
}
```

**Migrated Implementation:**
```kotlin
else {
    viewModelScope.launch {
        try {
            // Primary call: Fetch recommended transactions
            val primaryResult = transactionsRepository.fetchRecommTransactions(
                offset,
                filterDemandTypeForRecommendations(userPrefs.demandType),
                vehicleTypes, excludeTruckTypes, filterVehicleType,
                true, searchAfter = searchAfter
            )
            
            when (primaryResult) {
                is Resource.Success -> {
                    val _res = primaryResult.data!!
                    
                    // Update pagination state
                    searchAfter = _res.searchAfter
                    hasMoreData = searchAfter != null
                    offset = _res.offset
                    total = _res.transactions?.size ?: 0
                    if (total == 0) {
                        searchAfter = null
                        hasMoreData = false
                    }
                    loadsCount += total
                    
                    // 5 parallel calls
                    data class ParallelResults(
                        val bids: Resource<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>>,
                        val lowestBids: Resource<Pair<List<HomeBidsRequestItemData>, BulkLowestBidsResponse>>,
                        val intracity: Resource<TransactionsResponse>,
                        val splitCount: Resource<TransactionsResponse>,
                        val marketplace: Resource<SpotMarketplaceLoadsData>
                    )
                    
                    val parallelResults = coroutineScope {
                        val bidsDeferred = async {
                            bidsRepository.bidsForLoads(_res.transactions)
                        }
                        val lowestBidsDeferred = async {
                            bidsRepository.bulkLowestBidsForLoads(_res.transactions)
                        }
                        val intracityDeferred = async {
                            transactionsRepository.fetchIntracityRecommTransactions(
                                offset, userPrefs.demandType, vehicleTypes,
                                excludeTruckTypes, filterVehicleType, true
                            )
                        }
                        val splitCountDeferred = async {
                            transactionsRepository.fetchRecommTransactions(
                                offset,
                                filterDemandTypeForRecommendations(userPrefs.demandType),
                                vehicleTypes, excludeTruckTypes, filterVehicleType,
                                true, splitViewCount = true, searchAfter = null
                            )
                        }
                        val marketplaceDeferred = async {
                            if (hasMarketplaceAccess) {
                                transactionsRepository.fetchSpotMarketplaceTransactions(
                                    onlyCount = true, limit = 100, offset = 0
                                )
                            } else {
                                Resource.Success(SpotMarketplaceLoadsData(
                                    totalCount = 0, limit = 0, offset = 0,
                                    hasNext = false, transactions = emptyList()
                                ))
                            }
                        }
                        
                        ParallelResults(
                            bidsDeferred.await(),
                            lowestBidsDeferred.await(),
                            intracityDeferred.await(),
                            splitCountDeferred.await(),
                            marketplaceDeferred.await()
                        )
                    }
                    
                    // Extract data from parallel results
                    val loads = _res.transactions ?: emptyList()
                    val bids = when (parallelResults.bids) {
                        is Resource.Success -> parallelResults.bids.data?.second ?: emptyList()
                        else -> emptyList()
                    }
                    
                    // Extract tab counts
                    var intercityCount = 0
                    var nonDlvCount = 0
                    var marketplaceCount = 0
                    
                    when (val splitResult = parallelResults.splitCount) {
                        is Resource.Success -> {
                            splitResult.data?.loadCounts?.all?.forEach { item ->
                                when (item.key) {
                                    "INTERCITY" -> intercityCount = item.count ?: 0
                                    "NON_DELHIVERY" -> nonDlvCount = item.count ?: 0
                                }
                            }
                        }
                        else -> { /* counts remain 0 */ }
                    }
                    
                    when (val marketplaceResult = parallelResults.marketplace) {
                        is Resource.Success -> {
                            marketplaceCount = marketplaceResult.data?.totalCount ?: 0
                            cachedMarketplaceCount = marketplaceCount
                        }
                        else -> { /* count remains 0 */ }
                    }
                    
                    val count = if (selectedFilter == DemandType.Internal.type) {
                        intercityCount
                    } else {
                        nonDlvCount
                    }
                    loadsCountLiveData.postValue(count)
                    
                    // Build UI items list
                    mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                        add(Pair(HomeLoadsProgressItem(), Remove))
                        
                        add(Pair(HomeLoadsSearchItem(HomeLoadsSearchItemData(vehicleTypes)), AddUpdate))
                        
                        val totalCount = when (val splitResult = parallelResults.splitCount) {
                            is Resource.Success -> splitResult.data?.total ?: 0
                            else -> 0
                        }
                        val fullCount = totalCount + intercityCount + nonDlvCount + marketplaceCount
                        fullLoadsCountLiveData.postValue(fullCount)
                        
                        add(Pair(
                            HomeLoadsFilterItem(HomeLoadsFilterItemData(
                                selectedFilter, totalCount, intercityCount, nonDlvCount,
                                marketplaceCount, userPrefs.demandType
                            )), AddUpdate
                        ))
                        
                        if (!paginate && userPrefs.verificationStatus.equals("failed")) {
                            add(Pair(HomeLoadsKycPendingItem(), AddUpdate))
                        }
                        
                        if (!paginate) {
                            add(Pair(HomeLoadsTruckPriorityAccessItem(), AddUpdate))
                        }
                        
                        if (total == 0 && !paginate) {
                            add(Pair(HomeLoadsWarningItem_NoLoads, AddUpdate))
                        }
                        
                        for ((index, load) in loads.toMutableList().withIndex()) {
                            load.transactionId?.let { txnIds.add(it) }
                            populatePaymentFields(load)
                            
                            if (index.rem(HomeLoadsAddTruckItemDataConfig) == 0 && index != 0) {
                                add(Pair(HomeLoadsAddTruckItem(), Add))
                            }
                            add(Pair(HomeLoadsRequestItem(load), Add))
                        }
                        
                        if (!hasMoreData && !hasOrionLoadOnce && more_default_loads && totalFetchTitle < total) {
                            add(Pair(HomeLoadsInfoItem(), AddUpdate))
                        }
                        add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
                    }.let {
                        userLoadsData.postValue(it)
                        if (loads.isNotEmpty() && paginateCount == 0) {
                            intercityListShownTracked.postValue(true)
                        }
                    }
                }
                
                is Resource.Failure -> {
                    // CRITICAL: Fallback to supplier loads on error
                    fetchSupplierTransactions(
                        totalFetchTitle > 0,
                        selectedFilter,
                        demandType,
                        infoSearch,
                        excludeTruckTypes
                    )
                }
                
                is Resource.Loading -> { /* not expected */ }
            }
        } finally {
            dataLoadingLiveData.postValue(false)
        }
    }
}
```

**Key Points:**
- **5 parallel calls**: bids, lowestBids, intracity, splitCount, marketplace
- **CRITICAL FALLBACK**: On `Resource.Failure`, call `fetchSupplierTransactions()`
- Tab count extraction from `loadCounts.all`
- Conditional marketplace call based on `hasMarketplaceAccess`

---
### 2.3 fetchSupplierLoadsData()

**Current Implementation:**
```kotlin
private fun fetchSupplierLoadsData(...) {
    compositeDisposable += transactionsRepository.fetchLoadBoardTransactions(...)
        .flatMap { t ->
            // Update pagination state
            offsetFetch = t.offset
            totalFetchTitle = total + t.total
            totalFetch = t.total
            hasMoreData = t.hasNext
            
            // 3 parallel calls
            Single.zip(
                bidsRepository.bidsForLoads(t.transactions),
                bidsRepository.bulkLowestBidsForLoads(t.transactions),
                transactionsRepository.fetchIntracityRecommTransactions(..., onlyCount = true),
                Function3 { t1, t2, t3 ->
                    Quintuple(t1.first, t1.second, t2.second, t3, t)
                }
            )
        }
        .onBackground()
        .subscribe { _tRes, error ->
            if (!error && _tRes != null) {
                // Build UI items
                // Fetch marketplace count inline (separate call)
            } else {
                // Handle error
            }
            dataLoadingLiveData.postValue(false)
        }
}
```

**Migrated Implementation:**
```kotlin
private suspend fun fetchSupplierLoadsData(
    paginate: Boolean, selectedFilter: String, demandType: String,
    infoSearch: Boolean, excludeTruckTypes: String?
) {
    val primaryResult = transactionsRepository.fetchLoadBoardTransactions(
        offsetFetch, demandType, vehicleTypes, excludeTruckTypes,
        filterVehicleType, true, transactionIds
    )
    
    when (primaryResult) {
        is Resource.Success -> {
            val t = primaryResult.data!!
            
            // Update pagination state
            offsetFetch = t.offset
            totalFetchTitle = total + t.total
            totalFetch = t.total
            hasMoreData = t.hasNext
            loadPricePercent = t.loadPricePercent
            more_default_loads = t.more_loads
            loadsCountLiveData.postValue(totalFetchTitle)
            
            // 3 parallel calls
            data class SupplierParallelResults(
                val bids: Resource<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>>,
                val lowestBids: Resource<Pair<List<HomeBidsRequestItemData>, BulkLowestBidsResponse>>,
                val intracity: Resource<TransactionsResponse>
            )
            
            val parallelResults = coroutineScope {
                val bidsDeferred = async {
                    bidsRepository.bidsForLoads(t.transactions)
                }
                val lowestBidsDeferred = async {
                    bidsRepository.bulkLowestBidsForLoads(t.transactions)
                }
                val intracityDeferred = async {
                    transactionsRepository.fetchIntracityRecommTransactions(
                        offset, demandType, vehicleTypes, excludeTruckTypes,
                        filterVehicleType, true, onlyCount = true
                    )
                }
                
                SupplierParallelResults(
                    bidsDeferred.await(),
                    lowestBidsDeferred.await(),
                    intracityDeferred.await()
                )
            }
            
            // Fetch marketplace count (separate call, not blocking)
            var marketplaceCount = 0
            viewModelScope.launch {
                val marketplaceResult = transactionsRepository.fetchSpotMarketplaceTransactions(
                    onlyCount = true, limit = 100, offset = 0
                )
                when (marketplaceResult) {
                    is Resource.Success -> {
                        marketplaceCount = marketplaceResult.data?.totalCount ?: 0
                        cachedMarketplaceCount = marketplaceCount
                    }
                    else -> { /* count remains 0 */ }
                }
            }
            
            // Extract data
            val loadsWithBids = when (parallelResults.bids) {
                is Resource.Success -> parallelResults.bids.data?.first ?: emptyList()
                else -> emptyList()
            }
            val bids = when (parallelResults.bids) {
                is Resource.Success -> parallelResults.bids.data?.second ?: emptyList()
                else -> emptyList()
            }
            val bidTransactionIds = bids.map { it.transactionId }.toSet()
            val loads = loadsWithBids.filter { load ->
                load.transactionId !in bidTransactionIds
            }
            
            // Extract counts
            var intercityCount = 0
            var nonDlvCount = 0
            when (val intracityResult = parallelResults.intracity) {
                is Resource.Success -> {
                    if (demandType == DemandType.Internal.type) {
                        intercityCount = intracityResult.data?.total ?: 0
                    } else {
                        nonDlvCount = intracityResult.data?.total ?: 0
                    }
                }
                else -> { /* counts remain 0 */ }
            }
            
            // Build UI items
            mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                add(Pair(HomeLoadsProgressItem(), Remove))
                
                add(Pair(HomeLoadsSearchItem(HomeLoadsSearchItemData(vehicleTypes)), AddUpdate))
                
                val count = t.total + intercityCount + nonDlvCount + marketplaceCount
                fullLoadsCountLiveData.postValue(count)
                
                add(Pair(
                    HomeLoadsFilterItem(HomeLoadsFilterItemData(
                        selectedFilter, t.total, intercityCount, nonDlvCount,
                        marketplaceCount, userPrefs.demandType
                    )), AddUpdate
                ))
                
                if (!paginate) {
                    add(Pair(HomeLoadsTruckPriorityAccessItem(), AddUpdate))
                }
                
                if (total == 0 && !paginate) {
                    add(Pair(HomeLoadsWarningItem_NoLoads, AddUpdate))
                }
                
                for ((index, load) in loads.toMutableList().withIndex()) {
                    populatePaymentFields(load)
                    if (index.rem(HomeLoadsAddTruckItemDataConfig) == 0 && index != 0) {
                        add(Pair(HomeLoadsAddTruckItem(), Add))
                    }
                    add(Pair(HomeLoadsRequestItem(load), Add))
                }
                
                if (!hasMoreData && !hasOrionLoadOnce) {
                    if (more_default_loads) {
                        add(Pair(HomeLoadsInfoItem(), Remove))
                        add(Pair(HomeLoadsInfoItem(), AddUpdate))
                    } else {
                        add(Pair(HomeLoadsInfoItem(), Remove))
                    }
                    add(Pair(HomeLoadsMoreInfoItem(), Remove))
                    add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
                }
            }.let {
                userLoadsDataFetch.postValue(it)
            }
        }
        
        is Resource.Failure -> {
            mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                add(Pair(HomeLoadsProgressItem(), Remove))
                if (total == 0) {
                    add(Pair(HomeLoadsWarningItem_TimeOut, AddUpdate))
                }
                add(Pair(HomeLoadsMoreInfoItem(), Remove))
                add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
                hasMoreData = false
            }.let {
                if (!userLoadsData.value.isNullOrEmpty() && totalFetch > 0) {
                    userLoadsData.value?.let { existing -> it.addAll(existing) }
                }
                userLoadsDataFetch.postValue(it)
            }
        }
        
        is Resource.Loading -> { /* not expected */ }
    }
}
```

**Key Points:**
- **3 parallel calls**: bids, lowestBids, intracity count
- Marketplace count fetched separately (non-blocking)
- Filter loads to exclude those with existing bids
- Posts to `userLoadsDataFetch` (different LiveData than main fetch)

---
### 2.4 fetchMarketplaceLoadsData()

**Current Implementation:**
```kotlin
private fun fetchMarketplaceLoadsData(...) {
    compositeDisposable += transactionsRepository.fetchSpotMarketplaceTransactions(onlyCount = true, ...)
        .flatMap { countRes ->
            // Store count
            total = countRes.totalCount ?: 0
            cachedMarketplaceCount = total
            
            // Fetch actual data
            transactionsRepository.fetchSpotMarketplaceTransactions(onlyCount = false, ...)
        }
        .flatMap { _res ->
            // Update pagination state
            offset = _res.offset ?: 0
            hasMoreData = _res.hasNext ?: false
            
            // 2 parallel calls for bids
            Single.zip(
                bidsRepository.bidsForLoads(_res.transactions),
                bidsRepository.bulkLowestBidsForLoads(_res.transactions),
                BiFunction { t1, t2 -> Pair(t1, t2) }
            ).map { result ->
                Triple(_res.transactions, result.first.second, result.second.second)
            }
        }
        .onBackground()
        .subscribe { _tRes, error ->
            if (!error && _tRes != null) {
                // Build UI items
                // Fetch cross-tab counts (separate nested call)
            } else {
                error?.handle()
            }
            dataLoadingLiveData.postValue(false)
        }
}
```

**Migrated Implementation:**
```kotlin
private suspend fun fetchMarketplaceLoadsData(
    paginate: Boolean,
    onlyCount: Boolean,
    limit: Int
) {
    // Step 1: Fetch count only
    val countResult = transactionsRepository.fetchSpotMarketplaceTransactions(
        onlyCount = true, limit = limit, offset = 0
    )
    
    when (countResult) {
        is Resource.Success -> {
            val count = countResult.data?.totalCount ?: 0
            total = count
            cachedMarketplaceCount = total
            loadsCountLiveData.postValue(count)
        }
        is Resource.Failure -> {
            countResult.apiError?.handle()
            return
        }
        else -> { /* continue */ }
    }
    
    // Step 2: Fetch actual data
    val dataResult = transactionsRepository.fetchSpotMarketplaceTransactions(
        onlyCount = false, limit = limit, offset = offset
    )
    
    when (dataResult) {
        is Resource.Success -> {
            val _res = dataResult.data!!
            val transactions = _res.transactions
            
            // Update pagination state
            offset = _res.offset ?: 0
            if (total == 0) {
                hasMoreData = false
            }
            hasMoreData = _res.hasNext ?: false
            
            // Step 3: 2 parallel calls for bids
            data class BidsResults(
                val bids: Resource<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>>,
                val lowestBids: Resource<Pair<List<HomeBidsRequestItemData>, BulkLowestBidsResponse>>
            )
            
            val bidsResults = coroutineScope {
                val bidsDeferred = async {
                    bidsRepository.bidsForLoads(transactions)
                }
                val lowestBidsDeferred = async {
                    bidsRepository.bulkLowestBidsForLoads(transactions)
                }
                
                BidsResults(
                    bidsDeferred.await(),
                    lowestBidsDeferred.await()
                )
            }
            
            // Extract bids data
            val loads = transactions ?: emptyList()
            val bids = when (bidsResults.bids) {
                is Resource.Success -> bidsResults.bids.data?.second ?: emptyList()
                else -> emptyList()
            }
            val lowestBids = when (bidsResults.lowestBids) {
                is Resource.Success -> bidsResults.lowestBids.data?.second ?: emptyList()
                else -> emptyList()
            }
            
            // Build UI items
            mutableListOf<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
                add(Pair(HomeLoadsProgressItem(), Remove))
                
                add(Pair(HomeLoadsSearchItem(HomeLoadsSearchItemData(vehicleTypes)), AddUpdate))
                
                // Fetch cross-tab counts (nested parallel call)
                viewModelScope.launch {
                    data class CrossTabCounts(
                        val intracity: Resource<TransactionsResponse>,
                        val intercity: Resource<TransactionsResponse>
                    )
                    
                    val crossTabResults = coroutineScope {
                        val intracityDeferred = async {
                            transactionsRepository.fetchIntracityRecommTransactions(
                                offset, userPrefs.demandType, vehicleTypes, null,
                                filterVehicleType, true, onlyCount = true
                            )
                        }
                        val intercityDeferred = async {
                            transactionsRepository.fetchRecommTransactions(
                                offset,
                                filterDemandTypeForRecommendations(userPrefs.demandType),
                                vehicleTypes, null, filterVehicleType, true,
                                splitViewCount = true, searchAfter = null
                            )
                        }
                        
                        CrossTabCounts(
                            intracityDeferred.await(),
                            intercityDeferred.await()
                        )
                    }
                    
                    // Extract counts
                    val intracityCount = when (crossTabResults.intracity) {
                        is Resource.Success -> crossTabResults.intracity.data?.total ?: 0
                        else -> 0
                    }
                    
                    var intercityCount = 0
                    var nonDlvCount = 0
                    when (val intercityResult = crossTabResults.intercity) {
                        is Resource.Success -> {
                            intercityResult.data?.loadCounts?.all?.forEach { item ->
                                when (item.key) {
                                    "INTERCITY" -> intercityCount = item.count ?: 0
                                    "NON_DELHIVERY" -> nonDlvCount = item.count ?: 0
                                }
                            }
                        }
                        else -> { /* counts remain 0 */ }
                    }
                    
                    val totalAllCounts = intracityCount + intercityCount + nonDlvCount + cachedMarketplaceCount
                    fullLoadsCountLiveData.postValue(totalAllCounts)
                    
                    // Update filter with all counts
                    val filterItem = HomeLoadsFilterItem(
                        HomeLoadsFilterItemData(
                            "Marketplace", intracityCount, intercityCount,
                            nonDlvCount, cachedMarketplaceCount, userPrefs.demandType
                        )
                    )
                    userLoadsData.postValue(listOf(Pair(filterItem, AddUpdate)))
                }
                
                // Add filter item with marketplace count (initial with 0 for other counts)
                add(Pair(
                    HomeLoadsFilterItem(HomeLoadsFilterItemData(
                        "Marketplace", 0, 0, 0, cachedMarketplaceCount, userPrefs.demandType
                    )), AddUpdate
                ))
                
                if (!paginate) {
                    if (userPrefs.verificationStatus.equals("failed")) {
                        add(Pair(HomeLoadsKycPendingItem(), AddUpdate))
                    } else {
                        add(Pair(HomeMarketPlaceInfoItem(), AddUpdate))
                    }
                }
                
                if (total == 0 && !paginate) {
                    add(Pair(HomeLoadsWarningItem_NoLoads, AddUpdate))
                }
                
                for ((index, load) in loads.toMutableList().withIndex()) {
                    load.transactionId?.let { txnIds.add(it) }
                    
                    // Find and set lowest bid
                    val lowestBid = lowestBids.firstOrNull { b ->
                        b.transactionId.safeEquals(load.transactionId)
                    }
                    lowestBid?.let {
                        load.lowestBid = it.minBid
                        load.numBids = it.numBids
                    }
                    load.loadPricePercent = loadPricePercent
                    
                    // Find and set transaction bid
                    val transactionBid = bids.firstOrNull { b ->
                        b.transactionId.safeEquals(load.transactionId)
                    }
                    load.transactionBid = transactionBid
                    
                    populatePaymentFields(load)
                    
                    if (index.rem(HomeLoadsAddTruckItemDataConfig) == 0 && index != 0) {
                        add(Pair(HomeLoadsAddTruckItem(), Add))
                    }
                    add(Pair(HomeLoadsMarketplaceItem(load), Add))
                }
                
                if (!hasMoreData && !hasOrionLoadOnce) {
                    add(Pair(HomeLoadsMoreInfoItem(), AddUpdate))
                }
            }.let {
                userLoadsData.postValue(it)
                if (transactions?.isNotEmpty() == true && paginateCount == 0) {
                    marketPlaceListShownTracked.postValue(true)
                }
            }
        }
        
        is Resource.Failure -> {
            dataResult.apiError?.handle()
        }
        
        is Resource.Loading -> { /* not expected */ }
    }
}
```

**Key Points:**
- **Sequential count fetch**: First call gets count only, second gets data
- **2 parallel bids calls**: bids + lowestBids
- **Nested cross-tab counts fetch**: Separate `viewModelScope.launch` for intracity + intercity counts
- Uses `HomeLoadsMarketplaceItem` instead of `HomeLoadsRequestItem`

---
## 3. Critical Behavior Preservation Checklist

### 3.1 Pagination State Management

**State Fields:**
```kotlin
var searchAfter: SearchAfter? = null
var hasMoreData = true
var offset = 0
var total = 0
var loadsCount: Int = 0
```

**Preservation Rules:**
- ✅ Update `searchAfter` from `TransactionsResponse.searchAfter`
- ✅ Set `hasMoreData = (searchAfter != null && transactions.isNotEmpty())`
- ✅ Reset `searchAfter = null` and `hasMoreData = false` when `total == 0`
- ✅ Return early from `fetchUserTransactions(paginate=true)` when `hasMoreData == false`
- ✅ Post `HomeLoadsProgressItem` with `AddUpdate` operation before paginated API call
- ✅ Reset pagination state when `!paginate || infoSearch`

**Code Pattern:**
```kotlin
if (!paginate || infoSearch) {
    searchAfter = null
    offset = 0
    hasMoreData = true
}

if (paginate && !hasMoreData) {
    return
}

if (paginate) {
    paginateCount += 1
    Pair(HomeLoadsProgressItem(), AddUpdate).let { userLoadsData.postValue(listOf(it)) }
}
```

---

### 3.2 Tab Count Aggregation

**Count Sources:**
- Intracity count: From `fetchIntracityRecommTransactions().total`
- Intercity count: From `loadCounts.all` where `key == "INTERCITY"`
- Non-Delhivery count: From `loadCounts.all` where `key == "NON_DELHIVERY"`
- Marketplace count: From `fetchSpotMarketplaceTransactions(onlyCount=true).totalCount`

**Aggregation Formula:**
```kotlin
val fullCount = intracityCount + intercityCount + nonDlvCount + marketplaceCount
fullLoadsCountLiveData.postValue(fullCount)
```

**Null Handling:**
```kotlin
// When loadCounts is null, counts default to 0
var intercityCount = 0
var nonDlvCount = 0

splitResult.data?.loadCounts?.all?.forEach { item ->
    when (item.key) {
        "INTERCITY" -> intercityCount = item.count ?: 0
        "NON_DELHIVERY" -> nonDlvCount = item.count ?: 0
    }
}
```

**Per-Filter Count:**
```kotlin
val count = if (selectedFilter == DemandType.Internal.type) {
    intercityCount
} else {
    nonDlvCount
}
loadsCountLiveData.postValue(count)
```

---

### 3.3 Loading State Lifecycle

**Pattern:**
```kotlin
dataLoadingLiveData.postValue(true)  // Before any API call

viewModelScope.launch {
    try {
        // All fetch logic
    } finally {
        dataLoadingLiveData.postValue(false)  // Always reset
    }
}
```

**Critical Rules:**
- ✅ Post `true` BEFORE launching coroutine
- ✅ Post `false` in `finally` block (executes even on cancellation)
- ✅ Never post `false` inside `when` branches (use `finally` instead)

---

### 3.4 Error Fallback Logic

**Non-Intracity Branch:**
```kotlin
when (primaryResult) {
    is Resource.Failure -> {
        // CRITICAL: Fallback to supplier loads
        fetchSupplierTransactions(
            totalFetchTitle > 0,
            selectedFilter,
            demandType,
            infoSearch,
            excludeTruckTypes
        )
    }
}
```

**Intracity Branch:**
```kotlin
when (primaryResult) {
    is Resource.Failure -> {
        // NO FALLBACK: Post error directly
        primaryResult.apiError?.handle()
    }
}
```

**Marketplace Branch:**
```kotlin
when (dataResult) {
    is Resource.Failure -> {
        dataResult.apiError?.handle()
        // No fallback
    }
}
```

---

### 3.5 LiveData Contracts (Unchanged)

**LiveData Fields:**
```kotlin
var userLoadsData = MutableLiveData<List<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>()
var userLoadsDataFetch = MutableLiveData<List<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>()
var dataLoadingLiveData = MutableLiveData<Boolean>()
var loadsCountLiveData = MutableLiveData<Int>()
var fullLoadsCountLiveData = MutableLiveData<Int>()
```

**Preservation Rules:**
- ✅ Same types, same names
- ✅ `userLoadsData` for main fetch (fetchLoadsData)
- ✅ `userLoadsDataFetch` for supplier fallback (fetchSupplierLoadsData)
- ✅ `dataLoadingLiveData` for loading spinner
- ✅ `loadsCountLiveData` for per-filter count
- ✅ `fullLoadsCountLiveData` for total across all tabs

---

### 3.6 User Data Population

**Pattern:**
```kotlin
private fun populatePaymentFields(load: HomeBidsRequestItemData) {
    user?.supplierDetails?.let { supplier ->
        load.paymentMode = supplier.paymentMode
        load.advancePercentage = supplier.advancePercentage
    }
}

// Call for every load
for ((index, load) in loads.toMutableList().withIndex()) {
    populatePaymentFields(load)
    add(Pair(HomeLoadsRequestItem(load), Add))
}
```

**Preservation Rules:**
- ✅ Call `populatePaymentFields(load)` for EVERY load before posting to LiveData
- ✅ Applies to all branches: intracity, non-intracity, supplier, marketplace

---

### 3.7 Rapid Tab Switching Cancellation

**Pattern:**
```kotlin
private var currentFetchJob: Job? = null

fun fetchUserTransactions(...) {
    currentFetchJob?.cancel()
    currentFetchJob = viewModelScope.launch {
        // Fetch logic
    }
}

fun fetchSpotMarketplaceLoads(...) {
    currentFetchJob?.cancel()
    currentFetchJob = viewModelScope.launch {
        // Fetch logic
    }
}
```

**Preservation Rules:**
- ✅ Cancel previous job before launching new fetch
- ✅ Store new job reference
- ✅ Prevents stale data from completing after user switches tabs

---

### 3.8 Transaction ID Deduplication

**Pattern:**
```kotlin
var txnIds: ArrayList<String> = ArrayList()

// Reset on non-paginated fetch
if (!paginate) {
    txnIds.clear()
}

// Accumulate transaction IDs
for ((index, load) in loads.toMutableList().withIndex()) {
    load.transactionId?.let { txnIds.add(it) }
    // ...
}
```

**Preservation Rules:**
- ✅ Clear `txnIds` when `!paginate`
- ✅ Add every transaction ID to the list
- ✅ Used for deduplication in supplier loads fetch

---
## 4. Service & Repository Layer Changes

### 4.1 Service Layer Migration

#### RecommendationService.kt (In-Place Replacement)

**Before:**
```kotlin
interface RecommendationService {
    @POST("/get_sp_loads")
    fun recommendationTransactions(
        @Body request: ReccomdationRequest
    ): Single<BaseResponse<TransactionsResponse>>

    @POST("/get_sp_intracity_loads")
    fun recommendationIntracityTransactions(
        @Body request: ReccomdationRequest
    ): Single<BaseResponse<TransactionsResponse>>
}
```

**After:**
```kotlin
interface RecommendationService {
    @POST("/get_sp_loads")
    suspend fun recommendationTransactions(
        @Body request: ReccomdationRequest
    ): BaseResponse<TransactionsResponse>

    @POST("/get_sp_intracity_loads")
    suspend fun recommendationIntracityTransactions(
        @Body request: ReccomdationRequest
    ): BaseResponse<TransactionsResponse>
}
```

**Rationale:** Only `HomeLoadsViewModel` uses these methods, so in-place replacement is safe.

---

#### BidService.kt (Add Suspend Variants)

**Add New Methods:**
```kotlin
@GET("bids")
suspend fun bidsForLoadsSuspend(
    @Query("supplier_id") userId: String,
    @Query("transaction_ids") transactionIds: String? = null,
    @Query("contract_bids") contractBids: Boolean? = null
): BaseResponse<TransactionBidsResponseBody>

@GET("/bids/lowest")
suspend fun bulkLowestBidsForTransactionsSuspend(
    @Query("transaction_id_list") transactionIds: String
): BaseResponse<List<LowestBidResponse>>
```

**Keep Existing RxJava Methods:**
```kotlin
// Keep for other callers
@GET("bids")
fun bidsForLoads(...): Single<BaseResponse<TransactionBidsResponseBody>>

@GET("/bids/lowest")
fun bulkLowestBidsForTransactions(...): Single<BaseResponse<List<LowestBidResponse>>>
```

**Rationale:** Other ViewModels still use RxJava versions, so add suspend variants alongside.

---

#### TransactionService.kt (Add Suspend Variant)

**Add New Method:**
```kotlin
@GET("/v2/spot-marketplace/loads/")
suspend fun spotMarketplaceTransactionsSuspend(
    @Query("only_count") onlyCount: Boolean = false,
    @Query("limit") limit: Int,
    @Query("offset") offset: Int
): BaseResponse<SpotMarketplaceLoadsData>
```

**Keep Existing RxJava Method:**
```kotlin
// Keep for other callers
@GET("/v2/spot-marketplace/loads/")
fun spotMarketplaceTransactions(...): Single<BaseResponse<SpotMarketplaceLoadsData>>
```

---

### 4.2 Repository Layer Migration

#### TransactionsRepository.kt

**Pattern for All Methods:**
```kotlin
suspend fun fetchRecommTransactions(
    offset: Int, demand_type: String, vehicle_type: String? = null,
    excludeTruckTypes: String? = null, filterVehicleType: Boolean? = null,
    biddingGoingOn: Boolean = false, splitViewCount: Boolean? = null,
    searchAfter: SearchAfter?
): Resource<TransactionsResponse> = safeApiCall {
    val response = recommendationService.recommendationTransactions(
        ReccomdationRequest(
            userPrefs.parentId, UserTripsLoadLimit, offset,
            demand_type, vehicle_type, splitViewCount = splitViewCount,
            searchAfter = searchAfter
        )
    )
    if (response.isSuccess) {
        response.responseData ?: throw Exception("Null response data")
    } else {
        throw response.toHttpException()
    }
}
```

**Key Points:**
- Use `safeApiCall` from `BaseRepository`
- Check `response.isSuccess` inside lambda
- Throw `response.toHttpException()` on failure (maps to `Resource.Failure`)
- Throw `Exception("Null response data")` if data is null (maps to `ApiError.Unknown`)
- Return unwrapped data on success

**Methods to Migrate:**
1. `fetchRecommTransactions()` → `suspend fun`
2. `fetchIntracityRecommTransactions()` → `suspend fun`
3. `fetchSpotMarketplaceTransactions()` → `suspend fun`

---

#### BidsRepository.kt

**Pattern:**
```kotlin
suspend fun bidsForLoads(
    transactions: List<HomeBidsRequestItemData>?,
    contractBids: Boolean? = null
): Resource<Pair<List<HomeBidsRequestItemData>, List<TransactionBid>>> = safeApiCall {
    val response = bidService.bidsForLoadsSuspend(
        userRepository.userId(),
        if (transactions.isNullOrEmpty()) "" else
            transactions.map { it.transactionId }.joinToString(","),
        contractBids
    )
    if (response.isSuccess) {
        Pair(transactions ?: emptyList(), response.responseData!!.bids)
    } else {
        throw response.toHttpException()
    }
}

suspend fun bulkLowestBidsForLoads(
    transactions: List<HomeBidsRequestItemData>?
): Resource<Pair<List<HomeBidsRequestItemData>, BulkLowestBidsResponse>> = safeApiCall {
    val response = bidService.bulkLowestBidsForTransactionsSuspend(
        if (transactions.isNullOrEmpty()) "" else
            transactions.map { it.transactionId }.joinToString(",")
    )
    if (response.isSuccess) {
        Pair(transactions ?: emptyList(), response.responseData!!)
    } else {
        throw response.toHttpException()
    }
}
```

**Rename Existing RxJava Methods:**
```kotlin
// Rename to avoid overload conflicts
fun bidsForLoadsRx(...): Single<...>
fun bulkLowestBidsForLoadsRx(...): Single<...>
```

**Update Callers:**
- Find all callers of `bidsForLoads()` and `bulkLowestBidsForLoads()` outside `HomeLoadsViewModel`
- Update them to call `bidsForLoadsRx()` and `bulkLowestBidsForLoadsRx()`

---

### 4.3 Error Mapping (Existing - No Changes)

**BaseResponse.toHttpException():**
```kotlin
fun BaseResponse<*>.toHttpException(): HttpException {
    val errorCode = this.errorBody?.errorCode() ?: 400
    return HttpException(Response.error<Any>(errorCode, ResponseBody.create(null, "")))
}
```

**safeApiCall Mapping:**
| Exception | ApiError |
|-----------|----------|
| `SocketTimeoutException` | `Timeout` |
| `IOException` | `Network` |
| `HttpException(401)` | `Unauthorized` |
| `HttpException(403)` | `AccessDenied` |
| `HttpException(404)` | `NotFound` |
| `HttpException(503)` | `ServiceUnavailable` |
| `HttpException(other)` | `Unknown` |
| `Exception` (other) | `Unknown` |
| `CancellationException` | Rethrown (not caught) |

---
## 5. Testing Strategy

### 5.1 Test Dependencies

**Add to `app/build.gradle`:**
```groovy
dependencies {
    // Existing dependencies...
    
    // Coroutines testing
    testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3'
    
    // LiveData testing
    testImplementation 'androidx.arch.core:core-testing:2.2.0'
    
    // Mocking
    testImplementation 'io.mockk:mockk:1.13.8'
    
    // Property-based testing
    testImplementation 'io.kotest:kotest-runner-junit5:5.8.0'
    testImplementation 'io.kotest:kotest-property:5.8.0'
    testImplementation 'io.kotest:kotest-assertions-core:5.8.0'
}
```

---

### 5.2 Property-Based Tests

**Purpose:** Verify universal correctness properties across randomly generated inputs (minimum 100 iterations per property).

#### Property 1: Repository Success Unwrapping

**File:** `app/src/test/java/com/delhivery/axle/api/repository/TransactionsRepositoryTest.kt`

**Test:**
```kotlin
// Feature: get-sp-loads-coroutine-migration, Property 1: Repository success unwrapping
@Test
fun `property - repository returns Resource Success when BaseResponse isSuccess is true`() = runTest {
    checkAll(100, Arb.transactionsResponse()) { transactionsResponse ->
        // Given: BaseResponse with isSuccess=true
        val baseResponse = BaseResponse(
            isSuccess = true,
            responseData = transactionsResponse,
            errorBody = null
        )
        coEvery { recommendationService.recommendationTransactions(any()) } returns baseResponse
        
        // When: Calling repository method
        val result = transactionsRepository.fetchRecommTransactions(...)
        
        // Then: Result is Resource.Success with unwrapped data
        result shouldBe instanceOf<Resource.Success<TransactionsResponse>>()
        (result as Resource.Success).data shouldBe transactionsResponse
    }
}
```

**Arb Generator:**
```kotlin
fun Arb.Companion.transactionsResponse(): Arb<TransactionsResponse> = arbitrary {
    TransactionsResponse(
        transactions = Arb.list(Arb.homeBidsRequestItemData(), 0..20).bind(),
        total = Arb.int(0..100).bind(),
        offset = Arb.int(0..100).bind(),
        hasNext = Arb.boolean().bind(),
        searchAfter = Arb.searchAfter().orNull().bind(),
        loadCounts = Arb.loadCounts().orNull().bind(),
        // ... other fields
    )
}
```

---

#### Property 2: Repository Failure Mapping

**Test:**
```kotlin
// Feature: get-sp-loads-coroutine-migration, Property 2: Repository failure mapping via toHttpException
@Test
fun `property - repository returns Resource Failure with correct ApiError when BaseResponse isSuccess is false`() = runTest {
    checkAll(100, Arb.errorCode()) { errorCode ->
        // Given: BaseResponse with isSuccess=false
        val baseResponse = BaseResponse<TransactionsResponse>(
            isSuccess = false,
            responseData = null,
            errorBody = BaseErrorResponse(errorCode = errorCode)
        )
        coEvery { recommendationService.recommendationTransactions(any()) } returns baseResponse
        
        // When: Calling repository method
        val result = transactionsRepository.fetchRecommTransactions(...)
        
        // Then: Result is Resource.Failure with correct ApiError
        result shouldBe instanceOf<Resource.Failure>()
        val expectedApiError = when (errorCode) {
            401 -> ApiError.Unauthorized
            403 -> ApiError.AccessDenied
            404 -> ApiError.NotFound
            503 -> ApiError.ServiceUnavailable
            else -> ApiError.Unknown
        }
        (result as Resource.Failure).apiError shouldBe expectedApiError
    }
}
```

---

#### Property 3: Loading State Round Trip

**File:** `app/src/test/java/com/delhivery/axle/ui/home/fragments/loads/HomeLoadsViewModelTest.kt`

**Test:**
```kotlin
// Feature: get-sp-loads-coroutine-migration, Property 3: Loading state round trip
@Test
fun `property - dataLoadingLiveData transitions true to false regardless of success or failure`() = runTest {
    checkAll(100, Arb.fetchParameters(), Arb.boolean()) { params, shouldSucceed ->
        // Given: Mock repository response
        if (shouldSucceed) {
            coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), any()) } 
                returns Resource.Success(Arb.transactionsResponse().bind())
        } else {
            coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), any()) } 
                returns Resource.Failure(false, null, ApiError.Unknown)
        }
        
        // When: Calling fetchUserTransactions
        val loadingStates = mutableListOf<Boolean>()
        viewModel.dataLoadingLiveData.observeForever { loadingStates.add(it) }
        
        viewModel.fetchUserTransactions(params.paginate, params.demandType, params.selectedFilter, params.infoSearch, params.excludeTruckTypes)
        advanceUntilIdle()
        
        // Then: Loading state transitions true → false
        loadingStates shouldContain true
        loadingStates.last() shouldBe false
    }
}
```

---

#### Property 4: Pagination State Consistency

**Test:**
```kotlin
// Feature: get-sp-loads-coroutine-migration, Property 4: Pagination state consistency
@Test
fun `property - hasMoreData is true iff searchAfter is non-null and transactions is non-empty`() = runTest {
    checkAll(100, Arb.transactionsResponse()) { transactionsResponse ->
        // Given: Mock repository response
        coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), any()) } 
            returns Resource.Success(transactionsResponse)
        
        // When: Calling fetchUserTransactions
        viewModel.fetchUserTransactions(false, "Internal", "", false, null)
        advanceUntilIdle()
        
        // Then: hasMoreData matches expected value
        val expectedHasMoreData = transactionsResponse.searchAfter != null && 
                                  !transactionsResponse.transactions.isNullOrEmpty()
        viewModel.hasMoreData shouldBe expectedHasMoreData
        viewModel.searchAfter shouldBe transactionsResponse.searchAfter
    }
}
```

---

#### Property 5: Tab Count Aggregation

**Test:**
```kotlin
// Feature: get-sp-loads-coroutine-migration, Property 5: Tab count aggregation
@Test
fun `property - fullLoadsCountLiveData equals sum of all tab counts with null loadCounts defaulting to 0`() = runTest {
    checkAll(100, Arb.loadCounts().orNull(), Arb.int(0..100), Arb.int(0..100)) { loadCounts, intracityTotal, marketplaceCount ->
        // Given: Mock responses with varying loadCounts
        val intracityResponse = TransactionsResponse(total = intracityTotal, ...)
        val splitResponse = TransactionsResponse(loadCounts = loadCounts, ...)
        val marketplaceResponse = SpotMarketplaceLoadsData(totalCount = marketplaceCount, ...)
        
        coEvery { transactionsRepository.fetchIntracityRecommTransactions(any(), any(), any(), any(), any(), any(), any(), any()) } 
            returns Resource.Success(intracityResponse)
        coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), splitViewCount = true, any()) } 
            returns Resource.Success(splitResponse)
        coEvery { transactionsRepository.fetchSpotMarketplaceTransactions(any(), any(), any()) } 
            returns Resource.Success(marketplaceResponse)
        
        // When: Calling fetchUserTransactions
        val fullCounts = mutableListOf<Int>()
        viewModel.fullLoadsCountLiveData.observeForever { fullCounts.add(it) }
        
        viewModel.fetchUserTransactions(false, "Intracity", "Intracity", false, null)
        advanceUntilIdle()
        
        // Then: fullLoadsCountLiveData equals sum
        val intercityCount = loadCounts?.all?.firstOrNull { it.key == "INTERCITY" }?.count ?: 0
        val nonDlvCount = loadCounts?.all?.firstOrNull { it.key == "NON_DELHIVERY" }?.count ?: 0
        val expectedTotal = intracityTotal + intercityCount + nonDlvCount + marketplaceCount
        
        fullCounts.last() shouldBe expectedTotal
    }
}
```

---

#### Property 6: Parallel Call Failure Cancels Siblings

**Test:**
```kotlin
// Feature: get-sp-loads-coroutine-migration, Property 6: Parallel call failure cancels siblings
@Test
fun `property - parallel call failure cancels siblings and no partial results posted`() = runTest {
    checkAll(100, Arb.int(0..4)) { failingCallIndex ->
        // Given: One parallel call fails, others succeed
        val successResponse = Resource.Success(mockData)
        val failureResponse = Resource.Failure(false, null, ApiError.Unknown)
        
        val responses = List(5) { index ->
            if (index == failingCallIndex) failureResponse else successResponse
        }
        
        coEvery { bidsRepository.bidsForLoads(any()) } returns responses[0]
        coEvery { bidsRepository.bulkLowestBidsForLoads(any()) } returns responses[1]
        // ... setup other mocks
        
        // When: Calling fetchUserTransactions
        val postedData = mutableListOf<List<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>()
        viewModel.userLoadsData.observeForever { postedData.add(it) }
        
        viewModel.fetchUserTransactions(false, "Internal", "", false, null)
        advanceUntilIdle()
        
        // Then: No partial results posted (only progress item removal or error state)
        // Verify no loads were posted
        postedData.forEach { items ->
            items.none { it.first is HomeLoadsRequestItem } shouldBe true
        }
    }
}
```

---

#### Property 7: Parallel Call Success Produces Combined Result

**Test:**
```kotlin
// Feature: get-sp-loads-coroutine-migration, Property 7: Parallel call success produces combined result
@Test
fun `property - successful parallel calls produce combined result matching Single zip structure`() = runTest {
    checkAll(100, Arb.parallelCallResults()) { results ->
        // Given: All parallel calls succeed
        coEvery { bidsRepository.bidsForLoads(any()) } returns Resource.Success(results.bids)
        coEvery { bidsRepository.bulkLowestBidsForLoads(any()) } returns Resource.Success(results.lowestBids)
        coEvery { transactionsRepository.fetchIntracityRecommTransactions(any(), any(), any(), any(), any(), any()) } 
            returns Resource.Success(results.intracity)
        coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), splitViewCount = true, any()) } 
            returns Resource.Success(results.splitCount)
        coEvery { transactionsRepository.fetchSpotMarketplaceTransactions(any(), any(), any()) } 
            returns Resource.Success(results.marketplace)
        
        // When: Calling fetchUserTransactions
        val postedData = mutableListOf<List<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>()
        viewModel.userLoadsData.observeForever { postedData.add(it) }
        
        viewModel.fetchUserTransactions(false, "Internal", "", false, null)
        advanceUntilIdle()
        
        // Then: Combined result contains all expected data
        val finalItems = postedData.last()
        // Verify loads, bids, counts are all present
        finalItems.any { it.first is HomeLoadsRequestItem } shouldBe true
        finalItems.any { it.first is HomeLoadsFilterItem } shouldBe true
    }
}
```

---

#### Property 8: CancellationException Propagation

**Test:**
```kotlin
// Feature: get-sp-loads-coroutine-migration, Property 8: CancellationException propagation
@Test
fun `property - CancellationException is rethrown not mapped to Resource Failure`() = runTest {
    // Given: Repository method that gets cancelled
    coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), any()) } coAnswers {
        delay(1000)
        Resource.Success(mockData)
    }
    
    // When: Starting fetch and cancelling immediately
    val job = launch {
        viewModel.fetchUserTransactions(false, "Internal", "", false, null)
    }
    delay(100)
    job.cancel()
    
    // Then: No Resource.Failure posted, coroutine is cancelled cleanly
    // Verify no error handling was triggered
    verify(exactly = 0) { mockApiError.handle() }
}
```

---
### 5.3 Unit Tests (Specific Examples and Edge Cases)

**Purpose:** Verify specific branch logic, edge cases, and integration points with concrete inputs.

#### Test File: `HomeLoadsViewModelTest.kt`

**Test 1: Intracity Branch Success**
```kotlin
@Test
fun `intracity branch success posts correct items to userLoadsData`() = runTest {
    // Given: Mock intracity response
    val intracityResponse = TransactionsResponse(
        transactions = listOf(mockLoad1, mockLoad2),
        total = 2,
        offset = 0,
        searchAfter = null
    )
    coEvery { transactionsRepository.fetchIntracityRecommTransactions(any(), any(), any(), any(), any(), any(), any(), any()) } 
        returns Resource.Success(intracityResponse)
    
    // When: Calling fetchUserTransactions with Intracity filter
    viewModel.fetchUserTransactions(false, "Intracity", "Intracity", false, null)
    advanceUntilIdle()
    
    // Then: userLoadsData contains expected items
    val postedItems = viewModel.userLoadsData.value!!
    postedItems.any { it.first is HomeLoadsSearchItem } shouldBe true
    postedItems.any { it.first is HomeLoadsFilterItem } shouldBe true
    postedItems.count { it.first is HomeLoadsRequestItem } shouldBe 2
}
```

---

**Test 2: Non-Intracity Branch Success**
```kotlin
@Test
fun `non-intracity branch success with 5 parallel calls posts combined data`() = runTest {
    // Given: Mock all 5 parallel responses
    val primaryResponse = TransactionsResponse(transactions = listOf(mockLoad1), ...)
    val bidsResponse = Pair(listOf(mockLoad1), listOf(mockBid1))
    val lowestBidsResponse = Pair(listOf(mockLoad1), listOf(mockLowestBid1))
    val intracityResponse = TransactionsResponse(total = 5, ...)
    val splitCountResponse = TransactionsResponse(loadCounts = mockLoadCounts, ...)
    val marketplaceResponse = SpotMarketplaceLoadsData(totalCount = 3, ...)
    
    coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), searchAfter = any()) } 
        returns Resource.Success(primaryResponse)
    coEvery { bidsRepository.bidsForLoads(any()) } returns Resource.Success(bidsResponse)
    coEvery { bidsRepository.bulkLowestBidsForLoads(any()) } returns Resource.Success(lowestBidsResponse)
    coEvery { transactionsRepository.fetchIntracityRecommTransactions(any(), any(), any(), any(), any(), any()) } 
        returns Resource.Success(intracityResponse)
    coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), splitViewCount = true, any()) } 
        returns Resource.Success(splitCountResponse)
    coEvery { transactionsRepository.fetchSpotMarketplaceTransactions(any(), any(), any()) } 
        returns Resource.Success(marketplaceResponse)
    
    // When: Calling fetchUserTransactions
    viewModel.fetchUserTransactions(false, "Internal", "", false, null)
    advanceUntilIdle()
    
    // Then: Combined data posted
    val postedItems = viewModel.userLoadsData.value!!
    postedItems.count { it.first is HomeLoadsRequestItem } shouldBe 1
    viewModel.fullLoadsCountLiveData.value shouldBe (primaryResponse.total + 5 + 0 + 0 + 3)
}
```

---

**Test 3: Non-Intracity Fallback on Error**
```kotlin
@Test
fun `non-intracity branch falls back to fetchSupplierTransactions on primary call failure`() = runTest {
    // Given: Primary call fails
    coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), searchAfter = any()) } 
        returns Resource.Failure(false, null, ApiError.Unknown)
    
    // Mock supplier loads response
    val supplierResponse = TransactionsResponse(transactions = listOf(mockLoad1), ...)
    coEvery { transactionsRepository.fetchLoadBoardTransactions(any(), any(), any(), any(), any(), any(), any()) } 
        returns Resource.Success(supplierResponse)
    
    // When: Calling fetchUserTransactions
    viewModel.fetchUserTransactions(false, "Internal", "", false, null)
    advanceUntilIdle()
    
    // Then: fetchSupplierTransactions was called
    coVerify { transactionsRepository.fetchLoadBoardTransactions(any(), any(), any(), any(), any(), any(), any()) }
    
    // And: userLoadsDataFetch contains supplier loads
    val postedItems = viewModel.userLoadsDataFetch.value!!
    postedItems.count { it.first is HomeLoadsRequestItem } shouldBe 1
}
```

---

**Test 4: Intracity No Fallback on Error**
```kotlin
@Test
fun `intracity branch does not fall back to supplier loads on error`() = runTest {
    // Given: Intracity call fails
    coEvery { transactionsRepository.fetchIntracityRecommTransactions(any(), any(), any(), any(), any(), any(), any(), any()) } 
        returns Resource.Failure(false, null, ApiError.Unknown)
    
    // When: Calling fetchUserTransactions with Intracity filter
    viewModel.fetchUserTransactions(false, "Intracity", "Intracity", false, null)
    advanceUntilIdle()
    
    // Then: fetchSupplierTransactions was NOT called
    coVerify(exactly = 0) { transactionsRepository.fetchLoadBoardTransactions(any(), any(), any(), any(), any(), any(), any()) }
    
    // And: Error was handled
    verify { mockApiError.handle() }
}
```

---

**Test 5: Pagination Progress Item**
```kotlin
@Test
fun `paginate true posts HomeLoadsProgressItem before API call`() = runTest {
    // Given: Mock response
    coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), any()) } 
        returns Resource.Success(mockResponse)
    
    // When: Calling with paginate=true
    val postedItems = mutableListOf<List<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>()
    viewModel.userLoadsData.observeForever { postedItems.add(it) }
    
    viewModel.fetchUserTransactions(true, "Internal", "", false, null)
    advanceUntilIdle()
    
    // Then: First posted item is HomeLoadsProgressItem with AddUpdate
    postedItems.first().first().let { (item, operation) ->
        item shouldBe instanceOf<HomeLoadsProgressItem>()
        operation shouldBe AddUpdate
    }
}
```

---

**Test 6: Null loadCounts Edge Case**
```kotlin
@Test
fun `null loadCounts defaults INTERCITY and NON_DELHIVERY counts to 0`() = runTest {
    // Given: Response with null loadCounts
    val splitResponse = TransactionsResponse(
        total = 10,
        loadCounts = null,  // null loadCounts
        ...
    )
    coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), splitViewCount = true, any()) } 
        returns Resource.Success(splitResponse)
    
    // When: Calling fetchUserTransactions
    viewModel.fetchUserTransactions(false, "Intracity", "Intracity", false, null)
    advanceUntilIdle()
    
    // Then: fullLoadsCountLiveData uses 0 for INTERCITY and NON_DELHIVERY
    // (only intracity total + marketplace count)
    viewModel.fullLoadsCountLiveData.value shouldBe (intracityTotal + 0 + 0 + marketplaceCount)
}
```

---

**Test 7: Empty Transactions Edge Case**
```kotlin
@Test
fun `empty transactions sets hasMoreData to false and searchAfter to null`() = runTest {
    // Given: Response with empty transactions
    val emptyResponse = TransactionsResponse(
        transactions = emptyList(),
        total = 0,
        searchAfter = SearchAfter(...),  // Even if searchAfter is present
        ...
    )
    coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), any()) } 
        returns Resource.Success(emptyResponse)
    
    // When: Calling fetchUserTransactions
    viewModel.fetchUserTransactions(false, "Internal", "", false, null)
    advanceUntilIdle()
    
    // Then: Pagination state is reset
    viewModel.hasMoreData shouldBe false
    viewModel.searchAfter shouldBe null
}
```

---

**Test 8: Rapid Tab Switching Cancellation**
```kotlin
@Test
fun `rapid tab switching cancels previous fetch job`() = runTest {
    // Given: First fetch takes long time
    coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), any()) } coAnswers {
        delay(1000)
        Resource.Success(mockResponse1)
    }
    
    // When: Starting first fetch, then immediately starting second fetch
    viewModel.fetchUserTransactions(false, "Internal", "", false, null)
    delay(100)
    
    coEvery { transactionsRepository.fetchIntracityRecommTransactions(any(), any(), any(), any(), any(), any(), any(), any()) } 
        returns Resource.Success(mockResponse2)
    viewModel.fetchUserTransactions(false, "Intracity", "Intracity", false, null)
    advanceUntilIdle()
    
    // Then: Only second fetch completes, first is cancelled
    val postedItems = viewModel.userLoadsData.value!!
    // Verify only intracity items are present (not intercity)
    postedItems.any { (it.first as? HomeLoadsFilterItem)?.data?.selectedFilter == "Intracity" } shouldBe true
}
```

---

**Test 9: ViewModel onCleared Cancellation**
```kotlin
@Test
fun `onCleared cancels all coroutines and no LiveData updates after`() = runTest {
    // Given: Fetch in progress
    coEvery { transactionsRepository.fetchRecommTransactions(any(), any(), any(), any(), any(), any(), any()) } coAnswers {
        delay(1000)
        Resource.Success(mockResponse)
    }
    
    // When: Starting fetch and calling onCleared
    val postedItems = mutableListOf<List<Pair<BaseHomeLoadsRVAdapterItem<*>, DataRVAdapterOperationType>>>()
    viewModel.userLoadsData.observeForever { postedItems.add(it) }
    
    viewModel.fetchUserTransactions(false, "Internal", "", false, null)
    delay(100)
    
    val itemsBeforeCleared = postedItems.size
    viewModel.onCleared()
    advanceUntilIdle()
    
    // Then: No new items posted after onCleared
    postedItems.size shouldBe itemsBeforeCleared
}
```

---

**Test 10: Null responseData Edge Case**
```kotlin
@Test
fun `null responseData with isSuccess true returns Resource Failure with ApiError Unknown`() = runTest {
    // Given: BaseResponse with isSuccess=true but null data
    val baseResponse = BaseResponse<TransactionsResponse>(
        isSuccess = true,
        responseData = null,
        errorBody = null
    )
    coEvery { recommendationService.recommendationTransactions(any()) } returns baseResponse
    
    // When: Calling repository method
    val result = transactionsRepository.fetchRecommTransactions(...)
    
    // Then: Result is Resource.Failure with ApiError.Unknown
    result shouldBe instanceOf<Resource.Failure>()
    (result as Resource.Failure).apiError shouldBe ApiError.Unknown
}
```

---

### 5.4 Build Validation

**Manual Test:**
```bash
# Clean build
./gradlew clean

# Build development debug variant
./gradlew assembleDevelopmentDebug

# Expected: Build succeeds without errors
```

**Verification:**
- ✅ No compilation errors
- ✅ No unresolved references
- ✅ APK generated successfully

---
## 6. Migration Execution Checklist

### Phase 1: Setup and Dependencies
- [ ] Add test dependencies to `app/build.gradle` (Kotest, MockK, coroutines-test, core-testing)
- [ ] Verify build compiles: `./gradlew assembleDevelopmentDebug`
- [ ] Create test directory structure: `app/src/test/java/com/delhivery/axle/`

---

### Phase 2: Service Layer Migration
- [ ] **RecommendationService.kt**: Replace `Single<BaseResponse<T>>` with `suspend fun` returning `BaseResponse<T>`
  - [ ] `recommendationTransactions()` → `suspend fun`
  - [ ] `recommendationIntracityTransactions()` → `suspend fun`
- [ ] **BidService.kt**: Add suspend variants alongside existing RxJava methods
  - [ ] Add `bidsForLoadsSuspend()` → `suspend fun`
  - [ ] Add `bulkLowestBidsForTransactionsSuspend()` → `suspend fun`
  - [ ] Keep existing RxJava methods for other callers
- [ ] **TransactionService.kt**: Add suspend variant
  - [ ] Add `spotMarketplaceTransactionsSuspend()` → `suspend fun`
  - [ ] Keep existing RxJava method for other callers
- [ ] Verify build compiles after service layer changes

---

### Phase 3: Repository Layer Migration
- [ ] **TransactionsRepository.kt**: Replace methods with suspend equivalents using `safeApiCall`
  - [ ] `fetchRecommTransactions()` → `suspend fun` with `safeApiCall`
  - [ ] `fetchIntracityRecommTransactions()` → `suspend fun` with `safeApiCall`
  - [ ] `fetchSpotMarketplaceTransactions()` → `suspend fun` with `safeApiCall`
  - [ ] Check `BaseResponse.isSuccess` and throw `toHttpException()` on failure
  - [ ] Handle null `responseData` by throwing `Exception("Null response data")`
- [ ] **BidsRepository.kt**: Replace methods with suspend equivalents
  - [ ] `bidsForLoads()` → `suspend fun` with `safeApiCall`
  - [ ] `bulkLowestBidsForLoads()` → `suspend fun` with `safeApiCall`
  - [ ] Rename old RxJava methods to `bidsForLoadsRx()`, `bulkLowestBidsForLoadsRx()`
  - [ ] Update callers outside migration scope to use renamed RxJava methods
- [ ] Verify build compiles after repository layer changes

---

### Phase 4: Repository Layer Testing
- [ ] **TransactionsRepositoryTest.kt**: Create test file
  - [ ] Write Property 1: Repository success unwrapping (100+ iterations)
  - [ ] Write Property 2: Repository failure mapping (100+ iterations)
  - [ ] Write unit test: Null responseData edge case
  - [ ] Write unit test: IOException returns ApiError.Network
  - [ ] Write unit test: SocketTimeoutException returns ApiError.Timeout
- [ ] **BidsRepositoryTest.kt**: Create test file
  - [ ] Write Property 1 for bids methods
  - [ ] Write Property 2 for bids methods
  - [ ] Write unit tests for edge cases
- [ ] Run repository tests: `./gradlew testDevelopmentDebugUnitTest`
- [ ] Verify all repository tests pass

---

### Phase 5: ViewModel Layer Migration
- [ ] **HomeLoadsViewModel.kt**: Add `currentFetchJob` pattern
  - [ ] Add `private var currentFetchJob: Job? = null` field
  - [ ] Cancel previous job in `fetchUserTransactions()` before launching new fetch
  - [ ] Cancel previous job in `fetchSpotMarketplaceLoads()` before launching new fetch
- [ ] **fetchLoadsData() - Intracity Branch**: Migrate to coroutines
  - [ ] Replace `compositeDisposable +=` with `viewModelScope.launch`
  - [ ] Call `fetchIntracityRecommTransactions()` sequentially
  - [ ] Launch 2 parallel calls using `coroutineScope { async {} }`: splitCount + marketplace
  - [ ] Update pagination state in `Resource.Success` branch
  - [ ] Extract tab counts from `loadCounts.all`
  - [ ] Build UI items list and post to `userLoadsData`
  - [ ] Use `try/finally` for loading state management
  - [ ] NO fallback on error (post error directly)
- [ ] **fetchLoadsData() - Non-Intracity Branch**: Migrate to coroutines
  - [ ] Replace `compositeDisposable +=` with `viewModelScope.launch`
  - [ ] Call `fetchRecommTransactions()` sequentially for primary data
  - [ ] Launch 5 parallel calls using `coroutineScope { async {} }`: bids, lowestBids, intracity, splitCount, marketplace
  - [ ] Update pagination state in `Resource.Success` branch
  - [ ] Extract tab counts from `loadCounts.all`
  - [ ] Build UI items list and post to `userLoadsData`
  - [ ] CRITICAL: On `Resource.Failure`, call `fetchSupplierTransactions()` fallback
  - [ ] Use `try/finally` for loading state management
- [ ] **fetchSupplierLoadsData()**: Migrate to coroutines
  - [ ] Convert to `private suspend fun`
  - [ ] Call `fetchLoadBoardTransactions()` sequentially
  - [ ] Launch 3 parallel calls: bids, lowestBids, intracity count
  - [ ] Fetch marketplace count separately (non-blocking)
  - [ ] Filter loads to exclude those with existing bids
  - [ ] Post to `userLoadsDataFetch` (not `userLoadsData`)
- [ ] **fetchMarketplaceLoadsData()**: Migrate to coroutines
  - [ ] Convert to `private suspend fun`
  - [ ] Sequential count fetch (onlyCount=true)
  - [ ] Sequential data fetch (onlyCount=false)
  - [ ] Launch 2 parallel bids calls: bids + lowestBids
  - [ ] Nested cross-tab counts fetch in separate `viewModelScope.launch`
  - [ ] Use `HomeLoadsMarketplaceItem` for loads
- [ ] **Entry Points**: Wire to coroutine methods
  - [ ] Update `fetchUserTransactions()` to use `currentFetchJob` pattern
  - [ ] Update `fetchSpotMarketplaceLoads()` to use `currentFetchJob` pattern
  - [ ] Keep user-data-fetch-first pattern (check `user == null`)
  - [ ] Remove all `compositeDisposable` additions from migrated methods
- [ ] Verify build compiles after ViewModel migration

---

### Phase 6: ViewModel Layer Testing
- [ ] **HomeLoadsViewModelTest.kt**: Create test file
  - [ ] Setup: InstantTaskExecutorRule, TestDispatcher, MockK
  - [ ] Write Property 3: Loading state round trip (100+ iterations)
  - [ ] Write Property 4: Pagination state consistency (100+ iterations)
  - [ ] Write Property 5: Tab count aggregation (100+ iterations)
  - [ ] Write Property 6: Parallel call failure cancels siblings (100+ iterations)
  - [ ] Write Property 7: Parallel call success produces combined result (100+ iterations)
  - [ ] Write Property 8: CancellationException propagation (100+ iterations)
  - [ ] Write unit test: Intracity branch success
  - [ ] Write unit test: Non-intracity branch success
  - [ ] Write unit test: Non-intracity fallback on error
  - [ ] Write unit test: Intracity no fallback on error
  - [ ] Write unit test: Pagination progress item
  - [ ] Write unit test: Null loadCounts edge case
  - [ ] Write unit test: Empty transactions edge case
  - [ ] Write unit test: Rapid tab switching cancellation
  - [ ] Write unit test: ViewModel onCleared cancellation
- [ ] Run ViewModel tests: `./gradlew testDevelopmentDebugUnitTest`
- [ ] Verify all ViewModel tests pass

---

### Phase 7: Cleanup and Validation
- [ ] Remove dead RxJava code from migrated methods
  - [ ] Remove old `compositeDisposable +=` calls
  - [ ] Remove unused RxJava imports
  - [ ] Verify `convertResponse()` is NOT used in migrated flow (kept for other callers)
- [ ] Run full test suite: `./gradlew test`
- [ ] Verify all tests pass
- [ ] Build all variants: `./gradlew assembleDevelopmentDebug`
- [ ] Verify build succeeds without errors
- [ ] Manual smoke test: Launch app, navigate to Home Loads, verify loads display correctly
- [ ] Manual test: Switch between tabs (Intracity, Intercity, Marketplace) rapidly
- [ ] Manual test: Pagination (scroll to bottom, verify more loads fetch)
- [ ] Manual test: Error handling (disable network, verify error states)

---

### Phase 8: Code Review and Documentation
- [ ] Review all changes for correctness
- [ ] Verify all critical behaviors preserved (see Section 3)
- [ ] Update any relevant documentation
- [ ] Prepare commit message with migration summary
- [ ] Create PR with detailed description
- [ ] Request code review from team

---

### Phase 9: Deployment
- [ ] Merge PR after approval
- [ ] Monitor crash reports (Firebase Crashlytics)
- [ ] Monitor performance metrics (Firebase Performance)
- [ ] Verify no regressions in production
- [ ] Document any issues and resolutions

---

## 7. Rollback Plan

If critical issues are discovered post-deployment:

1. **Immediate Rollback**: Revert the migration commit
2. **Root Cause Analysis**: Investigate the issue using logs and crash reports
3. **Fix and Re-test**: Address the issue in a new branch
4. **Re-deploy**: Merge the fix and re-deploy

**Rollback Safety**: The migration is designed as a single atomic commit, making rollback straightforward.

---

## 8. Key Success Metrics

- ✅ All tests pass (property-based + unit tests)
- ✅ Build compiles without errors
- ✅ No regressions in load display functionality
- ✅ Pagination works correctly
- ✅ Tab switching is smooth (no stale data)
- ✅ Error handling works as expected (fallback for non-intracity)
- ✅ No crashes in production
- ✅ Performance metrics remain stable or improve

---

## 9. Common Pitfalls and Solutions

### Pitfall 1: Forgetting `finally` for Loading State
**Problem:** Loading spinner never disappears if error occurs.
**Solution:** Always use `try/finally` pattern:
```kotlin
try {
    // Fetch logic
} finally {
    dataLoadingLiveData.postValue(false)
}
```

---

### Pitfall 2: Not Cancelling Previous Fetch
**Problem:** Stale data appears when user switches tabs rapidly.
**Solution:** Use `currentFetchJob` pattern:
```kotlin
currentFetchJob?.cancel()
currentFetchJob = viewModelScope.launch { ... }
```

---

### Pitfall 3: Forgetting Fallback for Non-Intracity
**Problem:** Non-intracity branch shows error instead of supplier loads.
**Solution:** Always call `fetchSupplierTransactions()` in `Resource.Failure` branch for non-intracity.

---

### Pitfall 4: Null loadCounts Not Handled
**Problem:** Crash when `loadCounts` is null.
**Solution:** Use safe navigation and default to 0:
```kotlin
val intercityCount = loadCounts?.all?.firstOrNull { it.key == "INTERCITY" }?.count ?: 0
```

---

### Pitfall 5: Parallel Call Failure Not Handled
**Problem:** Partial results posted when one parallel call fails.
**Solution:** `coroutineScope { async {} }` automatically cancels siblings on failure. Verify no partial data is posted.

---

## 10. Additional Resources

- **Design Document**: `.kiro/specs/get-sp-loads-coroutine-migration/design.md`
- **Task List**: `.kiro/specs/get-sp-loads-coroutine-migration/tasks.md`
- **Kotlin Coroutines Guide**: https://kotlinlang.org/docs/coroutines-guide.html
- **Kotest Property Testing**: https://kotest.io/docs/proptest/property-based-testing.html
- **MockK Documentation**: https://mockk.io/
- **Android ViewModel Testing**: https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-basics

---

## Appendix A: Code Snippets Reference

### A.1 viewModelScope.launch Pattern
```kotlin
viewModelScope.launch {
    try {
        val result = repository.fetch()
        when (result) {
            is Resource.Success -> { /* handle */ }
            is Resource.Failure -> { /* handle */ }
        }
    } catch (e: Exception) {
        if (e !is CancellationException) {
            e.handle()
        }
    } finally {
        dataLoadingLiveData.postValue(false)
    }
}
```

---

### A.2 coroutineScope { async {} } Pattern
```kotlin
val parallelResults = coroutineScope {
    val result1 = async { repository.fetch1() }
    val result2 = async { repository.fetch2() }
    val result3 = async { repository.fetch3() }
    
    Triple(result1.await(), result2.await(), result3.await())
}
```

---

### A.3 safeApiCall Pattern
```kotlin
suspend fun fetchData(): Resource<Data> = safeApiCall {
    val response = service.getData()
    if (response.isSuccess) {
        response.responseData ?: throw Exception("Null response data")
    } else {
        throw response.toHttpException()
    }
}
```

---

### A.4 Tab Count Extraction Pattern
```kotlin
var intercityCount = 0
var nonDlvCount = 0

splitResult.data?.loadCounts?.all?.forEach { item ->
    when (item.key) {
        "INTERCITY" -> intercityCount = item.count ?: 0
        "NON_DELHIVERY" -> nonDlvCount = item.count ?: 0
    }
}

val fullCount = intracityCount + intercityCount + nonDlvCount + marketplaceCount
fullLoadsCountLiveData.postValue(fullCount)
```

---

## Appendix B: Testing Utilities

### B.1 Kotest Arb Generators
```kotlin
fun Arb.Companion.transactionsResponse(): Arb<TransactionsResponse> = arbitrary {
    TransactionsResponse(
        transactions = Arb.list(Arb.homeBidsRequestItemData(), 0..20).bind(),
        total = Arb.int(0..100).bind(),
        offset = Arb.int(0..100).bind(),
        hasNext = Arb.boolean().bind(),
        searchAfter = Arb.searchAfter().orNull().bind(),
        loadCounts = Arb.loadCounts().orNull().bind(),
        loadPricePercent = Arb.int(0..100).bind(),
        more_loads = Arb.boolean().bind()
    )
}

fun Arb.Companion.loadCounts(): Arb<LoadCounts> = arbitrary {
    LoadCounts(
        all = listOf(
            LoadCountItem("INTERCITY", Arb.int(0..50).bind()),
            LoadCountItem("NON_DELHIVERY", Arb.int(0..50).bind())
        )
    )
}

fun Arb.Companion.searchAfter(): Arb<SearchAfter> = arbitrary {
    SearchAfter(
        creationTime = Arb.long().bind(),
        transactionId = Arb.string().bind(),
        requiredOn = Arb.long().bind()
    )
}
```

---

### B.2 MockK Setup
```kotlin
@Before
fun setup() {
    MockKAnnotations.init(this)
    
    // Setup InstantTaskExecutorRule for LiveData
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    // Setup TestDispatcher for coroutines
    Dispatchers.setMain(StandardTestDispatcher())
    
    // Create ViewModel with mocked dependencies
    viewModel = HomeLoadsViewModel(
        transactionsRepository = mockTransactionsRepository,
        userRepository = mockUserRepository,
        bidsRepository = mockBidsRepository,
        truckRepository = mockTruckRepository,
        tripsRepository = mockTripsRepository,
        userPrefs = mockUserPrefs
    )
}

@After
fun tearDown() {
    Dispatchers.resetMain()
}
```

---

## Document Version

**Version:** 1.0  
**Date:** 2024  
**Author:** Kiro AI Assistant  
**Status:** Complete and Ready for Implementation

---

**End of Migration Plan**
