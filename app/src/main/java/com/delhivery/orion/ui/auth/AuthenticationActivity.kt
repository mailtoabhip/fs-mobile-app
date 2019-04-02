package com.delhivery.orion.ui.auth

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.IntentFilter
import android.os.Bundle
import com.delhivery.orion.R
import com.delhivery.orion.databinding.ActivityAuthenticationBinding
import com.delhivery.orion.receiver.OTPReceiver
import com.delhivery.orion.receiver.OTPReceiverInterface
import com.delhivery.orion.receiver.OTP_INTENT_FILTER
import com.delhivery.orion.ui.auth.AuthenticationUIError.InvalidOTP
import com.delhivery.orion.ui.auth.AuthenticationUIError.InvalidPhoneNo
import com.delhivery.orion.ui.auth.AuthenticationUIError.None
import com.delhivery.orion.ui.auth.AuthenticationUIState.LoadRequest
import com.delhivery.orion.ui.auth.AuthenticationUIState.LoginProgress
import com.delhivery.orion.ui.auth.AuthenticationUIState.OTP
import com.delhivery.orion.ui.auth.AuthenticationUIState.PhoneNo
import com.delhivery.orion.ui.auth.AuthenticationUIState.SelectRoute
import com.delhivery.orion.ui.base.BaseActivity
import com.delhivery.orion.ui.custom.DelhiveryOTPViewInterface
import com.delhivery.orion.ui.selectroute.SelectRouteActivity
import com.delhivery.orion.utils.extensions.actionDone
import com.delhivery.orion.utils.extensions.errorVibrate
import com.delhivery.orion.utils.extensions.isNotNullOrEmpty
import com.delhivery.orion.utils.extensions.onBackground
import com.delhivery.orion.utils.extensions.plusAssign
import com.delhivery.orion.utils.extensions.raisedFocus

class AuthenticationActivity : BaseActivity<ActivityAuthenticationBinding, AuthenticationViewModel>(),
    DelhiveryOTPViewInterface, OTPReceiverInterface {
  override fun getViewModelClass() = AuthenticationViewModel::class.java

  override fun layoutId() = R.layout.activity_authentication

  override fun requireConnection() = true

  /* OTP receiver */
  private val otpReceiver by lazy { OTPReceiver(this) }

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
      lengthAction(10) { viewModel.sendOTP() }
      actionDone { viewModel.sendOTP() }
    }

    /* otp view interface */
    binding.otpView.otpViewInterface = this

    /* Initiate state */
    viewModel.state = PhoneNo

    /* request sms receive permission */
    receiveSMSPermission {}
  }

  override fun onBackPressed() {
    when (binding.state) {
      PhoneNo -> {
        super.onBackPressed()
      }
      OTP -> viewModel.state = PhoneNo
      LoginProgress -> {/* do nothing when loading */
      }
    }
  }

  override fun otpSubmitted(otp: CharArray) {
    viewModel.verifyOTP(otp)
  }

  /**
   * Read receive permission
   */
  private fun receiveSMSPermission(action: (granted: Boolean) -> Unit) {
    compositeDisposable += requestPermission(Manifest.permission.RECEIVE_SMS)
        .onBackground()
        .subscribe { granted, error ->
          if (error == null && granted) {
            action(granted)
          } else {
            action(false)
            /* read permission error */
          }
        }
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
            /* read sms per */
            receiveSMSPermission {
              if (it) {
                registerReceiver(otpReceiver, IntentFilter(OTP_INTENT_FILTER))
              }
            }

            uiUtils.hideDelhiveryProgress()
            //show keyboard and clear otp
            uiUtils.toggleKeyboard(false)
            binding.otpView.clear(focusedIndex = 0, animate = false)

            /* show masked phone no */
            viewModel.phoneNo.let {
              if (it.length > 2) {
                binding.textOtpSentToPhoneNo.text =
                    getString(R.string.msg_otp_sent_to_phone_no, it.substring(it.length - 2))
              }
            }
          }
          LoginProgress -> {
            /* un register otp reader */
            unregisterReceiver(otpReceiver)

            //hide keyboard show progress view
            uiUtils.showDelhiveryProgress(
                title = "Logging you in..",
                message = "This usually takes few seconds to load. please be patient.",
                proTip = "Some tip regarding how to bid, or whats to be considered while bidding. "
            )
          }
          /* Login success, No user routes found - select route activity */
          SelectRoute -> {
            uiUtils.hideDelhiveryProgress()
            navigationUtils.navigate(SelectRouteActivity::class.java, finishAfter = true)
          }
          /* Login success, user routes found - navigate to load requests */
          LoadRequest -> {
            uiUtils.hideDelhiveryProgress()
            navigationUtils.navigate(SelectRouteActivity::class.java, finishAfter = true)
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
}