package com.delhivery.axle.ui.businessverification

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.databinding.library.BuildConfig
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.databinding.ActivityBusinessVerificationBinding
import com.delhivery.axle.databinding.DialogKycSubmittedBinding
//import com.delhivery.axle.databinding.DialogBusinessVerificationAttachmentBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.activity.home.homeActivityIntent
import com.delhivery.axle.ui.kyc.aadhaar.UploadedItemRVAdapterInterface
import com.delhivery.axle.ui.kyc.gst.DocUploadAdapter
import com.delhivery.axle.ui.paymentdetails.PaymentDetailsActivity
import com.delhivery.axle.ui.paymentdetails.VendorPolicyActivity
import com.delhivery.axle.ui.profile.MyProfileActivity
import com.delhivery.axle.utils.*
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


class BusinessVerificationActivity : BaseActivity<ActivityBusinessVerificationBinding,BusinessVerificationViewModel>(),DialogUtilsInterface, AWSUtils.AWSProgressInterface,
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


    val awsPath = "loadboard/lr/"
    val docUploadAdapter : DocUploadAdapter by lazy { DocUploadAdapter(this) }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()
    var truckNum =false
    var ownedTruck =false
    var attachedTruck =false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.editTruck.visibility=View.VISIBLE
        binding.layoutTruckrd.isSelected=true
        binding.layoutUploadLR.isSelected=false
        binding.textLR.isChecked=false
        binding.textTruck.isChecked=true
        if(intent?.extras!=null){
            viewModel.currentStep = navigationUtils.getNavigationStepFormat(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!, intent?.extras?.getInt(
                TotalStepsKey)!!)
        }
        if(userPrefs.rcRejectReason.isNotNullOrEmpty()) {
            binding.businessError.visibility=View.VISIBLE
            if(userPrefs.rcRejectReason.replace(" ", "").equals("Documentunderverification")){
                viewModel.errorText = userPrefs.rcRejectReason
            }else {
                if (userPrefs.rcNumber.isNotNullOrEmpty()) {
                    viewModel.errorText =
                        "Truck RC verification failed due to " + userPrefs.rcRejectReason
                    } else {
                        viewModel.errorText =
                            "LR-LN verification failed due to " + userPrefs.rcRejectReason
                    }
                }
            }else{
                binding.businessError.visibility=View.GONE
            }

        if(userPrefs.ownedTruck.isNotNullOrEmpty()){
            viewModel.ownedTruck.value=userPrefs.ownedTruck
            ownedTruck=true
            setSubmitButtonEnable()
        }else{
            binding.ownedTrucksEdittext.focusClick()
        }
        if(userPrefs.attachedTruck.isNotNullOrEmpty()){
            viewModel.attachedTruck.value=userPrefs.attachedTruck
            attachedTruck=true
            setSubmitButtonEnable()
        }

        if(!userPrefs.businessDocUrl.isNullOrEmpty()){
            if(userPrefs.businessDocUrl.contains("Lr_doc",true)){
                binding.editTruck.visibility=View.GONE
                binding.textLR.isChecked=true
                binding.rcErrorMsg.visibility=View.GONE
                binding.imgWrong.visibility = View.GONE
                binding.editTruck.error= false
                binding.editTruck.setTextColor(ContextCompat.getColor(this, R.color.heading_black))
                binding.textTruck.isChecked=false
                userPrefs.rcManualverificationreq=true
                binding.layoutUploadLR.isSelected=true
                binding.layoutTruckrd.isSelected=false
                binding.layoutTruckrd.visibility=View.GONE
                setSubmitButtonEnable()
                viewModel.selected.value="lr"
            }else{
                if(userPrefs.rcNumber.isNotNullOrEmpty()){
                    viewModel.truckNumber.value=userPrefs.rcNumber
                    truckNum=true
                    setSubmitButtonEnable()
                }
            }
        }else{
              if(userPrefs.rcNumber.isNotNullOrEmpty()){
              viewModel.truckNumber.value=userPrefs.rcNumber
              truckNum=true
               setSubmitButtonEnable()
            }
        }
        showUploadedDoc()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setSupportActionBar(binding.progressStepLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationUtils.showProgressSteps(binding.progressStepLayout, 2)

        if(userPrefs.retryVerification){
            binding.btnVerifyBusiness.setText(R.string.action_retry_verification)
        }

        binding.textTruck.setOnClickListener{
            binding.editTruck.visibility=View.VISIBLE
            binding.layoutTruckrd.isSelected=true
            binding.layoutUploadLR.isSelected=false
            binding.textLR.isChecked=false
            resetUploadData()
          if(viewModel.truckNumber.value.isNotNullOrEmpty()) {
              if (viewModel.truckNumber.value?.length!!>=8) {
                  truckNum = true
                  setSubmitButtonEnable()
              }
          }
            uploadArray.clear()
            binding.docUploadedLay.visibility=View.GONE
            binding.layoutUploadLR.visibility=View.GONE
            binding.textTruck.isChecked=true
            binding.layoutTruckrd.visibility=View.VISIBLE

        }
        viewModel.truckNumber.observe(this, Observer {
            userPrefs.rcManualverificationreq=false
            resetUploadData()
            showUploadImage()
            binding.layoutUploadLR.visibility=View.GONE
        })

        binding.ownedTrucksEdittext.lengthAction(1){
            ownedTruck=true
            setSubmitButtonEnable()
        }
        binding.ownedTrucksEdittext.lengthAction(0){
            ownedTruck=false
            setSubmitButtonEnable()
        }
        binding.attachedTrucksEdittext.lengthAction(1){
            attachedTruck=true
            setSubmitButtonEnable()
        }
         binding.attachedTrucksEdittext.lengthAction(0){
            attachedTruck=false
            setSubmitButtonEnable()
        }

        viewModel.truckNumber.observe(this, Observer {
            if(it.length>=8){
                truckNum=true
                setSubmitButtonEnable()
            }else{
                truckNum=false
                setSubmitButtonEnable()
            }
        })

        viewModel.delegationLiveData.observe(this, Observer {
            uploadImage(it.first, it.second)
        })
        viewModel.rcVerificationErrorMsg.observe(this, Observer {
            binding.editTruck.error=true
            ViewCompat.setElevation(binding.imgWrong, this.resources.getDimension( R.dimen.edit_text_raise_focus_z))
            binding.editTruck.setTextColor(ContextCompat.getColor(this, R.color.error_red))
            binding.rcErrorMsg.visibility=View.VISIBLE
            binding.imgWrong.visibility = View.VISIBLE
            binding.rcErrorMsg.setText(it)
        })
        viewModel.userUpdateLiveData.observe(this, Observer {
            if(it){
                userPrefs.ownedTruck=viewModel.ownedTruck.value.toString()
                userPrefs.attachedTruck=viewModel.attachedTruck.value.toString()
                userPrefs.rcNumber=viewModel.truckNumber.value.toString()
                if(userPrefs.retryVerification){
                    userPrefs.rcRejectReason= "Document under verification"
                    if(!userPrefs.isBankDetailsRejected) {
                        userPrefs.verificationStatus = "pending"
                    }
                    navigationUtils.navigate(MyProfileActivity::class.java,true)
                }else{
                    navigationUtils.navigate(PaymentDetailsActivity::class.java,true)
                }
            }
        })

        viewModel.verificationDocUploadFailed.observe(this, Observer {
            showUploadedDoc()
            userPrefs.ninteen4CDocUrl=""
            userPrefs.paymentDocUrl=""
        })


        viewModel.manualVerificationRequired.observe(this, Observer {
            if(it&&userPrefs.rcManualverificationreq) {
                viewModel.selected.value = "rc"
                userPrefs.rcManualverificationreq=true
                dialogUtils.showUploadRcDialog(getString(R.string.label_business),this)
            }else{
                //show successfully submitted page
                    if (viewModel.truckNumber.value.isNotNullOrEmpty()) {
                        viewModel.updateUserRCDetails(viewModel.truckNumber.value!!)
                    }
            }
        })
        viewModel.verificationDocUploadMsg.observe(this, Observer {
            //show successfully submitted page
            viewModel.updateUserDetails()
        })

        binding.editTruck.lengthAction(8){
            binding.rcErrorMsg.visibility =  View.GONE
            binding.imgWrong.visibility =  View.GONE
            binding.editTruck.error= false
            binding.editTruck.setTextColor(ContextCompat.getColor(this, R.color.heading_black))
        }


        binding.textLR.setOnClickListener{
            binding.editTruck.visibility=View.GONE
            binding.textLR.isChecked=true
            binding.rcErrorMsg.visibility=View.GONE
            binding.imgWrong.visibility = View.GONE
            binding.editTruck.error= false
            binding.editTruck.setTextColor(ContextCompat.getColor(this, R.color.heading_black))
            binding.textTruck.isChecked=false
            userPrefs.rcManualverificationreq=true
            binding.layoutUploadLR.isSelected=true
            binding.layoutTruckrd.isSelected=false
            binding.layoutTruckrd.visibility=View.GONE
            binding.layoutUploadLR.visibility=View.VISIBLE
            truckNum=false
            showUploadImage()
            setSubmitButtonEnable()
            viewModel.selected.value="lr"

        }
        binding.layoutUploadLR.setOnClickListener {
            val imageName = "LR_doc_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)
        }

        binding.btnVerifyBusiness.setOnClickListener {
            if(binding.textTruck.isChecked){
                if (viewModel.truckNumber.value.isNotNullOrEmpty()) {
                    if(viewModel.manualVerificationRequired.value!=true || userPrefs.rcManualverificationreq!=true) {
                        viewModel.validateRC(viewModel.truckNumber.value!!)
                    }else{
                        sendDocForVerification(uploadArray)
                    }
                }
            }else{
                sendDocForVerification(uploadArray)
            }
            viewModel.ownedTruck.value=binding.ownedTrucksEdittext.text.toString()
            viewModel.attachedTruck.value=binding.attachedTrucksEdittext.text.toString()

        }

        binding.docRemove.setOnClickListener {
            showUploadImage()

        }

    }

    private fun showUploadedDoc(){
        if(!userPrefs.businessDocUrl.isNullOrEmpty()){
            resetUploadData()
            uploadArray.add(Pair(userPrefs.businessDocUrl!!.replace(awsUtils.awsBasePath()+awsPath,""), (mPhotoFile?.length()?.div(1024)).toString()))
            showFileSelected()
        }
    }

    fun setSubmitButtonEnable(){
        if(attachedTruck && ownedTruck && truckNum ){
            binding.btnVerifyBusiness.isEnabled=true
        }else{
            binding.btnVerifyBusiness.isEnabled=false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        userPrefs.retryVerificationOnBack=true
        val bundle = Bundle()
        bundle.putInt(StepKey,2)
        navigationUtils.navigateKyc(this,true,bundle)
    }
    override fun onAWSSuccess(
        path: String
    ) {
        uiUtils.hideProgress()
        uploadArray.add(Pair(path.replace(awsPath,""), (mPhotoFile?.length()?.div(1024)).toString()))
        showFileSelected()
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
        if(!binding.textTruck.isChecked) {
            this.uploadImageName =
                "Lr_doc_" + userPrefs.phoneNumber + ".jpg"
            this.localImageName =
                "Lr_doc_" + userPrefs.phoneNumber + ".jpg"
        }else{
            this.uploadImageName =
                "RC_doc_"+ userPrefs.phoneNumber + ".jpg"
            this.localImageName =
                "Rc_doc_" + userPrefs.phoneNumber + ".jpg"
        }

        val items = arrayOf<CharSequence>("Take Photo", "Choose a file", "Cancel")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Take Photo" -> requestImageCapturePermissions(true)
                items[item] == "Choose a file" -> requestImageCapturePermissions(false)
                items[item] == "Cancel" -> {dialog.dismiss()
                userPrefs.rcManualverificationreq=false
                }
            }
        }
        builder.show()
    }

    private fun showFileSelected() {
        binding.layoutUploadLR.visibility=View.GONE
        binding.docUploadedLay.visibility=View.VISIBLE
        binding.docTitle.setText(uploadArray.get(0).first)
        binding.docSize.setText(uploadArray.get(0).second+" KB")
        if(viewModel.truckNumber.value.isNullOrEmpty()&&binding.textTruck.isChecked){
          truckNum=false
        }else{
            truckNum=true
        }
        setSubmitButtonEnable()
    }

    private fun showUploadImage() {
        binding.layoutUploadLR.visibility=View.VISIBLE
        binding.docUploadedLay.visibility=View.GONE
        if(uploadArray.size>0){
            uploadArray.clear()
        }
        truckNum=false
        setSubmitButtonEnable()
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

    private fun showKycSubmittedDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogKycSubmittedBinding.inflate(layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        dialog.show()
        Handler().postDelayed({
            dialog.dismiss()
            //change flow as per config
            navigationUtils.navigate(HomeActivity::class.java, true)
         /*     navigationUtils.checkNavigationKycStep(this,intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
            TotalStepsKey)!!,null)*/
        }, 2000)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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
                        if(!binding.textTruck.isChecked) {
                            this.uploadImageName = "Lr_doc_" +userPrefs.phoneNumber+"."+imageScopedFile.extension
                            this.localImageName =  "Lr_doc_" +userPrefs.phoneNumber+"."+imageScopedFile.extension
                        }else{
                            this.uploadImageName = "RC_doc_" +userPrefs.phoneNumber+"."+imageScopedFile.extension
                            this.localImageName =  "RC_doc_" +userPrefs.phoneNumber+"."+imageScopedFile.extension
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


    override fun getViewModelClass()= BusinessVerificationViewModel::class.java

    override fun layoutId()=R.layout.activity_business_verification

    override fun requireConnection()= false



}