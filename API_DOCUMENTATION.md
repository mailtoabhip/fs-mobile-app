# AxleApp API Documentation

## Overview

This document provides comprehensive documentation for the API calls triggered in the AxleApp's three main sections: **Loads**, **Contracts**, and **MyBids**. It includes trigger mechanisms, API endpoints, request payloads, and call counts for each section.

## Table of Contents

1. [API Call Triggers](#api-call-triggers)
2. [Loads Section](#loads-section)
3. [Marketplace Loads Section](#marketplace-loads-section)
4. [Contracts Section](#contracts-section)
5. [MyBids Section](#mybids-section)
6. [Bid Management APIs](#bid-management-apis)
7. [Spot Bidding Service APIs](#spot-bidding-service-apis)
8. [Count Fetching APIs](#count-fetching-apis)
9. [API Call Counts Summary](#api-call-counts-summary)
10. [Request/Response Models](#requestresponse-models)

---

## API Call Triggers

### Common Trigger Points

All three sections share common trigger mechanisms:

- **Fragment Initialization**: When the fragment is first created (`onViewCreated`)
- **Pull-to-Refresh**: User swipes down to refresh data
- **Filter Changes**: User selects different filters (Intracity, Intercity, etc.)
- **Pagination**: User scrolls to bottom to load more data
- **Tab Switching**: User switches between different tabs (for MyBids)

### Implementation Details

- **ViewModel Pattern**: All API calls are managed through ViewModels
- **Reactive Programming**: Uses RxJava with `Single.zip()` for parallel API calls
- **LiveData**: Results are observed through LiveData for UI updates
- **Error Handling**: Comprehensive error handling with retry mechanisms

---

## Loads Section

### Overview
The Loads section displays available transportation loads categorized into:
- **Delhivery Intracity**: Local city deliveries
- **Delhivery Intercity**: Cross-city deliveries within Delhivery network
- **Non-Delhivery Intercity**: Cross-city deliveries outside Delhivery network

### API Call Flow

#### 1. Primary Load Fetch
**Method**: `HomeLoadsViewModel.fetchUserTransactions()`

**For Intracity Loads:**
```kotlin
transactionsRepository.fetchIntracityRecommTransactions(
    offset, demandType, vehicleTypes, excludeTruckTypes, 
    filterVehicleType, true, null, searchAfter
)
```

**For Intercity Loads:**
```kotlin
transactionsRepository.fetchRecommTransactions(
    offset, demandType, vehicleTypes, excludeTruckTypes, 
    filterVehicleType, true, searchAfter
)
```

#### 2. Parallel API Calls
After fetching loads, the following parallel calls are made:

```kotlin
Single.zip(
    bidsRepository.bidsForLoads(transactions),
    bidsRepository.bulkLowestBidsForLoads(transactions),
    // Additional count calls for different load types
)
```

### API Endpoints

#### 1. Recommendation Transactions (Delhivery Intercity + Non-Delhivery)
```
POST /get_sp_loads
```

**Request Payload:**
```json
{
  "sp_id": "string",                    // Supplier ID
  "limit": 25,                          // Number of records to fetch
  "offset": 0,                          // Pagination offset
  "demand_types": "Internal,Others",    // Load types (Internal/Others/Intracity)
  "truck_types": "string",              // Vehicle types filter
  "split_view_count": true,             // Whether to split view counts
  "loads_with_bid_active": true,        // Include loads with active bids
  "skip_self_bids": true,               // Skip user's own bids
  "search_after": {                     // Pagination cursor
    "creation_time": "timestamp",
    "transaction_id": "string"
  }
}
```

#### 2. Intracity Transactions (Delhivery Intracity)
```
POST /get_sp_intracity_loads
```

**Request Payload:**
```json
{
  "sp_id": "string",                    // Supplier ID
  "limit": 25,                          // Number of records to fetch
  "offset": 0,                          // Pagination offset
  "truck_types": "string",              // Vehicle types filter
  "only_count": false,                  // Whether to fetch only counts
  "loads_with_bid_active": true,        // Include loads with active bids
  "skip_self_bids": true,               // Skip user's own bids
  "search_after": {                     // Pagination cursor
    "creation_time": "timestamp",
    "transaction_id": "string"
  }
}
```

#### 3. Loadboard Transactions (Fallback)
```
GET /transactions/loadboard/
```

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `sp_id` | string | Supplier ID |
| `offset` | int | Pagination offset |
| `limit` | int | Number of records (default: 25) |
| `demand_types` | string | Load types (default: "orion") |
| `truck_types` | string | Vehicle types filter |
| `valid_loads_only` | string | Only valid loads (default: "yes") |
| `exclude_truck_types` | string | Excluded vehicle types |
| `filter_vehicle_type` | boolean | Apply vehicle type filter |
| `bidding_going_on` | boolean | Bidding status filter |
| `exclude_trip_ids` | string | Excluded trip IDs |
| `exclude_union_area` | boolean | Exclude union areas (default: true) |
| `loads_with_bid_active` | boolean | Include loads with active bids (default: true) |

---

## Marketplace Loads Section

### Overview
The Marketplace Loads section displays spot marketplace loads available for bidding. This is a new filter tab added to the Loads section that shows loads from the spot marketplace.

### Features
- **Marketplace Filter Tab**: New filter option alongside Intracity and Intercity tabs
- **Dynamic Count Display**: Shows real-time count of marketplace loads
- **Parallel Bid Fetching**: Fetches bids for marketplace loads in parallel
- **Payment Fields**: Automatically populates payment mode and advance percentage
- **Pagination Support**: Supports infinite scroll for loading more marketplace loads

### API Call Flow

#### 1. Marketplace Loads Fetch
**Method**: `HomeLoadsViewModel.fetchSpotMarketplaceLoads()`

```kotlin
transactionsRepository.fetchSpotMarketplaceTransactions(
    onlyCount = false, 
    limit = 20
)
```

#### 2. Parallel API Calls
After fetching marketplace loads, the following parallel calls are made:

```kotlin
Single.zip(
    bidsRepository.bidsForLoads(transactions),
    bidsRepository.bulkLowestBidsForLoads(transactions),
    BiFunction { bids, lowestBids -> Pair(bids, lowestBids) }
)
```

### API Endpoint

#### Spot Marketplace Transactions
```
GET /spot_marketplace/loads
```

**Service**: TransactionService  
**Base URL (UAT)**: `http://orion-transaction-api-uat.delhivery.com`  
**Base URL (Dev)**: `https://orion-transaction-api-dev.delhivery.com`  
**Base URL (Prod)**: `https://orion-transaction-api.delhivery.com`

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `only_count` | boolean | No | false | Whether to fetch only counts |
| `limit` | int | Yes | 20 | Number of records to fetch |

**Full URL Example (UAT):**
```
http://orion-transaction-api-uat.delhivery.com/spot_marketplace/loads?only_count=false&limit=20
```

**Response:**
```json
{
  "success": true,
  "data": {
    "transactions": [
      {
        "transaction_id": "string",
        "origin_city": "string",
        "destination_city": "string",
        "truck_type": "string",
        "load_weight": 1000,
        "target_price": 1500,
        "pickup_date": "timestamp",
        "delivery_date": "timestamp",
        "status": "string",
        "payment_mode": "Advance|ToPay|Credit",
        "advance_percentage": 20,
        "speed": "EXP|NEXP"
      }
    ],
    "total": 50,
    "offset": 0,
    "hasNext": true,
    "searchAfter": {
      "creation_time": "timestamp",
      "transaction_id": "string"
    },
    "loadPricePercent": 85,
    "more_loads": true
  }
}
```

### User Interface Implementation

#### Filter Tab Display
```kotlin
// In HomeLoadsFilterItemVH
binding.dlvMarketplaceToggle.text = "Market Place (${item.data.marketplaceCount})"
binding.dlvMarketplaceToggle.visibility = View.VISIBLE
binding.dlvMarketplaceToggle.isSelected = (item.data.filterType == "Marketplace")
```

#### Click Handler
```kotlin
// In HomeLoadsFragment
HomeLoadMarketplace -> {
    analyticsUtil.moEngageTrackEvent(
        "EVENT_LOAD_MARKETPLACE_CLICKED",
        mutableListOf(PROPERTY_USER_ID),
        mutableListOf(userPrefs.userId())
    )
    selectedLoadFilter = "Marketplace"
    viewModel.fetchSpotMarketplaceLoads(paginate = false, onlyCount = false, limit = 20)
}
```

### Data Flow

1. **User Action**: User clicks on "Market Place" filter tab
2. **Analytics**: Track marketplace filter click event
3. **ViewModel**: Call `fetchSpotMarketplaceLoads()`
4. **User Data Fetch**: Fetch user data if not available (for payment fields)
5. **Repository Call**: Make API call via `TransactionService.spotMarketplaceTransactions()`
6. **Network Request**: `GET http://orion-transaction-api-uat.delhivery.com/spot_marketplace/loads?only_count=false&limit=20`
7. **Response Processing**:
   - Parse marketplace loads
   - Fetch bids for loads in parallel
   - Populate payment fields from user data
   - Set lowest bid information
8. **UI Update**: Update RecyclerView with marketplace loads
9. **Filter Update**: Update filter tab with marketplace count

### Implementation Details

#### Service Layer
```kotlin
// TransactionService.kt
@GET("/spot_marketplace/loads")
fun spotMarketplaceTransactions(
    @Query("only_count") onlyCount: Boolean = false,
    @Query("limit") limit: Int
): Single<BaseResponse<TransactionsResponse>>
```

#### Repository Layer
```kotlin
// TransactionsRepository.kt
fun fetchSpotMarketplaceTransactions(onlyCount: Boolean = false, limit: Int = UserTripsLoadLimit) =
    transactionService.spotMarketplaceTransactions(
        onlyCount = onlyCount,
        limit = limit
    ).convertResponse()
```

#### ViewModel Layer
```kotlin
// HomeLoadsViewModel.kt
fun fetchSpotMarketplaceLoads(
    paginate: Boolean = false,
    onlyCount: Boolean = false,
    limit: Int = 20
) {
    // Reset pagination for new fetch
    if (!paginate) {
        offset = 0
        searchAfter = null
        hasMoreData = true
    }
    
    // Fetch user data first if not available
    if (user == null) {
        userRepository.getUser(false)
            .onBackground()
            .subscribe { userModel, error ->
                if (!error && userModel != null) {
                    this.user = userModel
                    fetchMarketplaceLoadsData(paginate, onlyCount, limit)
                }
            }
    } else {
        fetchMarketplaceLoadsData(paginate, onlyCount, limit)
    }
}
```

### Payment Information Display

The marketplace loads display payment information fetched from user's supplier details:

#### Payment Modes
- **Advance**: Partial payment upfront
- **ToPay**: Full payment on delivery
- **Credit**: Payment on credit terms

#### Advance Percentage Display
- Only shown for Advance payment mode
- Example: "Advance (20%)" - means 20% advance payment required

### Error Handling

#### Network Errors
- Display error message to user
- Retry option available
- Use cached data if available

#### Empty State
- Display "No marketplace loads available" message
- Show info banner for searching more loads

#### API Timeout
- 30 seconds connection timeout
- Automatic retry with exponential backoff

### Performance Optimizations

#### Parallel Processing
```kotlin
Single.zip(
    bidsRepository.bidsForLoads(loads),
    bidsRepository.bulkLowestBidsForLoads(loads)
) { bids, lowestBids ->
    // Process both results together
}
```

#### Firebase Performance Tracking
```kotlin
val mainTrace = Firebase.performance.newTrace("fetch_spot_marketplace_transactions")
mainTrace.start()
// API call
mainTrace.stop()
```

### Pagination

#### Infinite Scroll
- Automatically loads more data when user scrolls to bottom
- Uses `hasMoreData` flag to prevent unnecessary API calls
- Shows loading indicator during pagination

#### Pagination Parameters
- Initial load: `limit = 20`, `offset = 0`
- Subsequent loads: Increment offset by number of loaded items
- Stop condition: `hasMoreData = false`

### Analytics Events

#### Marketplace Click Event
```kotlin
analyticsUtil.moEngageTrackEvent(
    "EVENT_LOAD_MARKETPLACE_CLICKED",
    mutableListOf(PROPERTY_USER_ID),
    mutableListOf(userPrefs.userId())
)
```

#### Tracked Metrics
- Marketplace filter click count
- Marketplace loads fetch count
- Marketplace loads display count
- User engagement time on marketplace loads

### API Call Counts

#### Per Marketplace Load Fetch
- **Total API Calls**: 3 per marketplace load fetch
  - 1x `spotMarketplaceTransactions()` - Fetch marketplace loads
  - 1x `bidsForLoads()` - Fetch user's bids for these loads
  - 1x `bulkLowestBidsForLoads()` - Fetch lowest bids for comparison
- **Optional**: 1x `getUser()` - If user data not cached

#### Pagination
- Each pagination action triggers 3 additional API calls
- Total API calls for 3 pages: 9 calls (3 per page)

### Testing Checklist

#### Functional Testing
- [ ] Marketplace filter tab displays correctly
- [ ] Click on marketplace tab fetches marketplace loads
- [ ] Marketplace count displays correctly in filter tab
- [ ] Payment information displays correctly
- [ ] Bid information displays correctly
- [ ] Lowest bid information displays correctly
- [ ] Pagination works correctly
- [ ] Empty state displays when no loads available
- [ ] Error state displays on API failure

#### Network Testing
- [ ] Verify correct base URL is used (via Chucker)
- [ ] Verify query parameters are correct
- [ ] Test with slow network connection
- [ ] Test with no network connection
- [ ] Verify retry mechanism works

#### UI Testing
- [ ] Filter tab selection state updates correctly
- [ ] Other filter tabs remain functional
- [ ] Marketplace loads display with correct layout
- [ ] Loading indicator displays during fetch
- [ ] Refresh functionality works

#### Analytics Testing
- [ ] Marketplace click event is tracked
- [ ] Event includes correct user ID
- [ ] Firebase performance traces are recorded

### Migration Notes

#### From RecommendationService to TransactionService
- **Previous**: API was in `RecommendationService` with base URL `https://orion-recommendation-api-uat.delhivery.com`
- **Current**: API is in `TransactionService` with base URL `http://orion-transaction-api-uat.delhivery.com`
- **Reason**: Marketplace loads are transaction-based, should use Transaction Service

#### Breaking Changes
- None - This is a new feature, no breaking changes to existing functionality

#### Backward Compatibility
- Feature is additive - does not affect existing Intracity and Intercity filters
- Other filters continue to work as before

---

## Contracts Section

### Overview
The Contracts section displays available contract opportunities categorized into:
- **Express Contracts**: Fast delivery contracts (LH_FTL)
- **Non-Express Contracts**: Standard delivery contracts (FRC)
- **Intracity Contracts**: Local city contracts (INTRACITY)

### API Call Flow

#### 1. Primary Contract Fetch
**Method**: `HomeContractsViewModel.fetchUserTransactions()`

```kotlin
transactionsRepository.fetchContractsTransactions(
    offset, demandType, allActiveFetched, UserTripsLoadLimit,
    matchLanePrefOriginCities, isFlexible, includeFlexibleContracts,
    searchAfterCreationTime, searchAfterTransactionId
)
```

#### 2. Parallel API Calls
```kotlin
Single.zip(
    bidsRepository.bidsForLoads(transactions, true),  // contractBids = true
    bidsRepository.bulkLowestBidsForLoads(transactions),
    transactionsRepository.fetchContractsSummaryCount()
)
```

### API Endpoints

#### 1. Contract Transactions
```
GET /transactions/loadboard/contracts
```

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `sp_id` | string | Supplier ID |
| `offset` | int | Pagination offset |
| `limit` | int | Number of records (default: 25) |
| `demand_types` | string | Contract types - Use `Internal` for LH_FTL (Express), `Corporate` for FRC (Non-Express), `Intracity` for intracity contracts, or comma-separated values like `Internal,Corporate` for both intercity types |
| `all_active_fetched` | boolean | Whether all active contracts fetched |
| `match_lane_pref_origin_cities` | boolean | Match lane preference cities |
| `is_flexible` | boolean | Filter flexible contracts |
| `include_flexible_contracts` | boolean | Include flexible contracts |
| `search_after_creation_time` | string | Pagination cursor - creation time |
| `search_after_transaction_id` | string | Pagination cursor - transaction ID |
| `loads_with_bid_active` | boolean | Include contracts with active bids (default: true) |
| `skip_self_bids` | boolean | Skip user's own bids (default: true) |

#### 2. Contract Summary Count
```
GET /transactions/loadboard/contracts
```

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `only_contract_counts` | string | Fetch only counts (default: "yes") |
| `sp_id` | string | Supplier ID |
| `match_lane_pref_origin_cities` | boolean | Match lane preference cities (default: true) |
| `include_flexible_contracts` | boolean | Include flexible contracts (default: true) |
| `loads_with_bid_active` | boolean | Include contracts with active bids (default: true) |
| `skip_self_bids` | boolean | Skip user's own bids (default: true) |

---

## MyBids Section

### Overview
The MyBids section displays user's placed bids categorized into:
- **Active Bids**: Open bids awaiting response
- **Confirmed Bids**: Accepted bids
- **Lost Bids**: Rejected or cancelled bids
- **Contract Bids**: Bids placed on contracts

### API Call Flow

#### 1. Bids Summary Fetch
**Method**: `HomeBidsViewModel.fetchBidsSummary()`

```kotlin
bidsRepository.userBidsSummary()
```

#### 2. Bids Fetch
**Method**: `HomeBidsViewModel.fetchBids()`

```kotlin
bidsRepository.userBids(offset, statuses, pending, contract, onlyFrcBids)
```

#### 3. Parallel API Calls
```kotlin
Single.zip(
    transactionsRepository.bulkTransactions(bids),
    bidsRepository.bulkLowestBidsForTransactions(bids),
    bidsRepository.bidsForBulkLoads(bids)
)
```

### API Endpoints

#### 1. Bids Summary
```
GET /bids/summary
```

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `supplier_id` | string | User ID |
| `include_adhoc_intracity_bid` | boolean | Include adhoc intracity bids (default: true) |
| `include_all_bid_types` | boolean | Include all bid types (default: true) |

#### 2. User Bids by Status
```
GET /bids/
```

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `supplier_id` | string | User ID |
| `limit` | int | Number of records (default: 10) |
| `offset` | int | Pagination offset |
| `contract_bids` | boolean | Include contract bids |
| `bid_statuses` | string | Comma-separated bid statuses (open,accepted,rejected) |
| `confirmation_pending` | boolean | Filter confirmation pending bids |
| `only_frc_bids` | boolean | Only FRC bids |
| `include_adhoc_intracity_bid` | boolean | Include adhoc intracity bids (default: true) |
| `include_all_bid_types` | boolean | Include all bid types (default: true) |

#### 3. Bulk Transactions
```
GET /transactions/list/
```

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `transactions_ids` | string | Comma-separated transaction IDs |
| `include_adhoc_intracity` | boolean | Include adhoc intracity (default: true) |
| `skip_od_in_halts` | boolean | Skip origin-destination in halts (default: true) |

#### 4. Bulk Lowest Bids
```
GET /bids/lowest
```

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `transaction_id_list` | string | Comma-separated transaction IDs |

#### 5. Bids for Loads
```
GET /bids
```

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `supplier_id` | string | User ID |
| `transaction_ids` | string | Comma-separated transaction IDs |
| `contract_bids` | boolean | Include contract bids |

---

## Bid Management APIs

### Overview
These APIs handle bid creation, updates, and management operations.

### API Endpoints

#### 1. Create Bid
```
POST /bids/
```

**Request Payload:**
```json
{
  "transaction_id": "string",           // Transaction ID
  "supplier_id": "string",              // Supplier ID
  "supplier_name": "string",            // Supplier name
  "test_bid": false,                    // Whether this is a test bid
  "bidding_type": "PMT|FTL",            // Bidding type (PMT/FTL)
  "bid_price": 1000,                    // Bid amount
  "freight_cost": 1000,                 // Freight cost
  "originator": "axle-app",             // Originator (default: "axle-app")
  "expected_arrival_time_pickup": "string",     // Expected arrival time
  "expected_arrival_time_pickup_remark": "string", // Arrival time remark
  "tentative_trip_count": 1,            // Tentative trip count
  "vehicle_number": "string",           // Vehicle number
  "placement_days": "string"            // Placement days
}
```

#### 2. Update Bid
```
PATCH /bids/
```

**Request Payload:**
```json
{
  "transaction_id": "string",           // Transaction ID
  "supplier_id": "string",              // Supplier ID
  "bid_id": "string",                   // Bid ID to update
  "bidding_type": "PMT|FTL",            // Bidding type
  "bid_price": 1000,                    // Updated bid amount
  "freight_cost": 1000,                 // Updated freight cost
  "action": "bid_update",               // Action type (default: "bid_update")
  "expected_arrival_time_pickup": "string",     // Expected arrival time
  "expected_arrival_time_pickup_remark": "string", // Arrival time remark
  "tentative_trip_count": 1,            // Tentative trip count
  "vehicle_number": "string",           // Vehicle number
  "placement_days": "string"            // Placement days
}
```

#### 3. Bulk Bid Create
```
POST /bids/
```

**Request Payload:**
```json
{
  "bidding_type": "PMT",                // Bidding type
  "originator": "axle-app",             // Originator
  "supplier_id": "string",              // Supplier ID
  "unallocated_load": 0.0,              // Unallocated load amount
  "supplier_name": "string",            // Supplier name
  "transaction_id": "string",           // Transaction ID
  "vehicle_data": [                     // Vehicle data array
    {
      "vehicle_type": "string",         // Vehicle type
      "bid_amount": 1000,               // Bid amount for this vehicle
      "vehicle_count": 1                // Number of vehicles
    }
  ]
}
```

#### 4. Bulk Bid Update
```
PATCH /bids/
```

**Request Payload:**
```json
{
  "bidding_type": "PMT",                // Bidding type
  "originator": "string",               // Originator
  "supplier_id": "string",              // Supplier ID
  "unallocated_load": 0.0,              // Unallocated load amount
  "action": "bid_update",               // Action type
  "transaction_id": "string",           // Transaction ID
  "vehicle_data": [                     // Vehicle modification data
    {
      "bid_id": "string",               // Bid ID to modify
      "vehicle_type": "string",         // Vehicle type
      "bid_amount": 1000,               // Updated bid amount
      "vehicle_count": 1                // Updated vehicle count
    }
  ]
}
```

#### 5. Bulk Bid Remove
```
PATCH /bids/
```

**Request Payload:**
```json
{
  "bidding_type": "PMT",                // Bidding type
  "originator": "string",               // Originator
  "supplier_id": "string",              // Supplier ID
  "unallocated_load": 0.0,              // Unallocated load amount
  "action": "bid_update",               // Action type
  "sub_action": "remove",               // Sub action
  "transaction_id": "string",           // Transaction ID
  "bid_id": ["bid1", "bid2"]            // Array of bid IDs to remove
}
```

#### 6. Accept Bid
```
POST /trips/accept
```

**Request Payload:**
```json
{
  "action_code": "SUP",                 // Action code
  "action_sub_code": "CNF",             // Action sub code
  "transaction_id": "string",           // Transaction ID
  "supplier_id": "string",              // Supplier ID
  "supplier_name": "string",            // Supplier name
  "bidding_type": "PMT|FTL",            // Bidding type
  "bid_price": 1000,                    // Bid amount
  "originator": "axle-app",             // Originator
  "vehicle_no": "string",               // Vehicle number
  "driver_phone": "string",             // Driver phone number
  "driver_name": "string"               // Driver name
}
```

---

## Spot Bidding Service APIs

### Overview
These APIs handle marketplace-specific operations, particularly for initiating calls between vendors and shippers through a bridge number system. This service is specifically designed for the spot marketplace bidding feature.

### Service Configuration

**Service Name**: `SpotBiddingService`

**Base URLs**:
- **Production**: `https://orion-user-loadboard-api.delhivery.com/spot/bidding`
- **Development**: `https://orion-user-loadboard-api-dev.delhivery.com/spot/bidding`
- **UAT**: `https://orion-user-loadboard-api-uat.delhivery.com/spot/bidding`

**Service File**: `app/src/main/java/com/delhivery/axle/api/service/SpotBiddingService.kt`

**Repository**: `SpotBiddingRepository`

### API Endpoints

#### 1. Initiate Marketplace Call
```
POST /marketplace/initiate-call
```

This endpoint is used to obtain a bridge number for connecting vendors with shippers in the marketplace. Instead of directly sharing personal phone numbers, the system provides a temporary bridge number that expires after a certain time.

**Service**: SpotBiddingService  
**Base URL (UAT)**: `https://orion-user-loadboard-api-uat.delhivery.com/spot/bidding`  
**Full URL (UAT)**: `https://orion-user-loadboard-api-uat.delhivery.com/spot/bidding/marketplace/initiate-call`

**Request Payload:**
```json
{
  "source": "axle_marketplace",       // Source identifier (fixed value)
  "transaction_id": "string",         // Transaction ID from marketplace load
  "bid_id": "string"                  // Bid ID for the marketplace transaction
}
```

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `source` | string | Yes | Source of the call initiation. Default: "axle_marketplace" |
| `transaction_id` | string | Yes | Unique identifier of the marketplace transaction |
| `bid_id` | string | Yes | Unique identifier of the bid |

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "bridge_number": "+919876543210",    // Bridge phone number for calling
      "vendor": "vendor_name",              // Vendor identifier
      "expiry": 1699876543000               // Expiry timestamp in milliseconds
    }
  ]
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `success` | boolean | Indicates if the request was successful |
| `data` | array | Array containing bridge number details |
| `data[].bridge_number` | string | Temporary phone number to dial for connecting with shipper |
| `data[].vendor` | string | Vendor/shipper identifier |
| `data[].expiry` | number | Unix timestamp (milliseconds) when the bridge number expires |

**Success Response Example:**
```json
{
  "success": true,
  "data": [
    {
      "bridge_number": "+919876543210",
      "vendor": "shipper_abc_123",
      "expiry": 1699876543000
    }
  ]
}
```

**Error Response Example:**
```json
{
  "success": false,
  "data": null
}
```

### Implementation

#### Repository Layer
```kotlin
// SpotBiddingRepository.kt
@Singleton
class SpotBiddingRepository @Inject constructor(
    private val spotBiddingService: SpotBiddingService
) : BaseRepository() {

    fun initiateMarketplaceCall(
        transactionId: String,
        bidId: String,
        source: String = "axle_marketplace"
    ): Single<InitiateCallResponse> {
        val request = InitiateCallRequest(
            source = source,
            transactionId = transactionId,
            bidId = bidId
        )
        return spotBiddingService.initiateMarketplaceCall(request)
    }
}
```

#### Usage in ViewModel
```kotlin
class MarketPlaceBidDetailsViewModel @Inject constructor(
    private val spotBiddingRepository: SpotBiddingRepository
) : BaseViewModel() {
    
    val callInitiationLiveData = MutableLiveData<InitiateCallResponse>()
    val errorLiveData = MutableLiveData<String>()
    
    fun initiateMarketplaceCall(transactionId: String, bidId: String) {
        spotBiddingRepository.initiateMarketplaceCall(
            transactionId = transactionId,
            bidId = bidId
        )
        .onBackground()
        .subscribe({ response ->
            if (response.success && !response.data.isNullOrEmpty()) {
                callInitiationLiveData.postValue(response)
            } else {
                errorLiveData.postValue("Failed to initiate call")
            }
        }, { error ->
            errorLiveData.postValue(error.message ?: "Unknown error occurred")
        })
    }
}
```

#### Usage in Activity
```kotlin
class MarketPlaceBidDetailsActivity : BaseActivity() {
    
    private fun setupCallButton() {
        binding.btnCall.setOnClickListener {
            viewModel.initiateMarketplaceCall(
                transactionId = transaction.transactionId,
                bidId = bid.bidId
            )
        }
    }
    
    private fun observeCallInitiation() {
        viewModel.callInitiationLiveData.observe(this) { response ->
            response.data?.firstOrNull()?.let { bridgeData ->
                val bridgeNumber = bridgeData.bridgeNumber
                val expiry = bridgeData.expiry
                
                // Make call using bridge number
                makePhoneCall(bridgeNumber)
                
                // Show expiry info to user
                showExpiryInfo(expiry)
            }
        }
        
        viewModel.errorLiveData.observe(this) { error ->
            showError(error)
        }
    }
    
    private fun makePhoneCall(phoneNumber: String?) {
        phoneNumber?.let {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$it")
            }
            startActivity(intent)
        }
    }
}
```

### Use Cases

#### Primary Use Case: Marketplace Load Contact
1. **Scenario**: User views a marketplace load and wants to contact the shipper
2. **User Action**: Clicks "Call Shipper" button in MarketPlace Bid Details screen
3. **API Call**: `POST /marketplace/initiate-call` with transaction_id and bid_id
4. **Response**: Bridge number is received
5. **Action**: Phone dialer opens with bridge number
6. **Security**: Personal numbers are protected; call expires after set time

#### Integration Points
- **MarketPlace Bid Details Screen** (`MarketPlaceBidDetailsActivity.kt`)
  - Primary usage: Contact shipper button
  - Shows bridge number expiry time
  
- **Home Loads Fragment - Marketplace Tab** (`HomeLoadsRVAdapterVH.kt`)
  - Quick call action from load card
  - Shows call availability status

### Data Models

#### Request Model
```kotlin
// InitiateCallRequest.kt
data class InitiateCallRequest(
    @SerializedName("source")
    val source: String = "axle_marketplace",
    
    @SerializedName("transaction_id")
    val transactionId: String,
    
    @SerializedName("bid_id")
    val bidId: String
)
```

#### Response Models
```kotlin
// InitiateCallResponse.kt
data class InitiateCallResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: List<BridgeNumberData>?
)

data class BridgeNumberData(
    @SerializedName("bridge_number")
    val bridgeNumber: String?,
    
    @SerializedName("vendor")
    val vendor: String?,
    
    @SerializedName("expiry")
    val expiry: Long?
)
```

### Error Handling

#### Common Error Scenarios

1. **Invalid Transaction ID**
   - Response: `success: false`
   - Action: Show error message to user
   - Retry: Not recommended

2. **Invalid Bid ID**
   - Response: `success: false`
   - Action: Verify bid exists and belongs to transaction
   - Retry: Not recommended

3. **Bridge Number Unavailable**
   - Response: `success: true` but empty data
   - Action: Show "Unable to connect" message
   - Retry: Allow after 30 seconds

4. **Network Error**
   - Response: Exception thrown
   - Action: Show network error message
   - Retry: Automatic retry with exponential backoff

5. **Bridge Number Expired**
   - Detection: Compare current time with expiry timestamp
   - Action: Request new bridge number
   - Retry: Automatic

### Best Practices

#### Security
- Never store bridge numbers beyond their expiry time
- Clear bridge number data after call is made
- Do not display bridge numbers in logs

#### User Experience
- Show bridge number expiry countdown to user
- Provide clear feedback when bridge number expires
- Auto-refresh bridge number if needed before expiry

#### Performance
- Cache bridge numbers until expiry
- Prefetch bridge number when user enters details screen
- Use single instance for multiple call attempts within expiry window

### Analytics Events

Track the following events for monitoring and analysis:

```kotlin
// Bridge number request initiated
analyticsUtil.trackEvent(
    "EVENT_MARKETPLACE_CALL_INITIATED",
    mapOf(
        "transaction_id" to transactionId,
        "bid_id" to bidId,
        "source" to "axle_marketplace"
    )
)

// Bridge number received successfully
analyticsUtil.trackEvent(
    "EVENT_MARKETPLACE_CALL_SUCCESS",
    mapOf(
        "transaction_id" to transactionId,
        "bridge_number_expiry" to expiry
    )
)

// Call made using bridge number
analyticsUtil.trackEvent(
    "EVENT_MARKETPLACE_CALL_PLACED",
    mapOf(
        "transaction_id" to transactionId,
        "time_to_expiry" to (expiry - currentTime)
    )
)

// Bridge number expired
analyticsUtil.trackEvent(
    "EVENT_MARKETPLACE_BRIDGE_EXPIRED",
    mapOf(
        "transaction_id" to transactionId,
        "was_used" to wasCallPlaced
    )
)
```

### Testing

#### Unit Tests
```kotlin
@Test
fun `test initiate marketplace call success`() {
    val response = InitiateCallResponse(
        success = true,
        data = listOf(
            BridgeNumberData(
                bridgeNumber = "+919876543210",
                vendor = "test_vendor",
                expiry = System.currentTimeMillis() + 3600000
            )
        )
    )
    
    // Test response parsing
    assertThat(response.success).isTrue()
    assertThat(response.data).isNotEmpty()
    assertThat(response.data?.first()?.bridgeNumber).isNotNull()
}
```

#### Integration Tests
- Test with valid transaction_id and bid_id
- Test with invalid IDs
- Test network failure scenarios
- Test bridge number expiry handling

#### Manual Testing via Chucker
1. Run app in debug mode
2. Navigate to marketplace bid details
3. Click "Call Shipper"
4. Open Chucker notification
5. Verify request URL: `https://orion-user-loadboard-api-uat.delhivery.com/spot/bidding/marketplace/initiate-call`
6. Verify request body contains correct transaction_id and bid_id
7. Verify response contains bridge_number and expiry

### API Call Count

#### Per Call Initiation
- **Total API Calls**: 1 per initiation
- **Optional Retry**: 1 additional call if expired and user retries

---

## Count Fetching APIs

### Overview
This section documents the APIs used to fetch counts for each section and sub-section in the AxleApp. These counts are used to display badges, tabs, and summary information throughout the application.

### Loads Section Counts

#### 1. Intracity Loads Count
**Method**: `HomeLoadsViewModel.fetchIntracityCounts()`

**API Endpoint**: `POST /get_sp_intracity_loads`

**Request Payload:**
```json
{
  "sp_id": "string",
  "limit": 0,
  "offset": 0,
  "truck_types": "string",
  "only_count": true,
  "loads_with_bid_active": true,
  "skip_self_bids": true
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "total_count": 25,
    "available_count": 20,
    "bid_active_count": 5
  }
}
```

#### 2. Intercity Loads Count
**Method**: `HomeLoadsViewModel.fetchIntercityCounts()`

**API Endpoint**: `POST /get_sp_loads`

**Request Payload:**
```json
{
  "sp_id": "string",
  "limit": 0,
  "offset": 0,
  "demand_types": "Internal,Others",
  "truck_types": "string",
  "split_view_count": true,
  "loads_with_bid_active": true,
  "skip_self_bids": true,
  "only_count": true
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "delhivery_intercity_count": 15,
    "non_delhivery_intercity_count": 10,
    "total_intercity_count": 25
  }
}
```

#### 3. Loads Summary Count
**Method**: `HomeLoadsViewModel.fetchLoadsSummaryCount()`

**API Endpoint**: `GET /transactions/loadboard/`

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `sp_id` | string | Supplier ID |
| `only_count` | string | Fetch only counts (default: "yes") |
| `demand_types` | string | Load types (Internal/Others/Intracity) |
| `truck_types` | string | Vehicle types filter |
| `loads_with_bid_active` | boolean | Include loads with active bids (default: true) |
| `skip_self_bids` | boolean | Skip user's own bids (default: true) |

**Response:**
```json
{
  "success": true,
  "data": {
    "intracity_count": 25,
    "intercity_count": 40,
    "total_loads_count": 65,
    "available_loads": 60,
    "bid_active_loads": 5
  }
}
```

### Contracts Section Counts

#### 1. Contract Summary Count
**Method**: `HomeContractsViewModel.fetchContractsSummaryCount()`

**API Endpoint**: `GET /transactions/loadboard/contracts`

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `only_contract_counts` | string | Fetch only counts (default: "yes") |
| `sp_id` | string | Supplier ID |
| `demand_types` | string | Contract types - Use `Internal` for LH_FTL (Express), `Corporate` for FRC (Non-Express), `Intracity` for intracity contracts, or comma-separated values like `Internal,Corporate` for both intercity types |
| `match_lane_pref_origin_cities` | boolean | Match lane preference cities (default: true) |
| `include_flexible_contracts` | boolean | Include flexible contracts (default: true) |
| `loads_with_bid_active` | boolean | Include contracts with active bids (default: true) |
| `skip_self_bids` | boolean | Skip user's own bids (default: true) |

**Response:**
```json
{
  "success": true,
  "data": {
    "express_contracts_count": 12,
    "non_express_contracts_count": 8,
    "intracity_contracts_count": 5,
    "total_contracts_count": 25,
    "available_contracts": 20,
    "bid_active_contracts": 5
  }
}
```

#### 2. Contract Type Specific Counts
**Method**: `HomeContractsViewModel.fetchContractTypeCounts()`

**API Endpoint**: `GET /transactions/loadboard/contracts`

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `only_contract_counts` | string | Fetch only counts (default: "yes") |
| `sp_id` | string | Supplier ID |
| `demand_types` | string | Contract types - Use `Internal` for LH_FTL (Express), `Corporate` for FRC (Non-Express), `Intracity` for intracity contracts, or comma-separated values like `Internal,Corporate` for both intercity types |
| `contract_type` | string | Contract subtype (LH_FTL/FRC/INTRACITY) |

**Response:**
```json
{
  "success": true,
  "data": {
    "lh_ftl_count": 12,
    "frc_count": 8,
    "intracity_count": 5,
    "flexible_contracts_count": 3
  }
}
```

### MyBids Section Counts

#### 1. Bids Summary Count
**Method**: `HomeBidsViewModel.fetchBidsSummary()`

**API Endpoint**: `GET /bids/summary`

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `supplier_id` | string | User ID |
| `include_adhoc_intracity_bid` | boolean | Include adhoc intracity bids (default: true) |
| `include_all_bid_types` | boolean | Include all bid types (default: true) |

**Response:**
```json
{
  "success": true,
  "data": {
    "myBids": 15,
    "confirmedBids": 8,
    "lostBids": 4,
    "contractBids": 3,
    "activeBids": 7,
    "pendingBids": 3
  }
}
```

#### 2. Bid Status Specific Counts
**Method**: `HomeBidsViewModel.fetchBidStatusCounts()`

**API Endpoint**: `GET /bids/`

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `supplier_id` | string | User ID |
| `limit` | int | Number of records (default: 0 for count only) |
| `offset` | int | Pagination offset (default: 0) |
| `bid_statuses` | string | Comma-separated bid statuses (open,accepted,rejected) |
| `contract_bids` | boolean | Include contract bids |
| `confirmation_pending` | boolean | Filter confirmation pending bids |
| `only_frc_bids` | boolean | Only FRC bids |
| `count_only` | boolean | Return only count (default: true) |

**Response:**
```json
{
  "success": true,
  "data": {
    "open_bids_count": 7,
    "accepted_bids_count": 8,
    "rejected_bids_count": 4,
    "pending_confirmation_count": 3,
    "contract_bids_count": 3,
    "frc_bids_count": 5
  }
}
```

### Dashboard Summary Counts

#### 1. Overall Dashboard Count
**Method**: `HomeViewModel.fetchDashboardSummary()`

**API Endpoint**: `GET /dashboard/summary`

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `sp_id` | string | Supplier ID |
| `include_all_sections` | boolean | Include all sections (default: true) |

**Response:**
```json
{
  "success": true,
  "data": {
    "loads": {
      "intracity_count": 25,
      "intercity_count": 40,
      "total_loads_count": 65
    },
    "contracts": {
      "express_count": 12,
      "non_express_count": 8,
      "total_contracts_count": 20
    },
    "bids": {
      "active_bids_count": 7,
      "confirmed_bids_count": 8,
      "total_bids_count": 15
    },
    "notifications": {
      "unread_count": 3,
      "total_notifications": 10
    }
  }
}
```

### Count Fetching Implementation

#### Parallel Count Fetching
The app uses parallel API calls to fetch counts efficiently:

```kotlin
Single.zip(
    loadsRepository.fetchLoadsSummaryCount(),
    contractsRepository.fetchContractsSummaryCount(),
    bidsRepository.fetchBidsSummary()
) { loadsCount, contractsCount, bidsCount ->
    DashboardSummary(
        loadsCount = loadsCount,
        contractsCount = contractsCount,
        bidsCount = bidsCount
    )
}
```

#### Count Caching Strategy
- **Memory Cache**: Counts are cached in ViewModels for 5 minutes
- **Local Database**: Counts are stored in Room database for offline access
- **Refresh Triggers**: Counts are refreshed on:
  - App launch
  - Pull-to-refresh
  - Section navigation
  - Bid operations (create/update/accept)

#### Count Update Mechanisms
```kotlin
// Real-time count updates
fun updateCountsAfterBidOperation(bidOperation: BidOperation) {
    when (bidOperation) {
        is BidCreated -> {
            activeBidsCount++
            totalBidsCount++
        }
        is BidAccepted -> {
            activeBidsCount--
            confirmedBidsCount++
        }
        is BidRejected -> {
            activeBidsCount--
            lostBidsCount++
        }
    }
}
```

### Count Display in UI

#### Tab Badges
```kotlin
// Example: Display count in tab badge
binding.tabLayout.getTabAt(0)?.let { tab ->
    tab.text = "Loads (${loadsCount})"
}

binding.tabLayout.getTabAt(1)?.let { tab ->
    tab.text = "Contracts (${contractsCount})"
}

binding.tabLayout.getTabAt(2)?.let { tab ->
    tab.text = "My Bids (${bidsCount})"
}
```

#### Section Headers
```kotlin
// Example: Display count in section header
binding.loadsHeader.text = "Available Loads (${availableLoadsCount})"
binding.contractsHeader.text = "Open Contracts (${openContractsCount})"
binding.bidsHeader.text = "Active Bids (${activeBidsCount})"
```

---

## API Call Counts Summary

### Loads Section

#### Delhivery Intracity
- **Total API Calls**: 3-4 per load fetch
  - 1x `fetchIntracityRecommTransactions()`
  - 1x `bidsForLoads()`
  - 1x `bulkLowestBidsForLoads()`
  - 1x `fetchRecommTransactions()` (for intercity counts)

#### Delhivery Intercity
- **Total API Calls**: 3-4 per load fetch
  - 1x `fetchRecommTransactions()`
  - 1x `bidsForLoads()`
  - 1x `bulkLowestBidsForLoads()`
  - 1x `fetchIntracityRecommTransactions()` (for intracity counts)

#### Non-Delhivery Intercity
- **Total API Calls**: 3-4 per load fetch
  - 1x `fetchRecommTransactions()`
  - 1x `bidsForLoads()`
  - 1x `bulkLowestBidsForLoads()`
  - 1x `fetchIntracityRecommTransactions()` (for intracity counts)

#### Marketplace Loads
- **Total API Calls**: 3 per marketplace load fetch
  - 1x `spotMarketplaceTransactions()` - Fetch marketplace loads
  - 1x `bidsForLoads()` - Fetch user's bids for these loads
  - 1x `bulkLowestBidsForLoads()` - Fetch lowest bids for comparison
- **Optional**: 1x `getUser()` - If user data not cached
- **Pagination**: 3 additional API calls per page

### Contracts Section
- **Total API Calls**: 3-4 per contract fetch
  - 1x `fetchContractsTransactions()`
  - 1x `bidsForLoads()` (with `contractBids=true`)
  - 1x `bulkLowestBidsForLoads()`
  - 1x `fetchContractsSummaryCount()`

### MyBids Section
- **Total API Calls**: 4-5 per bid fetch
  - 1x `fetchBidsSummary()`
  - 1x `userBids()`
  - 1x `bulkTransactions()`
  - 1x `bulkLowestBidsForTransactions()`
  - 1x `bidsForBulkLoads()`

### Bid Management Operations
- **Create Bid**: 2 API calls (create + fetch updated bid)
- **Update Bid**: 2 API calls (update + fetch updated bid)
- **Bulk Operations**: 1-3 API calls depending on operation type
- **Accept Bid**: 1 API call

### Count Fetching Operations
- **Loads Count**: 1-2 API calls per count fetch
- **Contracts Count**: 1-2 API calls per count fetch
- **Bids Count**: 1-2 API calls per count fetch
- **Dashboard Summary**: 3-4 parallel API calls

---

## Request/Response Models

### Common Response Structure
```json
{
  "success": true,
  "message": "string",
  "data": {
    // Response data varies by endpoint
  }
}
```

### Transaction Response
```json
{
  "transactions": [
    {
      "transaction_id": "string",
      "origin_city": "string",
      "destination_city": "string",
      "truck_type": "string",
      "load_weight": 1000,
      "bid_amount": 1000,
      "pickup_date": "timestamp",
      "delivery_date": "timestamp",
      "status": "string"
    }
  ],
  "total": 100,
  "offset": 0,
  "hasNext": true,
  "searchAfter": {
    "creation_time": "timestamp",
    "transaction_id": "string"
  }
}
```

### Bid Response
```json
{
  "bids": [
    {
      "bid_id": "string",
      "transaction_id": "string",
      "supplier_id": "string",
      "bid_amount": 1000,
      "status": "open|accepted|rejected",
      "created_at": "timestamp",
      "vehicle_type": "string"
    }
  ],
  "totalBids": 10
}
```

### Bid Summary Response
```json
{
  "myBids": 5,
  "confirmedBids": 2,
  "lostBids": 1,
  "contractBids": 3
}
```

---

## Performance Optimizations

### Parallel API Calls
The app uses `Single.zip()` to make parallel API calls, reducing total loading time:

```kotlin
Single.zip(
    apiCall1(),
    apiCall2(),
    apiCall3()
) { result1, result2, result3 ->
    // Process results
}
```

### Pagination
All list endpoints support pagination using:
- **Offset-based**: Traditional offset/limit pagination
- **Cursor-based**: Using `searchAfter` for better performance

### Caching
- **Local Database**: Offline data caching using Room database
- **Memory Caching**: ViewModel-level caching for frequently accessed data

### Error Handling
- **Retry Logic**: Automatic retry for failed requests
- **Fallback APIs**: Alternative endpoints for critical data
- **Graceful Degradation**: Show cached data when APIs fail

---

## Security Considerations

### Authentication
- All API calls require valid authentication tokens
- Token refresh mechanism for expired sessions

### Data Validation
- Input validation on all request parameters
- Sanitization of user-provided data

### Rate Limiting
- API rate limiting to prevent abuse
- Exponential backoff for retry attempts

---

## Monitoring and Analytics

### Performance Tracking
- Firebase Performance Monitoring for API call durations
- Custom traces for critical user journeys

### Error Tracking
- Comprehensive error logging
- User impact analysis for API failures

### Usage Analytics
- API usage patterns
- Performance metrics by section

---

*This documentation is maintained as part of the AxleApp project and should be updated whenever API changes are made.*
