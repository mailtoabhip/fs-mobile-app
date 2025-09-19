# Delayed Placement Card Layout

## Overview
A production-ready Android XML layout for displaying delayed placement information with detailed card design, action buttons, and comprehensive accessibility support.

## Components

### Main Layout
- **File**: `app/src/main/res/layout/item_delayed_placement_card.xml`
- **Type**: ConstraintLayout-based card with data binding
- **Dimensions**: 328x208dp (as per Figma design)

### Reusable Components

#### 1. Client Badge
- **File**: `app/src/main/res/layout/component_client_badge.xml`
- **Purpose**: Displays client information with icon, text, and close button
- **Features**: Rounded background, icon support, dismissible

#### 2. Info Item
- **File**: `app/src/main/res/layout/component_info_item.xml`
- **Purpose**: Reusable component for displaying icon + text information
- **Usage**: Truck info, time info, vehicle info, driver info, phone info

### Data Classes

#### 1. DelayedPlacementData
- **File**: `app/src/main/java/com/delhivery/axle/data/placements/DelayedPlacementData.kt`
- **Purpose**: Main data class containing all placement information
- **Properties**:
  - `delayText`: Delay status (e.g., "Delayed by 2 hrs")
  - `routeText`: Route information (e.g., "Kochi, Kerala → Jaipur, Rajasthan")
  - `stopsText`: Number of stops
  - `clientBadge`: Client badge data
  - `truckInfo`, `timeInfo`, `vehicleInfo`, `driverInfo`, `phoneInfo`: Information items

#### 2. ClientBadgeData
- **File**: `app/src/main/java/com/delhivery/axle/data/placements/ClientBadgeData.kt`
- **Purpose**: Data for client badge component
- **Properties**: `text`, `iconRes`, `showCloseButton`

#### 3. InfoItemData
- **File**: `app/src/main/java/com/delhivery/axle/data/placements/InfoItemData.kt`
- **Purpose**: Data for info item component
- **Properties**: `iconRes`, `text`, `iconContentDescription`, `textContentDescription`

### ViewHolder
- **File**: `app/src/main/java/com/delhivery/axle/ui/placements/DelayedPlacementViewHolder.kt`
- **Purpose**: RecyclerView ViewHolder with click handling
- **Features**: Action button click listeners, data binding

## Design Features

### Layout Structure
1. **Header Section**: Delay status + Client badge
2. **Divider**: Visual separation
3. **Content Section**: Route info + Details grid
4. **Action Buttons**: Fill Details, Share Details, Call

### Visual Elements
- **Card Design**: Rounded corners, elevation, ripple effect
- **Typography**: IBM Plex Sans font family (12sp, 14sp)
- **Colors**: Consistent with app theme
- **Icons**: Vector drawables for all icons
- **Spacing**: 8dp, 16dp, 24dp margins and padding

### Accessibility
- **Content Descriptions**: All interactive elements
- **Clickable Areas**: Proper touch targets
- **Screen Reader Support**: Semantic information
- **Focus Management**: Keyboard navigation support

## Usage

### In RecyclerView Adapter
```kotlin
class DelayedPlacementAdapter : RecyclerView.Adapter<DelayedPlacementViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DelayedPlacementViewHolder {
        val binding = ItemDelayedPlacementCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DelayedPlacementViewHolder(binding) { action, data ->
            // Handle action clicks
        }
    }
    
    override fun onBindViewHolder(holder: DelayedPlacementViewHolder, position: Int) {
        holder.bind(placementList[position])
    }
}
```

### Data Binding
```kotlin
// Set data in ViewHolder
binding.placementData = delayedPlacementData
binding.executePendingBindings()
```

## Resources

### Drawables
- `ic_bolt.xml`: Lightning bolt icon
- `ic_close.xml`: Close/X icon
- `ic_edit.xml`: Edit icon
- `ic_share.xml`: Share icon
- `ic_phone.xml`: Phone icon
- `ic_truck.xml`: Truck icon
- `ic_clock.xml`: Clock icon
- `ic_id_card.xml`: ID card icon
- `ic_user.xml`: User icon
- `bg_client_badge.xml`: Badge background
- `bg_dot.xml`: Dot background
- `bg_button_outline.xml`: Button outline background

### Colors
- `badge_background`: #F3F4F6
- `dot_color`: #6B7280
- `icon_color`: #374151
- `divider_color`: #E5E7EB
- `button_outline_color`: #D1D5DB
- `button_pressed_background`: #F9FAFB
- `button_disabled_background`: #F9FAFB
- `button_disabled_outline`: #E5E7EB

### Dimensions
- `text_size_12sp`: 12sp
- `text_size_14sp`: 14sp
- `size_2dp`: 2dp
- `size_6dp`: 6dp
- `size_10dp`: 10dp
- `size_32dp`: 32dp

### Strings
- All content descriptions and button texts
- Accessibility labels
- User-facing text

## Features

### Interactive Elements
1. **Fill Details Button**: Edit placement information
2. **Share Details Button**: Share placement details
3. **Call Button**: Contact driver
4. **Close Badge Button**: Dismiss client badge

### Responsive Design
- **ConstraintLayout**: Flexible layout system
- **Weight-based Distribution**: Equal button widths
- **Margin/Padding**: Consistent spacing
- **Text Handling**: Ellipsize for long text

### State Management
- **Pressed States**: Button press feedback
- **Disabled States**: Disabled button styling
- **Ripple Effects**: Material Design ripple
- **Focus States**: Keyboard navigation

## Customization

### Colors
Update colors in `app/src/main/res/values/colors.xml` to match your theme.

### Typography
Modify text sizes in `app/src/main/res/values/dimens.xml`.

### Icons
Replace vector drawables in `app/src/main/res/drawable/` with your preferred icons.

### Layout
Adjust margins, padding, and constraints in the XML layouts as needed.

## Testing

### Compilation
```bash
./gradlew compileDevelopmentDebugKotlin --no-daemon
```

### Layout Testing
- Test with different data sets
- Verify accessibility features
- Check responsive behavior
- Validate click handlers

## Dependencies

### Required
- AndroidX libraries
- Data Binding
- Material Design Components
- ConstraintLayout

### Optional
- Custom font resources
- Additional icon sets
- Animation libraries

## Notes

- All layouts use data binding for type safety
- Icons are vector drawables for scalability
- Colors and dimensions are resource-based
- Accessibility is built-in from the start
- Layout is optimized for performance
- Code follows Android best practices
