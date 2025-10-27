# Delayed Placement Card Integration

## Overview
This document explains how to integrate the new delayed placement card design into the existing placement listing system.

## What Was Added

### 1. New Item Type
- **File**: `app/src/main/java/com/delhivery/axle/ui/home/fragments/placements/HomePlacementsRVAdapterItem.kt`
- **Addition**: `DelayedPlacement(11)` enum value
- **Addition**: `HomePlacementsDelayedItem` class

### 2. New ViewHolder
- **File**: `app/src/main/java/com/delhivery/axle/ui/home/fragments/placements/HomePlacementsRVAdapterVH.kt`
- **Addition**: `HomePlacementsDelayedItemVH` class
- **Features**: 
  - Uses the new card layout (`item_delayed_placement_card.xml`)
  - Converts `HomePlacementsItemData` to `DelayedPlacementData`
  - Handles click actions for buttons

### 3. Updated Adapter
- **File**: `app/src/main/java/com/delhivery/axle/ui/home/fragments/placements/HomePlacementsRVAdapter.kt`
- **Changes**:
  - Added binding creation for `DelayedPlacement` type
  - Added ViewHolder creation for `ItemDelayedPlacementCardBinding`
  - Added binding logic for `HomePlacementsDelayedItem`

### 4. Updated Interface
- **File**: `app/src/main/java/com/delhivery/axle/ui/home/fragments/placements/HomePlacementsRVAdapterInterface.kt`
- **Changes**: Added `DelayedPlacement` to the clickable item types

### 5. Data Mapping Utility
- **File**: `app/src/main/java/com/delhivery/axle/utils/PlacementDataMapper.kt`
- **Purpose**: Converts `HomePlacementsItemData` to `DelayedPlacementData`
- **Features**: Maps all fields with proper fallbacks

### 6. Helper Utility
- **File**: `app/src/main/java/com/delhivery/axle/utils/DelayedPlacementItemHelper.kt`
- **Purpose**: Provides easy integration methods
- **Features**: 
  - `createDelayedPlacementItem()` method
  - Backward compatibility with old item types
  - Usage examples

## How to Use

### Option 1: Use the Helper Utility (Recommended)

```kotlin
// In your ViewModel, replace the existing item creation:
// OLD WAY:
when(load.loadType){
    LoadTypes.ftlAdhoc.name->  add(Pair(HomePlacementsIntercityAdhocRequestItem(load), DataRVAdapterOperationType.Add))
    LoadTypes.ftlRegular.name->  add(Pair(HomePlacementsIntercityContractsRequestItem(load), DataRVAdapterOperationType.Add))
    LoadTypes.intracityRegular.name->  add(Pair(HomePlacementsIntracityContractsRequestItem(load), DataRVAdapterOperationType.Add))
    LoadTypes.intracityAdhoc.name->  add(Pair(HomePlacementsIntracityAdhocRequestItem(load), DataRVAdapterOperationType.Add))
}

// NEW WAY:
add(DelayedPlacementItemHelper.createDelayedPlacementItem(load, useNewCardDesign = true))
```

### Option 2: Direct Usage

```kotlin
// Create the item directly
val delayedItem = HomePlacementsDelayedItem(loadData)
add(Pair(delayedItem, DataRVAdapterOperationType.Add))
```

### Option 3: Gradual Migration

```kotlin
// Use a flag to control which design to use
val useNewCardDesign = true // Set based on your requirements

for (load in delayedPlacementList) {
    add(DelayedPlacementItemHelper.createDelayedPlacementItem(load, useNewCardDesign))
}
```

## Integration Points

### 1. In HomePlacementsViewModel.kt

Replace the existing item creation logic in the `fetchPlacementLoads` method:

```kotlin
// Current code (lines 124-131):
for (load in delayedPlacementList){
    when(load.loadType){
        LoadTypes.ftlAdhoc.name->  add(Pair(HomePlacementsIntercityAdhocRequestItem(load), DataRVAdapterOperationType.Add))
        LoadTypes.ftlRegular.name->  add(Pair(HomePlacementsIntercityContractsRequestItem(load), DataRVAdapterOperationType.Add))
        LoadTypes.intracityRegular.name->  add(Pair(HomePlacementsIntracityContractsRequestItem(load), DataRVAdapterOperationType.Add))
        LoadTypes.intracityAdhoc.name->  add(Pair(HomePlacementsIntracityAdhocRequestItem(load), DataRVAdapterOperationType.Add))
    }
}

// Replace with:
for (load in delayedPlacementList){
    add(DelayedPlacementItemHelper.createDelayedPlacementItem(load, useNewCardDesign = true))
}
```

### 2. In addItemBasisLoadTypeAndDuration method

Replace the item creation logic in the time-based sections:

