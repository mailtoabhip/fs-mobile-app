package com.delhivery.axle.ui.auth

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.databinding.ActivityAuthenticationBinding
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_ID
import com.delhivery.axle.receiver.OTPReceiverInterface
import com.delhivery.axle.ui.accountdetails.AccountDetailsActivity
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidOTP
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidPhoneNo
import com.delhivery.axle.ui.auth.AuthenticationUIError.None
import com.delhivery.axle.ui.auth.AuthenticationUIState.AccountDetails
import com.delhivery.axle.ui.auth.AuthenticationUIState.Disabled
import com.delhivery.axle.ui.auth.AuthenticationUIState.HomePage
import com.delhivery.axle.ui.auth.AuthenticationUIState.LoginProgress
import com.delhivery.axle.ui.auth.AuthenticationUIState.OTP
import com.delhivery.axle.ui.auth.AuthenticationUIState.PhoneNo
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.utils.EVENT_LOGIN
import com.delhivery.axle.utils.EVENT_OTP_RESEND
import com.delhivery.axle.utils.EVENT_OTP_SEND
import com.delhivery.axle.utils.EVENT_OTP_VERIFIED
import com.delhivery.axle.utils.PROPERTY_MOBILE_NUMBER_ENTERED
import com.delhivery.axle.utils.PROPERTY_OTP_SEND_COUNT
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.USER_PROPERTY_PHONE_NO
import com.delhivery.axle.utils.USER_PROPERTY_UUID
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.onLengthReached
import com.delhivery.axle.utils.extensions.safeDispose
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.moengage.core.internal.USER_ATTRIBUTE_UNIQUE_ID
import com.moengage.core.internal.USER_ATTRIBUTE_USER_MOBILE
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Authentication screen
 */
class AuthenticationActivity : BaseActivity<ActivityAuthenticationBinding, AuthenticationViewModel>(), OTPReceiverInterface {

  override fun getViewModelClass() = AuthenticationViewModel::class.java

  override fun layoutId() = R.layout.activity_authentication

  override fun requireConnection() = true

  /* dismiss timeout disposable */
  private var timeoutDisposable: Disposable? = null
  private var activitySetupTrace: Trace? = null
  private var isFirstResume = true

  @Inject lateinit var userPrefs: UserPrefs

