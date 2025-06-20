package com.delhivery.axle.ui.auth

import android.content.Intent
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
import com.delhivery.axle.ui.auth.AuthenticationUIError.*
import com.delhivery.axle.ui.auth.AuthenticationUIState.*
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.custom.DelhiveryOTPViewInterface
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onBackground
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
class AuthenticationActivity : BaseActivity<ActivityAuthenticationBinding, AuthenticationViewModel>(),
        DelhiveryOTPViewInterface, OTPReceiverInterface {

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
    notificationId = intent?.extras?.getString(ARGS_NOTIFICATION_ID) ?: ""
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
          PhoneNo -> {
            finish()
          }
          OTP -> viewModel.state = PhoneNo
          Password -> viewModel.state = PhoneNo
          LoginProgress -> {/* do nothing when loading */
          }
          else -> {
          }
        }

      }
    })
    /*move to back screen*/
    binding.btnChangeNumber.setOnClickListener {
      when (binding.state) {
        PhoneNo -> {
          onBackPressedDispatcher.onBackPressed()
        }
        OTP -> viewModel.state = PhoneNo
        LoginProgress -> {/* do nothing when loading */
        }
        else -> {
        }
      }
    }

    /* phone no edit button setup */
    binding.editPhoneNo.apply {
      // raisedFocus()
      lengthAction(9){
        binding.btnSendOtp.isEnabled = false
      }

      lengthAction(10) {
        binding.btnSendOtp.isEnabled = true
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

    viewModel.otpStatusLiveData.observe(this, Observer {
      if (it == true) {
        timeoutDisposable = Observable.interval(0L, 1L, TimeUnit.SECONDS)
                .onBackground()
                .subscribe {
                  val timeLeft = 15L - it
                  if (timeLeft > 0) {
                    val f: NumberFormat = DecimalFormat("00")
                    binding.btnResendOtp.text = "${getString(string.label_resend_otp)} 00:"+ f.format(timeLeft!!)
                    binding.btnResendOtp.setTextColor(ContextCompat.getColor(applicationContext,R.color.color_hint))
                  } else if (timeLeft == 0L) {
                    binding.btnResendOtp.text = getString(string.label_resend_otp_done)
                    binding.btnResendOtp.setTextColor(ContextCompat.getColor(applicationContext,R.color.colorAccent))
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

    binding.loginUsingPassword.setOnClickListener{
      viewModel.state = Password
    }

    binding.loginButton.setOnClickListener{
      performLogin()
    }


    if (notificationId.isNotEmpty()) {
      markNotificationRead()
    }
  }

  override fun onResume() {
    super.onResume()
    if (activitySetupTrace != null && isFirstResume) {
      activitySetupTrace?.stop()
      isFirstResume = false
    }
  }

  override fun markNotificationRead() {
    super.markNotificationRead()
    viewModel.markNotificationRead(notificationId)
  }


  override fun otpSubmitted(otp: CharArray) {
    binding.btnVerifyOtp.isEnabled = true
    viewModel.verifyOTP(otp)
  }

  override fun otpFound(otp: String) {
    otpSubmitted(otp.toCharArray())
  }

  fun maskPhoneNumber(phone: String): String {
    return if (phone.length >= 6) {
      "X".repeat(6) + phone.substring(6)
    } else {
      "X".repeat(phone.length)
    }
  }
  private fun performLogin() {
    var flag= true
    if(binding.tilUserId.editText?.text == null  || binding.editUserId.text.toString() == ""){
      binding.tilUserId.isErrorEnabled = true
      binding.tilUserId.error ="Field can't be empty"
      flag= false
    }
    else{
      binding.tilUserId.isErrorEnabled = false
      binding.tilUserId.error = null
    }

    if(binding.tilUserPassword.editText?.text == null  || binding.editUserPassword.text.toString() == ""){
      binding.tilUserPassword.isErrorEnabled = true
      binding.tilUserPassword.error ="Field can't be empty"
      flag= false
    }
    else{
      binding.tilUserPassword.isErrorEnabled = false
      binding.tilUserPassword.error = null
    }

    if(flag){
      val userId=  binding.tilUserId.editText?.text.toString()
      val userPassword = binding.tilUserPassword.editText?.text.toString()
     // viewModel.loginUsingPassword("offroll@gmail.com","Off@12345678")
      viewModel.loginUsingPassword(userId,userPassword)
    }
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
            //show keyboard
            Handler(Looper.getMainLooper()).postDelayed({
              binding.editPhoneNo.requestFocus()
              uiUtils.toggleKeyboard(false)
            }, 600)
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
                        getString(string.msg_otp_sent_to_phone_no, maskPhoneNumber(it))
              }
            }
          }
          Password ->{
            uiUtils.hideDelhiveryProgress()
          }
          LoginProgress -> {
            //hide keyboard show progress view
            uiUtils.showProgress("Loading...")
          }
          /* Login success, user routes found - navigate to load requests */
          LoadRequest -> {
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
  inner class ErrorObserver : Observer<Pair<AuthenticationUIError, String?>> {
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
          InvalidOTP -> {   //Invalid OTP
            binding.otpError.visibility = View.VISIBLE
            uiUtils.hideProgress()
          }
          InvalidPassword -> {   //Invalid password clear fields
            navigationUtils.navigate(InvalidActivity::class.java, false)
            uiUtils.hideProgress()
            uiUtils.hideDelhiveryProgress()

          }
          None -> {/* nothing */
            binding.otpError.visibility = View.GONE
            uiUtils.hideProgress()
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