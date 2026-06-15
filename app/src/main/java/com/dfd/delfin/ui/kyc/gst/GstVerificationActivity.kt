package com.dfd.delfin.ui.kyc.gst

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.dfd.delfin.BuildConfig
import com.dfd.delfin.R
import com.dfd.delfin.R.string
import com.dfd.delfin.api.response.FileData
import com.dfd.delfin.data.gst.GstAction_ViewDetails
import com.dfd.delfin.data.transactions.TransactionTimeOutAction
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.kyc.address.CommunicationAddressActivity
import com.dfd.delfin.utils.*
import com.dfd.delfin.utils.extensions.getFileName
import com.dfd.delfin.utils.extensions.onBackground
import com.dfd.delfin.utils.extensions.plusAssign
import com.dfd.delfin.utils.prefs.UserPrefs
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import com.dfd.delfin.data.gst.GstDetailData
import com.dfd.delfin.data.gst.GstDetailItemData
import com.dfd.delfin.databinding.ActivityVerifyGstBinding
import com.dfd.delfin.ui.dialogs.ShowGstVerificationOtpDialog
import com.dfd.delfin.ui.kyc.aadhaar.UploadedItemRVAdapterInterface
import com.dfd.delfin.ui.kyc.gst.*
import com.dfd.delfin.utils.extensions.isNotNullOrEmpty
import com.dfd.delfin.utils.constants.FileType
import com.dfd.delfin.utils.DocumentUtils
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace


