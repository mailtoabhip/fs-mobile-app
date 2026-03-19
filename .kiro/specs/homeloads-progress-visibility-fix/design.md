# HomeLoads Progress Visibility Fix - Bugfix Design

## Overview

This bugfix addresses the loading indicator visibility issue in the HomeLoads section where the `HomeLoadsProgressItem` is incorrectly managed during filter tab switches. When users click on filter tabs (Intracity, Intercity, Marketplace, Others), the loading indicator is removed before it can be rendered, creating a poor user experience where the app appears unresponsive during data fetching. Additionally, the filter tabs (`HomeLoadsFilterItem`) and search bar (`HomeLoadsSearchItem`) remain visible during loading when they should be hidden.

The fix involves conditionally removing the progress item based on the `paginate` parameter in the ViewModel's fetch methods, ensuring the loading indicator remains visible during initial data loads while preserving the existing pagination behavior.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug - when a filter tab is clicked (paginate=false) and the progress item is removed before the UI can render it
- **Property (P)**: The desired behavior - progress item should remain visible during data fetch for filter tab clicks
- **Preservation**: Existing pagination behavior (paginate=true) that must remain unchanged by the fix
- **HomeLoadsProgressItem**: The loading indicator adapter item that shows a progress spinner in the RecyclerView
- **HomeLoadsFilterItem**: The filter tabs adapter item that displays Intracity/Intercity/Marketplace/Others tabs with counts
- **HomeLoadsSearchItem**: The search bar adapter item at the top of the loads list
- **paginate**: Boolean parameter indicating whether the fetch is for pagination (true) or initial load/filter switch (false)
- **resetStaticData()**: Adapter method that clears existing items and adds a progress item with AddUpdate operation
- **AddUpdate**: Adapter operation type that adds or updates an item in the RecyclerView
- **Remove**: Adapter operation type that removes an item from the RecyclerView
- **StateFlow**: Kotlin coroutine flow that holds UI state and emits updates to observers

## Bug Details

### Bug Condition

The bug manifests when a user clicks on any filter tab (Intracity, Intercity, Marketplace, Others) to switch between load types. The `HomeLoadsFragment.refreshData()` method calls `adapter.resetStaticData()` which adds a `HomeLoadsProgressItem` with `AddUpdate` operation. However, the ViewModel's fetch methods (`fetchUserTransactions()`, `fetchSpotMarketplaceLoads()`) immediately add a `HomeLoadsProgressItem` with `Remove` operation as the first item in the operations list. This Remove operation is processed by the adapter before the UI can render the progress item, resulting in no visible loading indicator.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type FilterTabClickEvent
  OUTPUT: boolean
  
  RETURN input.filterTab IN ['Intracity', 'Intercity', 'Marketplace', 'Others']
         AND input.paginate == false
         AND progressItemAddedByResetStaticData()
         AND progressItemRemovedByViewModel()
         AND NOT progressItemVisibleToUser()
