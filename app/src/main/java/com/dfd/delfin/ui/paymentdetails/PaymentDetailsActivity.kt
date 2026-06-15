package com.dfd.delfin.ui.paymentdetails

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.dfd.delfin.R
import com.dfd.delfin.api.response.FileData
import com.dfd.delfin.databinding.ActivityPaymentDetailsBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.kyc.aadhaar.UploadedItemRVAdapterInterface
import com.dfd.delfin.ui.kyc.gst.DocUploadAdapter
import com.dfd.delfin.utils.BitmapUtils
import com.dfd.delfin.utils.DialogUtilsInterface
import com.dfd.delfin.utils.DocumentUtils
import com.dfd.delfin.utils.EVENT_DOC_UPLOADED_WITH_WRONG_EXTENSION
import com.dfd.delfin.utils.EVENT_SUBMIT_PAYMENT_DETAILS
import com.dfd.delfin.utils.FileCompressor
import com.dfd.delfin.utils.ImageUtils
import com.dfd.delfin.utils.KycUtils
import com.dfd.delfin.utils.PROPERTY_PHONE_NO
import com.dfd.delfin.utils.PROPERTY_SOURCE_PAGE
import com.dfd.delfin.utils.PROPERTY_TTL
import com.dfd.delfin.utils.PROPERTY_TYPE_OF_DOC
import com.dfd.delfin.utils.PROPERTY_USER_ID
import com.dfd.delfin.utils.REQCODE_CAMERA
import com.dfd.delfin.utils.REQCODE_FILE_ATTACHMENTS
import com.dfd.delfin.utils.REQCODE_TAKE_PHOTO
import com.dfd.delfin.utils.WindowInsetsUtils
import com.dfd.delfin.utils.constants.FileType
import com.dfd.delfin.utils.extensions.focusClick
import com.dfd.delfin.utils.extensions.getFileName
import com.dfd.delfin.utils.extensions.isNotNullOrEmpty
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class PaymentDetailsActivity : BaseActivity<ActivityPaymentDetailsBinding, PaymentDetailsViewModel>(),DialogUtilsInterface, DocumentUtils.DocumentProgressInterface, DocumentUtils.DocumentListInterface,
    UploadedItemRVAdapterInterface {

    companion object {
        const val EXTRA_SCREEN_MODE = "extra_screen_mode"
        const val MODE_PAYMENT_DETAILS = "payment_details"
        const val MODE_194C_DECLARATION = "194c_declaration"

        fun intentForPayment(context: Context): Intent {
            return Intent(context, PaymentDetailsActivity::class.java).apply {
                putExtra(EXTRA_SCREEN_MODE, MODE_PAYMENT_DETAILS)
            }
        }

        fun intentFor194C(context: Context): Intent {
            return Intent(context, PaymentDetailsActivity::class.java).apply {
                putExtra(EXTRA_SCREEN_MODE, MODE_194C_DECLARATION)
            }
        }
    }

    private var screenMode: String = MODE_PAYMENT_DETAILS

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

    val docUploadAdapter : DocUploadAdapter by lazy { DocUploadAdapter(this) }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()
    var uploadArray1:ArrayList<Pair<String, String>> = ArrayList()
    var accountNum=false
    var accountName=true
    var ifsc = false
    var docUpload =true
    var nameDec =true
    var startTime: Long = 0
    var endTime: Long = 0
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("PaymentDetailsActivity_SetupTime")
        activitySetupTrace?.start()
        
        // Get screen mode from intent
        screenMode = intent.getStringExtra(EXTRA_SCREEN_MODE) ?: MODE_PAYMENT_DETAILS
        
        viewModel.accountHolderText.value= getString(R.string.hint_for_bank_validation)
        if(userPrefs.paymentRejectReason.isNotNullOrEmpty()){
            binding.paymentError.visibility=View.VISIBLE
            if(userPrefs.paymentRejectReason.replace(" ", "").equals("Documentunderverification")){
                viewModel.errorText = userPrefs.paymentRejectReason
            }else {
                viewModel.errorText =
                    "Payment verification failed due to " + userPrefs.paymentRejectReason
            }
        }else{
            binding.paymentError.visibility=View.GONE
        }
        if(userPrefs.paymentAccountName.isNotNullOrEmpty()){
            viewModel.accountHolderText.value=userPrefs.paymentAccountName
        }
        if(userPrefs.paymentAccountNumber.isNotNullOrEmpty() && !userPrefs.paymentAccountNumber.equals("Not available",true)){
            viewModel.accountText.value=userPrefs.paymentAccountNumber
        }else{
            if (screenMode == MODE_PAYMENT_DETAILS) {
                binding.accountNumEdittext.focusClick()
            }
        }
        if(userPrefs.ifscCode.isNotNullOrEmpty()){
            viewModel.ifscText.value=userPrefs.ifscCode
        }
        
        // Apply mode-based UI restrictions
        applyScreenModeRestrictions()
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                setResult(RESULT_CANCELED)
                finish()
            }

        })

        showUploadedDoc()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        /* Handle window insets for edge-to-edge display (API 35+) - Apply BEFORE setSupportActionBar */
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
        }
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        startTime = System.currentTimeMillis()
        accountName=true
        viewModel.accountHolderText.observe(this, Observer {
            if(viewModel.bankValidationApiFailed){
                if (userPrefs.panName.equals(it,true) || it.equals(
                        getString(R.string.hint_for_bank_validation)
                    )
                ) {
                    binding.accountHolderWarning.visibility = View.GONE
                    binding.nameDeclaration.visibility = View.GONE
                    nameDec = true
                    docUpload = true
                    enableSubmitButton()
                } else {
                    if (it.isNotNullOrEmpty()) {
                        nameDec = false
                        enableSubmitButton()
                        binding.accountHolderWarning.visibility = View.VISIBLE
                        binding.nameDeclaration.visibility = View.VISIBLE
                        binding.nameDeclaration.isChecked = false
                        viewModel.nameDeclaration = false
                    }
                }

            }else {
                if (viewModel.panMatched || it.equals(
                        getString(R.string.hint_for_bank_validation)
                    )
                ) {
                    binding.accountHolderWarning.visibility = View.GONE
                    binding.nameDeclaration.visibility = View.GONE
                    nameDec = true
                    docUpload = true
                    enableSubmitButton()
                } else {
                    if (it.isNotNullOrEmpty()) {
                        nameDec = false
                        enableSubmitButton()
                        binding.accountHolderWarning.visibility = View.VISIBLE
                        binding.nameDeclaration.visibility = View.VISIBLE
                        binding.nameDeclaration.isChecked = false
                        viewModel.nameDeclaration = false
                    }
                }
            }
        })

        viewModel.verificationDocUploadFailed.observe(this, Observer {
            showUploadedDoc()
            userPrefs.ninteen4CDocUrl=""
            userPrefs.paymentDocUrl=""
        })

        // Removed delegation token logic - direct upload now

        viewModel.verificationDocUploadMsg.observe(this, Observer {
                if(viewModel.selected194CUpload.value==true){
                    sendDocForVerification(uploadArray1)
                    viewModel.selected194CUpload.value=false
                }else{
                    if(binding.nameDeclaration.isChecked==true){
                        viewModel.nameDeclaration=true
                    }else{
                        viewModel.nameDeclaration=false
                    }
                   viewModel.updateUserDetails()
                }
        })

        viewModel.userUpdateLiveData.observe(this, Observer {
            if(it){
                userPrefs.paymentAccountNumber = viewModel.accountText.value.toString()
                userPrefs.accNumber = KycUtils.getEncryptedAccountNo(userPrefs.paymentAccountNumber)
                userPrefs.ifscCode = viewModel.ifscText.value.toString()
                userPrefs.paymentAccountName = viewModel.accountHolderText.value.toString()

                endTime = System.currentTimeMillis()
                val ttl = endTime - startTime
                analyticsUtil.moEngageTrackEvent(
                    EVENT_SUBMIT_PAYMENT_DETAILS,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TTL),
                    mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy", ttl.toString())
                )

                if(userPrefs.retryVerification) {
                    userPrefs.paymentRejectReason = "Document under verification"
                    userPrefs.isBankDetailsRejected = false
                    checkStatus()
                }
                finish()
                setResult(RESULT_OK)
            }else{
                viewModel.selected194CUpload.value = userPrefs.ninteen4CDocUrl.isNotNullOrEmpty()
            }
        })

