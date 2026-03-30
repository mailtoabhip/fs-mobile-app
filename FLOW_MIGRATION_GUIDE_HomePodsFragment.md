# Flow Migration Guide: HomePodsFragment

This document provides a comprehensive guide for migrating `HomePodsFragment` from RxJava-based `HomePodViewModel` to Flow-based `HomePodViewModelFlow` with lifecycle-aware state collection.

## Overview

The migration involves:
1. Injecting the new `HomePodViewModelFlow` alongside the existing ViewModel
2. Adding lifecycle-aware StateFlow and event collection
3. Implementing `renderState()` function for UI updates
4. Updating user intent triggers to call ViewModel intent methods
5. Maintaining backward compatibility during transition

## Step 1: Add Flow ViewModel Injection

Add the Flow-based ViewModel injection in the Fragment:

```kotlin
class HomePodsFragment : HomeBaseFragment<FragmentHomePodBinding, HomePodViewModel>(),
    HomePodRVAdapterInterface, ToolbarElevationChangeListener {

  // Existing RxJava ViewModel (keep for backward compatibility)
  // viewModel is already injected by base class
  
  // New Flow-based ViewModel
  @Inject lateinit var viewModelFlow: HomePodViewModelFlow
  
  // ... rest of the code
}
```

## Step 2: Add Lifecycle-Aware State Collection

Add state collection in `onViewCreated()`:

```kotlin
override fun onViewCreated(
  view: View,
  savedInstanceState: Bundle?
) {
  super.onViewCreated(view, savedInstanceState)
  
  // ... existing setup code ...
  
  // Collect UI state using lifecycle-aware coroutines
  viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
      viewModelFlow.uiState.collect { state ->
        renderState(state)
      }
    }
  }
  
  // Collect one-time events
  viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
      viewModelFlow.events.collect { event ->
        handleEvent(event)
      }
    }
  }
  
  // Collect pod counts
  viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
      viewModelFlow.podCounts.collect { podCounts ->
        podCounts?.let {
          // Update UI with pod counts if needed
        }
      }
    }
  }
  
  // ... rest of existing code ...
}
```

## Step 3: Implement renderState() Function

Add the `renderState()` function to handle all UI state changes:

```kotlin
/**
 * Renders UI based on current UiState.
 * This function handles all possible states exhaustively.
 */
private fun renderState(state: UiState<List<HomeTripsItemData>>) {
  when (state) {
    is UiState.Idle -> {
      // Initial state - show static items
      adapter.setItems(getStaticItems())
    }
    
    is UiState.Loading -> {
      if (state.isRefreshing) {
        // Pull-to-refresh in progress
        binding.refreshLayout.isRefreshing = true
      } else {
        // Initial loading - show progress
        isLoadingData = true
        adapter.operation(listOf(
          Pair(HomePodProgressItem(), DataRVAdapterOperationType.AddUpdate)
        ))
      }
    }
    
    is UiState.Success -> {
      // Hide loading indicators
      binding.refreshLayout.isRefreshing = false
      isLoadingData = false
      
      // Remove progress item
      val items = mutableListOf<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>()
      items.add(Pair(HomePodProgressItem(), DataRVAdapterOperationType.Remove))
      
      // Add trip items
      for (trip in state.data) {
        trip.selectable = viewModel.selectable
        items.add(Pair(HomePodTripItem(trip), DataRVAdapterOperationType.Add))
      }
      
      adapter.operation(items)
      
      // Update title with count
      _title = when (state.data.size) {
        0 -> getString(R.string.label_pod_status)
        else -> "${getString(R.string.label_pod_status)}(${state.data.size})"
      }
      
      // Show/hide load more footer
      if (state.isLoadingMore) {
        adapter.operation(listOf(
          Pair(HomePodProgressItem(), DataRVAdapterOperationType.AddUpdate)
        ))
      }
    }
    
    is UiState.Empty -> {
      // No data available
      binding.refreshLayout.isRefreshing = false
      isLoadingData = false
      
      val items = mutableListOf<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>()
      items.add(Pair(HomePodProgressItem(), DataRVAdapterOperationType.Remove))
      items.add(Pair(HomePodWarningItem_NoLoads, DataRVAdapterOperationType.AddUpdate))
      
      adapter.operation(items)
      
      _title = getString(R.string.label_pod_status)
    }
    
    is UiState.Error -> {
      // Error occurred
      binding.refreshLayout.isRefreshing = false
      isLoadingData = false
      
      val items = mutableListOf<Pair<BaseHomePodRVAdapterItem<*>, DataRVAdapterOperationType>>()
      items.add(Pair(HomePodProgressItem(), DataRVAdapterOperationType.Remove))
      items.add(Pair(HomePodWarningItem_TimeOut, DataRVAdapterOperationType.AddUpdate))
      
      adapter.operation(items)
      
      // Show error message
      uiUtils.showSnackbar(state.message)
    }
  }
}
```

## Step 4: Implement handleEvent() Function

Add the `handleEvent()` function for one-time events:

```kotlin
/**
 * Handles one-time UI events.
 * These events are consumed only once and don't replay after config changes.
 */
private fun handleEvent(event: UiEvent) {
  when (event) {
    is UiEvent.ShowToast -> {
      uiUtils.showToast(event.message)
    }
    
    is UiEvent.ShowSnackbar -> {
      if (event.action != null) {
        uiUtils.showSnackbarWithAction(
          message = event.message,
          actionText = event.action,
          action = { event.onActionClick?.invoke() }
        )
      } else {
        uiUtils.showSnackbar(event.message)
      }
    }
    
    is UiEvent.Navigate -> {
      // Handle navigation if needed
      // For now, navigation is handled through existing action system
    }
  }
}
```

