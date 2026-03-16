# Requirements Document

## Introduction

This document specifies the requirements for migrating HomeLoadsViewModel from LiveData with pure coroutines to Kotlin Flow APIs (StateFlow, SharedFlow). The migration modernizes the reactive data handling in the loads screen, improving lifecycle awareness, reducing boilerplate, and enabling better state management patterns. The migration covers the ViewModel, Fragment, and Repository layers while maintaining backward compatibility with existing RxJava-based code paths.

## Glossary

- **HomeLoadsViewModel**: The ViewModel class managing UI state and business logic for the loads screen
- **HomeLoadsFragment**: The Fragment class displaying loads data and observing ViewModel state
- **TransactionsRepository**: Repository providing API calls for fetching load transactions
- **StateFlow**: A hot Flow that emits the current and new state updates to collectors, replacing LiveData for UI state
- **SharedFlow**: A hot Flow for one-time events that should not be replayed to new collectors
- **MutableStateFlow**: A mutable version of StateFlow that allows emitting new values
- **MutableSharedFlow**: A mutable version of SharedFlow for emitting one-time events
- **Resource**: A sealed class representing Loading, Success, and Failure states for API operations
- **repeatOnLifecycle**: AndroidX lifecycle extension for safely collecting flows in lifecycle-aware manner
- **flowOn**: Flow operator to change the dispatcher for upstream operations
- **stateIn**: Flow operator to convert a cold Flow to a hot StateFlow
- **UiState**: A data class representing the complete UI state for the loads screen
- **UiEvent**: A sealed class representing one-time UI events like navigation, toasts, and errors

## Requirements

### Requirement 1: Define UI State Data Class

**User Story:** As a developer, I want a single data class representing all UI state, so that state management is centralized and predictable.

#### Acceptance Criteria

1. THE HomeLoadsViewModel SHALL define a HomeLoadsUiState data class containing all UI state properties currently spread across multiple LiveData fields
2. THE HomeLoadsUiState SHALL include properties for loads list, loading state, filter state, counts, pagination state, and error state
3. THE HomeLoadsUiState SHALL have sensible default values for all properties to represent the initial state
4. THE HomeLoadsUiState SHALL be immutable (data class with val properties only)

### Requirement 2: Define UI Events Sealed Class

**User Story:** As a developer, I want a sealed class for one-time UI events, so that events like navigation and toasts are not replayed on configuration changes.

#### Acceptance Criteria

1. THE HomeLoadsViewModel SHALL define a HomeLoadsUiEvent sealed class for one-time events
2. THE HomeLoadsUiEvent SHALL include subclasses for navigation events, toast messages, dialog triggers, and error notifications
3. THE HomeLoadsUiEvent SHALL include events for: ShowBidDialog, ShowSuccessDialog, ShowErrorToast, NavigateToBidDetails, ShowKycDialog, ShowAddTruckDialog
4. WHEN a UI event is emitted, THE HomeLoadsFragment SHALL consume it exactly once

### Requirement 3: Replace LiveData with StateFlow for UI State

**User Story:** As a developer, I want to use StateFlow for UI state, so that I get better null-safety and lifecycle handling.

#### Acceptance Criteria

1. THE HomeLoadsViewModel SHALL expose a StateFlow<HomeLoadsUiState> property named uiState
2. THE HomeLoadsViewModel SHALL use a private MutableStateFlow<HomeLoadsUiState> for internal state updates
3. WHEN state changes occur, THE HomeLoadsViewModel SHALL update state using copy() to maintain immutability
4. THE StateFlow SHALL be initialized with a default HomeLoadsUiState representing the initial loading state
5. THE HomeLoadsViewModel SHALL remove the following LiveData properties and migrate them to uiState: userLoadsData, userLoadsDataFetch, routesLiveData, loadsCountLiveData, fullLoadsCountLiveData, dataLoadingLiveData

### Requirement 4: Replace LiveData with SharedFlow for One-Time Events

