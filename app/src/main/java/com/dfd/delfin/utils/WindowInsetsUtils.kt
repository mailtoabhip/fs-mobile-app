package com.dfd.delfin.utils

import android.os.Build
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Utility class to handle window insets for edge-to-edge display compatibility
 * Especially important for Android 15 (API 35) which enforces edge-to-edge by default
 */
object WindowInsetsUtils {

    /**
     * Apply system window insets to a view, respecting the system bars
     * This ensures content doesn't draw behind status bar or navigation bar
     */
    fun applySystemWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }
    }

    /**
     * Apply only top system window insets (for status bar)
     */
    fun applyTopSystemWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }
    }

    /**
     * Apply only bottom system window insets (for navigation bar)
     */
    fun applyBottomSystemWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }
    }

    /**
     * Apply horizontal system window insets (left and right)
     */
    fun applyHorizontalSystemWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right
            )
            insets
        }
    }

    /**
     * Check if the device is running Android 15 (API 35) or higher
     * where edge-to-edge is enforced by default
     */
    fun isEdgeToEdgeEnforced(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
    }
}
