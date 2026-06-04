# Legacy Axle Code Removal Plan

## Overview

This document lists all loads/bids/offers/contracts/trips/marketplace/placements/POD code from the old Axle logistics app that is NOT needed in the new FS (Financial Services) app. Each section includes the files to remove and the affected points that need fixing after removal.

---

## Phase 1: UI Packages

### 1.1 `ui/bids/`
**Files to remove:** BidsViewModel, TripsActivity, TripsViewModel, DmtBidsRVAdapter, TripType, BidType, all files in this package.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel binding
- `ActivityBindingModule.kt` — remove Activity/Fragment bindings
- `HomeFragmentType.kt` — remove BidsFragment reference
- `HomeActivity.kt` — remove bids tab navigation
- Navigation from `HomeBidsFragment` references

---

### 1.2 `ui/biddetails/`
**Files to remove:** MarketPlaceBidDetailsActivity, MarketPlaceBidDetailsViewModel, BidDetailsCreateEditDialog, BulkBidsRVAdapter, AcceptAdhocIntracityBidBottomDialog, all files.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel binding
- `ActivityBindingModule.kt` — remove Activity binding
- Any navigation intents from `HomeBidsFragment`, `HomeLoadsFragment`

---

### 1.3 `ui/contractDetails/`
**Files to remove:** ContractDetailsActivity, ContractDetailsViewModel, ContractDetailsCreateEditDialog, PlacementsContractDetailsActivity, ContractsRouteDetailsAdapter, all files.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel bindings
- `ActivityBindingModule.kt` — remove Activity bindings
- Navigation from `HomeContractsFragment`

---

### 1.4 `ui/tripdetails/`
**Files to remove:** TripDetailsActivity, TripDetailsViewModel, TripMilestoneAdapter, TripPaymentSummaryRVAdapter, PodAdapter, UploadImageActivity, all files.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel bindings
- `ActivityBindingModule.kt` — remove Activity bindings
- Navigation from `HomeTripsFragment`, trip list items

---

### 1.5 `ui/searchload/`
**Files to remove:** SearchLoadActivity, SearchLoadViewModel, fragments/searchload/, fragments/searchresults/, all files.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel binding
- `ActivityBindingModule.kt` — remove Activity binding
- FAB or search button in `HomeLoadsFragment`

---

### 1.6 `ui/searchtrip/`
**Files to remove:** SearchActivity, SearchViewModel, SearchRVAdapter.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel binding
- `ActivityBindingModule.kt` — remove Activity binding

---

### 1.7 `ui/searchongoingtrip/`
**Files to remove:** SearchOngoingTripActivity, SearchOngoingTripViewModel, SearchOngoingTripRVAdapter.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel binding
- `ActivityBindingModule.kt` — remove Activity binding

---

### 1.8 `ui/placementdetails/`
**Files to remove:** PlacementDetailsActivity, PlacementDetailsViewModel, FilterDurationAdapter, all files.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel binding
- `ActivityBindingModule.kt` — remove Activity binding

---

### 1.9 `ui/sharerate/`
**Files to remove:** ShareRateActivity, ShareRateViewModel, HomeLoadAlertRequestItemData.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel binding
- `ActivityBindingModule.kt` — remove Activity binding
- Navigation from `HomeTrucksFragment` or rate share cards

---

### 1.10 `ui/selectroute/`
**Files to remove:** Entire directory (activity/, fragments/, SelectRouteFlowType).

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel bindings
- `ActivityBindingModule.kt` — remove Activity/Fragment bindings
- Navigation from onboarding or profile

---

### 1.11 `ui/selectroutewelcome/`
**Files to remove:** SelectRouteWelcomeActivity, SelectRouteWelcomeViewModel.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel binding
- `ActivityBindingModule.kt` — remove Activity binding

---

### 1.12 `ui/userroutes/`
**Files to remove:** ManageRouteActivity, UserRoutesActivity, UserRoutesRVAdapter.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel bindings
- `ActivityBindingModule.kt` — remove Activity bindings
- Profile menu item for "Manage Routes"

---

### 1.13 `ui/searchCity/` and `ui/searchcitystate/`
**Files to remove:** SearchCity, SearchCityViewModel, SearchCityRvAdapter, SearchCityStateActivity, SearchOriginCityActivity, PopularCitiesAdapter.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel bindings
- `ActivityBindingModule.kt` — remove Activity bindings
- Navigation from route selection or load search

---