## Step 5: Update refreshData() to Use Flow ViewModel

Update the `refreshData()` function to trigger the Flow-based ViewModel:

```kotlin
private fun refreshData() {
  adapter.resetStaticData()
  viewModel.selectedTransactions.clear()
  viewModel.selectable = false
  binding.btnSave.visibility = View.GONE
  
  // Build search request
  val request = com.delhivery.axle.api.request.SearchRequest()
  request.vendorId = viewModelFlow.userRepository.userId()
  
  // Set status-specific parameters
  when (viewModel.status) {
    TruckUnloaded -> {
      val cal = Calendar.getInstance()
      cal.add(Calendar.DATE, -14)
      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
      request.tripStatus = viewModel.status.statusKey
      request.value = DateUtils.formatDate(cal.time, OrionDateFormat)
    }
    EPodUploaded -> {
      request.tripStatus = EPodUploaded.statusKey + "," + TruckUnloaded.statusKey
      request.value = null
    }
    else -> {}
  }
  
  // Trigger search using Flow ViewModel
  viewModelFlow.searchTrips(request)
}
```

## Step 6: Update Pagination Listener

Update the `PaginationInterface` to use the Flow ViewModel:

```kotlin
/**
 * Pagination interface
 */
inner class PaginationInterface : PaginationScrollListener(
    UserTripsLoadLimit
) {
  override fun loadMore() {
    // Use Flow ViewModel for pagination
    viewModelFlow.loadMore()
  }

  override fun hasMore(): Boolean {
    // Check if more data is available from current state
    val currentState = viewModelFlow.uiState.value
    return currentState is UiState.Success && currentState.hasMore
  }

  override fun isLoading() = isLoadingData
}
```

## Step 7: Update SwipeRefreshLayout Listener

Update the refresh listener to use the Flow ViewModel:

```kotlin
binding.refreshLayout.setOnRefreshListener {
  // Use Flow ViewModel for refresh
  viewModelFlow.refresh()
}
```

## Step 8: Add Required Imports

Add these imports at the top of the file:

```kotlin
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import com.delhivery.axle.ui.common.UiState
import com.delhivery.axle.ui.common.UiEvent
```

## Step 9: Update Dagger Module

Ensure `HomePodViewModelFlow` is provided in the ViewModelModule:

```kotlin
@Module
abstract class ViewModelModule {
  
  // Existing ViewModel binding
  @Binds
  @IntoMap
  @ViewModelKey(HomePodViewModel::class)
  abstract fun bindHomePodViewModel(viewModel: HomePodViewModel): ViewModel
  
  // New Flow-based ViewModel binding
  @Binds
  @IntoMap
  @ViewModelKey(HomePodViewModelFlow::class)
  abstract fun bindHomePodViewModelFlow(viewModel: HomePodViewModelFlow): ViewModel
}
```

## Testing the Migration

### Manual Testing Checklist

1. **Initial Load**
   - [ ] App shows loading indicator on first launch
   - [ ] Trips load and display correctly
   - [ ] Empty state shows when no trips available
   - [ ] Error state shows on network failure

2. **Pull-to-Refresh**
   - [ ] Swipe down triggers refresh
   - [ ] Refresh indicator shows during refresh
   - [ ] Data updates after successful refresh
   - [ ] Existing data preserved on refresh failure

3. **Pagination**
   - [ ] Scroll to bottom triggers load more
   - [ ] Footer loading indicator shows during load more
   - [ ] New trips append to existing list
   - [ ] Load more stops when no more data available

4. **Error Handling**
   - [ ] Network errors show appropriate message
   - [ ] Retry button works correctly
   - [ ] Timeout errors handled gracefully

5. **Configuration Changes**
   - [ ] Screen rotation preserves state
   - [ ] No duplicate toasts/snackbars after rotation
   - [ ] Loading state survives rotation

6. **Memory Leaks**
   - [ ] Run LeakCanary to verify no leaks
   - [ ] Navigate away and back multiple times
   - [ ] Rotate screen multiple times

## Rollback Plan

If issues arise, you can easily rollback by:

1. Comment out the Flow ViewModel state collection code
2. Keep using the existing RxJava ViewModel
3. The old code path remains intact

## Benefits of Migration

1. **Modern Architecture**: Uses Kotlin Coroutines and Flow instead of RxJava
2. **Lifecycle-Aware**: Automatic cancellation when Fragment is not visible
3. **Type-Safe**: Sealed classes ensure all states are handled
4. **Testable**: Easy to test with Turbine and coroutine test utilities
5. **Memory Safe**: No memory leaks with proper lifecycle management
6. **Predictable**: MVI pattern ensures unidirectional data flow

## Next Steps

After successful migration of HomePodsFragment:

1. Migrate other fragments using the same pattern
2. Remove RxJava dependencies once all endpoints migrated
3. Add comprehensive unit tests for ViewModels
4. Add property-based tests for correctness properties
5. Document patterns for team reference

## Support

For questions or issues during migration:
- Review the design document at `.kiro/specs/flow-based-api-state-management/design.md`
- Check the requirements at `.kiro/specs/flow-based-api-state-management/requirements.md`
- Refer to the tasks list at `.kiro/specs/flow-based-api-state-management/tasks.md`
