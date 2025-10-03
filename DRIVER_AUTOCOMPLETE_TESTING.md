# Driver Autocomplete Testing Guide

This guide explains how to test the driver name autocomplete functionality using mock data.

## Overview

The driver autocomplete feature has been enhanced with mock data support for testing purposes. This allows you to test the functionality without making actual API calls.

## Mock Data

### Available Mock Drivers by Vehicle Number

| Vehicle Number | Available Drivers |
|----------------|-------------------|
| MP09QT0001 | Rajesh Kumar, Amit Singh, Suresh Patel |
| MP09QT0002 | Ravi Sharma, Vikram Gupta, Anil Verma |
| MP09QT0003 | Raj Kumar, Manoj Tiwari, Deepak Yadav |
| DL01AB1234 | Sunil Kumar, Pradeep Singh, Ramesh Kumar |
| MH02CD5678 | Suresh Kumar, Amit Kumar, Rajesh Singh |
| Other | All 15 mock drivers |

### Mock Driver Data Structure

Each mock driver includes:
- **Driver Name**: Full name (e.g., "Rajesh Kumar")
- **Phone Number**: 10-digit mobile number (e.g., "9876543210")

## How to Enable Test Mode

### Method 1: In ContractDetailsActivity (Already Enabled)

The test mode is already enabled in `ContractDetailsActivity` for testing:

```kotlin
// In onCreate method
autoCompleteUtils.enableTestMode()
```

### Method 2: Programmatically

```kotlin
// Enable test mode
autoCompleteUtils.enableTestMode()

// Disable test mode (use real API)
autoCompleteUtils.disableTestMode()
```

## Testing the Autocomplete

### In ContractDetailsActivity

1. Navigate to the placement details section
2. Enter a vehicle number (e.g., "MP09QT0001")
3. Click on the driver name field
4. Start typing a driver name (e.g., "Raj")
5. You should see filtered suggestions from the mock data
6. Select a driver from the dropdown

### Using the Test Activity

A dedicated test activity has been created: `DriverAutocompleteTestActivity`

To use it:
1. Add the activity to your AndroidManifest.xml
2. Launch the activity
3. Use the test buttons to set different vehicle numbers
4. Type in the driver name field to see autocomplete suggestions

## Mock Data Features

### Filtering
- Driver names are filtered case-insensitively
- Partial matches work (e.g., typing "raj" will show "Rajesh Kumar" and "Raj Kumar")
- Real-time filtering as you type

### Vehicle-Specific Data
- Different vehicle numbers return different driver lists
- Simulates real-world scenario where drivers are associated with specific vehicles

### Network Simulation
- 500ms delay simulates network call
- Progress indicator shows during loading
- Error handling for edge cases

## Testing Scenarios

### 1. Basic Autocomplete
- Enter vehicle number: "MP09QT0001"
- Type driver name: "raj"
- Expected: Shows "Rajesh Kumar" and "Raj Kumar"

### 2. No Matches
- Enter vehicle number: "MP09QT0001"
- Type driver name: "xyz"
- Expected: No suggestions shown

### 3. Vehicle Change
- Enter vehicle number: "MP09QT0001"
- Type driver name: "raj"
- Change vehicle number to: "MP09QT0002"
- Expected: Driver list updates to new vehicle's drivers

### 4. Manual Typing
- Enter vehicle number: "MP09QT0001"
- Type driver name manually: "New Driver"
- Expected: Validates input and allows manual entry

## Production Deployment

**Important**: Remember to disable test mode before production:

```kotlin
// Remove or comment out this line in ContractDetailsActivity
// autoCompleteUtils.enableTestMode()
```

Or set it to false:

```kotlin
autoCompleteUtils.disableTestMode()
```

## Mock Data Customization

To add more mock data or modify existing data, edit the `MockDriverData.kt` file:

```kotlin
fun getMockDriverDataForVehicle(vehicleNumber: String): List<DriverDataResponse> {
    return when (vehicleNumber.uppercase()) {
        "YOUR_VEHICLE_NUMBER" -> listOf(
            DriverDataResponse("Driver Name", "Phone Number"),
            // Add more drivers...
        )
        // Add more vehicle numbers...
    }
}
```

## Troubleshooting

### No Suggestions Appearing
1. Check if test mode is enabled
2. Verify vehicle number is entered
3. Ensure driver name has at least 2 characters

### Wrong Driver List
1. Verify vehicle number matches mock data
2. Check if vehicle number is properly formatted

### Performance Issues
1. Mock data includes 500ms delay simulation
2. This is intentional to simulate network latency
3. Reduce delay in `loadMockDriversForVehicle` if needed

## Files Modified

- `AutoCompleteUtils.kt` - Added test mode support
- `MockDriverData.kt` - Mock data utility
- `DriverAutocompleteTestActivity.kt` - Test activity
- `ContractDetailsActivity.kt` - Enabled test mode
- `activity_driver_autocomplete_test.xml` - Test activity layout