**User Story:** As a developer, I want to use SharedFlow for one-time events, so that events are not replayed on configuration changes.

#### Acceptance Criteria

1. THE HomeLoadsViewModel SHALL expose a SharedFlow<HomeLoadsUiEvent> property named uiEvent
2. THE HomeLoadsViewModel SHALL use a private MutableSharedFlow<HomeLoadsUiEvent> for emitting events
3. THE SharedFlow SHALL be configured with replay = 0 to prevent event replay
4. THE HomeLoadsViewModel SHALL remove the following LiveData properties and migrate them to uiEvent: bidsActionLiveData, bulkBidActionLiveData, acceptBidLiveData, lowestBidLiveData, reviseBidLiveData, truckGetLiveData, editBulkLiveData
5. THE HomeLoadsViewModel SHALL remove the following tracking LiveData and migrate to uiEvent: intercityListShownTracked, intracityListShownTracked, marketPlaceListShownTracked

### Requirement 5: Update Fragment to Collect Flows

**User Story:** As a developer, I want the Fragment to collect flows lifecycle-aware, so that collection stops when the Fragment is not visible.

#### Acceptance Criteria

1. THE HomeLoadsFragment SHALL collect uiState using repeatOnLifecycle(Lifecycle.State.STARTED)
2. THE HomeLoadsFragment SHALL collect uiEvent using repeatOnLifecycle(Lifecycle.State.STARTED)
3. THE HomeLoadsFragment SHALL launch flow collection in viewLifecycleOwner.lifecycleScope
4. THE HomeLoadsFragment SHALL remove all LiveData observe() calls and replace with flow collection
5. WHEN the Fragment is stopped, THE flow collection SHALL automatically pause
6. WHEN the Fragment is started again, THE flow collection SHALL automatically resume

### Requirement 6: Migrate Repository Methods to Return Flow

**User Story:** As a developer, I want repository methods to return Flow, so that I can leverage flow operators for data transformation.

#### Acceptance Criteria

1. THE TransactionsRepository SHALL provide Flow-returning versions of API methods alongside existing suspend functions
2. THE Flow-returning methods SHALL emit Resource.Loading before making the API call
3. THE Flow-returning methods SHALL emit Resource.Success or Resource.Failure based on API response
4. THE Flow-returning methods SHALL use flowOn(Dispatchers.IO) for network operations
5. THE BaseRepository SHALL provide a safeApiCallFlow helper function that wraps API calls in Flow with proper error handling

### Requirement 7: Implement State Update Helper Functions

**User Story:** As a developer, I want helper functions for common state updates, so that state mutations are consistent and readable.

#### Acceptance Criteria

1. THE HomeLoadsViewModel SHALL provide a private updateState function that takes a lambda (HomeLoadsUiState) -> HomeLoadsUiState
2. THE updateState function SHALL use _uiState.update { } for atomic state updates
3. THE HomeLoadsViewModel SHALL provide a private emitEvent suspend function for emitting UI events
4. WHEN multiple state updates occur rapidly, THE StateFlow SHALL coalesce updates to prevent UI thrashing

### Requirement 8: Maintain Progress LiveData for Legacy Dialog Integration

**User Story:** As a developer, I want to keep progressLiveData as LiveData temporarily, so that existing progress dialog integration continues to work.

#### Acceptance Criteria

1. THE HomeLoadsViewModel SHALL retain progressLiveData as MutableLiveData<Boolean> for backward compatibility
2. THE HomeLoadsFragment SHALL continue to observe progressLiveData using the existing ProgressObserver
3. IF the progress dialog utility is migrated to Flow in the future, THEN THE progressLiveData SHALL be migrated to StateFlow

### Requirement 9: Handle Pagination State in UI State

**User Story:** As a developer, I want pagination state managed in the UI state, so that pagination behavior is predictable and testable.

#### Acceptance Criteria

