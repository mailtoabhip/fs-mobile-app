package com.delhivery.axle.ui.kyc.identityverification

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
import com.delhivery.axle.databinding.ActivityIdentityVerificationBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.kyc.address.CommunicationAddressActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.constants.FileType
import com.delhivery.axle.utils.extensions.focusClick
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

class IdentityVerificationActivity: BaseActivity<ActivityIdentityVerificationBinding, IdentityVerificationViewModel>(),
    DocumentUtils.DocumentProgressInterface, DocumentUtils.DocumentListInterface{

    private var isCamera: Boolean = false
    private var mPhotoFile: File? = null
    private lateinit var uploadImageName: String
    private lateinit var localImageName: String
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

    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()
    var startTime: Long = 0
    var endTime: Long = 0
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("IdentityVerificationActivity_SetupTime")
        activitySetupTrace?.start()
        binding.editCinLayout.visibility= View.VISIBLE
        binding.layoutCin.isSelected=true
        binding.layoutUdyog.isSelected=false
        binding.layoutShop.isSelected=false
        binding.textCin.isChecked=true
        binding.textUdyog.isChecked=false
        binding.textShop.isChecked=false
        if(intent?.extras!=null) {
            viewModel.currentStep = navigationUtils.getNavigationStepFormat(
                intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!, intent?.extras?.getInt(
                    TotalStepsKey
                )!!
            )
            if (userPrefs.identityRejectReason.isNotNullOrEmpty()) {
                binding.identityError.visibility = View.VISIBLE
                if(userPrefs.identityRejectReason.replace(" ", "").equals("Documentunderverification")){
                    viewModel.errorText = userPrefs.identityRejectReason
                }else {
                    if (userPrefs.identityType.equals("cin")) {
                        if (userPrefs.cinNumber.isNotNullOrEmpty()) {
                            binding.identityError.text =
                                ("CIN verification failed due to " + userPrefs.identityRejectReason)
                            viewModel.errorText =
                                ("CIN verification failed due to " + userPrefs.identityRejectReason)
                        }
                    } else if (userPrefs.identityType.equals("udyog_aadhaar")) {
                        if (userPrefs.udyogNumber.isNotNullOrEmpty()) {
                            binding.identityError.text =
                                ("Udyog Aadhaar verification failed due to " + userPrefs.identityRejectReason)
                            viewModel.errorText =
                                ("Udyog Aadhaar verification failed due to " + userPrefs.identityRejectReason)
                        }
                    } else if (userPrefs.identityType.equals("shop_establishment")) {
                        if (userPrefs.shopNumber.isNotNullOrEmpty()) {
                            binding.identityError.text =
                                ("Shop Establishment verification failed due to " + userPrefs.identityRejectReason)
                            viewModel.errorText =
                                ("Shop Establishment verification failed due to " + userPrefs.identityRejectReason)
                        }
                    }
                }
            }else {
                binding.identityError.visibility = View.GONE
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
                navigationUtils.navigateKyc(this@IdentityVerificationActivity,true,bundle)
                finish()
            }
        })
        navigationUtils.showProgressSteps(binding.progressStepLayout, 2)
        startTime = System.currentTimeMillis()

        binding.textCin.setOnClickListener{
            clickedCin(false)
        }
        binding.textUdyog.setOnClickListener{
            clickedUdyog(false)

        }
        binding.textShop.setOnClickListener{
            clickedShopNumber(false)

        }

        when {
            userPrefs.cinNumber.isNotNullOrEmpty() && userPrefs.identityDocUrl.lowercase().contains("cin")-> {
                clickedCin(true)
                viewModel.cinNumber = userPrefs.cinNumber
            }
            userPrefs.udyogNumber.isNotNullOrEmpty() &&userPrefs.identityDocUrl.lowercase().contains("udyog")-> {
                clickedUdyog(true)
                viewModel.udyogNumber = userPrefs.udyogNumber
            }
            userPrefs.shopNumber.isNotNullOrEmpty() &&userPrefs.identityDocUrl.lowercase().contains("shop") -> {
                clickedShopNumber(true)
                viewModel.shopNumber = userPrefs.shopNumber
            }
        }
        if(userPrefs.cinNumber.isNullOrEmpty() &&userPrefs.udyogNumber.isNullOrEmpty()&&userPrefs.shopNumber.isNullOrEmpty()){
            binding.editCin.focusClick()
            clickedCin(true)
        }

        // Removed delegation token logic - direct upload now

        viewModel.userUpdateLiveData.observe(this, Observer {
            if(it){
                var identityType = ""
                when {
                    binding.editCin.length()>0 -> {
                        identityType = "cin_number"
                        userPrefs.cinNumber = viewModel.cinNumber
                        userPrefs.udyogNumber = ""
                        userPrefs.shopNumber =""
                    }
                    binding.editUdyog.length()>0 -> {
                        identityType = "udyog_aadhaar_number"
                        userPrefs.udyogNumber = viewModel.udyogNumber
                        userPrefs.cinNumber = ""
                        userPrefs.shopNumber =""
                    }
                    binding.editShop.length()>0 -> {
                        identityType = "shop_establishment_number"
                        userPrefs.shopNumber = viewModel.shopNumber
                        userPrefs.cinNumber = ""
                        userPrefs.udyogNumber =""
                    }
                }
                if(userPrefs.retryVerification){
                    userPrefs.identityRejectReason= "Document under verification"
                }

                endTime = System.currentTimeMillis()
                val ttl = endTime - startTime
                analyticsUtil.moEngageTrackEvent(
                    EVENT_SUBMIT_IDENTITY,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TTL, PROPERTY_IDENTITY_SELECTED),
                    mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy", ttl.toString(), identityType)
                )
            navigationUtils.checkNavigationKycStep(this,intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
                TotalStepsKey)!!,null)
            }
        })

        viewModel.verificationDocUploadLiveData.observe(this, Observer {
            if(it){
                viewModel.updateUserDetails()
            }
        })

        binding.docRemove.setOnClickListener {
            showUploadImage()
        }
        binding.uploadDoc.setOnClickListener{
            if((binding.textCin.isChecked && binding.editCin.length()>0)||(binding.textUdyog.isChecked&& binding.editUdyog.length()>0)||(binding.textShop.isChecked&& binding.editShop.length()>0)){
                var docType=""
                when {
                    binding.textCin.isChecked -> {
                        docType = "CIN_"
                    }
                    binding.textUdyog.isChecked -> {
                        docType = "Udyog_"
                    }
                    binding.textShop.isChecked -> {
                        docType = "Shop_"
                    }
                }
                val imageName = docType + System.currentTimeMillis()+".jpg"
                captureImage(imageName, imageName)
            }else{
                uiUtils.showToast("Please provide details first")
            }
        }

        binding.btnVerifyIdentity.setOnClickListener {
           if(binding.textCin.isChecked &&binding.editCin.length()>0){
               viewModel.selected = "cin"
           }else if (binding.textUdyog.isChecked && binding.editUdyog.length()>0){
               viewModel.selected = "udyog_aadhaar"
           }else if (binding.textShop.isChecked&& binding.editShop.length()>0){
               viewModel.selected = "shop_establishment"
           }
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

   private fun clickedCin(uploaded:Boolean){
       binding.editCinLayout.visibility= View.VISIBLE
       binding.editUdyogLayout.visibility= View.GONE
       binding.editShopLayout.visibility= View.GONE
       binding.layoutCin.isSelected=true
       binding.layoutUdyog.isSelected=false
       binding.layoutShop.isSelected=false
       binding.textCin.isChecked=true
       binding.textUdyog.isChecked=false
       binding.textShop.isChecked=false
       binding.editUdyog.text?.clear()
       binding.editShop.text?.clear()
       if(uploaded){
           showUploadedDoc()
       }else showUploadImage()
   }
    private fun clickedUdyog(uploaded: Boolean){
        binding.editCinLayout.visibility= View.GONE
        binding.editUdyogLayout.visibility= View.VISIBLE
        binding.editShopLayout.visibility= View.GONE
        binding.layoutCin.isSelected=false
        binding.layoutUdyog.isSelected=true
        binding.layoutShop.isSelected=false
        binding.textCin.isChecked=false
        binding.textUdyog.isChecked=true
        binding.textShop.isChecked=false
        binding.editCin.text?.clear()
        binding.editShop.text?.clear()
        if(uploaded){
            showUploadedDoc()
        }else showUploadImage()
    }
    private fun clickedShopNumber(uploaded: Boolean){
        binding.editCinLayout.visibility= View.GONE
        binding.editUdyogLayout.visibility= View.GONE
        binding.editShopLayout.visibility= View.VISIBLE
        binding.layoutCin.isSelected=false
        binding.layoutUdyog.isSelected=false
        binding.layoutShop.isSelected=true
        binding.textCin.isChecked=false
        binding.textUdyog.isChecked=false
        binding.textShop.isChecked=true
        binding.editCin.text?.clear()
        binding.editUdyog.text?.clear()
        if(uploaded){
            showUploadedDoc()
        }else showUploadImage()
    }
    private fun requestImageCapturePermissions(isCamera: Boolean) {
        this.isCamera = isCamera
        compositeDisposable += requestPermission(arrayOf(Manifest.permission.CAMERA))
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

    private fun showUploadedDoc(){
        if(userPrefs.identityDocUrl.isNullOrEmpty()){
         showUploadImage()
        }else{
            resetUploadData()
            uploadArray.add(Pair(uploadImageName, (mPhotoFile?.length()?.div(1024)).toString()))
            showFileSelected()
        }
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

    private fun uploadImage(file: File) {
        uiUtils.showProgress()
        val docType = "iv" // VAPT doc specifies "iv" for Identity Verification
        // VAPT requirement: IV needs docType (PAN_, CIN_, Udyog_, Shop_) in dynamic_variables
        val ivDocType = when {
            binding.textCin.isChecked -> "CIN_"
            binding.textUdyog.isChecked -> "Udyog_"
            binding.textShop.isChecked -> "Shop_"
            else -> "CIN_"
        }
        val dynamicVariables = mapOf("docType" to ivDocType)
        documentUtils.uploadDocument(file, FileType.IMAGE, docType, this, dynamicVariables)
    }

    private fun captureImage(
        uploadImageName: String,
        localImageName: String
    ) {
        this.uploadImageName = uploadImageName
        this.localImageName = localImageName

        val items = arrayOf<CharSequence>("Take Photo", "Choose a file", "Cancel")
        val builder = AlertDialog.Builder(this,R.style.DatePickerTheme)
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
        binding.uploadDocLay.visibility=View.GONE
        binding.docUploadedLay.visibility=View.VISIBLE
        binding.docTitle.setText(uploadArray.get(0).first)
        binding.docSize.setText(uploadArray.get(0).second+" KB")
        binding.btnVerifyIdentity.isEnabled =true
    }

     private fun showUploadImage() {
         binding.uploadDocLay.visibility=View.VISIBLE
         binding.docUploadedLay.visibility=View.GONE
         if(uploadArray.size>0){
             uploadArray.clear()
         }
         binding.btnVerifyIdentity.isEnabled =false
     }

    fun sendDocForVerification(uploadArray:ArrayList<Pair<String, String>>) {
        if(uploadArray.isNotEmpty()){
            // Extract downloadUrls from uploadArray (first element of Pair is downloadUrl from /document/upload)
            // These downloadUrls will be sent to /upload_document API for verification
            val downloadUrls = uploadArray.map { it.first }
            viewModel.verifyByDoc(downloadUrls)
        }else{
            uiUtils.showToast("No file selected")
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


    override fun onDocumentSuccess(downloadUrl: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Upload successful")
        // Store downloadUrl (from BE response) instead of filename
        // This downloadUrl will be sent to /upload_document API for verification
        uploadArray.add(Pair(downloadUrl, (mPhotoFile?.length()?.div(1024)).toString()))
        showFileSelected()
        resetUploadData()
    }

    override fun onDocumentFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Upload failed: $error")
        resetUploadData()
    }

    // Download functionality
    override fun onDocumentListSuccess(files: List<FileData>) {
        uiUtils.hideProgress()
        if (files.isNotEmpty()) {
            // Handle successful document list - show files to user
            showDocumentList(files)
        } else {
            uiUtils.showToast("No identity documents found")
        }
    }

    override fun onDocumentListFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Failed to load identity documents: $error")
    }

    private fun showDocumentList(files: List<FileData>) {
        // Show list of available identity documents for download
        val fileNames = files.map { it.filename }
        uiUtils.showToast("Found ${files.size} identity document(s): ${fileNames.joinToString(", ")}")
    }

    //This functionality is not needed. Can be used in future.
    private fun downloadIdentityDocuments() {
        uiUtils.showProgress()
        val docType = when {
            binding.textCin.isChecked -> "cin"
            binding.textUdyog.isChecked -> "udyog_aadhaar"
            binding.textShop.isChecked -> "shop_establishment"
            else -> "cin"
        }
        documentUtils.listDocuments(docType, this)
    }

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
                        uploadImage(mPhotoFile!!)
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
                       var docType=""
                        when {
                            binding.textCin.isChecked -> {
                                docType = "CIN_"
                            }
                            binding.textUdyog.isChecked -> {
                                docType = "Udyog_"
                            }
                            binding.textShop.isChecked -> {
                                docType = "Shop_"
                            }
                        }
                        this.uploadImageName = docType + System.currentTimeMillis()+"."+imageScopedFile.extension
                        this.localImageName =  docType + System.currentTimeMillis()+"."+imageScopedFile.extension
                        if(imageScopedFile.extension=="jpg" ||imageScopedFile.extension=="png" || imageScopedFile.extension=="jpeg"){
                            mPhotoFile = fileCompressor.compressToFile(File(imageScopedFile.path), localImageName)
                        }else if (imageScopedFile.extension=="pdf"){
                            mPhotoFile = imageScopedFile
                        }else{
                            analyticsUtil.moEngageTrackEvent(
                                EVENT_DOC_UPLOADED_WITH_WRONG_EXTENSION,
                                mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO,
                                    PROPERTY_TYPE_OF_DOC, PROPERTY_SOURCE_PAGE),
                                mutableListOf(userPrefs.userId() , userPrefs.phoneNumber.toString(),imageScopedFile.extension,"identity_verification")
                            )
                            uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                            return
                        }

                        if (mPhotoFile == null) {
                            uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                            return
                        }
                        uiUtils.showProgress()
                        uploadImage(mPhotoFile!!)
                    } catch (e: IOException) {
                        uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                    }
                } else {
                    uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                }
            }
        }}

    override fun getViewModelClass()= IdentityVerificationViewModel::class.java

    override fun layoutId()= R.layout.activity_identity_verification

    override fun requireConnection()= false

    private fun identityVerificationIntent(
        context: Context
    ) = Intent(context, CommunicationAddressActivity::class.java).apply {
    }
}

