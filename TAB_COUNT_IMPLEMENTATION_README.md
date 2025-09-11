# Tab Count Implementation for HomePlacementsFragment

## Overview
This implementation adds count display in brackets for the "Delayed" and "Expected" tabs in the HomePlacementsFragment.

## Changes Made

### 1. HomePlacementsViewModel.kt
- **Added LiveData for count tracking:**
  ```kotlin
  var delayedCountLiveData = MutableLiveData<Int>()
  var expectedCountLiveData = MutableLiveData<Int>()
  ```

- **Updated fetchPlacementLoads method:**
  - Added count posting after data segregation:
  ```kotlin
  // Post counts to LiveData
  delayedCountLiveData.postValue(delayedPlacementList.size)
  expectedCountLiveData.postValue(expectedPlacementList.size)
  ```

### 2. HomePlacementsFragment.kt
- **Added Observer for count changes:**
  ```kotlin
  // Observe count changes and update tabs
  viewModel.delayedCountLiveData.observe(viewLifecycleOwner, Observer { count ->
      updateTabCounts(count, null)
  })

  viewModel.expectedCountLiveData.observe(viewLifecycleOwner, Observer { count ->
      updateTabCounts(null, count)
  })
  ```

- **Enhanced updateTabCounts method:**
  - Made parameters nullable to allow partial updates
  - Added null safety checks
  - Improved error handling

## How It Works

1. **Data Loading:** When `fetchPlacementLoads()` is called, the ViewModel processes the data and segregates items into `delayedPlacementList` and `expectedPlacementList`.

2. **Count Posting:** After segregation, the ViewModel posts the counts to the respective LiveData objects.

3. **UI Update:** The Fragment observes these LiveData objects and automatically updates the tab counts when data changes.

4. **Tab Display:** The counts are displayed in brackets next to the tab titles (e.g., "Delayed (5)", "Expected (3)").

## Features

- **Real-time Updates:** Counts update automatically when data changes
- **Null Safety:** Handles cases where counts might be null
- **Error Handling:** Graceful error handling in case of exceptions
- **Performance:** Uses LiveData for efficient UI updates
- **Consistent Styling:** Maintains existing tab styling and colors

## Usage

The implementation is automatic and requires no additional code changes. The counts will appear as soon as data is loaded in either the Delayed or Expected fragments.

## Testing

To test the implementation:
1. Navigate to the Placements screen
2. Wait for data to load
3. Verify that counts appear in brackets next to tab titles
4. Switch between tabs to ensure counts are displayed correctly
5. Refresh data to verify counts update dynamically

## Dependencies

- AndroidX Lifecycle (for LiveData and Observer)
- Existing HomePlacementsFragment structure
- Existing badge_tab.xml layout

## Notes

- Counts are only displayed when greater than 0
- Empty counts show no brackets
- Tab styling remains consistent with existing design
- Implementation is backward compatible
