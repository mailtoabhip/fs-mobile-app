package com.delhivery.axle.ui.splash

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings.Secure
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivitySplashBinding
import com.delhivery.axle.fcm.ARGS_DEEPLINK_ID
import com.delhivery.axle.fcm.ARGS_DEEPLINK_TYPE
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_ID
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_KEY
import com.delhivery.axle.fcm.ARGS_NOTIFICATION_TYPE
import com.delhivery.axle.fcm.ARGS_PREFERRED_TRANSACTION_ID
import com.delhivery.axle.fcm.ARGS_TRANSACTION_IDS
import com.delhivery.axle.fcm.ARGS_VEHICLE_NUMBER
import com.delhivery.axle.fcm.*
import com.delhivery.axle.ui.accountdetails.AccountDetailsActivity
import com.delhivery.axle.ui.auth.AuthenticationActivity
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFragment
import com.delhivery.axle.ui.splash.SplashPostState.AccountDetails
import com.delhivery.axle.ui.splash.SplashPostState.Auth
import com.delhivery.axle.ui.splash.SplashPostState.Home
import com.delhivery.axle.utils.EVENT_ADD_TRUCK_SUBMIT
import com.delhivery.axle.utils.EVENT_APP_OPEN
import com.delhivery.axle.utils.EVENT_HOME_SEARCH_INITIATE
import com.delhivery.axle.utils.EVENT_UPDATE_APP
import com.delhivery.axle.utils.EVENT_UPDATE_CANCEL
import com.delhivery.axle.utils.PROPERTY_CURRENT_VERSION
import com.delhivery.axle.utils.PROPERTY_HOUR_OF_DAY
import com.delhivery.axle.utils.PROPERTY_LATEST_VERSION
import com.delhivery.axle.utils.PROPERTY_ORDER_COUNT
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.USER_PROPERTY_ANDROID_ID
import com.delhivery.axle.utils.USER_PROPERTY_ANDROID_VERSION
import com.delhivery.axle.ui.splash.SplashPostState.*
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.Calendar
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

    //For pricing
    pricingId =  intent?.extras?.getString(ARGS_PRICING_ID) ?: ""
    pricingSortKey =  intent?.extras?.getString(ARGS_PRICING_SORT_KEY) ?: ""
    notificationFrom =  intent?.extras?.getString(ARGS_NOTIFICATION_FROM) ?: ""
    pricingOfferId =  intent?.extras?.getString(ARGS_OFFER_ID) ?: ""


  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* start splash animation */
    userPrefs.previousNavigationTab = HomeLoadsFragment::class.java.name
    userPrefs.currentNavigationTab = HomeLoadsFragment::class.java.name
    animate()
    checkForDynamicLinks()
    binding.btnGetStarted.visibility = View.GONE
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
            if(ifUpdateFalse && userPrefs.hasLoggedIn){
              binding.btnGetStarted.visibility = View.GONE
              postAnimate(isAuthenticated)
            }else{
              binding.btnGetStarted.visibility = View.VISIBLE
            }
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
                viewModel.saveBannerTextConfig(
                    remoteConfig.getString("advert_share_rate_page_banner_text")
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

            val androidId = Secure.getString(
              this.contentResolver,
              Secure.ANDROID_ID
            )
            //get device and app level details
            analyticsUtil.moEngageUserAttribute(USER_PROPERTY_ANDROID_ID,androidId)
            analyticsUtil.moEngageUserAttribute(USER_PROPERTY_ANDROID_VERSION,pInfo.versionName+"("+currentCode.toString()+")")

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
    userPrefs.setPreviousScreen(this.javaClass.name)
    if (state == Home && type != "") {
      val bundle = Bundle()
      bundle.putString(ARGS_DEEPLINK_TYPE , type)
      bundle.putString(ARGS_DEEPLINK_ID , tid)
     navigationUtils.navigate(HomeActivity::class.java, true, bundle)
    } else {
      when (state) {
        Auth -> AuthenticationActivity::class
        Home -> HomeActivity::class
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

          //For pricing
          bundle.putString(ARGS_PRICING_ID, pricingId)
          bundle.putString(ARGS_PRICING_SORT_KEY, pricingSortKey)
          bundle.putString(ARGS_OFFER_ID, pricingOfferId)
          bundle.putString(ARGS_NOTIFICATION_FROM, notificationFrom)

        }
          navigationUtils.navigate(it.java, true, bundle)
      }
    }
  }
}

/* delay before animation starts */
private const val SplashAnimationDelay = 2000L