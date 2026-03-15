# Requirements Document

## Introduction

This feature introduces a standardized approach for handling API calls using Kotlin Coroutines with a sealed class-based UI state management pattern. The implementation will replace the existing RxJava-based asynchronous operations with coroutines, providing a cleaner, more readable approach to handling Loading, Success, and Error states. A new BaseRepository abstract class will provide centralized error handling with typed ApiError categories. ViewModels will expose LiveData with these states, enabling reactive UI updates following the existing MVVM architecture.

## Glossary

- **Resource**: A sealed class representing the result of an API operation (Success or Failure)
- **ApiError**: An enum class categorizing different types of API errors (Timeout, Network, Unauthorized, etc.)
- **BaseRepository**: An abstract class providing safe API call wrapper with comprehensive error handling
- **ErrorLogger**: An interface for logging errors, injectable for flexibility
- **Repository**: A class extending BaseRepository, responsible for making API calls and returning Resource wrapped results
- **ViewModel**: A class that holds UI-related data and exposes LiveData for UI observation
- **Coroutine_Scope**: The scope in which coroutines are launched, typically viewModelScope for ViewModels
- **LiveData**: An observable data holder class that respects the lifecycle of app components
- **Retrofit_Service**: Interface defining API endpoints using Retrofit annotations
- **Suspend_Function**: A function that can be paused and resumed, used for coroutine-based operations
- **Deferred**: A non-blocking cancellable future representing a coroutine result, used with async for parallel execution
- **async-await**: A pattern for launching parallel coroutines where async returns Deferred and await retrieves the result

## Requirements

### Requirement 1: Resource Sealed Class Definition

**User Story:** As a developer, I want a sealed class that represents API call results, so that I can handle Success and Failure states in a type-safe manner with detailed error information.

#### Acceptance Criteria

1. THE Resource SHALL be defined as a sealed class with two subclasses: Success and Failure
2. THE Success subclass SHALL contain a generic type parameter to hold the response data
3. THE Failure subclass SHALL contain isNetworkError boolean, optional errorCode integer, and ApiError enum
4. THE Resource SHALL be placed in the api/repository package alongside BaseRepository
5. THE Resource SHALL support nullable data in Success for API calls that return no body

### Requirement 2: ApiError Enum Definition

**User Story:** As a developer, I want an enum that categorizes different API error types, so that I can handle specific error scenarios appropriately in the UI.

#### Acceptance Criteria

1. THE ApiError SHALL be defined as an enum class with the following values: Timeout, Network, Unauthorized, AccessDenied, NotFound, ServiceUnavailable, Unknown
2. THE ApiError.Timeout SHALL represent SocketTimeoutException scenarios
3. THE ApiError.Network SHALL represent IOException/connectivity issues
4. THE ApiError.Unauthorized SHALL represent HTTP 401 responses
5. THE ApiError.AccessDenied SHALL represent HTTP 403 responses
6. THE ApiError.NotFound SHALL represent HTTP 404 responses
7. THE ApiError.ServiceUnavailable SHALL represent HTTP 503 responses
8. THE ApiError.Unknown SHALL represent all other unhandled error scenarios
9. THE ApiError SHALL be placed in the api/repository package for reusability

### Requirement 3: BaseRepository Abstract Class

**User Story:** As a developer, I want an abstract BaseRepository class with a safeApiCall function, so that all repositories inherit consistent error handling without code duplication.

#### Acceptance Criteria

1. THE BaseRepository SHALL be an abstract class that accepts an ErrorLogger dependency via constructor
2. THE BaseRepository SHALL provide a suspend function safeApiCall that accepts a suspend lambda and returns Resource<T>
3. WHEN the API call succeeds, THE safeApiCall SHALL return Resource.Success with the response data
4. WHEN a CancellationException occurs, THE safeApiCall SHALL rethrow it to respect coroutine cancellation
5. WHEN a SocketTimeoutException occurs, THE safeApiCall SHALL log the error and return Resource.Failure with ApiError.Timeout
6. WHEN an IOException occurs, THE safeApiCall SHALL return Resource.Failure with isNetworkError=true and ApiError.Network
7. WHEN an HttpException occurs, THE safeApiCall SHALL map the HTTP code to appropriate ApiError and return Resource.Failure
8. WHEN any other Exception occurs, THE safeApiCall SHALL log the error and return Resource.Failure with ApiError.Unknown
9. THE BaseRepository SHALL be placed in the api/repository package

### Requirement 4: ErrorLogger Interface

**User Story:** As a developer, I want an ErrorLogger interface for logging errors, so that I can inject different implementations for production and testing.

#### Acceptance Criteria

1. THE ErrorLogger SHALL be defined as an interface with a log(exception: Exception) function
2. THE ErrorLogger implementation SHALL be injectable via Dagger
3. THE production ErrorLogger SHALL log errors to Crashlytics or appropriate logging service
4. THE ErrorLogger SHALL be placed in the utils package for reusability

### Requirement 5: Coroutine-Based Repository Implementation

**User Story:** As a developer, I want repositories to extend BaseRepository and make API calls using coroutines, so that I can handle asynchronous operations cleanly with inherited error handling.

#### Acceptance Criteria

