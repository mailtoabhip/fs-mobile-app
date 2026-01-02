package com.delhivery.axle.ui.home.fragments.bids

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.roundToInt

/**
 * Device display information including aspect ratio and screen classification
 */
data class DeviceDisplayInfo(
    val widthPx: Int,
    val heightPx: Int,
    val widthDp: Int,
    val heightDp: Int,
    val densityDpi: Int,
    val density: Float,
    val aspectRatio: Float,
    val isTablet: Boolean,
    val isSmallScreen: Boolean
)

/**
 * Get device display information including aspect ratio
 * This helps determine how to layout views across different device sizes
 */
fun Context.getDeviceDisplayInfo(): DeviceDisplayInfo {
    val displayMetrics = resources.displayMetrics
    val widthPx = displayMetrics.widthPixels
    val heightPx = displayMetrics.heightPixels
    
    val widthDp = (widthPx / displayMetrics.density).toInt()
    val heightDp = (heightPx / displayMetrics.density).toInt()
    
    val aspectRatio = widthPx.toFloat() / heightPx.toFloat()
    
    // Check if tablet (sw >= 600dp)
    val isTablet = resources.configuration.smallestScreenWidthDp >= 600
    
    // Check if small screen (width < 360dp)
    val isSmallScreen = widthDp < 360
    
    return DeviceDisplayInfo(
        widthPx = widthPx,
        heightPx = heightPx,
        widthDp = widthDp,
        heightDp = heightDp,
        densityDpi = displayMetrics.densityDpi,
        density = displayMetrics.density,
        aspectRatio = aspectRatio,
        isTablet = isTablet,
        isSmallScreen = isSmallScreen
    )
}

/**
 * Get available content width accounting for margins and padding
 * 
 * @param horizontalMargin Total horizontal margin in dp (left + right)
 * @param horizontalPadding Total horizontal padding in dp (left + right)
 * @return Available width in pixels
 */
fun View.getAvailableContentWidth(
    horizontalMargin: Int = 0,
    horizontalPadding: Int = 0
): Int {
    val displayInfo = context.getDeviceDisplayInfo()
    val marginPx = horizontalMargin.dpToPx(context)
    val paddingPx = horizontalPadding.dpToPx(context)
    return displayInfo.widthPx - marginPx - paddingPx
}

/**
 * Distribute width percentage among multiple views in a row
 * Prevents overlapping by assigning proportional widths
 * 
 * @param views List of views to distribute width to
 * @param percentages List of percentage values (should sum to ~1.0f or less)
 * @param spacing Total spacing between views in dp
 * @param horizontalMargin Horizontal margin of parent in dp
 */
fun View.distributeWidthProportionally(
    views: List<View>,
    percentages: List<Float>,
    spacing: Int = 8,
    horizontalMargin: Int = 16
) {
    require(views.size == percentages.size) {
        "Number of views must match number of percentages"
    }
    
    val displayInfo = context.getDeviceDisplayInfo()
    val marginPx = horizontalMargin.dpToPx(context)
    val spacingPx = spacing.dpToPx(context)
    
    // Calculate available width
    val totalAvailableWidth = displayInfo.widthPx - (marginPx * 2) - (spacingPx * (views.size - 1))
    
    views.forEachIndexed { index, view ->
        val widthPx = (totalAvailableWidth * percentages[index]).roundToInt()
        view.layoutParams = view.layoutParams?.apply {
            width = widthPx
        } ?: ViewGroup.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}

/**
 * Calculate optimal width percentage based on device screen size
 * Adjusts percentages for small screens vs tablets
 * 
 * @param itemCount Number of items in the row
 * @param equalDistribution Whether to distribute equally or use smart defaults
 * @return List of percentages for each item
 */
fun Context.calculateOptimalWidthPercentages(
    itemCount: Int,
    equalDistribution: Boolean = false
): List<Float> {
    val displayInfo = getDeviceDisplayInfo()
    
    return when {
        equalDistribution -> List(itemCount) { 1f / itemCount }
        
        // For small screens, adjust percentages to prevent overlap
        displayInfo.isSmallScreen -> when (itemCount) {
            2 -> listOf(0.55f, 0.45f) // Give more space to first item
            3 -> listOf(0.4f, 0.3f, 0.3f)
            else -> List(itemCount) { 1f / itemCount }
        }
        
        // For tablets, can use more balanced distribution
        displayInfo.isTablet -> when (itemCount) {
            2 -> listOf(0.5f, 0.5f)
            3 -> listOf(0.35f, 0.35f, 0.3f)
            else -> List(itemCount) { 1f / itemCount }
        }
        
        // For normal screens
        else -> when (itemCount) {
            2 -> listOf(0.5f, 0.5f)
            3 -> listOf(0.4f, 0.3f, 0.3f)
            else -> List(itemCount) { 1f / itemCount }
        }
    }
}

/**
 * Helper to convert dp to px
 */
fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).roundToInt()
}

