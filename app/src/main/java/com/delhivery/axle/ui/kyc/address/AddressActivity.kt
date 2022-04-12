package com.delhivery.axle.ui.kyc.address

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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.api.request.AddAddressModel
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.data.address.AddressDetailData
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.businessverification.DocUploadAdapter
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.*
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.android.synthetic.main.activity_verify_pan.*
import kotlinx.android.synthetic.main.dialog_add_alternate_address.*
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


class AddressActivity : BaseActivity<ActivityAddressBinding, CommunicationAddressViewModel>(),AWSUtils.AWSProgressInterface{


    init {
        StatusBarColor = Color.parseColor("#ededff")
    }
    private var isCamera: Boolean = false
    private var mPhotoFile: File? = null
    private lateinit var uploadImageName: String
    private lateinit var localImageName: String
    val awsPath = "loadboard/address/"
    var addressDeleted =false
    val docUploadAdapter : DocUploadAdapter by lazy { DocUploadAdapter() }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()


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

    @Inject lateinit var autoCompleteUtils: AutoCompleteUtils

    var flatFilled = false
    var areaFilled = false
    var cityFilled = false
    var pincodeFilled = false
    var proofTypeFilled = false
    var docUploadProof = false
    var selectedAddress =""
    var isSameAsGST =false
    var defaultSelectGst = true
    var selectedAddressData= AddAddressModel()
    var alternateAddressData= AddAddressModel()
    var gstAddressData= AddAddressModel()
    override fun getViewModelClass() = CommunicationAddressViewModel::class.java

    override fun layoutId() = R.layout.activity_address

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(intent?.extras!=null){
            viewModel.currentStep = navigationUtils.getNavigationStepFormat(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!, intent?.extras?.getInt(
                TotalStepsKey)!!)
            if(userPrefs.addressRejectReason.isNotNullOrEmpty()) {
                binding.addressError.visibility=View.VISIBLE
                viewModel.errorText = userPrefs.addressRejectReason
            }else{
                binding.addressError.visibility=View.GONE
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        title = " "
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(!userPrefs.getAddressList().isNullOrEmpty()){
            for(addressData in userPrefs.getAddressList()!!){
                if(addressData?.addressType!!.startsWith("g",true)){
                    viewModel.gstAddress= addressData.address?:""
                    binding.gstAddressLayout.visibility = View.VISIBLE
                    gstAddressData =  AddAddressModel(userPrefs.phoneNumber,addressData.address,addressData.proofDocumentType,addressData.documentUrls,addressData.addressType,false)
                    if(userPrefs.businessAddress.isNotNullOrEmpty() && userPrefs.businessAddress.equals(addressData.address)){
                        binding.gstAddressLayout.isSelected = true
                        selectedAddressData = gstAddressData
                    }else if(userPrefs.getAddressList()!!.size<2){
                        binding.gstAddressLayout.isSelected = true
                        selectedAddressData = gstAddressData
                    }
                }else if(addressData?.addressType!!.startsWith("al",true)){
                    viewModel.alternateAddress= addressData.address?:""
                    binding.alternateAddressLayout.visibility = View.VISIBLE
                    binding.btnAddAlternateAddress.visibility = View.GONE
                    alternateAddressData =  AddAddressModel(userPrefs.phoneNumber,addressData.address,addressData.proofDocumentType,addressData.documentUrls,addressData.addressType,false)
                    if(userPrefs.businessAddress.isNotNullOrEmpty() && userPrefs.businessAddress.equals(addressData.address)){
                        binding.alternateAddressLayout.isSelected = true
                        selectedAddressData = alternateAddressData
                    }else{
                        binding.gstAddressLayout.isSelected = true
                        selectedAddressData = gstAddressData
                    }
                }


            }
        }
        if(userPrefs.retryVerification){
            binding.btnSubmitDetails.setText(R.string.action_retry_verification)
        }
        binding.gstAddressLayout.setOnClickListener {
            binding.gstAddressLayout.isSelected = true
            binding.alternateAddressLayout.isSelected = false
            selectedAddressData = gstAddressData
        }

        binding.alternateAddressLayout.setOnClickListener {
            binding.gstAddressLayout.isSelected = false
            binding.alternateAddressLayout.isSelected = true
            selectedAddressData = alternateAddressData
        }

        binding.btnAddAlternateAddress.setOnClickListener {
          showAddAlternateAddressDialog(false,null)
      }
        binding.btnSubmitDetails.setOnClickListener {
            if(selectedAddressData.addressType.equals("gst")){
                isSameAsGST =true
            }
            viewModel.updateCommunicationAddress(selectedAddressData.address!!,isSameAsGST)
        }
        viewModel.delegationLiveData.observe(this, Observer {
            uploadImage(it.first, it.second)
        })
        viewModel.captureAddressProof.observe(this, Observer {
            if(it){
                val imageName = "Add_" + System.currentTimeMillis()+".jpg"
                captureImage(imageName, imageName)
                viewModel.captureAddressProof.postValue(false)
            }
        })
        binding.iconEdit.setOnClickListener {
            showAddAlternateAddressDialog(true,alternateAddressData)
        }

        viewModel.subAddressLiveData.observe(this, Observer {
            if (it) {
                uiUtils.showSnackbar("Address updated")
                if(userPrefs.retryVerification){
                    userPrefs.addressRejectReason= ""
                }
                 addDataToPreference()
                userPrefs.businessAddress = selectedAddressData.address!!
                navigationUtils.checkNavigationKycStep(this,intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
                    TotalStepsKey)!!,null)

            } else {
                uiUtils.showSnackbar("Error encountered, Please try again.")
            }
        })


    }