class GstVerificationActivity  : BaseActivity<ActivityVerifyGstBinding, GstVerificationViewModel>(),
        DialogUtilsInterface, GstRVAdapterInterface, DocumentUtils.DocumentProgressInterface, DocumentUtils.DocumentListInterface, UploadedItemRVAdapterInterface {
    init {
        StatusBarColor = Color.parseColor("#ededff")
    }
    @Inject lateinit var userPrefs: UserPrefs

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
    /* RV adapter */
    private val gstRVAdapter: GstRVAdapter by lazy { GstRVAdapter(this) }
    var currSelectedGst:String? = null
    val docUploadAdapter : DocUploadAdapter by lazy { DocUploadAdapter(this) }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()
    var startTime: Long = 0
    var endTime: Long = 0
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("GstVerificationActivity_SetupTime")
        activitySetupTrace?.start()
        if(intent?.extras!=null){
            viewModel.currentStep = navigationUtils.getNavigationStepFormat(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!, intent?.extras?.getInt(
                    TotalStepsKey)!!)
            if(userPrefs.identityRejectReason.isNotNullOrEmpty()) {
                binding.gstError.visibility=View.VISIBLE
                if(userPrefs.identityRejectReason.replace(" ", "").equals("Documentunderverification")){
                    viewModel.errorText = userPrefs.identityRejectReason
                }else {
                    viewModel.errorText =
                        ("GST verification failed due to " + userPrefs.identityRejectReason)
                }
            }else{
                binding.gstError.visibility=View.GONE
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


        if(userPrefs.retryVerification){
            binding.btnVerifyGst.setText(R.string.action_retry_verification)
        }
        if(userPrefs.gstNumber.isNotNullOrEmpty()){
           currSelectedGst=userPrefs.gstNumber

        }
        binding.gstList.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@GstVerificationActivity.gstRVAdapter
        }

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            refreshData()
        }

        viewModel.gstNumbersLiveData.observe(this, Observer {
            it?.let { _items ->

                gstRVAdapter.operation(_items)
            }
        })

        viewModel.gstDetailsLiveData.observe(this, Observer {
            gstRVAdapter.notifyDataSetChanged()
        })

        refreshData()

        binding.btnVerifyGst.setOnClickListener {
            dialogUtils.showVerifcationOptionsDialog(getString(R.string.label_gst_dialog_option2),this)
        }

        viewModel.resetKycLiveData.observe(this, Observer {
            if (it) {
                viewModel.updateUserDetails()
            } else {
                uiUtils.showSnackbar(getString(string.error_update_failed))
            }
        })

        viewModel.otpRecieved.observe(
                this, Observer {
            if(it){
                if(userPrefs.phoneNumber!=null)
                ShowGstVerificationOtpDialog(this,this,uiUtils,userPrefs.phoneNumber!!,dialogUtils,getString(R.string.label_gst_dialog_option2),viewModel,this@GstVerificationActivity,userPrefs,currSelectedGst?:"").show()
            }
        }
        )
        viewModel.docVerified.observe(
                this, Observer {
            if(it){
                uiUtils.hideProgress()
                if(userPrefs.gstNumber.equals(currSelectedGst, ignoreCase = true) ||userPrefs.gstNumber.isEmpty()){
                    viewModel.updateUserDetails()
                }else{
                    viewModel.resetKycDetails(userPrefs.retryVerification)
                }

            }else{
                uiUtils.hideProgress()
                viewModel.docVerificationFailedCount.postValue(viewModel.docVerificationFailedCount.value!!+1)
                resetUploadData()
                uploadArray =  ArrayList()
                viewModel.docVerificationFailedCount.postValue(viewModel.docVerificationFailedCount.value!!+1)
                if(viewModel.docVerificationFailedCount.value!! <1){
                dialogUtils.showUploadFailDialog(getString(R.string.label_gst_dialog_option2),this)
                }
            }
        }
        )

        viewModel.docVerificationFailedCount.observe(this, Observer {
            if(viewModel.docVerificationFailedCount.value==2){
                viewModel.docVerificationFailedCount.value=0
                userPrefs.isGstNotBypassed=false
                if(userPrefs.gstNumber.equals(currSelectedGst, ignoreCase = true) || userPrefs.gstNumber.isEmpty()){
                    viewModel.updateUserDetails()
                }else{
                    viewModel.resetKycDetails(userPrefs.retryVerification)
                }
            }
        })

        viewModel.userUpdateLiveData.observe(this, Observer {
            if (it) {
                if(userPrefs.retryVerification){
                    userPrefs.identityRejectReason= "Document under verification"
                }
                endTime = System.currentTimeMillis()
                val ttl = endTime - startTime
                analyticsUtil.moEngageTrackEvent(
                    EVENT_SUBMIT_GST,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TTL),
                    mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy", ttl.toString())
                )
                finish()
                setResult(RESULT_OK)
            } else {
                uiUtils.showSnackbar("GST verification failed, please try again")
            }
        })

        viewModel.gstDetailData.observe(this, Observer {
            binding.btnVerifyGst.isEnabled = it!=null && it.gstNumber.isNotNullOrEmpty()
        })
    }

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

    override fun getRequestAadhaarOtp() {
        viewModel.getRequestOtp(true)
    }

    override fun setAccountRoleSelection(selected: String) {

    }

    override fun navigateToBusinessVerification() {
    }

    override fun handleAction(
            actionId: String,
            item: BaseGstRVAdapterItem<*>
    ) {
        when (actionId) {
            GstAction_ViewDetails -> {
            }
            TransactionTimeOutAction -> {
                refreshData()
            }
        }
    }

    override fun fetchDetails(data: GstDetailData) {
        viewModel.fetchDetails(data)
    }

    override fun fetchCurrSelected(): String? {
      return currSelectedGst
    }

    override fun fetchCheckedDetails(data: GstDetailItemData?) {
        currSelectedGst = data?.gstNumber
        viewModel.gstDetailData.value =  data
        binding.btnVerifyGst.isEnabled =true
    }


    override fun onDocumentSuccess(downloadUrl: String) {
        uiUtils.hideProgress()
        // Store downloadUrl (from BE response) instead of filename
        // This downloadUrl will be sent to /upload_document API for verification
        uploadArray.add(
            Pair(
                downloadUrl, (mPhotoFile?.length()
                ?.div(1024)).toString()
            )
        )
        dialogUtils.showAttachmentDialog(docUploadAdapter,uploadArray,this,getString(R.string.label_gst_dialog_option2))
        resetUploadData()
    }

    override fun onDocumentFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Failed to upload: $error")
        dialogUtils.showAttachmentDialog(docUploadAdapter,uploadArray,this,getString(R.string.label_gst_dialog_option2))
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
        this.uploadImageName =  "GST_" + System.currentTimeMillis()+"_"+userPrefs.phoneNumber+".jpg"
        this.localImageName =  "GST_" + System.currentTimeMillis()+"_"+userPrefs.phoneNumber+".jpg"

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
            // Extract downloadUrls from uploadArray (first element of Pair is downloadUrl from /document/upload)
            // These downloadUrls will be sent to /upload_document API for verification
            val downloadUrls = uploadArray.map { it.first }
            viewModel.verifyByDoc(downloadUrls)
        }else{
            uiUtils.showToast("No file selected")
        }
        uploadArray.clear()
    }


    private fun refreshData() {
        viewModel.gstFetchList.clear()
        gstRVAdapter.resetStaticData()
        viewModel.fetchGstNumbers()
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
        val docType = "gst_certificate"
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
                        inputStream.copyTo(outputStream, bufferSize = 16384)
                        this.uploadImageName = "GST_" + System.currentTimeMillis()+"_"+userPrefs.phoneNumber+"."+imageScopedFile.extension
                        this.localImageName =  "GST_" + System.currentTimeMillis()+"_"+userPrefs.phoneNumber+"."+imageScopedFile.extension
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
                                mutableListOf(userPrefs.userId() , userPrefs.phoneNumber.toString(),imageScopedFile.extension,"gst")
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


    override fun onItemClicked(item: BaseGstRVAdapterItem<*>) {
        val data = item.data as? GstDetailData
        currSelectedGst = data?.gstDetailItemData?.gstNumber
        viewModel.gstDetailData.value =  data?.gstDetailItemData
        gstRVAdapter.notifyDataSetChanged()
    }

    override fun getViewModelClass(): Class<GstVerificationViewModel> = GstVerificationViewModel::class.java

    override fun layoutId() = R.layout.activity_verify_gst

    override fun requireConnection() = false
    override fun handleDeleteAction(item: Pair<String, String>) {
        uploadArray.remove(item)
    }

}

fun gstAddressVerificationIntent(
        context: Context
) = Intent(context, CommunicationAddressActivity::class.java).apply {
}
