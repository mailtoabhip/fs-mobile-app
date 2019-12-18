package com.delhivery.axle.ui.tripdetails

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.view.View
import android.view.ViewTreeObserver.OnPreDrawListener
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.databinding.ActivityUploadImageBinding
import com.delhivery.axle.injection.module.GlideApp
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
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * Created by saurabh
 * for Delhivery Private Limited
 **
 *
 * Activity to upload images
 *
 **
 */
class UploadImageActivity : BaseActivity<ActivityUploadImageBinding, UploadImageViewModel>(),
    AWSProgressInterface {

  override fun getViewModelClass() = UploadImageViewModel::class.java

  override fun layoutId() = R.layout.activity_upload_image

  override fun requireConnection() = true

  private lateinit var uploadImageName: String
  private lateinit var localImageName: String
  private var isCamera: Boolean = false
  private var mPhotoFile: File? = null
  @Inject lateinit var fileCompressor: FileCompressor
  @Inject lateinit var awsUtils: AWSUtils
  @Inject lateinit var bitmapUtils: BitmapUtils

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    /* validate intent */
    try {
      require(
          !(intent == null || !intent.hasExtra(TransactionIdIntentKey))
      ) { "Required data $TransactionIdIntentKey not found" }
    } catch (e: Exception) {
      finish()
    }

    viewModel.transactionId = intent?.getStringExtra(TransactionIdIntentKey) ?: ""
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    title = "Upload ePod"

    viewModel.delegationLiveData.observe(this, Observer {
      uploadImage(it.first, it.second)
    })

    viewModel.uploadResultLiveData.observe(this, Observer {
      if (it) {
        setResult(Activity.RESULT_OK)
        finish()
      } else {
        uiUtils.showSnackbar("Upload Failed, Please try again")
      }
    })

    binding.imagePod1.setOnClickListener {
      captureImage(1)
    }
    binding.imagePod2.setOnClickListener {
      captureImage(2)
    }
    binding.imagePod3.setOnClickListener {
      captureImage(3)
    }
    binding.imagePod4.setOnClickListener {
      captureImage(4)
    }
    binding.imagePod5.setOnClickListener {
      captureImage(5)
    }
    binding.imagePod6.setOnClickListener {
      captureImage(6)
    }

    binding.deletePod1.setOnClickListener {
      deleteImage(1, binding.deletePod1, binding.imagePod1)
    }
    binding.deletePod2.setOnClickListener {
      deleteImage(2, binding.deletePod2, binding.imagePod2)
    }
    binding.deletePod3.setOnClickListener {
      deleteImage(3, binding.deletePod3, binding.imagePod3)
    }
    binding.deletePod4.setOnClickListener {
      deleteImage(4, binding.deletePod4, binding.imagePod4)
    }
    binding.deletePod5.setOnClickListener {
      deleteImage(5, binding.deletePod5, binding.imagePod5)
    }
    binding.deletePod6.setOnClickListener {
      deleteImage(6, binding.deletePod6, binding.imagePod6)
    }

    binding.btnAction.setOnClickListener {
      if (!viewModel.imageUrls.isNullOrEmpty())
        viewModel.uploadPod()
    }
  }

  private fun captureImage(num: Int) {
    if (viewModel.imagePaths.isNullOrEmpty()) {
      startCapture(
          "${viewModel.transactionId}-1", "vendor_pod_${viewModel.transactionId}-1"
      )
    } else {
      for (url in viewModel.imageUrls) {
        if (url.endsWith("$num.jpg")) {
          viewImage(url, "View ePod $num")
          return
        }
      }
      startCapture(
          "${viewModel.transactionId}-$num",
          "vendor_pod_${viewModel.transactionId}-$num"
      )
    }
  }

  private fun deleteImage(
    num: Int,
    delete: AppCompatImageView,
    image: AppCompatImageView
  ) {
    with(viewModel.imageUrls.iterator()) {
      forEach {
        if (it.endsWith("$num.jpg")) {
          remove()
        }
      }
    }
    with(viewModel.imagePaths.iterator()) {
      forEach {
        if (it.endsWith("$num")) {
          remove()
        }
      }
    }
    image.setImageResource(R.drawable.ic_camera)
    delete.visibility = View.GONE
  }

  private fun uploadImage(
    delegationToken: DelegationToken,
    file: File
  ) {
    uiUtils.showProgress()
    val awsPath = "trips/vendor_pod/${viewModel.transactionId}/" + uploadImageName + ".jpg"
    awsUtils.startUpload(delegationToken, awsPath, file, this)
    viewModel.imagePaths.add(file.path)
  }

  override fun onAWSSuccess(
    path: String
  ) {
    if (!isFinishing) {
      analyticsUtil.trackEvent(
          EVENT_POD_UPLOAD,
          mutableListOf(PROPERTY_STATUS),
          mutableListOf(VALUE_SUCCESS)
      )
      uiUtils.hideProgress()
      uiUtils.showSnackbar(getString(R.string.msg_file_upload_successful))
      viewModel.imageUrls.add(path)
      updateView()
      resetUploadData()
    }
  }

  override fun onAWSFailure() {
    if (!isFinishing) {
      analyticsUtil.trackEvent(
          EVENT_POD_UPLOAD,
          mutableListOf(PROPERTY_STATUS),
          mutableListOf(VALUE_FAILURE)
      )
      uiUtils.hideProgress()
      uiUtils.showSnackbar(getString(R.string.msg_file_upload_failed))
      resetUploadData()
    }
  }

  private fun resetUploadData() {
    mPhotoFile = null
    uploadImageName = ""
    localImageName = ""
  }

  private fun updateView() {
    if (!viewModel.imagePaths.isNullOrEmpty()) {
      for (path in viewModel.imagePaths) {
        when {
          path.endsWith("1") -> {
            binding.deletePod1.visibility = View.VISIBLE
            loadImage(path, binding.imagePod1)
          }
          path.endsWith("2") -> {
            binding.deletePod2.visibility = View.VISIBLE
            loadImage(path, binding.imagePod2)
          }
          path.endsWith("3") -> {
            binding.deletePod3.visibility = View.VISIBLE
            loadImage(path, binding.imagePod3)
          }
          path.endsWith("4") -> {
            binding.deletePod4.visibility = View.VISIBLE
            loadImage(path, binding.imagePod4)
          }
          path.endsWith("5") -> {
            binding.deletePod5.visibility = View.VISIBLE
            loadImage(path, binding.imagePod5)
          }
          path.endsWith("6") -> {
            binding.deletePod6.visibility = View.VISIBLE
            loadImage(path, binding.imagePod6)
          }
        }
      }
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
          if (checkPermission(arrayOf(WRITE_EXTERNAL_STORAGE, CAMERA))) {
            dispatchTakePictureIntent()
          }
        items[item] == "Choose from Library" ->
          if (checkPermission(arrayOf(WRITE_EXTERNAL_STORAGE, CAMERA))) {
            dispatchGalleryIntent()
          }
        items[item] == "Cancel" -> dialog.dismiss()
      }
    }
    builder.show()
  }

  private fun createImageFile(): File {
    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(localImageName, ".jpg", storageDir)
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
          val selectedImage = data?.data
          try {
            mPhotoFile = fileCompressor.compressToFile(
                File(getRealPathFromUri(selectedImage)),
                localImageName
            )
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

  private fun getRealPathFromUri(uri: Uri?): String {
    if (uri != null) {
      var result: String? = null
      val proj = arrayOf(Media.DATA)
      val cursor = contentResolver.query(uri, proj, null, null, null)
      if (cursor != null) {
        if (cursor.moveToFirst()) {
          val columnIndex = cursor.getColumnIndexOrThrow(proj[0])
          result = cursor.getString(columnIndex)
        }
        cursor.close()
      }
      if (result == null) {
        result = ""
      }
      return result
    }
    return ""
  }

}

/* intent keys */
private const val TransactionIdIntentKey = "transaction_id"

/**
 * Upload Image intent
 */
fun uploadImageIntent(
  context: Context,
  transactionId: String
) = Intent(context, UploadImageActivity::class.java).apply {
  putExtra(TransactionIdIntentKey, transactionId)
}
