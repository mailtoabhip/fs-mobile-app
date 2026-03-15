# Changelog: Resource.Loading State Addition

**Date**: March 2026  
**Change Type**: Enhancement  
**Scope**: Resource sealed class state management

---

## Overview

Added `Resource.Loading` as a third state to the `Resource` sealed class, replacing the separate `isLoading: LiveData<Boolean>` pattern. This change provides a single source of truth for API operation states and enforces exhaustive when expression handling in the UI layer.

---

## Files Modified

### 1. `app/src/main/java/com/delhivery/axle/api/repository/Resource.kt`

**Change**: Added `Loading` object as a new state in the Resource sealed class.

**Before**:
```kotlin
sealed class Resource<out T> {
    data class Success<out T>(val data: T?) : Resource<T>()
    data class Failure(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val apiError: ApiError
    ) : Resource<Nothing>()
}
```

**After**:
```kotlin
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T?) : Resource<T>()
    data class Failure(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val apiError: ApiError
    ) : Resource<Nothing>()
}
```

**Impact**: 
- All UI code observing `Resource<T>` must now handle three states instead of two
- Provides single source of truth for loading state
- Enforces exhaustive when expressions

---

### 2. `app/src/main/java/com/delhivery/axle/ui/example/ExampleViewModel.kt`

**Changes**:
1. Removed separate `isLoading: LiveData<Boolean>` properties
2. Updated methods to emit `Resource.Loading` before API calls
3. Simplified ViewModel code by removing manual loading flag management

**Before**:
```kotlin
class ExampleViewModel @Inject constructor(
    private val repository: ExampleRepository
) : ViewModel() {

    private val _userDataState = MutableLiveData<Resource<ExampleUserData>>()
    val userDataState: LiveData<Resource<ExampleUserData>> = _userDataState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.fetchUserData()
            _userDataState.value = result
            _isLoading.value = false
        }
    }
}
```

**After**:
```kotlin
class ExampleViewModel @Inject constructor(
    private val repository: ExampleRepository
) : ViewModel() {

    private val _userDataState = MutableLiveData<Resource<ExampleUserData>>()
    val userDataState: LiveData<Resource<ExampleUserData>> = _userDataState

    fun fetchUserData() {
        viewModelScope.launch {
            _userDataState.value = Resource.Loading
            val result = repository.fetchUserData()
            _userDataState.value = result
        }
    }
}
```

**Removed**:
- `_isLoading: MutableLiveData<Boolean>` property
- `isLoading: LiveData<Boolean>` exposed property
- Manual `_isLoading.value = true/false` calls

**Added**:
- `_userDataState.value = Resource.Loading` emission before API call

---

### 3. `app/src/main/java/com/delhivery/axle/ui/example/UIObservationPatterns.kt`

**Changes**: Updated all 5 UI observation patterns to handle `Resource.Loading` state.

#### Pattern 1: Basic Resource Observation

**Before**:
```kotlin
// Observe loading state
viewModel.isLoading.observe(this) { isLoading ->
    if (isLoading) {
        progressBar.visibility = View.VISIBLE
    } else {
        progressBar.visibility = View.GONE
    }
}

// Observe Resource state
viewModel.userDataState.observe(this) { resource ->
    when (resource) {
        is Resource.Success -> { /* handle success */ }
        is Resource.Failure -> { /* handle failure */ }
    }
}
```

**After**:
```kotlin
// Single observation with exhaustive when
viewModel.userDataState.observe(this) { resource ->
    when (resource) {
        is Resource.Loading -> {
            progressBar.visibility = View.VISIBLE
            contentLayout.visibility = View.GONE
        }
        is Resource.Success -> {
            progressBar.visibility = View.GONE
            contentLayout.visibility = View.VISIBLE
            // handle success
        }
        is Resource.Failure -> {
            progressBar.visibility = View.GONE
            contentLayout.visibility = View.GONE
            // handle failure
        }
    }
}
```

#### Pattern 2: Extension Functions (Updated)

**Before**: Used `onSuccess` and `onFailure` extension functions with separate loading observation.

**After**: Uses standard when expression with Loading state:
```kotlin
viewModel.combinedDataState.observe(viewLifecycleOwner) { resource ->
    when (resource) {
        is Resource.Loading -> showLoading()
        is Resource.Success -> {
            hideLoading()
            resource.data?.let { /* update UI */ }
        }
        is Resource.Failure -> {
            hideLoading()
            showError(resource.apiError.toErrorMessage(requireContext()))
        }
    }
}
```

#### Pattern 3: Lifecycle-Aware Observation

**Added**: `is Resource.Loading -> showLoadingState()` branch to when expression.

#### Pattern 4: Retry Logic

**Before**: Separate loading view management.

**After**:
```kotlin
when (resource) {
    is Resource.Loading -> {
        hideErrorView()
        showLoadingView()
    }
    is Resource.Success -> {
        hideLoadingView()
        hideErrorView()
        showLoadsList(resource.data)
    }
    is Resource.Failure -> {
        hideLoadingView()
        hideLoadsList()
        showErrorViewWithRetry(...)
    }
}
```

#### Pattern 5: Pull-to-Refresh

**Before**: Separate `isLoading` observation to control `SwipeRefreshLayout`.

**After**:
```kotlin
when (resource) {
    is Resource.Loading -> {
        swipeRefreshLayout.isRefreshing = true
    }
    is Resource.Success -> {
        swipeRefreshLayout.isRefreshing = false
        // update UI
    }
    is Resource.Failure -> {
        swipeRefreshLayout.isRefreshing = false
        // show error
    }
}
```

