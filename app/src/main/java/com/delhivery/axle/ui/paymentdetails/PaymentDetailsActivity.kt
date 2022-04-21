package com.delhivery.axle.ui.paymentdetails

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
import com.delhivery.axle.databinding.ActivityIdentityVerificationBinding
import com.delhivery.axle.databinding.ActivityPaymentDetailsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.kyc.aadhaar.UploadedItemRVAdapterInterface
import com.delhivery.axle.ui.kyc.gst.DocUploadAdapter
import com.delhivery.axle.ui.kyc.identityverification.IdentityVerificationViewModel
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(userPrefs.paymentRejectReason.isNotNullOrEmpty()){
            binding.paymentError.text="Payment verification failed due to"+userPrefs.paymentRejectReason
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        viewModel.accountHolderText.observe(this, Observer {
            if(userPrefs.bankName.equals(it,true)){
                binding.accountHolderWarning.visibility= View.GONE
                binding.nameDeclaration.visibility=View.GONE
            }else{
                binding.accountHolderWarning.visibility= View.VISIBLE
                binding.nameDeclaration.visibility=View.VISIBLE
            }
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


// 194c upload condition
        if (userPrefs.ownedTruck.isNotNullOrEmpty()) {
            if (userPrefs.ownedTruck.toInt() <= 10) {
                binding.uploadDoc1.visibility = View.VISIBLE
            } else {
                binding.uploadDoc1.visibility = View.GONE
            }
        }
    // show error reject
        userPrefs.identityRejectReason=""
        if(userPrefs.identityRejectReason.isNotNullOrEmpty()){
            binding.paymentError.visibility=View.VISIBLE
        }else{
            binding.paymentError.visibility=View.GONE
        }

        binding.btnSubmit.setOnClickListener {
          sendDocForVerification(uploadArray)
            if(binding.nameDeclaration.isChecked){
                viewModel.nameDeclaration=true
            }else{
                viewModel.nameDeclaration=false
            }
        }
        binding.uploadDocLay.setOnClickListener {
            val imageName = "accountProof_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)
            viewModel.selected194CUpload.value=false
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
                accountNum=true
                enableSubmitButton()
            }else{
                accountNum=false
                enableSubmitButton()
            }
        })
        viewModel.accountHolderText.observe(this, Observer {
            if(it.isNotNullOrEmpty()){
                accountName=true
                enableSubmitButton()
            }else{
                accountName=false
                enableSubmitButton()
            }
        })
        viewModel.ifscText.observe(this, Observer {
            if(it.isNotNullOrEmpty()){
                ifsc=true
                enableSubmitButton()
            }else{
                ifsc=false
                enableSubmitButton()
            }
        })



    }

    fun enableSubmitButton(){
        if(accountName&&accountNum&&ifsc&&docUpload) {
            binding.btnSubmit.isEnabled = true
        }else{
            binding.btnSubmit.isEnabled = false
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
                                "account_proof_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"
                            this.localImageName =
                                "account_proof_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"
                        }else{
                            this.uploadImageName =
                                "194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"
                            this.localImageName =
                                "194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"
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