### 1.14 `ui/invoicereview/`
**Files to remove:** InvoiceReviewActivity, InvoiceReviewViewModel, InvoiceParticularAdapter.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel binding
- `ActivityBindingModule.kt` — remove Activity binding

---

### 1.15 `ui/paymentdetails/`
**Files to remove:** PaymentDetailsActivity, PaymentDetailsViewModel, VendorPolicyActivity.

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel bindings
- `ActivityBindingModule.kt` — remove Activity bindings

---

### 1.16 Home Fragment Sub-packages

| Directory | Files |
|-----------|-------|
| `ui/home/fragments/loads/` | HomeLoadsRVAdapter, HomeLoadsRVAdapterVH, etc. |
| `ui/home/fragments/loads_truck/` | HomeLoadsTruckFragment, HomeLoadsTruckViewModel |
| `ui/home/fragments/bids/` | HomeBidsFragment, HomeBidsViewModel, HomeBidsRVAdapter |
| `ui/home/fragments/contracts/` | HomeContractsFragment, HomeContractsViewModel, HomeContractsRVAdapter |
| `ui/home/fragments/trips/` | HomeTripsFragment, HomeTripsViewModel, HomeTripsRVAdapter |
| `ui/home/fragments/placements/` | HomePlacementsFragment, HomePlacementsViewModel, all delayed/expected fragments |
| `ui/home/fragments/pod/` | HomePodsFragment, HomePodViewModel, HomePodRVAdapter, tab fragments |
| `ui/home/fragments/alerts/` | HomeAlertsFragment, HomeAlertsViewModel |

**Affected by removal:**
- `HomeFragmentType.kt` — remove fragment type entries
- `HomeFragmentsAdapter.kt` — remove fragment instantiation
- `HomeFragmentsBindingModule.kt` — remove `@ContributesAndroidInjector` entries
- `ViewModelFactoryModule.kt` — remove ViewModel bindings
- `HomeActivity.kt` — remove tab/navigation references
- Bottom navigation menu XML — remove tab items

---

### 1.17 Home Activity Sub-packages

| Directory | Files |
|-----------|-------|
| `ui/home/activity/transactiondetail/` | TransactionDetailActivity, TransactionDetailViewModel |
| `ui/home/activity/transactionlist/` | TransactionsActivity, TransactionsViewModel, TransactionsRVAdapter |
| `ui/home/activity/fuel/` | ActiveTripsActivity, ActiveTripsViewModel, ActiveTripsRVAdapter |
| `ui/home/activity/docket/` | DocketUpdateActivity, DocketUpdateViewModel, DocketAdapter |

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove ViewModel bindings
- `ActivityBindingModule.kt` — remove Activity bindings

---

### 1.18 Dialogs

| File | Related Feature |
|------|----------------|
| `ui/dialogs/BidConfirmReviseDialog.kt` | Bids |
| `ui/dialogs/TripsFilterDialog.kt` | Trips |
| `ui/dialogs/LoadsServiceInfoBottomSheetDialogFragment.kt` | Loads |
| `ui/dialogs/ChangePaymentModeDialog.kt` | Trips |
| `ui/dialogs/RouteDeleteDialog.kt` | Routes/Loads |

**Affected by removal:**
- `HomeFragmentsBindingModule.kt` — remove dialog fragment bindings
- References from ViewModels that show these dialogs

---

### 1.19 Custom Views

| File | Related Feature |
|------|----------------|
| `ui/custom/DelhiveryBidAnimatedSearchBar.kt` | Bids search |
| `ui/custom/DelhiveryFabCardMenu.kt` | Load FAB menu |

**Affected by removal:**
- Layout XMLs that reference these custom views
- Fragment code that initializes them

---

## Phase 2: API Services

| Service File | UrlConfig Entry | Affected NetworkModule Binding |
|-------------|-----------------|-------------------------------|
| `api/service/LoadBoardService.kt` | `LoadboardService` | `provideLoadBoardService()` |
| `api/service/LoadCycleService.kt` | `LoadCycleService` | `provideLoadCycleService()` |
| `api/service/SpotBiddingService.kt` | `SpotBiddingService` | `provideSpotBiddingService()` |
| `api/service/TransactionService.kt` | `TransactionService` | `provideTransactionService()` |
| `api/service/TripService.kt` | `TripService` | `provideTripService()` |
| `api/service/RecommendationService.kt` | `RecommendationService` | `provideRecommendationService()` |
| `api/service/PriceService.kt` | `PriceService` | `providePriceService()` |
| `api/service/PayableService.kt` | `PayableService` | `providePayableService()` |
| `api/service/InvoiceService.kt` | `InvoiceService` | `provideInvoiceService()` |
| `api/service/CityService.kt` | `CityService` | `provideCityService()` |
| `api/service/WarehouseService.kt` | `WarehouseService` | `provideWarehouseService()` |

