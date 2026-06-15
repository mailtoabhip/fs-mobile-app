package com.dfd.delfin.ui.kyc.address

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.dfd.delfin.BuildConfig
import com.dfd.delfin.R
import com.dfd.delfin.api.request.AddAddressModel
import com.dfd.delfin.api.response.FileData
import com.dfd.delfin.databinding.ActivityCommunicationAddressBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.businessverification.BusinessVerificationActivity
import com.dfd.delfin.utils.CurrentStepKey
import com.dfd.delfin.utils.TotalStepsKey
import com.dfd.delfin.utils.extensions.setup
import com.dfd.delfin.ui.businessverification.DocUploadAdapter
import com.dfd.delfin.utils.*
import com.dfd.delfin.utils.constants.FileType
import com.dfd.delfin.utils.extensions.*
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class CommunicationAddressActivity  : BaseActivity<ActivityCommunicationAddressBinding, CommunicationAddressViewModel>(),DocumentUtils.DocumentProgressInterface, DocumentUtils.DocumentListInterface {
    init {
        StatusBarColor = Color.parseColor("#ededff")
    }
    private var isCamera: Boolean = false
    private var mPhotoFile: File? = null
    private lateinit var uploadImageName: String
    private lateinit var localImageName: String
    val awsPath = "loadboard/address/"
    val docUploadAdapter : DocUploadAdapter by lazy { DocUploadAdapter() }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()
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

    @Inject lateinit var autoCompleteUtils: AutoCompleteUtils

    var flatFilled = false
    var areaFilled = false
    var cityFilled = false
    var pincodeFilled = false
    var proofTypeFilled = false
    var docUploadProof = true
    var dataSetFromPref =false
    var startTime: Long = 0
    var endTime: Long = 0


    override fun getViewModelClass() = CommunicationAddressViewModel::class.java

    override fun layoutId() = R.layout.activity_communication_address

    override fun requireConnection() = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("CommunicationAddressActivity_SetupTime")
        activitySetupTrace?.start()
        if(intent?.extras!=null){
            viewModel.currentStep = navigationUtils.getNavigationStepFormat(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!, intent?.extras?.getInt(
                TotalStepsKey)!!)
            if(userPrefs.addressRejectReason.isNotNullOrEmpty()) {
                binding.addressError.visibility=View.VISIBLE
                if(userPrefs.addressRejectReason.replace(" ", "").equals("Documentunderverification")){
                    viewModel.errorText = userPrefs.addressRejectReason
                }else {
                    viewModel.errorText =
                        "Address verification failed due to " + userPrefs.addressRejectReason
                }
            }else{
                binding.addressError.visibility=View.GONE
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setResult(RESULT_CANCELED)
                finish()
            }
        })

        startTime = System.currentTimeMillis()
        /* Address Proof */

        binding.btnSubmitDetails.setOnClickListener {

            if(viewModel.areaAddress.value!!.length<3){
                binding.errorArea.visibility=View.VISIBLE
                binding.imgWrongArea.visibility=View.VISIBLE
                ViewCompat.setElevation(binding.imgWrongArea, this.resources.getDimension( R.dimen.edit_text_raise_focus_z))
                binding.errorArea.setText("Minimum 3 characters required")
                areaFilled = false
                enableSubmitButton()
            }else {
                if (pincodeFilled) {
                    viewModel.documentProofType = binding.spinnerProof.selectedItem.toString()
                }
                viewModel.addNewAddress(false)
            }

        }
        viewModel.areaAddress.observe(this, Observer {
            if(it.isNotNullOrEmpty()){
                binding.errorArea.visibility=View.GONE
                binding.imgWrongArea.visibility=View.GONE
                areaFilled = true
                enableSubmitButton()
            }else{
                areaFilled = false
                enableSubmitButton()
            }
        })

        var isEdited = false
        if(userPrefs.businessAddress.isNotNullOrEmpty()){
            if(!userPrefs.getAddressList().isNullOrEmpty()){
                if(userPrefs.getAddressList()!!.size>0){
                    for(item in userPrefs.getAddressList()!!){
                        if(item?.addressType!!.startsWith("al")){
                            isEdited = true
                            fillDataFromBusinessAddress(item)
                        }
                    }
                }

            }

        }else{
            binding.editFlat.focusClick()
        }

        var cityLength = 0
        if(isEdited){
            autoCompleteUtils.skipFirstAutoCompleteCity(binding.autoCompleteCity) {
                uiUtils.toggleKeyboard()
                cityFilled = true
                cityLength = binding.autoCompleteCity.text.length
                enableSubmitButton()
            }
        }else{
            autoCompleteUtils.autoCompleteCity(binding.autoCompleteCity) {
                uiUtils.toggleKeyboard()
                cityFilled = true
                cityLength = binding.autoCompleteCity.text.length
                enableSubmitButton()
            }
        }

        binding.autoCompleteCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s == null || s.length == 0) {
                    cityFilled = false
                    enableSubmitButton()
                }else if (cityLength>0&& s.length<cityLength){
                    cityFilled = false
                    enableSubmitButton()
                }
            }
        })
        binding.editArea.lengthAction(1){
            areaFilled = true
            enableSubmitButton()
        }
        binding.editArea.lengthAction(0){
            areaFilled = false
            enableSubmitButton()
        }
        binding.editFlat.lengthAction(1){
            flatFilled = true
            enableSubmitButton()
        }
        binding.editFlat.lengthAction(0){
            flatFilled = false
            enableSubmitButton()
        }
        binding.editPincode.lengthAction(6){
            pincodeFilled = true
            enableSubmitButton()
        }
        binding.editPincode.lengthAction(5){
            pincodeFilled = false
            enableSubmitButton()
        }
        binding.uploadDocLay.setOnClickListener {
            val imageName = "Address_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)
        }
       binding.docRemove.setOnClickListener {
           showUploadImage()
       }

        var ert:Int? = null

        if (userPrefs.aadhaarNumber.isNullOrEmpty()) {
            ert = R.array.array_address__proof_type
        } else {
            ert = R.array.array_address__proof_type_aadhar_flow

        }


         binding.spinnerProof.setup(ert) { p, v ->
            if(p>0){
                proofTypeFilled = true
                Log.d("udyog",userPrefs.udyogNumber)
                if(!dataSetFromPref) {
                    showUploadImage()
                    if (!userPrefs.retryVerification){
                        if (userPrefs.aadhaarNumber.isNullOrEmpty()) {
                            if ((userPrefs.udyogNumber.isNotEmpty() && p == 4) || (userPrefs.shopNumber.isNotEmpty() && p == 5)) {
                                binding.textProof.visibility = View.GONE
                                binding.docLayout.visibility = View.GONE
                                docUploadProof = true
                            } else if ((userPrefs.businessDocType.equals("rc") && p == 6) || (userPrefs.businessDocType.equals("lr") && p == 2)
                            ) {
                                binding.textProof.visibility = View.GONE
                                binding.docLayout.visibility = View.GONE
                                docUploadProof = true
                            } else {
                                binding.textProof.visibility = View.VISIBLE
                                binding.docLayout.visibility = View.VISIBLE
                                docUploadProof = false
                            }

                        } else {
                            if ((userPrefs.udyogNumber.isNotEmpty() && p == 4)) {
                                binding.textProof.visibility = View.GONE
                                binding.docLayout.visibility = View.GONE
                                docUploadProof = true
                            } else if ((userPrefs.businessDocType.equals("rc") && p == 6) || (userPrefs.businessDocType.equals("lr") && p == 2)
                            ) {
                                binding.textProof.visibility = View.GONE
                                binding.docLayout.visibility = View.GONE
                                docUploadProof = true
                            } else {
                                binding.textProof.visibility = View.VISIBLE
                                binding.docLayout.visibility = View.VISIBLE
                                docUploadProof = false

                            }
                        }
                    }
                    enableSubmitButton()
                }else{
                    dataSetFromPref=false
                }
            }else{
                proofTypeFilled =false
                enableSubmitButton()
            }
        }

        viewModel.addAddressLiveData.observe(this, Observer {
            if (it) {
                var address =
                    viewModel.flatAddress + "," + viewModel.areaAddress.value + "," + viewModel.cityAddress + "-" + viewModel.pincodeAddress
                viewModel.updateCommunicationAddress(address, false)
            }
        })

        viewModel.subAddressLiveData.observe(this, Observer {
            if (it) {
                uiUtils.showSnackbar("Address updated")

                var address = viewModel.flatAddress + "," + viewModel.areaAddress.value + "," + viewModel.cityAddress + "-" + viewModel.pincodeAddress
                addDataToPreference(address)
                userPrefs.businessAddress = address

                endTime = System.currentTimeMillis()
                val ttl = endTime - startTime
                analyticsUtil.moEngageTrackEvent(
                    EVENT_SUBMIT_OFFICE_ADDRESS,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TTL, PROPERTY_ADD_PROOF_TYPE),
                    mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy", ttl.toString(), viewModel.documentProofType?:"alternate")
                )

//                navigationUtils.checkNavigationKycStep(this,intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
//                    TotalStepsKey)!!,null)
                if(userPrefs.retryVerification) {
                    userPrefs.addressRejectReason = "Document under verification"
                }
                finish()
                setResult(RESULT_OK)

            } else {
                uiUtils.showSnackbar("Error encountered, Please try again.")
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
    private fun addDataToPreference(address:String){
        val listOfAddress = mutableListOf<AddAddressModel>()
        val alternateAddressData =  AddAddressModel(userPrefs.phoneNumber, address,viewModel.documentProofType,viewModel.documentProofUrl,viewModel.addressType,false)
        listOfAddress.add(alternateAddressData)

        userPrefs.setAddressList(listOfAddress)
    }
    private fun showUploadImage() {
        binding.uploadDocLay.visibility=View.VISIBLE
        binding.docUploadedLay.visibility=View.GONE
        if(uploadArray.size>0){
            uploadArray.clear()
        }
        docUploadProof= false
        enableSubmitButton()
    }
    private fun enableSubmitButton(){
        binding.btnSubmitDetails.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof
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
                        dispatchGalleryIntent()
                    }
                } else {
                    uiUtils.showSnackbar(getString(R.string.storage_camera_permission))
                }
            }
    }

    private fun fillDataFromBusinessAddress(addressData: AddAddressModel) {
        var docArray: ArrayList<Pair<String, String>> = ArrayList()
        var docPath = ""
        if (!addressData.documentUrls.isNullOrEmpty()) {
            docPath = addressData.documentUrls?.get(0)!!
        }
        var docproofRec = ""
        var addressRec = addressData.address
        var flatRec = addressRec?.split(",")
            ?.get(0)
        //binding.editFlat.setText(flatRec)
        viewModel.flatAddress = flatRec!!
        flatFilled = true
        var areaRec = addressRec?.split(",")
            ?.get(1)
        //  binding.editArea.setText(areaRec)
        viewModel.areaAddress.value = areaRec!!
        areaFilled = true
        var citynPinRec = addressRec?.split(",")
            ?.get(2)
        var cityRec = citynPinRec?.split("-")
            ?.get(0)
        //  binding.autoCompleteCity.setText(cityRec)
        viewModel.cityAddress = cityRec!!
        cityFilled = true
        var pincodeRec = citynPinRec?.split("-")
            ?.get(1)
        pincodeFilled = true
        viewModel.pincodeAddress = pincodeRec!!
        if (docPath.isNullOrEmpty()) {
            resetUploadData()
            showUploadImage()
            //  binding.uploadDocLay.visibility= View.VISIBLE
            // binding.uploadedDocLay.visibility= View.GONE
            docUploadProof = false

        } else {
            dataSetFromPref = true
            docArray.add(
                Pair(
                    docPath, (mPhotoFile?.length()?.div(1024)).toString()
                )
            )
            if (addressData.documentUrls != null)
                viewModel.documentProofUrl.addAll(addressData.documentUrls!!)
            binding.uploadDocLay.visibility = View.GONE
            binding.docUploadedLay.visibility = View.VISIBLE
            binding.docTitle.setText(docArray.get(0).first)
            docUploadProof = true
            enableSubmitButton()
        }

        proofTypeFilled = true
        var spinnerIndex = 0
        if (addressData.proofDocumentType != null) {
            if(userPrefs.aadhaarNumber.isNotNullOrEmpty()) {
                if(userPrefs.isGstsByPanNotRegistered){
                    spinnerIndex = when {
                        addressData.proofDocumentType!!.startsWith("V", true) -> 1
                        addressData.proofDocumentType!!.startsWith("lr", true) -> 2
                        addressData.proofDocumentType!!.startsWith("le", true) -> 3
                        addressData.proofDocumentType!!.startsWith("ud", true) -> 4
                        addressData.proofDocumentType!!.startsWith("dr", true) -> 5
                        addressData.proofDocumentType!!.startsWith("rc", true) -> 6
                        else -> 0
                    }
                }else {
                    spinnerIndex = when {
                        addressData.proofDocumentType!!.startsWith("V", true) -> 1
                        addressData.proofDocumentType!!.startsWith("lr", true) -> 2
                        addressData.proofDocumentType!!.startsWith("le", true) -> 3
                        addressData.proofDocumentType!!.startsWith("ud", true) -> 4
                        addressData.proofDocumentType!!.startsWith("sh", true) -> 5
                        else -> 0
                    }
                }
            }else{
                spinnerIndex = when {
                    addressData.proofDocumentType!!.startsWith("V", true) -> 1
                    addressData.proofDocumentType!!.startsWith("lr", true) -> 2
                    addressData.proofDocumentType!!.startsWith("le", true) -> 3
                    addressData.proofDocumentType!!.startsWith("ud", true) -> 4
                    addressData.proofDocumentType!!.startsWith("sh", true) -> 5
                    else -> 0
                }
            }
            proofTypeFilled=true
            enableSubmitButton()
        }

        binding.spinnerProof.post(Runnable {
            binding.spinnerProof.setSelection(spinnerIndex)
            docproofRec = binding.spinnerProof.selectedItem.toString()
        })
        if (userPrefs.aadhaarNumber.isNullOrEmpty()) {
            binding.spinnerProof.setup(R.array.array_address__proof_type) { p, v ->
                if (p > 0) {
                    proofTypeFilled = true
                }
            }
            enableSubmitButton()
        } else {
            if(userPrefs.isGstsByPanNotRegistered){
                binding.spinnerProof.setup(R.array.array_address__proof_type) { p, v ->
                    if (p > 0) {
                        proofTypeFilled = true
                    }
                }
            }else{
                binding.spinnerProof.setup(R.array.array_address__proof_type_aadhar_flow) { p, v ->
                    if (p > 0) {
                        proofTypeFilled = true
                    }
                }
            }
            enableSubmitButton()
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
        // For CommunicationAddressActivity, all documents are communication address proofs
        // The specific proof type is determined when the user submits the form
        val docType = "alternate_address_proof"
        documentUtils.uploadDocument(file, fileType, docType, this)
    }

    private fun captureImage(
        uploadImageName: String,
        localImageName: String
    ) {
        this.uploadImageName = uploadImageName
        this.localImageName = localImageName

        val items = arrayOf<CharSequence>("Take Photo", "Choose from file", "Cancel")
        val builder = AlertDialog.Builder(this,R.style.DatePickerTheme)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Take Photo" -> requestImageCapturePermissions(true)
                items[item] == "Choose from file" -> requestImageCapturePermissions(false)
                items[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun showFileSelected() {
       binding.uploadDocLay.visibility=View.GONE
        binding.docUploadedLay.visibility=View.VISIBLE
        docUploadProof=true
        binding.docTitle.setText(uploadArray.get(0).first)
        binding.docSize.setText(uploadArray.get(0).second+" KB")
        enableSubmitButton()
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


    override fun onDocumentSuccess(documentUrl: String) {
        uiUtils.hideProgress()
        viewModel.documentProofUrl.clear()
        uploadArray.clear()
        viewModel.documentProofUrl.add(documentUrl)
        uploadArray.add(Pair(documentUrl.substringAfterLast("/"), (mPhotoFile?.length()?.div(1024)).toString()))
        showFileSelected()
        resetUploadData()
    }

    override fun onDocumentFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showSnackbar(error)
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
                        inputStream.copyTo(outputStream, bufferSize = 16384)
                        this.uploadImageName = "Address_" + System.currentTimeMillis()+"."+imageScopedFile.extension
                        this.localImageName =  "Address_" + System.currentTimeMillis()+"."+imageScopedFile.extension
                        if(imageScopedFile.extension=="jpg" ||imageScopedFile.extension=="png" || imageScopedFile.extension=="jpeg"){
                            fileType = FileType.IMAGE
                            mPhotoFile = fileCompressor.compressToFile(File(imageScopedFile.path), localImageName)
                        }else if (imageScopedFile.extension=="pdf"){
                            fileType = FileType.PDF
                            mPhotoFile = imageScopedFile
                        }else{
                            analyticsUtil.moEngageTrackEvent(
                                EVENT_DOC_UPLOADED_WITH_WRONG_EXTENSION,
                                mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO,
                                    PROPERTY_TYPE_OF_DOC, PROPERTY_SOURCE_PAGE),
                                mutableListOf(userPrefs.userId() , userPrefs.phoneNumber.toString(),imageScopedFile.extension,"communication_address")
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


    fun businessVerificationIntent(
        context: Context
    ) = Intent(context, BusinessVerificationActivity::class.java).apply {
    }
}