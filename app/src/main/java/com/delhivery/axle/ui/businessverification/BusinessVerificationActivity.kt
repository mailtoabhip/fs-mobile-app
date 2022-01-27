package com.delhivery.axle.ui.businessverification

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioGroup
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityBusinessVerificationBinding
import com.delhivery.axle.databinding.DialogBusinessVerificationAttachmentBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.getFileName
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


class BusinessVerificationActivity : BaseActivity<ActivityBusinessVerificationBinding,BusinessVerificationViewModel>() {
   // val rg :RadioGroup = RadioGroup(this)

    private var isCamera: Boolean = false
    private var mPhotoFile: File? = null
    private lateinit var uploadImageName: String
    private lateinit var localImageName: String
    @Inject
    lateinit var imageUtils: ImageUtils
    @Inject
    lateinit var awsUtils: AWSUtils
    @Inject
    lateinit var fileCompressor: FileCompressor
    @Inject
    lateinit var bitmapUtils: BitmapUtils

    val awsPath = "loadboard/business/"
    val docUploadAdapter :DocUploadAdapter by lazy { DocUploadAdapter() }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        rg.addView(binding.textTruck)
//        rg.addView(binding.textLR)

//        rg.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
//            when (i) {
//                R.id.text_truck -> {
//                    viewModel.selected.postValue(true)
//
//                }
//                R.id.text_LR -> {
//                    viewModel.selected.postValue(false)
////                    val intent = Intent()
////                        .setType("*/*")
////                        .setAction(Intent.ACTION_GET_CONTENT)
////
////                    startActivityForResult(Intent.createChooser(intent, "Select a file"),
////                        Companion.REQUESTFILECHOOSERCODE
////                    )
//                    val imageName = "LR_doc_" + System.currentTimeMillis()+".jpg"
//                    captureImage(imageName, imageName)
//
//                }
//            }
//        })


        binding.textTruck.setOnClickListener{
            binding.editTruck.visibility=View.VISIBLE
            binding.textLR.isSelected=false

        }
        viewModel.truckNumber.observe(this, Observer {
            if(it.length==9){
                binding.btnVerifyBusiness.isEnabled=true
            }

        }

        )

        binding.textLR.setOnClickListener{
            binding.editTruck.visibility=View.GONE
            binding.textTruck.isSelected=false
            val imageName = "LR_doc_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)
        }

        binding.btnVerifyBusiness.setOnClickListener {


        }

    }

    private fun requestImageCapturePermissions(isCamera: Boolean) {
        this.isCamera = isCamera
        compositeDisposable += requestPermission(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
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

    private fun showFileSelected() {
        val dialog = Dialog(this)
        val bindingDialog= DialogBusinessVerificationAttachmentBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        docUploadAdapter.setItems(uploadArray)
        bindingDialog.attachmentList.adapter = this@BusinessVerificationActivity.docUploadAdapter

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.buttonSubmit.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.buttonUploadMore.setOnClickListener {
            val imageName = "LR_doc_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.Animation_Design_BottomSheetDialog
        dialog.window!!.setGravity(Gravity.BOTTOM)
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


//    override fun onAWSSuccess(
//        path: String
//    ) {
//        uiUtils.hideProgress()
//        uploadArray.add(Pair(path.replace(awsPath,""), (mPhotoFile?.length()?.div(1024)).toString()))
//        showFileSelected()
//        resetUploadData()
//    }
//
//    override fun onAWSFailure() {
//        uiUtils.hideProgress()
//        resetUploadData()
//    }
    private fun resetUploadData() {
        mPhotoFile = null
        uploadImageName = ""
        localImageName = ""
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
                       // viewModel.getDelegationToken(mPhotoFile!!)
                        uploadArray.add(Pair("BUSI_23-04-2021_08:30:26", (mPhotoFile?.length()?.div(1024)).toString()))
                        showFileSelected()
                        uiUtils.hideProgress()

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
                        uploadArray.add(Pair("BUSI_23-04-2021_08:30:24", (mPhotoFile?.length()?.div(1024)).toString()))
                        showFileSelected()
                        uiUtils.hideProgress()
                    } catch (e: IOException) {
                        uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                    }
                } else {
                    uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                }
            }
        }
    }





    override fun getViewModelClass()= BusinessVerificationViewModel::class.java

    override fun layoutId()=R.layout.activity_business_verification

    override fun requireConnection()= false

    companion object {
        private const val REQUESTFILECHOOSERCODE : Int = 111
    }

}