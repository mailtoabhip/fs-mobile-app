package com.delhivery.axle.ui.accountdetails

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle

import com.delhivery.axle.R
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.databinding.ActivityAccountDetailsBinding
import com.delhivery.axle.ui.accountaction.AccountType
import com.delhivery.axle.ui.accountaction.IntentExtraModeTypeKey
import com.delhivery.axle.ui.accountaction.IntentExtraRoleTypeKey
import com.delhivery.axle.ui.accountrole.AccountRole
import com.delhivery.axle.ui.accountrole.AccountRoleActivity
import com.delhivery.axle.ui.auth.AuthenticationUIState

import com.delhivery.axle.ui.base.BaseLocationActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject
import android.text.method.LinkMovementMethod

import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import com.delhivery.axle.utils.extensions.focusClick
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.delhivery.axle.utils.WindowInsetsUtils

class AccountDetailsActivity :BaseLocationActivity<ActivityAccountDetailsBinding, AccountDetailsViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#ededff")
    }

    override fun getViewModelClass() = AccountDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_account_details

    override fun requireConnection() = false

    @Inject lateinit var userPrefs:UserPrefs

    var startTime: Long = 0
    var endTime: Long = 0
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("AccountDetailsActivity_SetupTime")
        activitySetupTrace?.start()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        startTime = System.currentTimeMillis()

        trackEvent()

        onLocationButtonClicked()

        userPrefs.hasLoggedIn = false
        binding.privacyText.movementMethod = LinkMovementMethod.getInstance()
        binding.checkWhatsapp.isChecked=true
        viewModel.whatsapp.value = true
        /* observe and update ui state */
        viewModel.stateLiveData.observe(this, StateObserver())

        if (intent != null || intent.hasExtra(IntentExtraModeTypeKey)) {
            viewModel.mode = AccountType.byTypeId(intent.getIntExtra(IntentExtraModeTypeKey, AccountType.Unknown.typeId))
        }

        if (intent != null || intent.hasExtra(IntentExtraRoleTypeKey)) {
            viewModel.role = AccountRole.byTypeId(intent.getIntExtra(IntentExtraRoleTypeKey, AccountRole.Unknown.typeId))
        }

            viewModel.createUserStatusLiveData.observe(this, Observer {
            })

            viewModel.business_name.observe(this, Observer {
                checkEnable()
            })

            viewModel.username.observe(this, Observer {
                checkEnable()
            })

            binding.checkTerms.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.termsCheck.value = isChecked
                checkEnable()
            }

            binding.checkWhatsapp.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.whatsapp.value = isChecked
            }

            binding.btnCreateAccount.setOnClickListener {
                viewModel.progressLiveData.postValue(true)
                val username = viewModel.username.value?.trim()
                val businessName = viewModel.business_name.value?.trim()
                endTime = System.currentTimeMillis()
                val ttl = endTime - startTime
                analyticsUtil.moEngageTrackEvent(
                    EVENT_SUBMITTED_ABOUT_YOURSELF,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_USERNAME,
                        PROPERTY_BUSINESS_NAME, PROPERTY_TNC, PROPERTY_TTL),
                    mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy", username?:"dummy name",
                        businessName?:"dummy business", viewModel.termsCheck.value.toString(), ttl.toString())
                )
                viewModel.createAccount(UpdateUserRequest(businessName = businessName,
                        userName = username,
                        referralCode = viewModel.referral_code.value?.trim(),
                        receiveWhatsappNotifications = viewModel.whatsapp.value,
                isLocationEnabled = viewModel.locationOption.value))
            }
        binding.editName.focusClick()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

    fun checkEnable() {
        binding.btnCreateAccount.isEnabled = (viewModel.business_name.value?.trim().isNotNullOrEmpty() && viewModel.username.value?.trim().isNotNullOrEmpty()
                && viewModel.termsCheck.value == true)
    }

  /*  override fun onBackPressed() {
        super.onBackPressed()
    }*/

    override fun updateLocationFlowState(flowState: LocationFlowState) {
          if(flowState.equals(LocationFlowState.PermissionGranted)){
              viewModel.locationOption.value = true
          }
    }

    fun trackEvent(){
        analyticsUtil.moEngageTrackEvent(
            EVENT_VIEW_ABOUT_YOURSELF,
            mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO),
            mutableListOf(userPrefs.userId(), userPrefs.phoneNumber?:"dummy")
        )
    }

    inner class StateObserver : Observer<AuthenticationUIState?> {
        override fun onChanged(it: AuthenticationUIState?) {
            it?.let { state ->
                when (state) {
                    AuthenticationUIState.PhoneNo -> { }
                    AuthenticationUIState.OTP -> {uiUtils.hideDelhiveryProgress()
                    uiUtils.showSnackbar(viewModel.errorAccountCreate?:"Something went wrong, Please try again")
                    }
                    AuthenticationUIState.Password ->{}
                    AuthenticationUIState.LoginProgress -> {
                        uiUtils.showDelhiveryProgress(
                                "Getting details", "This usually takes few seconds to load. please be patient.",
                                "This usually takes few seconds to load. please be patient."
                        )
                    }

                    AuthenticationUIState.LoadRequest -> {
                      //  uiUtils.showProgress()
                        val intent = Intent(this@AccountDetailsActivity, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    AuthenticationUIState.AccountDetails -> {
                        uiUtils.showProgress("Loading...")
                        navigationUtils.navigate(AccountDetailsActivity::class.java, false)
                    }
                    AuthenticationUIState.Disabled -> {
                        uiUtils.hideDelhiveryProgress()
                        dialogUtils.showBasicConfirmDialog(R.string.title_dialog_supplier_disabled,
                                R.string.msg_dialog_supplier_disabled,
                                getString(R.string.label_call_us), getString(R.string.label_mail_us),
                                { callHelpline() }, { sendMail() }
                        )
                    }
                }
            }
        }
    }

}

/**
 * Get [AccountDetailActivity]
 */
fun accountDetailIntent(
        context: Context,
        mode: AccountType?,
        role: AccountRole?
) = Intent(context, AccountDetailsActivity::class.java).apply {
    if(role?.typeId!=-1) putExtra(IntentExtraRoleTypeKey, role?.typeId)
    if(mode?.typeId!=-1)putExtra(IntentExtraModeTypeKey, mode?.typeId)
}

fun accountRoleIntent(
        context: Context,
        mode: AccountType?,
        role: AccountRole?
) = Intent(context, AccountRoleActivity::class.java).apply {
    if(role?.typeId!=-1)putExtra(IntentExtraRoleTypeKey, role?.typeId)
    if(mode?.typeId!=-1) putExtra(IntentExtraModeTypeKey, mode?.typeId)
}

