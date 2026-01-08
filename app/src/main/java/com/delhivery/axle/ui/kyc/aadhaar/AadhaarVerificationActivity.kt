package com.delhivery.axle.ui.kyc.aadhaar

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.api.response.FileData
import com.delhivery.axle.databinding.ActivityVerifyAadharBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.ShowVerificationOtpDialog
import com.delhivery.axle.ui.kyc.address.CommunicationAddressActivity
import com.delhivery.axle.ui.kyc.gst.DocUploadAdapter
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.constants.FileType
import com.delhivery.axle.utils.DocumentUtils
import com.delhivery.axle.utils.extensions.getFileName
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import com.delhivery.axle.utils.extensions.focusClick
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.lang.StringBuilder


class AadhaarVerificationActivity  : BaseActivity<ActivityVerifyAadharBinding, AadhaarVerificationViewModel>(),
    DialogUtilsInterface, DocumentUtils.DocumentProgressInterface, DocumentUtils.DocumentListInterface,UploadedItemRVAdapterInterface {
    init {
        StatusBarColor = Color.parseColor("#ededff")
    }
    @Inject lateinit var userPrefs: UserPrefs

    override fun getViewModelClass() = AadhaarVerificationViewModel::class.java

    override fun layoutId() = R.layout.activity_verify_aadhar

    override fun requireConnection() = false

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


    val docUploadAdapter : DocUploadAdapter by lazy { DocUploadAdapter(this) }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()

    var startTime: Long = 0
    var endTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("AadhaarVerificationActivity_SetupTime")
        activitySetupTrace?.start()
        if(intent?.extras!=null){
            viewModel.currentStep = navigationUtils.getNavigationStepFormat(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!, intent?.extras?.getInt(
                TotalStepsKey)!!)
            if(userPrefs.identityRejectReason.isNotNullOrEmpty()) {
                binding.aadhaarError.visibility=View.VISIBLE
                if(userPrefs.identityRejectReason.replace(" ", "").equals("Documentunderverification")){
                    viewModel.errorText = userPrefs.identityRejectReason
                }else {
                    viewModel.errorText =
                        ("Aadhaar verification failed due to " + userPrefs.identityRejectReason)
                }
            }else{
                binding.aadhaarError.visibility=View.GONE
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.progressStepLayout.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.progressStepLayout.toolbar)
    }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                userPrefs.retryVerificationOnBack=true
                val bundle = Bundle()
                bundle.putInt(StepKey,0)
                navigationUtils.navigateKyc(this@AadhaarVerificationActivity,false,bundle)
                finish()
            }
        })

        navigationUtils.showProgressSteps(binding.progressStepLayout, 2)
        startTime = System.currentTimeMillis()

        binding.editAadhaar.apply {
            lengthAction(14){
                enableSubmit()
                uiUtils.toggleKeyboard(true)
            }
            lengthAction(13){
               enableSubmit()
            }
        }
        binding.aadhaarTermCondition.setOnClickListener {
            if(binding.aadhaarTermCondition.isChecked){
                enableSubmit()
            }else{
                enableSubmit()
            }
        }
        if(userPrefs.aadhaarNumber.isNotNullOrEmpty()){
            var aadhaarfill = StringBuilder()
            for((i,item) in userPrefs.aadhaarNumber.withIndex()){
                aadhaarfill.append(item)
                if(i==3){
                    aadhaarfill.append("-")
                }
                if(i==7){
                    aadhaarfill.append("-")
                }
            }
            viewModel.aadhaarCardNumber = aadhaarfill.toString()
            binding.aadhaarTermCondition.isChecked =true
            viewModel.aadhaarPolicyAccepted = true
        }else{
            binding.editAadhaar.focusClick()
        }

        binding.btnVerifyAadhaar.setOnClickListener {
            dialogUtils.showVerifcationOptionsDialog(getString(R.string.upload_aadhaar_text),this)
        }


        viewModel.otpRecieved.observe(
            this, Observer {
              if(it){
                  ShowVerificationOtpDialog(this,this,uiUtils,userPrefs.phoneNumber!!,dialogUtils,getString(R.string.upload_aadhaar_text),viewModel,this@AadhaarVerificationActivity).show()
              }
            }
        )
        viewModel.docVerified.observe(
         this, Observer {
                if(it){
                    uiUtils.hideProgress()
                    viewModel.updateUserDetails()

                }else{
                    uiUtils.hideProgress()
                    viewModel.docVerificationFailedCount.postValue(viewModel.docVerificationFailedCount.value!!+1)
                    resetUploadData()
                    uploadArray =  ArrayList()
                    if(viewModel.docVerificationFailedCount.value!=null&&viewModel.docVerificationFailedCount.value!! <1){
                        dialogUtils.showUploadFailDialog(getString(R.string.upload_aadhaar_text),this)
                    }
                }
            }
        )
        viewModel.docVerificationFailedCount.observe(this, Observer {
            if(viewModel.docVerificationFailedCount.value==2){
                viewModel.docVerificationFailedCount.value=0
                viewModel.updateUserDetails()
            }
        })
        // Removed delegation token observer - using direct DocumentUtils upload now

        viewModel.userUpdateLiveData.observe(this, Observer {
            if (it) {
                if(userPrefs.retryVerification){
                    userPrefs.identityRejectReason= "Document under verification"
                }
                endTime = System.currentTimeMillis()
                val ttl = endTime - startTime
                analyticsUtil.moEngageTrackEvent(
                    EVENT_SUBMIT_AADHAR,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TTL),
                    mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy", ttl.toString())
                )
                navigationUtils.checkNavigationKycStep(this,intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
                    TotalStepsKey)!!,null)
            } else {
                uiUtils.showSnackbar("Update Failed, Please try again")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }
    private fun enableSubmit() {
        if(binding.editAadhaar.length()==14 && binding.aadhaarTermCondition.isChecked){
            viewModel.aadhaarPolicyAccepted = true
            binding.btnVerifyAadhaar.isEnabled =true
        }else{
            viewModel.aadhaarPolicyAccepted = false
            binding.btnVerifyAadhaar.isEnabled =false
        }

    }

    override fun getRequestAadhaarOtp() {
       viewModel.getRequestAadhaarOtp(true)
    }

    override fun setAccountRoleSelection(selected: String) {
    }

    override fun navigateToBusinessVerification() {
    }

    override fun onDocumentSuccess(downloadUrl: String) {
        uiUtils.hideProgress()
        // Store full downloadUrl (not just filename)
        // This downloadUrl will be sent to /upload_document API for verification
        uploadArray.add(
            Pair(
                downloadUrl, (mPhotoFile?.length()
                ?.div(1024)).toString()
            )
        )
        dialogUtils.showAttachmentDialog(docUploadAdapter,uploadArray,this,getString(R.string.upload_aadhaar_text))
        resetUploadData()
    }

    override fun onDocumentFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Failed to upload: $error")
        dialogUtils.showAttachmentDialog(docUploadAdapter,uploadArray,this,getString(R.string.upload_aadhaar_text))
        resetUploadData()
    }

    override fun onDocumentListSuccess(documents: List<FileData>) {
        // Handle document list if needed
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

    private fun requestImageCapturePermissions(isCamera: Boolean) {
        this.isCamera = isCamera
        compositeDisposable += requestPermission(
            arrayOf(
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
                    this, BuildConfig.APPLICATION_ID + ".provider", mPhotoFile!!
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQCODE_TAKE_PHOTO)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
    }

    override  fun captureImage(
        uploadImageName: String,
        localImageName: String
    ) {
        this.uploadImageName =  "Aadhaar_" + System.currentTimeMillis()+".jpg"
        this.localImageName =  "Aadhaar_" + System.currentTimeMillis()+".jpg"

        val items = arrayOf<CharSequence>("Take Photo", "Choose a file", "Cancel")
        val builder = android.app.AlertDialog.Builder(this,R.style.DatePickerTheme)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Take Photo" -> requestImageCapturePermissions(true)
                items[item] == "Choose a file" -> requestImageCapturePermissions(false)
                items[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    override fun sendDocForVerification(uploadArray:ArrayList<Pair<String, String>>) {
        if(uploadArray.isNotEmpty()){
                val imageUrls= mutableListOf<String>()
               for(i in uploadArray){
               // The URL is already complete from the document upload
               imageUrls.add(i.first)
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

    private fun uploadImage(file: File, fileType: FileType) {
        val docType = "aadhaar"
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
            REQCODE_FILE_ATTACHMENTS->{
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
                            this.uploadImageName = "Aadhaar_" + System.currentTimeMillis()+"."+imageScopedFile.extension
                            this.localImageName =  "Aadhaar_" + System.currentTimeMillis()+"."+imageScopedFile.extension
                        if(imageScopedFile.extension=="jpg" ||imageScopedFile.extension=="png" || imageScopedFile.extension=="jpeg"){
                            fileType = FileType.IMAGE
                            mPhotoFile = fileCompressor.compressToFile(File(imageScopedFile.path), localImageName)
                        }else if (imageScopedFile.extension=="pdf"){
                            fileType = FileType.PDF
                            mPhotoFile = imageScopedFile
                        }else{
                            analyticsUtil.moEngageTrackEvent(
                                EVENT_DOC_UPLOADED_WITH_WRONG_EXTENSION,
                                mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TYPE_OF_DOC, PROPERTY_SOURCE_PAGE),
                                mutableListOf(userPrefs.userId(), userPrefs.phoneNumber.toString(), imageScopedFile.extension, "aadhaar")
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


}

fun addressVerificationIntent(
    context: Context
) = Intent(context, CommunicationAddressActivity::class.java).apply {
}

