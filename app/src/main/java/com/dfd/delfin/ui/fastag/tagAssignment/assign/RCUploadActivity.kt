package com.dfd.delfin.ui.fastag.tagAssignment.assign

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
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.dfd.delfin.BuildConfig
import com.dfd.delfin.R
import com.dfd.delfin.api.repository.Resource
import com.dfd.delfin.databinding.ActivityFastagAssignmentBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.dialogs.RcUploadFailedBottomSheetDialogFragment
import com.dfd.delfin.ui.dialogs.UploadOptionsBottomSheetDialogFragment
import com.dfd.delfin.utils.DocumentUtils
import com.dfd.delfin.utils.FileCompressor
import com.dfd.delfin.utils.WindowInsetsUtils
import com.dfd.delfin.utils.extensions.MimeTypes
import com.dfd.delfin.utils.extensions.filePickerChooser
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class RCUploadActivity : BaseActivity<ActivityFastagAssignmentBinding, RCUploadViewModel>() {

    override fun getViewModelClass() = RCUploadViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_assignment
    override fun requireConnection() = true

    @Inject
    lateinit var documentUtils: DocumentUtils
    @Inject
    lateinit var fileCompressor: FileCompressor

    private var rcFrontUploaded = false
    private var rcBackUploaded = false
    private var rcFrontFile: File? = null
    private var rcBackFile: File? = null
    private var lastJobId: String? = null

    private var currentUploadTarget: UploadTarget = UploadTarget.RC_FRONT
    private var mPhotoFile: File? = null

    private enum class UploadTarget {
        RC_FRONT, RC_BACK
    }

    companion object {
        private const val EXTRA_VEHICLE_NUMBER = "extra_vehicle_number"
        private const val EXTRA_CHASSIS_NUMBER = "extra_chassis_number"
        private const val EXTRA_ORDER_ID = "extra_order_id"
        private const val EXTRA_ORDER_ITEM_ID = "extra_order_item_id"
        private const val EXTRA_TAG_COLOR = "extra_tag_color"
        private const val EXTRA_BANK = "extra_bank"
        private const val EXTRA_VEHICLE_CLASS = "extra_vehicle_class"

        fun newIntent(
            context: Context,
            vehicleNumber: String,
            orderId: String = "",
            orderItemId: Int = 0,
            tagColor: String = "",
            bank: String = "",
            vehicleClass: String = ""
        ): Intent {
            return Intent(context, RCUploadActivity::class.java).apply {
                putExtra(EXTRA_VEHICLE_NUMBER, vehicleNumber)
                putExtra(EXTRA_ORDER_ID, orderId)
                putExtra(EXTRA_ORDER_ITEM_ID, orderItemId)
                putExtra(EXTRA_TAG_COLOR, tagColor)
                putExtra(EXTRA_BANK, bank)
                putExtra(EXTRA_VEHICLE_CLASS, vehicleClass)
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
        setupToolbar()
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
                uploadRcToServer()
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
                            context = this@RCUploadActivity,
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
                    context = this@RCUploadActivity,
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
                rcFrontFile = file
                rcFrontUploaded = true
            }
            UploadTarget.RC_BACK -> {
                binding.uploadRcBack.setUploadedFile(file, "RC_Back.jpg")
                rcBackFile = file
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

    // ---- RC Upload API ----

    private fun uploadRcToServer() {
        val frontFile = rcFrontFile ?: return
        val backFile = rcBackFile ?: return
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        val orderItemId = intent.getIntExtra(EXTRA_ORDER_ITEM_ID, 0)

        val mediaType = MediaType.parse("image/jpeg")
        val rcFrontPart = MultipartBody.Part.createFormData(
            "rc_front",
            frontFile.name,
            RequestBody.create(mediaType, frontFile)
        )
        val rcBackPart = MultipartBody.Part.createFormData(
            "rc_back",
            backFile.name,
            RequestBody.create(mediaType, backFile)
        )

        viewModel.uploadRcImages(rcFrontPart, rcBackPart, orderId, orderItemId)
    }

    private fun setupObservers() {
        viewModel.rcUploadState.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    uiUtils.showProgress()
                }
                is Resource.Success -> {
                    val jobId = resource.data?.jobId
                    if (!jobId.isNullOrEmpty()) {
                        lastJobId = jobId
                        // Start polling — keep progress showing
                        viewModel.startRcPolling(jobId)
                    } else {
                        uiUtils.hideProgress()
                        navigateToVehicleImageUpload("")
                    }
                }
                is Resource.Failure -> {
                    uiUtils.hideProgress()
                    dialogUtils.showErrorDialog(
                        if (resource.isNetworkError) "Network error. Please check your connection."
                        else resource.errorMessage ?: "RC upload failed. Please try again.", 3L
                    )
                }
            }
        })

        viewModel.rcProcessStatus.observe(this, Observer { resource ->
            if (resource == null) return@Observer
            when (resource) {
                is Resource.Success -> {
                    uiUtils.hideProgress()
                    val status = resource.data?.status?.uppercase()
                    when (status) {
                        "COMPLETED" -> {
                            val journeyId = resource.data.journeyId ?: ""
                            val skipVehicleUpload = resource.data.skipVehicleImageUpload == true
                            Toast.makeText(this, "RC uploaded successfully", Toast.LENGTH_SHORT).show()
                            if (skipVehicleUpload) {
                                navigateToTagMapping(journeyId)
                            } else {
                                navigateToVehicleImageUpload(journeyId)
                            }
                        }
                        "FAILED" -> {
                            showRcUploadFailedBottomSheet()
                        }
                        "TIMEOUT" -> {
                            showRcProcessingTimeoutBottomSheet()
                        }
                        "NOT_FOUND" -> {
                            dialogUtils.showErrorDialog("RC processing not found. Please try again.", 3L)
                        }
                    }
                }
                is Resource.Failure -> {
                    uiUtils.hideProgress()
                    dialogUtils.showErrorDialog("Processing check failed. Please try again.", 3L)
                }
                is Resource.Loading -> { /* no-op */ }
            }
        })
    }

    private fun navigateToVehicleImageUpload(journeyId: String) {
        val vehicleNumber = intent.getStringExtra(EXTRA_VEHICLE_NUMBER) ?: ""
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        val orderItemId = intent.getIntExtra(EXTRA_ORDER_ITEM_ID, 0)
        val tagColor = intent.getStringExtra(EXTRA_TAG_COLOR) ?: ""
        val bank = intent.getStringExtra(EXTRA_BANK) ?: ""
        val vehicleClass = intent.getStringExtra(EXTRA_VEHICLE_CLASS) ?: ""
        startActivity(VehicleImageUploadActivity.newIntent(this, vehicleNumber, orderId, orderItemId, journeyId, tagColor, bank, vehicleClass))
    }

    private fun navigateToTagMapping(journeyId: String) {
        val vehicleNumber = intent.getStringExtra(EXTRA_VEHICLE_NUMBER) ?: ""
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        val orderItemId = intent.getIntExtra(EXTRA_ORDER_ITEM_ID, 0)
        val tagColor = intent.getStringExtra(EXTRA_TAG_COLOR) ?: ""
        val bank = intent.getStringExtra(EXTRA_BANK) ?: ""
        val vehicleClass = intent.getStringExtra(EXTRA_VEHICLE_CLASS) ?: ""
        val intent = android.content.Intent(this, com.dfd.delfin.ui.fastag.tagMapping.TagMappingActivity::class.java).apply {
            putExtra(com.dfd.delfin.ui.fastag.tagMapping.TagMappingActivity.EXTRA_ORDER_ID, orderId)
            putExtra(com.dfd.delfin.ui.fastag.tagMapping.TagMappingActivity.EXTRA_ORDER_ITEM_ID, orderItemId.toString())
            putExtra(com.dfd.delfin.ui.fastag.tagMapping.TagMappingActivity.EXTRA_VEHICLE_CLASS, vehicleClass)
            putExtra(com.dfd.delfin.ui.fastag.tagMapping.TagMappingActivity.EXTRA_JOURNEY_ID, journeyId)
            putExtra(com.dfd.delfin.ui.fastag.tagMapping.TagMappingActivity.EXTRA_VEHICLE_NUMBER, vehicleNumber)
            putExtra(com.dfd.delfin.ui.fastag.tagMapping.TagMappingActivity.EXTRA_TAG_COLOR, tagColor)
            putExtra(com.dfd.delfin.ui.fastag.tagMapping.TagMappingActivity.EXTRA_BANK, bank)
        }
        navigationUtils.navigate(intent, false)
    }

    private fun showRcUploadFailedBottomSheet() {
        val bottomSheet = RcUploadFailedBottomSheetDialogFragment.newInstance(
            title = getString(R.string.rc_upload_failed_title),
            subtitle = getString(R.string.rc_upload_failed_subtitle),
            showButtons = false
        )
        bottomSheet.show(supportFragmentManager, "rc_upload_failed")
    }

    private fun showRcProcessingTimeoutBottomSheet() {
        val bottomSheet = RcUploadFailedBottomSheetDialogFragment.newInstance(
            title = getString(R.string.rc_processing_timeout_title),
            subtitle = getString(R.string.rc_processing_timeout_subtitle),
            iconRes = R.drawable.fs_payment_pending,
            showButtons = true,
            onTryAgain = {
                lastJobId?.let { jobId ->
                    uiUtils.showProgress()
                    viewModel.startRcPolling(jobId)
                }
            },
            onMaybeLater = {
                val intent = Intent(this, com.dfd.delfin.ui.home.activity.home.HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        )
        bottomSheet.show(supportFragmentManager, "rc_processing_timeout")
    }
}
