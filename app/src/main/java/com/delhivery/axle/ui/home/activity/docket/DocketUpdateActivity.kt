package com.delhivery.axle.ui.home.activity.docket

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewTreeObserver.OnPreDrawListener
import android.widget.DatePicker
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.databinding.ActivityHpodDetailsBinding
import com.delhivery.axle.databinding.ActivityUpdateDocketBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.tripdetails.imageViewIntent
import com.delhivery.axle.utils.AWSUtils
import com.delhivery.axle.utils.AWSUtils.AWSProgressInterface
import com.delhivery.axle.utils.BitmapUtils
import com.delhivery.axle.utils.DatePatterns
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.FileCompressor
import com.delhivery.axle.utils.ImageUtils
import com.delhivery.axle.utils.REQCODE_CAMERA
import com.delhivery.axle.utils.REQCODE_GALLERY_PHOTO
import com.delhivery.axle.utils.REQCODE_TAKE_PHOTO
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.getFileName
import com.delhivery.axle.utils.extensions.getSerializable
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.toDate
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import javax.inject.Inject

/**
 **
 *
 * Activity update docket details
 *
 **
 */
class DocketUpdateActivity : BaseActivity<ActivityHpodDetailsBinding, DocketUpdateViewModel>(),
    OnDateSetListener, AWSProgressInterface {

  override fun getViewModelClass() = DocketUpdateViewModel::class.java

  override fun layoutId() = R.layout.activity_hpod_details

  override fun requireConnection() = true

  private var isCamera: Boolean = false
  private var mPhotoFile: File? = null
  private lateinit var uploadImageName: String
  private lateinit var localImageName: String
  @Inject lateinit var imageUtils: ImageUtils
  @Inject lateinit var awsUtils: AWSUtils
  @Inject lateinit var fileCompressor: FileCompressor
  @Inject lateinit var bitmapUtils: BitmapUtils
  @Inject lateinit var userPrefs: UserPrefs
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("DocketUpdateActivity_SetupTime")
    activitySetupTrace?.start()
    /* validate intent */
    require(
        !(intent == null || (!intent.hasExtra(TransactionIdsIntentKey) && !intent.hasExtra(
            TripDataIntentKey
        )))
    ) { "Required data $TransactionIdsIntentKey or $TripDataIntentKey not found" }

    if (intent.hasExtra(TransactionIdsIntentKey))
      viewModel.transactionIds =
        intent.getStringArrayListExtra(TransactionIdsIntentKey) ?: mutableListOf()
    if (intent.hasExtra(TripDataIntentKey)) {
      viewModel.trip = intent.getSerializable(TripDataIntentKey,HomeTripsItemData::class.java)
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    // Set up toolbar
    binding.btnBack.setOnClickListener {
      onBackPressedDispatcher.onBackPressed()
    }
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.appBarLayout)
    }
    
    // Hide the badge for this screen
    binding.badgeEpodPending.visibility = View.GONE
    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        userPrefs.setPreviousScreen(this.javaClass.name)
        finish()
      }
    })
    if (viewModel.trip != null) {
      viewModel.transactionIds.add(viewModel.trip!!.transactionId)
      binding.trackingNumberInput.setText(viewModel.trip!!.podDispatchAwbNumber)
      binding.dispatchDateInput.setText(viewModel.trip!!.podDispatchDate)
      binding.podContainerSmall.visibility = View.VISIBLE
      binding.podContainer.visibility = View.GONE
      // Load existing image if available
      if (!viewModel.trip!!.podDispatchDocketImage.isNullOrEmpty()) {
        viewModel.imageUrl = viewModel.trip!!.podDispatchDocketImage
        binding.podImageCardSmall.visibility = View.VISIBLE
        binding.addIconSmall.visibility = View.GONE
        GlideApp.with(this)
            .load(viewModel.trip!!.podDispatchDocketImage)
            .into(binding.podImageSmall)
        binding.podImageSmall.visibility = View.VISIBLE
        binding.deleteButtonSmall.visibility = View.VISIBLE
      }
    } else {
      binding.podContainerSmall.visibility = View.GONE
      binding.podContainer.visibility = View.VISIBLE
    }

    viewModel.delegationLiveData.observe(this, Observer {
      uploadImage(it.first, it.second)
    })

    viewModel.statusLiveData.observe(this, Observer {
      if (it) {
        setResult(Activity.RESULT_OK)
        finish()
      }
    })

    // Full width container click listener (for new upload)
    binding.podContainer.setOnClickListener {
      val imageName = "docket_" + System.currentTimeMillis()
      captureImage(imageName, imageName)
    }
    
    // Small container click listener (for viewing/updating existing image)
    binding.podContainerSmall.setOnClickListener {
      val imageName = "docket_" + System.currentTimeMillis()
      if (viewModel.imageUrl.isNullOrEmpty()) {
        captureImage(imageName, imageName)
      } else {
        userPrefs.setPreviousScreen(this.javaClass.name)
        startActivity(imageViewIntent(this, viewModel.imageUrl, "Docket Image"))
      }
    }

    binding.deleteButtonSmall.setOnClickListener {
      deleteImage(binding.deleteButtonSmall, binding.podImageSmall)
    }
    
    binding.deleteButton.setOnClickListener {
      deleteImage(binding.deleteButton, binding.podImage)
    }

    binding.dispatchDateInput.setOnClickListener {
      dialogUtils.datePicker(listener = this, maxDate = 0)
    }
    
    binding.dispatchDateContainer.setOnClickListener {
      dialogUtils.datePicker(listener = this, maxDate = 0)
    }

    binding.calendarIcon.setOnClickListener {
      dialogUtils.datePicker(listener = this, maxDate = 0)
    }

    binding.submitButtonMaxWidth.setOnClickListener {
      uiUtils.toggleKeyboard()
      if (isValid()) {
        viewModel.updateDispatchDetails()
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

  private fun isValid(): Boolean {
    if (viewModel.imageUrl.isNullOrEmpty()) {
      uiUtils.showSnackbar("Upload Docket image")
      return false
    }

    if (binding.trackingNumberInput.text.toString().isNullOrEmpty()) {
      uiUtils.showSnackbar("Enter tracking number")
      return false
    }
    viewModel.trackingNumber = binding.trackingNumberInput.text.toString()

    if (viewModel.dateOfDispatch.isNullOrEmpty()) {
      uiUtils.showSnackbar("Select dispatch date")
      return false
    }

    return true
  }


  private fun captureImage(
    uploadImageName: String,
    localImageName: String
  ) {
    this.uploadImageName = uploadImageName
    this.localImageName = localImageName

    val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
    val builder = AlertDialog.Builder(this)
    builder.setItems(items) { dialog, item ->
      when {
        items[item] == "Take Photo" -> requestImageCapturePermissions(true)
        items[item] == "Choose from Library" -> requestImageCapturePermissions(false)
        items[item] == "Cancel" -> dialog.dismiss()
      }
    }
    builder.show()
  }

  private fun deleteImage(
    delete: AppCompatImageView,
    image: AppCompatImageView
  ) {
    viewModel.imagePath = ""
    viewModel.imageUrl = ""
    image.visibility = View.GONE
    delete.visibility = View.GONE
    
    // Show appropriate upload container
    if (viewModel.trip != null) {
      binding.podImageCardSmall.visibility = View.GONE
      binding.addIconSmall.visibility = View.VISIBLE
    } else {
      binding.podImageCard.visibility = View.GONE
      binding.addIcon.visibility = View.VISIBLE
    }
  }

  private fun loadImage(
    path: String?,
    view: AppCompatImageView
  ) {
    view.viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
      override fun onPreDraw(): Boolean {
        view.viewTreeObserver.removeOnPreDrawListener(this)
        val imageViewHeight = view.measuredHeight
        val imageViewWidth = view.measuredWidth
        path?.let {
          GlideApp.with(view.context)
              .load(bitmapUtils.decodeSampledBitmap(path, imageViewWidth, imageViewHeight))
              .into(view)
        }
        return true
      }
    })
  }

  private fun requestImageCapturePermissions(isCamera: Boolean) {
    this.isCamera = isCamera
    compositeDisposable += requestPermission(
        arrayOf(
            Manifest.permission.CAMERA
        ).apply {
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
            uiUtils.showSnackbar(getString(string.storage_camera_permission))
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
        startActivityForResult(takePictureIntent, REQCODE_TAKE_PHOTO)
      } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }

  }

  private fun createImageFile(): File {
    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(localImageName, ".jpg", storageDir)
  }

  private fun dispatchGalleryIntent() {
    val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivityForResult(pickPhoto, REQCODE_GALLERY_PHOTO)
  }

  private fun uploadImage(
    delegationToken: DelegationToken,
    file: File
  ) {
    uiUtils.showProgress()
    val awsPath = "trips/vendor_pod/docket/$uploadImageName.jpg"
    awsUtils.startUpload(delegationToken, awsPath, file, this)
    viewModel.imagePath = file.path
  }

  override fun onDateSet(
    view: DatePicker?,
    year: Int,
    month: Int,
    dayOfMonth: Int
  ) {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, dayOfMonth)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    binding.dispatchDateInput.setText(
      "${calendar.get(Calendar.DAY_OF_MONTH)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(
          Calendar.YEAR
      )}"
    )
    viewModel.dateOfDispatch = DateUtils.formatDate(calendar.toDate(), DatePatterns.PODDateFormat)
  }

  override fun onAWSSuccess(
    path: String
  ) {
    uiUtils.hideProgress()
    uiUtils.showSnackbar(getString(R.string.msg_file_upload_successful))
    viewModel.imageUrl = path
    
    // Show image in appropriate container
    if (viewModel.trip != null) {
      binding.podImageCardSmall.visibility = View.VISIBLE
      binding.addIconSmall.visibility = View.GONE
      binding.deleteButtonSmall.visibility = View.VISIBLE
      binding.podImageSmall.visibility = View.VISIBLE
      loadImage(viewModel.imagePath, binding.podImageSmall)
    } else {
      binding.podImageCard.visibility = View.VISIBLE
      binding.addIcon.visibility = View.GONE
      binding.deleteButton.visibility = View.VISIBLE
      binding.podImage.visibility = View.VISIBLE
      loadImage(viewModel.imagePath, binding.podImage)
    }
    
    resetUploadData()
  }

  override fun onAWSFailure() {
    uiUtils.hideProgress()
    uiUtils.showSnackbar(getString(R.string.msg_file_upload_failed))
    resetUploadData()
  }

  private fun resetUploadData() {
    mPhotoFile = null
    uploadImageName = ""
    localImageName = ""
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
      REQCODE_CAMERA -> {
        requestImageCapturePermissions(isCamera)
      }
    }
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
            mPhotoFile = imageUtils.compressToFile(mPhotoFile!!, localImageName)
            uiUtils.showProgress()
            viewModel.getDelegationToken(mPhotoFile!!)
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
            val parcelFileDescriptor =
              contentResolver?.openFileDescriptor(selectedImage, "r", null)
            require(parcelFileDescriptor != null)
            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            require(
                contentResolver != null && contentResolver?.getFileName(selectedImage) != null
            )
            val imageScopedFile =
              File(cacheDir, contentResolver?.getFileName(selectedImage)!!)
            val outputStream = FileOutputStream(imageScopedFile)
            IOUtils.copy(inputStream, outputStream)

            mPhotoFile = fileCompressor.compressToFile(File(imageScopedFile.path), localImageName)
            if (mPhotoFile == null) {
              uiUtils.showToast(getString(R.string.msg_image_capture_failed))
              return
            }
            uiUtils.showProgress()
            viewModel.getDelegationToken(mPhotoFile!!)
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
private const val TransactionIdsIntentKey = "transaction_ids"
private const val TripDataIntentKey = "trip_data"

/**
 * Update docket intent
 */
fun docketUpdateIntent(
  context: Context,
  transactionIds: ArrayList<String>? = null,
  trip: HomeTripsItemData? = null
) = Intent(context, DocketUpdateActivity::class.java).apply {
  transactionIds?.let { putStringArrayListExtra(TransactionIdsIntentKey, transactionIds) }
  trip?.let { putExtra(TripDataIntentKey, trip) }
}
