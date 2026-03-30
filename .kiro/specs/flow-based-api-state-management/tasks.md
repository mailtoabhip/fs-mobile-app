# Implementation Plan: Flow-based API State Management

## Overview

This implementation plan converts the Axle Android application from RxJava-based API state management to a modern Kotlin Flow-based architecture using the MVI (Model-View-Intent) pattern. The migration follows a 4-phase approach with the searchTrips endpoint as a reference implementation. Each task builds incrementally, ensuring testability and maintaining backward compatibility.

## Tasks

- [x] 1. Phase 1: Foundation - Create core Flow infrastructure
  - [x] 1.1 Create UiState sealed class for type-safe state modeling
    - Create `app/src/main/java/com/delhivery/axle/ui/common/UiState.kt`
    - Implement sealed class with Idle, Loading, Success, Empty, and Error states
    - Add generic type parameter for reusability across endpoints
    - Include metadata fields (isRefreshing, isLoadingMore, hasMore)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8_

  - [x] 1.2 Create UiEvent sealed class for one-time events
    - Create `app/src/main/java/com/delhivery/axle/ui/common/UiEvent.kt`
    - Implement sealed class with ShowToast, ShowSnackbar, and Navigate variants
    - Add necessary parameters for each event type
    - _Requirements: 8.1, 8.8_

  - [x] 1.3 Create Flow conversion utility (safeApiCallFlow)
    - Create `app/src/main/java/com/delhivery/axle/utils/extensions/FlowExtensions.kt`
    - Implement safeApiCallFlow extension function that wraps suspend calls in Flow
    - Add exception handling for all error types (Network, Timeout, HTTP, Unknown)
    - Emit Resource.Loading before API call, then Success or Failure
    - Use flowOn(Dispatchers.IO) for background execution
    - Handle CancellationException correctly (rethrow to propagate)
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.7_

  - [ ]* 1.4 Write unit tests for safeApiCallFlow utility
    - Create test file for FlowExtensions
    - Test successful API call emits Loading then Success
    - Test network error emits Loading then Failure with Network ApiError
    - Test timeout error emits Loading then Failure with Timeout ApiError
    - Test HTTP errors map to correct ApiError types
    - Test cancellation propagates correctly
    - _Requirements: 12.5, 12.7_

  - [ ]* 1.5 Write property tests for Flow conversion
    - **Property 1: Suspend API call data preservation**
    - **Validates: Requirements 1.1, 1.2**
    - **Property 2: Flow emission order (Loading before Success/Failure)**
    - **Validates: Requirements 3.3**
    - **Property 3: Error information preservation**
    - **Validates: Requirements 1.3, 1.7, 3.5**
    - **Property 4: Cancellation propagation**
    - **Validates: Requirements 1.5**

- [x] 2. Phase 1: Update Retrofit service to use suspend functions
  - [x] 2.1 Update LoadCycleService interface with suspend functions
    - Modify `app/src/main/java/com/delhivery/axle/api/service/LoadCycleService.kt`
    - Convert searchTrips from returning Single to suspend function
    - Convert getFrequentLanes from returning Single to suspend function
    - Keep existing RxJava methods for backward compatibility (mark as deprecated)
    - _Requirements: 1.1, 13.2, 13.3_

  - [x] 2.2 Update LoadCycleRepository with Flow-based methods
    - Modify `app/src/main/java/com/delhivery/axle/api/repository/LoadCycleRepository.kt`
    - Add searchTripsFlow method using safeApiCallFlow utility
    - Add getFrequentLanesFlow method using safeApiCallFlow utility
    - Keep existing RxJava methods for backward compatibility
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 13.1, 13.2_

  - [ ]* 2.3 Write unit tests for LoadCycleRepository Flow methods
    - Test searchTripsFlow emits Loading then Success on successful API call
    - Test searchTripsFlow emits Loading then Failure on network error
    - Test searchTripsFlow emits Loading then Failure on HTTP error with correct error code
    - Test getFrequentLanesFlow follows same emission pattern
    - _Requirements: 12.2, 12.7_

