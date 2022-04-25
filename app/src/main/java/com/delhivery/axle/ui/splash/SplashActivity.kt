package com.delhivery.axle.ui.splash

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.animation.OvershootInterpolator
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivitySplashBinding
import com.delhivery.axle.fcm.*
import com.delhivery.axle.ui.accountaction.AccountActionActivity
import com.delhivery.axle.ui.accountdetails.AccountDetailsActivity
import com.delhivery.axle.ui.accountrole.AccountRoleActivity
import com.delhivery.axle.ui.auth.AuthenticationActivity
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.businessverification.BusinessVerificationActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.kyc.gst.GstVerificationActivity
import com.delhivery.axle.ui.kyc.aadhaar.AadhaarVerificationActivity
import com.delhivery.axle.ui.kyc.address.AddressActivity
import com.delhivery.axle.ui.kyc.address.CommunicationAddressActivity
import com.delhivery.axle.ui.kyc.address.CommunicationAddressViewModel
import com.delhivery.axle.ui.kyc.identityverification.IdentityVerificationActivity
import com.delhivery.axle.ui.kyc.pan.PanVerificationActivity
import com.delhivery.axle.ui.onboarding.BasicDetailsActivity
import com.delhivery.axle.ui.onboarding.OnboardingActivity
import com.delhivery.axle.ui.paymentdetails.PaymentDetailsActivity
import com.delhivery.axle.ui.paymentdetails.VendorPolicyActivity
import com.delhivery.axle.ui.searchcitystate.SearchCityStateActivity
import com.delhivery.axle.ui.splash.SplashPostState.*
import com.delhivery.axle.ui.userroutes.UserRoutesActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import com.github.florent37.kotlin.pleaseanimate.please
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.*
import javax.inject.Inject

/**
 * Splash screen
 */
class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {
  init {
    StatusBarColor = Color.parseColor("#181818")
  }

  override fun getViewModelClass() = SplashViewModel::class.java

  override fun layoutId() = R.layout.activity_splash

  var latestCode :Int = 0
  var currentCode :Int =0
  var type :String = ""
  var tid :String  = ""
  lateinit var isAuthenticated :SplashPostState
  var ifUpdateFalse=false
  override fun requireConnection() = false
  @Inject lateinit var userPrefs: UserPrefs

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    //Capture event
    val cal = Calendar.getInstance()
    val currentHourIn24Format = cal[Calendar.HOUR_OF_DAY]
    analyticsUtil.trackEvent(
            EVENT_APP_OPEN,
            mutableListOf(PROPERTY_USER_ID, PROPERTY_HOUR_OF_DAY),
            mutableListOf(userPrefs.userId() , currentHourIn24Format.toString())
    )

    notificationId = intent?.extras?.getString(ARGS_NOTIFICATION_KEY) ?: ""
    transactions = intent?.extras?.getString(ARGS_TRANSACTION_IDS) ?: ""
    notificationType = intent?.extras?.getString(ARGS_NOTIFICATION_TYPE) ?: ""
    preferredTransactionId = intent?.extras?.getString(ARGS_PREFERRED_TRANSACTION_ID) ?: ""

