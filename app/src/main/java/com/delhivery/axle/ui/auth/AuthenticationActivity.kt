package com.delhivery.axle.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.databinding.ActivityAuthenticationBinding
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_ID
import com.delhivery.axle.receiver.OTPReceiverInterface
import com.delhivery.axle.ui.auth.AuthenticationUIError.*
import com.delhivery.axle.ui.auth.AuthenticationUIState.Disabled
import com.delhivery.axle.ui.auth.AuthenticationUIState.LoadRequest
import com.delhivery.axle.ui.auth.AuthenticationUIState.LoginProgress
import com.delhivery.axle.ui.auth.AuthenticationUIState.OTP
import com.delhivery.axle.ui.auth.AuthenticationUIState.PhoneNo
import com.delhivery.axle.ui.auth.AuthenticationUIState.SelectRoute
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.custom.DelhiveryOTPViewInterface
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.selectroute.activity.SelectRouteWelcomeIntentExtra
import com.delhivery.axle.ui.selectroute.activity.selectRouteIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.Config.AxleOnboardingEmail
import com.delhivery.axle.utils.extensions.actionDone
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.raisedFocus
import com.delhivery.axle.utils.extensions.safeDispose
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Authentication screen
 */
class AuthenticationActivity : BaseActivity<ActivityAuthenticationBinding, AuthenticationViewModel>(),
    DelhiveryOTPViewInterface, OTPReceiverInterface {

  override fun getViewModelClass() = AuthenticationViewModel::class.java

  override fun layoutId() = R.layout.activity_authentication

  override fun requireConnection() = true

  /* dismiss timeout disposable */
  private var timeoutDisposable: Disposable? = null

  @Inject lateinit var userPrefs: UserPrefs

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    notificationId = intent?.extras?.getString(ARGS_NOTIFICATION_ID) ?: ""
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* observe and update ui state */
    viewModel.stateLiveData.observe(this, StateObserver())

    /* observe errors and update ui */
    viewModel.errorLiveData.observe(this, ErrorObserver())

    /* phone no edit button setup */
    binding.editPhoneNo.apply {
      raisedFocus()
      lengthAction(10) {
        // Capture event
        analyticsUtil.trackEvent(
                EVENT_OTP_SEND,
                mutableListOf(PROPERTY_MOBILE_NUMBER_ENTERED),
                mutableListOf(binding.editPhoneNo.text.toString())
        )
        viewModel.sendOTP()
      }
      actionDone {
        // Capture event
        analyticsUtil.trackEvent(
                EVENT_OTP_SEND,
                mutableListOf(PROPERTY_MOBILE_NUMBER_ENTERED),
                mutableListOf(binding.editPhoneNo.text.toString())
        )
        viewModel.sendOTP()
      }
    }

    binding.loginUsingPassword.setOnClickListener{
      viewModel.loginUsingPassword("offroll@gmail.com","Off@12345678")
    }


    viewModel.otpStatusLiveData.observe(this, Observer {
      if (it == true) {
        timeoutDisposable = Observable.interval(0L, 1L, TimeUnit.SECONDS)
            .onBackground()
            .subscribe {
              val timeLeft = 15L - it
              if (timeLeft > 0) {
                binding.btnResendOtp.text = "${getString(string.label_resend_otp)}($timeLeft)"
                binding.btnResendOtp.isEnabled = false
              } else if (timeLeft == 0L) {
                binding.btnResendOtp.text = getString(string.label_resend_otp)
                binding.btnResendOtp.isEnabled = true
              } else {
                viewModel.otpStatusLiveData.postValue(false)
              }
            }
      } else {
        timeoutDisposable.safeDispose()
      }
    })

    /* otp view interface */
    binding.otpView.otpViewInterface = this

    /* Initiate state */
    viewModel.state = PhoneNo

    binding.btnResendOtp.setOnClickListener {
      // Capture event
      analyticsUtil.trackEvent(
              EVENT_OTP_RESEND,
              mutableListOf(PROPERTY_MOBILE_NUMBER_ENTERED),
              mutableListOf(viewModel.phoneNo)
      )
      viewModel.otpSendCount +=1
      viewModel.sendOTP()
    }

    binding.btnSendOtp.setOnClickListener {
      // Capture event
      analyticsUtil.trackEvent(
              EVENT_OTP_SEND,
              mutableListOf(PROPERTY_MOBILE_NUMBER_ENTERED),
              mutableListOf(viewModel.phoneNo)
      )
      viewModel.sendOTP()
    }

    if (notificationId.isNotEmpty()) {
      markNotificationRead()
    }
  }

  override fun markNotificationRead() {
    super.markNotificationRead()
    viewModel.markNotificationRead(notificationId)
  }

  override fun onBackPressed() {
    when (binding.state) {
      PhoneNo -> {
        super.onBackPressed()
      }
      OTP -> viewModel.state = PhoneNo
      LoginProgress -> {/* do nothing when loading */
      }
      else -> {
      }
    }
  }

  override fun otpSubmitted(otp: CharArray) {
    viewModel.verifyOTP(otp)
  }

  override fun otpFound(otp: String) {
    otpSubmitted(otp.toCharArray())
  }

  /**
   * [AuthenticationUIState] observer
   */
  inner class StateObserver : Observer<AuthenticationUIState> {
    override fun onChanged(it: AuthenticationUIState?) {
      it?.let { state ->
        binding.state = state
        when (state) {
          PhoneNo -> {
            //hide keyboard
            uiUtils.toggleKeyboard()
          }
          OTP -> {
            uiUtils.hideDelhiveryProgress()
            //show keyboard and clear otp
            uiUtils.toggleKeyboard(false)
            binding.otpView.clear(focusedIndex = 0, animate = false)

            /* show masked phone no */
            viewModel.phoneNo.let {
              if (it.length > 2) {
                binding.textOtpSentToPhoneNo.text =
                  getString(string.msg_otp_sent_to_phone_no, it.substring(it.length - 2))
              }
            }
          }
          LoginProgress -> {
            //hide keyboard show progress view
            uiUtils.showDelhiveryProgress(
                title = "Logging you in..",
                message = "This usually takes few seconds to load. please be patient.",
                proTip = "Some tip regarding how to bid, or whats to be considered while bidding. "
            )
          }
          /* Login success, No user routes found - select route activity */
          SelectRoute -> {
            userPrefs.firstRoute = true
            // Capture event
            analyticsUtil.trackEvent(
                    EVENT_OTP_VERIFIED,
                    mutableListOf(PROPERTY_MOBILE_NUMBER_ENTERED , PROPERTY_USER_ID , PROPERTY_OTP_SEND_COUNT),
                    mutableListOf(viewModel.phoneNo , userPrefs.userId() , viewModel.otpSendCount.toString())
            )
            uiUtils.hideDelhiveryProgress()
            val bundle = Bundle()
            bundle.putBoolean(SelectRouteWelcomeIntentExtra, true)
            navigationUtils.navigateForActivityResult(
                intent = selectRouteIntent(this@AuthenticationActivity),
                requestCode = REQCODE_ADD_ROUTES, extras = bundle
            )
          }
          /* Login success, user routes found - navigate to load requests */
          LoadRequest -> {
            // Capture event
            analyticsUtil.trackEvent(
                    EVENT_OTP_VERIFIED,
                    mutableListOf(PROPERTY_MOBILE_NUMBER_ENTERED , PROPERTY_USER_ID , PROPERTY_OTP_SEND_COUNT),
                    mutableListOf(viewModel.phoneNo , userPrefs.userId() , viewModel.otpSendCount.toString())
            )
            uiUtils.hideDelhiveryProgress()
            navigationUtils.navigate(HomeActivity::class.java, true)
          }
          Disabled -> {
            uiUtils.hideDelhiveryProgress()
            dialogUtils.showBasicConfirmDialog(string.title_dialog_supplier_disabled,
                string.msg_dialog_supplier_disabled,
                getString(string.label_call_us), getString(string.label_mail_us),
                { callHelpline() }, { sendMail() }
            )
          }
        }
      }
    }
  }

  /**
   * [AuthenticationUIError] observer
   */
  inner class ErrorObserver : Observer<Pair<AuthenticationUIError, String?>> {
    override fun onChanged(it: Pair<AuthenticationUIError, String?>?) {
      it?.let { error ->
        /* show error message in snackbar if not null || empty */
        if (error.second.isNotNullOrEmpty()) {
          uiUtils.showSnackbar(error.second!!)
        }
        /* handle each error state */
        when (error.first) {
          InvalidPhoneNo -> {   //Invalid phone number functionality
            dialogUtils.showBasicConfirmDialog(string.title_dialog_invalid_num,
                string.msg_dialog_invalid_num,
                getString(string.label_call_us), getString(string.label_mail_us),
                { callHelpline() }, { sendMail(AxleOnboardingEmail) }
            )
            binding.editPhoneNo.errorVibrate()
          }
          InvalidOTP -> {   //Invalid OTP clear fields
            binding.otpView.clear()
          }
          InvalidPassword -> {   //Invalid password clear fields
         //   binding.otpView.clear()
          }
          None -> {/* nothing */
          }
        }
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
      REQCODE_ADD_ROUTES -> navigationUtils.navigate(
          HomeActivity::class.java, true
      )
    }
  }
}