    override fun onBackPressed() {
        super.onBackPressed()
        userPrefs.retryVerificationOnBack=true
        val bundle = Bundle()
        bundle.putInt(StepKey,1)
        navigationUtils.navigateKyc(this,true,bundle)
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

    private fun captureImage(
        uploadImageName: String,
        localImageName: String
    ) {
        this.uploadImageName = uploadImageName
        this.localImageName = localImageName

        val items = arrayOf<CharSequence>("Take Photo", "Choose from file", "Cancel")
        val builder = AlertDialog.Builder(this)
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

        viewModel.showSubmitedDialog.postValue(true)
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


    override fun onAWSSuccess(
        path: String
    ) {
        uiUtils.hideProgress()
        viewModel.documentProofUrl.clear()
        val s3url= awsUtils.awsBasePath()
        viewModel.documentProofUrl.add(s3url+path)
        uploadArray.add(Pair(path.replace(awsPath,""), (mPhotoFile?.length()?.div(1024)).toString()))
        showFileSelected()
        resetUploadData()
    }

    override fun onAWSFailure() {
        uiUtils.hideProgress()
        resetUploadData()
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
                        viewModel.getDelegationToken(mPhotoFile!!)
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
                        this.uploadImageName = "Add_" + System.currentTimeMillis()+"."+imageScopedFile.extension
                        this.localImageName =  "Add_" + System.currentTimeMillis()+"."+imageScopedFile.extension
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


    private fun showAddAlternateAddressDialog(isEdit:Boolean, addressData: AddAddressModel?) {
        val dialog = Dialog(this)
        val bindingDialog =DialogAddAlternateAddressBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        bindingDialog.uploadDocLay.visibility= View.VISIBLE
        bindingDialog.uploadedDocLay.visibility= View.GONE
        docUploadProof=false
        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.spinnerProof.setup(R.array.array_address__proof_type) { p, v ->
            proofTypeFilled = p>0
            enableAddAddressDialogButton(bindingDialog)
        }

        bindingDialog.btnSubmitDetails.setOnClickListener {
            viewModel.documentProofType = bindingDialog.spinnerProof.selectedItem.toString()
            viewModel.flatAddress =  bindingDialog.editFlat.text.toString()
            viewModel.areaAddress =  bindingDialog.editArea.text.toString()
            viewModel.cityAddress =  bindingDialog.autoCompleteCity.text.toString()
            viewModel.pincodeAddress =  bindingDialog.editPincode.text.toString()
            viewModel.addNewAddress(false)
            dialog.dismiss()
        }
        bindingDialog.btnSaveChanges.setOnClickListener {
            viewModel.documentProofType = bindingDialog.spinnerProof.selectedItem.toString()
            viewModel.flatAddress =  bindingDialog.editFlat.text.toString()
            viewModel.areaAddress =  bindingDialog.editArea.text.toString()
            viewModel.cityAddress =  bindingDialog.autoCompleteCity.text.toString()
            viewModel.pincodeAddress =  bindingDialog.editPincode.text.toString()
            viewModel.addNewAddress(false)
            dialog.dismiss()
        }
        var onlyDelete = false
        var cityLength = 0
        autoCompleteUtils.autoCompleteCity(bindingDialog.autoCompleteCity) {
            uiUtils.toggleKeyboard()
            cityFilled = true
            cityLength = bindingDialog.autoCompleteCity.text.length
            enableAddAddressDialogButton(bindingDialog)
        }
        bindingDialog.autoCompleteCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s == null || s.length == 0) {
                    cityFilled = false
                    enableAddAddressDialogButton(bindingDialog)
                }else if (cityLength>0&& s.length<cityLength){
                    cityFilled = false
                    enableAddAddressDialogButton(bindingDialog)
                }
            }
        })

