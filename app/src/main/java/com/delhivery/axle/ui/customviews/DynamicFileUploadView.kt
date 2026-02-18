package com.delhivery.axle.ui.customviews

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.delhivery.axle.R
import com.delhivery.axle.api.response.FormField
import com.delhivery.axle.databinding.ViewDynamicFileUploadBinding

/**
 * Custom view for rendering FILE fields dynamically.
 * Two-state layout: upload card (label + UPLOAD button) or uploaded card (doc icon + label + filename + trash).
 */
class DynamicFileUploadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewDynamicFileUploadBinding
    private var fileSelectedListener: ((Uri) -> Unit)? = null
    private var fileRemovedListener: (() -> Unit)? = null
    private var currentFileUri: Uri? = null
    private var currentField: FormField? = null
    private var isAdditionalDocMode = false

    init {
        binding = ViewDynamicFileUploadBinding.inflate(LayoutInflater.from(context), this, true)
        setupClickListeners()
    }

    /**
     * Configure the view based on FormField configuration
     */
    fun setFieldConfig(field: FormField) {
        currentField = field

        // Set label on upload card
        binding.tvLabel.text = field.displayLabel

        // Set label on uploaded card (without mandatory indicator)
        binding.tvUploadedLabel.text = field.displayLabel

        // Set subtitle (help text)
        field.helpText?.let {
            binding.tvSubtitle.text = it
            binding.tvSubtitle.visibility = VISIBLE
        } ?: run {
            binding.tvSubtitle.visibility = GONE
        }
    }

    /**
     * Get current file URI
     */
    fun getFileUri(): Uri? = currentFileUri

    /**
     * Set file URI and show uploaded state
     */
    fun setFileUri(uri: Uri) {
        currentFileUri = uri
        showUploadedState(uri)
    }

    /**
     * Clear selected file and reset to upload state
     */
    fun clearFile() {
        currentFileUri = null
        showUploadState()
        clearError()
    }

    /**
     * Set error message
     */
    fun setError(message: String?) {
        if (message != null) {
            binding.tvError.text = message
            binding.tvError.visibility = VISIBLE
        } else {
            binding.tvError.visibility = GONE
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        binding.tvError.visibility = GONE
    }

    /**
     * Set listener for file selection
     */
    fun setOnFileSelectedListener(listener: (Uri) -> Unit) {
        fileSelectedListener = listener
    }

    /**
     * Set listener for file removal
     */
    fun setOnFileRemovedListener(listener: () -> Unit) {
        fileRemovedListener = listener
    }

    /**
     * Remove horizontal padding (for use in containers that already have padding)
     */
    fun removeHorizontalPadding() {
        binding.llRoot.setPadding(0, binding.llRoot.paddingTop, 0, binding.llRoot.paddingBottom)
    }

    /**
     * Set additional document mode - uses paperclip icon and shows "Uploaded" text
     */
    fun setAdditionalDocMode() {
        isAdditionalDocMode = true
        binding.ivDocIcon.setImageResource(R.drawable.ic_paperclip)
    }

    /**
     * Setup click listeners for upload card and delete button
     */
    private fun setupClickListeners() {
        // Upload card click opens file picker
        binding.cvUploadCard.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            fileSelectedListener?.invoke(Uri.EMPTY)
        }

        // Delete button clears file and notifies listener
        binding.ivDelete.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            clearFile()
            fileRemovedListener?.invoke()
        }
    }

    /**
     * Show upload state: upload card visible, uploaded card hidden
     */
    private fun showUploadState() {
        binding.cvUploadCard.visibility = VISIBLE
        binding.cvUploadedCard.visibility = GONE
    }

    /**
     * Show uploaded state: upload card hidden, uploaded card visible with filename
     */
    private fun showUploadedState(uri: Uri) {
        binding.cvUploadCard.visibility = GONE
        binding.cvUploadedCard.visibility = VISIBLE

        // Extract and display filename
        val filename = getFilenameFromUri(uri)
        
        if (isAdditionalDocMode) {
            // For additional docs: show filename as label, "Uploaded" as subtitle
            binding.tvUploadedLabel.text = filename
            binding.tvFilename.text = "Uploaded"
        } else {
            // For required docs: show field label, filename as subtitle
            binding.tvFilename.text = filename
        }
    }

    /**
     * Extract filename from URI
     */
    private fun getFilenameFromUri(uri: Uri): String {
        var filename = "uploaded_file"

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    filename = cursor.getString(nameIndex) ?: filename
                }
            }
        } catch (e: Exception) {
            // Fallback to last path segment
            uri.lastPathSegment?.let { filename = it }
        }

        return filename
    }
}