**Affected by removal:**
- `NetworkModule.kt` — remove all `@Provides` methods for these services
- `UrlConfig.kt` — remove corresponding URL entries (optional, they're just unused enum values)
- All repositories that depend on these services

---

## Phase 3: Repositories

| Repository File | Depends On (Service) | Used By |
|----------------|---------------------|---------|
| `LoadboardRepository.kt` | LoadBoardService | HomeLoadsVM, HomeTrucksVM, ProfileDetailsVM, various VMs |
| `LoadCycleRepository.kt` | LoadCycleService | MyWorker, HomeBidsVM |
| `SpotBiddingRepository.kt` | SpotBiddingService | MarketPlaceBidDetailsVM |
| `TransactionsRepository.kt` | TransactionService | TransactionsVM, HomeTripsVM |
| `TripsRepository.kt` | TripService | TripDetailsVM, HomeTripsVM |
| `PriceRepository.kt` | PriceService | MyWorker, HomeBidsVM |
| `PayableRepository.kt` | PayableService | TripDetailsVM |
| `InvoiceRepository.kt` | InvoiceService | InvoiceReviewVM |
| `WarehouseRepository.kt` | WarehouseService | SearchLoadVM |
| `SalesCodeRepository.kt` | — | SalesCodeVM |

**Affected by removal:**
- `ViewModelFactoryModule.kt` — ViewModels that inject these repos will error
- `HomeTrucksViewModel.kt` — currently injects `LoadboardRepository` (check if still needed for FASTag trucks)
- Remove `@Inject constructor` references in all dependent ViewModels (which should already be deleted in Phase 1)

---

## Phase 4: Request/Response Models

### Requests to Remove
| File | Used By |
|------|---------|
| `BidsRequest.kt` | BidsRepository, BidDetailsVM |
| `LoadCycleRequest.kt` | LoadCycleRepository |
| `ConfirmCollectionRequest.kt` | TripDetailsVM |
| `InitiateCallRequest.kt` | BidDetailsVM, TripDetailsVM |
| `InvoiceActionRequest.kt` | InvoiceReviewVM |
| `InvoiceDownloadRequest.kt` | InvoiceReviewVM |
| `PaymentBreakupRequest.kt` | TripDetailsVM |
| `PodRequest.kt` | TripDetailsVM |
| `ReccomdationRequest.kt` | RecommendationService |
| `UpdatePriceRequest.kt` | PriceService |
| `UpdateRouteRequest.kt` | LoadBoardService |
| `WarehouseRequest.kt` | WarehouseService |
| `DisputeSubmissionRequest.kt` | TripDetailsVM |

### Responses to Remove
| File | Used By |
|------|---------|
| `BidsResponse.kt` | LoadBoardService, HomeBidsVM |
| `ContractsSummaryResponse.kt` | HomeContractsVM |
| `ConfirmCollectionResponse.kt` | TripDetailsVM |
| `InitiateCallResponse.kt` | BidDetailsVM |
| `IntercityRecommondedSummaryResponse.kt` | RecommendationService |
| `InvoiceActionResponse.kt` | InvoiceReviewVM |
| `InvoiceDetailsResponse.kt` | InvoiceReviewVM |
| `InvoiceDownloadResponse.kt` | InvoiceReviewVM |
| `LoadCycleResponse.kt` | LoadCycleService |
| `PayableResponse.kt` | PayableService |
| `PaymentBreakupResponse.kt` | TripDetailsVM |
| `PlacementsLoadsResponse.kt` | HomePlacementsVM |
| `PopularLocationsResponse.kt` | SearchLoadVM |
| `TransactionsResponse.kt` | TransactionService |
| `TripsResponse.kt` | TripService |
| `GetPricingDataResponse.kt` | PriceService |
| `DriverDataResponse.kt` | TripDetailsVM |
| `DisputeIssuesResponse.kt` | TripDetailsVM |
| `DisputeSubmissionResponse.kt` | TripDetailsVM |
| `FacilityAddressResponse.kt` | WarehouseService |
| `InventoryResponse.kt` | LoadBoardService |
| `ServiceGroupsResponse.kt` | LoadBoardService |
| `ServiceRequirementsResponse.kt` | LoadBoardService |

**Affected by removal:**
- Compile errors in services/repositories that reference these models (should be deleted first)
- `BaseResponse.kt` — keep, it's used by FASTag/Auth APIs too

---

## Phase 5: Data Models

### Directories to Remove Entirely
| Directory | File Count |
|-----------|-----------|
| `data/bids/` | ~8 files |
| `data/biddetail/` | ~2 files |
| `data/home/loads/` | ~14 files |
| `data/home/bids/` | ~7 files |
| `data/home/contracts/` | ~5 files |
| `data/home/trips/` | ~7 files |
| `data/home/placements/` | ~8 files |
| `data/home/pod/` | ~4 files |
| `data/transactions/` | ~5 files |
| `data/tripdetail/` | ~6 files |
| `data/placements/` | ~3 files |
| `data/sharerates/` | ~4 files |
| `data/userroutes/` | ~2 files |
| `data/dispute/` | ~2 files |
| `data/address/` | ~3 files |

### Individual Files
- `data/DocketItem.kt`
- `data/PodItem.kt`
- `data/PlacementDetailsModels.kt`
- `data/RouteMappingModel.kt`
- `data/TripHistoryModel.kt`

**Affected by removal:**
- Adapters and ViewModels that use these as list item types
- Layout XMLs with data binding that reference these models

---

## Phase 6: Database (Room)

| File | Purpose |
|------|---------|
| `database/dao/OffersDao.kt` | Offers CRUD |
| `database/entity/OffersEntity.kt` | Offers table |
| `database/entity/SearchLoadHistoryEntity.kt` | Load search history |
| `database/dao/SearchHistoryDao.kt` | Search history CRUD |

**Affected by removal:**
- `AppDatabase.kt` — remove DAO references and entity from `@Database` annotation
- `MyWorker.kt` — already disabled but references OffersDao
- Room migration may be needed if DB version changes

---

## Phase 7: Background Workers & Utils

| File | Purpose | Affected By Removal |
|------|---------|---------------------|
| `SyncOfferData/MyWorker.kt` | Offers sync | `ViewModelFactoryModule.kt` (WorkerKey binding), `DaggerWorkerFactory` |
| `exception/NoBidsFoundException.kt` | Bid error | HomeBidsVM |
| `utils/LoadTypeUtils.kt` | Load type helpers | Adapters, VMs |
| `utils/PlacementDataMapper.kt` | Placement mapping | HomePlacementsVM |
| `utils/PaginationScrollListener.kt` | List pagination | Multiple fragments (check if used by FASTag lists too) |
| `utils/DynamicPaginationScrollListener.kt` | Dynamic pagination | Multiple fragments |

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove `@WorkerKey(MyWorker::class)` binding
- Check if `PaginationScrollListener` is used by any FS-related lists before removing

---

## Phase 8: Layout XMLs (~137+ files)

### Activities
`activity_bids.xml`, `activity_trips.xml`, `activity_active_trips.xml`, `activity_contract_details.xml`, `activity_placements_contract_details.xml`, `activity_marketplace_bid_details.xml`, `activity_search_load.xml`, `activity_search_ongoing_trip.xml`, `activity_trip_details.xml`, `activity_placements_details.xml`, `activity_placements.xml`, `activity_transaction_detail.xml`, `activity_transaction_details.xml`, `activity_transactions.xml`, `activity_update_docket.xml`, `activity_upload_image.xml`, `activity_epod_details.xml`, `activity_hpod_details.xml`

### Fragments
`fragment_home_bids.xml`, `fragment_home_contracts.xml`, `fragment_home_loads_truck.xml`, `fragment_home_trips.xml`, `fragment_home_placements.xml`, `fragment_home_placements_v2.xml`, `fragment_home_placements_v3.xml`, `fragment_home_placements_delayed.xml`, `fragment_home_placements_expected.xml`, `fragment_home_pod.xml`, `fragment_home_new_pod.xml`, `fragment_pending_pod_tab.xml`, `fragment_submitted_pod_tab.xml`, `fragment_search_load.xml`

### Cards & Items
All `card_bids_*`, `card_load_*`, `card_contracts_*`, `card_place_bid_*`, `card_strong_bid*`, `card_weak_bid*`, `view_bid_*`, `view_home_bids_*`, `view_home_loads_*`, `view_home_trips_*`, `view_home_contracts_*`, `view_trip_*`, `view_contracts_*`, `view_placements_*`, `view_pod_*`, `view_transaction_*`, `view_active_trip_*`, `view_completed_trip_*`, `load_delhivery_*`, `item_bid_*`, `item_trip_*`, `item_loads_*`, `item_placement_*`, `item_pod.xml`, `item_bottom_button_marketplace*`, `item_bottom_card_load*`

### Dialogs
`dialog_bid_*`, `dialog_bulk_bid_*`, `dialog_confirm_bid*`, `dialog_contracts_*`, `dialog_trips_filter*`, `dialog_loads_service_info*`, `dialog_bottom_accept_intracity*`, `dialog_bottom_truck_unloading*`, `dialog_placement_details_edit*`, `dialog_epod_success*`

**Affected by removal:**
- Ensure no remaining Activity/Fragment references these layouts in `layoutId()`
- Check `AndroidManifest.xml` — remove `<activity>` declarations for deleted activities

---

## Phase 9: AndroidManifest.xml Cleanup

After removing all UI files, remove corresponding `<activity>` entries from `AndroidManifest.xml`:
- All bid/trip/contract/placement/search/transaction/docket activities
- Any intent filters for deep links to loads/bids

---

## Phase 10: Dagger Modules Cleanup

| Module File | What to Remove |
|-------------|---------------|
| `ActivityBindingModule.kt` | All `@ContributesAndroidInjector` for deleted Activities |
| `HomeFragmentsBindingModule.kt` | All `@ContributesAndroidInjector` for deleted Fragments |
| `ViewModelFactoryModule.kt` | All `@Binds @IntoMap @ViewModelKey` for deleted ViewModels + `@WorkerKey(MyWorker::class)` |
| `NetworkModule.kt` | All `@Provides` for deleted Services (LoadBoard, LoadCycle, SpotBidding, Transaction, Trip, Recommendation, Price, Payable, Invoice, City, Warehouse) |

---

## Removal Order (Recommended)

1. **Dagger bindings** — Comment out/remove bindings in modules first to identify compile errors
2. **UI packages** — Delete feature directories
3. **API Services** — Delete service interfaces
4. **Repositories** — Delete repository classes
5. **Request/Response models** — Delete API models
6. **Data models** — Delete data classes
7. **Database** — Remove Room entities/DAOs, update AppDatabase
8. **Workers/Utils** — Delete workers and unused utils
9. **Layouts** — Delete XML files
10. **Manifest** — Remove activity declarations
11. **UrlConfig** — Remove unused URL entries
12. **Build & verify** — `./gradlew assembleDevelopmentDebug`

---

## Estimated Impact

| Metric | Count |
|--------|-------|
| Files to remove | ~400+ |
| Lines of code removed | ~30,000+ (estimate) |
| Dagger bindings to update | ~50+ |
| Layout XMLs to delete | ~137+ |
| APK size reduction | Significant (TBD after removal) |

---

## Phase 11: Workers, Android Services & Broadcast Receivers

### Workers

| Worker | File | Purpose | Keep/Remove | Notes |
|--------|------|---------|-------------|-------|
| `MyWorker` | `SyncOfferData/MyWorker.kt` | Periodic offers/loads sync | **REMOVE** | Already disabled. Remove file + Dagger `@WorkerKey` binding in `ViewModelFactoryModule` |
| `RefreshTokenWorker` | `tokenExpiryHandling/RefreshTokenWorker.kt` | Periodic UMS token refresh (old auth) | **REMOVE** | Uses old UMS endpoint (`/v2/refresh_token`). Replaced by `TokenAuthenticator` for FS auth. Remove file + Dagger `@WorkerKey` binding in `ViewModelFactoryModule` |

**Affected by removal:**
- `ViewModelFactoryModule.kt` — remove both `@WorkerKey` bindings
- `DaggerWorkerFactory.kt` — may become empty (only keep if future workers are added)
- `KotlinApp.kt` — `WorkManager.initialize()` can stay (needed for any future workers)
- `DelhiveryFCMService.kt` — **remove** the `RefreshTokenWorker` enqueue block (lines ~126-140) that starts the worker on push notification receipt

---

### Android Services (Declared in Manifest)

| Service | File | Purpose | Keep/Remove | Notes |
|---------|------|---------|-------------|-------|
| `DelhiveryFCMService` | `fcm/DelhiveryFCMService.kt` | Firebase push notifications | **KEEP (modify)** | Still needed for FS notifications. Remove loads/bids notification handling extras (`transaction_ids`, `pricing_id`, `pricing_sort_key`, `offer_id`, `vehicle_number`). Remove `RefreshTokenWorker` enqueue logic. |
| `TransferService` (AWS) | (Amazon SDK) | S3 file upload | **KEEP** | Used for document/KYC uploads |

**DelhiveryFCMService cleanup needed:**
- Remove extras: `ARGS_TRANSACTION_IDS`, `ARGS_PREFERRED_TRANSACTION_ID`, `ARGS_VEHICLE_NUMBER`, `ARGS_PRICING_ID`, `ARGS_PRICING_SORT_KEY`, `ARGS_NOTIFICATION_FROM`, `ARGS_OFFER_ID`
- Remove `RefreshTokenWorker` periodic work enqueue block
- Keep: FCM token handling, MoEngage pass-through, basic notification display, `HomeActivity` intent

---

### Broadcast Receivers

| Receiver | File/Location | Purpose | Keep/Remove | Notes |
|----------|--------------|---------|-------------|-------|
| `OTPReceiver` | `receiver/OTPReceiver.kt` | Auto-read OTP from SMS | **KEEP** | Used by auth flow |
| `networkReceiver` | `ConnectionLiveData.kt` | Network connectivity changes | **KEEP** | Core infrastructure |
| `mMessageReceiver` in `EditTruckDialog` | `ui/trucks/EditTruckDialog.kt` | City selection broadcast | **KEEP** | Part of truck management (FASTag) |
| `mMessageReceiver` in `ActivateTruckDialog` | `ui/trucks/ActivateTruckDialog.kt` | City selection broadcast | **KEEP** | Part of truck management (FASTag) |
| `cityReceiver` in `BuyFastagBottomSheetDialogFragment` | `ui/dialogs/BuyFastagBottomSheetDialogFragment.kt` | City selection for FASTag | **KEEP** | Part of FASTag flow |
| `mMessageReceiver` in `AddTruckBottomSheetDialogFragment` | `ui/dialogs/AddTruckBottomSheetDialogFragment.kt` | City selection for truck add | **KEEP** | Part of truck management |
| `mMessageReceiver` in `AddTruckBottomSheetDialog` | `ui/dialogs/AddTruckBottomSheetDialog.kt` | City selection for truck add | **KEEP** | Part of truck management |
| `mMessageReceiver` in `ChangePaymentModeDialog` | `ui/dialogs/ChangePaymentModeDialog.kt` | Payment mode for trips | **REMOVE** | Part of trip flow (removed in Phase 1.18) |
| `onDownloadComplete` in `FastagTransactionDetailsActivity` | `ui/fastag/fastag_details/FastagTransactionDetailsActivity.kt` | Download complete for FASTag txns | **KEEP** | Part of FASTag flow |
| `onDownloadComplete` in `ConsolidatedPageActivity` | `ui/ledger/ConsolidatedPageActivity.kt` | Download complete for ledger | **KEEP** | Part of wallet/ledger flow |
| `receiver` in `DelhiveryFCMService` | `fcm/DelhiveryFCMService.kt` | Notification dismiss tracking | **KEEP** | Generic notification infra |

---

### tokenExpiryHandling package

| File | Purpose | Keep/Remove |
|------|---------|-------------|
| `tokenExpiryHandling/RefreshTokenWorker.kt` | Old UMS token refresh worker | **REMOVE** |
| `tokenExpiryHandling/` (entire package) | Token expiry via WorkManager | **REMOVE** — replaced by `TokenAuthenticator` |

**Affected by removal:**
- `DelhiveryFCMService.kt` — remove import and `RefreshTokenWorker` enqueue code
- `ViewModelFactoryModule.kt` — remove `@WorkerKey(RefreshTokenWorker::class)` binding
- `KotlinApp.kt` — no change needed (WorkManager init stays)

---

### Summary of Worker/Service/Receiver Changes

| Action | Count |
|--------|-------|
| Workers to REMOVE | 2 (MyWorker, RefreshTokenWorker) |
| Android Services to MODIFY | 1 (DelhiveryFCMService — cleanup) |
| Android Services to REMOVE | 0 |
| Broadcast Receivers to REMOVE | 1 (ChangePaymentModeDialog receiver, removed with dialog) |
| Broadcast Receivers to KEEP | 10 |
| Packages to REMOVE entirely | 2 (`SyncOfferData/`, `tokenExpiryHandling/`) |