        bindingDialog.editArea.lengthAction(3){
            areaFilled = true
            enableAddAddressDialogButton(bindingDialog)
        }
        bindingDialog.editArea.lengthAction(2){
            areaFilled = false
            enableAddAddressDialogButton(bindingDialog)
        }
        bindingDialog.editFlat.lengthAction(1){
            flatFilled = true
            enableAddAddressDialogButton(bindingDialog)
        }
        bindingDialog.editFlat.lengthAction(0){
            flatFilled = false
            enableAddAddressDialogButton(bindingDialog)
        }
        bindingDialog.editPincode.lengthAction(6){
            pincodeFilled = true
            enableAddAddressDialogButton(bindingDialog)
        }
        bindingDialog.editPincode.lengthAction(5){
            pincodeFilled = false
            enableAddAddressDialogButton(bindingDialog)
        }
        viewModel.addAddressLiveData.observe(this, Observer {
            if (it) {
                viewModel.alternateAddress= viewModel.flatAddress + "," + viewModel.areaAddress + "," + viewModel.cityAddress + "-" + viewModel.pincodeAddress
                viewModel.addressType = "alternate"
                binding.alternateAddressLayout.visibility = View.VISIBLE
                binding.btnAddAlternateAddress.visibility = View.GONE
                alternateAddressData =  AddAddressModel(userPrefs.phoneNumber,   viewModel.alternateAddress,viewModel.documentProofType,viewModel.documentProofUrl,viewModel.addressType,false)
                binding.alternateAddressLayout.isSelected = true
                binding.gstAddressLayout.isSelected = false
                binding.alterateAddressText.setText(viewModel.alternateAddress)
                selectedAddressData = alternateAddressData
                addDataToPreference()
            } else {
                uiUtils.showSnackbar("Error encountered, Please try again.")
            }
        })
        viewModel.deleteAddressLiveData.observe(this, Observer {
            if (it) {
                    viewModel.alternateAddress=""
                    binding.alternateAddressLayout.visibility = View.GONE
                    binding.btnAddAlternateAddress.visibility = View.VISIBLE
                    binding.gstAddressLayout.isSelected = true
                    alternateAddressData =  AddAddressModel()
                    selectedAddressData = gstAddressData
                    addDataToPreference()
            } else {
                uiUtils.showSnackbar("Error encountered, Please try again.")
            }
        })

        viewModel.showSubmitedDialog.observe(this, Observer {
            if(it){
                bindingDialog.uploadDocLay.visibility= View.GONE
                bindingDialog.uploadedDocLay.visibility= View.VISIBLE
                docUploadProof=true
                bindingDialog.docTitle.setText(uploadArray.get(0).first)
                bindingDialog.docSize.setText(uploadArray.get(0).second+" KB")
                enableAddAddressDialogButton(bindingDialog)
            }
        })

        bindingDialog.docRemove.setOnClickListener {
            resetUploadData()
            docUploadProof=false
            enableAddAddressDialogButton(bindingDialog)
            bindingDialog.uploadDocLay.visibility= View.VISIBLE
            bindingDialog.uploadedDocLay.visibility= View.GONE
        }

       bindingDialog.uploadDocLay.setOnClickListener {
           viewModel.captureAddressProof.postValue(true)
       }
        bindingDialog.btnConfirmDelete.setOnClickListener {
            confirmDelete(viewModel.documentProofType!!,viewModel.flatAddress,viewModel.areaAddress,viewModel.cityAddress,viewModel.pincodeAddress,dialog)
        }

