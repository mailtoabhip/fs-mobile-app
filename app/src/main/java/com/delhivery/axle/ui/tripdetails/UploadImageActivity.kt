package com.delhivery.axle.ui.tripdetails

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnPreDrawListener
import android.view.Window
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.amazonaws.util.IOUtils
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.data.PodItem
import com.delhivery.axle.data.PodState
import com.delhivery.axle.data.home.trips.PODStatus
import com.delhivery.axle.databinding.ActivityEpodDetailsBinding
import com.delhivery.axle.databinding.DialogBidRevisedSuccessBinding
import com.delhivery.axle.databinding.DialogEpodSuccessBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.AWSUtils
import com.delhivery.axle.utils.AWSUtils.AWSProgressInterface
import com.delhivery.axle.utils.BitmapUtils
import com.delhivery.axle.utils.EVENT_POD_UPLOAD
import com.delhivery.axle.utils.FileCompressor
import com.delhivery.axle.utils.PROPERTY_STATUS
import com.delhivery.axle.utils.REQCODE_GALLERY_PHOTO
import com.delhivery.axle.utils.REQCODE_TAKE_PHOTO
import com.delhivery.axle.utils.VALUE_FAILURE
import com.delhivery.axle.utils.VALUE_SUCCESS
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.getFileName
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import androidx.core.graphics.toColorInt

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * Activity to upload images
 *
 **
 */