#### Key Takeaways Updated

**Before**:
- Handle loading state separately from Resource state
- Use extension functions (onSuccess/onFailure) for cleaner code

**After**:
- Resource.Loading replaces separate isLoading LiveData
- Always use exhaustive when expressions for Resource handling (Loading, Success, Failure)
- Emit Resource.Loading before every API call in ViewModel

---

### 4. `app/src/test/java/com/delhivery/axle/api/repository/BaseRepositoryTest.kt`

**Change**: No functional changes required. Tests remain the same as they test repository methods that return `Resource.Success` or `Resource.Failure` directly. The `Resource.Loading` state is emitted at the ViewModel layer, not the repository layer.

**Note**: Tests continue to verify that:
- `safeApiCall` returns `Resource.Success` on success
- `safeApiCall` returns `Resource.Failure` with correct `ApiError` on exceptions
- `parallelApiCall2` and `parallelApiCall3` work correctly

---

### 5. `app/src/test/java/com/delhivery/axle/ui/example/ExampleViewModelTest.kt`

**Change**: New test file created to verify Resource.Loading state emission and state transitions at the ViewModel layer.

**Test Coverage**:
```kotlin
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
}
```

**Test Methods**:
1. `fetchUserData emits Loading then Success` — Verifies Loading → Success transition
2. `fetchUserData emits Loading then Failure on error` — Verifies Loading → Failure transition
3. `fetchCombinedData emits Loading then Success` — Tests parallel API calls
4. `fetchCombinedData emits Loading then Failure on timeout` — Tests timeout scenario
5. `Resource Loading state exists and is singleton` — Verifies singleton pattern
6. `Resource sealed class has three states` — Tests exhaustive when expression

**Why This Test File Was Added**:
- BaseRepositoryTest tests the repository layer (no Loading state there)
- ExampleViewModelTest tests the ViewModel layer where Loading is emitted
- Ensures Loading is always emitted before API calls
- Validates proper state transitions
- Prevents regressions if Loading emission is removed

---

## Summary of Changes

### Added
- `Resource.Loading` object state to Resource sealed class

### Removed
- Separate `isLoading: LiveData<Boolean>` pattern from ViewModels
- Manual loading flag management (`_isLoading.value = true/false`)
- Dual observation pattern (one for loading, one for data)

### Modified
- ExampleViewModel: Simplified to emit `Resource.Loading` before API calls
- UIObservationPatterns: All 5 patterns updated to handle Loading state
- Documentation: Updated comments and KDocs to reflect new pattern

### Benefits
1. **Single Source of Truth**: One LiveData stream contains all states
2. **Exhaustive Handling**: Compiler enforces handling all three states
3. **Cleaner ViewModels**: No manual loading flag management
4. **Explicit State Transitions**: Loading → Success/Failure is clear
5. **Simpler UI Code**: One observation instead of two

### Migration Guide for Existing Code

**Step 1**: Update ViewModel
```kotlin
// Before
fun fetchData() {
    viewModelScope.launch {
        _isLoading.value = true
        val result = repository.fetchData()
        _dataState.value = result
        _isLoading.value = false
    }
}

// After
fun fetchData() {
    viewModelScope.launch {
        _dataState.value = Resource.Loading
        val result = repository.fetchData()
        _dataState.value = result
    }
}
```

**Step 2**: Remove isLoading LiveData
```kotlin
// Remove these lines
private val _isLoading = MutableLiveData<Boolean>()
val isLoading: LiveData<Boolean> = _isLoading
```

**Step 3**: Update UI observation
```kotlin
// Before
viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
    if (isLoading) showLoading() else hideLoading()
}
viewModel.dataState.observe(viewLifecycleOwner) { resource ->
    when (resource) {
        is Resource.Success -> { /* ... */ }
        is Resource.Failure -> { /* ... */ }
    }
}

// After
viewModel.dataState.observe(viewLifecycleOwner) { resource ->
    when (resource) {
        is Resource.Loading -> showLoading()
        is Resource.Success -> {
            hideLoading()
            // handle success
        }
        is Resource.Failure -> {
            hideLoading()
            // handle failure
        }
    }
}
```

---

## Files Affected Summary

| File | Change Type | Description |
|------|-------------|-------------|
| `Resource.kt` | Modified | Added `Loading` object state |
| `ExampleViewModel.kt` | Modified | Removed `isLoading` LiveData, emit `Resource.Loading` |
| `UIObservationPatterns.kt` | Modified | Updated all 5 patterns to handle Loading state |
| `BaseRepositoryTest.kt` | No change | Tests remain valid (Loading emitted at ViewModel layer) |
| `ExampleViewModelTest.kt` | New file | ViewModel tests for Loading state emission and transitions |

**Total files modified**: 3  
**Total files added**: 1  
**Total lines added**: ~200  
**Total lines removed**: ~40  
**Net change**: +160 lines (cleaner ViewModels + comprehensive tests)

---

## Verification

All changes verified with zero compilation errors:
```
✓ Resource.kt - No diagnostics found
✓ ExampleViewModel.kt - No diagnostics found
✓ UIObservationPatterns.kt - No diagnostics found
✓ BaseRepositoryTest.kt - No diagnostics found
✓ ExampleViewModelTest.kt - No diagnostics found
```

All tests are ready to run and will verify:
- Repository layer: Exception handling and parallel API calls (BaseRepositoryTest)
- ViewModel layer: Loading state emission and state transitions (ExampleViewModelTest)