        if(isEdit&& addressData!=null){
            bindingDialog.btnSubmitDetails.visibility = View.GONE
            bindingDialog.saveDeleteLl.visibility = View.VISIBLE
            fillDataFromBusinessAddress(addressData,bindingDialog)
        }else{
            bindingDialog.btnSubmitDetails.visibility = View.VISIBLE
            bindingDialog.saveDeleteLl.visibility = View.GONE
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)

    }

    fun addDataToPreference(){
        val listOfAddress = mutableListOf<AddAddressModel>()
        listOfAddress.add(gstAddressData)
        if(alternateAddressData.addressType.equals("alternate")){
            listOfAddress.add(alternateAddressData)
        }
        userPrefs.setAddressList(listOfAddress)
    }
    fun enableAddAddressDialogButton(bindingDialog:DialogAddAlternateAddressBinding){
        bindingDialog.btnSaveChanges.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof
        bindingDialog.btnSubmitDetails.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof

    }
    private fun fillDataFromBusinessAddress(addressData: AddAddressModel,bindingDialog: DialogAddAlternateAddressBinding){
        var docArray:ArrayList<Pair<String, String>> = ArrayList()
        var docPath =""
        if(!addressData.documentUrls.isNullOrEmpty()){
            docPath= addressData.documentUrls?.get(0)!!
        }
        var docproofRec=""
        var addressRec =  addressData.address
        var flatRec = addressRec?.split(",")?.get(0)
        bindingDialog.editFlat.setText(flatRec)
         viewModel.flatAddress = flatRec!!
        flatFilled=true
        var areaRec = addressRec?.split(",")?.get(1)
        bindingDialog.editArea.setText(areaRec)
        viewModel.areaAddress = areaRec!!
        areaFilled=true
        var citynPinRec = addressRec?.split(",")?.get(2)
        var cityRec =citynPinRec?.split("-")?.get(0)
        bindingDialog.autoCompleteCity.setText(cityRec)
         viewModel.cityAddress = cityRec!!
        cityFilled=true
        var pincodeRec = citynPinRec?.split("-")?.get(1)
        pincodeFilled=true
        bindingDialog.editPincode.setText(pincodeRec)
         viewModel.pincodeAddress = pincodeRec!!
        if(docPath.isNullOrEmpty()){
            resetUploadData()
            bindingDialog.uploadDocLay.visibility= View.VISIBLE
            bindingDialog.uploadedDocLay.visibility= View.GONE
            docUploadProof=false

        }else{
            docArray.add(Pair(docPath!!.replace(awsUtils.awsBasePath()+awsPath,""), (mPhotoFile?.length()?.div(1024)).toString()))
            if(addressData.documentUrls!=null)
                viewModel.documentProofUrl.addAll(addressData.documentUrls!!)
            bindingDialog.uploadDocLay.visibility= View.GONE
            bindingDialog.uploadedDocLay.visibility = View.VISIBLE
            bindingDialog.docTitle.setText(docArray.get(0).first)
            docUploadProof=true
        }


        var spinnerIndex=0
        if(addressData.proofDocumentType!=null) {
            spinnerIndex = when {
                addressData.proofDocumentType!!.startsWith("V", true) -> 1
                addressData.proofDocumentType!!.startsWith("lr", true) -> 2
                addressData.proofDocumentType!!.startsWith("le", true) -> 3
                addressData.proofDocumentType!!.startsWith("ud", true) -> 4
                addressData.proofDocumentType!!.startsWith("sh", true) -> 5
                else -> 0
            }

        }
        if(spinnerIndex>0){
            proofTypeFilled=true
        }
        bindingDialog.spinnerProof.post(Runnable { bindingDialog.spinnerProof.setSelection(spinnerIndex)
            viewModel.documentProofType=bindingDialog.spinnerProof.selectedItem.toString()
        })

        enableAddAddressDialogButton(bindingDialog)
    }
    fun confirmDelete(proofType :String,flatAddress:String,areaAddress:String,cityAddress:String,pinCode:String, editDialog:Dialog) {
        val dialog = Dialog(this)
        val bindingDialog = DialogConfirmAddressDialogBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.buttonConfirm.setOnClickListener {
            //action after confirm button
            editDialog.dismiss()
            viewModel.documentProofType =  proofType
            viewModel.flatAddress = flatAddress
            viewModel.areaAddress = areaAddress
            viewModel.cityAddress = cityAddress
            viewModel.pincodeAddress = pinCode
            viewModel.addNewAddress(true)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    override fun requireConnection(): Boolean =true

}