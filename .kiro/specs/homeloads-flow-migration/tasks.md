# Implementation Plan: HomeLoads Flow Migration

## Overview

This plan migrates HomeLoadsViewModel from LiveData with pure coroutines to Kotlin Flow APIs (StateFlow, SharedFlow). The implementation follows a bottom-up approach: first establishing the foundation (data models and repository helpers), then migrating the ViewModel, and finally updating the Fragment to collect flows. Each task includes property-based tests to validate correctness properties from the design document.

## Tasks

- [x] 1. Create UI state and event models
  - [x] 1.1 Create HomeLoadsUiState data class in HomeLoadsViewModel.kt
    - Define data class with all state properties: loads, loadsFetch, isLoading, isLoadingMore, hasMoreData, offset, total, searchAfter, selectedFilter, vehicleTypes, filterVehicleType, intracityCount, intercityCount, nonDlvCount, marketplaceCount, fullLoadsCount, loadsCount, hasRoutes, error, isNetworkError
    - Set sensible default values for all properties
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [x] 1.2 Create HomeLoadsUiEvent sealed class in HomeLoadsViewModel.kt
    - Define sealed class with subclasses: BidActionResult, BulkBidActionResult, AcceptBidResult, LowestBidResult, ReviseBid, EditBulkResult, TruckTypesLoaded, TrackIntracityListShown, TrackIntercityListShown, TrackMarketplaceListShown, ShowErrorToast, ShowError
    - _Requirements: 2.1, 2.2, 2.3_

- [x] 2. Add Flow support to BaseRepository
  - [x] 2.1 Implement safeApiCallFlow helper in BaseRepository.kt
    - Add safeApiCallFlow function that wraps API calls in Flow<Resource<T>>
    - Emit Resource.Loading before API call
    - Emit Resource.Success or Resource.Failure based on result
    - Use flowOn(Dispatchers.IO) for network operations
    - Handle CancellationException, SocketTimeoutException, IOException, HttpException
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

  - [ ]* 2.2 Write property test for safeApiCallFlow emission sequence
    - **Property 3: Flow Emission Sequence**
    - **Validates: Requirements 6.2, 6.3**
    - Test that Flow emits Loading first, then exactly one terminal state (Success or Failure)
    - Use Kotest property testing with generated API scenarios

- [x] 3. Migrate HomeLoadsViewModel to StateFlow and SharedFlow
  - [x] 3.1 Add StateFlow and SharedFlow properties to HomeLoadsViewModel
    - Add private _uiState: MutableStateFlow<HomeLoadsUiState>
    - Add public uiState: StateFlow<HomeLoadsUiState>
    - Add private _uiEvent: MutableSharedFlow<HomeLoadsUiEvent> with replay=0, extraBufferCapacity=64, onBufferOverflow=DROP_OLDEST
    - Add public uiEvent: SharedFlow<HomeLoadsUiEvent>
    - Initialize _uiState with default HomeLoadsUiState()
    - _Requirements: 3.1, 3.2, 3.4, 4.1, 4.2, 4.3, 15.1, 15.2_

  - [x] 3.2 Add state update helper functions to HomeLoadsViewModel
    - Add private updateState function using _uiState.update { }
    - Add private suspend emitEvent function using _uiEvent.emit()
    - Add private sendEvent function using _uiEvent.tryEmit()
    - _Requirements: 7.1, 7.2, 7.3_

  - [ ]* 3.3 Write property test for immutable state updates
    - **Property 2: Immutable State Updates via Copy**
    - **Validates: Requirements 3.3**
    - Test that state updates create new instances while preserving unchanged fields
    - Use Kotest property testing with generated state update scenarios

  - [ ]* 3.4 Write property test for SharedFlow event single consumption
    - **Property 1: SharedFlow Event Single Consumption**
    - **Validates: Requirements 2.4, 4.3**
    - Test that events are not replayed to collectors that start after emission
    - Use Kotest property testing with generated UI events

