# Changelog: Coroutines-Based API State Management

**Feature**: Coroutines-based API calls with sealed class state management  
**Spec**: `.kiro/specs/coroutines-api-state-management/`  
**Date**: March 2026  
**Architecture**: MVVM with Repository pattern, Dagger 2 DI, Kotlin Coroutines

---

## Table of Contents

1. [Spec Files Created](#1-spec-files-created)
2. [New Files Created](#2-new-files-created)
3. [Modified Files](#3-modified-files)
4. [Repository ErrorLogger Injection Updates](#4-repository-errorlogger-injection-updates)
5. [Test Dependencies Added](#5-test-dependencies-added)
6. [String Resources Added](#6-string-resources-added)

---

## 1. Spec Files Created

### `.kiro/specs/coroutines-api-state-management/requirements.md`
- **Type**: New file
- **Purpose**: Requirements document defining user stories and acceptance criteria for the coroutines-based API state management feature.
- **Contents**: User stories for Resource sealed class, ApiError enum, BaseRepository with safeApiCall/parallelApiCall2/parallelApiCall3, ErrorLogger interface, Dagger integration, ViewModel patterns, and UI observation patterns.

### `.kiro/specs/coroutines-api-state-management/design.md`
- **Type**: New file
- **Purpose**: Technical design document detailing the architecture, class diagrams, data flow, and implementation approach.

### `.kiro/specs/coroutines-api-state-management/tasks.md`
- **Type**: New file
- **Purpose**: Implementation task list with ordered, actionable tasks referencing specific requirements.

### `.kiro/specs/coroutines-api-state-management/.config.kiro`
- **Type**: New file
- **Purpose**: Spec configuration file storing spec type and workflow type metadata.

---

## 2. New Files Created

### 2.1 Core Infrastructure

#### `app/src/main/java/com/delhivery/axle/api/repository/ApiError.kt`
- **Type**: New file
- **Package**: `com.delhivery.axle.api.repository`
- **Purpose**: Enum class defining categorized API error types for consistent error handling.
- **Contents**:
  - `Timeout` — SocketTimeoutException (server took too long)
  - `Network` — IOException (connectivity issues)
  - `Unauthorized` — HTTP 401 (authentication required)
  - `AccessDenied` — HTTP 403 (insufficient permissions)
  - `NotFound` — HTTP 404 (resource not found)
  - `ServiceUnavailable` — HTTP 503 (server temporarily unavailable)
  - `Unknown` — All other unhandled errors

#### `app/src/main/java/com/delhivery/axle/api/repository/Resource.kt`
- **Type**: New file (replaced previous RxJava-based Resource class)
- **Package**: `com.delhivery.axle.api.repository`
- **Purpose**: Sealed class representing API operation results with type-safe loading, success, and failure states.
- **Contents**:
  - `Resource.Loading` — Loading state indicating API operation in progress (added in enhancement)
  - `Resource.Success<T>(data: T?)` — Successful API response with nullable data
  - `Resource.Failure(isNetworkError: Boolean, errorCode: Int?, apiError: ApiError)` — Failed response with error details

#### `app/src/main/java/com/delhivery/axle/api/repository/BaseRepository.kt`
- **Type**: New file (replaced previous empty BaseRepository class)
- **Package**: `com.delhivery.axle.api.repository`
- **Purpose**: Abstract base repository providing safe API call wrappers with comprehensive exception handling.
- **Constructor**: `abstract class BaseRepository(private val errorLogger: ErrorLogger)`
- **Methods**:
  - `safeApiCall(apiCall: suspend () -> T): Resource<T>` — Wraps single API call with exception-to-ApiError mapping
  - `parallelApiCall2(call1, call2, transform): Resource<R>` — Executes two API calls concurrently using `coroutineScope` + `async`
  - `parallelApiCall3(call1, call2, call3, transform): Resource<R>` — Executes three API calls concurrently
  - `mapHttpCodeToApiError(code: Int): ApiError` — Private helper mapping HTTP status codes to ApiError
- **Exception Handling**:
  - `CancellationException` → Rethrown (respects coroutine cancellation)
  - `SocketTimeoutException` → `ApiError.Timeout`
  - `IOException` → `ApiError.Network`
  - `HttpException` → Mapped by HTTP code (401→Unauthorized, 403→AccessDenied, 404→NotFound, 503→ServiceUnavailable, else→Unknown)
  - `Exception` → `ApiError.Unknown`

### 2.2 Error Logging

#### `app/src/main/java/com/delhivery/axle/utils/ErrorLogger.kt`
- **Type**: New file
- **Package**: `com.delhivery.axle.utils`
- **Purpose**: Interface for logging errors, enabling different implementations for production and testing.
- **Contents**:
  - `fun log(exception: Exception)` — Single method interface for exception logging

#### `app/src/main/java/com/delhivery/axle/utils/CrashlyticsErrorLogger.kt`
- **Type**: New file
- **Package**: `com.delhivery.axle.utils`
- **Purpose**: Production implementation of ErrorLogger that logs to Firebase Crashlytics.
- **Contents**:
  - `override fun log(exception: Exception)` — Calls `FirebaseCrashlytics.getInstance().recordException(exception)`

### 2.3 Dependency Injection

#### `app/src/main/java/com/delhivery/axle/injection/qualifier/IoDispatcher.kt`
- **Type**: New file
- **Package**: `com.delhivery.axle.injection.qualifier`
- **Purpose**: Dagger `@Qualifier` annotation for injecting the IO CoroutineDispatcher.
- **Contents**: `@Qualifier @Retention(AnnotationRetention.BINARY) annotation class IoDispatcher`

#### `app/src/main/java/com/delhivery/axle/injection/qualifier/MainDispatcher.kt`
- **Type**: New file
- **Package**: `com.delhivery.axle.injection.qualifier`
- **Purpose**: Dagger `@Qualifier` annotation for injecting the Main CoroutineDispatcher.
- **Contents**: `@Qualifier @Retention(AnnotationRetention.BINARY) annotation class MainDispatcher`

#### `app/src/main/java/com/delhivery/axle/injection/module/CoroutineModule.kt`
- **Type**: New file
- **Package**: `com.delhivery.axle.injection.module`
- **Purpose**: Dagger module providing coroutine dispatchers and ErrorLogger.
- **Provides**:
  - `@IoDispatcher fun provideIoDispatcher(): CoroutineDispatcher` → `Dispatchers.IO`
  - `@MainDispatcher fun provideMainDispatcher(): CoroutineDispatcher` → `Dispatchers.Main`
  - `fun provideErrorLogger(): ErrorLogger` → `CrashlyticsErrorLogger()`

### 2.4 Example Patterns & Documentation

#### `app/src/main/java/com/delhivery/axle/api/repository/ExampleRepository.kt`
- **Type**: New file
- **Package**: `com.delhivery.axle.api.repository`
- **Purpose**: Reference implementation demonstrating the coroutine-based API pattern with BaseRepository.
- **Patterns Demonstrated**:
  - Extending BaseRepository with ErrorLogger injection
  - Injecting IO dispatcher for API calls
  - Using `safeApiCall` for single API operations
  - Using `parallelApiCall2` for two concurrent API operations
  - Using `parallelApiCall3` for three concurrent API operations
  - Using `withContext(ioDispatcher)` to switch to IO thread
- **Example Data Models Defined**:
  - `ExampleUserData(id: String, name: String)`
  - `ExampleWalletData(balance: Double, currency: String)`
  - `ExampleOrderData(orderId: String, status: String)`
  - `ExampleCombinedData(user: ExampleUserData, wallet: ExampleWalletData)`
  - `ExampleDashboardData(user: ExampleUserData, wallet: ExampleWalletData, orders: List<ExampleOrderData>)`

#### `app/src/main/java/com/delhivery/axle/ui/example/ExampleViewModel.kt`
- **Type**: New file
- **Package**: `com.delhivery.axle.ui.example`
- **Purpose**: Reference ViewModel demonstrating coroutine-based API pattern with Resource states and LiveData.
- **Patterns Demonstrated**:
  - Exposing `LiveData<Resource<T>>` for API operations
  - Using `viewModelScope.launch` for coroutine launching
  - Emitting `Resource.Loading` before API call
  - Emitting Resource result (Success/Failure) after API call completes
- **LiveData Exposed**:
  - `userDataState: LiveData<Resource<ExampleUserData>>`
  - `combinedDataState: LiveData<Resource<ExampleCombinedData>>`

#### `app/src/main/java/com/delhivery/axle/utils/extensions/ResourceExtensions.kt`
- **Type**: New file
- **Package**: `com.delhivery.axle.utils.extensions`
- **Purpose**: Extension functions for Resource sealed class to simplify UI handling.
- **Extension Functions**:
  - `ApiError.toErrorMessageResId(): Int` — Maps ApiError to string resource ID
  - `ApiError.toErrorMessage(context: Context): String` — Gets localized error message string
  - `Resource<T>.onSuccess(block: (T?) -> Unit): Resource<T>` — Executes block on Success
  - `Resource<T>.onFailure(block: (ApiError, Boolean) -> Unit): Resource<T>` — Executes block on Failure

#### `app/src/main/java/com/delhivery/axle/ui/example/UIObservationPatterns.kt`
- **Type**: New file
- **Package**: `com.delhivery.axle.ui.example`
- **Purpose**: Documentation file with 5 comprehensive UI observation patterns in code comments.
- **Patterns Documented**:
  1. Basic Resource Observation with When Expression (Activity) - handles Loading, Success, Failure
  2. Using When Expression for Cleaner Code (Fragment) - handles all three states
  3. Fragment with Lifecycle-Aware Observation (`viewLifecycleOwner`) - with Loading state
  4. Retry Logic with Resource - includes Loading state handling
  5. Pull-to-Refresh with Resource - Loading controls SwipeRefreshLayout

### 2.5 Tests

#### `app/src/test/java/com/delhivery/axle/api/repository/BaseRepositoryTest.kt`
- **Type**: New file
- **Package**: `com.delhivery.axle.api.repository`
- **Purpose**: Unit tests for BaseRepository verifying safeApiCall exception handling and parallel API calls.
- **Test Classes**:
  - `BaseRepositoryTest` — 12 test methods
  - `TestRepository` — Test implementation of BaseRepository with `@Inject constructor(errorLogger: ErrorLogger)`
  - `TestErrorLogger` — Test implementation of ErrorLogger that stores logged exceptions in `loggedExceptions: MutableList<Exception>`
- **Test Methods**:
  - `testSafeApiCallSuccess()` — Verifies successful API call returns `Resource.Success`
  - `testSafeApiCallTimeout()` — Verifies `SocketTimeoutException` maps to `ApiError.Timeout`
  - `testSafeApiCallNetworkError()` — Verifies `IOException` maps to `ApiError.Network`
  - `testSafeApiCallUnauthorized()` — Verifies HTTP 401 maps to `ApiError.Unauthorized`
  - `testSafeApiCallAccessDenied()` — Verifies HTTP 403 maps to `ApiError.AccessDenied`
  - `testSafeApiCallNotFound()` — Verifies HTTP 404 maps to `ApiError.NotFound`
  - `testSafeApiCallServiceUnavailable()` — Verifies HTTP 503 maps to `ApiError.ServiceUnavailable`
  - `testSafeApiCallUnknownError()` — Verifies `RuntimeException` maps to `ApiError.Unknown`
  - `testParallelApiCall2Success()` — Verifies two concurrent calls return combined `Resource.Success`
  - `testParallelApiCall2Failure()` — Verifies failure in one concurrent call returns `Resource.Failure`
  - `testParallelApiCall3Success()` — Verifies three concurrent calls return combined `Resource.Success`
  - `testDependencyInjection()` — Verifies ErrorLogger is properly injected into repository
- **Test Setup**: Uses `@Before` method to create shared `testRepository` and `errorLogger` instances

---

## 3. Modified Files

### `app/src/main/java/com/delhivery/axle/injection/component/AppComponent.kt`
- **Type**: Modified
- **Change**: Added `CoroutineModule::class` to the `@Component(modules = [...])` annotation.
- **Before**:
  ```kotlin
  @Component(
      modules = [AppModule::class, ViewModelFactoryModule::class, AndroidSupportInjectionModule::class,
        ActivityBindingModule::class, NetworkModule::class, ServiceModule::class]
  )
  ```
- **After**:
  ```kotlin
  @Component(
      modules = [AppModule::class, ViewModelFactoryModule::class, AndroidSupportInjectionModule::class,
        ActivityBindingModule::class, NetworkModule::class, ServiceModule::class, CoroutineModule::class]
  )
  ```
- **Added Import**: `import com.delhivery.axle.injection.module.CoroutineModule`

### `app/src/main/res/values/strings.xml`
- **Type**: Modified
- **Change**: Added 7 error message string resources at the end of the file (before closing `</resources>` tag).
- **Added Strings**:
  ```xml
  <!-- Error messages for Resource API states -->
  <string name="error_timeout">Request timed out. Please try again.</string>
  <string name="error_network">No internet connection. Please check your network.</string>
  <string name="error_unauthorized">Session expired. Please login again.</string>
  <string name="error_access_denied">You don\'t have permission to access this resource.</string>
  <string name="error_not_found">The requested resource was not found.</string>
  <string name="error_service_unavailable">Service is temporarily unavailable. Please try again later.</string>
  <string name="error_unknown">Something went wrong. Please try again.</string>
  ```

### `app/build.gradle`
- **Type**: Modified
- **Change**: Added test dependencies for Kotest, MockK, and Coroutines testing.
- **Added Dependencies**:
  ```groovy
  // Kotest for property-based testing
  testImplementation 'io.kotest:kotest-runner-junit5:5.5.5'
  testImplementation 'io.kotest:kotest-assertions-core:5.5.5'
  testImplementation 'io.kotest:kotest-property:5.5.5'

  // Coroutines testing
  testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4'

  // MockK for mocking
  testImplementation 'io.mockk:mockk:1.13.4'
  ```

---

## 4. Repository ErrorLogger Injection Updates

All 21 existing repository classes were updated to:
1. Add `import com.delhivery.axle.utils.ErrorLogger` to imports
2. Add `errorLogger: ErrorLogger` parameter to `@Inject constructor`
3. Change `BaseRepository()` to `BaseRepository(errorLogger)` in class declaration

Each repository change follows this pattern:

**Before**:
```kotlin
import ...  // no ErrorLogger import

class SomeRepository @Inject constructor(
    private val someService: SomeService
) : BaseRepository() {
```

**After**:
```kotlin
import com.delhivery.axle.utils.ErrorLogger
import ...

class SomeRepository @Inject constructor(
    private val someService: SomeService,
    errorLogger: ErrorLogger
) : BaseRepository(errorLogger) {
```

### Complete List of Updated Repository Files


| # | File | Constructor Before | Constructor After |
|---|------|-------------------|-------------------|
| 1 | `app/src/main/java/com/delhivery/axle/api/repository/AuthenticationRepository.kt` | `@Inject constructor(umsService: UMSService, loadBoardService: LoadBoardService, userPrefs: UserPrefs, networkInterceptor: DelhiveryNetworkInterceptor)` | `@Inject constructor(umsService: UMSService, loadBoardService: LoadBoardService, userPrefs: UserPrefs, networkInterceptor: DelhiveryNetworkInterceptor, errorLogger: ErrorLogger)` |
| 2 | `app/src/main/java/com/delhivery/axle/api/repository/BidsRepository.kt` | `@Inject constructor(userRepository: UserRepository, bidService: BidService, userPrefs: UserPrefs)` | `@Inject constructor(userRepository: UserRepository, bidService: BidService, userPrefs: UserPrefs, errorLogger: ErrorLogger)` |
| 3 | `app/src/main/java/com/delhivery/axle/api/repository/ExpenseRepository.kt` | `@Inject constructor(expenseService: ExpenseService)` | `@Inject constructor(expenseService: ExpenseService, errorLogger: ErrorLogger)` |
| 4 | `app/src/main/java/com/delhivery/axle/api/repository/FuelRepository.kt` | `@Inject constructor(fuelService: FuelService, userPrefs: UserPrefs)` | `@Inject constructor(fuelService: FuelService, userPrefs: UserPrefs, errorLogger: ErrorLogger)` |
| 5 | `app/src/main/java/com/delhivery/axle/api/repository/InventoryRepository.kt` | `@Inject constructor(inventoryService: InventoryService, cityService: CityService)` | `@Inject constructor(inventoryService: InventoryService, cityService: CityService, errorLogger: ErrorLogger)` |
| 6 | `app/src/main/java/com/delhivery/axle/api/repository/LoadboardRepository.kt` | `@Inject constructor(loadboardService: LoadBoardService)` | `@Inject constructor(loadboardService: LoadBoardService, errorLogger: ErrorLogger)` |
| 7 | `app/src/main/java/com/delhivery/axle/api/repository/LoadCycleRepository.kt` | `@Inject constructor(loadsService: LoadCycleService)` | `@Inject constructor(loadsService: LoadCycleService, errorLogger: ErrorLogger)` |
| 8 | `app/src/main/java/com/delhivery/axle/api/repository/NotificationRepository.kt` | `@Inject constructor(notificationService: NotificationService)` | `@Inject constructor(notificationService: NotificationService, errorLogger: ErrorLogger)` |
| 9 | `app/src/main/java/com/delhivery/axle/api/repository/OMCRepository.kt` | `@Inject constructor(omcService: OMCService)` | `@Inject constructor(omcService: OMCService, errorLogger: ErrorLogger)` |
| 10 | `app/src/main/java/com/delhivery/axle/api/repository/PayableRepository.kt` | `@Inject constructor(payableService: PayableService)` | `@Inject constructor(payableService: PayableService, errorLogger: ErrorLogger)` |
| 11 | `app/src/main/java/com/delhivery/axle/api/repository/PaymentRepository.kt` | `@Inject constructor(paymentService: PaymentService, tripsService: TripService)` | `@Inject constructor(paymentService: PaymentService, tripsService: TripService, errorLogger: ErrorLogger)` |
| 12 | `app/src/main/java/com/delhivery/axle/api/repository/PriceRepository.kt` | `@Inject constructor(priceService: PriceService)` | `@Inject constructor(priceService: PriceService, errorLogger: ErrorLogger)` |
| 13 | `app/src/main/java/com/delhivery/axle/api/repository/SpotBiddingRepository.kt` | `@Inject constructor(spotBiddingService: SpotBiddingService)` | `@Inject constructor(spotBiddingService: SpotBiddingService, errorLogger: ErrorLogger)` |
| 14 | `app/src/main/java/com/delhivery/axle/api/repository/TPSRepository.kt` | `@Inject constructor(tpsService: TPSService, userPrefs: UserPrefs)` | `@Inject constructor(tpsService: TPSService, userPrefs: UserPrefs, errorLogger: ErrorLogger)` |
| 15 | `app/src/main/java/com/delhivery/axle/api/repository/TransactionsRepository.kt` | `@Inject constructor(transactionService: TransactionService, userRepository: UserRepository, userPrefs: UserPrefs, recommendationService: RecommendationService)` | `@Inject constructor(transactionService: TransactionService, userRepository: UserRepository, userPrefs: UserPrefs, recommendationService: RecommendationService, errorLogger: ErrorLogger)` |
| 16 | `app/src/main/java/com/delhivery/axle/api/repository/TripsRepository.kt` | `@Inject constructor(userRepository: UserRepository, tripsService: TripService, transactionService: TransactionService)` | `@Inject constructor(userRepository: UserRepository, tripsService: TripService, transactionService: TransactionService, errorLogger: ErrorLogger)` |
| 17 | `app/src/main/java/com/delhivery/axle/api/repository/TruckRepository.kt` | `@Inject constructor(truckService: TruckService)` | `@Inject constructor(truckService: TruckService, errorLogger: ErrorLogger)` |
| 18 | `app/src/main/java/com/delhivery/axle/api/repository/UserRepository.kt` | `@Inject constructor(appDB: AppDatabase, userPrefs: UserPrefs, userService: UserService, umsService: UMSService, loadBoardService: LoadBoardService)` | `@Inject constructor(appDB: AppDatabase, userPrefs: UserPrefs, userService: UserService, umsService: UMSService, loadBoardService: LoadBoardService, errorLogger: ErrorLogger)` |
| 19 | `app/src/main/java/com/delhivery/axle/api/repository/UtilityRepository.kt` | `@Inject constructor(utilityService: UtilityService)` | `@Inject constructor(utilityService: UtilityService, errorLogger: ErrorLogger)` |
| 20 | `app/src/main/java/com/delhivery/axle/api/repository/WalletRepository.kt` | `@Inject constructor(walletService: WalletService)` | `@Inject constructor(walletService: WalletService, errorLogger: ErrorLogger)` |
| 21 | `app/src/main/java/com/delhivery/axle/api/repository/WarehouseRepository.kt` | `@Inject constructor(warehouseService: WarehouseService)` | `@Inject constructor(warehouseService: WarehouseService, errorLogger: ErrorLogger)` |

> **Note**: `ExampleRepository.kt` was created new with ErrorLogger already in its constructor and is not listed here as a "modified" file.

---

## 5. Test Dependencies Added

### `app/build.gradle` — Dependencies Block

| Dependency | Version | Scope | Purpose |
|-----------|---------|-------|---------|
| `io.kotest:kotest-runner-junit5` | 5.5.5 | `testImplementation` | JUnit 5 test runner for Kotest |
| `io.kotest:kotest-assertions-core` | 5.5.5 | `testImplementation` | Core assertion library for Kotest |
| `io.kotest:kotest-property` | 5.5.5 | `testImplementation` | Property-based testing support |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | 1.6.4 | `testImplementation` | Coroutines test utilities (`runTest`, `TestDispatcher`) |
| `io.mockk:mockk` | 1.13.4 | `testImplementation` | Kotlin mocking framework |

---

## 6. String Resources Added

### `app/src/main/res/values/strings.xml`

| Resource Name | Value |
|--------------|-------|
| `error_timeout` | `Request timed out. Please try again.` |
| `error_network` | `No internet connection. Please check your network.` |
| `error_unauthorized` | `Session expired. Please login again.` |
| `error_access_denied` | `You don't have permission to access this resource.` |
| `error_not_found` | `The requested resource was not found.` |
| `error_service_unavailable` | `Service is temporarily unavailable. Please try again later.` |
| `error_unknown` | `Something went wrong. Please try again.` |

---

## Summary of All Files

### New Files (15 total)

| # | File Path | Type |
|---|-----------|------|
| 1 | `.kiro/specs/coroutines-api-state-management/requirements.md` | Spec |
| 2 | `.kiro/specs/coroutines-api-state-management/design.md` | Spec |
| 3 | `.kiro/specs/coroutines-api-state-management/tasks.md` | Spec |
| 4 | `.kiro/specs/coroutines-api-state-management/.config.kiro` | Spec Config |
| 5 | `app/src/main/java/com/delhivery/axle/api/repository/ApiError.kt` | Enum |
| 6 | `app/src/main/java/com/delhivery/axle/api/repository/Resource.kt` | Sealed Class |
| 7 | `app/src/main/java/com/delhivery/axle/api/repository/BaseRepository.kt` | Abstract Class |
| 8 | `app/src/main/java/com/delhivery/axle/utils/ErrorLogger.kt` | Interface |
| 9 | `app/src/main/java/com/delhivery/axle/utils/CrashlyticsErrorLogger.kt` | Implementation |
| 10 | `app/src/main/java/com/delhivery/axle/injection/qualifier/IoDispatcher.kt` | Qualifier Annotation |
| 11 | `app/src/main/java/com/delhivery/axle/injection/qualifier/MainDispatcher.kt` | Qualifier Annotation |
| 12 | `app/src/main/java/com/delhivery/axle/injection/module/CoroutineModule.kt` | Dagger Module |
| 13 | `app/src/main/java/com/delhivery/axle/api/repository/ExampleRepository.kt` | Example |
| 14 | `app/src/main/java/com/delhivery/axle/ui/example/ExampleViewModel.kt` | Example |
| 15 | `app/src/main/java/com/delhivery/axle/utils/extensions/ResourceExtensions.kt` | Extensions |
| 16 | `app/src/main/java/com/delhivery/axle/ui/example/UIObservationPatterns.kt` | Documentation |
| 17 | `app/src/test/java/com/delhivery/axle/api/repository/BaseRepositoryTest.kt` | Unit Tests |

### Modified Files (24 total)

| # | File Path | Change Type |
|---|-----------|-------------|
| 1 | `app/src/main/java/com/delhivery/axle/injection/component/AppComponent.kt` | Added CoroutineModule to @Component |
| 2 | `app/src/main/res/values/strings.xml` | Added 7 error message strings |
| 3 | `app/build.gradle` | Added 5 test dependencies |
| 4 | `app/src/main/java/com/delhivery/axle/api/repository/AuthenticationRepository.kt` | ErrorLogger injection |
| 5 | `app/src/main/java/com/delhivery/axle/api/repository/BidsRepository.kt` | ErrorLogger injection |
| 6 | `app/src/main/java/com/delhivery/axle/api/repository/ExpenseRepository.kt` | ErrorLogger injection |
| 7 | `app/src/main/java/com/delhivery/axle/api/repository/FuelRepository.kt` | ErrorLogger injection |
| 8 | `app/src/main/java/com/delhivery/axle/api/repository/InventoryRepository.kt` | ErrorLogger injection |
| 9 | `app/src/main/java/com/delhivery/axle/api/repository/LoadboardRepository.kt` | ErrorLogger injection |
| 10 | `app/src/main/java/com/delhivery/axle/api/repository/LoadCycleRepository.kt` | ErrorLogger injection |
| 11 | `app/src/main/java/com/delhivery/axle/api/repository/NotificationRepository.kt` | ErrorLogger injection |
| 12 | `app/src/main/java/com/delhivery/axle/api/repository/OMCRepository.kt` | ErrorLogger injection |
| 13 | `app/src/main/java/com/delhivery/axle/api/repository/PayableRepository.kt` | ErrorLogger injection |
| 14 | `app/src/main/java/com/delhivery/axle/api/repository/PaymentRepository.kt` | ErrorLogger injection |
| 15 | `app/src/main/java/com/delhivery/axle/api/repository/PriceRepository.kt` | ErrorLogger injection |
| 16 | `app/src/main/java/com/delhivery/axle/api/repository/SpotBiddingRepository.kt` | ErrorLogger injection |
| 17 | `app/src/main/java/com/delhivery/axle/api/repository/TPSRepository.kt` | ErrorLogger injection |
| 18 | `app/src/main/java/com/delhivery/axle/api/repository/TransactionsRepository.kt` | ErrorLogger injection |
| 19 | `app/src/main/java/com/delhivery/axle/api/repository/TripsRepository.kt` | ErrorLogger injection |
| 20 | `app/src/main/java/com/delhivery/axle/api/repository/TruckRepository.kt` | ErrorLogger injection |
| 21 | `app/src/main/java/com/delhivery/axle/api/repository/UserRepository.kt` | ErrorLogger injection |
| 22 | `app/src/main/java/com/delhivery/axle/api/repository/UtilityRepository.kt` | ErrorLogger injection |
| 23 | `app/src/main/java/com/delhivery/axle/api/repository/WalletRepository.kt` | ErrorLogger injection |
| 24 | `app/src/main/java/com/delhivery/axle/api/repository/WarehouseRepository.kt` | ErrorLogger injection |

**Total files affected: 41** (17 new + 24 modified)


---

## 7. Enhancement: Resource.Loading State Addition

**Date**: March 2026  
**Change Type**: Enhancement to Resource sealed class

### Overview
Added `Resource.Loading` as a third state to the Resource sealed class, replacing the separate `isLoading: LiveData<Boolean>` pattern. This provides a single source of truth for API operation states.

### Files Modified

#### `app/src/main/java/com/delhivery/axle/api/repository/Resource.kt`
- **Change**: Added `object Loading : Resource<Nothing>()` as first state
- **Impact**: All UI code must now handle three states (Loading, Success, Failure) instead of two

#### `app/src/main/java/com/delhivery/axle/ui/example/ExampleViewModel.kt`
- **Removed**: `_isLoading: MutableLiveData<Boolean>` and `isLoading: LiveData<Boolean>` properties
- **Added**: `_userDataState.value = Resource.Loading` emission before API calls
- **Simplified**: No manual loading flag management needed

#### `app/src/main/java/com/delhivery/axle/ui/example/UIObservationPatterns.kt`
- **Updated**: All 5 UI observation patterns to handle `Resource.Loading` state
- **Changed**: Pattern 1 now shows single observation instead of dual (loading + data)
- **Changed**: Pattern 2 uses when expression instead of extension functions
- **Changed**: Pattern 5 controls SwipeRefreshLayout directly from Resource.Loading
- **Updated**: Key Takeaways section to reflect Loading state pattern

### Benefits
1. **Single Source of Truth**: One LiveData stream contains all states (Loading, Success, Failure)
2. **Exhaustive Handling**: Compiler enforces handling all three states in when expressions
3. **Cleaner ViewModels**: No manual loading flag management
4. **Explicit State Transitions**: Loading → Success/Failure is clear and predictable
5. **Simpler UI Code**: One observation instead of two separate LiveData streams

### Migration Pattern

**Before** (Separate Loading State):
```kotlin
// ViewModel
private val _isLoading = MutableLiveData<Boolean>()
val isLoading: LiveData<Boolean> = _isLoading

fun fetchData() {
    viewModelScope.launch {
        _isLoading.value = true
        val result = repository.fetchData()
        _dataState.value = result
        _isLoading.value = false
    }
}

// UI
viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
    if (isLoading) showLoading() else hideLoading()
}
viewModel.dataState.observe(viewLifecycleOwner) { resource ->
    when (resource) {
        is Resource.Success -> { /* ... */ }
        is Resource.Failure -> { /* ... */ }
    }
}
```

**After** (Loading as Resource State):
```kotlin
// ViewModel
fun fetchData() {
    viewModelScope.launch {
        _dataState.value = Resource.Loading
        val result = repository.fetchData()
        _dataState.value = result
    }
}

// UI
viewModel.dataState.observe(viewLifecycleOwner) { resource ->
    when (resource) {
        is Resource.Loading -> showLoading()
        is Resource.Success -> {
            hideLoading()
            // handle success
        }
        is Resource.Failure -> {
            hideLoading()
            // handle failure
        }
    }
}
```

### Detailed Changes Document
See `RESOURCE_LOADING_STATE_CHANGES.md` for complete before/after code examples and migration guide.

---

## 8. ViewModel Tests for Resource.Loading State

**Date**: March 2026  
**Change Type**: New test file for ViewModel layer

### Overview
Created comprehensive unit tests for ExampleViewModel to verify proper Resource.Loading state emission and state transitions.

### New Test File

#### `app/src/test/java/com/delhivery/axle/ui/example/ExampleViewModelTest.kt`
- **Type**: New file
- **Package**: `com.delhivery.axle.ui.example`
- **Purpose**: Unit tests for ExampleViewModel verifying Resource state emissions and transitions
- **Test Framework**: JUnit 4 with MockK, Coroutines Test, and AndroidX Arch Core Testing
- **Test Methods**:
  - `fetchUserData emits Loading then Success` — Verifies Loading is emitted before Success
  - `fetchUserData emits Loading then Failure on error` — Verifies Loading is emitted before Failure
  - `fetchCombinedData emits Loading then Success` — Verifies Loading emission for parallel calls
  - `fetchCombinedData emits Loading then Failure on timeout` — Verifies Loading emission on timeout
  - `Resource Loading state exists and is singleton` — Verifies Loading is a singleton object
  - `Resource sealed class has three states` — Verifies exhaustive when expression handling

### Test Coverage
- **State Emission Order**: Verifies Loading is always emitted before Success/Failure
- **State Transitions**: Tests all possible state transitions (Loading → Success, Loading → Failure)
- **Singleton Pattern**: Confirms Resource.Loading is a singleton object
- **Exhaustive Handling**: Validates that when expressions must handle all three states
- **Error Scenarios**: Tests timeout, network errors, and other failure cases

### Dependencies Used
- `InstantTaskExecutorRule` — For synchronous LiveData execution in tests
- `StandardTestDispatcher` — For controlled coroutine execution
- `MockK` — For mocking repository and observers
- `kotlinx-coroutines-test` — For coroutine testing utilities
- `androidx.arch.core:core-testing` — For LiveData testing support (already in build.gradle)

### Key Test Patterns
```kotlin
// Verify Loading is emitted first, then Success/Failure
verify(exactly = 1) { observer.onChanged(Resource.Loading) }
verify(exactly = 1) { observer.onChanged(Resource.Success(data)) }
verify(exactly = 2) { observer.onChanged(any()) } // Total emissions
```

### Why These Tests Matter
1. **Ensures Loading State**: Confirms ViewModels emit Loading before API calls
2. **State Transition Validation**: Verifies proper state flow (Loading → Success/Failure)
3. **Prevents Regressions**: Catches if Loading emission is accidentally removed
4. **Documents Behavior**: Serves as living documentation of expected ViewModel behavior
5. **Compile-Time Safety**: Tests exhaustive when expression handling

---

## Updated Summary of All Files

### New Files (17 total)

| # | File Path | Type | Notes |
|---|-----------|------|-------|
| 1 | `.kiro/specs/coroutines-api-state-management/requirements.md` | Spec | |
| 2 | `.kiro/specs/coroutines-api-state-management/design.md` | Spec | |
| 3 | `.kiro/specs/coroutines-api-state-management/tasks.md` | Spec | |
| 4 | `.kiro/specs/coroutines-api-state-management/.config.kiro` | Spec Config | |
| 5 | `app/src/main/java/com/delhivery/axle/api/repository/ApiError.kt` | Enum | |
| 6 | `app/src/main/java/com/delhivery/axle/api/repository/Resource.kt` | Sealed Class | **Enhanced with Loading state** |
| 7 | `app/src/main/java/com/delhivery/axle/api/repository/BaseRepository.kt` | Abstract Class | |
| 8 | `app/src/main/java/com/delhivery/axle/utils/ErrorLogger.kt` | Interface | |
| 9 | `app/src/main/java/com/delhivery/axle/utils/CrashlyticsErrorLogger.kt` | Implementation | |
| 10 | `app/src/main/java/com/delhivery/axle/injection/qualifier/IoDispatcher.kt` | Qualifier Annotation | |
| 11 | `app/src/main/java/com/delhivery/axle/injection/qualifier/MainDispatcher.kt` | Qualifier Annotation | |
| 12 | `app/src/main/java/com/delhivery/axle/injection/module/CoroutineModule.kt` | Dagger Module | |
| 13 | `app/src/main/java/com/delhivery/axle/api/repository/ExampleRepository.kt` | Example | |
| 14 | `app/src/main/java/com/delhivery/axle/ui/example/ExampleViewModel.kt` | Example | **Updated to use Loading state** |
| 15 | `app/src/main/java/com/delhivery/axle/utils/extensions/ResourceExtensions.kt` | Extensions | |
| 16 | `app/src/main/java/com/delhivery/axle/ui/example/UIObservationPatterns.kt` | Documentation | **Updated all 5 patterns** |
| 17 | `app/src/test/java/com/delhivery/axle/api/repository/BaseRepositoryTest.kt` | Unit Tests | Repository layer tests |
| 18 | `app/src/test/java/com/delhivery/axle/ui/example/ExampleViewModelTest.kt` | Unit Tests | **ViewModel layer tests for Loading state** |
| 19 | `RESOURCE_LOADING_STATE_CHANGES.md` | Documentation | Enhancement changelog |

### Modified Files (24 total)

All repository files and configuration files remain the same as documented in Section 4.

**Total files affected: 43** (19 new + 24 modified)
