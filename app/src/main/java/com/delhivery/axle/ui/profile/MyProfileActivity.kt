package com.delhivery.axle.ui.profile

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Observer
import androidx.work.WorkManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.api.response.FileData
import com.delhivery.axle.databinding.ActivityMyProfileBinding
import com.delhivery.axle.databinding.DialogAccountDeletionSubmittedBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.loadwallet.loadWalletIntent
import com.delhivery.axle.utils.BitmapUtils
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.DocumentUtils
import com.delhivery.axle.utils.EVENT_USER_DELETE_ACCOUNT
import com.delhivery.axle.utils.EVENT_USER_LOGOUT
import com.delhivery.axle.utils.EVENT_VIEW_MY_PROFILE
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_TIME_SINCE_LAST_LOGIN
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import javax.inject.Inject


class MyProfileActivity  : BaseActivity<ActivityMyProfileBinding, HomeProfileViewModel>(), DocumentUtils.DocumentProgressInterface, DocumentUtils.DocumentListInterface  {
    init {
        StatusBarColor = Color.parseColor("#ffffff")
    }

    override fun getViewModelClass() = HomeProfileViewModel::class.java

    override fun layoutId() = R.layout.activity_my_profile

    override fun requireConnection() = false

    @Inject lateinit var documentUtils: DocumentUtils

    @Inject lateinit var userPrefs:UserPrefs

    val TAG_SYNC_DATA = "TAG_SYNC_DATA"
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = false
    @Inject
    lateinit var bitmapUtils: BitmapUtils
    
