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
import com.delhivery.axle.ui.dialogs.UploadOptionsBottomSheetDialogFragment
import com.delhivery.axle.utils.DocumentUtils
import com.delhivery.axle.utils.FileCompressor
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class VehicleImageUploadActivity :
    BaseActivity<ActivityVehicleImageUploadBinding, VehicleImageUploadViewModel>() {

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
    private var lastJobId: String? = null

    private var currentUploadTarget: UploadTarget = UploadTarget.VEHICLE_FRONT
    private var mPhotoFile: File? = null

    private enum class UploadTarget {
        VEHICLE_FRONT, VEHICLE_SIDE
    }

    companion object {
        private const val EXTRA_VEHICLE_NUMBER = "extra_vehicle_number"
        private const val EXTRA_ORDER_ID = "extra_order_id"
        private const val EXTRA_ORDER_ITEM_ID = "extra_order_item_id"
        private const val EXTRA_JOURNEY_ID = "extra_journey_id"
        private const val EXTRA_TAG_COLOR = "extra_tag_color"
        private const val EXTRA_BANK = "extra_bank"
        private const val EXTRA_VEHICLE_CLASS = "extra_vehicle_class"

        fun newIntent(
            context: Context,
            vehicleNumber: String,
            orderId: String = "",
            orderItemId: Int = 0,
            journeyId: String = "",
            tagColor: String = "",
            bank: String = "",
            vehicleClass: String = ""
        ): Intent {
            return Intent(context, VehicleImageUploadActivity::class.java).apply {
                putExtra(EXTRA_VEHICLE_NUMBER, vehicleNumber)
                putExtra(EXTRA_ORDER_ID, orderId)
                putExtra(EXTRA_ORDER_ITEM_ID, orderItemId)
                putExtra(EXTRA_JOURNEY_ID, journeyId)
                putExtra(EXTRA_TAG_COLOR, tagColor)
                putExtra(EXTRA_BANK, bank)
                putExtra(EXTRA_VEHICLE_CLASS, vehicleClass)
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

    // TODO: Remove file picker once temp testing is done — use camera only
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
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
        setupObservers()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.llParent)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
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
            // TODO: Remove this drawer and use camera directly once temp testing is done
            showUploadOptionsBottomSheet(getString(R.string.upload_vehicle_front))
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
            // TODO: Remove this drawer and use camera directly once temp testing is done
            showUploadOptionsBottomSheet(getString(R.string.upload_vehicle_side))
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

    // ---- Upload Options Bottom Sheet ----
    // TODO: Remove this method and use camera directly once temp testing is done

    private fun showUploadOptionsBottomSheet(title: String) {
        val bottomSheet = UploadOptionsBottomSheetDialogFragment.newInstance(
            title = title,
            onUploadFile = { launchFilePicker() },
            onTakePhoto = { requestCameraPermissions() }
        )
        bottomSheet.show(supportFragmentManager, "upload_options")
    }

    private fun launchFilePicker() {
        val chooserIntent = com.delhivery.axle.utils.extensions.filePickerChooser(
            "Select file",
            com.delhivery.axle.utils.extensions.MimeTypes.IMAGE_JPG,
            com.delhivery.axle.utils.extensions.MimeTypes.IMAGE_JPEG
        )
        try {
            filePickerLauncher.launch(chooserIntent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                this,
                "No file picker app found",
                android.widget.Toast.LENGTH_SHORT
            ).show()
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
        val journeyId = intent.getStringExtra(EXTRA_JOURNEY_ID) ?: ""

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

        viewModel.uploadVehicleImages(frontPart, sidePart, orderId, orderItemId, journeyId)
    }

    private fun setupObservers() {
        viewModel.uploadState.observe(this, androidx.lifecycle.Observer { resource ->
            when (resource) {
                is com.delhivery.axle.api.repository.Resource.Loading -> {
                    uiUtils.showProgress()
                }

                is com.delhivery.axle.api.repository.Resource.Success -> {
                    val jobId = resource.data?.jobId
                    if (!jobId.isNullOrEmpty()) {
                        lastJobId = jobId
                        // Start polling — keep progress showing
                        viewModel.startPolling(jobId)
                    } else {
                        uiUtils.hideProgress()
                        navigateToTagMapping()
                    }
                }

                is com.delhivery.axle.api.repository.Resource.Failure -> {
                    uiUtils.hideProgress()
                    dialogUtils.showErrorDialog(
                        if (resource.isNetworkError) "Network error. Please check your connection."
                        else resource.errorMessage ?: "Upload failed. Please try again.", 3L
                    )
                }
            }
        })

        viewModel.processStatus.observe(this, androidx.lifecycle.Observer { resource ->
            if (resource == null) return@Observer
            when (resource) {
                is com.delhivery.axle.api.repository.Resource.Success -> {
                    uiUtils.hideProgress()
                    val status = resource.data?.status?.uppercase()
                    when (status) {
                        "COMPLETED" -> {
                            android.widget.Toast.makeText(
                                this,
                                "Vehicle images uploaded successfully",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            navigateToTagMapping()
                        }

                        "FAILED" -> {
                            showVehicleUploadFailedBottomSheet()
                        }

                        "TIMEOUT" -> {
                            showVehicleProcessingTimeoutBottomSheet()
                        }

                        "NOT_FOUND" -> {
                            dialogUtils.showErrorDialog(
                                "Vehicle image processing not found. Please try again.",
                                3L
                            )
                        }
                    }
                }

                is com.delhivery.axle.api.repository.Resource.Failure -> {
                    uiUtils.hideProgress()
                    dialogUtils.showErrorDialog("Processing check failed. Please try again.", 3L)
                }

                is com.delhivery.axle.api.repository.Resource.Loading -> { /* no-op */
                }
            }
        })
    }

    private fun showVehicleUploadFailedBottomSheet() {
        val bottomSheet =
            com.delhivery.axle.ui.dialogs.RcUploadFailedBottomSheetDialogFragment.newInstance(
                title = getString(R.string.vehicle_upload_failed_title),
                subtitle = getString(R.string.vehicle_upload_failed_subtitle),
                showButtons = false
            )
        bottomSheet.show(supportFragmentManager, "vehicle_upload_failed")
    }

    private fun showVehicleProcessingTimeoutBottomSheet() {
        val bottomSheet =
            com.delhivery.axle.ui.dialogs.RcUploadFailedBottomSheetDialogFragment.newInstance(
                title = getString(R.string.rc_processing_timeout_title),
                subtitle = getString(R.string.vehicle_processing_timeout_subtitle),
                iconRes = R.drawable.fs_payment_pending,
                showButtons = true,
                onTryAgain = {
                    lastJobId?.let { jobId ->
                        uiUtils.showProgress()
                        viewModel.startPolling(jobId)
                    }
                },
                onMaybeLater = {
                    val intent = android.content.Intent(
                        this,
                        com.delhivery.axle.ui.home.activity.home.HomeActivity::class.java
                    )
                    intent.flags =
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            )
        bottomSheet.show(supportFragmentManager, "vehicle_processing_timeout")
    }

    private fun navigateToTagMapping() {
        val vehicleNumber = intent.getStringExtra(EXTRA_VEHICLE_NUMBER) ?: ""
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        val orderItemId = intent.getIntExtra(EXTRA_ORDER_ITEM_ID, 0)
        val journeyId = intent.getStringExtra(EXTRA_JOURNEY_ID) ?: ""
        val tagColor = intent.getStringExtra(EXTRA_TAG_COLOR) ?: ""
        val bank = intent.getStringExtra(EXTRA_BANK) ?: ""
        val vehicleClass = intent.getStringExtra(EXTRA_VEHICLE_CLASS) ?: ""
        val navIntent = android.content.Intent(
            this,
            com.delhivery.axle.ui.fastag.tagMapping.TagMappingActivity::class.java
        ).apply {
            putExtra(
                com.delhivery.axle.ui.fastag.tagMapping.TagMappingActivity.EXTRA_ORDER_ID,
                orderId
            )
            putExtra(
                com.delhivery.axle.ui.fastag.tagMapping.TagMappingActivity.EXTRA_ORDER_ITEM_ID,
                orderItemId
            )
            putExtra(
                com.delhivery.axle.ui.fastag.tagMapping.TagMappingActivity.EXTRA_VEHICLE_CLASS,
                vehicleClass
            )
            putExtra(
                com.delhivery.axle.ui.fastag.tagMapping.TagMappingActivity.EXTRA_JOURNEY_ID,
                journeyId
            )
            putExtra(
                com.delhivery.axle.ui.fastag.tagMapping.TagMappingActivity.EXTRA_VEHICLE_NUMBER,
                vehicleNumber
            )
            putExtra(
                com.delhivery.axle.ui.fastag.tagMapping.TagMappingActivity.EXTRA_TAG_COLOR,
                tagColor
            )
            putExtra(com.delhivery.axle.ui.fastag.tagMapping.TagMappingActivity.EXTRA_BANK, bank)
        }
        navigationUtils.navigate(navIntent, false)
    }
}