1. THE Repository SHALL extend BaseRepository and call super constructor with ErrorLogger
2. THE Repository SHALL define suspend functions for API calls that use safeApiCall wrapper
3. THE Repository SHALL support both new coroutine-based calls and existing RxJava calls during migration
4. THE Repository SHALL inject the IO dispatcher for API calls via Dagger

### Requirement 6: ViewModel LiveData Exposure

**User Story:** As a developer, I want ViewModels to expose LiveData with Resource states, so that Activities/Fragments can observe and react to state changes.

#### Acceptance Criteria

1. THE ViewModel SHALL expose LiveData of type Resource for each API operation
2. THE ViewModel SHALL use viewModelScope to launch coroutines for API calls
3. THE ViewModel SHALL provide a separate loading state LiveData or combine with Resource
4. WHEN an API call is initiated, THE ViewModel SHALL emit loading state
5. WHEN an API call completes, THE ViewModel SHALL emit the Resource result to LiveData
6. THE ViewModel SHALL extend the existing BaseViewModel to maintain compatibility with current architecture

### Requirement 7: UI State Observation and Rendering

**User Story:** As a developer, I want Activities/Fragments to observe LiveData and update UI based on Resource states, so that users see appropriate feedback during API operations.

#### Acceptance Criteria

1. WHEN loading state is observed, THE Activity/Fragment SHALL display a loading indicator
2. WHEN Resource.Success is observed, THE Activity/Fragment SHALL hide the loading indicator and display the data
3. WHEN Resource.Failure is observed, THE Activity/Fragment SHALL hide the loading indicator and display appropriate error message based on ApiError type
4. THE Activity/Fragment SHALL use a when expression to handle all ApiError types exhaustively
5. THE Activity/Fragment SHALL use lifecycle-aware observation to prevent memory leaks
6. THE Activity/Fragment SHALL show user-friendly error messages mapped from ApiError enum

### Requirement 8: Retrofit Service Coroutine Support

**User Story:** As a developer, I want Retrofit service interfaces to support suspend functions, so that API calls can be made using coroutines.

#### Acceptance Criteria

1. THE Retrofit_Service interface SHALL define API endpoints as suspend functions returning Response objects or direct response types
2. THE Retrofit_Service SHALL maintain backward compatibility with existing RxJava-based endpoints during migration
3. WHEN a new API endpoint is added, THE Retrofit_Service SHALL use suspend function syntax
4. THE Retrofit_Service SHALL use Response wrapper when access to error bodies is needed

### Requirement 9: Dagger Integration for Coroutine Dispatchers

**User Story:** As a developer, I want coroutine dispatchers to be injectable via Dagger, so that I can easily test ViewModels and Repositories with test dispatchers.

#### Acceptance Criteria

1. THE Dagger module SHALL provide CoroutineDispatcher instances for IO and Main dispatchers
2. THE Repository SHALL inject the IO dispatcher for API calls
3. THE ViewModel SHALL inject the Main dispatcher for LiveData updates if needed
4. THE dispatchers SHALL be qualified with annotations to distinguish between IO and Main
5. THE ErrorLogger implementation SHALL be provided via Dagger module

### Requirement 10: Parallel Network Calls with Async-Await Utility Functions

**User Story:** As a developer, I want utility functions to execute 2 or 3 API calls in parallel using async-await pattern with a transform lambda, so that I can reduce total loading time and combine results into a single data model.

#### Acceptance Criteria

1. THE BaseRepository SHALL provide a suspend function parallelApiCall2 that accepts two suspend lambdas and a transform function
2. THE parallelApiCall2 SHALL launch both API calls concurrently using async within coroutineScope
3. THE parallelApiCall2 SHALL await both results and apply the transform function to combine them
4. THE parallelApiCall2 SHALL return Resource.Success with transformed data when both calls succeed
5. THE parallelApiCall2 SHALL return Resource.Error with the exception when any call fails
6. THE BaseRepository SHALL provide a suspend function parallelApiCall3 that accepts three suspend lambdas and a transform function
7. THE parallelApiCall3 SHALL launch all three API calls concurrently using async within coroutineScope
8. THE parallelApiCall3 SHALL await all results and apply the transform function to combine them
9. THE parallelApiCall3 SHALL return Resource.Success with transformed data when all calls succeed
10. THE parallelApiCall3 SHALL return Resource.Error with the exception when any call fails
11. THE Repository SHALL use parallelApiCall2 or parallelApiCall3 to fetch multiple independent data sources and combine into a single data class
12. THE ViewModel SHALL call repository parallel methods and handle Resource.Success and Resource.Error using when expression
13. WHEN Resource.Success is received, THE ViewModel SHALL update state with the combined data
14. WHEN Resource.Error is received, THE ViewModel SHALL show error to the user

#### Usage Example

```kotlin
// Repository usage
suspend fun getHomeData(): Resource<HomeData> {
    return parallelApiCall3(
        call1 = { api.getUser() },
        call2 = { api.getWallet() },
        call3 = { api.getOrders() }
    ) { user, wallet, orders ->
        HomeData(user = user, wallet = wallet, orders = orders)
    }
}

// ViewModel usage
viewModelScope.launch {
    when (val result = repository.getHomeData()) {
        is Resource.Success -> _state.value = result.data
        is Resource.Error -> showError(result.throwable)
    }
}
```
