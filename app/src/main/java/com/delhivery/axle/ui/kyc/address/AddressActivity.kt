package com.delhivery.axle.ui.kyc.address

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Add
import com.amazonaws.util.IOUtils
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.data.address.AddressDetailData
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.databinding.ActivityAddressBinding
import com.delhivery.axle.databinding.DialogAddAlternateAddressBinding
import com.delhivery.axle.databinding.DialogGstAttachmentsBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.businessverification.DocUploadAdapter
import com.delhivery.axle.ui.kyc.gst.GstRVAdapter
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.getFileName
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.setup
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class AddressActivity : BaseActivity<ActivityAddressBinding, CommunicationAddressViewModel>(),AWSUtils.AWSProgressInterface,AddressRVAdapterInterface {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    init {
        StatusBarColor = Color.parseColor("#ededff")
    }
    private var isCamera: Boolean = false
    private var mPhotoFile: File? = null
    private lateinit var uploadImageName: String
    private lateinit var localImageName: String
    val awsPath = "loadboard/business/"
    val docUploadAdapter : DocUploadAdapter by lazy { DocUploadAdapter() }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()

    private val AddressRVAdapter by lazy { AddressRVAdapter(this) }

    @Inject
    lateinit var imageUtils: ImageUtils
    @Inject
    lateinit var awsUtils: AWSUtils
    @Inject
    lateinit var fileCompressor: FileCompressor
    @Inject
    lateinit var bitmapUtils: BitmapUtils


    var flatFilled = false
    var areaFilled = false
    var cityFilled = false
    var pincodeFilled = false
    var proofTypeFilled = false
    var docUploadProof = true
    var selectedAddress =""
    override fun getViewModelClass() = CommunicationAddressViewModel::class.java

    override fun layoutId() = R.layout.activity_address

    override fun requireConnection() = false

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
      binding.btnAddAlternateAddress.setOnClickListener {
          showAddAlternateAddressDialog()
      }
        binding.btnSubmitDetails.setOnClickListener {
            viewModel.updateCommunicationAddress(selectedAddress)
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
        binding.addressList.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@AddressActivity.AddressRVAdapter
        }
        viewModel.AddressLiveData.observe(this, Observer {
            it?.let { _items ->
          //      if(viewModel.addAddressLiveData.value!!) {
                    AddressRVAdapter.operation(_items)
          //      }
            }
        })
        viewModel.updateAddressLiveData.observe(this, Observer {
            if (it) {
                //  startActivity(gstIntent(this))
                uiUtils.showSnackbar("Address updated")

            } else {
                uiUtils.showSnackbar("Error encountered, Please try again.")
            }
        })


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
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(pickPhoto, REQCODE_GALLERY_PHOTO)
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

        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Take Photo" -> requestImageCapturePermissions(true)
                items[item] == "Choose from Library" -> requestImageCapturePermissions(false)
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
        viewModel.documentProofUrl.add(path)
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

            REQCODE_GALLERY_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        val selectedImage = data?.data
                        require(selectedImage != null)
                        val parcelFileDescriptor =
                            contentResolver?.openFileDescriptor(selectedImage, "r", null)
                        require(parcelFileDescriptor != null)
                        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                        require(
                            contentResolver != null && contentResolver?.getFileName(selectedImage) != null
                        )
                        val imageScopedFile =
                            File(cacheDir, contentResolver?.getFileName(selectedImage)!!)
                        val outputStream = FileOutputStream(imageScopedFile)
                        IOUtils.copy(inputStream, outputStream)

                        mPhotoFile = fileCompressor.compressToFile(File(imageScopedFile.path), localImageName)
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













    private fun showAddAlternateAddressDialog() {
        val dialog = Dialog(this)
        val bindingDialog =DialogAddAlternateAddressBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.spinnerProof.setup(R.array.array_address__proof_type) { p, v ->
            if(p>0){
                proofTypeFilled = true
                bindingDialog.btnSubmitDetails.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof
            }else{
                proofTypeFilled =false
                bindingDialog.btnSubmitDetails.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof
            }
        }

        bindingDialog.btnSubmitDetails.setOnClickListener {


            viewModel.documentProofType =  bindingDialog.spinnerProof.selectedItem.toString()
            viewModel.flatAddress = bindingDialog.editFlat.text.toString()
            viewModel.areaAddress = bindingDialog.editArea.text.toString()
            viewModel.cityAddress = bindingDialog.editCity.text.toString()
            viewModel.pincodeAddress =bindingDialog.editPincode.text.toString()
            var businessAddress = viewModel.flatAddress +","+viewModel.areaAddress+","+viewModel.cityAddress+"-"+viewModel.pincodeAddress

            viewModel.addNewAddress()
            viewModel.documentProofUrl.clear()
            //  navigationUtils.navigate(businessVerificationIntent(this),false)

        }

        //check length and enable/disable submit button
        bindingDialog.editCity.lengthAction(3){
            cityFilled = true
            bindingDialog.btnSubmitDetails.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof
        }
        bindingDialog.editCity.lengthAction(2){
            cityFilled = false
            bindingDialog.btnSubmitDetails.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof
        }
        bindingDialog.editArea.lengthAction(3){
            areaFilled = true
            bindingDialog.btnSubmitDetails.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof
        }
        bindingDialog.editArea.lengthAction(2){
            areaFilled = false
            bindingDialog.btnSubmitDetails.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof
        }
        bindingDialog.editFlat.lengthAction(3){
            flatFilled = true
            bindingDialog.btnSubmitDetails.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof
        }
        bindingDialog.editFlat.lengthAction(2){
            flatFilled = false
            bindingDialog.btnSubmitDetails.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof
        }
        bindingDialog.editPincode.lengthAction(6){
            pincodeFilled = true
            bindingDialog.btnSubmitDetails.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof
        }
        bindingDialog.editPincode.lengthAction(5){
            pincodeFilled = false
            bindingDialog.btnSubmitDetails.isEnabled = flatFilled&&areaFilled&&pincodeFilled&&cityFilled&&proofTypeFilled&&docUploadProof
        }
        viewModel.addAddressLiveData.observe(this, Observer {
            if (it) {
                //  startActivity(gstIntent(this))
                dialog.dismiss()
            } else {
                uiUtils.showSnackbar("Error encountered, Please try again.")
                dialog.dismiss()
            }
        })

       bindingDialog.uploadDocLay.setOnClickListener {
           viewModel.captureAddressProof.postValue(true)
       }
        viewModel.showSubmitedDialog.observe(this, Observer {
            if(it){
                bindingDialog.uploadDocLay.visibility= View.GONE
                bindingDialog.uploadDocLay.visibility= View.VISIBLE
                bindingDialog.docTitle.setText(uploadArray.get(0).first)
                bindingDialog.docSize.setText(uploadArray.get(0).second+" KB")
            }
        })

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    override fun handleAction(actionId: String, item: BaseAddressRVAdapterItem<*>) {

    }

    override fun onItemClicked(item: BaseAddressRVAdapterItem<*>) {
        super.onItemClicked(item)
        selectedAddress=item.data.key()
    }

}