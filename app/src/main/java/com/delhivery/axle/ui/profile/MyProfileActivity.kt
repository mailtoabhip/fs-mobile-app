package com.delhivery.axle.ui.profile

import android.Manifest
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Observer
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.delhivery.axle.BuildConfig
import com.delhivery.axle.R
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.databinding.ActivityMyProfileBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.ledger.consolidatedPageIntent
import com.delhivery.axle.ui.profile.kycdetails.ProfileKYCDetailsActivity
import com.delhivery.axle.ui.profile.profiledetails.ProfileDetailsActivity
import com.delhivery.axle.ui.team.teamMembersIntent
import com.delhivery.axle.ui.userroutes.userRoutesIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import java.io.File
import javax.inject.Inject


class MyProfileActivity  : BaseActivity<ActivityMyProfileBinding, HomeProfileViewModel>(), AWSUtils.AWSProgressInterface  {
    init {
        StatusBarColor = Color.parseColor("#ffffff")
    }

    override fun getViewModelClass() = HomeProfileViewModel::class.java

    override fun layoutId() = R.layout.activity_my_profile

    override fun requireConnection() = false

    @Inject lateinit var awsUtils: AWSUtils

    @Inject lateinit var userPrefs:UserPrefs

    @Inject
    lateinit var bitmapUtils: BitmapUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* setup toolbar */
        setSupportActionBar(binding.toolbar)
        title = "My Profile"

        if(userPrefs.companyName.isNotEmpty()) {
            binding.profile.text = userPrefs.companyName[0].toUpperCase().toString()
        }
        binding.appversion.text = "App version ${BuildConfig.VERSION_NAME}"

        setVerficationStatus()

        binding.logoutLayout.setOnClickListener {
            confirmLogout()
        }

        binding.startKyc.setOnClickListener {
            val bundle = Bundle()
            if(userPrefs.pancard.isEmpty()){
                bundle.putInt(StepKey, 0)
            }else if(!userPrefs.isAadhaartVerfied && userPrefs.pancard.toCharArray().get(3).toLowerCase().equals("p")){
                bundle.putInt(StepKey, 1)
            }else if(!userPrefs.isGstVerfied && !userPrefs.pancard.toCharArray().get(3).toLowerCase().equals("p")){
                bundle.putInt(StepKey, 1)
            }else if(userPrefs.businessAddress.isEmpty()){
                bundle.putInt(StepKey, 2)
            }
            navigationUtils.navigateKyc(this,false,bundle)
        }

        if(viewModel.userPrefs.profileImageUrl.isNotNullOrEmpty()){
            downloadLogo()
        }

        binding.card1.visibility = View.GONE
        binding.profile.visibility = View.VISIBLE


        viewModel.setUserState()

        /* observe and update ui state */
        viewModel.stateLiveData.observe(this, StateObserver())

        binding.kycLayout.setOnClickListener {
            navigationUtils.navigate(ProfileKYCDetailsActivity::class.java)
        }

        binding.profileLayout.setOnClickListener {
            navigationUtils.navigate(ProfileDetailsActivity::class.java)
        }

        binding.teamLayout.setOnClickListener {
            navigationUtils.navigate(ProfileDetailsActivity::class.java)
        }


        if (viewModel.userPrefs.isParent) {
            binding.teamLayout.visibility = View.VISIBLE
        } else {
            binding.teamLayout.visibility = View.GONE
        }

        binding.routeLayout.setOnClickListener {
                startActivity(userRoutesIntent(this))
        }

        binding.teamLayout.setOnClickListener {
               startActivity(teamMembersIntent(this))
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
            navigationUtils.navigate(HelpSupportActivity::class.java)
        }

        binding.bankLayout.setOnClickListener {
            navigationUtils.navigate(BankDetailsActivity::class.java)
        }

        binding.helpLayout.setOnClickListener {
            navigationUtils.navigate(HelpSupportActivity::class.java)
        }

        binding.helpLayout.setOnClickListener {
            navigationUtils.navigate(HelpSupportActivity::class.java)
        }

        binding.earningsLayout.setOnClickListener {
                startActivity(consolidatedPageIntent(this))
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

    private fun setVerficationStatus() {
        if(userPrefs.accountSetup) {
            if (userPrefs.verificationStatus.equals("pending")) {
                binding.verifyBadge.visibility = View.GONE
                binding.kycpendingLayout.visibility = View.VISIBLE
                binding.ratingsLayout.visibility = View.VISIBLE
                binding.kycfailedLayout.visibility = View.GONE
                binding.kycLayout.visibility = View.GONE
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
        }else{
            binding.verifyBadge.visibility = View.VISIBLE
            binding.kycpendingLayout.visibility = View.GONE
            binding.ratingsLayout.visibility = View.GONE
            binding.kycfailedLayout.visibility = View.GONE
            binding.kycLayout.visibility = View.VISIBLE
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    /**
     * ProfileUIState
     */
    inner class StateObserver : Observer<ProfileUIState> {
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
                    analyticsUtil.trackEvent(
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
        val file = getFile()
        if (file != null) {
            viewModel.getDownloadDelegationToken(viewModel.userPrefs.profileImageUrl, file)
        } else {
            uiUtils.showSnackbar("Can't process image")
        }
    }

    private fun getFile(): File? {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val basePath = "$storageDir/" + System.currentTimeMillis()
        return File(basePath + "_profile.jpg")
    }

    override fun onResume() {
        super.onResume()
        if(viewModel.userPrefs.profileImageUrl.isNotNullOrEmpty()){
            downloadLogo()
        }
    }

    override fun onAWSSuccess(
            path: String
    ) {
        binding.card1.visibility = View.VISIBLE
        binding.profile.visibility = View.GONE
        uiUtils.hideProgress()
          viewModel.imageUrl = path
        loadImage(viewModel.imagePath, binding.profilepic)
    }

    override fun onAWSFailure() {
        uiUtils.hideProgress()
        uiUtils.showSnackbar("Image processing failed, please try again.")
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

}