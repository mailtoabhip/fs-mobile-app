package com.dfd.delfin.ui.profile.profiledetails

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.dfd.delfin.BuildConfig
import com.dfd.delfin.R
import com.dfd.delfin.api.response.FileData
import com.dfd.delfin.databinding.ActivityProfileDetailsBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.utils.*
import com.dfd.delfin.utils.constants.FileType
import com.dfd.delfin.utils.extensions.*
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


class ProfileDetailsActivity : BaseActivity<ActivityProfileDetailsBinding, ProfileDetailsViewModel>(), DocumentUtils.DocumentProgressInterface, DocumentUtils.DocumentListInterface, DialogUtilsInterface {

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
    lateinit var documentUtils: DocumentUtils
    @Inject
    lateinit var fileCompressor: FileCompressor
    @Inject
    lateinit var bitmapUtils: BitmapUtils
    @Inject
    lateinit var userPrefs: UserPrefs

    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("ProfileDetailsActivity_SetupTime")
        activitySetupTrace?.start()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title =  "Profile Details"

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                userPrefs.setPreviousScreen(this.javaClass.name)
                finish()
            }
        })

        viewModel.businessName.value = viewModel.userPrefs.companyName
        viewModel.userName.value = viewModel.userPrefs.userName
        uiUtils.toggleKeyboard()
        binding.busineesName.clearFocus()

        if(viewModel.userPrefs.profileImageUrl.isNotNullOrEmpty()){
        //if(true){
            downloadLogo()
            binding.card1.visibility = View.VISIBLE
            binding.profile.visibility = View.GONE
        }else{
            binding.card1.visibility = View.GONE
            binding.profile.visibility = View.VISIBLE
        }

        viewModel.businessName.observe(this, androidx.lifecycle.Observer {
            if (it.isNotNullOrEmpty()) {
                binding.profile.text = it[0].uppercaseChar().toString()
            }
            enableSubmitButton()
        })
        viewModel.userName.observe(this, androidx.lifecycle.Observer {
            enableSubmitButton()
        })


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

        // Removed delegation token logic - direct upload now

        viewModel.statusLiveData.observe(this, androidx.lifecycle.Observer {
            if (it) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        })

        binding.btnSave.setOnClickListener {
            viewModel.updateUserDetails()
        }

        // Download functionality removed - new API only supports upload
        // If download is needed, use listDocuments() to get file URLs

    }

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

    private fun downloadLogo() {
        // Use new DocumentUtils to list profile images
        uiUtils.showProgress()
        val docType = "visiting_card"
        documentUtils.listDocuments(docType, this)
    }

    override fun getRequestAadhaarOtp() {
    }

    override fun setAccountRoleSelection(selected: String) {
    }

    override fun navigateToBusinessVerification() {
     val bundle =Bundle()
     bundle.putInt(StepKey, 3)
        navigationUtils.navigateKyc(this, false, bundle)
    }

    override fun captureImage(
            uploadImageName: String,
            localImageName: String
    ) {
        this.uploadImageName = uploadImageName
        this.localImageName = localImageName

        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder = AlertDialog.Builder(this,R.style.DatePickerTheme)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Take Photo" -> requestImageCapturePermissions(true)
                items[item] == "Choose from Library" -> requestImageCapturePermissions(false)
                items[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    override fun sendDocForVerification(uploadArray: ArrayList<Pair<String, String>>) {
    }

    private fun loadImage(
            path: String?,
            view: AppCompatImageView
    ) {

        path?.let {
            // Convert dp to pixels (52dp = fixed size)
            val sizeInPx = (52 * resources.displayMetrics.density).toInt()

            Glide.with(view.context)
                .load(bitmapUtils.decodeSampledBitmap(path, sizeInPx, sizeInPx))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("DOWNLOAD_IMAGE-onLoadFailed==>>", "onLoadFailed")
                        binding.card1.visibility = View.GONE
                        binding.profile.text = viewModel.userPrefs.companyName?.get(0).toString().uppercase()
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
                        Log.d("DOWNLOAD_IMAGE-onResourceReady==>>", "onResourceReady")
                        binding.card1.visibility = View.VISIBLE
                        binding.profile.visibility = View.GONE
                        return false
                    }

                }).circleCrop().into(view)
        }

        /*view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view.viewTreeObserver.removeOnPreDrawListener(this)
                val imageViewHeight = view.measuredHeight
                val imageViewWidth = view.measuredWidth
                path?.let {
                    Glide.with(view.context)
                            .load(bitmapUtils.decodeSampledBitmap(path, imageViewWidth, imageViewHeight))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                            .listener(object : RequestListener<Drawable?> {
                                override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                                        isFirstResource: Boolean
                                ): Boolean {
                                    Log.d("DOWNLOAD_IMAGE-onLoadFailed==>>", "onLoadFailed")
                                    binding.card1.visibility = View.GONE
                                    binding.profile.text = viewModel.userPrefs.companyName?.get(0).toString().uppercase()
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
                                    Log.d("DOWNLOAD_IMAGE-onResourceReady==>>", "onResourceReady")
                                    binding.card1.visibility = View.VISIBLE
                                    binding.profile.visibility = View.GONE
                                    return false
                                }

                            }).circleCrop().into(view)
                }
                return true
            }
        })*/
    }

    private fun requestImageCapturePermissions(isCamera: Boolean) {
        this.isCamera = isCamera
        // Only request CAMERA permission - scoped storage doesn't require WRITE_EXTERNAL_STORAGE
        // Camera uses getExternalFilesDir() (app-specific storage)
        // Gallery uses ACTION_PICK with ContentResolver (scoped storage)
        compositeDisposable += requestPermission(
                arrayOf(Manifest.permission.CAMERA)
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

    @SuppressLint("SuspiciousIndentation")
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


    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(localImageName, ".jpg", storageDir)
    }

    private fun dispatchGalleryIntent() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(pickPhoto, REQCODE_GALLERY_PHOTO)
    }

    private fun uploadImage(file: File) {
        uiUtils.showProgress()
        val docType = "profile" // Profile image document type (VAPT doc specifies "profile" for profile images)
        // VAPT requirement: Profile needs uploadImageName in dynamic_variables
        val dynamicVariables = mapOf("uploadImageName" to uploadImageName)
        documentUtils.uploadDocument(file, FileType.IMAGE, docType, this, dynamicVariables)
        viewModel.imagePath = file.path
    }

    override fun onDocumentSuccess(downloadUrl: String) {
        uiUtils.showToast("Upload successful")
        // downloadUrl is the download URL returned from /document/upload API
        binding.card1.visibility = View.VISIBLE
        binding.profile.visibility = View.GONE
        uiUtils.hideProgress()
        viewModel.imageUrl = downloadUrl // Store downloadUrl from API response
        loadImage(viewModel.imagePath, binding.profilepic)
        resetUploadData()
    }

    override fun onDocumentFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Image processing failed: $error")
        resetUploadData()
    }

    // Download functionality
    override fun onDocumentListSuccess(files: List<FileData>) {
        uiUtils.hideProgress()
        if (files.isNotEmpty()) {
            // Get the most recent profile image (first in list)
            val latestFile = files.first()
            Log.d("DOWNLOAD_IMAGE-latestFile==>>", "$latestFile")
            downloadProfileImage(latestFile)
        } else {
            uiUtils.showToast("No profile images found")
            // Show default profile view
            binding.card1.visibility = View.GONE
            binding.profile.visibility = View.VISIBLE
        }
    }

    override fun onDocumentListFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Failed to load profile images: $error")
        // Show default profile view on failure
        binding.card1.visibility = View.GONE
        binding.profile.visibility = View.VISIBLE
    }

    private fun downloadProfileImage(file: FileData) {
        // No permission needed - using app-specific storage (getExternalFilesDir)
        // which doesn't require WRITE_EXTERNAL_STORAGE on Android 10+ (API 29+)
        try{
            val localFile = getFile()
            // Download the image using the download URL
            Log.d("DOWNLOAD_IMAGE-localFile==>>", "$localFile")
            downloadImageFromUrl(file.downloadUrl, localFile)
        } catch (e: Exception){
            uiUtils.showSnackbar("Can't create local file for download")
            binding.card1.visibility = View.GONE
            binding.profile.visibility = View.VISIBLE
        }
    }

    private fun downloadImageFromUrl(downloadUrl: String, localFile: File) {
        // Use Glide to download and cache the image
        Glide.with(this)
            .download(downloadUrl)
            .into(object : com.bumptech.glide.request.target.CustomTarget<File>() {
                override fun onResourceReady(resource: File, transition: com.bumptech.glide.request.transition.Transition<in File>?) {
                    try {
                        // Copy the downloaded file to our local file
                        resource.copyTo(localFile, overwrite = true)
                        Log.d("DOWNLOAD_IMAGE-localFile.path==>>", "${localFile.path}")
                        viewModel.imagePath = localFile.path
                        loadImage(localFile.path, binding.profilepic)
                    } catch (e: Exception) {
                        Log.d("DOWNLOAD_IMAGE-catch==>>", "${e.printStackTrace()}")
                        uiUtils.showSnackbar("Failed to save image: ${e.message}")
                        binding.card1.visibility = View.GONE
                        binding.profile.visibility = View.VISIBLE
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.d("DOWNLOAD_IMAGE-onLoadFailed==>>", "${errorDrawable.toString()}")
                    uiUtils.showSnackbar("Failed to download profile image")
                    binding.card1.visibility = View.GONE
                    binding.profile.visibility = View.VISIBLE
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Called when the image is cleared
                    Log.d("DOWNLOAD_IMAGE-onLoadCleared==>>", "onLoadCleared")
                }
            })
    }

    private fun showDocumentList(files: List<com.dfd.delfin.api.response.DocumentFile>) {
        // Show list of available profile images for download
        val fileNames = files.map { it.filename }
        uiUtils.showSnackbar("Found ${files.size} profile image(s): ${fileNames.joinToString(", ")}")
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
                        uploadImage(mPhotoFile!!)
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
                        inputStream.copyTo(outputStream, bufferSize = 16384)

                        // Strict validation for Image types only for Profile Pictures
                        if (imageScopedFile.extension == "jpg" || imageScopedFile.extension == "png" || imageScopedFile.extension == "jpeg") {
                            mPhotoFile = fileCompressor.compressToFile(File(imageScopedFile.path), localImageName)
                        } else {
                            // Profile pictures only support images. PDF or other types are not appropriate here.
                            uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                            return
                        }

                        if (mPhotoFile == null) {
                            uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                            return
                        }
                        uiUtils.showProgress()
                        uploadImage(mPhotoFile!!)
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

    private fun getFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val basePath = "$storageDir/" + System.currentTimeMillis()
        return File(basePath + "_profile.jpg")
    }
}