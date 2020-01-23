package com.delhivery.axle.ui.home.activity.docket

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewTreeObserver.OnPreDrawListener
import android.widget.DatePicker
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DelegationToken
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
import com.delhivery.axle.utils.REQCODE_STORAGE
import com.delhivery.axle.utils.REQCODE_TAKE_PHOTO
import com.delhivery.axle.utils.extensions.getFileName
import com.delhivery.axle.utils.extensions.toDate
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
class DocketUpdateActivity : BaseActivity<ActivityUpdateDocketBinding, DocketUpdateViewModel>(),
    OnDateSetListener, AWSProgressInterface {

  override fun getViewModelClass() = DocketUpdateViewModel::class.java

  override fun layoutId() = R.layout.activity_update_docket

  override fun requireConnection() = true

  private var isCamera: Boolean = false
  private var mPhotoFile: File? = null
  private lateinit var uploadImageName: String
  private lateinit var localImageName: String
  @Inject lateinit var imageUtils: ImageUtils
  @Inject lateinit var awsUtils: AWSUtils
  @Inject lateinit var fileCompressor: FileCompressor
  @Inject lateinit var bitmapUtils: BitmapUtils

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    /* validate intent */
    require(
        !(intent == null || !intent.hasExtra(TransactionIdsIntentKey))
    ) { "Required data $TransactionIdsIntentKey not found" }

    /* set transaction id */
    viewModel.transactionIds =
      intent.getStringArrayListExtra(TransactionIdsIntentKey) ?: mutableListOf()
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    setSupportActionBar(binding.toolbar)
    title = "Dispatch Details"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    viewModel.delegationLiveData.observe(this, Observer {
      uploadImage(it.first, it.second)
    })

    viewModel.statusLiveData.observe(this, Observer {
      if (it) {
        setResult(Activity.RESULT_OK)
        finish()
      }
    })

    binding.containerImage.setOnClickListener {
      val imageName = "docket_" + System.currentTimeMillis()
      if (viewModel.imageUrl.isNullOrEmpty()) captureImage(imageName, imageName)
      else startActivity(imageViewIntent(this, viewModel.imageUrl, "Docket Image"))
    }

    binding.deleteImage.setOnClickListener {
      deleteImage(binding.deleteImage, binding.image)
    }

    binding.textDate.setOnClickListener {
      dialogUtils.datePicker(listener = this)
    }

    binding.imgDate.setOnClickListener {
      dialogUtils.datePicker(listener = this)
    }

    binding.btnSave.setOnClickListener {
      uiUtils.toggleKeyboard()
      if (isValid()) {
        viewModel.updateDispatchDetails()
      }
    }
  }

  private fun isValid(): Boolean {
    if (viewModel.imageUrl.isNullOrEmpty()) {
      uiUtils.showSnackbar("Upload Docket image")
      return false
    }

    if (binding.textTrackingNumber.text.toString().isNullOrEmpty()) {
      uiUtils.showSnackbar("Enter traking number")
      return false
    }
    viewModel.trackingNumber = binding.textTrackingNumber.text.toString()

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
    image.setImageResource(R.drawable.ic_camera)
    delete.visibility = View.GONE
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

    val storagePermission =
      ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val cameraPermission =
      ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
    if (storagePermission != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(
          this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
          REQCODE_STORAGE
      )
    } else if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQCODE_CAMERA)
    } else {
      if (isCamera) {
        dispatchTakePictureIntent()
      } else {
        dispatchGalleryIntent()
      }
    }
  }

  private fun dispatchTakePictureIntent() {
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (takePictureIntent.resolveActivity(packageManager) != null) {
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
    binding.textDate.text =
      "${calendar.get(Calendar.DAY_OF_MONTH)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(
          Calendar.YEAR
      )}"
    viewModel.dateOfDispatch = DateUtils.formatDate(calendar.toDate(), DatePatterns.PODDateFormat)
  }

  override fun onAWSSuccess(
    path: String
  ) {
    uiUtils.hideProgress()
    uiUtils.showSnackbar(getString(R.string.msg_file_upload_successful))
    viewModel.imageUrl = path
    binding.deleteImage.visibility = View.VISIBLE
    loadImage(viewModel.imagePath, binding.image)
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

/**
 * Update docket intent
 */
fun docketUpdateIntent(
  context: Context,
  transactionIds: ArrayList<String>
) = Intent(context, DocketUpdateActivity::class.java).apply {
  putStringArrayListExtra(TransactionIdsIntentKey, transactionIds)
}