    // Store current download context
    private var currentProfileImageUrl: String? = null
    private var currentProfileFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("MyProfileActivity_SetupTime")
        activitySetupTrace?.start()
        isFirstResume = true
        /* setup toolbar */
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                userPrefs.setPreviousScreen(this.javaClass.name)
                finish()
            }
        })
        analyticsUtil.moEngageTrackEvent(
            EVENT_VIEW_MY_PROFILE,
            mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO),
            mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:""))

        binding.profile.text = getUserInitials()
        binding.appversion.text = "--v${BuildConfig.VERSION_NAME}"

        binding.logoutLayout.setOnClickListener {
            WorkManager.getInstance(applicationContext).cancelAllWorkByTag(TAG_SYNC_DATA)
            confirmLogout()
        }

        viewModel.logoutResultLiveData.observe(this) { success ->
            if (success) {
                navigationUtils.logout("Successfully logged out", "fromUser")
            } else {
                uiUtils.showSnackbar("Logout failed. Please try again.")
            }
        }

        viewModel.accountDeleteLiveData.observe(this) {
            if (it) {
                analyticsUtil.moEngageTrackEvent(EVENT_USER_DELETE_ACCOUNT)
                analyticsUtil.moEngageTrackEvent(
                    EVENT_USER_DELETE_ACCOUNT,
                    mutableListOf(PROPERTY_USER_ID , PROPERTY_TIME_SINCE_LAST_LOGIN),
                    mutableListOf(userPrefs.userId() , DateUtils.timeDiff(userPrefs.lastLoginTime))
                )
                userPrefs.requestedDeletion = true
                userPrefs.returningFromDeletion = true
                showAccountDeletionRequestDialog()
            }
        }



        if(viewModel.userPrefs.profileImageUrl.isNotNullOrEmpty()){
            downloadLogo()
        }


        binding.card1.visibility = View.GONE
        binding.profile.visibility = View.VISIBLE


        viewModel.setUserState()

        /* observe and update ui state */
        viewModel.stateLiveData.observe(this, StateObserver())


        binding.delhiveryWallet.setOnClickListener {
            userPrefs.setPreviousScreen(this.javaClass.name)
            startActivity(loadWalletIntent(this))
        }

    }
    private fun showAccountDeletionRequestDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogAccountDeletionSubmittedBinding.inflate(this.layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)
        dialog.setCancelable(false)
        if(!this.isFinishing)
            dialog.show()
        if(!this.isFinishing)
            Handler(Looper.myLooper()!!).postDelayed({
                dialog.dismiss()
                viewModel.logout()
            }, 3000)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    /**
     * ProfileUIState
     */
    inner class StateObserver : Observer<ProfileUIState?> {
        override fun onChanged(it: ProfileUIState?) {
            it?.let { state ->
                binding.state = state
                when (state) {
                    ProfileUIState.Axle->{}
                    ProfileUIState.PostLoad->{}
                    ProfileUIState.PostTruck->{ }
                }
            }
        }
    }
    
    /**
     * Confirm and logout
     */
    private fun confirmLogout() {
        dialogUtils.showBasicConfirmDialog(
                R.string.title_dialog_logout,
                R.string.msg_dialog_logout,
                positiveAction = "LOGOUT",
                negativeAction = "BACK",
                positiveClickListener = {
                    it.dismiss()
                    analyticsUtil.moEngageTrackEvent(
                            EVENT_USER_LOGOUT,
                            mutableListOf(PROPERTY_USER_ID , PROPERTY_TIME_SINCE_LAST_LOGIN),
                            mutableListOf(userPrefs.userId() , DateUtils.timeDiff(userPrefs.lastLoginTime))
                    )
                    viewModel.logout()
                }
        )
    }

    private fun downloadLogo() {
        val profileImageUrl = viewModel.userPrefs.profileImageUrl
        if (profileImageUrl.isNullOrEmpty()) {
            return
        }
        
        val file = getFile()
        if (file == null) {
            uiUtils.showSnackbar("Can't process image")
            return
        }
        
        // Extract s3_path from full S3 URL
        val s3Path = extractS3PathFromUrl(profileImageUrl)
        if (s3Path != null) {
            // Store mapping for callback
            currentProfileImageUrl = profileImageUrl
            currentProfileFile = file
            uiUtils.showProgress()
            // Use secure Document API to download
            documentUtils.downloadByS3Path(s3Path, this)
        } else {
            uiUtils.showSnackbar("Unable to extract document path from URL")
        }
    }
    
    /**
     * Extract s3_path from full S3 URL
     * Example: "https://bucket.s3.region.amazonaws.com/path/to/file.jpg"
     * Returns: "path/to/file.jpg"
     */
    private fun extractS3PathFromUrl(docUrl: String): String? {
        return try {
            // Check if it's already a relative path (no http/https protocol and no .amazonaws.com)
            if (!docUrl.startsWith("http://") && !docUrl.startsWith("https://") && !docUrl.contains(".amazonaws.com")) {
                // Already a relative path, return as-is (remove query parameters if any)
                return docUrl.split("?")[0]
            }
            
            // Construct AWS base path from config
            val bucket = com.delhivery.axle.config.AWSConfig.Bucket.value()
            val region = com.delhivery.axle.config.AWSConfig.ServerRegion.value()
            val awsBasePath = "https://$bucket.s3.$region.amazonaws.com/"
            
            // Remove base URL from full S3 URL
            if (docUrl.startsWith(awsBasePath)) {
                docUrl.replace(awsBasePath, "").split("?")[0] // Remove query parameters if any
            } else {
                // Fallback: Try to extract path from URL pattern (bucket.s3.region.amazonaws.com/path)
                val parts = docUrl.split(".amazonaws.com/")
                if (parts.size > 1) {
                    parts[1].split("?")[0] // Remove query parameters if any
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getFile(): File? {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val basePath = "$storageDir/" + System.currentTimeMillis()
        return File(basePath + "_profile.jpg")
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUser()
        binding.profile.text = getUserInitials()
        if(viewModel.userPrefs.profileImageUrl.isNotNullOrEmpty()){
            downloadLogo()
        }
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

    override fun onDocumentSuccess(downloadUrl: String) {
        binding.card1.visibility = View.VISIBLE
        binding.profile.visibility = View.GONE
        uiUtils.hideProgress()
        // downloadUrl is the download URL returned from /document/upload API
        viewModel.imageUrl = downloadUrl
        loadImage(viewModel.imagePath, binding.profilepic)
    }

    override fun onDocumentFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showSnackbar("Image processing failed: $error")
    }

    // Download functionality
    override fun onDocumentListSuccess(files: List<FileData>) {
        if (files.isNotEmpty()) {
            val documentFile = files.first()
            val profileImageUrl = currentProfileImageUrl
            val profileFile = currentProfileFile
            
            if (profileImageUrl != null && profileFile != null) {
                // Download file from pre-signed URL
                downloadFileFromUrl(documentFile.downloadUrl, profileImageUrl, profileFile)
            } else {
                uiUtils.hideProgress()
                uiUtils.showSnackbar("Download context lost")
                currentProfileImageUrl = null
                currentProfileFile = null
            }
        } else {
            uiUtils.hideProgress()
            uiUtils.showSnackbar("No profile images found")
            currentProfileImageUrl = null
            currentProfileFile = null
        }
    }

    override fun onDocumentListFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showSnackbar("Failed to load profile images: $error")
        currentProfileImageUrl = null
        currentProfileFile = null
    }
    
    /**
     * Download file from pre-signed URL using OkHttpClient
     */
    private fun downloadFileFromUrl(downloadUrl: String, originalUrl: String, file: File) {
        compositeDisposable += io.reactivex.Observable.fromCallable {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(downloadUrl)
                .build()
            
            val response: Response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                response.body()?.byteStream()?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                file.absolutePath
            } else {
                null
            }
        }
        .onBackground()
        .subscribe({ filePath ->
            if (filePath != null) {
                // Set the image path and load it
                viewModel.imagePath = filePath
                loadImage(filePath, binding.profilepic)
            } else {
                uiUtils.showSnackbar("Download failed")
            }
            uiUtils.hideProgress()
            currentProfileImageUrl = null
            currentProfileFile = null
        }, { error ->
            uiUtils.hideProgress()
            uiUtils.showSnackbar("Download error: ${error.message}")
            currentProfileImageUrl = null
            currentProfileFile = null
        })
    }

    //This functionality is not needed. Can be used in future.
    private fun downloadProfileImages() {
        uiUtils.showProgress()
        val docType = "visiting_card" // Document type for profile images
        documentUtils.listDocuments(docType, this)
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
                                    binding.profile.text = getUserInitials()
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

    private fun getUserInitials(): String {
        val name = if (userPrefs.userName.isNotNullOrEmpty()) userPrefs.userName else userPrefs.companyName
        if (name.isNullOrBlank()) return ""
        val parts = name.trim().split("\\s+".toRegex())
        return when {
            parts.size >= 2 -> "${parts[0][0]}${parts[1][0]}".uppercase()
            parts.size == 1 && parts[0].isNotEmpty() -> parts[0][0].uppercaseChar().toString()
            else -> ""
        }
    }

}