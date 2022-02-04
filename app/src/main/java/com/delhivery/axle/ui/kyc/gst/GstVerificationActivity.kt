package com.delhivery.axle.ui.kyc.gst

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.data.gst.GstAction_ViewDetails
import com.delhivery.axle.data.transactions.TransactionTimeOutAction
import com.delhivery.axle.databinding.*

import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.activity.transactionlist.transactionsIntent
import com.delhivery.axle.ui.kyc.aadhaar.AadhaarVerificationActivity
import com.delhivery.axle.ui.kyc.aadhaar.addressVerificationIntent
import com.delhivery.axle.ui.kyc.pan.PanVerificationActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.*
import kotlinx.android.synthetic.main.activity_verify_pan.*
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


class GstVerificationActivity  : BaseActivity<ActivityVerifyGstBinding, GstVerificationViewModel>(), GstRVAdapterInterface, AWSUtils.AWSProgressInterface {
    init {
        StatusBarColor = Color.parseColor("#ededff")
    }
    override fun getViewModelClass() = GstVerificationViewModel::class.java

    override fun layoutId() = R.layout.activity_verify_gst

    private val gstRVAdapter by lazy { GstRVAdapter(this) }

    override fun requireConnection() = false

    private val pan_number:String? = null

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

    val awsPath = "loadboard/gst/"
    val docUploadAdapter :DocUploadAdapter by lazy { DocUploadAdapter() }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(intent?.extras!=null){
            viewModel.currentStep = navigationUtils.getNavigationStepFormat(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!, intent?.extras?.getInt(
                TotalStepsKey)!!)
            progress.progress = navigationUtils.getNavigationPercentage(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
                TotalStepsKey)!!)
        }

    }

    /*show verification options dialog*/
    private fun showVerifcationOptionsDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogVerifyGstBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.gstDocLayout.setOnClickListener {
            val imageName = "GST_doc_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)
            dialog.dismiss()
        }

        bindingDialog.verifyOtpLayout.setOnClickListener {
            showVerifcationOtpDialog()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
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

    private fun uploadImage(
            delegationToken: DelegationToken,
            file: File
    ) {
        uiUtils.showProgress()
        val path = "$awsPath$uploadImageName"
        awsUtils.startUpload(delegationToken, path,file, this)
    }

    /*show gst verification otp dialog*/
    private fun showVerifcationOtpDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogVerifyGstOtpBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.closeBtn.setOnClickListener {
            showVerifcationOptionsDialog()
            dialog.dismiss()
        }

        bindingDialog.buttonShare.setOnClickListener {
            dialog.dismiss()
           // navigationUtils.navigate( aadhaarVerificationIntent(this), false)
            navigationUtils.checkNavigationKycStep(this,intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
                TotalStepsKey)!!,null)
        }
        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigationUtils.navigate(PanVerificationActivity::class.java, true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.gstList.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@GstVerificationActivity.gstRVAdapter
        }

        viewModel.gstLiveData.observe(this, Observer {
            it?.let { _items ->
                gstRVAdapter.operation(_items)
            }
        })

        pan_number?.let { refreshData(it) }

        binding.btnVerifyGst.setOnClickListener {
          showVerifcationOptionsDialog()
        }

        viewModel.delegationLiveData.observe(this, Observer {
            uploadImage(it.first, it.second)
        })
    }

    private fun refreshData(pan_number:String) {
        viewModel.getGstDetails(pan_number)
    }

    override fun handleAction(
            actionId: String,
            item: BaseGstRVAdapterItem<*>
    ) {
        when (actionId) {
            GstAction_ViewDetails -> {
                //val dataItem = item.data as? GstDetailData
                //dataItem?.let { showVerifcationOptionsDialog(it) }
                showVerifcationOptionsDialog()
            }

            TransactionTimeOutAction -> {
                pan_number?.let { refreshData(it) }
            }
        }
    }

    override fun onAWSSuccess(
            path: String
    ) {
        uiUtils.hideProgress()
        uploadArray.add(Pair(path.replace(awsPath,""), (mPhotoFile?.length()?.div(1024)).toString()))
       showAttachmentDialog()
        resetUploadData()
    }

    override fun onAWSFailure() {
        uiUtils.hideProgress()
        showUploadFailDialog()
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

    /*show attachment dialog*/
    private fun showAttachmentDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogGstAttachmentsBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        docUploadAdapter.setItems(uploadArray)
        bindingDialog.attachmentList.adapter = this@GstVerificationActivity.docUploadAdapter

        bindingDialog.closeBtn.setOnClickListener {
            showVerifcationOptionsDialog()
            dialog.dismiss()
        }

        bindingDialog.buttonSubmit.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.buttonUploadMore.setOnClickListener {
            val imageName = "GST_doc_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    /*show upload fail dialog*/
    private fun showUploadFailDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogGstUploadErrorBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.buttonUploadAgain.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

}
fun aadhaarVerificationIntent(
    context: Context
) = Intent(context, AadhaarVerificationActivity::class.java).apply {
}