  init {
    StatusBarColor = Color.parseColor("#ededff")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activitySetupTrace = FirebasePerformance.getInstance().newTrace("AuthenticationActivity_SetupTime")
    activitySetupTrace?.start()
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* observe and update ui state */
    viewModel.stateLiveData.observe(this, StateObserver())

    /* observe errors and update ui */
    viewModel.errorLiveData.observe(this, ErrorObserver())

    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        when (binding.state) {
          PhoneNo -> finish()
          OTP, LoginProgress -> viewModel.state = PhoneNo
          else -> { }
        }
      }
    })

    binding.icBack.setOnClickListener {
      when (binding.state) {
        PhoneNo -> onBackPressedDispatcher.onBackPressed()
        OTP, LoginProgress -> viewModel.state = PhoneNo
        else -> { }
      }
    }

    /* phone no edit button setup */
    binding.editPhoneNo.apply {
      // raisedFocus()
      onLengthReached(10) {reached ->
        binding.btnSendOtp.isEnabled = reached
        binding.ivCheck.visibility = if (reached)View.VISIBLE else View.GONE
      }
    }

    binding.btnSendOtp.setOnClickListener {
      // Capture event
      analyticsUtil.moEngageTrackEvent(
              EVENT_OTP_SEND,
              mutableListOf(PROPERTY_MOBILE_NUMBER_ENTERED),
              mutableListOf(binding.editPhoneNo.text.toString())
      )
      viewModel.sendOTP()
    }

    viewModel.otpStatusLiveData.observe(this, Observer { started ->
      if (started == true) {
        timeoutDisposable.safeDispose()
        timeoutDisposable = Observable.interval(0L, 1L, TimeUnit.SECONDS)
                .onBackground()
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe { tick ->
                  val timeLeft = 15L - tick
                  when {
                    timeLeft > 0 -> {
                      val f: NumberFormat = DecimalFormat("00")
                      binding.btnResendOtp.text = "${getString(string.label_resend_otp)} 00:${f.format(timeLeft)}"
                      binding.btnResendOtp.setTextColor(ContextCompat.getColor(applicationContext, R.color.color_hint))
                      binding.btnResendOtp.isEnabled = false
                    }
                    else -> {
                      // timeLeft <= 0: countdown done, enable resend and stop the timer
                      binding.btnResendOtp.text = getString(string.label_resend_otp_done)
                      binding.btnResendOtp.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
                      binding.btnResendOtp.isEnabled = true
                      timeoutDisposable.safeDispose()
                    }
                  }
                }
      } else {
        timeoutDisposable.safeDispose()
      }
    })

    /* otp view interface */
    binding.otpView.onOtpComplete = { otp -> viewModel.verifyOTP(otp) }

    /* Initiate state */
    viewModel.state = PhoneNo

    binding.btnResendOtp.setOnClickListener {
      analyticsUtil.moEngageTrackEvent(
              EVENT_OTP_RESEND,
              mutableListOf(PROPERTY_MOBILE_NUMBER_ENTERED),
              mutableListOf(viewModel.phoneNo)
      )
      viewModel.otpSendCount +=1
      binding.otpError.visibility = View.GONE
      viewModel.sendOTP()
    }

    binding.btnSendOtp.setOnClickListener {
      // Capture event
      analyticsUtil.moEngageTrackEvent(
              EVENT_OTP_SEND,
              mutableListOf(PROPERTY_MOBILE_NUMBER_ENTERED),
              mutableListOf(viewModel.phoneNo)
      )
      binding.otpError.visibility = View.GONE
      viewModel.sendOTP()
    }


  }

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }



  override fun otpFound(otp: String) {
    viewModel.verifyOTP(otp.toCharArray())
  }

  fun maskPhoneNumber(phone: String): String {
    return if (phone.length >= 6) {
      "X".repeat(6) + phone.substring(6)
    } else {
      "X".repeat(phone.length)
    }
  }

  private fun resetVerifyButton() {
    binding.progressVerifyOtp.visibility = View.GONE
    binding.btnVerifyOtp.text = getString(string.action_verify_otp)
    binding.btnVerifyOtp.isEnabled = false // stays disabled until next OTP entry
  }

  /**
   * [AuthenticationUIState] observer
   */
  inner class StateObserver : Observer<AuthenticationUIState?> {
    override fun onChanged(it: AuthenticationUIState?) {
      it?.let { state ->
        binding.state = state
        when (state) {
          PhoneNo -> {
            //show keyboard
            binding.icBack.visibility = View.INVISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
              binding.editPhoneNo.requestFocus()
              uiUtils.toggleKeyboard(false)
            }, 600)
          }
          OTP -> {
            binding.icBack.visibility = View.VISIBLE
            uiUtils.hideDelhiveryProgress()
            uiUtils.toggleKeyboard(false)
            resetVerifyButton()
            binding.otpView.clear()
            /* show masked phone no */
            viewModel.phoneNo.let {
              if (it.length > 2) {
                binding.textOtpSentToPhoneNo.text =
                        getString(string.msg_otp_sent_to_phone_no, maskPhoneNumber(it))
              }
            }
          }
          LoginProgress -> {
            // Show loading inside the verify button instead of a full-screen overlay
            binding.icBack.visibility = View.VISIBLE
            binding.btnVerifyOtp.text = ""
            binding.btnVerifyOtp.isEnabled = false
            binding.progressVerifyOtp.visibility = View.VISIBLE
            uiUtils.toggleKeyboard(true)
          }
          /* Login success, user routes found - navigate to load requests */
          HomePage -> {
            analyticsUtil.moEngageTrackEvent(EVENT_LOGIN)
            userPrefs.setPreviousScreen(AuthenticationActivity::class.java.name)
            // Capture event
            analyticsUtil.moEngageTrackEvent(
                    EVENT_OTP_VERIFIED,
                    mutableListOf(PROPERTY_MOBILE_NUMBER_ENTERED , PROPERTY_USER_ID , PROPERTY_OTP_SEND_COUNT),
                    mutableListOf(viewModel.phoneNo , userPrefs.userId() , viewModel.otpSendCount.toString())
            )
            uiUtils.hideDelhiveryProgress()
            userPrefs.isFirstOpenRate=true
            navigationUtils.navigate(HomeActivity::class.java, true)
          }
          AccountDetails -> {
            analyticsUtil.moEngageTrackEvent(EVENT_LOGIN)
            userPrefs.setPreviousScreen(AuthenticationActivity::class.java.name)
            userPrefs.userId().let {
              analyticsUtil.moEngageUserAttribute(USER_PROPERTY_UUID,it)
              analyticsUtil.moEngageUserAttribute(USER_ATTRIBUTE_UNIQUE_ID,it)
            }
            userPrefs.phoneNumber?.let {
              analyticsUtil.moEngageUserAttribute(USER_ATTRIBUTE_USER_MOBILE,it)
              analyticsUtil.moEngageUserAttribute(USER_PROPERTY_PHONE_NO,it)
            }
            uiUtils.showProgress("Loading...")
            navigationUtils.navigate(AccountDetailsActivity::class.java, true)
          }
          Disabled -> {
            userPrefs.userId().let {
              analyticsUtil.moEngageUserAttribute(USER_PROPERTY_UUID,it)
              analyticsUtil.moEngageUserAttribute(USER_ATTRIBUTE_UNIQUE_ID,it)
            }
            userPrefs.phoneNumber?.let {
              analyticsUtil.moEngageUserAttribute(USER_ATTRIBUTE_USER_MOBILE,it)
              analyticsUtil.moEngageUserAttribute(USER_PROPERTY_PHONE_NO,it)
            }
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
  inner class ErrorObserver : Observer<Pair<AuthenticationUIError, String?>?> {
    override fun onChanged(it: Pair<AuthenticationUIError, String?>?) {
      it?.let { error ->
        /* show error message in dialog if not null || empty */
        if (error.second.isNotNullOrEmpty()) {
          dialogUtils.showErrorDialog(error.second!!,3L)
        }
        /* handle each error state */
        when (error.first) {
          InvalidPhoneNo -> {
            binding.editPhoneNo.errorVibrate()
          }
          InvalidOTP -> {
            binding.otpError.visibility = View.VISIBLE
            binding.otpView.showError(true)
            resetVerifyButton()
            uiUtils.hideProgress()
          }
          None -> {
            binding.otpError.visibility = View.GONE
            binding.otpView.showError(false)
            resetVerifyButton()
            uiUtils.hideProgress()
          }
        }

      }
    }
  }
}