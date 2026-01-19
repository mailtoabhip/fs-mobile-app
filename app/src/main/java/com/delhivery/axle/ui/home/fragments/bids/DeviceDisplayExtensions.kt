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
 * Helper extension to convert sp to px
 */
fun Float.spToPx(context: Context): Float {
    return android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_SP,
        this,
        context.resources.displayMetrics
    )
}

/**
 * Dynamically adjusts text size of multiple TextViews to fit within available width
 * while maintaining relative size hierarchy
 * Measures actual text content and scales down proportionally if needed
 * 
 * IMPORTANT: This function handles unmeasured views by forcing measurement if needed
 * 
 * @param views List of TextViews to adjust
 * @param textSizes List of desired text sizes for each view (in sp)
 * @param availableWidth Total available width in pixels
 * @param minTextSize Minimum allowed text size (in sp)
 * @param spacingBetweenViews List of spacing between each pair of views in pixels (size must be views.size - 1)
 * @param drawablePadding Padding for drawables in pixels
 */
fun adjustTextSizeToFit(
    views: List<android.widget.TextView>,
    textSizes: List<Float>,
    availableWidth: Int,
    minTextSize: Float,
    spacingBetweenViews: List<Int>,
    drawablePadding: Int
) {
    require(views.size == textSizes.size) {
        "Number of views (${views.size}) must match number of text sizes (${textSizes.size})"
    }
    
    require(spacingBetweenViews.size == views.size - 1 || spacingBetweenViews.isEmpty()) {
        "Spacing list size (${spacingBetweenViews.size}) must be views.size - 1 (${views.size - 1}) or empty"
    }
    
    if (views.isEmpty() || availableWidth <= 0) return
    
    // IMPORTANT: Force measure if views haven't been measured yet
    views.forEach { view ->
        if (view.width == 0) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
        }
    }
    
    // First, set each view to its intended text size
    views.forEachIndexed { index, view ->
        view.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, textSizes[index])
    }
    
    // Measure total width needed with original sizes
    var totalWidthNeeded = 0f
    
    views.forEachIndexed { index, textView ->
        val paint = android.text.TextPaint().apply {
            isAntiAlias = true
            typeface = textView.typeface
            textSize = textSizes[index].spToPx(textView.context)
        }
        
        // Measure text width
        val text = textView.text?.toString() ?: ""
        val textWidth = paint.measureText(text)
        
        // Account for drawable (icon) width if present
        val drawableWidth = textView.compoundDrawables[0]?.intrinsicWidth ?: 0
        val drawableSpace = if (drawableWidth > 0) drawableWidth + drawablePadding else 0
        
        // Account for padding
        val paddingSpace = textView.paddingStart + textView.paddingEnd
        
        totalWidthNeeded += textWidth + drawableSpace + paddingSpace
        
        // Add variable spacing between views (except for last view)
        if (index < views.size - 1 && spacingBetweenViews.isNotEmpty()) {
            totalWidthNeeded += spacingBetweenViews[index]
        }
    }
    
    // If total width exceeds available width, scale down proportionally
    if (totalWidthNeeded > availableWidth) {
        val scaleFactor = availableWidth / totalWidthNeeded
        
        // Apply scaled sizes to each view, maintaining hierarchy
        views.forEachIndexed { index, textView ->
            val scaledSize = (textSizes[index] * scaleFactor).coerceAtLeast(minTextSize)
            textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledSize)
        }
    }
    
    // Request layout update
    views.forEach { it.requestLayout() }
}

/**
 * Convenience overload: adjusts text size with uniform spacing between all views
 * Maintains backward compatibility with existing code
 * 
 * @param views List of TextViews to adjust
 * @param textSizes List of desired text sizes for each view (in sp)
 * @param availableWidth Total available width in pixels
 * @param minTextSize Minimum allowed text size (in sp)
 * @param spacingBetweenViews Uniform spacing between views in pixels
 * @param drawablePadding Padding for drawables in pixels
 */
fun adjustTextSizeToFit(
    views: List<android.widget.TextView>,
    textSizes: List<Float>,
    availableWidth: Int,
    minTextSize: Float,
    spacingBetweenViews: Int,
    drawablePadding: Int
) {
    // Convert uniform spacing to list of same spacing for all gaps
    val spacingList = if (views.size > 1) List(views.size - 1) { spacingBetweenViews } else emptyList()
    
    // Call the more flexible version
    adjustTextSizeToFit(views, textSizes, availableWidth, minTextSize, spacingList, drawablePadding)
}

/**
 * Convenience overload: adjusts text size with same size for all views and uniform spacing
 * Maintains backward compatibility with existing code
 * 
 * @param views List of TextViews to adjust
 * @param availableWidth Total available width in pixels
 * @param defaultTextSize Desired text size for all views (in sp)
 * @param minTextSize Minimum allowed text size (in sp)
 * @param spacingBetweenViews Uniform spacing between views in pixels
 * @param drawablePadding Padding for drawables in pixels
 */
fun adjustTextSizeToFit(
    views: List<android.widget.TextView>,
    availableWidth: Int,
    defaultTextSize: Float,
    minTextSize: Float,
    spacingBetweenViews: Int,
    drawablePadding: Int
) {
    // Convert single size to list of same size for all views
    val textSizes = List(views.size) { defaultTextSize }
    
    // Call the more flexible version
    adjustTextSizeToFit(views, textSizes, availableWidth, minTextSize, spacingBetweenViews, drawablePadding)
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

