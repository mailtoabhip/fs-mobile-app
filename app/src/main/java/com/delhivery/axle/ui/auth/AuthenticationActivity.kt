package com.delhivery.axle.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.databinding.ActivityAuthenticationBinding
import com.delhivery.axle.receiver.OTPReceiverInterface
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidOTP
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidPhoneNo
import com.delhivery.axle.ui.auth.AuthenticationUIError.None
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
import com.delhivery.axle.utils.Config.AxleOnboardingEmail
import com.delhivery.axle.utils.ContactUtils
import com.delhivery.axle.utils.EVENT_OTP_RESEND
import com.delhivery.axle.utils.EVENT_OTP_SEND
import com.delhivery.axle.utils.EVENT_OTP_VERIFIED
import com.delhivery.axle.utils.REQCODE_ADD_ROUTES
import com.delhivery.axle.utils.extensions.actionDone
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.raisedFocus
import com.delhivery.axle.utils.extensions.safeDispose
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthenticationActivity : BaseActivity<ActivityAuthenticationBinding, AuthenticationViewModel>(),
    DelhiveryOTPViewInterface, OTPReceiverInterface {

  override fun getViewModelClass() = AuthenticationViewModel::class.java

  override fun layoutId() = R.layout.activity_authentication

  override fun requireConnection() = true

  @Inject lateinit var contactUtils: ContactUtils

  /* dismiss timeout disposable */
  private var timeoutDisposable: Disposable? = null

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* observe and update ui state */
    viewModel.stateLiveData.observe(this, StateObserver())

    /* obvserve errors and update ui */
    viewModel.errorLiveData.observe(this, ErrorObserver())

    /* phone no edit button setup */
    binding.editPhoneNo.apply {
      raisedFocus()
      lengthAction(10) {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_OTP_SEND,
            mutableListOf(),
            mutableListOf()
        )
        viewModel.sendOTP()
      }
      actionDone {
        // Capture event
        analyticsUtil.trackEvent(
            EVENT_OTP_SEND,
            mutableListOf(),
            mutableListOf()
        )
        viewModel.sendOTP()
      }
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
          mutableListOf(),
          mutableListOf()
      )
      viewModel.sendOTP()
    }

    binding.btnSendOtp.setOnClickListener {
      // Capture event
      analyticsUtil.trackEvent(
          EVENT_OTP_SEND,
          mutableListOf(),
          mutableListOf()
      )
      viewModel.sendOTP()
    }
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
            // Capture event
            analyticsUtil.trackEvent(
                EVENT_OTP_VERIFIED,
                mutableListOf(),
                mutableListOf()
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
                mutableListOf(),
                mutableListOf()
            )
            uiUtils.hideDelhiveryProgress()
            navigationUtils.navigate(HomeActivity::class.java, true)
          }
          Disabled -> {
            uiUtils.hideDelhiveryProgress()
            dialogUtils.showBasicConfirmDialog(
                string.title_dialog_supplier_disabled,
                string.msg_dialog_supplier_disabled,
                "RETRY", "MAIL US",
                {
                  it.dismiss()
                  navigationUtils.logout("Supplier disabled")
                },
                {
                  when (contactUtils.openGmail(receiver = "axle-support@delhivery.com")) {
                    false -> {
                      it.dismiss()
                      uiUtils.showToast("Sorry...You don't have any mail app installed")
                    }
                    else -> {
                    }
                  }
                }
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
            dialogUtils.showBasicConfirmDialog(
                string.title_dialog_invalid_num,
                string.msg_dialog_invalid_num,
                "RETRY", "MAIL US",
                {
                  it.dismiss()
                },
                {
                  when (contactUtils.openGmail(receiver = AxleOnboardingEmail)) {
                    false -> {
                      it.dismiss()
                      uiUtils.showToast("Sorry...You don't have any mail app installed")
                    }
                    else -> {
                    }
                  }
                }
            )
            binding.editPhoneNo.errorVibrate()
          }
          InvalidOTP -> {   //Invalid OTP clear fields
            binding.otpView.clear()
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