     //For Inventory only
    vehicleNumber = intent?.extras?.getString(ARGS_VEHICLE_NUMBER) ?: ""
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* start splash animation */
    animate()
    checkForDynamicLinks()
    binding.btnGetStarted.setOnClickListener {
      if(ifUpdateFalse) {
        postAnimate(isAuthenticated)
      }
    }

  }

  private fun checkForDynamicLinks() {
    FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
            .addOnSuccessListener(this){
              Log.d("dynamicLinkFromSplash","Dynamic link Received")
              var deepLink: Uri? = null
              if (it != null) {
                deepLink = it.link
              }
              if(deepLink != null){
                Log.d("dynamicLinkFromSplash","Deep link Received" +deepLink.toString())
                type = deepLink.getQueryParameter("type")?:""
                tid = deepLink.getQueryParameter("id")?:""
                Log.d("dynamicLinkFromSplash","Deep link Parameters $tid $type")
              }
            }
            .addOnFailureListener(this){
              Log.d("dynamicLinkFromSplash", "getDynamicLink:onFailure")
            }
  }

  /**
   * Splash animation chain
   */
  private fun animate() {
     isAuthenticated = viewModel.postState()

      checkForUpdatedVersion { it ->
        when (it) {
          true -> {
            dialogUtils.showBasicConfirmDialog(
                R.string.title_dialog_update,
                R.string.msg_dialog_update,
                positiveAction = "UPDATE",
                negativeAction = "CANCEL",
                positiveClickListener = {
                  analyticsUtil.trackEvent(
                          EVENT_UPDATE_APP,
                          mutableListOf(PROPERTY_USER_ID , PROPERTY_CURRENT_VERSION , PROPERTY_LATEST_VERSION),
                          mutableListOf(userPrefs.userId(), currentCode.toString() , latestCode.toString())
                  )
                  it.dismiss()
                  openPlayStore()
                },
                negativeClickListener = {
                  analyticsUtil.trackEvent(
                          EVENT_UPDATE_CANCEL,
                          mutableListOf(PROPERTY_USER_ID , PROPERTY_CURRENT_VERSION , PROPERTY_LATEST_VERSION),
                          mutableListOf(userPrefs.userId(), currentCode.toString() , latestCode.toString() )
                  )
                  it.dismiss()
                  finish()
                }
            )
          }
          false -> {
           ifUpdateFalse=true
          }
        }
      }

  }

  private fun openPlayStore() {
    val appPackageName = packageName
    try {
      startActivity(
          Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
      )
    } catch (anfe: android.content.ActivityNotFoundException) {
      startActivity(
          Intent(
              Intent.ACTION_VIEW, Uri.parse(
              "https://play.google.com/store/apps/details?id=$appPackageName"
          )
          )
      )
    }
  }

  private fun checkForUpdatedVersion(completedAction: (success: Boolean) -> Unit) {
    val configSettings = FirebaseRemoteConfigSettings.Builder()
        .setMinimumFetchIntervalInSeconds(0)
        .build()

    val remoteConfig = FirebaseRemoteConfig.getInstance()
    remoteConfig.setConfigSettingsAsync(configSettings)

    FirebaseRemoteConfig.getInstance()
        .fetchAndActivate()
        .addOnCompleteListener(
            this
        ) {
          if (it.isSuccessful) {
            val currentVersionCode: Int
            val playStoreVersionCode: Int = try {
              remoteConfig.activate()
              remoteConfig.getString("android_latest_version_code")
                  .toInt()
            } catch (e: Exception) {
              0
            }

            try {
              viewModel.savePMTValidation(
                  remoteConfig.getString("max_pmt_rate").toInt(),
                  remoteConfig.getString("max_cost_per_km").toInt()
              )
            } catch (e: NumberFormatException) {
              //Do Nothing
            }
              try{
              viewModel.saveLoadPostKycConfig(
                  remoteConfig.getString("post_load")
              )
                  viewModel.saveTruckPostKycConfig(
                      remoteConfig.getString("post_truck")
                  )
          } catch (e: Exception) {
            //Do Nothing
        }

            val pInfo = this.packageManager.getPackageInfo(packageName, 0)
            currentVersionCode = if (VERSION.SDK_INT >= VERSION_CODES.P) {
              pInfo.longVersionCode.toInt()
            } else {
              pInfo.versionCode
            }
            currentCode = currentVersionCode
            latestCode = playStoreVersionCode
            completedAction(playStoreVersionCode > currentVersionCode)
          } else {
            completedAction(false)
          }
        }
        .addOnFailureListener { completedAction(false) }
        .addOnCanceledListener { completedAction(false) }
  }

  private fun postAnimate(state: SplashPostState) {
    /**
     * Check If it's from deep link
     * */
    if (state == Home && type != "") {
      val bundle = Bundle()
      bundle.putString(ARGS_DEEPLINK_TYPE , type)
      bundle.putString(ARGS_DEEPLINK_ID , tid)
     navigationUtils.navigate(HomeActivity::class.java, true, bundle)
    } else {
      when (state) {
        Auth -> AuthenticationActivity::class
        Home -> PaymentDetailsActivity::class
        AccountDetails -> AccountDetailsActivity::class
      }.let {
        val bundle = Bundle()
        if (!TextUtils.isEmpty(notificationId)) {
          bundle.putString(ARGS_NOTIFICATION_ID, notificationId)
          bundle.putString(ARGS_NOTIFICATION_TYPE, notificationType)
          bundle.putString(ARGS_TRANSACTION_IDS, transactions)
          bundle.putString(ARGS_PREFERRED_TRANSACTION_ID, preferredTransactionId)
          //For Inventory
          bundle.putString(ARGS_VEHICLE_NUMBER, vehicleNumber)
        }
          navigationUtils.navigate(it.java, true, bundle)
      }
    }
  }
}

/* delay before animation starts */
private const val SplashAnimationDelay = 2000L