END FUNCTION
```

### Examples

- **Intracity → Marketplace**: User clicks Marketplace tab, `refreshData()` adds progress item, `fetchSpotMarketplaceLoads(paginate=false)` immediately removes it, user sees no loading indicator
- **Marketplace → Intercity**: User clicks Intercity tab, `refreshData()` adds progress item, `fetchUserTransactions(paginate=false, demandType="INTERCITY")` immediately removes it, user sees no loading indicator
- **Intercity → Others**: User clicks Others tab, `refreshData()` adds progress item, `fetchUserTransactions(paginate=false, demandType="NON_DELHIVERY")` immediately removes it, user sees no loading indicator
- **Pagination (Edge Case - Expected Behavior)**: User scrolls to bottom, `fetchUserTransactions(paginate=true)` removes old progress item and adds new one at bottom, progress item IS visible (this behavior should be preserved)

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Pagination loading (paginate=true) must continue to work exactly as before - progress item should be removed and re-added at the bottom of the list
- Mouse clicks and touch interactions on filter tabs must continue to work
- Tab count updates must continue to display correctly
- Error handling and timeout UI must continue to work
- KYC pending banner display logic must remain unchanged
- Routes banner visibility logic must remain unchanged
- Search functionality must continue to work

**Scope:**
All inputs that do NOT involve filter tab clicks for initial data load (paginate=false) should be completely unaffected by this fix. This includes:
- Pagination requests (paginate=true) when scrolling to load more data
- Error state handling and retry mechanisms
- Tab count updates from parallel API calls
- Other adapter item operations (search bar, filter tabs, load items, banners)

## Hypothesized Root Cause

Based on the bug description and code analysis, the root cause is:

1. **Unconditional Progress Item Removal**: The ViewModel's fetch methods unconditionally add `Pair(HomeLoadsProgressItem(), Remove)` as the first operation in the adapter operations list, regardless of whether it's a pagination request or an initial load/filter switch.

2. **Race Condition Between Adapter Operations**: When `refreshData()` is called:
   - `adapter.resetStaticData()` adds `HomeLoadsProgressItem()` with `AddUpdate` operation
   - Immediately after, ViewModel's fetch method adds `HomeLoadsProgressItem()` with `Remove` operation
   - The StateFlow emits the Remove operation before the UI can render the AddUpdate operation
   - Result: Progress item is removed before it becomes visible

3. **Missing Conditional Logic**: The code lacks a conditional check to distinguish between:
   - Initial load/filter switch (paginate=false) - should NOT remove progress item
   - Pagination (paginate=true) - should remove old progress item and add new one at bottom

4. **Incorrect Adapter Item Management**: The ViewModel always includes `HomeLoadsSearchItem` and `HomeLoadsFilterItem` in the operations list, even during loading state, when they should be hidden until data is ready.

## Correctness Properties

Property 1: Bug Condition - Progress Item Visibility During Filter Tab Clicks

_For any_ filter tab click event where paginate=false and a progress item has been added by resetStaticData(), the fixed ViewModel fetch methods SHALL NOT remove the progress item, allowing it to remain visible to the user during the data fetch operation, and SHALL hide the search bar and filter tabs until data is loaded.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4**

Property 2: Preservation - Pagination Loading Behavior

_For any_ pagination request where paginate=true and the user has scrolled to the bottom of the list, the fixed ViewModel fetch methods SHALL continue to remove the old progress item and add a new progress item at the bottom of the list, preserving the existing pagination loading behavior exactly as it was before the fix.

**Validates: Requirements 3.1, 3.4, 3.7**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `app/src/main/java/com/delhivery/axle/ui/home/fragments/loads/HomeLoadsViewModel.kt`

**Function**: `fetchLoadsData()` (Intracity branch - lines 445-540)

**Specific Changes**:
1. **Wrap Progress Item Remove Operation**: Add conditional check before removing progress item
   - Current: `add(Pair(HomeLoadsProgressItem(), Remove))`
   - Fixed: `if (paginate) { add(Pair(HomeLoadsProgressItem(), Remove)) }`
   - Location: Line 446 in the Intracity branch

2. **Conditional Search Bar Addition**: Only add search bar after loading completes
   - Current: Always adds `HomeLoadsSearchItem` immediately
   - Fixed: Add search bar only after data is loaded (not during loading state)
   - Location: Line 450 in the Intracity branch

3. **Conditional Filter Tabs Addition**: Only add filter tabs after loading completes
   - Current: Always adds `HomeLoadsFilterItem` immediately
   - Fixed: Add filter tabs only after data is loaded (not during loading state)
   - Location: Line 455 in the Intracity branch

4. **Progress Item Visibility During Loading**: Ensure progress item remains visible
   - When paginate=false, do NOT add Remove operation
   - Progress item added by `resetStaticData()` will remain visible
   - Only remove progress item when data is ready to be displayed

**Function**: `fetchLoadsData()` (Non-intracity branch - lines 545-800)

**Specific Changes**:
1. **Wrap Progress Item Remove Operation**: Add conditional check before removing progress item
   - Current: `add(Pair(HomeLoadsProgressItem(), Remove))`
   - Fixed: `if (paginate) { add(Pair(HomeLoadsProgressItem(), Remove)) }`
   - Location: Line 676 in the non-intracity branch

2. **Conditional Search Bar Addition**: Only add search bar after loading completes
   - Current: Always adds `HomeLoadsSearchItem` immediately
   - Fixed: Add search bar only after data is loaded (not during loading state)
   - Location: Line 679 in the non-intracity branch

3. **Conditional Filter Tabs Addition**: Only add filter tabs after loading completes
   - Current: Always adds `HomeLoadsFilterItem` immediately
   - Fixed: Add filter tabs only after data is loaded (not during loading state)
   - Location: Line 686 in the non-intracity branch

**Function**: `fetchSupplierLoadsData()` (lines 850-990)

**Specific Changes**:
1. **Wrap Progress Item Remove Operation**: Add conditional check before removing progress item
   - Current: `add(Pair(HomeLoadsProgressItem(), Remove))`
   - Fixed: `if (paginate) { add(Pair(HomeLoadsProgressItem(), Remove)) }`
   - Location: Line 879

2. **Conditional Search Bar Addition**: Only add search bar after loading completes
   - Location: Line 900

3. **Conditional Filter Tabs Addition**: Only add filter tabs after loading completes
   - Location: Line 903

**Function**: `fetchMarketplaceLoadsData()` (lines 1050-1300)

**Specific Changes**:
1. **Wrap Progress Item Remove Operation**: Add conditional check before removing progress item
   - Current: `add(Pair(HomeLoadsProgressItem(), Remove))`
   - Fixed: `if (paginate) { add(Pair(HomeLoadsProgressItem(), Remove)) }`
   - Location: Line 1153

2. **Conditional Search Bar Addition**: Only add search bar after loading completes
   - Location: Line 1157

3. **Conditional Filter Tabs Addition**: Only add filter tabs after loading completes
   - Location: Line 1175

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Write instrumented tests that simulate filter tab clicks and verify that the progress item is NOT visible on unfixed code. Run these tests on the UNFIXED code to observe failures and understand the root cause.

**Test Cases**:
1. **Intracity to Marketplace Switch**: Simulate clicking Marketplace tab, verify progress item is NOT visible on unfixed code (will fail on unfixed code)
2. **Marketplace to Intercity Switch**: Simulate clicking Intercity tab, verify progress item is NOT visible on unfixed code (will fail on unfixed code)
3. **Intercity to Others Switch**: Simulate clicking Others tab, verify progress item is NOT visible on unfixed code (will fail on unfixed code)
4. **Multiple Rapid Switches**: Simulate rapid filter tab switching, verify progress item is NOT visible on unfixed code (will fail on unfixed code)

**Expected Counterexamples**:
- Progress item is removed before UI can render it
- User sees no loading indicator during filter tab switches
- Possible causes: unconditional Remove operation, race condition between adapter operations, missing conditional logic

**Test File**: `app/src/androidTest/java/com/delhivery/axle/HomeLoadsFilterLoadingStateTest.kt` (already exists)

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed function produces the expected behavior.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := fetchLoadsData_fixed(input)
  ASSERT progressItemVisibleToUser(result)
  ASSERT searchBarHiddenDuringLoading(result)
  ASSERT filterTabsHiddenDuringLoading(result)
END FOR
```

