# Requirements Document

## Introduction

This document defines requirements for migrating the Axle Android application from RxJava-based API state management to a modern Kotlin Flow-based architecture using MVI (Model-View-Intent) pattern. The migration focuses on the searchTrips endpoint as a reference implementation that can be replicated across other API endpoints. The solution provides clear UI state modeling, lifecycle-aware state collection, and robust error handling while maintaining testability and scalability.

## Glossary

- **Flow_Converter**: Component that converts RxJava Single responses to Kotlin Flow emissions
- **State_Manager**: ViewModel component that manages UI state using StateFlow
- **UI_State**: Sealed class representing all possible UI states (Loading, Success, Error, Empty)
- **Repository_Layer**: Data layer that handles API calls and Flow conversion
- **ViewModel_Layer**: Presentation logic layer that processes intents and emits UI states
- **UI_Layer**: Fragment/Activity that observes state and renders UI
- **MVI_Architecture**: Model-View-Intent architectural pattern for unidirectional data flow
- **One_Time_Event**: Events that should be consumed only once (navigation, toasts, snackbars)
- **Lifecycle_Collector**: Coroutine-based collector that respects Android lifecycle
- **SearchTrips_API**: POST /trips endpoint that searches for available trips
- **BaseResponse**: Generic API response wrapper containing data and metadata
- **SearchTripsResponse**: Response model containing trip list and pagination data

## Requirements

### Requirement 1: RxJava to Flow Conversion

**User Story:** As a developer, I want to convert RxJava Single API calls to Kotlin Flow, so that the codebase uses modern coroutine-based reactive programming.

#### Acceptance Criteria

1. THE Flow_Converter SHALL convert RxJava Single<BaseResponse<T>> to Flow<Resource<T>>
2. WHEN the RxJava Single emits a value, THE Flow_Converter SHALL emit Resource.Success with the response data
3. WHEN the RxJava Single emits an error, THE Flow_Converter SHALL emit Resource.Failure with appropriate ApiError
4. THE Flow_Converter SHALL execute on Dispatchers.IO to avoid blocking the main thread
5. THE Flow_Converter SHALL handle cancellation by respecting coroutine cancellation signals
6. THE Repository_Layer SHALL expose suspend functions that return Flow<Resource<T>>
7. THE Flow_Converter SHALL preserve all error information from the original RxJava error handling

### Requirement 2: UI State Model Definition

**User Story:** As a developer, I want a clear and type-safe UI state model, so that all possible UI states are explicitly handled and the UI remains consistent.

#### Acceptance Criteria

1. THE UI_State SHALL be implemented as a sealed class with exhaustive state variants
2. THE UI_State SHALL include a Loading state to indicate API operations in progress
3. THE UI_State SHALL include a Success state containing the response data
4. THE UI_State SHALL include an Error state containing error details and retry capability
5. THE UI_State SHALL include an Empty state for successful responses with no data
6. THE UI_State SHALL be generic to support reuse across different API endpoints
7. THE UI_State SHALL include metadata such as isRefreshing flag for pull-to-refresh scenarios
8. FOR ALL UI states, the sealed class SHALL enforce exhaustive when expressions in the UI layer

### Requirement 3: Repository Layer Flow Implementation

**User Story:** As a developer, I want the Repository layer to provide Flow-based API methods, so that ViewModels can collect reactive data streams.

#### Acceptance Criteria

1. THE Repository_Layer SHALL provide suspend functions that return Flow<Resource<T>>
2. WHEN searchTrips is called, THE LoadCycleRepository SHALL convert the RxJava Single to Flow
3. THE Repository_Layer SHALL emit Resource.Loading before starting the API call
4. WHEN the API call succeeds, THE Repository_Layer SHALL emit Resource.Success with data
5. WHEN the API call fails, THE Repository_Layer SHALL emit Resource.Failure with ApiError
6. THE Repository_Layer SHALL use the existing safeApiCallFlow method from BaseRepository
7. THE Repository_Layer SHALL maintain backward compatibility with existing RxJava methods during migration
8. THE Repository_Layer SHALL handle pagination parameters (offset, limit) in the request

### Requirement 4: ViewModel State Management with MVI

**User Story:** As a developer, I want the ViewModel to manage UI state using StateFlow with MVI pattern, so that state changes are predictable and testable.

#### Acceptance Criteria

1. THE State_Manager SHALL expose UI state as StateFlow<UI_State<T>>
2. THE State_Manager SHALL process user intents through explicit intent handler functions
3. WHEN an intent is received, THE State_Manager SHALL update the StateFlow with appropriate state transitions
4. THE State_Manager SHALL use viewModelScope for launching coroutines
5. THE State_Manager SHALL collect Flow emissions from Repository and map them to UI states
6. THE State_Manager SHALL maintain a single source of truth for UI state
7. THE State_Manager SHALL handle pagination state separately from main data state
8. THE State_Manager SHALL expose StateFlow as read-only to prevent external state mutations

