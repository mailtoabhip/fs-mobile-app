package com.delhivery.axle.ui.paymentdetails

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.databinding.library.BuildConfig
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.databinding.ActivityIdentityVerificationBinding
import com.delhivery.axle.databinding.ActivityPaymentDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.businessverification.BusinessVerificationActivity
import com.delhivery.axle.ui.kyc.aadhaar.UploadedItemRVAdapterInterface
import com.delhivery.axle.ui.kyc.gst.DocUploadAdapter
import com.delhivery.axle.ui.kyc.identityverification.IdentityVerificationViewModel
import com.delhivery.axle.ui.onboarding.BasicDetailsActivity
import com.delhivery.axle.ui.profile.MyProfileActivity
import com.delhivery.axle.utils.AWSUtils
import com.delhivery.axle.utils.BitmapUtils
import com.delhivery.axle.utils.DialogUtilsInterface
import com.delhivery.axle.utils.EVENT_SUBMIT_BUSINESS_PROOF
import com.delhivery.axle.utils.EVENT_SUBMIT_PAYMENT_DETAILS
import com.delhivery.axle.utils.FileCompressor
import com.delhivery.axle.utils.ImageUtils
import com.delhivery.axle.utils.PROPERTY_BUSINESS_PROOF_TYPE
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_TTL
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.REQCODE_CAMERA
import com.delhivery.axle.utils.REQCODE_FILE_ATTACHMENTS
import com.delhivery.axle.utils.REQCODE_TAKE_PHOTO
import com.delhivery.axle.utils.StepKey
import com.delhivery.axle.utils.extensions.focusClick
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

