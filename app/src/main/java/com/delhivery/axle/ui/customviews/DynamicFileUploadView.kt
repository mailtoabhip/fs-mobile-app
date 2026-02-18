package com.delhivery.axle.ui.customviews

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.delhivery.axle.api.response.FormField
import com.delhivery.axle.databinding.ViewDynamicFileUploadBinding

/**
 * Custom view for rendering FILE fields dynamically.
 * Single card layout: label + subtitle on left, UPLOAD button or thumbnail+cross on right.
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

    init {
        binding = ViewDynamicFileUploadBinding.inflate(LayoutInflater.from(context), this, true)
        setupClickListeners()
    }

    /**
     * Configure the view based on FormField configuration
     */
    fun setFieldConfig(field: FormField) {
        currentField = field

        // Set label with mandatory indicator
        val labelText = if (field.mandatory) {
            "${field.displayLabel} *"
        } else {
            field.displayLabel
        }
        binding.tvLabel.text = labelText

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
     * Set file URI and show thumbnail preview
     */
    fun setFileUri(uri: Uri) {
        currentFileUri = uri
        showFilePreview(uri)
    }

    /**
     * Clear selected file and reset to upload state
     */
    fun clearFile() {
        currentFileUri = null
        binding.tvUploadButton.visibility = VISIBLE
        binding.flFilePreview.visibility = GONE
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
     * Setup click listeners for upload card and remove button
     */
    private fun setupClickListeners() {
        // Card click opens file picker only when no file is selected
        binding.cvUploadCard.setOnClickListener {
            if (currentFileUri == null) {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                fileSelectedListener?.invoke(Uri.EMPTY)
            }
        }

        // Remove button clears file and notifies listener
        binding.ivRemove.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            clearFile()
            fileRemovedListener?.invoke()
        }
    }

    /**
     * Swap right side from UPLOAD button to thumbnail + cross
     */
    private fun showFilePreview(uri: Uri) {
        binding.tvUploadButton.visibility = GONE
        binding.flFilePreview.visibility = VISIBLE
        loadThumbnail(uri)
    }

    /**
     * Load thumbnail for image files, fallback to gallery icon
     */
    private fun loadThumbnail(uri: Uri) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    binding.ivThumbnail.setImageBitmap(bitmap)
                } else {
                    binding.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        } catch (e: Exception) {
            binding.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }
}
