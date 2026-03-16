# Implementation Plan: BaseResponse Extension Refactor

## Overview

This implementation plan refactors repetitive BaseResponse<T> unwrapping logic into a reusable extension function. The refactoring eliminates code duplication across repository methods while maintaining identical behavior and backward compatibility.

## Tasks

- [x] 1. Create toResource() extension function
  - Add extension function to BaseResponse.kt file in `app/src/main/java/com/delhivery/axle/api/response/`
  - Implement logic: check isSuccess, validate responseData, throw appropriate exceptions
  - Add comprehensive KDoc documentation with @return and @throws tags
  - Ensure generic type parameter T is preserved
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 4.1, 4.2, 4.3, 4.5_

  - [ ]* 1.1 Write property test for success response unwrapping
    - **Property 1: Success Response Unwrapping**
    - **Validates: Requirements 1.3**
    - Test that any BaseResponse with isSuccess=true and non-null responseData returns that data
    - Use Kotest property testing with 100+ iterations

  - [ ]* 1.2 Write property test for failure response exception
    - **Property 2: Failure Response Exception**
    - **Validates: Requirements 1.5**
    - Test that any BaseResponse with isSuccess=false throws HttpException
    - Use Kotest property testing with 100+ iterations

  - [ ]* 1.3 Write unit tests for toResource() extension function
    - Test success case with valid data
    - Test success case with null data (should throw Exception)
    - Test failure case (should throw HttpException)
    - _Requirements: 1.3, 1.4, 1.5_

- [x] 2. Refactor fetchRecommTransactions method
  - [x] 2.1 Update TransactionsRepository.fetchRecommTransactions
    - Replace existing if-else unwrapping logic with `response.toResource()` call
    - Maintain safeApiCall wrapper
    - Ensure method signature remains unchanged
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 3.2_

  - [ ]* 2.2 Write property test for behavioral equivalence
    - **Property 3: Refactoring Behavioral Equivalence**
    - **Validates: Requirements 2.3, 3.3, 3.5**
    - Test that refactored method produces identical results to original implementation
    - Use mocked service responses with various success/failure scenarios

  - [ ]* 2.3 Write integration test for refactored method
    - Test fetchRecommTransactions with mocked service
    - Verify Resource.Success returned for successful responses
    - Verify Resource.Failure returned for error responses
    - _Requirements: 2.3, 2.5, 3.5_

- [x] 3. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Refactor additional repository methods (optional expansion)
  - [x] 4.1 Identify other repository methods using the old pattern
    - Search for similar if-else unwrapping blocks in TransactionsRepository
    - Search for similar patterns in BidsRepository
    - Document methods that can benefit from refactoring
    - _Requirements: 1.1, 3.4_

  - [x] 4.2 Apply toResource() to identified methods
    - Refactor fetchIntracityRecommTransactions if it exists
    - Refactor fetchSpotMarketplaceTransactions if it exists
    - Refactor BidsRepository methods with similar patterns
    - _Requirements: 2.1, 2.2, 3.4_

  - [ ]* 4.3 Write integration tests for additional refactored methods
    - Test each refactored method maintains identical behavior
    - _Requirements: 3.3, 3.5_

- [x] 5. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- The extension function uses Kotlin generics to maintain type safety
- All refactoring maintains backward compatibility - no breaking changes
- Property tests validate universal correctness properties across all inputs
- Unit tests validate specific examples and edge cases
- Kotest property testing framework is recommended (add dependency: `io.kotest:kotest-property:5.8.0`)
