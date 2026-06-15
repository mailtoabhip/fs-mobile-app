package com.dfd.delfin.utils.extensions

import android.content.Intent

/**
 * MIME type constants for common file types
 */
object MimeTypes {
    const val IMAGE_ALL = "image/*"
    const val IMAGE_JPEG = "image/jpeg"
    const val IMAGE_PNG = "image/png"
    const val IMAGE_JPG = "image/jpg"
    const val PDF = "application/pdf"
    const val ALL = "*/*"
}

/**
 * Creates an Intent for picking files with specified MIME types.
 *
 * @param mimeTypes Vararg of MIME type strings (e.g., "image/*", "application/pdf")
 *                  If empty, defaults to all file types ("*/*")
 * @return Intent configured for file picking with ACTION_GET_CONTENT
 *
 * Example usage:
 * ```
 * // Pick any image
 * val intent = filePickerIntent(MimeTypes.IMAGE_ALL)
 *
 * // Pick JPEG or PNG images
 * val intent = filePickerIntent(MimeTypes.IMAGE_JPEG, MimeTypes.IMAGE_PNG)
 *
 * // Pick images or PDFs
 * val intent = filePickerIntent(MimeTypes.IMAGE_ALL, MimeTypes.PDF)
 * ```
 */
private fun filePickerIntent(vararg mimeTypes: String): Intent {
    return Intent(Intent.ACTION_GET_CONTENT).apply {
        // Set primary type - use first mime type or default to all
        type = mimeTypes.firstOrNull() ?: MimeTypes.ALL
        
        addCategory(Intent.CATEGORY_OPENABLE)
        
        // If multiple MIME types provided, add them as extra
        if (mimeTypes.size > 1) {
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        } else if (mimeTypes.size == 1) {
            // Even for single type, add as array for consistency
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(mimeTypes[0]))
        }
    }
}

/**
 * Creates a chooser Intent for picking files with specified MIME types.
 * Extension function on Context for convenient access from Activities, Fragments, etc.
 *
 * @param title Title to display in the chooser dialog
 * @param mimeTypes Vararg of MIME type strings
 * @return Chooser Intent wrapping the file picker intent
 *
 * Example usage:
 * ```
 * // In Activity or Fragment
 * val chooserIntent = filePickerChooser("Select Image", MimeTypes.IMAGE_ALL)
 * launcher.launch(chooserIntent)
 * ```
 */
fun filePickerChooser(title: String, vararg mimeTypes: String): Intent {
    val pickerIntent = filePickerIntent(*mimeTypes)
    return Intent.createChooser(pickerIntent, title)
}
