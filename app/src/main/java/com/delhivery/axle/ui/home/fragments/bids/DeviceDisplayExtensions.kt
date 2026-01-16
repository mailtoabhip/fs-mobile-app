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
 * Measures actual text content and scales down if needed so nothing gets truncated
 * 
 * IMPORTANT: This function now handles unmeasured views by forcing measurement if needed
 */
fun adjustTextSizeToFit(
    views: List<android.widget.TextView>,
    availableWidth: Int,
    defaultTextSize: Float,
    minTextSize: Float,
    spacingBetweenViews: Int,
    drawablePadding: Int
) {
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
    
    // First, set all views to default text size
    views.forEach { it.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, defaultTextSize) }
    
    // Create a Paint object and copy properties from first view
    val paint = android.text.TextPaint().apply {
        isAntiAlias = true
        textSize = defaultTextSize.spToPx(views[0].context)
    }
    
    // Measure total width needed with default text size
    var totalWidthNeeded = 0f
    
    views.forEachIndexed { index, textView ->
        // Copy typeface for accurate measurement
        paint.typeface = textView.typeface
        paint.textSize = defaultTextSize.spToPx(textView.context)
        
        // Measure text width
        val text = textView.text?.toString() ?: ""
        val textWidth = paint.measureText(text)
        
        // Account for drawable (icon) width if present
        val drawableWidth = textView.compoundDrawables[0]?.intrinsicWidth ?: 0
        val drawableSpace = if (drawableWidth > 0) drawableWidth + drawablePadding else 0
        
        // Account for padding
        val paddingSpace = textView.paddingStart + textView.paddingEnd
        
        totalWidthNeeded += textWidth + drawableSpace + paddingSpace
        
        // Add spacing between views (except for last view)
        if (index < views.size - 1) {
            totalWidthNeeded += spacingBetweenViews
        }
    }
    
    // If total width exceeds available width, scale down text size proportionally
    if (totalWidthNeeded > availableWidth) {
        val scaleFactor = availableWidth / totalWidthNeeded
        val newTextSize = (defaultTextSize * scaleFactor).coerceAtLeast(minTextSize)
        
        // Apply the scaled text size to all views
        views.forEach { textView ->
            textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, newTextSize)
        }
    }
    
    // Request layout update
    views.forEach { it.requestLayout() }
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

