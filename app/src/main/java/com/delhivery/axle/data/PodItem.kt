package com.delhivery.axle.data

import android.net.Uri

/**
 * Data model representing a single POD (Proof of Delivery) image item
 */
data class PodItem(
    val id: Int, // 1-10
    val state: PodState,
    val imageUri: Uri? = null,
    val imagePath: String? = null, // Local file path
    val imageUrl: String? = null // AWS URL after upload
)

/**
 * State of a POD item
 */
enum class PodState {
    EMPTY,           // Not yet visible
    AVAILABLE,       // Visible, ready for selection
    SELECTED,        // Image selected, showing thumbnail (not yet uploaded)
    UPLOADING,       // Currently uploading to server
    UPLOADED         // Successfully uploaded
}