/**
 * Helper to convert px to dp
 */
fun Int.pxToDp(context: Context): Int {
    return (this / context.resources.displayMetrics.density).roundToInt()
}

/**
 * Set max width based on percentage of screen width
 * Useful for preventing text views from taking too much space
 * 
 * @param percentage Percentage of screen width (0.0f to 1.0f)
 * @param horizontalMargin Total horizontal margin to account for in dp
 */
fun View.setMaxWidthPercent(percentage: Float, horizontalMargin: Int = 0) {
    val displayInfo = context.getDeviceDisplayInfo()
    val marginPx = horizontalMargin.dpToPx(context)
    val maxWidthPx = ((displayInfo.widthPx - marginPx) * percentage).roundToInt()
    
    // maxWidth is only available on TextView and its subclasses
    if (this is android.widget.TextView) {
        this.maxWidth = maxWidthPx
    }
}

/**
 * Set max width in pixels for TextView (type-safe extension)
 * 
 * @param maxWidthPx Maximum width in pixels
 * @param enableEllipsize Whether to enable ellipsis for overflow text
 */
fun android.widget.TextView.setMaxWidthSafe(maxWidthPx: Int, enableEllipsize: Boolean = true) {
    this.maxWidth = maxWidthPx
    if (enableEllipsize) {
        this.ellipsize = android.text.TextUtils.TruncateAt.END
        // Don't force maxLines - let XML or natural wrapping handle it
    }
}

/**
 * Set max width as percentage for TextView with optional ellipsis
 * 
 * @param percentage Percentage of available width (0.0f to 1.0f)
 * @param availableWidth Total available width in pixels
 * @param enableEllipsize Whether to enable ellipsis for overflow text
 */
fun android.widget.TextView.setMaxWidthPercent(
    percentage: Float, 
    availableWidth: Int,
    enableEllipsize: Boolean = true
) {
    val maxWidthPx = (availableWidth * percentage).toInt()
    setMaxWidthSafe(maxWidthPx, enableEllipsize)
}

/**
 * Apply constraint layout guidelines dynamically based on screen size
 * Adjusts guideline percentages for different device types
 * 
 * @param deviceInfo Device display information
 * @param leftPercentage Default left guideline percentage
 * @param rightPercentage Default right guideline percentage
 * @return Adjusted guideline percentages as Pair(left, right)
 */
fun ConstraintLayout.applyDynamicGuidelines(
    deviceInfo: DeviceDisplayInfo,
    leftPercentage: Float = 0.4f,
    rightPercentage: Float = 0.6f
): Pair<Float, Float> {
    return when {
        deviceInfo.isSmallScreen -> {
            // For small screens, give more space to left side
            Pair(0.45f, 0.55f)
        }
        deviceInfo.isTablet -> {
            // For tablets, use default or slightly adjusted
            Pair(leftPercentage, rightPercentage)
        }
        else -> {
            // For normal screens
            Pair(leftPercentage, rightPercentage)
        }
    }
}

/**
 * Calculate available width for card content
 * Accounts for card margins and content padding
 * 
 * @param cardMarginDp Card horizontal margin in dp (typically 12dp each side)
 * @param contentPaddingDp Content horizontal padding in dp (typically 16dp each side)
 * @return Available width in pixels
 */
fun Context.calculateAvailableCardWidth(
    cardMarginDp: Int = 12,
    contentPaddingDp: Int = 16
): Int {
    val displayInfo = getDeviceDisplayInfo()
    val cardMarginPx = (cardMarginDp * 2).dpToPx(this)
    val contentPaddingPx = (contentPaddingDp * 2).dpToPx(this)
    return displayInfo.widthPx - cardMarginPx - contentPaddingPx
}

