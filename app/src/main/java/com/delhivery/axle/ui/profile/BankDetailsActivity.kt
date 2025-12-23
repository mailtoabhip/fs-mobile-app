package com.delhivery.axle.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.transition.Transition
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DocumentFile
import com.delhivery.axle.api.response.FileData
import com.delhivery.axle.databinding.ActivityBankDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.kyc.aadhaar.UploadedItemRVAdapterInterface
import com.delhivery.axle.ui.kyc.gst.DocUploadAdapter
import com.delhivery.axle.ui.paymentdetails.PaymentDetailsActivity
import com.delhivery.axle.utils.DocumentUtils
import com.delhivery.axle.utils.BitmapUtils
import com.delhivery.axle.utils.DialogUtilsInterface
import com.delhivery.axle.utils.EVENT_DOC_UPLOADED_WITH_WRONG_EXTENSION
import com.delhivery.axle.utils.FileCompressor
import com.delhivery.axle.utils.ImageUtils
import com.delhivery.axle.utils.constants.FileType
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_SOURCE_PAGE
import com.delhivery.axle.utils.PROPERTY_TYPE_OF_DOC
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.REQCODE_CAMERA
import com.delhivery.axle.utils.REQCODE_FILE_ATTACHMENTS
import com.delhivery.axle.utils.REQCODE_TAKE_PHOTO
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.getFileName
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class BankDetailsActivity : BaseActivity<ActivityBankDetailsBinding, BankDetailsViewModel>(),
    DialogUtilsInterface, DocumentUtils.DocumentProgressInterface, DocumentUtils.DocumentListInterface,
    UploadedItemRVAdapterInterface {



    private var isCamera: Boolean = false
    private var mPhotoFile: File? = null
    private lateinit var uploadImageName: String
    private lateinit var localImageName: String
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true
    @Inject
    lateinit var imageUtils: ImageUtils
    @Inject
    lateinit var documentUtils: DocumentUtils
    @Inject
    lateinit var fileCompressor: FileCompressor
    @Inject
    lateinit var bitmapUtils: BitmapUtils
    @Inject
    lateinit var userPrefs: UserPrefs


    val docUploadAdapter : DocUploadAdapter by lazy { DocUploadAdapter(this) }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()
    var uploadArray1:ArrayList<Pair<String, String>> = ArrayList()


    lateinit var path:String
    var showProg:Boolean = false
    var download=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("BankDetailsActivity_SetupTime")
        activitySetupTrace?.start()
        viewModel.getKycDoc()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)

    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title =  "Payment Details"

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                userPrefs.setPreviousScreen(this.javaClass.name)
                finish()
            }
        })

        if (userPrefs.ownedTruck.isNotNullOrEmpty()) {
            if (userPrefs.ownedTruck.toInt() <= 10) {
                binding.uploadDoc1.visibility = View.VISIBLE
            } else {
                binding.uploadDoc1.visibility = View.GONE
            }
        }

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

        // Removed delegation token observer - using direct DocumentUtils upload now

        viewModel.verificationDocUploadMsg.observe(this, Observer {
            uiUtils.showSnackbar(it)
            binding.btnSubmit.isEnabled=false
        })
        viewModel.accountkycDocuments.observe(this, Observer {
            if(it.isNotNullOrEmpty()){
                uploadArray1.add(
                    Pair(
                        it.substringAfterLast("/"), (mPhotoFile?.length()
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
                        it.substringAfterLast("/"), (mPhotoFile?.length()
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
            try{
                downloadLogo("passbook_front_page")
                download = true
            } catch (e: Exception) {
                //uiUtils.showSnackbar("Error: ${e.message}")
            }
        }
        binding.docDownload1.setOnClickListener {
            try{
                downloadLogo("section_194C")
                download = true
            } catch (e: Exception) {
                //uiUtils.showSnackbar("Error: ${e.message}")
            }
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
    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

    override fun onDocumentSuccess(downloadUrl: String) {
        if(!download) {
            uiUtils.hideProgress()
            // Store downloadUrl (from BE response) instead of filename
            // This downloadUrl will be sent to /upload_document API for verification
            uploadArray.add(
                Pair(
                    downloadUrl, (mPhotoFile?.length()
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

    override fun onDocumentFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Failed to upload: $error")
        resetUploadData()
    }

    override fun onDocumentListSuccess(documents: List<FileData>) {
        if (!isFinishing && download && documents.isNotEmpty()) {
            uiUtils.hideProgress()

            val document = documents.first()
            val localFile = getFile(document.filename)

            localFile?.let { file ->
                downloadImageFromUrl(document.downloadUrl, file)
            }
        }
    }

    private fun downloadImageFromUrl(downloadUrl: String, localFile: File) {
        Glide.with(this)
            .download(downloadUrl)
            .into(object : CustomTarget<File>() {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    uiUtils.showSnackbar("Download failed")
                    showProg = false
                    download = false
                }

                override fun onResourceReady(
                    resource: File,
                    transition: com.bumptech.glide.request.transition.Transition<in File>?
                ) {
                    try {
                        resource.copyTo(localFile, overwrite = true)
                        uiUtils.showSnackbar("Document downloaded successfully")
                        showProg = false
                        download = false
                    } catch (e: Exception) {
                        uiUtils.showSnackbar("Failed to save file: ${e.message}")
                        showProg = false
                        download = false
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
    }

    override fun onDocumentListFailure(error: String) {
        uiUtils.showSnackbar(error)
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
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
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
                            documentUtils.listDocuments(item, this)
                        } else {
                            uiUtils.showSnackbar("Can't process image")
                        }
                    } else {
                        uiUtils.hideProgress()
                        uiUtils.showSnackbar(getString(R.string.storage_permission))
                    }
                }
        } else {
            showProg = true
            uiUtils.showProgress()
            val file = getFile(item)
            if (file != null) {
                documentUtils.listDocuments(item, this)
            } else {
                uiUtils.showSnackbar("Can't process image")
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
            // Extract downloadUrls from uploadArray (first element of Pair is downloadUrl from /document/upload)
            // These downloadUrls will be sent to /upload_document API for verification
            val downloadUrls = uploadArray.map { it.first }
            viewModel.verifyByDoc(downloadUrls)
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

    private fun uploadImage(file: File, fileType: FileType) {
        val docType = "section_194C"
        documentUtils.uploadDocument(file, fileType, docType, this)
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
                        uploadImage(mPhotoFile!!, FileType.IMAGE)
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
                        val fileType: FileType
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
                        if(imageScopedFile.extension=="jpg" ||imageScopedFile.extension=="png" || imageScopedFile.extension=="jpeg"){
                            fileType = FileType.IMAGE
                            mPhotoFile = fileCompressor.compressToFile(File(imageScopedFile.path), localImageName)
                        }else if (imageScopedFile.extension=="pdf"){
                            fileType = FileType.PDF
                            mPhotoFile = imageScopedFile
                        }else{
                            analyticsUtil.moEngageTrackEvent(
                                EVENT_DOC_UPLOADED_WITH_WRONG_EXTENSION,
                                mutableListOf(
                                    PROPERTY_USER_ID, PROPERTY_PHONE_NO,
                                    PROPERTY_TYPE_OF_DOC, PROPERTY_SOURCE_PAGE
                                ),
                                mutableListOf(userPrefs.userId() , userPrefs.phoneNumber.toString(),imageScopedFile.extension,"bank_validation")
                            )

                            uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                            return
                        }

                        if (mPhotoFile == null) {
                            uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                            return
                        }
                        uiUtils.showProgress()
                        uploadImage(mPhotoFile!!, fileType)
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