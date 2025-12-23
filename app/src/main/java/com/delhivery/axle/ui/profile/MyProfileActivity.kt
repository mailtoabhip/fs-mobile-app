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
import com.delhivery.axle.ui.ledger.consolidatedPageIntent
import com.delhivery.axle.ui.profile.kycdetails.ProfileKYCDetailsActivity
import com.delhivery.axle.ui.profile.profiledetails.ProfileDetailsActivity
import com.delhivery.axle.ui.profile.raterewards.ShareRateGetRewardsActivity
import com.delhivery.axle.ui.team.teamMembersIntent
import com.delhivery.axle.ui.userroutes.userRoutesIntent
import com.delhivery.axle.utils.*
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
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        title = "My Profile"
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

        if(userPrefs.companyName.isNotNullOrEmpty()) {
            binding.profile.text = userPrefs.companyName[0].uppercaseChar().toString()
        }
        binding.appversion.text = "App version ${BuildConfig.VERSION_NAME}"

        binding.logoutLayout.setOnClickListener {
            WorkManager.getInstance(applicationContext).cancelAllWorkByTag(TAG_SYNC_DATA)
            confirmLogout()
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
        viewModel.getUserLiveData.observe(this, Observer {
            if(it){
                setVerficationStatus()
                setIssueCount()
            }
        })


        binding.startKyc.setOnClickListener {
            if(userPrefs.isLoadBoardClient== false || userPrefs.isLoadBoardSupplier == false) {
                //do nothing
            }else{
                if(!userPrefs.isUserVerfied){
                    val bundle = Bundle()
                    if(userPrefs.pancard.isNullOrEmpty()) {
                        bundle.putInt(StepKey, 0)
                        navigationUtils.navigateKyc(this,false,bundle)
                    }else  if(!(userPrefs.aadhaarNumber.isNotNullOrEmpty() ||userPrefs.gstNumber.isNotNullOrEmpty() ||(userPrefs.cinNumber.isNotNullOrEmpty()||userPrefs.shopNumber.isNotNullOrEmpty()||userPrefs.udyogNumber.isNotNullOrEmpty()))){
                        bundle.putInt(StepKey, 1)
                        navigationUtils.navigateKyc(this,false,bundle)
                    }else  if(userPrefs.businessAddress.isNullOrEmpty()){
                        bundle.putInt(StepKey, 2)
                        navigationUtils.navigateKyc(this,false,bundle)
                    }else  if(!userPrefs.userMode.equals("post_load")){
                        if( userPrefs.rcNumber.isNullOrEmpty() && !userPrefs.isTruckingDocumentUploaded){
                            bundle.putInt(StepKey, 3)
                            navigationUtils.navigateKyc(this,false,bundle)
                        }else{
                            uiUtils.showSnackbar("KYC Completed, Verification Pending")
                        }
                    }else{
                        uiUtils.showSnackbar("KYC Completed, Verification Pending")
                    }
                }
            }
        }

        if(viewModel.userPrefs.profileImageUrl.isNotNullOrEmpty()){
            downloadLogo()
        }

        binding.btnRetry.setOnClickListener {
            uiUtils.showProgress()
            viewModel.getKYCDetails("retry")
        }

        binding.card1.visibility = View.GONE
        binding.profile.visibility = View.VISIBLE


        viewModel.setUserState()

        /* observe and update ui state */
        viewModel.stateLiveData.observe(this, StateObserver())

        binding.kycLayout.setOnClickListener {
            uiUtils.showProgress()
            viewModel.getKYCDetails("detail")
        }

        viewModel.kycDetailData.observe(this, Observer {
            if(it.first.kycData.isNullOrEmpty()){
                uiUtils.showSnackbar("Something went wrong, please try again")
            }else{
                for (k in it.first.kycData!!){
                    if(k.verificationOverallType.equals("identity")){
                        userPrefs.identityType = k.verificationType.toString()
                        if(k.verificationStatus.equals("failed")){
                            if(k.verificationStatusReasonCode.equals("others")) {
                                userPrefs.identityRejectReason = k.verificationStatusReasonMessage?.replace("_"," ")?:""
                            }else{
                                userPrefs.identityRejectReason = k.verificationStatusReasonCode?.replace("_"," ")?:""
                            }
                        }else if(k.verificationStatus.equals("pending")){
                            userPrefs.identityRejectReason = "Document under verification"
                        }else{
                            userPrefs.identityRejectReason = ""
                        }
                    }else if(k.verificationOverallType.equals("pan")){
                      if(k.verificationStatus.equals("failed")){
                          if(k.verificationStatusReasonCode.equals("others")) {
                              userPrefs.panRejectReason = k.verificationStatusReasonMessage?.replace("_"," ")?:""
                          }else{
                              userPrefs.panRejectReason = k.verificationStatusReasonCode?.replace("_"," ")?:""
                          }
                      }else if(k.verificationStatus.equals("pending")){
                          userPrefs.panRejectReason = "Document under verification"
                      }else{
                          userPrefs.panRejectReason = ""
                      }

                    }else if(k.verificationOverallType.equals("trucking_business")){
                        userPrefs.businessType = k.verificationType.toString()
                        if(k.verificationStatus.equals("failed")){
                            if(k.verificationStatusReasonCode.equals("others")) {
                                userPrefs.rcRejectReason = k.verificationStatusReasonMessage?.replace("_"," ")?:""
                            }else{
                                userPrefs.rcRejectReason = k.verificationStatusReasonCode?.replace("_"," ")?:""
                            }
                        }else if(k.verificationStatus.equals("pending")){
                            userPrefs.rcRejectReason = "Document under verification"
                        }else{
                            userPrefs.rcRejectReason = ""
                        }

                    }else if(k.verificationOverallType.equals("address")){
                        if(k.verificationStatus.equals("failed")){
                            if(k.verificationStatusReasonCode.equals("others")) {
                                userPrefs.addressRejectReason = k.verificationStatusReasonMessage?.replace("_"," ")?:""
                            }else{
                                userPrefs.addressRejectReason = k.verificationStatusReasonCode?.replace("_"," ")?:""
                            }
                        }else if(k.verificationStatus.equals("pending")){
                            userPrefs.addressRejectReason = "Document under verification"
                        }else{
                            userPrefs.addressRejectReason = ""
                        }
                    }else if(k.verificationOverallType.equals("bank_details")){
                        if(k.verificationStatus.equals("failed")){
                            if(k.verificationStatusReasonCode.equals("others")) {
                                userPrefs.paymentRejectReason = k.verificationStatusReasonMessage?.replace("_"," ")?:""
                            }else{
                                userPrefs.paymentRejectReason = k.verificationStatusReasonCode?.replace("_"," ")?:""
                            }
                        }else if(k.verificationStatus.equals("pending")){
                            userPrefs.paymentRejectReason = "Document under verification"
                        }else{
                            userPrefs.paymentRejectReason = ""
                        }

                    }
                }
                if(it.second.equals("detail")) {
                    userPrefs.setPreviousScreen(this.javaClass.name)
                    navigationUtils.navigate(ProfileKYCDetailsActivity::class.java,true)
                }else if(it.second.equals("bank")){
                    userPrefs.setPreviousScreen(this.javaClass.name)
                    navigationUtils.navigate(BankDetailsActivity::class.java,true)
                }
                else{
                    if(!it.second.equals("noredirect")) {
                        userPrefs.setPreviousScreen(this.javaClass.name)
                        userPrefs.retryVerification = true
                        userPrefs.retryVerificationOnBack = false
                        val bundle = Bundle()
                        bundle.putInt(StepKey, 0)
                        navigationUtils.navigateKyc(this, true, bundle)
                    }
                }
            }
            uiUtils.hideProgress()
        })

        binding.profileLayout.setOnClickListener {
            userPrefs.setPreviousScreen(this.javaClass.name)
            navigationUtils.navigate(ProfileDetailsActivity::class.java)
        }

        binding.teamLayout.setOnClickListener {
            userPrefs.setPreviousScreen(this.javaClass.name)
            navigationUtils.navigate(ProfileDetailsActivity::class.java)
        }

        if (viewModel.userPrefs.isParent) {
            binding.teamLayout.visibility = View.VISIBLE
        } else {
            binding.teamLayout.visibility = View.GONE
        }

        binding.routeLayout.setOnClickListener {
            userPrefs.setPreviousScreen(this.javaClass.name)
            startActivity(userRoutesIntent(this))
        }

        binding.teamLayout.setOnClickListener {
            userPrefs.setPreviousScreen(this.javaClass.name)
            startActivity(teamMembersIntent(this))
        }
        binding.shareLayout.setOnClickListener {
            navigationUtils.navigate(ShareRateGetRewardsActivity::class.java)
        }
        binding.podLayout.setOnClickListener {
            if(binding.podaddress.visibility == View.VISIBLE){
               binding.podaddress.visibility = View.GONE
                binding.arrow7.rotation = 90F
            }else{
                binding.podaddress.visibility = View.VISIBLE
                binding.arrow7.rotation = 270F
            }
        }

        binding.helpLayout.setOnClickListener {
            userPrefs.setPreviousScreen(this.javaClass.name)
            navigationUtils.navigate(HelpSupportActivity::class.java)
        }

        binding.bankLayout.setOnClickListener {
            userPrefs.setPreviousScreen(this.javaClass.name)
            viewModel.getKYCDetails("bank")
            uiUtils.showProgress()
        }

        binding.helpLayout.setOnClickListener {
            userPrefs.setPreviousScreen(this.javaClass.name)
            navigationUtils.navigate(HelpSupportActivity::class.java)
        }

        binding.helpLayout.setOnClickListener {
            userPrefs.setPreviousScreen(this.javaClass.name)
            navigationUtils.navigate(HelpSupportActivity::class.java)
        }

        binding.earningsLayout.setOnClickListener {
            userPrefs.setPreviousScreen(this.javaClass.name)
            startActivity(consolidatedPageIntent(this))
        }

        // Download functionality removed - new API only supports upload
        // If download is needed, use listDocuments() to get file URLs

    }

    private fun setIssueCount(){
        if(userPrefs.verificationStatus.equals("failed")){
            if (userPrefs.noOfVerificationIssues.isNotNullOrEmpty()){
                if(userPrefs.isBankDetailsRejected && !userPrefs.noOfVerificationIssues.equals("0"))
                {
                    binding.issues.text = (userPrefs.noOfVerificationIssues.toInt()-1).toString()+" Issues"
                }else{
                    binding.issues.text = userPrefs.noOfVerificationIssues+" Issues"
                }
                binding.issues.visibility = View.VISIBLE
            }else{
                binding.issues.visibility = View.GONE
            }
            if(binding.issues.text.trim().equals("1 Issues")){
                binding.issues.text="1 Issue"
            }
            if(binding.issues.text.trim().equals("0 Issues")){
                binding.issues.visibility = View.GONE
                binding.btnRetry.visibility=View.GONE
            }
        }else{
            binding.issues.visibility = View.GONE
        }
        if(userPrefs.isBankDetailsRejected){
            binding.issuesPayment.text = "1 Issue"
            binding.issuesPayment.visibility=View.VISIBLE
        }else{
            binding.issuesPayment.visibility=View.GONE
        }
    }
    private fun setVerficationStatus() {
        if((userPrefs.isLoadBoardClient == false || userPrefs.isLoadBoardSupplier == false)) {
            binding.verifyBadge.visibility = View.VISIBLE
            binding.kycpendingLayout.visibility = View.GONE
            binding.ratingsLayout.visibility = View.GONE
            binding.kycfailedLayout.visibility = View.GONE
            binding.kycLayout.visibility = View.VISIBLE
        }else{
            if (userPrefs.verificationStatus.equals("pending")) {
                binding.verifyBadge.visibility = View.GONE
                binding.kycpendingLayout.visibility = View.VISIBLE
                binding.ratingsLayout.visibility = View.VISIBLE
                binding.kycfailedLayout.visibility = View.GONE
                binding.kycLayout.visibility = View.VISIBLE
            } else if (userPrefs.verificationStatus.equals("success")) {
                binding.verifyBadge.visibility = View.VISIBLE
                binding.kycpendingLayout.visibility = View.GONE
                binding.ratingsLayout.visibility = View.GONE
                binding.kycfailedLayout.visibility = View.GONE
                binding.kycLayout.visibility = View.VISIBLE
            } else if (userPrefs.verificationStatus.equals("failed")) {
                binding.verifyBadge.visibility = View.GONE
                binding.kycpendingLayout.visibility = View.GONE
                binding.ratingsLayout.visibility = View.VISIBLE
                binding.kycfailedLayout.visibility = View.VISIBLE
                binding.kycLayout.visibility = View.VISIBLE
            }
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
                navigationUtils.logout("Successfully logged out","fromUser")
            }, 3000)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
                    navigationUtils.logout("Successfully logged out","fromUser")
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

}