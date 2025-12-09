package com.delhivery.axle.ui.home.activity.docket

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog.OnDateSetListener
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
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.DatePicker
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.amazonaws.util.IOUtils
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.data.DocketState
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.PODStatus
import com.delhivery.axle.databinding.ActivityHpodDetailsBinding
import com.delhivery.axle.databinding.DialogEpodSuccessBinding
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
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
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
import androidx.core.graphics.toColorInt

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
  private var currentDocketId: Int = 1 // Currently selected docket ID
  
  @Inject lateinit var imageUtils: ImageUtils
  @Inject lateinit var awsUtils: AWSUtils
  @Inject lateinit var fileCompressor: FileCompressor
  @Inject lateinit var bitmapUtils: BitmapUtils
  @Inject lateinit var userPrefs: UserPrefs
  
  private lateinit var docketAdapter: DocketAdapter
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true
  private var podStatus: PODStatus? = null
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
      // Extract POD status from trip data
      podStatus = viewModel.trip?.podAction()
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
    
    // Update badge styling based on pod_status
    updatePodStatusBadge()
    
    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        userPrefs.setPreviousScreen(this.javaClass.name)
        finish()
      }
    })
    
    // Set disabled text color for submit button programmatically
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
    
    // Set up RecyclerView
    setupRecyclerView()
    
    // Initialize dockets
    viewModel.initializeDockets()
    
    // Initially show big container, hide RecyclerView
    binding.podContainerMaxWidth.visibility = View.VISIBLE
    binding.docketRecyclerView.visibility = View.GONE
    
    // Big container click listener
    binding.podContainerMaxWidth.setOnClickListener {
      // Start upload process to make docket available
      viewModel.startUploadProcess()
      
      // Hide big container, show RecyclerView
      binding.podContainerMaxWidth.visibility = View.GONE
      binding.docketRecyclerView.visibility = View.VISIBLE
      
      // Immediately open camera/gallery dialog
      currentDocketId = 1
      val imageName = "docket_${viewModel.transactionIds.firstOrNull() ?: System.currentTimeMillis()}_1"
      captureImage(imageName, imageName)
    }
    
    if (viewModel.trip != null) {
      viewModel.transactionIds.add(viewModel.trip!!.transactionId)
      binding.trackingNumberInput.setText(viewModel.trip!!.podDispatchAwbNumber)
      binding.dispatchDateInput.setText(viewModel.trip!!.podDispatchDate)
      
      // Set the dateOfDispatch in viewModel for validation
      if (!viewModel.trip!!.podDispatchDate.isNullOrEmpty()) {
        viewModel.dateOfDispatch = viewModel.trip!!.podDispatchDate.toString()
      }
      
      // Load existing image if available
      if (!viewModel.trip!!.podDispatchDocketImage.isNullOrEmpty()) {
        // Show RecyclerView directly with existing image
        binding.podContainerMaxWidth.visibility = View.GONE
        binding.docketRecyclerView.visibility = View.VISIBLE
        viewModel.loadExistingDocket(viewModel.trip!!.podDispatchDocketImage.toString())
      }
      // If no existing image, big container stays visible (user clicks to start)
    }
    // If new upload (trip == null), big container stays visible (user clicks to start)

    // Observe docket items changes
    viewModel.docketItemsLiveData.observe(this, Observer { items ->
      docketAdapter.updateItems(items)
      
      // Show/hide big container vs RecyclerView based on active dockets
      val hasActiveDockets = items.any { it.state != DocketState.EMPTY }
      if (hasActiveDockets) {
        binding.podContainerMaxWidth.visibility = View.GONE
        binding.docketRecyclerView.visibility = View.VISIBLE
      } else {
        // Revert to initial state if no active dockets
        binding.podContainerMaxWidth.visibility = View.VISIBLE
        binding.docketRecyclerView.visibility = View.GONE
      }
      
      // Enable/disable Submit button based on form validation
      validateForm()
    })

    viewModel.delegationLiveData.observe(this, Observer {
      uploadImage(it.first, it.second)
    })

    viewModel.statusLiveData.observe(this, Observer { isSuccess ->
      uiUtils.hideProgress()
      if (isSuccess) {
        showSuccessDialog(isSuccess)
      } else {
        // Re-validate form and enable submit button on failure
        validateForm()
      }
    })
    
    // Add text change listener for tracking number to validate form
    binding.trackingNumberInput.addTextChangedListener(object : android.text.TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
      override fun afterTextChanged(s: android.text.Editable?) {
        validateForm()
      }
    })

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
        // Show progress at the start of the entire process
        uiUtils.showProgress()
        
        // Check if there are selected dockets to upload first
        val selectedDockets = viewModel.getSelectedDockets()
        if (selectedDockets.isNotEmpty()) {
          // Upload selected dockets first, then update dispatch details
          uploadSelectedDockets()
        } else {
          // No new images to upload, directly update dispatch details
          viewModel.updateDispatchDetails()
        }
      }
    }
  }
  
  /**
   * Update the pod status badge styling based on pod_status from trip data
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
        badge.text = "ePOD Pending"
        badge.backgroundTintList = ColorStateList.valueOf("#FFF7EB".toColorInt())
        badge.setTextColor(ContextCompat.getColor(this, R.color.pending_font))
      }
    }
  }
  
  private fun setupRecyclerView() {
    docketAdapter = DocketAdapter(
        onDocketClick = { docketId ->
          currentDocketId = docketId
          val docketItem = viewModel.getDocketItems().find { it.id == docketId }
          
          // If already uploaded, view the image
          if (docketItem?.state == DocketState.UPLOADED && docketItem.imageUrl != null) {
            viewImage(docketItem.imageUrl, "Tracking Picture")
            return@DocketAdapter
          }
          
          val imageName = "docket_${viewModel.transactionIds}_$docketId"
          captureImage(imageName, imageName)
        },
        onDeleteClick = { docketId ->
          viewModel.deleteDocket(docketId)
        },
        bitmapUtils = bitmapUtils
    )
    
    binding.docketRecyclerView.apply {
      layoutManager = GridLayoutManager(this@DocketUpdateActivity, 4)
      adapter = docketAdapter
    }
  }
  
  /**
   * Validate form and enable/disable submit button
   */
  private fun validateForm() {
    val docketItems = viewModel.getDocketItems()
    val hasDocketImage = docketItems.any { 
      it.state == DocketState.SELECTED || it.state == DocketState.UPLOADED 
    }
    
    val hasTrackingNumber = !binding.trackingNumberInput.text.toString().trim().isEmpty()
    val hasDispatchDate = !viewModel.dateOfDispatch.isNullOrEmpty()
    
    // Enable submit button only if all fields are filled
    binding.submitButtonMaxWidth.isEnabled = hasDocketImage && hasTrackingNumber && hasDispatchDate
  }

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }

  private fun isValid(): Boolean {
    // Check if docket image is selected or uploaded
    val docketItems = viewModel.getDocketItems()
    val hasImage = docketItems.any { 
      it.state == DocketState.SELECTED || it.state == DocketState.UPLOADED 
    }
    
    if (!hasImage) {
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
        items[item] == "Cancel" -> {
          dialog.dismiss()
          // If user cancels and we're in initial state, reset dockets
          if (isInitialState()) {
            viewModel.resetDockets()
          }
        }
      }
    }
    builder.setCancelable(false)
    builder.show()
    builder.setOnCancelListener {
      // If user cancels (back button) and we're in initial state, reset dockets
      if (isInitialState()) {
        viewModel.resetDockets()
      }
    }
  }
  
  /**
   * Check if we are in the initial state (only first docket is available, nothing else)
   */
  private fun isInitialState(): Boolean {
    val activeDockets = viewModel.getDocketItems().filter { it.state != DocketState.EMPTY }
    return activeDockets.size == 1 && activeDockets[0].id == 1 && activeDockets[0].state == DocketState.AVAILABLE
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
    // Progress is already shown at the start of submit process
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
    
    // Validate form after date is set
    validateForm()
  }

  override fun onAWSSuccess(
    path: String
  ) {
    uiUtils.showSnackbar(getString(R.string.msg_file_upload_successful))
    
    // Update viewModel with upload success
    viewModel.onUploadSuccess(currentDocketId, path)
    
    resetUploadData()
    
    // After upload success, check if there are more selected dockets to upload
    val selectedDockets = viewModel.getSelectedDockets()
    if (selectedDockets.isNotEmpty()) {
      // Upload next selected docket (progress already showing)
      uploadSelectedDockets()
    } else {
      // All uploads complete, now update dispatch details (progress still showing)
      viewModel.updateDispatchDetails()
    }
  }

  override fun onAWSFailure() {
    uiUtils.hideProgress()
    uiUtils.showSnackbar(getString(R.string.msg_file_upload_failed))
    
    // Update viewModel with upload failure
    viewModel.onUploadFailure(currentDocketId)
    
    resetUploadData()
  }

  private fun uploadSelectedDockets() {
    val selectedDockets = viewModel.getSelectedDockets()
    if (selectedDockets.isEmpty()) {
      viewModel.updateDispatchDetails()
      return
    }
    
    // Upload first selected docket
    val docket = selectedDockets.first()
    currentDocketId = docket.id
    
    docket.imagePath?.let { path ->
      val file = File(path)
      if (file.exists()) {
        // Set upload image name before starting upload
        uploadImageName = "docket_${viewModel.transactionIds.firstOrNull() ?: System.currentTimeMillis()}_${docket.id}"
        viewModel.startUpload(currentDocketId, file)
      } else {
        uiUtils.hideProgress()
        uiUtils.showSnackbar("Image file not found")
      }
    } ?: run {
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Image file not found")
    }
  }
  
  private fun resetUploadData() {
    mPhotoFile = null
    uploadImageName = ""
    localImageName = ""
  }
  
  private fun viewImage(
    path: String?,
    title: String
  ) {
    if (path.isNotNullOrEmpty()) {
      startActivity(imageViewIntent(this, path ?: "", title))
    }
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
            
            // Set image as selected in viewModel (will upload on Submit)
            viewModel.setImageSelected(currentDocketId, mPhotoFile!!.path)
            
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
            
            // Set image as selected in viewModel (will upload on Submit)
            viewModel.setImageSelected(currentDocketId, mPhotoFile!!.path)
            
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
  
  private fun showSuccessDialog(isSuccess: Boolean) {
    if (isFinishing || !isSuccess) return
    
    val dialog = Dialog(this)
    val bindingDialog = DialogEpodSuccessBinding.inflate(layoutInflater)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(bindingDialog.root)

    bindingDialog.textTitle.text = "Courier details submitted successfully!"
    bindingDialog.textMessage.visibility = View.GONE
    bindingDialog.iconSuccess.setImageResource(R.drawable.ic_green_tick)

    // Done button listener
    bindingDialog.doneButtonMaxWidth.setOnClickListener {
        dialog.dismiss()
        setResult(Activity.RESULT_OK)
        finish()
    }

    dialog.show()
    dialog.setCancelable(false)
    dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
    dialog.window!!.setGravity(Gravity.BOTTOM)
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
