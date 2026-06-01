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
import com.delhivery.axle.databinding.ActivityFastagAssignmentBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.UploadOptionsBottomSheetDialogFragment
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

class FastagAssignmentActivity : BaseActivity<ActivityFastagAssignmentBinding, FastagAssignmentViewModel>() {

    override fun getViewModelClass() = FastagAssignmentViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_assignment
    override fun requireConnection() = true

    @Inject
    lateinit var documentUtils: DocumentUtils
    @Inject
    lateinit var fileCompressor: FileCompressor

    private var rcFrontUploaded = false
    private var rcBackUploaded = false

    private var currentUploadTarget: UploadTarget = UploadTarget.RC_FRONT
    private var mPhotoFile: File? = null

    private enum class UploadTarget {
        RC_FRONT, RC_BACK
    }

    companion object {
        private const val EXTRA_VEHICLE_NUMBER = "extra_vehicle_number"
        private const val EXTRA_CHASSIS_NUMBER = "extra_chassis_number"

        fun newIntent(
            context: Context,
            vehicleNumber: String,
            chassisNumber: String
        ): Intent {
            return Intent(context, FastagAssignmentActivity::class.java).apply {
                putExtra(EXTRA_VEHICLE_NUMBER, vehicleNumber)
                putExtra(EXTRA_CHASSIS_NUMBER, chassisNumber)
            }
        }
    }

    // File picker launcher (same pattern as FastagDynamicDisputeFormActivity)
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
                val uri = Uri.fromFile(file)
                handleFileResult(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vehicleNumber = intent.getStringExtra(EXTRA_VEHICLE_NUMBER) ?: ""
        val chassisNumber = intent.getStringExtra(EXTRA_CHASSIS_NUMBER) ?: ""

        setupToolbar()
        setupVehicleInfo(vehicleNumber)
        setupUploadCards()
        updateContinueButton()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
            WindowInsetsUtils.applyBottomSystemWindowInsets(binding.btnContinue)
        }
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener { finish() }
    }

    private fun setupVehicleInfo(vehicleNumber: String) {
        binding.tvVehicleNumber.text = "Vehicle Number: $vehicleNumber"
    }

    private fun setupUploadCards() {
        binding.uploadRcFront.setTitle(getString(R.string.upload_rc_front))
        binding.uploadRcFront.setSubtitle(getString(R.string.upload_file_format_info))
        binding.uploadRcFront.setOnUploadClickListener {
            currentUploadTarget = UploadTarget.RC_FRONT
            showUploadOptionsBottomSheet(getString(R.string.upload_rc_front))
        }
        binding.uploadRcFront.setOnRemoveClickListener {
            binding.uploadRcFront.resetUploadState()
            rcFrontUploaded = false
            updateContinueButton()
        }

        binding.uploadRcBack.setTitle(getString(R.string.upload_rc_back))
        binding.uploadRcBack.setSubtitle(getString(R.string.upload_file_format_info))
        binding.uploadRcBack.setOnUploadClickListener {
            currentUploadTarget = UploadTarget.RC_BACK
            showUploadOptionsBottomSheet(getString(R.string.upload_rc_back))
        }
        binding.uploadRcBack.setOnRemoveClickListener {
            binding.uploadRcBack.resetUploadState()
            rcBackUploaded = false
            updateContinueButton()
        }

        binding.btnContinue.setOnClickListener {
            if (rcFrontUploaded && rcBackUploaded) {
                // TODO: Submit uploaded documents
                uiUtils.showSnackbar("Documents submitted successfully")
                finish()
            }
        }
    }

    private fun showUploadOptionsBottomSheet(title: String) {
        val bottomSheet = UploadOptionsBottomSheetDialogFragment.newInstance(
            title = title,
            onUploadFile = { requestFilePickerPermissions() },
            onTakePhoto = { requestCameraPermissions() }
        )
        bottomSheet.show(supportFragmentManager, "upload_options")
    }

    // ---- File Picker (same pattern as FastagDynamicDisputeFormActivity) ----

    private fun requestFilePickerPermissions() {
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

    // ---- Camera (same pattern as ShareRateActivity) ----

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
        val prefix = when (currentUploadTarget) {
            UploadTarget.RC_FRONT -> "rc_front_"
            UploadTarget.RC_BACK -> "rc_back_"
        }
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(prefix, ".jpg", storageDir)
    }

    // ---- Handle file result (same compression pattern as DynamicDispute) ----

    private fun handleFileResult(uri: Uri) {
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType) ?: "bin"

        val timestamp = System.currentTimeMillis()
        val outputFileName = when (currentUploadTarget) {
            UploadTarget.RC_FRONT -> "RC_Front_${timestamp}.jpg"
            UploadTarget.RC_BACK -> "RC_Back_${timestamp}.jpg"
        }

        if (extension.lowercase() == "png") {
            lifecycleScope.launch {
                try {
                    val destPath = File(cacheDir, "converted_${System.currentTimeMillis()}.jpg").absolutePath
                    val converted = documentUtils.convertPngToJpg(
                        sourcePath = documentUtils.getPathFromUri(
                            context = this@FastagAssignmentActivity,
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
                    context = this@FastagAssignmentActivity,
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
        when (currentUploadTarget) {
            UploadTarget.RC_FRONT -> {
                binding.uploadRcFront.setUploadedFile(file, "RC_Front.jpg")
                rcFrontUploaded = true
            }
            UploadTarget.RC_BACK -> {
                binding.uploadRcBack.setUploadedFile(file, "RC_Back.jpg")
                rcBackUploaded = true
            }
        }
        updateContinueButton()
    }

    private fun updateContinueButton() {
        val enabled = rcFrontUploaded && rcBackUploaded
        binding.btnContinue.isEnabled = enabled
        binding.btnContinue.alpha = if (enabled) 1.0f else 0.5f
    }
}
