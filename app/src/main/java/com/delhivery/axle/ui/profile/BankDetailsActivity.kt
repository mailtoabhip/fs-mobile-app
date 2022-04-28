package com.delhivery.axle.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.core.content.FileProvider
import androidx.databinding.library.BuildConfig
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.databinding.ActivityBankDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.kyc.aadhaar.UploadedItemRVAdapterInterface
import com.delhivery.axle.ui.kyc.gst.DocUploadAdapter
import com.delhivery.axle.ui.paymentdetails.PaymentDetailsActivity
import com.delhivery.axle.utils.AWSUtils
import com.delhivery.axle.utils.BitmapUtils
import com.delhivery.axle.utils.DialogUtilsInterface
import com.delhivery.axle.utils.FileCompressor
import com.delhivery.axle.utils.ImageUtils
import com.delhivery.axle.utils.REQCODE_CAMERA
import com.delhivery.axle.utils.REQCODE_FILE_ATTACHMENTS
import com.delhivery.axle.utils.REQCODE_TAKE_PHOTO
import com.delhivery.axle.utils.extensions.getFileName
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class BankDetailsActivity : BaseActivity<ActivityBankDetailsBinding, BankDetailsViewModel>(),
    DialogUtilsInterface, AWSUtils.AWSProgressInterface,
    UploadedItemRVAdapterInterface {



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
    @Inject
    lateinit var userPrefs: UserPrefs


    val awsPath = "loadboard/payment/"
    val docUploadAdapter : DocUploadAdapter by lazy { DocUploadAdapter(this) }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()
    var uploadArray1:ArrayList<Pair<String, String>> = ArrayList()


    lateinit var path:String
    var showProg:Boolean = false
    var download=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getKycDoc()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title =  "Payment Details"
        if(userPrefs.paymentRejectReason.isNotNullOrEmpty()){
            binding.paymentError.visibility=View.VISIBLE
            binding.paymentError.text="Payment verification failed due to "+userPrefs.paymentRejectReason
            binding.btnRetry.visibility=View.VISIBLE
            binding.btnRetry.isEnabled=true
            binding.uploadDoc1.visibility=View.GONE
            binding.btnSubmit.visibility=View.GONE
            if(userPrefs.paymentRejectReason.replace(" ", "").equals("Documentunderverification")){
                binding.paymentError.text=userPrefs.paymentRejectReason
                binding.btnRetry.visibility=View.GONE
                binding.btnSubmit.visibility=View.VISIBLE
                binding.uploadDoc1.visibility=View.VISIBLE
            }else {
                binding.paymentError.text="Payment verification failed due to "+userPrefs.paymentRejectReason
            }
        }else{
            binding.paymentError.visibility=View.GONE
            binding.btnRetry.visibility=View.GONE
            binding.btnSubmit.visibility=View.VISIBLE
            binding.uploadDoc1.visibility=View.VISIBLE
        }
        if (userPrefs.ownedTruck.isNotNullOrEmpty()) {
            if (userPrefs.ownedTruck.toInt() <= 10) {
                binding.uploadDoc1.visibility = View.VISIBLE
            } else {
                binding.uploadDoc1.visibility = View.GONE
            }
        }

        viewModel.delegationLiveData.observe(this, Observer {
            uploadImage(it.first, it.second)
        })

        viewModel.verificationDocUploadMsg.observe(this, Observer {
            uiUtils.showSnackbar(it)
        })

        viewModel.delegationDownloadLiveData.observe(this, Observer {
            if (it != null) {
                awsUtils.startDownload(it.first, it.second, it.third, this)
                viewModel.imagePath = it.third.path
            }
        })
        viewModel.accountkycDocuments.observe(this, Observer {
            if(it.isNotNullOrEmpty()){
                uploadArray1.add(
                    Pair(
                        it.substringAfter(awsPath, ""), (mPhotoFile?.length()
                        ?.div(1024)).toString()
                    )
                )
             showAccountProof()
            }
        })
        viewModel.nine4CkycDocuments.observe(this, Observer {
            if(it.isNotNullOrEmpty()){
                uploadArray.add(
                    Pair(
                        it.substringAfter(awsPath, ""), (mPhotoFile?.length()
                        ?.div(1024)).toString()
                    )
                )
               show194CSelected()
            }
        })

        binding.btnRetry.setOnClickListener {
            userPrefs.retryVerification=true
            navigationUtils.navigate(PaymentDetailsActivity::class.java,true)
        }

        binding.docRemove.setOnClickListener {
            downloadLogo(viewModel.accountkycDocuments.value?.replace(awsUtils.awsBasePath(), "")!!)
            download=true
        }
        binding.docDownload1.setOnClickListener {
            downloadLogo(viewModel.nine4CkycDocuments.value?.replace(awsUtils.awsBasePath(), "")!!)
            download=true
        }
        binding.uploadDocLay1.setOnClickListener {
            val imageName = "194C_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)
        }
        binding.docRemove1.setOnClickListener {
            showUploadImage()
        }
        binding.btnSubmit.setOnClickListener {
            sendDocForVerification(uploadArray)
        }


}
    override fun onAWSSuccess(
        path: String
    ) {
        if(!download) {
            uiUtils.hideProgress()
            uploadArray.add(
                Pair(
                    path.replace(awsPath, ""), (mPhotoFile?.length()
                    ?.div(1024)).toString()
                )
            )
            showFileSelected()
            resetUploadData()
        }else{
            uiUtils.hideProgress()
            uiUtils.showSnackbar("Document downloaded successfully")
            showProg = false
        }
    }

    override fun onAWSFailure() {
        uiUtils.hideProgress()
        uiUtils.showToast("Failed to upload")
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

    private fun downloadLogo(item: String) {
        compositeDisposable += requestPermission(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        )
            .onBackground()
            .subscribe { granted, error ->
                if (error == null && granted) {
                    showProg = true
                    uiUtils.showProgress()
                    val file = getFile(item)
                    if (file != null) {
                        viewModel.getDownloadDelegationToken(item, file)
                    } else {
                        uiUtils.showSnackbar("Can't process image")
                    }
                } else {
                    uiUtils.hideProgress()
                    uiUtils.showSnackbar(getString(R.string.storage_permission))
                }
            }
    }
    private fun getFile(item: String): File? {
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        val basePath = "$storageDir/"+System.currentTimeMillis()
        val arrString = item.split("/")
        return File(basePath + arrString[arrString.size - 1])
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
                        dispatchFileIntent()
                    }
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
                this, com.delhivery.axle.BuildConfig.APPLICATION_ID + ".provider", mPhotoFile!!
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQCODE_TAKE_PHOTO)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun getRequestAadhaarOtp() {
    }

    override fun setAccountRoleSelection(selected: String) {
    }

    override fun navigateToBusinessVerification() {
    }

    override  fun captureImage(
        uploadImageName: String,
        localImageName: String
    ) {
        this.uploadImageName =  "194C_" + System.currentTimeMillis()+"_"+userPrefs.phoneNumber+".jpg"
        this.localImageName =  "194C_" + System.currentTimeMillis()+"_"+userPrefs.phoneNumber+".jpg"

        val items = arrayOf<CharSequence>("Take Photo", "Choose a file", "Cancel")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Take Photo" -> requestImageCapturePermissions(true)
                items[item] == "Choose a file" -> requestImageCapturePermissions(false)
                items[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun showFileSelected() {
        binding.uploadDocLay1.visibility=View.GONE
        binding.docUploadedLay1.visibility=View.VISIBLE
        binding.docTitle1.setText(uploadArray.get(0).first)
        binding.docSize1.setText(uploadArray.get(0).second+" KB")
        binding.btnSubmit.isEnabled=true
        binding.docRemove1.visibility=View.VISIBLE
        binding.docDownload1.visibility=View.GONE
    }
    private fun show194CSelected() {
        binding.uploadDocLay1.visibility=View.GONE
        binding.docUploadedLay1.visibility=View.VISIBLE
        binding.docTitle1.setText(uploadArray.get(0).first)
        binding.docSize1.setText(uploadArray.get(0).second+" KB")
        binding.docRemove1.visibility=View.VISIBLE
        binding.docDownload1.visibility=View.GONE
    }
    private fun showAccountProof() {
        binding.docUploadedLay.visibility=View.VISIBLE
        binding.docTitle.setText(uploadArray1.get(0).first)
        binding.docSize.setText(uploadArray1.get(0).second+" KB")
    }

    private fun showUploadImage() {
        binding.uploadDocLay1.visibility=View.VISIBLE
        binding.docUploadedLay1.visibility=View.GONE
        if(uploadArray.size>0){
            uploadArray.clear()
        }
        binding.btnSubmit.isEnabled=false
    }


    override fun sendDocForVerification(uploadArray:ArrayList<Pair<String, String>>) {
        if(uploadArray.isNotEmpty()){
            val imageUrls= mutableListOf<String>()
            val s3url= awsUtils.awsBasePath()
            for(i in uploadArray){
                imageUrls.add(s3url+awsPath+i.first)
            }
            viewModel.verifyByDoc(imageUrls)
        }else{
            uiUtils.showToast("No file selected")
        }
        uploadArray.clear()
    }


    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(localImageName, ".jpg", storageDir)
    }

    private fun dispatchFileIntent() {
        val intent = Intent()
        intent.type = "*/*"
        val mimetypes = arrayOf("image/*", "application/pdf")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
            intent,
            REQCODE_FILE_ATTACHMENTS
        )
    }

    private fun uploadImage(
        delegationToken: DelegationToken,
        file: File
    ) {
        uiUtils.showProgress()
        val path = "$awsPath$uploadImageName"
        awsUtils.startUpload(delegationToken, path,file, this)
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
            REQCODE_FILE_ATTACHMENTS ->{
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        val selectedFile = data?.data
                        require(selectedFile != null)
                        val parcelFileDescriptor =
                            contentResolver?.openFileDescriptor(selectedFile, "r", null)
                        require(parcelFileDescriptor != null)
                        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                        require(
                            contentResolver != null && contentResolver?.getFileName(selectedFile) != null
                        )
                        val imageScopedFile =
                            File(cacheDir, contentResolver?.getFileName(selectedFile)!!)
                        val outputStream = FileOutputStream(imageScopedFile)
                        IOUtils.copy(inputStream, outputStream)
                        this.uploadImageName = "194C_" + System.currentTimeMillis()+"_"+userPrefs.phoneNumber+"."+imageScopedFile.extension
                        this.localImageName =  "194C_" + System.currentTimeMillis()+"_"+userPrefs.phoneNumber+"."+imageScopedFile.extension
                        if(imageScopedFile.extension==".jpg" ||imageScopedFile.extension==".png" || imageScopedFile.extension==".jpeg"){
                            mPhotoFile = fileCompressor.compressToFile(File(imageScopedFile.path), localImageName)
                        }else{
                            mPhotoFile = imageScopedFile
                        }

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

    override fun handleDeleteAction(item: Pair<String, String>) {
        uploadArray.remove(item)
    }


    override fun getViewModelClass() = BankDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_bank_details

    override fun requireConnection() = true

}