### Requirement 5: Lifecycle-Aware State Collection

**User Story:** As a developer, I want the UI to collect state in a lifecycle-aware manner, so that state updates are only processed when the UI is visible and resources are not wasted.

#### Acceptance Criteria

1. THE UI_Layer SHALL use repeatOnLifecycle(Lifecycle.State.STARTED) for state collection
2. THE Lifecycle_Collector SHALL automatically cancel collection when the lifecycle moves below STARTED
3. THE Lifecycle_Collector SHALL automatically resume collection when the lifecycle returns to STARTED
4. THE UI_Layer SHALL collect StateFlow using the collect or collectLatest operator
5. WHEN the Fragment is destroyed, THE Lifecycle_Collector SHALL cancel all active collections
6. THE UI_Layer SHALL not use lifecycleScope.launch directly without lifecycle awareness
7. THE UI_Layer SHALL handle configuration changes without losing state or causing memory leaks

### Requirement 6: UI State Rendering

**User Story:** As a user, I want the UI to accurately reflect the current state of data loading, so that I understand what the app is doing and can take appropriate actions.

#### Acceptance Criteria

1. WHEN UI_State is Loading, THE UI_Layer SHALL display a progress indicator
2. WHEN UI_State is Success with data, THE UI_Layer SHALL display the data in the RecyclerView
3. WHEN UI_State is Success with empty data, THE UI_Layer SHALL display an empty state message
4. WHEN UI_State is Error, THE UI_Layer SHALL display an error message with retry option
5. WHEN UI_State is Error with network failure, THE UI_Layer SHALL display a network-specific error message
6. THE UI_Layer SHALL use exhaustive when expressions to handle all UI_State variants
7. THE UI_Layer SHALL update the SwipeRefreshLayout state based on isRefreshing flag
8. THE UI_Layer SHALL hide the progress indicator when transitioning from Loading to any other state

### Requirement 7: Error Handling and Recovery

**User Story:** As a user, I want clear error messages and the ability to retry failed operations, so that I can recover from errors without restarting the app.

#### Acceptance Criteria

1. WHEN an API call fails, THE State_Manager SHALL emit Error state with ApiError details
2. THE UI_Layer SHALL display user-friendly error messages based on ApiError type
3. WHEN ApiError is Network, THE UI_Layer SHALL display "No internet connection" message
4. WHEN ApiError is Timeout, THE UI_Layer SHALL display "Request timed out" message
5. WHEN ApiError is Unauthorized, THE UI_Layer SHALL display "Session expired" message
6. THE UI_Layer SHALL provide a retry button in the error state
7. WHEN the user taps retry, THE UI_Layer SHALL trigger the same intent that caused the error
8. THE State_Manager SHALL clear error state when a new intent is processed

### Requirement 8: One-Time Event Handling

**User Story:** As a developer, I want one-time events like navigation and toasts to be consumed only once, so that configuration changes don't trigger duplicate events.

#### Acceptance Criteria

1. THE State_Manager SHALL expose one-time events using SharedFlow with replay=0
2. THE One_Time_Event SHALL be consumed only once per emission
3. WHEN a configuration change occurs, THE One_Time_Event SHALL not be re-emitted
4. THE UI_Layer SHALL collect one-time events separately from UI state
5. THE State_Manager SHALL emit navigation events as one-time events
6. THE State_Manager SHALL emit toast/snackbar messages as one-time events
7. THE UI_Layer SHALL handle one-time events in a lifecycle-aware manner
8. THE One_Time_Event SHALL support different event types (Navigation, ShowToast, ShowSnackbar)

### Requirement 9: Pagination Support

**User Story:** As a user, I want to load more trips as I scroll, so that I can browse through large lists without waiting for all data to load at once.

#### Acceptance Criteria

1. THE State_Manager SHALL maintain pagination state (offset, hasMore, isLoadingMore)
2. WHEN the user scrolls to the bottom, THE UI_Layer SHALL trigger a loadMore intent
3. WHEN loading more data, THE State_Manager SHALL set isLoadingMore to true
4. WHEN loading more data, THE State_Manager SHALL not replace existing data with Loading state
5. WHEN more data is loaded successfully, THE State_Manager SHALL append new items to existing data
6. WHEN hasMore is false, THE UI_Layer SHALL not trigger additional loadMore intents
7. WHEN loading more data fails, THE State_Manager SHALL preserve existing data and show error
8. THE UI_Layer SHALL display a footer progress indicator when isLoadingMore is true

### Requirement 10: Pull-to-Refresh Support

**User Story:** As a user, I want to pull down to refresh the trip list, so that I can manually check for new trips.

#### Acceptance Criteria