- [x] 4. Migrate loads fetching methods to use StateFlow
  - [x] 4.1 Update fetchUserTransactions to update StateFlow
    - Replace userLoadsData.postValue with updateState for loads list
    - Replace dataLoadingLiveData.postValue with updateState for isLoading
    - Update pagination state (hasMoreData, offset, total, searchAfter) in StateFlow
    - Preserve currentFetchJob cancellation behavior
    - Handle CancellationException without logging errors
    - _Requirements: 3.5, 9.1, 9.2, 9.3, 12.1, 12.2, 12.3, 12.4_

  - [x] 4.2 Update fetchSupplierTransactions to update StateFlow
    - Replace userLoadsDataFetch.postValue with updateState for loadsFetch
    - Update loading and pagination state in StateFlow
    - _Requirements: 3.5, 9.1, 9.2, 9.3_

  - [ ]* 4.3 Write property test for pagination state transitions
    - **Property 5: Pagination State Transitions**
    - **Validates: Requirements 9.2, 9.3**
    - Test that pagination triggers correct state transitions: isLoadingMore=true → API call → isLoadingMore=false with updated offset/hasMoreData
    - Use Kotest property testing with generated pagination scenarios

  - [ ]* 4.4 Write property test for job cancellation behavior
    - **Property 8: Job Cancellation Behavior**
    - **Validates: Requirements 12.2, 12.3, 12.4**
    - Test that new fetch cancels previous job, CancellationException doesn't propagate, and UI state isn't updated from cancelled fetch
    - Use Kotest property testing with concurrent fetch scenarios

- [x] 5. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Migrate filter and count methods to use StateFlow
  - [x] 6.1 Update filter change methods to update StateFlow
    - Replace selectedFilter updates with updateState
    - Ensure filter changes trigger data refresh
    - Update vehicleTypes and filterVehicleType in StateFlow
    - _Requirements: 10.1, 10.2_

  - [ ]* 6.2 Write property test for filter change triggers refresh
    - **Property 6: Filter Change Triggers Refresh**
    - **Validates: Requirements 10.2**
    - Test that filter changes update selectedFilter AND initiate data refresh
    - Use Kotest property testing with generated filter scenarios

  - [x] 6.3 Update count fetching methods to update StateFlow
    - Replace loadsCountLiveData.postValue with updateState for loadsCount
    - Replace fullLoadsCountLiveData.postValue with updateState for fullLoadsCount
    - Update intracityCount, intercityCount, nonDlvCount, marketplaceCount atomically in single state emission
    - _Requirements: 11.1, 11.2, 14.3_

  - [ ]* 6.4 Write property test for atomic count updates
    - **Property 7: Atomic Count Updates**
    - **Validates: Requirements 11.2, 14.3**
    - Test that all counts are updated in a single atomic state emission
    - Use Kotest property testing with generated count scenarios

- [x] 7. Migrate bid action methods to use SharedFlow
  - [x] 7.1 Update bid action methods to emit events
    - Replace bidsActionLiveData.postValue with emitEvent(BidActionResult)
    - Replace bulkBidActionLiveData.postValue with emitEvent(BulkBidActionResult)
    - Replace acceptBidLiveData.postValue with emitEvent(AcceptBidResult)
    - Replace lowestBidLiveData.postValue with emitEvent(LowestBidResult)
    - Replace reviseBidLiveData.postValue with emitEvent(ReviseBid)
    - Replace editBulkLiveData.postValue with emitEvent(EditBulkResult)
    - _Requirements: 4.4_

  - [x] 7.2 Update truck fetching method to emit event
    - Replace truckGetLiveData.postValue with emitEvent(TruckTypesLoaded)
    - _Requirements: 4.4_

- [x] 8. Migrate analytics tracking to use SharedFlow
  - [x] 8.1 Update analytics tracking methods to emit events
    - Replace intercityListShownTracked.postValue with emitEvent(TrackIntercityListShown)
    - Replace intracityListShownTracked.postValue with emitEvent(TrackIntracityListShown)
    - Replace marketPlaceListShownTracked.postValue with emitEvent(TrackMarketplaceListShown)
    - Ensure events are emitted only on first successful fetch (paginateCount == 0)
    - _Requirements: 4.5, 13.1, 13.2, 13.3, 13.4_

  - [ ]* 8.2 Write property test for analytics tracking event emission
    - **Property 9: Analytics Tracking Event Emission**
    - **Validates: Requirements 13.1, 13.2, 13.3**
    - Test that tracking events are emitted exactly once on first successful fetch based on selected filter
    - Use Kotest property testing with generated fetch scenarios

