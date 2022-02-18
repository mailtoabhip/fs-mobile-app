package com.delhivery.axle.ui.profile.profiledetails

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.amazonaws.util.IOUtils
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.databinding.ActivityProfileDetailsBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.*
import kotlinx.android.synthetic.main.activity_profile_details.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.StringBuilder
import javax.inject.Inject


class ProfileDetailsActivity : BaseActivity<ActivityProfileDetailsBinding, ProfileDetailsViewModel>(), AWSUtils.AWSProgressInterface {

    override fun getViewModelClass() = ProfileDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_profile_details

    override fun requireConnection() = true

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

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title =  "Profile Details"

        viewModel.businessName.value = viewModel.userPrefs.companyName
        viewModel.userName.value = viewModel.userPrefs.userName
        uiUtils.toggleKeyboard()
        binding.busineesName.clearFocus()

        setUserRoleOption()

        if(viewModel.userPrefs.profileImageUrl.isNotNullOrEmpty()){
            downloadLogo()
            binding.card1.visibility = View.VISIBLE
            binding.profile.visibility = View.GONE
        }else{
            binding.card1.visibility = View.GONE
            binding.profile.visibility = View.VISIBLE
        }

        if((viewModel.userPrefs.isLoadBoardClient == false || viewModel.userPrefs.isLoadBoardSupplier == false)){
            binding.switchLay.visibility = View.VISIBLE
            binding.loadSwitch.isChecked = viewModel.userPrefs.canViewThirdPartyLoads
        }else{
            if(viewModel.userPrefs.userMode.equals("post_load")) {
                binding.switchLay.visibility = View.GONE
            }else{
                binding.switchLay.visibility = View.VISIBLE
                binding.loadSwitch.isChecked = viewModel.userPrefs.canViewThirdPartyLoads
            }

        }

        viewModel.businessName.observe(this, androidx.lifecycle.Observer {
            if (it.isNotNullOrEmpty()) {
                binding.profile.text = it[0].toUpperCase().toString()
            }
            enableSubmitButton()
        })
        viewModel.userName.observe(this, androidx.lifecycle.Observer {
            enableSubmitButton()
        })

        binding.radioType.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId==binding.radioType.getChildAt(0).id){
                viewModel.userMode = "post_load"
            }else if(checkedId==binding.radioType.getChildAt(1).id){
                viewModel.userMode = "post_truck"
            }else if(checkedId==binding.radioType.getChildAt(2).id){
                viewModel.userMode = "both"
            }
            setUserRoleOption()
        }

        viewModel.userUpdateLiveData.observe(this, androidx.lifecycle.Observer {
            if (it) {
                uiUtils.showSnackbar("User details updated successfully")
            } else {
                uiUtils.showSnackbar("Update Failed, Please try again")
            }
        })

        binding.updateLogo.setOnClickListener {
            val imageName = "profile_" + System.currentTimeMillis()
            captureImage(imageName, imageName)
        }

        viewModel.delegationLiveData.observe(this, androidx.lifecycle.Observer {
            uploadImage(it.first, it.second)
        })

        viewModel.statusLiveData.observe(this, androidx.lifecycle.Observer {
            if (it) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        })

        binding.btnSave.setOnClickListener {
            viewModel.loadSwitch = loadSwitch.isChecked
            viewModel.userRole = binding.spinnerProof.selectedItem.toString().replace(" ", "_").toLowerCase()
            viewModel.updateUserDetails()
        }

        viewModel.delegationDownloadLiveData.observe(this, Observer {
            if (it != null) {
                awsUtils.startDownload(it.first, it.second, it.third, this)
                viewModel.imagePath = it.third.path
            } else {
                uiUtils.showSnackbar("Please try again")
            }
        })

    }

    private fun downloadLogo() {
        compositeDisposable += requestPermission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .onBackground()
                .subscribe { granted, error ->
                    if (error == null && granted) {
                        val file = getFile()
                        if (file != null) {
                               viewModel.getDownloadDelegationToken(viewModel.userPrefs.profileImageUrl, file)
                        } else {
                            uiUtils.showSnackbar("Can't process image")
                        }
                    } else {
                        uiUtils.showSnackbar(getString(R.string.storage_permission))
                    }
                }
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

    private fun loadImage(
            path: String?,
            view: AppCompatImageView
    ) {
        view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view.viewTreeObserver.removeOnPreDrawListener(this)
                val imageViewHeight = view.measuredHeight
                val imageViewWidth = view.measuredWidth
                path?.let {
                    GlideApp.with(view.context)
                            .load(bitmapUtils.decodeSampledBitmap(path, imageViewWidth, imageViewHeight))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                            .listener(object : RequestListener<Drawable?> {
                                override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                                        isFirstResource: Boolean
                                ): Boolean {
                                    binding.card1.visibility = View.GONE
                                    binding.profile.text = viewModel.userPrefs.companyName?.get(0).toString().toUpperCase()
                                    binding.profile.visibility = View.VISIBLE
                                    return false
                                }

                                override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                ): Boolean {
                                    binding.card1.visibility = View.VISIBLE
                                    binding.profile.visibility = View.GONE
                                    return false
                                }

                            }).circleCrop().into(view)
                }
                return true
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

    private fun setUserRoleOption(){
        if(viewModel.userMode.equals("post_truck")) {
            (binding.radioType.getChildAt(1) as RadioButton).isChecked = true
            binding.spinnerProof.setup(R.array.array_occupation_post_truck) { p, v ->
            }
            setRoleSelect()
        }else  if(viewModel.userMode.equals("post_load")) {
            (binding.radioType.getChildAt(0) as RadioButton).isChecked = true
            binding.spinnerProof.setup(R.array.array_occupation_post_load) { p, v ->
            }
            setRoleSelect()
        }else  if(viewModel.userMode.equals("both")) {
            (binding.radioType.getChildAt(2) as RadioButton).isChecked = true
            binding.spinnerProof.setup(R.array.array_occupation_both) { p, v ->
            }
            setRoleSelect()
        }
    }

    private fun setRoleSelect(){
        if (viewModel.userPrefs.userRole.isNotNullOrEmpty()) {
            val myString = viewModel.userPrefs.userRole.split("_")
            val y = StringBuilder()
            var i = 0
            for (m in myString){
                i= i+1
                if(i>1){
                    y.append(" ")
                }
                y.append(m.substring(0, 1).toUpperCase() + m.substring(1).toLowerCase())

            }
            binding.spinnerProof.setSelection((binding.spinnerProof.getAdapter() as ArrayAdapter<String?>).getPosition(y.toString()))
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
        val awsPath = "loadboard/profile/$uploadImageName.jpg"
        awsUtils.startUpload(delegationToken, awsPath, file, this)
        viewModel.imagePath = file.path
    }

    override fun onAWSSuccess(
            path: String
    ) {
        binding.card1.visibility = View.VISIBLE
        binding.profile.visibility = View.GONE
        uiUtils.hideProgress()
        viewModel.imageUrl = path
        loadImage(viewModel.imagePath, binding.profilepic)
        resetUploadData()
    }

    override fun onAWSFailure() {
        uiUtils.hideProgress()
        uiUtils.showSnackbar("Image processing failed, please try again.")
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

    private fun enableSubmitButton() {
        binding.btnSave.isEnabled = viewModel.businessName.value?.trim().isNotNullOrEmpty() && viewModel.userName.value?.trim().isNotNullOrEmpty()
    }

    private fun getFile(): File? {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val basePath = "$storageDir/" + System.currentTimeMillis()
            return File(basePath + "_profile.jpg")
    }
}