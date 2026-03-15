# Implementation Plan: Coroutines API State Management

## Overview

This implementation plan introduces a standardized coroutine-based API state management pattern for the Axle Android application. The implementation creates core components (Resource, ApiError, BaseRepository, ErrorLogger) with Dagger integration and provides patterns for ViewModel and UI layer adoption.

## Tasks

- [x] 1. Set up core data models and error types
  - [x] 1.1 Create ApiError enum in api/repository package
    - Define enum values: Timeout, Network, Unauthorized, AccessDenied, NotFound, ServiceUnavailable, Unknown
    - Add KDoc comments for each value
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9_

  - [x] 1.2 Create Resource sealed class in api/repository package
    - Define Success data class with generic type parameter and nullable data
    - Define Failure data class with isNetworkError, errorCode, and apiError fields
    - Add KDoc comments
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [ ]* 1.3 Write property test for Resource sealed class structure
    - **Property 1: Resource Sealed Class Structure Invariants**
    - **Validates: Requirements 1.1, 1.2, 1.3, 1.5**

- [x] 2. Implement ErrorLogger interface and Crashlytics implementation
  - [x] 2.1 Create ErrorLogger interface in utils package
    - Define log(exception: Exception) function
    - Add KDoc comments
    - _Requirements: 4.1, 4.4_

  - [x] 2.2 Create CrashlyticsErrorLogger implementation in utils package
    - Implement ErrorLogger interface
    - Log exceptions to Firebase Crashlytics
    - _Requirements: 4.3_

- [x] 3. Create Dagger module for coroutine dispatchers
  - [x] 3.1 Create IoDispatcher qualifier annotation in injection/qualifier package
    - Define @Qualifier annotation with BINARY retention
    - _Requirements: 9.4_

  - [x] 3.2 Create MainDispatcher qualifier annotation in injection/qualifier package
    - Define @Qualifier annotation with BINARY retention
    - _Requirements: 9.4_

  - [x] 3.3 Create CoroutineModule in injection/module package
    - Provide IO dispatcher with @IoDispatcher qualifier
    - Provide Main dispatcher with @MainDispatcher qualifier
    - Provide ErrorLogger implementation (CrashlyticsErrorLogger)
    - Use @Singleton scope
    - _Requirements: 9.1, 9.2, 9.3, 9.5_

  - [x] 3.4 Register CoroutineModule in AppComponent
    - Add CoroutineModule to the modules list in AppComponent
    - _Requirements: 9.1_

- [x] 4. Checkpoint - Verify Dagger setup compiles
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Implement BaseRepository with safeApiCall
  - [x] 5.1 Create BaseRepository abstract class in api/repository package
    - Accept ErrorLogger via constructor
    - Implement safeApiCall suspend function with exception handling
    - Map CancellationException to rethrow
    - Map SocketTimeoutException to ApiError.Timeout
    - Map IOException to ApiError.Network with isNetworkError=true
    - Map HttpException to appropriate ApiError based on status code
    - Map other exceptions to ApiError.Unknown
    - Add private mapHttpCodeToApiError helper function
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9_

  - [ ]* 5.2 Write property test for safeApiCall exception mapping
    - **Property 2: safeApiCall Exception-to-ApiError Mapping**
    - **Validates: Requirements 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 2.8**

  - [ ]* 5.3 Write property test for HTTP status code mapping
    - **Property 3: HTTP Status Code to ApiError Mapping**
    - **Validates: Requirements 2.4, 2.5, 2.6, 2.7, 2.8**

- [x] 6. Implement parallel API call utilities
  - [x] 6.1 Add parallelApiCall2 to BaseRepository
    - Accept two suspend lambdas and transform function
    - Launch both calls concurrently using async within coroutineScope
    - Await both results and apply transform
    - Return Resource.Success on success, Resource.Failure on any exception
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

  - [x] 6.2 Add parallelApiCall3 to BaseRepository
    - Accept three suspend lambdas and transform function
    - Launch all three calls concurrently using async within coroutineScope
    - Await all results and apply transform
    - Return Resource.Success on success, Resource.Failure on any exception
    - _Requirements: 10.6, 10.7, 10.8, 10.9, 10.10_

  - [ ]* 6.3 Write property test for parallelApiCall2 correctness
    - **Property 4: parallelApiCall2 Correctness**
    - **Validates: Requirements 10.2, 10.3, 10.4, 10.5**

  - [ ]* 6.4 Write property test for parallelApiCall3 correctness
    - **Property 5: parallelApiCall3 Correctness**
    - **Validates: Requirements 10.7, 10.8, 10.9, 10.10**

  - [ ]* 6.5 Write property test for transform function application
    - **Property 7: Transform Function Application**
    - **Validates: Requirements 10.3, 10.8**

- [x] 7. Checkpoint - Verify BaseRepository implementation
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Create example repository implementation pattern
  - [x] 8.1 Create ExampleRepository demonstrating the pattern
    - Extend BaseRepository with ErrorLogger injection
    - Inject IO dispatcher with @IoDispatcher qualifier
    - Demonstrate safeApiCall usage with withContext(ioDispatcher)
    - Demonstrate parallelApiCall2 usage for combined data fetching
    - Add comments explaining the pattern for migration reference
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 9. Create example ViewModel pattern
  - [x] 9.1 Create ExampleViewModel demonstrating the pattern
    - Expose LiveData of type Resource for API operations
    - Expose separate loading state LiveData
    - Use viewModelScope.launch for coroutine launching
    - Emit loading state before API call
    - Emit Resource result after API call completes
    - Set loading to false after Resource emission
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 10.12, 10.13, 10.14_

  - [ ]* 9.2 Write property test for ViewModel state lifecycle
    - **Property 6: ViewModel State Lifecycle**
    - **Validates: Requirements 6.4, 6.5, 10.13**

- [x] 10. Create UI observation utilities and patterns
  - [x] 10.1 Create ResourceExtensions.kt with helper extension functions
    - Add extension function for exhaustive when handling
    - Add helper for mapping ApiError to user-friendly messages
    - _Requirements: 7.4, 7.6_

  - [x] 10.2 Document UI observation pattern in code comments
    - Show Activity/Fragment observation pattern
    - Demonstrate loading indicator handling
    - Demonstrate exhaustive ApiError handling with when expression
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

- [x] 11. Add test dependencies to build.gradle
  - [x] 11.1 Add Kotest and coroutine test dependencies
    - Add kotest-runner-junit5, kotest-assertions-core, kotest-property
    - Add kotlinx-coroutines-test
    - Add mockk for mocking
    - _Requirements: Testing Strategy from Design_

- [x] 12. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using Kotest
- Unit tests validate specific examples and edge cases
- The implementation maintains backward compatibility with existing RxJava-based code
- Example files serve as migration reference for existing repositories
