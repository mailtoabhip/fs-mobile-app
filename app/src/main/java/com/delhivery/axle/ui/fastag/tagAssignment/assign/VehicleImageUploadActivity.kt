package com.delhivery.axle.ui.fastag.tagAssignment.assign

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityVehicleImageUploadBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.SampleImageBottomSheetDialogFragment
import com.delhivery.axle.ui.fastag.tagAssignment.assign.kyv.FastagImageUploadActivity
import com.delhivery.axle.utils.DocumentUtils
import com.delhivery.axle.utils.FileCompressor
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class VehicleImageUploadActivity : BaseActivity<ActivityVehicleImageUploadBinding, VehicleImageUploadViewModel>() {

    override fun getViewModelClass() = VehicleImageUploadViewModel::class.java
    override fun layoutId() = R.layout.activity_vehicle_image_upload
    override fun requireConnection() = true

    @Inject
    lateinit var documentUtils: DocumentUtils
    @Inject
    lateinit var fileCompressor: FileCompressor

    private var vehicleFrontUploaded = false
    private var vehicleSideUploaded = false
    private var vehicleFrontFile: File? = null
    private var vehicleSideFile: File? = null

    private var currentUploadTarget: UploadTarget = UploadTarget.VEHICLE_FRONT
    private var mPhotoFile: File? = null

    private enum class UploadTarget {
        VEHICLE_FRONT, VEHICLE_SIDE
    }

    companion object {
        private const val EXTRA_VEHICLE_NUMBER = "extra_vehicle_number"
        private const val EXTRA_ORDER_ID = "extra_order_id"
        private const val EXTRA_ORDER_ITEM_ID = "extra_order_item_id"

        fun newIntent(
            context: Context,
            vehicleNumber: String,
            orderId: String = "",
            orderItemId: Int = 0
        ): Intent {
            return Intent(context, VehicleImageUploadActivity::class.java).apply {
                putExtra(EXTRA_VEHICLE_NUMBER, vehicleNumber)
                putExtra(EXTRA_ORDER_ID, orderId)
                putExtra(EXTRA_ORDER_ITEM_ID, orderItemId)
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
        // Vehicle Front card
        binding.uploadVehicleFront.setTitle(getString(R.string.upload_vehicle_front))
        binding.uploadVehicleFront.setSubtitle(getString(R.string.upload_file_format_info))
        binding.uploadVehicleFront.setButtonText(getString(R.string.take_photo_action))
        binding.uploadVehicleFront.showSampleImageLink {
            showSampleImageBottomSheet(
                getString(R.string.vehicle_front_image_title),
                R.drawable.vehicle_front,
                getString(R.string.vehicle_front_guidelines)
            )
        }
        binding.uploadVehicleFront.setOnUploadClickListener {
            currentUploadTarget = UploadTarget.VEHICLE_FRONT
            requestCameraPermissions()
        }
        binding.uploadVehicleFront.setOnRemoveClickListener {
            binding.uploadVehicleFront.resetUploadState()
            binding.uploadVehicleFront.setButtonText(getString(R.string.take_photo_action))
            vehicleFrontUploaded = false
            updateContinueButton()
        }

        // Vehicle Side card
        binding.uploadVehicleSide.setTitle(getString(R.string.upload_vehicle_side))
        binding.uploadVehicleSide.setSubtitle(getString(R.string.upload_file_format_info))
        binding.uploadVehicleSide.setButtonText(getString(R.string.take_photo_action))
        binding.uploadVehicleSide.showSampleImageLink {
            showSampleImageBottomSheet(
                getString(R.string.vehicle_side_image_title),
                R.drawable.vehicle_side,
                getString(R.string.vehicle_side_guidelines)
            )
        }
        binding.uploadVehicleSide.setOnUploadClickListener {
            currentUploadTarget = UploadTarget.VEHICLE_SIDE
            requestCameraPermissions()
        }
        binding.uploadVehicleSide.setOnRemoveClickListener {
            binding.uploadVehicleSide.resetUploadState()
            binding.uploadVehicleSide.setButtonText(getString(R.string.take_photo_action))
            vehicleSideUploaded = false
            updateContinueButton()
        }

        // Continue button
        binding.btnContinue.setOnClickListener {
            if (vehicleFrontUploaded && vehicleSideUploaded) {
                uploadVehicleImagesToServer()
            }
        }
    }

    // ---- Sample Image Bottom Sheet ----

    private fun showSampleImageBottomSheet(title: String, imageRes: Int, guidelines: String) {
        val bottomSheet = SampleImageBottomSheetDialogFragment.newInstance(
            title = title,
            sampleImageRes = imageRes,
            guidelines = guidelines
        )
        bottomSheet.show(supportFragmentManager, "sample_image")
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
        val prefix = when (currentUploadTarget) {
            UploadTarget.VEHICLE_FRONT -> "vehicle_front_"
            UploadTarget.VEHICLE_SIDE -> "vehicle_side_"
        }
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(prefix, ".jpg", storageDir)
    }

    // ---- Handle file result ----

    private fun handleFileResult(uri: Uri) {
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType) ?: "bin"

        val timestamp = System.currentTimeMillis()
        val outputFileName = when (currentUploadTarget) {
            UploadTarget.VEHICLE_FRONT -> "Vehicle_Front_${timestamp}.jpg"
            UploadTarget.VEHICLE_SIDE -> "Vehicle_Side_${timestamp}.jpg"
        }

        if (extension.lowercase() == "png") {
            lifecycleScope.launch {
                try {
                    val destPath = File(cacheDir, "converted_${timestamp}.jpg").absolutePath
                    val converted = documentUtils.convertPngToJpg(
                        sourcePath = documentUtils.getPathFromUri(
                            context = this@VehicleImageUploadActivity,
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
                    context = this@VehicleImageUploadActivity,
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
            UploadTarget.VEHICLE_FRONT -> {
                binding.uploadVehicleFront.setUploadedFile(file, "Vehicle_Front.jpg")
                vehicleFrontFile = file
                vehicleFrontUploaded = true
            }
            UploadTarget.VEHICLE_SIDE -> {
                binding.uploadVehicleSide.setUploadedFile(file, "Vehicle_Side.jpg")
                vehicleSideFile = file
                vehicleSideUploaded = true
            }
        }
        updateContinueButton()
    }

    private fun updateContinueButton() {
        val enabled = vehicleFrontUploaded && vehicleSideUploaded
        binding.btnContinue.isEnabled = enabled
        binding.btnContinue.alpha = if (enabled) 1.0f else 0.5f
    }

    // ---- Vehicle Image Upload API ----

    private fun uploadVehicleImagesToServer() {
        val frontFile = vehicleFrontFile ?: return
        val sideFile = vehicleSideFile ?: return
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        val orderItemId = intent.getIntExtra(EXTRA_ORDER_ITEM_ID, 0)

        val mediaType = okhttp3.MediaType.parse("image/jpeg")
        val frontPart = okhttp3.MultipartBody.Part.createFormData(
            "vehicle_front",
            frontFile.name,
            okhttp3.RequestBody.create(mediaType, frontFile)
        )
        val sidePart = okhttp3.MultipartBody.Part.createFormData(
            "vehicle_side",
            sideFile.name,
            okhttp3.RequestBody.create(mediaType, sideFile)
        )

        viewModel.uploadVehicleImages(frontPart, sidePart, orderId, orderItemId)
        observeUpload()
    }

    private fun observeUpload() {
        viewModel.uploadState.observe(this, androidx.lifecycle.Observer { resource ->
            when (resource) {
                is com.delhivery.axle.api.repository.Resource.Loading -> {
                    uiUtils.showProgress()
                }
                is com.delhivery.axle.api.repository.Resource.Success -> {
                    val jobId = resource.data?.jobId
                    if (!jobId.isNullOrEmpty()) {
                        // Start polling — keep progress showing
                        viewModel.startPolling(jobId)
                        observeProcessStatus()
                    } else {
                        uiUtils.hideProgress()
                        navigateToFastagImageUpload()
                    }
                }
                is com.delhivery.axle.api.repository.Resource.Failure -> {
                    uiUtils.hideProgress()
                    if (resource.isNetworkError) {
                        uiUtils.showSnackbar("Network error. Please check your connection.")
                    } else {
                        uiUtils.showSnackbar(resource.errorMessage ?: "Upload failed. Please try again.")
                    }
                }
            }
        })
    }

    private fun observeProcessStatus() {
        viewModel.processStatus.observe(this, androidx.lifecycle.Observer { resource ->
            when (resource) {
                is com.delhivery.axle.api.repository.Resource.Success -> {
                    uiUtils.hideProgress()
                    val status = resource.data?.status?.uppercase()
                    when (status) {
                        "COMPLETED", "TIMEOUT" -> navigateToFastagImageUpload()
                        "FAILED", "NOT_FOUND" -> {
                            uiUtils.showSnackbar("Vehicle image processing failed. Please try again.")
                        }
                    }
                }
                is com.delhivery.axle.api.repository.Resource.Failure -> {
                    uiUtils.hideProgress()
                    uiUtils.showSnackbar("Processing check failed. Please try again.")
                }
                is com.delhivery.axle.api.repository.Resource.Loading -> { /* no-op */ }
            }
        })
    }

    private fun navigateToFastagImageUpload() {
        val vehicleNumber = intent.getStringExtra(EXTRA_VEHICLE_NUMBER) ?: ""
        startActivity(FastagImageUploadActivity.newIntent(this, vehicleNumber))
    }
}