**Test Plan**: After implementing the fix, run the same tests that failed on unfixed code. They should now pass, demonstrating that the progress item remains visible during filter tab switches.

**Test Cases**:
1. **Intracity to Marketplace Switch**: Verify progress item IS visible after fix
2. **Marketplace to Intercity Switch**: Verify progress item IS visible after fix
3. **Intercity to Others Switch**: Verify progress item IS visible after fix
4. **Multiple Rapid Switches**: Verify progress item IS visible for each switch after fix

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed function produces the same result as the original function.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT fetchLoadsData_original(input) = fetchLoadsData_fixed(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs

**Test Plan**: Observe behavior on UNFIXED code first for pagination and other interactions, then write property-based tests capturing that behavior.

**Test Cases**:
1. **Pagination Loading Preservation**: Observe that pagination (paginate=true) works correctly on unfixed code, then write test to verify this continues after fix
2. **Tab Count Updates Preservation**: Observe that tab counts update correctly on unfixed code, then write test to verify this continues after fix
3. **Error Handling Preservation**: Observe that error states display correctly on unfixed code, then write test to verify this continues after fix
4. **Search Functionality Preservation**: Observe that search works correctly on unfixed code, then write test to verify this continues after fix

**Test File**: `app/src/androidTest/java/com/delhivery/axle/HomeLoadsPreservationPropertyTest.kt` (already exists)

### Unit Tests

- Test conditional progress item removal logic in ViewModel
- Test adapter operations list construction for paginate=true vs paginate=false
- Test StateFlow emissions for loading state changes
- Test that search bar and filter tabs are hidden during loading

### Property-Based Tests

- Generate random filter tab switch sequences and verify progress item visibility
- Generate random pagination scenarios and verify existing behavior is preserved
- Test that all non-filter-switch inputs continue to work across many scenarios
- Generate random loading state transitions and verify correct adapter item visibility

### Integration Tests

- Test full filter tab switch flow with progress item visibility in each context
- Test switching between all filter tabs and verify loading indicator appears
- Test that visual feedback (progress spinner) is visible during data fetch
- Test rapid filter tab switching with network delays
- Test pagination loading continues to work correctly after fix
