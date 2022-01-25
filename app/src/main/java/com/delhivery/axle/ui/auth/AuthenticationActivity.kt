package com.delhivery.axle.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.databinding.ActivityAuthenticationBinding
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_ID
import com.delhivery.axle.receiver.OTPReceiverInterface
import com.delhivery.axle.ui.accountsetup.AccountSetupActivity
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidOTP
import com.delhivery.axle.ui.auth.AuthenticationUIError.InvalidPhoneNo
import com.delhivery.axle.ui.auth.AuthenticationUIError.None
import com.delhivery.axle.ui.auth.AuthenticationUIState.*
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.custom.DelhiveryOTPViewInterface
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.kyc.gst.GstVerificationActivity
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
import kotlinx.android.synthetic.main.activity_authentication.*
import kotlinx.android.synthetic.main.truck_spinner_item.view.*
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*
import kotlinx.android.synthetic.main.view_trip_history_item.view.*
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlinx.android.synthetic.main.view_active_trips_progress_item.*
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*
import kotlinx.android.synthetic.main.view_home_loads_progress_item.view
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

  init {
    StatusBarColor = Color.parseColor("#ededff")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    notificationId = intent?.extras?.getString(ARGS_NOTIFICATION_ID) ?: ""
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* observe and update ui state */
    viewModel.stateLiveData.observe(this, StateObserver())

    /* observe errors and update ui */
    viewModel.errorLiveData.observe(this, ErrorObserver())

    /*move to back screen*/
    binding.btnChangeNumber.setOnClickListener {
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

    /* phone no edit button setup */
    binding.editPhoneNo.apply {
      //raisedFocus()
      lengthAction(9){
        binding.btnSendOtp.isEnabled = false
      }

     lengthAction(10) {
        // Capture event
        analyticsUtil.trackEvent(
                EVENT_OTP_SEND,
                mutableListOf(PROPERTY_MOBILE_NUMBER_ENTERED),
                mutableListOf(binding.editPhoneNo.text.toString())
        )
       binding.btnSendOtp.isEnabled = true
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

    viewModel.otpStatusLiveData.observe(this, Observer {
      if (it == true) {
        timeoutDisposable = Observable.interval(0L, 1L, TimeUnit.SECONDS)
            .onBackground()
            .subscribe {
              val timeLeft = 15L - it
              if (timeLeft > 0) {
                val f: NumberFormat = DecimalFormat("00")
                binding.btnResendOtp.text = "${getString(string.label_resend_otp)} 00:"+ f.format(timeLeft!!)
                binding.btnResendOtp.setTextColor(resources.getColor(R.color.color_hint))
              } else if (timeLeft == 0L) {
                binding.btnResendOtp.text = getString(string.label_resend_otp_done)
                binding.btnResendOtp.setTextColor(resources.getColor(R.color.colorAccent))
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

    binding.btnVerifyOtp.setOnClickListener {
      if(viewModel.otpCurrent.isNotNullOrEmpty() && viewModel.otpCurrent.length==4) {
        viewModel.verifyOTP(viewModel.otpCurrent.toCharArray())
      }
    }

    /*binding.loginUsingPassword.setOnClickListener{
      viewModel.state = Password
    }*/

    /*binding.loginButton.setOnClickListener{
      performLogin()
    }*/


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
      Password -> viewModel.state = PhoneNo
      LoginProgress -> {/* do nothing when loading */
      }
      else -> {
      }
    }
  }

  override fun otpSubmitted(otp: CharArray) {
    //viewModel.otpCurrent = otp.toString()
    viewModel.verifyOTP(otp)
  }

  override fun otpFound(otp: String) {
    otpSubmitted(otp.toCharArray())
    binding.btnVerifyOtp.isEnabled = true
  }

  /*private fun performLogin() {
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
  }*/

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
                  getString(string.msg_otp_sent_to_phone_no, it)
              }
            }
          }
          Password ->{
            uiUtils.hideDelhiveryProgress()
          }
          LoginProgress -> {
            //hide keyboard show progress view
            uiUtils.showProgress()
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
          /* otp verified, account set up needed */
          AccountSetup -> {
            uiUtils.hideDelhiveryProgress()
            navigationUtils.navigate(GstVerificationActivity::class.java, true)
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
          AuthenticationUIError.InvalidPassword -> {   //Invalid password clear fields
            navigationUtils.navigate(InvalidActivity::class.java, false)
            //uiUtils.showSnackbar("Invalid username or password")
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