1. THE HomeLoadsUiState SHALL include properties for: hasMoreData, isLoadingMore, offset, total, searchAfter
2. WHEN pagination is triggered, THE HomeLoadsViewModel SHALL update isLoadingMore to true
3. WHEN pagination completes, THE HomeLoadsViewModel SHALL update hasMoreData and offset appropriately
4. THE HomeLoadsFragment SHALL use uiState.hasMoreData and uiState.isLoadingMore for pagination decisions

### Requirement 10: Handle Filter State in UI State

**User Story:** As a developer, I want filter state managed in the UI state, so that filter changes trigger appropriate UI updates.

#### Acceptance Criteria

1. THE HomeLoadsUiState SHALL include properties for: selectedFilter, vehicleTypes, filterVehicleType
2. WHEN filter changes, THE HomeLoadsViewModel SHALL update the filter state and trigger data refresh
3. THE HomeLoadsFragment SHALL observe filter state changes and update UI accordingly

### Requirement 11: Handle Tab Counts in UI State

**User Story:** As a developer, I want tab counts managed in the UI state, so that count badges update reactively.

#### Acceptance Criteria

1. THE HomeLoadsUiState SHALL include properties for: intracityCount, intercityCount, nonDlvCount, marketplaceCount
2. WHEN counts are fetched, THE HomeLoadsViewModel SHALL update all count properties atomically
3. THE HomeLoadsFragment SHALL observe count changes and update tab badges accordingly

### Requirement 12: Preserve Coroutine Job Cancellation Behavior

**User Story:** As a developer, I want rapid tab switching to cancel previous fetches, so that stale data does not overwrite fresh data.

#### Acceptance Criteria

1. THE HomeLoadsViewModel SHALL retain the currentFetchJob property for tracking active fetch operations
2. WHEN a new fetch is initiated, THE HomeLoadsViewModel SHALL cancel any existing currentFetchJob
3. THE HomeLoadsViewModel SHALL handle CancellationException gracefully without logging errors
4. WHEN a fetch is cancelled, THE UI state SHALL not be updated with partial or stale data

### Requirement 13: Migrate Analytics Tracking Events

**User Story:** As a developer, I want analytics tracking to work with the new Flow-based architecture, so that user behavior is still tracked.

#### Acceptance Criteria

1. WHEN intracityListShownTracked event is needed, THE HomeLoadsViewModel SHALL emit a TrackIntracityListShown event via uiEvent
2. WHEN intercityListShownTracked event is needed, THE HomeLoadsViewModel SHALL emit a TrackIntercityListShown event via uiEvent
3. WHEN marketPlaceListShownTracked event is needed, THE HomeLoadsViewModel SHALL emit a TrackMarketplaceListShown event via uiEvent
4. THE HomeLoadsFragment SHALL handle tracking events by calling analyticsUtil methods

### Requirement 14: Ensure Thread Safety for State Updates

**User Story:** As a developer, I want state updates to be thread-safe, so that concurrent updates do not cause race conditions.

#### Acceptance Criteria

1. THE HomeLoadsViewModel SHALL use MutableStateFlow.update { } for all state mutations
2. THE HomeLoadsViewModel SHALL NOT directly assign to _uiState.value from multiple coroutines
3. WHEN parallel API calls complete, THE HomeLoadsViewModel SHALL merge results atomically using update { }

### Requirement 15: Add Unit Test Support for Flow-Based ViewModel

**User Story:** As a developer, I want the Flow-based ViewModel to be testable, so that I can write unit tests for state transitions.

#### Acceptance Criteria

1. THE HomeLoadsViewModel SHALL expose uiState as StateFlow (not MutableStateFlow) for encapsulation
2. THE HomeLoadsViewModel SHALL expose uiEvent as SharedFlow (not MutableSharedFlow) for encapsulation
3. THE HomeLoadsUiState data class SHALL support equality comparison for test assertions
4. THE HomeLoadsUiEvent sealed class SHALL support equality comparison for test assertions