- [x] 9. Update routes and error handling to use StateFlow
  - [x] 9.1 Update routes checking to update StateFlow
    - Replace routesLiveData.postValue with updateState for hasRoutes
    - _Requirements: 3.5_

  - [x] 9.2 Update error handling to use StateFlow and SharedFlow
    - Update error state in StateFlow (error, isNetworkError)
    - Emit ShowError and ShowErrorToast events via SharedFlow
    - Handle fallback to supplier transactions on error for non-intracity loads
    - _Requirements: 3.5_

- [x] 10. Remove deprecated LiveData properties from HomeLoadsViewModel
  - [x] 10.1 Remove migrated LiveData properties
    - Remove userLoadsData, userLoadsDataFetch, routesLiveData, loadsCountLiveData, fullLoadsCountLiveData, dataLoadingLiveData
    - Remove bidsActionLiveData, bulkBidActionLiveData, acceptBidLiveData, lowestBidLiveData, reviseBidLiveData, truckGetLiveData, editBulkLiveData
    - Remove intercityListShownTracked, intracityListShownTracked, marketPlaceListShownTracked
    - Retain progressLiveData for legacy dialog integration
    - _Requirements: 3.5, 4.4, 4.5, 8.1, 8.2, 8.3_

- [x] 11. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 12. Update HomeLoadsFragment to collect flows
  - [x] 12.1 Add flow collection for uiState in HomeLoadsFragment
    - Collect viewModel.uiState using repeatOnLifecycle(Lifecycle.State.STARTED)
    - Launch collection in viewLifecycleOwner.lifecycleScope
    - Implement renderState function to update UI based on state
    - _Requirements: 5.1, 5.3, 5.5, 5.6_

  - [x] 12.2 Add flow collection for uiEvent in HomeLoadsFragment
    - Collect viewModel.uiEvent using repeatOnLifecycle(Lifecycle.State.STARTED)
    - Launch collection in viewLifecycleOwner.lifecycleScope
    - Implement handleEvent function to process one-time events
    - _Requirements: 5.2, 5.3, 5.5, 5.6_

  - [x] 12.3 Implement renderState function in HomeLoadsFragment
    - Update adapter with loads and loadsFetch
    - Update tab counts via HomeLoadsTruckFragment._instance.dataToUpdate
    - Update routes banner visibility based on hasRoutes
    - Update loading state
    - Handle error states (error, isNetworkError)
    - _Requirements: 5.4_

  - [x] 12.4 Implement handleEvent function in HomeLoadsFragment
    - Handle BidActionResult, BulkBidActionResult, AcceptBidResult, LowestBidResult, ReviseBid, EditBulkResult
    - Handle TruckTypesLoaded
    - Handle TrackIntracityListShown, TrackIntercityListShown, TrackMarketplaceListShown
    - Handle ShowErrorToast, ShowError
    - _Requirements: 5.4, 13.4_

- [x] 13. Remove deprecated LiveData observers from HomeLoadsFragment
  - [x] 13.1 Remove migrated LiveData observe calls
    - Remove all observe() calls for migrated LiveData properties
    - Retain progressLiveData.observe for legacy dialog integration
    - _Requirements: 5.4, 8.3_

- [ ]* 14. Write property test for StateFlow conflation
  - **Property 4: StateFlow Conflation**
  - **Validates: Requirements 7.4**
  - Test that rapid state updates are conflated and final state matches last emission
  - Use Kotest property testing with rapid update scenarios

- [x] 15. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional property-based tests and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at reasonable breaks
- Property tests validate universal correctness properties using Kotest
- Unit tests (not included in this plan) should validate specific examples and edge cases
- The migration preserves backward compatibility with RxJava-based repository code
- progressLiveData is intentionally retained for legacy dialog integration
- Job cancellation behavior is preserved to handle rapid tab switching