```kotlin
// Current code (lines 239-244, 249-254, etc.):
for(load in _0_2hoursPlacementList){
    when(load.loadType){
        LoadTypes.ftlAdhoc.name->  add(Pair(HomePlacementsIntercityAdhocRequestItem(load), DataRVAdapterOperationType.Add))
        LoadTypes.ftlRegular.name->  add(Pair(HomePlacementsIntercityContractsRequestItem(load), DataRVAdapterOperationType.Add))
        LoadTypes.intracityRegular.name->  add(Pair(HomePlacementsIntracityContractsRequestItem(load), DataRVAdapterOperationType.Add))
        LoadTypes.intracityAdhoc.name->  add(Pair(HomePlacementsIntracityAdhocRequestItem(load), DataRVAdapterOperationType.Add))
    }
}

// Replace with:
for(load in _0_2hoursPlacementList){
    add(DelayedPlacementItemHelper.createDelayedPlacementItem(load, useNewCardDesign = true))
}
```

## Action Handling

The new card supports the following actions:

### 1. Fill Details
- **Action ID**: `"fill_details"`
- **Purpose**: Edit placement information
- **Handler**: Implement in your fragment/activity

### 2. Share Details
- **Action ID**: `"share_details"`
- **Purpose**: Share placement details
- **Handler**: Implement sharing functionality

### 3. Call
- **Action ID**: `"call"`
- **Purpose**: Contact driver
- **Handler**: Implement phone call functionality

### 4. Close Badge
- **Action ID**: `"close_badge"`
- **Purpose**: Dismiss client badge
- **Handler**: Hide or remove the badge

### Example Action Handler

```kotlin
override fun handleAction(actionId: String, item: BaseHomePlacementsRVAdapterItem<*>) {
    when (actionId) {
        "fill_details" -> {
            // Handle fill details
            val data = (item as HomePlacementsDelayedItem).data
            // Navigate to edit screen or show dialog
        }
        "share_details" -> {
            // Handle share details
            val data = (item as HomePlacementsDelayedItem).data
            // Implement sharing logic
        }
        "call" -> {
            // Handle call
            val data = (item as HomePlacementsDelayedItem).data
            // Make phone call
        }
        "close_badge" -> {
            // Handle badge dismissal
            // Update UI to hide badge
        }
        else -> {
            // Handle other actions
        }
    }
}
```

## Data Mapping

The `PlacementDataMapper` automatically converts `HomePlacementsItemData` to `DelayedPlacementData`:

### Mapped Fields:
- **Delay Text**: Uses `formatReportingTime()` method
- **Route Text**: Combines origin and destination with states
- **Stops Text**: Uses `haltStops()` method
- **Client Badge**: Uses `loadType()` method
- **Truck Info**: Combines vehicle type and number
- **Time Info**: Uses `formatReportingTime()` method
- **Vehicle Info**: Uses vehicle number
- **Driver Info**: Uses driver name
- **Phone Info**: Uses driver phone

### Fallback Values:
- Missing data shows "Not available"
- Empty strings show appropriate fallbacks
- Icons use default drawable resources

## Testing

### 1. Compilation Test
```bash
./gradlew compileDevelopmentDebugKotlin --no-daemon
```

### 2. Layout Testing
- Test with different data sets
- Verify all buttons work correctly
- Check accessibility features
- Test with missing data

### 3. Integration Testing
- Test with existing placement data
- Verify backward compatibility
- Test action handlers
- Check performance

## Benefits

### 1. Improved UI
- Modern card design
- Better visual hierarchy
- Consistent styling
- Enhanced accessibility

### 2. Better UX
- Clear action buttons
- Intuitive layout
- Responsive design
- Touch-friendly targets

### 3. Maintainability
- Reusable components
- Clean data mapping
- Easy to customize
- Well-documented code

### 4. Performance
- Efficient data binding
- Optimized layouts
- Minimal memory usage
- Fast rendering

## Migration Strategy

### Phase 1: Add New Components
- ✅ Add new item type and ViewHolder
- ✅ Update adapter and interface
- ✅ Create data mapping utilities

### Phase 2: Test Integration
- Test with sample data
- Verify all functionality
- Check performance
- Fix any issues

### Phase 3: Gradual Rollout
- Use feature flag to control usage
- A/B test with users
- Monitor performance metrics
- Gather feedback

### Phase 4: Full Migration
- Replace all old item types
- Remove old code
- Update documentation
- Train team members

## Troubleshooting

### Common Issues:

1. **Compilation Errors**
   - Check imports are correct
   - Verify binding classes exist
   - Ensure all dependencies are added

2. **Layout Issues**
   - Check drawable resources exist
   - Verify color resources are defined
   - Test on different screen sizes

3. **Data Mapping Issues**
   - Check field mappings in `PlacementDataMapper`
   - Verify fallback values
   - Test with edge cases

4. **Action Handling Issues**
   - Implement all required action handlers
   - Check action IDs match
   - Verify click listeners are set

## Support

For questions or issues:
1. Check this documentation
2. Review the code examples
3. Test with sample data
4. Contact the development team

## Future Enhancements

- Add animations
- Support for more action types
- Customizable layouts
- Theme support
- Performance optimizations
