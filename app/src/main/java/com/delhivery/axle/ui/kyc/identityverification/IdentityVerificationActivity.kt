package com.delhivery.axle.ui.kyc.identityverification

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.databinding.ActivityBusinessVerificationBinding
import com.delhivery.axle.databinding.ActivityIdentityVerificationBinding
import com.delhivery.axle.databinding.DialogKycSubmittedBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.businessverification.BusinessVerificationViewModel
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.kyc.address.CommunicationAddressActivity
import com.delhivery.axle.ui.kyc.gst.DocUploadAdapter
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

class IdentityVerificationActivity: BaseActivity<ActivityIdentityVerificationBinding, IdentityVerificationViewModel>(),
    AWSUtils.AWSProgressInterface, View.OnClickListener {

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


    val awsPath = "loadboard/iv/"
  //  val docUploadAdapter : DocUploadAdapter by lazy { DocUploadAdapter(this) }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.editCinLayout.visibility= View.VISIBLE
        binding.layoutCin.isSelected=true
        binding.layoutUdyog.isSelected=false
        binding.layoutShop.isSelected=false
        binding.textCin.isChecked=true
        binding.textUdyog.isChecked=false
        binding.textShop.isChecked=false
//        if(intent?.extras!=null){
//            viewModel.currentStep = navigationUtils.getNavigationStepFormat(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!, intent?.extras?.getInt(
//                TotalStepsKey)!!)
//            progress.progress = navigationUtils.getNavigationPercentage(intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
//                TotalStepsKey)!!)
//        }

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.textCin.setOnClickListener{
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
            showUploadImage()
        }
        binding.textUdyog.setOnClickListener{
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
            showUploadImage()
        }
        binding.textShop.setOnClickListener{
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
            showUploadImage()
        }

        viewModel.delegationLiveData.observe(this, Observer {
            uploadImage(it.first, it.second)
        })

        viewModel.userUpdateLiveData.observe(this, Observer {
            if(it){
              //  showKycSubmittedDialog()
            }
        })

        binding.docRemove.setOnClickListener(this)

        binding.uploadDoc.setOnClickListener{
            if((binding.textCin.isChecked && binding.editCin.length()>0)||(binding.textUdyog.isChecked&& binding.editUdyog.length()>0)||(binding.textShop.isChecked&& binding.editShop.length()>0)){
                val imageName = "doc_" + System.currentTimeMillis()+".jpg"
                captureImage(imageName, imageName)
            }else{
                uiUtils.showToast("Please provide details first")
            }
        }

        binding.btnVerifyIdentity.setOnClickListener {
           // userPrefs.cinNumber =
            navigationUtils.navigate(identityVerificationIntent(this),true,null)
//            navigationUtils.checkNavigationKycStep(this,intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
//                TotalStepsKey)!!,null)
         /*   if(binding.textTruck.isChecked){
                viewModel.validateRC(viewModel.truckNumber.value!!)
            }*/

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

        val items = arrayOf<CharSequence>("Take Photo", "Choose a file", "Cancel")
        val builder = AlertDialog.Builder(this)
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
             uploadArray.removeAt(0)
         }
         binding.btnVerifyIdentity.isEnabled =false
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
                       var docType=""
                        if(binding.textCin.isChecked){
                            docType = "Cin_"
                        }else if (binding.textUdyog.isChecked){
                            docType = "Udyog_"
                        }else if (binding.textShop.isChecked){
                            docType = "Shop_"
                        }
                        this.uploadImageName = docType + System.currentTimeMillis()+"."+imageScopedFile.extension
                        this.localImageName =  docType + System.currentTimeMillis()+"."+imageScopedFile.extension
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
        }}

    override fun getViewModelClass()= IdentityVerificationViewModel::class.java

    override fun layoutId()= R.layout.activity_identity_verification

    override fun requireConnection()= false
    override fun onClick(p0: View?) {
       showUploadImage()
    }

    private fun identityVerificationIntent(
        context: Context
    ) = Intent(context, CommunicationAddressActivity::class.java).apply {
    }
}

