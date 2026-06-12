package com.delhivery.axle.ui.accountdetails

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityAccountDetailsBinding
import com.delhivery.axle.ui.auth.AuthenticationUIState
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.utils.EVENT_SUBMITTED_ABOUT_YOURSELF
import com.delhivery.axle.utils.EVENT_VIEW_ABOUT_YOURSELF
import com.delhivery.axle.utils.PROPERTY_BUSINESS_NAME
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_TNC
import com.delhivery.axle.utils.PROPERTY_TTL
import com.delhivery.axle.utils.PROPERTY_USERNAME
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.focusClick
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

class AccountDetailsActivity :BaseActivity<ActivityAccountDetailsBinding, AccountDetailsViewModel>() {

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
    
        /* Handle window insets for edge-to-edge display (API 35+) */
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
          WindowInsetsUtils.applyTopSystemWindowInsets(binding.parentCl)
        }
        binding.tvFirstName.text = HtmlCompat.fromHtml(
            getString(R.string.str_first_name_required),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        startTime = System.currentTimeMillis()
        trackEvent()
        //onLocationButtonClicked()
        /* observe and update ui state */
        viewModel.stateLiveData.observe(this, StateObserver())

        viewModel.firstName.observe(this, Observer {
            checkEnable()
        })

        viewModel.commConsent.observe(this, Observer {
            checkEnable()
        })

        binding.btnCreateAccount.setOnClickListener {
            endTime = System.currentTimeMillis()
            val ttl = endTime - startTime
            analyticsUtil.moEngageTrackEvent(
                EVENT_SUBMITTED_ABOUT_YOURSELF,
                mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_USERNAME,
                    PROPERTY_BUSINESS_NAME, PROPERTY_TNC, PROPERTY_TTL),
                mutableListOf(userPrefs.userId(), userPrefs.phoneNumber ?: "dummy",
                    viewModel.firstName.value?.trim() ?: "dummy name",
                    viewModel.lastName.value?.trim() ?: "dummy business",
                    viewModel.commConsent.value.toString(), ttl.toString())
            )
            viewModel.createAccount()
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Profile Details"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.editName.focusClick()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

    fun checkEnable() {
        binding.btnCreateAccount.isEnabled = (
                viewModel.firstName.value?.trim().isNotNullOrEmpty()
                        //&& viewModel.lastName.value?.trim().isNotNullOrEmpty()
                        && viewModel.commConsent.value == true)
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
                    AuthenticationUIState.OTP -> {
                        uiUtils.showSnackbar(viewModel.errorAccountCreate?:"Something went wrong, Please try again")
                    }
                    AuthenticationUIState.LoginProgress -> {
                        // Loading UI removed - API calls continue normally
                    }

                    AuthenticationUIState.HomePage -> {
                      //  uiUtils.showProgress()
                        val intent = Intent(this@AccountDetailsActivity, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    AuthenticationUIState.AccountDetails -> {
                        //uiUtils.showProgress("Loading...")
                        val intent = Intent(this@AccountDetailsActivity, AccountDetailsActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                       // navigationUtils.navigate(AccountDetailsActivity::class.java, false)
                    }
                    AuthenticationUIState.Disabled -> {
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