1. WHEN the user pulls to refresh, THE UI_Layer SHALL trigger a refresh intent
2. WHEN refreshing, THE State_Manager SHALL set isRefreshing to true in the UI state
3. WHEN refreshing, THE State_Manager SHALL reset pagination state (offset=0)
4. WHEN refresh completes successfully, THE State_Manager SHALL replace existing data with new data
5. WHEN refresh completes, THE State_Manager SHALL set isRefreshing to false
6. THE UI_Layer SHALL update SwipeRefreshLayout.isRefreshing based on state.isRefreshing
7. WHEN refresh fails, THE State_Manager SHALL preserve existing data and show error in snackbar
8. THE UI_Layer SHALL not show full-screen error during refresh if data already exists

### Requirement 11: Reusability and Scalability

**User Story:** As a developer, I want the Flow-based architecture to be easily reusable for other API endpoints, so that I can migrate the entire codebase efficiently.

#### Acceptance Criteria

1. THE UI_State SHALL be generic and reusable across different data types
2. THE Flow_Converter SHALL be implemented as an extension function on Single<T>
3. THE State_Manager pattern SHALL be documented with clear examples
4. THE Repository_Layer pattern SHALL be consistent across all repositories
5. THE UI_Layer collection pattern SHALL be extractable into a reusable base class or extension
6. THE MVI_Architecture SHALL support both simple and complex API scenarios
7. THE error handling pattern SHALL be consistent across all ViewModels
8. THE codebase SHALL include a reference implementation for searchTrips that others can follow

### Requirement 12: Testing Support

**User Story:** As a developer, I want the Flow-based architecture to be easily testable, so that I can write unit tests for ViewModels and integration tests for repositories.

#### Acceptance Criteria

1. THE State_Manager SHALL be testable using Turbine or similar Flow testing libraries
2. THE Repository_Layer SHALL be testable by mocking the service layer
3. THE UI_State transitions SHALL be verifiable in unit tests
4. THE State_Manager SHALL not depend on Android framework classes except ViewModel
5. THE Flow_Converter SHALL be testable independently of the Repository
6. THE ViewModel tests SHALL use TestCoroutineDispatcher for deterministic testing
7. THE Repository tests SHALL verify correct Resource emissions for success and error cases
8. THE UI_State SHALL be easily assertable in tests due to its sealed class structure

### Requirement 13: Backward Compatibility During Migration

**User Story:** As a developer, I want to maintain backward compatibility during migration, so that the app remains functional while migrating endpoints incrementally.

#### Acceptance Criteria

1. THE Repository_Layer SHALL maintain existing RxJava methods alongside new Flow methods
2. THE LoadCycleRepository SHALL provide both searchTrips() and searchTripsFlow() methods
3. THE existing ViewModels SHALL continue to work with RxJava methods during migration
4. THE new Flow-based implementation SHALL not break existing functionality
5. THE migration SHALL be incremental, allowing one endpoint at a time to be converted
6. THE BaseRepository SHALL support both RxJava and Flow patterns simultaneously
7. WHEN all endpoints are migrated, THE RxJava methods SHALL be marked as deprecated
8. THE migration documentation SHALL provide clear before/after examples

### Requirement 14: Memory Leak Prevention

**User Story:** As a developer, I want the Flow-based architecture to prevent memory leaks, so that the app remains performant and stable over time.

#### Acceptance Criteria

1. THE State_Manager SHALL use viewModelScope for all coroutine launches
2. THE UI_Layer SHALL use viewLifecycleOwner.lifecycleScope for all collections
3. THE Lifecycle_Collector SHALL automatically cancel when the lifecycle is destroyed
4. THE State_Manager SHALL not hold references to Activity or Fragment instances
5. THE UI_Layer SHALL not create coroutines that outlive the view lifecycle
6. THE StateFlow SHALL not cause memory leaks due to its conflated nature
7. THE SharedFlow for one-time events SHALL use replay=0 to prevent memory accumulation
8. THE codebase SHALL be verifiable with LeakCanary to detect any memory leaks

### Requirement 15: Performance Optimization

**User Story:** As a user, I want the app to load and display trip data quickly, so that I can browse trips without delays.

#### Acceptance Criteria

1. THE Flow_Converter SHALL execute API calls on Dispatchers.IO
2. THE State_Manager SHALL use StateFlow which is conflated to avoid unnecessary UI updates
3. THE UI_Layer SHALL use collectLatest for data that can be superseded by newer emissions
4. THE Repository_Layer SHALL not perform unnecessary data transformations
5. THE UI_State SHALL use data classes with structural equality for efficient comparison
6. THE RecyclerView adapter SHALL use DiffUtil for efficient list updates
7. THE State_Manager SHALL debounce rapid intent emissions where appropriate
8. THE codebase SHALL avoid blocking the main thread during state transitions

