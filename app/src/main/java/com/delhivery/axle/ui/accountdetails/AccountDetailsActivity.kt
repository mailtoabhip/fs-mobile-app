package com.delhivery.axle.ui.accountdetails

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.databinding.ActivityAccountDetailsBinding
import com.delhivery.axle.databinding.ActivityVerifyPanBinding
import com.delhivery.axle.ui.accountaction.AccountActionActivity
import com.delhivery.axle.ui.accountaction.AccountType
import com.delhivery.axle.ui.accountaction.IntentExtraModeTypeKey
import com.delhivery.axle.ui.accountaction.IntentExtraRoleTypeKey
import com.delhivery.axle.ui.accountrole.AccountRole
import com.delhivery.axle.ui.accountrole.AccountRoleActivity
import com.delhivery.axle.ui.accountrole.accountRoleIntent
import com.delhivery.axle.ui.auth.AuthenticationActivity
import com.delhivery.axle.ui.auth.AuthenticationUIState

import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.base.BaseLocationActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsActivity
import com.delhivery.axle.ui.home.activity.transactionlist.transactionsIntent
import com.delhivery.axle.ui.kyc.gst.GstVerificationActivity
import com.delhivery.axle.ui.selectroute.activity.SelectRouteWelcomeIntentExtra
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.actionDone
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.raisedFocus
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*
import javax.inject.Inject


class AccountDetailsActivity :BaseLocationActivity<ActivityAccountDetailsBinding, AccountDetailsViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#ededff")
    }

    override fun getViewModelClass() = AccountDetailsViewModel::class.java

    override fun layoutId() = R.layout.activity_account_details

    override fun requireConnection() = false

    @Inject lateinit var userPrefs:UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        onLocationButtonClicked()

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
                viewModel.createAccount(UpdateUserRequest(business_name = viewModel.business_name.value,
                        user_name = viewModel.username.value,
                        referral_code = viewModel.referral_code.value,
                        receive_whatsapp_notifications = viewModel.whatsapp.value,
                is_location_enabled = viewModel.locationOption.value))
            }
    }

    fun checkEnable() {
        binding.btnCreateAccount.isEnabled = (viewModel.business_name.value.isNotNullOrEmpty() && viewModel.username.value.isNotNullOrEmpty()
                && viewModel.termsCheck.value == true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.updateUserStatusLiveData.postValue(null)
        startActivity(accountRoleIntent(this, viewModel.mode, viewModel.role))
    }

    override fun updateLocationFlowState(flowState: LocationFlowState) {
          if(flowState.equals(LocationFlowState.PermissionGranted)){
              viewModel.locationOption.value = true
          }
    }

    inner class StateObserver : Observer<AuthenticationUIState> {
        override fun onChanged(it: AuthenticationUIState?) {
            it?.let { state ->
                when (state) {
                    AuthenticationUIState.PhoneNo -> { }
                    AuthenticationUIState.OTP -> { }
                    AuthenticationUIState.Password ->{}
                    AuthenticationUIState.LoginProgress -> {
                        uiUtils.showDelhiveryProgress(
                                "Getting details", "This usually takes few seconds to load. please be patient.",
                                "This usually takes few seconds to load. please be patient."
                        )
                    }
                    /* Login success, No user routes found - select route activity */
                    AuthenticationUIState.SelectRoute -> {
                        userPrefs.firstRoute = true
                        uiUtils.showProgress()
                        val bundle = Bundle()
                        bundle.putBoolean(SelectRouteWelcomeIntentExtra, true)
                        navigationUtils.navigateForActivityResult(
                                intent = selectRouteIntent(this@AccountDetailsActivity),
                                requestCode = REQCODE_ADD_ROUTES, extras = bundle, finishAfter = true
                        )
                    }
                    /* Login success, user routes found - navigate to load requests */
                    AuthenticationUIState.LoadRequest -> {
                        uiUtils.showProgress()
                        navigationUtils.navigate(HomeActivity::class.java, true)
                    }
                    /* otp verified, account set up needed */
                    AuthenticationUIState.AccountRole -> {
                        uiUtils.showProgress("Loading...")
                        navigationUtils.navigate(AccountRoleActivity::class.java, false)
                    }
                    AuthenticationUIState.AccountAction -> {
                        uiUtils.showProgress("Loading...")
                        navigationUtils.navigate(AccountActionActivity::class.java, false)
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

