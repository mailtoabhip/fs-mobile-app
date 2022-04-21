package com.delhivery.axle.ui.accountdetails

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.databinding.ActivityAccountDetailsBinding
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
import com.delhivery.axle.ui.selectroute.activity.SelectRouteActivity
import com.delhivery.axle.ui.selectroute.activity.SelectRouteWelcomeIntentExtra
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.actionDone
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.raisedFocus
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*
import javax.inject.Inject
import android.text.method.LinkMovementMethod

import android.text.Spanned

import android.text.style.ClickableSpan

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView.BufferType
import com.delhivery.axle.R.string
import com.delhivery.axle.config.UrlConfig

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

        userPrefs.hasLoggedIn = false
        val firstString = getString(string.i_read_accept)
        val secondString = getString(string.label_privacy_policy)
        val thirdString = getString(string.and)
        val fourthString = getString(string.label_condition)
        val word1: Spannable = SpannableString(firstString)
        val word2: Spannable = SpannableString(secondString)
        val word3: Spannable = SpannableString(thirdString)
        val word4: Spannable = SpannableString(fourthString)
        word1.setSpan(ForegroundColorSpan(this.resources.getColor(R.color.heading_black)), 0, word1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.conditionPrivacy.setText(word1)

        val span2: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                when (contactUtils.openURL("${UrlConfig.DashboardUrl.url()}/#/paymentterms")) {
                    false -> uiUtils.showSnackbar("Could not open url")
                }
            }
        }
        word2.setSpan(span2, 0, word2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.conditionPrivacy.append(word2)
        word3.setSpan(ForegroundColorSpan(this.resources.getColor(R.color.heading_black)), 0, word3.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.conditionPrivacy.append(word3)
        val span4: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                //to be replaced with condition url
                when (contactUtils.openURL("${UrlConfig.DashboardUrl.url()}/#/paymentterms")) {
                    false -> uiUtils.showSnackbar("Could not open url")
                }
            }
        }
        word4.setSpan(span4, 0, word4.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        binding.conditionPrivacy.append(word4)
        binding.conditionPrivacy.movementMethod = LinkMovementMethod.getInstance()

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
                viewModel.createAccount(UpdateUserRequest(businessName = viewModel.business_name.value?.trim(),
                        userName = viewModel.username.value?.trim(),
                        referralCode = viewModel.referral_code.value?.trim(),
                        receiveWhatsappNotifications = viewModel.whatsapp.value,
                isLocationEnabled = viewModel.locationOption.value))
            }
    }

    fun checkEnable() {
        binding.btnCreateAccount.isEnabled = (viewModel.business_name.value?.trim().isNotNullOrEmpty() && viewModel.username.value?.trim().isNotNullOrEmpty()
                && viewModel.termsCheck.value == true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
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
                    AuthenticationUIState.OTP -> {uiUtils.hideDelhiveryProgress()
                    uiUtils.showSnackbar("Something went wrong, Please try again")
                    }
                    AuthenticationUIState.Password ->{}
                    AuthenticationUIState.LoginProgress -> {
                        uiUtils.showDelhiveryProgress(
                                "Getting details", "This usually takes few seconds to load. please be patient.",
                                "This usually takes few seconds to load. please be patient."
                        )
                    }
                    /* Login success, No user routes found - select route activity */
                   /* AuthenticationUIState.SelectRoute -> {
                        userPrefs.firstRoute = true
                        uiUtils.showProgress()
                        val bundle = Bundle()
                        bundle.putBoolean(SelectRouteWelcomeIntentExtra, true)

                       val intent = Intent(this@AccountDetailsActivity, SelectRouteActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        navigationUtils.navigateForActivityResult(
                            intent,
                                requestCode = REQCODE_ADD_ROUTES, extras = bundle, finishAfter = true
                        )
                    }*/
                    /* Login success, user routes found - navigate to load requests */
                    AuthenticationUIState.LoadRequest -> {
                        uiUtils.showProgress()
                        val intent = Intent(this@AccountDetailsActivity, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    /* otp verified, account set up needed */
                   /* AuthenticationUIState.AccountRole -> {
                        uiUtils.showProgress("Loading...")
                        navigationUtils.navigate(AccountRoleActivity::class.java, false)
                    }
                    AuthenticationUIState.AccountAction -> {
                        uiUtils.showProgress("Loading...")
                        navigationUtils.navigate(AccountActionActivity::class.java, false)
                    }*/
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