class PaymentDetailsActivity : BaseActivity<ActivityPaymentDetailsBinding, PaymentDetailsViewModel>(),DialogUtilsInterface, AWSUtils.AWSProgressInterface,
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
    var accountNum=false
    var accountName=false
    var ifsc = false
    var docUpload =false
    var nameDec =true
    var startTime: Long = 0
    var endTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            binding.accountNumEdittext.focusClick()
        }
        if(userPrefs.ifscCode.isNotNullOrEmpty()){
            viewModel.ifscText.value=userPrefs.ifscCode
        }
        showUploadedDoc()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setSupportActionBar(binding.progressStepLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationUtils.showProgressSteps(binding.progressStepLayout, 3)
        startTime = System.currentTimeMillis()

        viewModel.accountHolderText.observe(this, Observer {
            if(userPrefs.panName.equals(it,true)){
                binding.accountHolderWarning.visibility= View.GONE
                binding.nameDeclaration.visibility=View.GONE
                nameDec=true
                enableSubmitButton()
            }else{
                if(it.isNotNullOrEmpty()) {
                    nameDec=false
                    enableSubmitButton()
                    binding.accountHolderWarning.visibility = View.VISIBLE
                    binding.nameDeclaration.visibility = View.VISIBLE
                }
            }
        })

        viewModel.verificationDocUploadFailed.observe(this, Observer {
            showUploadedDoc()
            userPrefs.ninteen4CDocUrl=""
            userPrefs.paymentDocUrl=""
        })

        viewModel.delegationLiveData.observe(this, Observer {
            uploadImage(it.first, it.second)
        })

        viewModel.verificationDocUploadMsg.observe(this, Observer {
                if(viewModel.selected194CUpload.value==true){
                    sendDocForVerification(uploadArray1)
                    viewModel.selected194CUpload.value=false
                }else{
                   viewModel.updateUserDetails()
                }
        })

        viewModel.userUpdateLiveData.observe(this, Observer {
            if(it){
                userPrefs.paymentAccountNumber = viewModel.accountText.value.toString()
                userPrefs.ifscCode = viewModel.ifscText.value.toString()
                userPrefs.paymentAccountName = viewModel.accountHolderText.value.toString()

                endTime = System.currentTimeMillis()
                val ttl = endTime - startTime
                analyticsUtil.trackEvent(
                    EVENT_SUBMIT_PAYMENT_DETAILS,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TTL),
                    mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy", ttl.toString())
                )

                if(userPrefs.retryVerification){
                    userPrefs.paymentRejectReason="Document under verification"
                    userPrefs.isBankDetailsRejected=false
                    checkStatus()
                    navigationUtils.navigate(MyProfileActivity::class.java,true,null)
                }else {
                    navigationUtils.navigate(VendorPolicyActivity::class.java, true, null)
                }
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
                sendDocForVerification(uploadArray)
                if (binding.nameDeclaration.isChecked) {
                    viewModel.nameDeclaration = true
                } else {
                    viewModel.nameDeclaration = false
                }
            }
        }
        binding.uploadDocLay.setOnClickListener {
            val imageName = "accountProof_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)
        }
        binding.uploadDocLay1.setOnClickListener {
            val imageName = "194C_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)
            viewModel.selected194CUpload.value=true
        }
        binding.docRemove.setOnClickListener {
            showUploadImage()
        }
        binding.docRemove1.setOnClickListener {
            showUploadImage1()
            viewModel.selected194CUpload.value=false

        }
        viewModel.accountText.observe(this, Observer {
            if(it.isNotNullOrEmpty()){
                binding.imgWrongAccount.visibility=View.GONE
                binding.errorAccountNum.visibility=View.GONE
                accountNum=true
                enableSubmitButton()
            }else{
                accountNum=false
                enableSubmitButton()
            }

        })
        binding.accountNumEdittext.lengthAction(1){
            accountNum=true
            enableSubmitButton()
        }
        binding.accountNumEdittext.lengthAction(0){
            accountNum=false
            enableSubmitButton()
        }

        viewModel.accountHolderText.observe(this, Observer {
            if(it.isNotNullOrEmpty()){
                accountName=true
                enableSubmitButton()
            }else{
                accountName=false
                enableSubmitButton()
            }
        })
        binding.accountHolderEdittext.lengthAction(1){
            accountName=true
            enableSubmitButton()
        }
        binding.accountHolderEdittext.lengthAction(0){
            accountName=false
            enableSubmitButton()
        }

        viewModel.ifscText.observe(this, Observer {
            if(it.isNotNullOrEmpty()){
                ifsc=true
                enableSubmitButton()
            }else{
                ifsc=false
                enableSubmitButton()
            }
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

    override fun onBackPressed() {
        super.onBackPressed()
        if(userPrefs.retryVerification){
            navigationUtils.navigate(MyProfileActivity::class.java, true)
        }else {
            val bundle = Bundle()
            bundle.putInt(StepKey,3)
            navigationUtils.navigateKyc(this,true,bundle)
            navigationUtils.navigate(BusinessVerificationActivity::class.java, true)
        }
    }

    fun enableSubmitButton(){
        if(accountName&&accountNum&&ifsc&&docUpload&&nameDec) {
            binding.btnSubmit.isEnabled = true
        }else{
            binding.btnSubmit.isEnabled = false
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
    override fun onAWSSuccess(
        path: String
    ) {
        uiUtils.hideProgress()
        if(viewModel.selected194CUpload.value != true){
            uploadArray.add(Pair(path.replace(awsPath,""), (mPhotoFile?.length()?.div(1024)).toString()))
            showFileSelected()

        }else{
            uploadArray1.add(Pair(path.replace(awsPath,""), (mPhotoFile?.length()?.div(1024)).toString()))
            showFileSelected1()
        }
        resetUploadData()
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
    private fun showUploadedDoc(){
        if(userPrefs.paymentDocUrl.isNullOrEmpty()){
            showUploadImage()
        }else{
            resetUploadData()
            uploadArray.add(Pair(userPrefs.paymentDocUrl!!.replace(awsUtils.awsBasePath()+awsPath,""), (mPhotoFile?.length()?.div(1024)).toString()))
            showFileSelected()
        }
        if(userPrefs.ninteen4CDocUrl.isNullOrEmpty()){
            showUploadImage1()
        }else{
            resetUploadData()
            uploadArray1.add(Pair(userPrefs.ninteen4CDocUrl!!.replace(awsUtils.awsBasePath()+awsPath,""), (mPhotoFile?.length()?.div(1024)).toString()))
            showFileSelected1()
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
        if(viewModel.selected194CUpload.value != true) {
            this.uploadImageName =
                "account_proof_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"
            this.localImageName =
                "account_proof_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"
        }else{
            this.uploadImageName =
                "194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"
            this.localImageName =
                "194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"
        }

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
                        if(viewModel.selected194CUpload.value != true) {
                            this.uploadImageName =
                                "account_proof_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber +"."+ imageScopedFile.extension
                            this.localImageName =
                                "account_proof_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber +"."+imageScopedFile.extension
                        }else{
                            this.uploadImageName =
                                "194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber +"."+ imageScopedFile.extension
                            this.localImageName =
                                "194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber +"."+ imageScopedFile.extension
                        }
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


}