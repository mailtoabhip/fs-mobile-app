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
import android.os.PersistableBundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.data.address.AddressDetailData
import com.delhivery.axle.databinding.*
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.businessverification.DocUploadAdapter
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.getFileName
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.setup
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.android.synthetic.main.activity_verify_pan.*
import kotlinx.android.synthetic.main.dialog_add_alternate_address.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


class AddressActivity : BaseActivity<ActivityAddressBinding, CommunicationAddressViewModel>(),AWSUtils.AWSProgressInterface,AddressRVAdapterInterface {


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

    private val addressRVAdapter by lazy { AddressRVAdapter(this) }

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



    var flatFilled = false
    var areaFilled = false
    var cityFilled = false
    var pincodeFilled = false
    var proofTypeFilled = false
    var docUploadProof = true
    var selectedAddress =""
    var isSameAsGST =false
    override fun getViewModelClass() = CommunicationAddressViewModel::class.java

    override fun layoutId() = R.layout.activity_address

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
       viewModel.fetchAndAddUserAddress()
      binding.btnAddAlternateAddress.setOnClickListener {
          showAddAlternateAddressDialog()
      }
        binding.btnSubmitDetails.setOnClickListener {
            viewModel.updateCommunicationAddress(selectedAddress,isSameAsGST)
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
            adapter = this@AddressActivity.addressRVAdapter
        }
        viewModel.AddressLiveData.observe(this, Observer {
            it?.let { _items ->
          //      if(viewModel.addAddressLiveData.value!!) {
                    addressRVAdapter.operation(_items)
          //      }
            }
        })
        viewModel.alternateAddressAdded.observe(this, Observer {
            if(it){
            binding.btnAddAlternateAddress.visibility=View.GONE
            }
            else{
                binding.btnAddAlternateAddress.visibility=View.VISIBLE
            }
        })
        viewModel.updateAddressLiveData.observe(this, Observer {
            if (it) {
                //  startActivity(gstIntent(this))
                uiUtils.showSnackbar("Address updated")
                navigationUtils.checkNavigationKycStep(this,intent?.extras?.getInt(CurrentStepKey)?.plus(1)!!,intent?.extras?.getInt(
                    TotalStepsKey)!!,null)

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
                        this.uploadImageName = "Add_" + System.currentTimeMillis()+"_"+userPrefs.phoneNumber+"."+imageScopedFile.extension
                        this.localImageName =  "Add_" + System.currentTimeMillis()+"_"+userPrefs.phoneNumber+"."+imageScopedFile.extension
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
            viewModel.addNewAddress(false)
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
        bindingDialog.docRemove.setOnClickListener {
            resetUploadData()
            bindingDialog.uploadDocLay.visibility= View.VISIBLE
            bindingDialog.uploadedDocLay.visibility= View.GONE
        }

       bindingDialog.uploadDocLay.setOnClickListener {
           viewModel.captureAddressProof.postValue(true)
       }
        viewModel.showSubmitedDialog.observe(this, Observer {
            if(it){
                uiUtils.showToast("showsub")
                bindingDialog.uploadDocLay.visibility= View.GONE
                bindingDialog.uploadedDocLay.visibility= View.VISIBLE
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


    fun confirmDelete(proofType :String,flatAddress:String,areaAddress:String,cityAddress:String,pinCode:String) {
        val dialog = Dialog(this)
        val bindingDialog = DialogConfirmAddressDialogBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.buttonConfirm.setOnClickListener {
            //action after confirm button
            viewModel.documentProofType =  proofType
            viewModel.flatAddress = flatAddress
            viewModel.areaAddress = areaAddress
            viewModel.cityAddress = cityAddress
            viewModel.pincodeAddress = pinCode
            viewModel.addNewAddress(true)
            viewModel.documentProofUrl.clear()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }





    private fun showEditAlternateAddressDialog(addressDataItem: AddressDataItem) {
        val dialog = Dialog(this)
        val bindingDialog = DialogEditAlternateAddressBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        var docproofRec=""
        var addressRec= addressDataItem.key()
        var flatRec = addressRec.split(",").get(0)
        bindingDialog.editFlat.setText(flatRec)
        var areaRec = addressRec.split(",").get(1)
        bindingDialog.editArea.setText(areaRec)
        var citynPinRec = addressRec.split(",").get(2)
        var cityRec =citynPinRec.split("-").get(0)
        bindingDialog.editCity.setText(cityRec)
        var pincodeRec = citynPinRec.split("-").get(1)
        bindingDialog.editPincode.setText(pincodeRec)
        var spinnerIndex=0
        if(addressDataItem.data.proofDocumentType!!.startsWith("v",true)){
            spinnerIndex=1
        }else  if(addressDataItem.data.proofDocumentType!!.startsWith("lr",true)){
            spinnerIndex=2
        }else  if(addressDataItem.data.proofDocumentType!!.startsWith("le",true)){
            spinnerIndex=3
        }
        bindingDialog.spinnerProof.post(Runnable { bindingDialog.spinnerProof.setSelection(spinnerIndex)
            docproofRec=bindingDialog.spinnerProof.selectedItem.toString()

        })

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.docRemove.setOnClickListener {
            resetUploadData()
            bindingDialog.uploadDocLay.visibility= View.VISIBLE
            bindingDialog.uploadedDocLay.visibility= View.GONE
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

            viewModel.documentProofType =  docproofRec
            viewModel.flatAddress = flatRec
            viewModel.areaAddress = areaRec
            viewModel.cityAddress = cityRec
            viewModel.pincodeAddress =pincodeRec
            viewModel.addNewAddress(true)

            viewModel.documentProofType =  bindingDialog.spinnerProof.selectedItem.toString()
            viewModel.flatAddress = bindingDialog.editFlat.text.toString()
            viewModel.areaAddress = bindingDialog.editArea.text.toString()
            viewModel.cityAddress = bindingDialog.editCity.text.toString()
            viewModel.pincodeAddress =bindingDialog.editPincode.text.toString()
            viewModel.addNewAddress(false)
            viewModel.documentProofUrl.clear()

        }
        bindingDialog.btnConfirmDelete.setOnClickListener {



            confirmDelete(bindingDialog.spinnerProof.selectedItem.toString(),bindingDialog.editFlat.text.toString(),bindingDialog.editArea.text.toString()
                ,bindingDialog.editCity.text.toString(),bindingDialog.editPincode.text.toString())

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
                bindingDialog.uploadedDocLay.visibility= View.VISIBLE
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

    override fun editItem(item: AddressDataItem) {
        showEditAlternateAddressDialog(item)
    }

    override fun selectItem(item: AddressDataItem, position: Int) {
        selectedAddress=item.data.key()
        if(item.data.addressType.equals("gst",true)) {
            isSameAsGST=true
        }

        if(viewModel.selectedAdapterPos==-1) {
           var addressDataItem= AddressDataItem(
                AddressDetailData(
                    item.data.phone_number,
                    item.data?.address!!,
                    item.data.proofDocumentType,
                    item.data.documentUrls,
                    item.data.addressType,
                    item.data.isDeleted,
                    true
                )
            )
            viewModel.selectedAdapterPos=position
            viewModel.lastSelectedAddressLiveData.postValue(addressDataItem)
            addressRVAdapter.updateItem(addressDataItem)
            addressRVAdapter.notifyDataSetChanged()

        }else{

        if(viewModel.selectedAdapterPos==position){
           addressRVAdapter.updateItem(item)
            addressRVAdapter.notifyDataSetChanged()
        }else{
          var addressDataItem=AddressDataItem(
              AddressDetailData(
                  item.data.phone_number,
                  item.data?.address!!,
                  item.data.proofDocumentType,
                  item.data.documentUrls,
                  item.data.addressType,
                  item.data.isDeleted,
                  true
              )
          )
            var lastAddressDataItem=AddressDataItem(
                AddressDetailData(
                    viewModel.lastSelectedAddressLiveData.value?.data?.phone_number,
                    viewModel.lastSelectedAddressLiveData.value?.data?.address!!,
                    viewModel.lastSelectedAddressLiveData.value?.data?.proofDocumentType,
                    viewModel.lastSelectedAddressLiveData.value?.data?.documentUrls,
                    viewModel.lastSelectedAddressLiveData.value?.data?.addressType,
                    viewModel.lastSelectedAddressLiveData.value?.data?.isDeleted,
                    false
                )
            )
            viewModel.selectedAdapterPos=position
            viewModel.lastSelectedAddressLiveData.postValue(addressDataItem)
            addressRVAdapter.updateItem(addressDataItem)
            addressRVAdapter.updateItem(lastAddressDataItem)
            addressRVAdapter.notifyDataSetChanged()

        }

    }
    }

    override fun requireConnection(): Boolean =true

}