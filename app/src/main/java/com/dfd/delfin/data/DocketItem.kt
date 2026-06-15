package com.dfd.delfin.data

import android.net.Uri

/**
 * Data model representing a single Docket image item
 */
data class DocketItem(
    val id: Int, // 1 for docket (can be extended to more in future)
    val state: DocketState,
    val imageUri: Uri? = null,
    val imagePath: String? = null, // Local file path
    val imageUrl: String? = null // AWS URL after upload
)

/**
 * State of a Docket item
 */
enum class DocketState {
    EMPTY,           // Not yet visible
    AVAILABLE,       // Visible, ready for selection
    SELECTED,        // Image selected, showing thumbnail (not yet uploaded)
    UPLOADING,       // Currently uploading to server
    UPLOADED         // Successfully uploaded
}