// 194c upload condition
        if (userPrefs.ownedTruck.isNotNullOrEmpty()) {
            if (userPrefs.ownedTruck.toInt() <= 10) {
                binding.uploadDoc1.visibility = View.VISIBLE
            } else {
                binding.uploadDoc1.visibility = View.GONE
            }
        }



        binding.btnSubmit.setOnClickListener {
            if(viewModel.accountText.value!!.length <9){
                binding.imgWrongAccount.visibility=View.VISIBLE
                binding.errorAccountNum.visibility=View.VISIBLE
                binding.errorAccountNum.text="Account number cannot be less than 9 digits"
                ViewCompat.setElevation(binding.imgWrongAccount, this.resources.getDimension( R.dimen.edit_text_raise_focus_z))
                accountNum=false
                enableSubmitButton()
            }else {
                if(viewModel.bankValidationApiFailed){
                    sendDocForVerification(uploadArray)
                }else {
                    if (viewModel.bankValidated) {
                        if (viewModel.selected194CUpload.value == true) {
                            sendDocForVerification(uploadArray1)
                            viewModel.selected194CUpload.value = false
                        } else {
                            if(binding.nameDeclaration.isChecked==true){
                                viewModel.nameDeclaration=true
                            }else{
                                viewModel.nameDeclaration=false
                            }
                                viewModel.updateUserDetails()
                        }
                    } else {
                        viewModel.getBankName(
                            accountNum = viewModel.accountText.value!!,
                            ifsc = viewModel.ifscText.value?.uppercase()!!
                        )
                    }
                }

                if (binding.nameDeclaration.isChecked) {
                    viewModel.nameDeclaration = true
                } else {
                    viewModel.nameDeclaration = false
                }
            }
        }
        binding.uploadDocLay.setOnClickListener {
            viewModel.accountUpload.value = true
            val imageName = "accountProof_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)

        }
        binding.uploadDocLay1.setOnClickListener {
            viewModel.selected194CUpload.value=true
            viewModel.accountUpload.value = false
            val imageName = "194C_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)
        }
        binding.docRemove.setOnClickListener {
            showUploadImage()
            viewModel.accountUpload.value = false
        }
        binding.docRemove1.setOnClickListener {
            showUploadImage1()
            viewModel.selected194CUpload.value=false
        }
        viewModel.bankValidaton.observe(this, Observer {
            if (it.first){
                viewModel.bankValidationApiFailed=false
                viewModel.panMatched=it.second.validated!!
                if(it.second.validated!!){
                    viewModel.bankValidated=false
                    viewModel.accountHolderText.value=it.second.accountHolderName
                    binding.accountHolderEdittext.setText("Account Holder Name - "+it.second.accountHolderName)
                    if(viewModel.selected194CUpload.value==true){
                        sendDocForVerification(uploadArray1)
                        viewModel.selected194CUpload.value=false
                    }else{
                        if(it.second.validated!!||viewModel.nameDeclaration==true) {
                            viewModel.updateUserDetails()
                        }else{
                            viewModel.bankValidated=true
                        }
                    }
                }else{
                    viewModel.accountHolderText.value=it.second.accountHolderName
                    binding.accountHolderEdittext.setText("Account Holder Name - "+it.second.accountHolderName)
                    if(it.second.validated!!){
                        binding.accountHolderWarning.visibility= View.GONE
                        binding.nameDeclaration.visibility=View.GONE
                        nameDec=true
                        docUpload = true
                        enableSubmitButton()
                    }else{
                        if(it.second.accountHolderName.isNotNullOrEmpty()) {
                            nameDec=false
                            enableSubmitButton()
                            binding.accountHolderWarning.visibility = View.VISIBLE
                            binding.nameDeclaration.visibility = View.VISIBLE
                        }
                    }
                    viewModel.bankValidated=true
                }
            }else{
                viewModel.bankValidationApiFailed=true
                binding.uploadDoc.visibility=View.VISIBLE
                binding.accountHolderEdittext.visibility=View.INVISIBLE
                binding.accountHolderEdittext1.visibility=View.VISIBLE
                viewModel.accountHolderText.value=""
                binding.accountHolderEdittext1.setText("")
            }
        })

        viewModel.accountDoesNotExist.observe(this, Observer {
          if(it.first){
              dialogUtils.showErrorDialog(it.second)
          }
        })
        viewModel.accountText.observe(this, Observer {
            if(it.isNotNullOrEmpty()){
                binding.imgWrongAccount.visibility=View.GONE
                binding.errorAccountNum.visibility=View.GONE
                accountNum=true
                if(viewModel.bankValidationApiFailed) {
                    viewModel.bankValidationApiFailed = false
                    binding.uploadDoc.visibility = View.GONE
                    binding.accountHolderEdittext.visibility = View.VISIBLE
                    binding.accountHolderEdittext1.visibility = View.INVISIBLE
                    viewModel.accountHolderText.value = ""
                    binding.accountHolderEdittext1.setText("")
                    accountName=true
                }
                enableSubmitButton()
            }else{
                binding.accountHolderEdittext.text =getString(R.string.hint_for_bank_validation)
                accountNum=false
                enableSubmitButton()
            }
            binding.accountHolderWarning.visibility= View.GONE
            binding.nameDeclaration.visibility=View.GONE
            viewModel.accountHolderText.value=getString(R.string.hint_for_bank_validation)
            nameDec=true
            docUpload = true
            viewModel.bankValidated=false
            enableSubmitButton()

        })
        binding.accountNumEdittext.lengthAction(1){
            accountNum=true
            enableSubmitButton()
        }
        binding.accountNumEdittext.lengthAction(0){
            accountNum=false
            enableSubmitButton()
        }

        binding.accountHolderEdittext1.lengthAction(1){
            accountName=true
            enableSubmitButton()
        }
        binding.accountHolderEdittext1.lengthAction(0){
            accountName=false
            enableSubmitButton()
        }

        viewModel.ifscText.observe(this, Observer {
            if(it.isNotNullOrEmpty()){
                if(viewModel.bankValidationApiFailed) {
                    viewModel.bankValidationApiFailed = false
                    binding.uploadDoc.visibility = View.GONE
                    binding.accountHolderEdittext.visibility = View.VISIBLE
                    binding.accountHolderEdittext1.visibility = View.INVISIBLE
                    viewModel.accountHolderText.value = ""
                    binding.accountHolderEdittext1.setText("")
                    accountName=true
                }
                ifsc=true
                enableSubmitButton()
            }else{
                binding.accountHolderEdittext.text = getString(R.string.hint_for_bank_validation)
                ifsc=false
                enableSubmitButton()
            }
            binding.accountHolderWarning.visibility= View.GONE
            binding.nameDeclaration.visibility=View.GONE
            viewModel.accountHolderText.value=getString(R.string.hint_for_bank_validation)
            nameDec=true
            viewModel.bankValidated=false
            enableSubmitButton()
        })

        binding.ifscEdittext.lengthAction(1){
            ifsc=true
            enableSubmitButton()
        }
        binding.ifscEdittext.lengthAction(0){
            ifsc=false
            enableSubmitButton()
        }


        binding.nameDeclaration.setOnClickListener {
            if(binding.nameDeclaration.isChecked){
                nameDec=true
                Log.d("enable",accountName.toString()+accountNum.toString()+ifsc.toString()+docUpload.toString())
                enableSubmitButton()
            }else{
                nameDec=false
                enableSubmitButton()
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


    fun enableSubmitButton(){
        Log.d("Enabke",accountName.toString()+accountNum.toString()+ifsc.toString()+docUpload.toString()+nameDec.toString())
        if(accountName&&accountNum&&ifsc&&nameDec&&docUpload) {
            binding.btnSubmit.isEnabled = true
        }else{
            binding.btnSubmit.isEnabled = false
        }
    }
    
    /**
     * Apply UI restrictions based on screen mode.
     * - MODE_PAYMENT_DETAILS: User can edit payment fields, 194C section is hidden
     * - MODE_194C_DECLARATION: User can only upload 194C, payment fields are read-only
     */
    private fun applyScreenModeRestrictions() {
        when (screenMode) {
            MODE_PAYMENT_DETAILS -> {
                // Hide 194C upload section when opened for payment details
                // binding.uploadDoc1.visibility = View.GONE
                // Payment fields remain editable (default behavior)
            }
            MODE_194C_DECLARATION -> {
                // Disable payment fields editing - make them read-only
                binding.accountNumEdittext.isEnabled = false
                binding.accountNumEdittext.alpha = 0.6f
                binding.ifscEdittext.isEnabled = false
                binding.ifscEdittext.alpha = 0.6f
                binding.accountHolderEdittext1.isEnabled = false
                binding.accountHolderEdittext1.alpha = 0.6f
                
                // Disable payment document upload
                binding.uploadDocLay.isEnabled = false
                binding.uploadDocLay.alpha = 0.6f
                binding.docRemove.isEnabled = false
                
                // Show 194C upload section
                binding.uploadDoc1.visibility = View.VISIBLE
                
                // Set validation flags for payment fields as already valid (since they're read-only)
                accountNum = userPrefs.paymentAccountNumber.isNotNullOrEmpty() && 
                             !userPrefs.paymentAccountNumber.equals("Not available", true)
                ifsc = userPrefs.ifscCode.isNotNullOrEmpty()
                accountName = true
                docUpload = true
                nameDec = true
                
                enableSubmitButton()
            }
        }
    }
    
    fun checkStatus(){
        if (userPrefs.panRejectReason.isNotNullOrEmpty()&& (userPrefs.panRejectReason.replace(" ", "").equals("Documentunderverification"))) {
        } else if (userPrefs.identityRejectReason.isNotNullOrEmpty() && !(userPrefs.identityRejectReason.replace(" ", "").equals("Documentunderverification"))) {
        } else if (userPrefs.addressRejectReason.isNotNullOrEmpty()&& !(userPrefs.addressRejectReason.replace(" ", "").equals("Documentunderverification"))) {
        } else if (userPrefs.rcRejectReason.isNotNullOrEmpty() && !(userPrefs.rcRejectReason.replace(" ", "").equals("Documentunderverification"))) {
        } else {
            userPrefs.verificationStatus="pending"
        }


    }

    override fun layoutId() = R.layout.activity_payment_details

    override fun requireConnection() =true

    override fun getViewModelClass()= PaymentDetailsViewModel::class.java
    override fun onDocumentSuccess(downloadUrl: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Upload successful")
        // Store downloadUrl (from BE response) instead of filename
        // This downloadUrl will be sent to /upload_document API for verification
        if(viewModel.accountUpload.value == true){
            uploadArray.add(Pair(downloadUrl, (mPhotoFile?.length()?.div(1024)).toString()))
            showFileSelected()

        }else if(viewModel.selected194CUpload.value == true){
            uploadArray1.add(Pair(downloadUrl, (mPhotoFile?.length()?.div(1024)).toString()))
            showFileSelected1()
        }
        resetUploadData()
    }

    override fun onDocumentFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Failed to upload: $error")
        resetUploadData()
    }

    // Download functionality
    override fun onDocumentListSuccess(files: List<FileData>) {
        uiUtils.hideProgress()
        if (files.isNotEmpty()) {
            // Handle successful document list - show files to user
            showDocumentList(files)
        } else {
            uiUtils.showToast("No payment documents found")
        }
    }

    override fun onDocumentListFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Failed to load payment documents: $error")
    }

    private fun showDocumentList(files: List<FileData>) {
        // Show list of available payment documents for download
        val fileNames = files.map { it.filename }
        uiUtils.showToast("Found ${files.size} payment document(s): ${fileNames.joinToString(", ")}")
    }

    //This functionality is not needed. Can be used in future.
    private fun downloadPaymentDocuments() {
        uiUtils.showProgress()
        val docType = "cancelled_cheque" // Document type for payment verification
        documentUtils.listDocuments(docType, this)
    }

    //This functionality is not needed. Can be used in future.
    private fun download194CDocuments() {
        uiUtils.showProgress()
        val docType = "section_194C" // Document type for 194C documents
        documentUtils.listDocuments(docType, this)
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
    private fun showUploadedDoc(){
        if(userPrefs.paymentDocUrl.isNullOrEmpty()){
            showUploadImage()
        }else{
            // Use the stored downloadUrl from userPrefs (full URL from backend)
            uploadArray.add(Pair(userPrefs.paymentDocUrl, "N/A"))
            showFileSelected()
        }
        if(userPrefs.ninteen4CDocUrl.isNullOrEmpty()){
            showUploadImage1()
        }else{
            // Use the stored downloadUrl from userPrefs (full URL from backend)
            uploadArray1.add(Pair(userPrefs.ninteen4CDocUrl, "N/A"))
            showFileSelected1()
        }
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

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            mPhotoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(
                this, com.dfd.delfin.BuildConfig.APPLICATION_ID + ".provider", mPhotoFile!!
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
        if(viewModel.accountUpload.value == true) {
            this.uploadImageName =
                "account_proof_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"
            this.localImageName =
                "account_proof_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"
        }else if(viewModel.selected194CUpload.value == true) {
            this.uploadImageName =
                "194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"
            this.localImageName =
                "194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"
        }

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

    private fun showFileSelected() {
        binding.uploadDocLay.visibility=View.GONE
        binding.docUploadedLay.visibility=View.VISIBLE
        binding.docTitle.setText(uploadArray.get(0).first)
        binding.docSize.setText(uploadArray.get(0).second+" KB")
        docUpload=true
        enableSubmitButton()
    }
    private fun showFileSelected1() {
        binding.uploadDocLay1.visibility=View.GONE
        binding.docUploadedLay1.visibility=View.VISIBLE
        binding.docTitle1.setText(uploadArray1.get(0).first)
        binding.docSize1.setText(uploadArray1.get(0).second+" KB")

    }

    private fun showUploadImage() {
        binding.uploadDocLay.visibility=View.VISIBLE
        binding.docUploadedLay.visibility=View.GONE
        if(uploadArray.size>0){
            uploadArray.clear()
        }
        docUpload=false
        enableSubmitButton()
    }
    private fun showUploadImage1() {
        binding.uploadDocLay1.visibility=View.VISIBLE
        binding.docUploadedLay1.visibility=View.GONE
        if(uploadArray1.size>0){
            uploadArray1.clear()
        }
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

    private fun uploadImage(file: File) {
        uiUtils.showProgress()
        val docType = "payment_details" // VAPT doc specifies "payment_details" for payment/bank docs
        // VAPT requirement: Payment/Bank needs phoneNumber and proofType in dynamic_variables
        val proofType = if(viewModel.accountUpload.value == true) "account_proof" else "194C"
        val dynamicVariables = mapOf(
            "phoneNumber" to (userPrefs.phoneNumber?.toString() ?: ""),
            "proofType" to proofType
        )
        documentUtils.uploadDocument(file, FileType.IMAGE, docType, this, dynamicVariables)
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
                        if(viewModel.accountUpload.value == true) {
                            this.uploadImageName =
                                "account_proof_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber +"."+ mPhotoFile?.extension
                            this.localImageName =
                                "account_proof_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber +"."+mPhotoFile?.extension
                        }else if(viewModel.selected194CUpload.value == true) {
                            this.uploadImageName =
                                "194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber +"."+ mPhotoFile?.extension
                            this.localImageName =
                                "194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber +"."+ mPhotoFile?.extension
                        }
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
                        inputStream.copyTo(outputStream, bufferSize = 16384)
                        if(viewModel.accountUpload.value == true) {
                            this.uploadImageName =
                                "account_proof_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber +"."+ imageScopedFile.extension
                            this.localImageName =
                                "account_proof_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber +"."+imageScopedFile.extension
                        }else if(viewModel.selected194CUpload.value == true) {
                            this.uploadImageName =
                                "194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber +"."+ imageScopedFile.extension
                            this.localImageName =
                                "194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber +"."+ imageScopedFile.extension
                        }
                        if(imageScopedFile.extension=="jpg" ||imageScopedFile.extension=="png" || imageScopedFile.extension=="jpeg"){
                            mPhotoFile = fileCompressor.compressToFile(File(imageScopedFile.path), localImageName)
                        }else if (imageScopedFile.extension=="pdf"){
                            mPhotoFile = imageScopedFile
                        }else{
                            analyticsUtil.moEngageTrackEvent(
                                EVENT_DOC_UPLOADED_WITH_WRONG_EXTENSION,
                                mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO,
                                    PROPERTY_TYPE_OF_DOC, PROPERTY_SOURCE_PAGE),
                                mutableListOf(userPrefs.userId() , userPrefs.phoneNumber.toString(),imageScopedFile.extension,"payment")
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
        }
    }

    override fun handleDeleteAction(item: Pair<String, String>) {
        uploadArray.remove(item)
    }


}