- [ ] 3. Checkpoint - Verify foundation layer
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Phase 2: Implement SearchTripsViewModel with MVI pattern
  - [x] 4.1 Create SearchTripsViewModel with StateFlow and intent handlers
    - Create `app/src/main/java/com/delhivery/axle/ui/searchtrips/SearchTripsViewModel.kt`
    - Add StateFlow for UI state (private MutableStateFlow, public StateFlow)
    - Add SharedFlow for one-time events (replay=0)
    - Implement searchTrips intent handler
    - Implement refresh intent handler
    - Implement loadMore intent handler
    - Implement retry intent handler
    - Add pagination state management (currentOffset, hasMore, pageSize)
    - Store currentSearchRequest for retry functionality
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 9.1_

  - [x] 4.2 Implement Resource to UiState mapping in ViewModel
    - Add mapResourceToUiState private function
    - Map Resource.Loading to UiState.Loading
    - Map Resource.Success with data to UiState.Success
    - Map Resource.Success with empty list to UiState.Empty
    - Map Resource.Failure to UiState.Error with user-friendly messages
    - Add getErrorMessage function for ApiError to message mapping
    - _Requirements: 4.5, 7.1, 7.2, 7.3, 7.4, 7.5_

  - [x] 4.3 Implement pagination logic in ViewModel
    - Handle loadMore intent with isLoadingMore flag
    - Append new data to existing list on successful load more
    - Preserve existing data on load more failure
    - Update currentOffset after successful pagination
    - Check hasMore flag before triggering load more
    - Build request with offset and limit parameters
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8_

  - [x] 4.4 Implement refresh logic in ViewModel
    - Handle refresh intent with isRefreshing flag
    - Reset pagination state (offset=0, hasMore=true)
    - Replace existing data with new data on successful refresh
    - Preserve existing data on refresh failure and emit snackbar event
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.7, 10.8_

  - [ ]* 4.5 Write unit tests for SearchTripsViewModel
    - Test searchTrips emits Idle, Loading, then Success states
    - Test searchTrips emits Error state on API failure
    - Test loadMore appends data to existing list
    - Test loadMore preserves data on failure
    - Test refresh replaces existing data
    - Test refresh preserves data on failure and emits snackbar event
    - Test retry triggers same request with same parameters
    - Test error state clears on new intent
    - Test one-time events are emitted correctly
    - _Requirements: 12.1, 12.3, 12.4_

  - [ ]* 4.6 Write property tests for ViewModel state management
    - **Property 5: Resource to UiState mapping correctness**
    - **Validates: Requirements 4.5**
    - **Property 6: State transition sequences**
    - **Validates: Requirements 4.3**
    - **Property 7: Pagination data accumulation**
    - **Validates: Requirements 9.5**
    - **Property 8: Pagination data preservation on failure**
    - **Validates: Requirements 9.4, 9.7**
    - **Property 9: Refresh data replacement**
    - **Validates: Requirements 10.4**
    - **Property 10: Refresh data preservation on failure**
    - **Validates: Requirements 10.7**
    - **Property 15: Retry request consistency**
    - **Validates: Requirements 7.7**
    - **Property 16: Error state clearing on new intent**
    - **Validates: Requirements 7.8**

- [x] 5. Phase 2: Create/Update SearchTripsFragment with lifecycle-aware collection
  - [x] 5.1 Create or update SearchTripsFragment with data binding
    - Create/update `app/src/main/java/com/delhivery/axle/ui/searchtrips/SearchTripsFragment.kt`
    - Set up ViewModel injection using Dagger
    - Initialize view binding
    - Set up RecyclerView with adapter
    - Set up SwipeRefreshLayout
    - _Requirements: 5.1, 6.1_

  - [x] 5.2 Implement lifecycle-aware StateFlow collection
    - Use viewLifecycleOwner.lifecycleScope for coroutine scope
    - Use repeatOnLifecycle(Lifecycle.State.STARTED) for state collection
    - Collect uiState StateFlow and call renderState function
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.7, 14.1, 14.2, 14.3_

  - [x] 5.3 Implement lifecycle-aware event collection
    - Collect events SharedFlow in separate coroutine
    - Handle ShowToast events with Toast
    - Handle ShowSnackbar events with Snackbar
    - Handle Navigate events with navigation
    - _Requirements: 8.4, 8.7_

  - [x] 5.4 Implement renderState function for UI updates
    - Handle UiState.Idle (show initial empty view or do nothing)
    - Handle UiState.Loading (show progress bar or set isRefreshing)
    - Handle UiState.Success (show RecyclerView with data, handle isLoadingMore)
    - Handle UiState.Empty (show empty state message)
    - Handle UiState.Error (show error view with retry button)
    - Use exhaustive when expression for all states
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8_

  - [x] 5.5 Implement user intent triggers
    - Set up search button click to trigger searchTrips intent
    - Set up SwipeRefreshLayout listener to trigger refresh intent
    - Set up RecyclerView scroll listener to trigger loadMore intent
    - Set up retry button click to trigger retry intent
    - _Requirements: 4.2, 9.2, 10.1_

  - [x] 5.6 Update SearchTripsAdapter for pagination support
    - Add footer view holder for loading more indicator
    - Implement setLoadingMore method to show/hide footer
    - Use DiffUtil for efficient list updates
    - _Requirements: 9.8, 15.6_

  - [ ]* 5.7 Write property tests for lifecycle and events
    - **Property 11: One-time event single consumption**
    - **Validates: Requirements 8.2**
    - **Property 12: One-time event no replay after config change**
    - **Validates: Requirements 8.3**
    - **Property 13: Lifecycle collection control**
    - **Validates: Requirements 5.2, 5.3**
    - **Property 14: State preservation across configuration changes**
    - **Validates: Requirements 5.7**
    - **Property 17: No memory leaks after destruction**
    - **Validates: Requirements 5.5, 14.1-14.8**

