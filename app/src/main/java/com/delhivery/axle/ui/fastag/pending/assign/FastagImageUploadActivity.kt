package com.delhivery.axle.ui.fastag.pending.assign

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityFastagImageUploadBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.UploadOptionsBottomSheetDialogFragment
import com.delhivery.axle.ui.dialogs.VehicleVerifiedBottomSheetDialogFragment
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.utils.DocumentUtils
import com.delhivery.axle.utils.FileCompressor
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.MimeTypes
import com.delhivery.axle.utils.extensions.filePickerChooser
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class FastagImageUploadActivity : BaseActivity<ActivityFastagImageUploadBinding, FastagImageUploadViewModel>() {

    override fun getViewModelClass() = FastagImageUploadViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_image_upload
    override fun requireConnection() = true

    @Inject
    lateinit var documentUtils: DocumentUtils
    @Inject
    lateinit var fileCompressor: FileCompressor

    private var fastagImageUploaded = false
    private var mPhotoFile: File? = null

    companion object {
        private const val EXTRA_VEHICLE_NUMBER = "extra_vehicle_number"

        fun newIntent(context: Context, vehicleNumber: String): Intent {
            return Intent(context, FastagImageUploadActivity::class.java).apply {
                putExtra(EXTRA_VEHICLE_NUMBER, vehicleNumber)
            }
        }
    }

    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                handleFileResult(uri)
            }
        }
    }

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            mPhotoFile?.let { file ->
                handleFileResult(Uri.fromFile(file))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vehicleNumber = intent.getStringExtra(EXTRA_VEHICLE_NUMBER) ?: ""

        setupToolbar()
        setupVehicleInfo(vehicleNumber)
        setupUploadCard()
        updateSubmitButton()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
            WindowInsetsUtils.applyBottomSystemWindowInsets(binding.btnSubmit)
        }
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener { finish() }
        binding.ivHelp.setOnClickListener {
            // TODO: Open help/support
        }
    }

    private fun setupVehicleInfo(vehicleNumber: String) {
        binding.tvVehicleNumber.text = "Vehicle Number: $vehicleNumber"
    }

    private fun setupUploadCard() {
        binding.uploadFastagImage.setOnUploadClickListener {
            showUploadOptionsBottomSheet()
        }
        binding.uploadFastagImage.setOnRemoveClickListener {
            binding.uploadFastagImage.resetUploadState()
            fastagImageUploaded = false
            updateSubmitButton()
        }

        binding.btnSubmit.setOnClickListener {
            if (fastagImageUploaded) {
                val vehicleNumber = intent.getStringExtra(EXTRA_VEHICLE_NUMBER) ?: ""
                showVehicleVerifiedBottomSheet(vehicleNumber)
            }
        }
    }

    private fun showUploadOptionsBottomSheet() {
        val bottomSheet = UploadOptionsBottomSheetDialogFragment.newInstance(
            title = getString(R.string.upload_fastag_image_label),
            onUploadFile = { launchFilePicker() },
            onTakePhoto = { requestCameraPermissions() }
        )
        bottomSheet.show(supportFragmentManager, "upload_options")
    }

    // ---- File Picker ----

    private fun launchFilePicker() {
        val chooserIntent = filePickerChooser(
            "Select file",
            MimeTypes.IMAGE_JPG,
            MimeTypes.IMAGE_JPEG
        )
        try {
            filePickerLauncher.launch(chooserIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "No file picker app found", Toast.LENGTH_SHORT).show()
        }
    }

    // ---- Camera ----

    private fun requestCameraPermissions() {
        compositeDisposable += requestPermission(
            arrayOf(Manifest.permission.CAMERA).apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                    plus(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        )
            .onBackground()
            .subscribe { granted, error ->
                if (error == null && granted) {
                    dispatchTakePictureIntent()
                } else {
                    uiUtils.showSnackbar(getString(R.string.storage_camera_permission))
                }
            }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            mPhotoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(
                this, BuildConfig.APPLICATION_ID + ".provider", mPhotoFile!!
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            cameraLauncher.launch(takePictureIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            uiUtils.showSnackbar(getString(R.string.msg_image_capture_failed))
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("fastag_image_", ".jpg", storageDir)
    }

    // ---- Handle file result ----

    private fun handleFileResult(uri: Uri) {
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType) ?: "bin"

        val timestamp = System.currentTimeMillis()
        val outputFileName = "FASTag_Image_${timestamp}.jpg"

        if (extension.lowercase() == "png") {
            lifecycleScope.launch {
                try {
                    val destPath = File(cacheDir, "converted_${timestamp}.jpg").absolutePath
                    val converted = documentUtils.convertPngToJpg(
                        sourcePath = documentUtils.getPathFromUri(
                            context = this@FastagImageUploadActivity,
                            uri = uri
                        ),
                        destPath = destPath
                    )
                    if (!converted) {
                        dialogUtils.showErrorDialog(
                            "Selected image is corrupted or unsupported. Please choose another image.",
                            3L
                        )
                        return@launch
                    }
                    val compressedFile = fileCompressor.compressToFile(
                        File(destPath), outputFileName
                    )
                    onFileReady(compressedFile)
                } catch (e: Exception) {
                    dialogUtils.showErrorDialog(
                        "Selected image is corrupted or unsupported. Please choose another image.",
                        3L
                    )
                }
            }
        } else {
            try {
                val filePath = documentUtils.getPathFromUri(
                    context = this@FastagImageUploadActivity,
                    uri = uri
                )
                val compressedFile = fileCompressor.compressToFile(
                    File(filePath), outputFileName
                )
                onFileReady(compressedFile)
            } catch (e: Exception) {
                dialogUtils.showErrorDialog(
                    "Selected image is corrupted or unsupported. Please choose another image.",
                    3L
                )
            }
        }
    }

    private fun onFileReady(file: File) {
        binding.uploadFastagImage.setUploadedFile(file, "FASTag_Image.jpg")
        fastagImageUploaded = true
        updateSubmitButton()
    }

    private fun updateSubmitButton() {
        binding.btnSubmit.isEnabled = fastagImageUploaded
        binding.btnSubmit.alpha = if (fastagImageUploaded) 1.0f else 0.5f
    }

    private fun showVehicleVerifiedBottomSheet(vehicleNumber: String) {
        val bottomSheet = VehicleVerifiedBottomSheetDialogFragment.newInstance(
            vehicleNumber = vehicleNumber,
            onGoToHomepage = {
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        )
        bottomSheet.show(supportFragmentManager, "vehicle_verified")
    }
}
