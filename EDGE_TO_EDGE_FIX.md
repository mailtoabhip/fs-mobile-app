# Edge-to-Edge Display Fix for Android 15 (API 35)

## Problem Description
After migrating to target API 35, the app's header and footer content is overflowing into the system UI areas (status bar and navigation bar). This is due to Android 15's new edge-to-edge display enforcement.

## Root Cause
- Android 15 (API 35) enforces edge-to-edge display by default
- Apps now draw behind system bars (status bar and navigation bar)
- The existing layout doesn't account for system window insets
- Content flows into system UI areas causing visual issues

## Solution Implemented

### 1. BaseActivity Updates
- Added `WindowCompat.setDecorFitsSystemWindows(window, false)` for API 35+
- This enables proper edge-to-edge handling

### 2. WindowInsetsUtils Utility Class
Created a utility class (`WindowInsetsUtils.kt`) with methods to:
- Apply system window insets to views
- Handle top insets (status bar)
- Handle bottom insets (navigation bar)
- Check if edge-to-edge is enforced (API 35+)

### 3. Layout Updates
Updated key activity layouts to include:
- `android:fitsSystemWindows="true"` on root containers
- `android:fitsSystemWindows="true"` on toolbars and bottom navigation

### 4. HomeActivity Updates
- Added window insets handling for toolbar and bottom navigation
- Applied top insets to toolbar
- Applied bottom insets to bottom navigation

## Files Modified

### Core Files:
1. `app/src/main/java/com/delhivery/axle/ui/base/BaseActivity.kt`
2. `app/src/main/java/com/delhivery/axle/ui/home/activity/home/HomeActivity.kt`
3. `app/src/main/java/com/delhivery/axle/utils/WindowInsetsUtils.kt` (new)

### Layout Files:
1. `app/src/main/res/layout/activity_home.xml`
2. `app/src/main/res/layout/activity_truck.xml`
3. `app/src/main/res/layout/activity_consolidated_page.xml`

## Testing
- Test on Android 15 (API 35) devices
- Verify content doesn't overflow into system UI
- Check that toolbars and bottom navigation are properly positioned
- Ensure content is fully visible and accessible

## Additional Recommendations

### For Other Activities:
Apply similar fixes to other activities that have toolbars or bottom navigation:
1. Add `android:fitsSystemWindows="true"` to root containers
2. Add `android:fitsSystemWindows="true"` to toolbars
3. Use `WindowInsetsUtils` for programmatic insets handling if needed

### For Fragments:
If fragments have their own layouts with system UI elements, apply the same principles:
- Add `android:fitsSystemWindows="true"` to root containers
- Use `WindowInsetsUtils` for dynamic insets handling

## Benefits
- ✅ Fixes content overflow into system UI
- ✅ Maintains backward compatibility with older Android versions
- ✅ Provides reusable utility for future edge-to-edge handling
- ✅ Follows Android's recommended edge-to-edge implementation
- ✅ Improves user experience on Android 15+ devices

## Future Considerations
- Monitor for any additional edge-to-edge requirements in future Android versions
- Consider implementing more sophisticated insets handling for complex layouts
- Test thoroughly on various device sizes and orientations
