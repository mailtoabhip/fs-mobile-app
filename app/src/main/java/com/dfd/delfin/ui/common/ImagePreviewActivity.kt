package com.dfd.delfin.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.dfd.delfin.databinding.ActivityImagePreviewBinding
import java.io.File

/**
 * Common full-screen image preview screen.
 * Can display images from file path or URI.
 * Used for previewing RC, vehicle, and FASTag images.
 *
 * Usage:
 * ```kotlin
 * // From file path
 * startActivity(ImagePreviewActivity.newIntent(context, filePath = "/path/to/image.jpg"))
 *
 * // From URI
 * startActivity(ImagePreviewActivity.newIntent(context, uri = "content://..."))
 * ```
 */
class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImagePreviewBinding

    companion object {
        private const val EXTRA_FILE_PATH = "extra_file_path"
        private const val EXTRA_URI = "extra_uri"

        fun newIntent(context: Context, filePath: String? = null, uri: String? = null): Intent {
            return Intent(context, ImagePreviewActivity::class.java).apply {
                filePath?.let { putExtra(EXTRA_FILE_PATH, it) }
                uri?.let { putExtra(EXTRA_URI, it) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Edge-to-edge setup
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Apply top insets to the top bar so buttons don't sit under the status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutTopBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        loadImage()
        setupClickListeners()
    }

    private fun loadImage() {
        val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
        val uriString = intent.getStringExtra(EXTRA_URI)

        when {
            !filePath.isNullOrEmpty() -> {
                Glide.with(this)
                    .load(File(filePath))
                    .into(binding.ivPreviewImage)
            }
            !uriString.isNullOrEmpty() -> {
                Glide.with(this)
                    .load(Uri.parse(uriString))
                    .into(binding.ivPreviewImage)
            }
        }
    }

    private fun setupClickListeners() {
        binding.ivClose.setOnClickListener {
            finish()
        }
    }
}
