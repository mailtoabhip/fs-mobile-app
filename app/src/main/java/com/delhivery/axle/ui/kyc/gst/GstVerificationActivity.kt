package com.delhivery.axle.ui.kyc.gst

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.data.gst.GstAction_ViewDetails
import com.delhivery.axle.data.transactions.TransactionTimeOutAction
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.kyc.address.CommunicationAddressActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.getFileName
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import com.delhivery.axle.data.gst.GstDetailData
import com.delhivery.axle.databinding.ActivityVerifyGstBinding
import com.delhivery.axle.ui.dialogs.ShowGstVerificationOtpDialog
import com.delhivery.axle.ui.kyc.aadhaar.UploadedItemRVAdapterInterface
import com.delhivery.axle.ui.kyc.gst.*
import com.delhivery.axle.ui.kyc.pan.PanVerificationActivity
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import kotlinx.android.synthetic.main.activity_verify_pan.*


class GstVerificationActivity  : BaseActivity<ActivityVerifyGstBinding, GstVerificationViewModel>(),
        DialogUtilsInterface, GstRVAdapterInterface, AWSUtils.AWSProgressInterface, UploadedItemRVAdapterInterface {
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
    lateinit var awsUtils: AWSUtils
    @Inject
    lateinit var fileCompressor: FileCompressor
    @Inject
    lateinit var bitmapUtils: BitmapUtils
    /* RV adapter */
    private val gstRVAdapter: GstRVAdapter by lazy { GstRVAdapter(this) }
    var currSelectedGst:String? = null
    val awsPath = "loadboard/gst/"
    val docUploadAdapter : DocUploadAdapter by lazy { DocUploadAdapter(this) }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(intent?.extras!=null){
            viewModel.currentStep = navigationUtils.getNavigationStepFormat(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!, intent?.extras?.getInt(
                    TotalStepsKey)!!)
            progress.progress = navigationUtils.getNavigationPercentage(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
                    TotalStepsKey)!!)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        viewModel.otpRecieved.observe(
                this, Observer {
            if(it){
                ShowGstVerificationOtpDialog(this,this,uiUtils,userPrefs.phoneNumber!!,dialogUtils,getString(R.string.label_gst_dialog_option2),viewModel,this@GstVerificationActivity).show()
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
                resetUploadData()
                uploadArray =  ArrayList()
                dialogUtils.showUploadFailDialog(getString(R.string.label_gst_dialog_option2),this)
            }
        }
        )
        viewModel.delegationLiveData.observe(this, Observer {
            uploadImage(it.first, it.second)
        })

        viewModel.userUpdateLiveData.observe(this, Observer {
            if (it) {
                navigationUtils.checkNavigationKycStep(this,intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
                        TotalStepsKey)!!,null)
            } else {
                uiUtils.showSnackbar("GST verification failed, please try again")
            }
        })

        viewModel.gstDetailData.observe(this, Observer {
            binding.btnVerifyGst.isEnabled = it!=null && it.gstNumber.isNotNullOrEmpty()
        })
    }

    override fun getRequestAadhaarOtp() {
        viewModel.getRequestOtp(true)
    }

    override fun setAccountRoleSelection(selected: String) {

    }

    override fun navigateToBusinessVerification() {
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val bundle = Bundle()
        bundle.putInt(StepKey, 0)
        navigationUtils.navigateKyc(this,false,bundle)
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

    override fun onAWSSuccess(
            path: String
    ) {
        uiUtils.hideProgress()
        uploadArray.add(Pair(path.replace(awsPath,""), (mPhotoFile?.length()?.div(1024)).toString()))
        dialogUtils.showAttachmentDialog(docUploadAdapter,uploadArray,this,getString(R.string.label_gst_dialog_option2),awsUtils)
        resetUploadData()
    }

    override fun onAWSFailure() {
        uiUtils.hideProgress()
        uiUtils.showToast("Failed to upload")
        dialogUtils.showAttachmentDialog(docUploadAdapter,uploadArray,this,getString(R.string.label_gst_dialog_option2),awsUtils)
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

    override  fun captureImage(
            uploadImageName: String,
            localImageName: String
    ) {
        this.uploadImageName =  "GST_" + System.currentTimeMillis()+".jpg"
        this.localImageName =  "GST_" + System.currentTimeMillis()+".jpg"

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
                        this.uploadImageName = "GST_" + System.currentTimeMillis()+"."+imageScopedFile.extension
                        this.localImageName =  "GST_" + System.currentTimeMillis()+"."+imageScopedFile.extension
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