class UploadImageActivity : BaseActivity<ActivityEpodDetailsBinding, UploadImageViewModel>(),
    AWSProgressInterface {

  override fun getViewModelClass() = UploadImageViewModel::class.java

  override fun layoutId() = R.layout.activity_epod_details

  override fun requireConnection() = true

  private lateinit var uploadImageName: String
  private lateinit var localImageName: String
  private var mPhotoFile: File? = null
  private var currentPodId: Int = 0
  @Inject lateinit var fileCompressor: FileCompressor
  @Inject lateinit var awsUtils: AWSUtils
  @Inject lateinit var bitmapUtils: BitmapUtils
  private lateinit var podAdapter: PodAdapter
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true
  private var podStatus: PODStatus? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("UploadImageActivity_SetupTime")
    activitySetupTrace?.start()
    /* validate intent */
    try {
      require(
          !(intent == null || !intent.hasExtra(TransactionIdIntentKey))
      ) { "Required data $TransactionIdIntentKey not found" }
    } catch (e: Exception) {
      finish()
    }

    viewModel.transactionId = intent?.getStringExtra(TransactionIdIntentKey) ?: ""
    viewModel.reachedTime = intent?.getStringExtra(ReachedTimeIntentKey) ?: ""
    viewModel.unloadedTime = intent?.getStringExtra(UnloadedTimeIntentKey) ?: ""

    // Read pod_status from intent as enum
    podStatus = intent?.getSerializableExtra(PodStatusIntentKey) as? PODStatus
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.appBarLayout)
    }

    binding.btnBack.setOnClickListener {
      onBackPressed()
    }

    val disabledTextColor = ContextCompat.getColor(this, R.color.disabled_button_text)
    val enabledTextColor = Color.WHITE
    val colorStateList = ColorStateList(
      arrayOf(
        intArrayOf(-android.R.attr.state_enabled), // Disabled state
        intArrayOf(android.R.attr.state_enabled)    // Enabled state
      ),
      intArrayOf(
        disabledTextColor, // Color for disabled state
        enabledTextColor   // Color for enabled state
      )
    )
    binding.submitButtonMaxWidth.setTextColor(colorStateList)

    // Initialize RecyclerView
    podAdapter = PodAdapter(
      onPodClick = { podId ->
        captureImage(podId)
      },
      onDeleteClick = { podId ->
        viewModel.deletePod(podId)
      },
      bitmapUtils = bitmapUtils
    )

    binding.podRecyclerView.apply {
      layoutManager = GridLayoutManager(this@UploadImageActivity, 4)
      adapter = podAdapter
    }

    // Ensure initial visibility: maxWidth visible, RecyclerView hidden
    binding.containerPodMaxWidth.visibility = View.VISIBLE
    binding.podRecyclerView.visibility = View.GONE

    // Observe pod items changes FIRST (before initializing)
    viewModel.podItemsLiveData.observe(this, Observer { podItems ->
      podAdapter.updateItems(podItems)
      // Show RecyclerView and hide maxWidth container when any pod is available/selected/uploading/uploaded
      val hasActivePods = podItems.any {
        it.state != PodState.EMPTY
      }
      if (hasActivePods) {
        binding.containerPodMaxWidth.visibility = View.GONE
        binding.podRecyclerView.visibility = View.VISIBLE
        // Force layout to ensure RecyclerView is measured
        binding.podRecyclerView.requestLayout()
      } else {
        // Revert to initial state if no active pods
        binding.containerPodMaxWidth.visibility = View.VISIBLE
        binding.podRecyclerView.visibility = View.GONE
      }

      // Enable/disable Submit button based on selected images
      val hasSelectedImages = podItems.any { it.state == PodState.SELECTED }
      binding.submitButtonMaxWidth.isEnabled = hasSelectedImages
    })

    // Initialize pods AFTER observer is set up
    viewModel.initializePods()

    viewModel.delegationLiveData.observe(this, Observer {
      uploadImage(it.first, it.second, currentPodId)

      // After this upload completes, check if there are more selected pods to upload
      // This will be handled in onAWSSuccess
    })

    viewModel.uploadResultLiveData.observe(this, Observer {
      if (it) {
        // Don't finish here - wait for POD submission to complete
        // setResult and finish will be called after POD submission succeeds
      } else {
        uiUtils.showSnackbar("Upload Failed, Please try again")
      }
    })

    // Observe POD submission result (final API call)
    viewModel.podSubmissionResultLiveData.observe(this, Observer { isSuccess ->
      uiUtils.hideProgress()
      if (isSuccess) {
        // Set result before showing success dialog
        setResult(Activity.RESULT_OK)
        showBidSuccessDialog(false)
      } else {
          viewModel.getPodItems().forEach {
              if(it.state == PodState.SELECTED || it.state == PodState.UPLOADED)
                  it.state = PodState.SELECTED
          }

          binding.submitButtonMaxWidth.isEnabled = true
      }
    })

    // Max width container click - start first upload
    binding.containerPodMaxWidth.setOnClickListener {
      // Start upload process - this will make first pod available
      viewModel.startUploadProcess()
      // Immediately show RecyclerView and hide maxWidth (observer will also handle this, but do it immediately for better UX)
      binding.containerPodMaxWidth.visibility = View.GONE
      binding.podRecyclerView.visibility = View.VISIBLE
      captureImage(1)
    }

    // Submit button - upload all selected images
    binding.submitButtonMaxWidth.setOnClickListener {
      val selectedPods = viewModel.getSelectedPods()
      if (selectedPods.isNotEmpty()) {
        // Start uploading all selected images
        uploadAllSelectedImages(selectedPods)
      } else {
        uiUtils.showSnackbar("Please select at least one image to upload")
      }
    }

    // Update badge styling based on pod_status
    updatePodStatusBadge()
  }

  /**
   * Update the pod status badge styling based on pod_status from intent
   */
  private fun updatePodStatusBadge() {
    val badge = binding.badgeEpodPending
    when (podStatus) {
      PODStatus.REJECT -> {
        badge.text = "ePOD Rejected"
        badge.backgroundTintList = ColorStateList.valueOf("#FEEFEF".toColorInt())
        badge.setTextColor("#8E2720".toColorInt())
      }
      PODStatus.REVIEW -> {
        badge.text = "ePOD Under Review"
        badge.backgroundTintList = ColorStateList.valueOf("#F0F0F0".toColorInt())
        badge.setTextColor("#121A31".toColorInt())
      }
      PODStatus.VIEWPOD -> {
        badge.text = "ePOD Verified"
        badge.backgroundTintList = ColorStateList.valueOf("#ECFDF5".toColorInt())
        badge.setTextColor("#065F46".toColorInt())
      }
      PODStatus.UPLOAD -> {
        badge.text = "ePOD Pending"
        badge.backgroundTintList = ColorStateList.valueOf("#FFF7EB".toColorInt())
        badge.setTextColor(ContextCompat.getColor(this, R.color.pending_font))
      }
      null -> {
        badge.visibility = View.GONE
      }
    }
  }

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }


  private fun captureImage(podId: Int) {
    currentPodId = podId
    val podItem = viewModel.getPodItems().find { it.id == podId }

    // If already uploaded, view the image
    if (podItem?.state == PodState.UPLOADED && podItem.imageUrl != null) {
      viewImage(podItem.imageUrl, "View ePod $podId")
      return
    }

    // If already selected, allow reselection (will replace current selection)
    // Start capture
    startCapture(
        "${viewModel.transactionId}-$podId",
        "vendor_pod_${viewModel.transactionId}-$podId"
    )
  }

  private fun uploadImage(
    delegationToken: DelegationToken,
    file: File,
    podId: Int
  ) {
    val awsPath = "trips/temp/vendor_pod/${viewModel.transactionId}/" + uploadImageName + ".jpg"
    awsUtils.startUpload(delegationToken, awsPath, file, this)
    viewModel.startUpload(podId, file.path)
  }

  override fun onAWSSuccess(
    path: String
  ) {
    if (!isFinishing) {
      analyticsUtil.moEngageTrackEvent(
          EVENT_POD_UPLOAD,
          mutableListOf(PROPERTY_STATUS),
          mutableListOf(VALUE_SUCCESS)
      )
      // Construct full S3 URL from the path (S3 key)
      val fullS3Url = awsUtils.awsBasePath() + path
      viewModel.onUploadSuccess(currentPodId, fullS3Url)

      // Check if there are more selected pods to upload
      val remainingSelectedPods = viewModel.getSelectedPods()
      if (remainingSelectedPods.isNotEmpty()) {
        // Upload next selected image
        uploadSingleImage(remainingSelectedPods.first())
      } else {
        // All images uploaded, now submit POD
        if (!viewModel.imageUrls.isNullOrEmpty()) {
          uiUtils.showProgress()
          viewModel.uploadPod()
        } else {
          uiUtils.hideProgress()
        }
        resetUploadData()
      }
    }
  }

  override fun onAWSFailure() {
    if (!isFinishing) {
      analyticsUtil.moEngageTrackEvent(
          EVENT_POD_UPLOAD,
          mutableListOf(PROPERTY_STATUS),
          mutableListOf(VALUE_FAILURE)
      )
      uiUtils.hideProgress()
      uiUtils.showSnackbar(getString(R.string.msg_file_upload_failed))
      // Reset to SELECTED state so user can retry
      viewModel.onUploadFailure(currentPodId)
      resetUploadData()
    }
  }

  /**
   * Upload all selected images when Submit is clicked
   */
  private fun uploadAllSelectedImages(selectedPods: List<PodItem>) {
    if (selectedPods.isEmpty()) {
      uiUtils.showSnackbar("No images selected")
      return
    }

    // Upload first image, then chain the rest
    val firstPod = selectedPods.first()
    uploadSingleImage(firstPod)
  }

  /**
   * Upload a single image
   */
  private fun uploadSingleImage(podItem: PodItem) {
    val file = File(podItem.imagePath ?: return)
    if (!file.exists()) {
      uiUtils.showSnackbar("Image file not found for pod ${podItem.id}")
      return
    }

    currentPodId = podItem.id
    uploadImageName = "${viewModel.transactionId}-${podItem.id}"
    localImageName = "vendor_pod_${viewModel.transactionId}-${podItem.id}"

    uiUtils.showProgress()
    viewModel.getDelegationToken(file)
  }

  private fun resetUploadData() {
    mPhotoFile = null
    uploadImageName = ""
    localImageName = ""
    currentPodId = 0
  }

  private fun viewImage(
    path: String?,
    title: String
  ) {
    if (path.isNotNullOrEmpty()) {
      startActivity(imageViewIntent(this, path ?: "", title))
    }
  }

  private fun startCapture(
    uploadImageName: String,
    localImageName: String
  ) {
    this.uploadImageName = uploadImageName
    this.localImageName = localImageName

    val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
    val builder = AlertDialog.Builder(this)
    builder.setItems(items) { dialog, item ->
      when {
        items[item] == "Take Photo" ->
          requestImageCapturePermissions(true)
        items[item] == "Choose from Library" ->
          requestImageCapturePermissions(false)
        items[item] == "Cancel" -> {
          dialog.dismiss()
          if (viewModel.isInitialState()) {
            viewModel.resetPods()
          }
        }
      }
    }
      builder.setCancelable(false)
    builder.show()
    builder.setOnCancelListener {
      if (viewModel.isInitialState()) {
        viewModel.resetPods()
      }
    }
  }

  private fun requestImageCapturePermissions(isCamera: Boolean) {
    compositeDisposable += requestPermission(
      arrayOf(Manifest.permission.CAMERA).apply {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
          plus(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      }
    )
        .onBackground()
        .subscribe { granted, error ->
          if (error == null && granted) {
            if (isCamera) {
              dispatchTakePictureIntent()
            } else {
              dispatchGalleryIntent()
            }
          } else {
            uiUtils.showSnackbar(getString(R.string.storage_camera_permission))
          }
        }

  }

  private fun createImageFile(): File {
    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(localImageName, ".jpg", storageDir)
  }

  private fun dispatchTakePictureIntent() {
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
      try {
        mPhotoFile = createImageFile()
        val photoURI = FileProvider.getUriForFile(
            this, BuildConfig.APPLICATION_ID + ".provider", mPhotoFile!!
        )
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(takePictureIntent, REQCODE_TAKE_PHOTO)
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
  }

    private fun showBidSuccessDialog(isError: Boolean) {
        if (isFinishing || isError) return

        val dialog = Dialog(this)
        val bindingDialog = DialogEpodSuccessBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.textTitle.text = "ePOD submitted successfully!"
        bindingDialog.textMessage.visibility = View.GONE
        bindingDialog.iconSuccess.setImageResource(R.drawable.ic_green_tick)

        // Done button listener
        bindingDialog.doneButtonMaxWidth.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
        dialog.setCancelable(false)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

  private fun dispatchGalleryIntent() {
    val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivityForResult(pickPhoto, REQCODE_GALLERY_PHOTO)
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      REQCODE_TAKE_PHOTO -> {
        if (resultCode == Activity.RESULT_OK) {
          if (mPhotoFile == null) {
            uiUtils.showToast(getString(R.string.msg_image_capture_failed))
            return
          }
          try {
            mPhotoFile = fileCompressor.compressToFile(mPhotoFile!!, localImageName)
            // Just set image as selected (no upload yet)
            // For camera capture, imageUri is null since we use FileProvider URI
            viewModel.setImageSelected(currentPodId, mPhotoFile!!.path, imageUri = null)
            resetUploadData()
          } catch (e: IOException) {
            uiUtils.showToast(getString(R.string.msg_image_capture_failed))
          }
        } else {
          uiUtils.showToast(getString(R.string.msg_image_capture_failed))
        }
      }

      REQCODE_GALLERY_PHOTO -> {
        if (resultCode == Activity.RESULT_OK) {
          try {
            val selectedImage = data?.data
            require(selectedImage != null)
            val parcelFileDescriptor = contentResolver?.openFileDescriptor(selectedImage, "r", null)
            require(parcelFileDescriptor != null)
            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            require(contentResolver != null && contentResolver?.getFileName(selectedImage) != null)
            val imageScopedFile = File(cacheDir, contentResolver?.getFileName(selectedImage)!!)
            val outputStream = FileOutputStream(imageScopedFile)
            IOUtils.copy(inputStream, outputStream)

            mPhotoFile = fileCompressor.compressToFile(File(imageScopedFile.path), localImageName)
            if (mPhotoFile == null) {
              uiUtils.showToast(getString(R.string.msg_image_capture_failed))
              return
            }
            // Just set image as selected (no upload yet)
            // Store the original gallery URI for reference
            viewModel.setImageSelected(currentPodId, mPhotoFile!!.path, imageUri = selectedImage)
            resetUploadData()
          } catch (e: IOException) {
            uiUtils.showToast(getString(R.string.msg_image_capture_failed))
          }
        } else {
          uiUtils.showToast(getString(R.string.msg_image_capture_failed))
        }
      }
    }
  }

}

/* intent keys */
private const val TransactionIdIntentKey = "transaction_id"
private const val ReachedTimeIntentKey = "reached_time"
private const val UnloadedTimeIntentKey = "unloaded_time"
private const val PodStatusIntentKey = "pod_status"

/**
 * Upload Image intent
 */
fun uploadImageIntent(
  context: Context,
  transactionId: String,
  reachedTime: String,
  unloadedTime: String,
  podStatus: PODStatus? = null
) = Intent(context, UploadImageActivity::class.java).apply {
    putExtra(TransactionIdIntentKey, transactionId)
    putExtra(ReachedTimeIntentKey, reachedTime)
    putExtra(UnloadedTimeIntentKey, unloadedTime)
    podStatus?.let { putExtra(PodStatusIntentKey, it) }
}