- [ ] 6. Checkpoint - Verify reference implementation
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. Phase 3: Verify and update layout resources (if needed)
  - [ ] 7.1 Verify existing fragment layout has required views
    - Verify SwipeRefreshLayout exists
    - Verify ProgressBar for initial loading exists
    - Verify RecyclerView for trip list exists
    - Add empty state view if not present
    - Add error state view with retry button if not present
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [ ] 7.2 Create footer layout for pagination loading indicator (if not exists)
    - Check if `layout/item_loading_footer.xml` exists
    - Create layout with ProgressBar for load more indicator if needed
    - _Requirements: 9.8_

  - [ ] 7.3 Verify string resources for error messages exist
    - Check if error message strings exist (network error, timeout, unauthorized, etc.)
    - Add missing strings for empty state messages if needed
    - Add missing strings for retry button text if needed
    - _Requirements: 7.2, 7.3, 7.4, 7.5_

- [ ] 8. Phase 3: Add testing dependencies and configuration
  - [ ] 8.1 Update app/build.gradle with testing dependencies
    - Add Mockk for mocking (testImplementation)
    - Add Coroutines Test for testing coroutines (testImplementation)
    - Add Turbine for Flow testing (testImplementation)
    - Add Kotest for property-based testing (testImplementation)
    - Add Truth for assertions (testImplementation)
    - Add LeakCanary for memory leak detection (debugImplementation)
    - _Requirements: 12.1, 12.2, 12.6, 14.8_

  - [ ] 8.2 Configure test runners for property-based tests
    - Update test configuration to support Kotest
    - Add JUnit 5 support if needed
    - _Requirements: 12.1_

- [ ] 9. Phase 4: Integration and documentation
  - [ ] 9.1 Wire SearchTripsFragment into navigation graph
    - Update navigation graph to include SearchTripsFragment
    - Add navigation actions from other screens if needed
    - Test navigation flow
    - _Requirements: 8.8_

  - [ ] 9.2 Update Dagger modules for dependency injection
    - Add SearchTripsViewModel to ViewModelModule
    - Ensure LoadCycleRepository is properly provided
    - Verify injection works correctly
    - _Requirements: 4.1_

  - [ ] 9.3 Add code documentation and migration guide
    - Document UiState pattern with KDoc comments
    - Document Flow conversion pattern with examples
    - Document lifecycle-aware collection pattern
    - Create inline comments explaining MVI flow
    - Add migration guide for other endpoints in code comments
    - _Requirements: 11.3, 11.4, 11.7, 13.8_

  - [ ] 9.4 Verify backward compatibility
    - Ensure existing RxJava methods still work
    - Test that other screens using RxJava are not affected
    - Verify no breaking changes to existing functionality
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

- [ ] 10. Final checkpoint - End-to-end verification
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at phase boundaries
- Property tests validate universal correctness properties from design document
- Unit tests validate specific examples and edge cases
- The implementation maintains backward compatibility with existing RxJava code
- SearchTripsViewModel serves as reference implementation for migrating other endpoints
- All coroutines use appropriate scopes (viewModelScope, viewLifecycleOwner.lifecycleScope)
- StateFlow is used for UI state (conflated, survives config changes)
- SharedFlow with replay=0 is used for one-time events (no replay after config changes)
- The 4-phase approach ensures incremental progress with validation at each stage
