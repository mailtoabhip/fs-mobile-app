package com.dfd.delfin.ui.common

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.dfd.delfin.R
import java.io.File

/**
 * Reusable upload card component with two visual states:
 *
 * **Default state**: Camera icon + title + subtitle + Upload button
 * **Uploaded state**: Thumbnail + filename + "Uploaded" status + file size + Re-upload/Remove buttons
 *
 * Usage in XML:
 * ```xml
 * <com.delhivery.axle.ui.common.UploadCardView
 *     android:id="@+id/uploadRcFront"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:uploadTitle="Upload RC Front"
 *     app:uploadSubtitle="JPEG • 10MB • PDF/Photo • 5MB" />
 * ```
 */
class UploadCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Default state views
    private val ivUploadIcon: ImageView
    private val tvTitle: TextView
    private val tvSubtitle: TextView
    private val tvSampleImage: TextView
    private val btnUpload: AppCompatButton

    // Uploaded state views
    private val ivThumbnail: ImageView
    private val tvFileName: TextView
    private val layoutStatus: LinearLayout
    private val layoutUploadedActions: LinearLayout
    private val btnReupload: AppCompatButton
    private val btnRemove: AppCompatButton

    private var onUploadClick: (() -> Unit)? = null
    private var onReuploadClick: (() -> Unit)? = null
    private var onRemoveClick: (() -> Unit)? = null
    private var onSampleImageClick: (() -> Unit)? = null
    private var uploadedFilePath: String? = null

    private var isUploaded = false

    init {
        LayoutInflater.from(context).inflate(R.layout.component_upload_card, this, true)

        // Default state
        ivUploadIcon = findViewById(R.id.ivUploadIcon)
        tvTitle = findViewById(R.id.tvUploadTitle)
        tvSubtitle = findViewById(R.id.tvUploadSubtitle)
        tvSampleImage = findViewById(R.id.tvSampleImage)
        btnUpload = findViewById(R.id.btnUpload)

        // Uploaded state
        ivThumbnail = findViewById(R.id.ivThumbnail)
        tvFileName = findViewById(R.id.tvFileName)
        layoutStatus = findViewById(R.id.layoutStatus)
        layoutUploadedActions = findViewById(R.id.layoutUploadedActions)
        btnReupload = findViewById(R.id.btnReupload)
        btnRemove = findViewById(R.id.btnRemove)

        btnUpload.setOnClickListener {
            onUploadClick?.invoke()
        }

        btnReupload.setOnClickListener {
            onReuploadClick?.invoke()
        }

        btnRemove.setOnClickListener {
            onRemoveClick?.invoke()
        }

        tvSampleImage.setOnClickListener {
            onSampleImageClick?.invoke()
        }

        ivThumbnail.setOnClickListener {
            uploadedFilePath?.let { path ->
                context.startActivity(ImagePreviewActivity.newIntent(context, filePath = path))
            }
        }

        // Read custom attributes
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.UploadCardView)
            val title = typedArray.getString(R.styleable.UploadCardView_uploadTitle)
            val subtitle = typedArray.getString(R.styleable.UploadCardView_uploadSubtitle)
            title?.let { t -> setTitle(t) }
            subtitle?.let { s -> setSubtitle(s) }
            typedArray.recycle()
        }
    }

    fun setTitle(title: String) {
        tvTitle.text = title
    }

    fun setSubtitle(subtitle: String) {
        tvSubtitle.text = subtitle
    }

    fun setOnUploadClickListener(listener: () -> Unit) {
        onUploadClick = listener
        onReuploadClick = listener
    }

    fun setOnRemoveClickListener(listener: () -> Unit) {
        onRemoveClick = listener
    }

    /**
     * Show the "Sample Image" link next to the title
     */
    fun showSampleImageLink(listener: () -> Unit) {
        tvSampleImage.visibility = View.VISIBLE
        onSampleImageClick = listener
    }

    /**
     * Set custom button text (e.g., "Take Photo" instead of "Upload")
     */
    fun setButtonText(text: String) {
        btnUpload.text = text
    }

    /**
     * Transition to uploaded state showing thumbnail, file name, and action buttons.
     */
    fun setUploadedFile(file: File, displayName: String? = null) {
        isUploaded = true
        uploadedFilePath = file.absolutePath

        // Hide default state
        ivUploadIcon.visibility = View.GONE
        tvTitle.visibility = View.GONE
        tvSubtitle.visibility = View.GONE
        tvSampleImage.visibility = View.GONE
        btnUpload.visibility = View.GONE

        // Show uploaded state
        ivThumbnail.visibility = View.VISIBLE
        tvFileName.visibility = View.VISIBLE
        layoutStatus.visibility = View.VISIBLE
        layoutUploadedActions.visibility = View.VISIBLE

        // Set data
        tvFileName.text = displayName ?: file.name

        // Load thumbnail with cache skip to handle re-uploads
        Glide.with(context)
            .load(file)
            .skipMemoryCache(true)
            .centerCrop()
            .into(ivThumbnail)
    }

    /**
     * Transition to uploaded state using a URI.
     */
    fun setUploadedFile(uri: Uri, fileName: String, fileSizeBytes: Long) {
        isUploaded = true

        // Hide default state
        ivUploadIcon.visibility = View.GONE
        tvTitle.visibility = View.GONE
        tvSubtitle.visibility = View.GONE
        tvSampleImage.visibility = View.GONE
        btnUpload.visibility = View.GONE

        // Show uploaded state
        ivThumbnail.visibility = View.VISIBLE
        tvFileName.visibility = View.VISIBLE
        layoutStatus.visibility = View.VISIBLE
        layoutUploadedActions.visibility = View.VISIBLE

        // Set data
        tvFileName.text = fileName

        // Load thumbnail
        Glide.with(context)
            .load(uri)
            .centerCrop()
            .into(ivThumbnail)
    }

    /**
     * Show the uploaded file name (legacy method for backward compatibility)
     */
    fun setUploadedFileName(fileName: String) {
        isUploaded = true

        // Hide default state
        ivUploadIcon.visibility = View.GONE
        tvTitle.visibility = View.GONE
        tvSubtitle.visibility = View.GONE
        tvSampleImage.visibility = View.GONE
        btnUpload.visibility = View.GONE

        // Show uploaded state
        ivThumbnail.visibility = View.VISIBLE
        tvFileName.visibility = View.VISIBLE
        layoutStatus.visibility = View.VISIBLE
        layoutUploadedActions.visibility = View.VISIBLE

        // Set data
        tvFileName.text = fileName
        ivThumbnail.setImageResource(R.drawable.ic_camera_upload)
        ivThumbnail.scaleType = ImageView.ScaleType.CENTER
    }

    /**
     * Reset to initial default upload state
     */
    fun resetUploadState() {
        isUploaded = false
        uploadedFilePath = null

        // Show default state
        ivUploadIcon.visibility = View.VISIBLE
        tvTitle.visibility = View.VISIBLE
        tvSubtitle.visibility = View.VISIBLE
        btnUpload.visibility = View.VISIBLE
        // tvSampleImage visibility is preserved (controlled by showSampleImageLink)

        // Hide uploaded state
        ivThumbnail.visibility = View.GONE
        tvFileName.visibility = View.GONE
        layoutStatus.visibility = View.GONE
        layoutUploadedActions.visibility = View.GONE
    }

    /**
     * Check if a file has been uploaded
     */
    fun hasUploadedFile(): Boolean {
        return isUploaded
    }
}
