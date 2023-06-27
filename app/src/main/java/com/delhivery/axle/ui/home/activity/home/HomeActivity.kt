package com.delhivery.axle.ui.home.activity.home
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityHomeBinding
import com.delhivery.axle.fcm.*
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.bids.userTripsIntent
import com.delhivery.axle.ui.contractDetails.contractDetailsIntent
import com.delhivery.axle.ui.home.fragments.*
import com.delhivery.axle.ui.home.fragments.HomeFragmentType.*
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsFragment
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFragment
import com.delhivery.axle.ui.home.fragments.pod.HomePodsFragment
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsFragment
import com.delhivery.axle.ui.ledger.consolidatedPageIntent
import com.delhivery.axle.ui.paymentdetails.VendorPolicyActivity
import com.delhivery.axle.ui.profile.MyProfileActivity
import com.delhivery.axle.ui.profile.raterewards.ShareRateGetRewardsActivity
import com.delhivery.axle.ui.sharerate.ShareRateActivity
import com.delhivery.axle.ui.splash.StartRoutingActivity
import com.delhivery.axle.ui.team.teamMembersIntent
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.ui.trucks.TruckActivity
import com.delhivery.axle.ui.userroutes.userRoutesIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onPageSelected
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.FirebaseInAppMessagingClickListener
import com.google.firebase.inappmessaging.model.Action
import com.google.firebase.inappmessaging.model.CampaignMetadata
import com.google.firebase.inappmessaging.model.InAppMessage
import com.moengage.core.internal.MoEConstants
import java.util.*
import javax.inject.Inject

/**
 * Home screen
 */
class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(),
  OnNavigationItemSelectedListener, FirebaseInAppMessagingClickListener {
  override fun getViewModelClass() = HomeViewModel::class.java
  override fun layoutId() = R.layout.activity_home
  override fun requireConnection() = true
  var fragmentType : String ?= ""
  var dplink_tid : String = ""
  var dplink_type : String = ""
  var fromLink = false
  var fromNotification = false
  var fromDeepLink = false
  var fromDeepLinkContract = false
  var fromNotificationContract= false
  var vehicleNum =""
  var count =0
  @Inject lateinit var userPrefs : UserPrefs
  /* home fragments pager adapter */
  private val pagerAdapter: HomeFragmentsAdapter by lazy {
    HomeFragmentsAdapter(supportFragmentManager)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        finish()
      }
    })
    notificationId = intent?.extras?.getString(ARGS_NOTIFICATION_ID) ?: ""
    val transactions = intent?.extras?.getString(ARGS_TRANSACTION_IDS) ?: ""
    if (transactions.isNotEmpty())
      transactionIds = transactions.split(",")
        .map { it.trim() }
    notificationType = intent?.extras?.getString(ARGS_NOTIFICATION_TYPE) ?: ""
    preferredTransactionId = intent?.extras?.getString(ARGS_PREFERRED_TRANSACTION_ID) ?: ""
    //For inventory
    vehicleNumber = intent?.extras?.getString(ARGS_VEHICLE_NUMBER) ?: ""
      //For pricing
    pricingId = intent?.extras?.getString(ARGS_PRICING_ID) ?: ""
    pricingSortKey = intent?.extras?.getString(ARGS_PRICING_SORT_KEY) ?: ""
    notificationFrom = intent?.extras?.getString(ARGS_NOTIFICATION_FROM) ?: ""
    pricingOfferId = intent?.extras?.getString(ARGS_OFFER_ID) ?: ""

    fragmentType = intent?.extras?.getString(IntentExtraFragmentTypeKey)
    dplink_tid = intent?.extras?.getString(ARGS_DEEPLINK_ID) ?:""
    dplink_type = intent?.extras?.getString(ARGS_DEEPLINK_TYPE) ?:""
    fromLink = false
    fromNotification = false
    fromDeepLink = false
    fromDeepLinkContract = false
    fromNotificationContract= false
    viewModel.getUserDetails()
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    viewModel.userUpdateLiveData.observe(this, Observer {
      if(it){
        setUserAttributes()
        navigationUtils.navigateOnboardingSteps(true)
        /* setup toolbar */
        setSupportActionBar(binding.toolbar)
        title = "Home"
        if(!userPrefs.userName.isEmpty()) {
          binding.profile.text = userPrefs.userName[0].toUpperCase().toString()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTitle.text = title
        /* setup view pager */
        binding.viewpager.apply {
          offscreenPageLimit = HomeFragmentType.count()
          processDeepLink()
          adapter = pagerAdapter
          /* update ui on page changed */
          onPageSelected { p ->
            HomeFragmentType.pos(p)
              ?.let {
                uiUtils.toggleKeyboard()
                this@HomeActivity.title = HomeFragmentType.pos(p)
                  ?.fragment?.title
              binding.bottomNav.selectedItemId = it.menuId
              observeFragmentLiveData(p)
            }
      }
      binding.toolbarTitle.text = title
      FirebaseInAppMessaging.getInstance().addClickListener(this@HomeActivity)
    }
    binding.viewpager.disableScroll(true)

    /* set navigation item selection listener */
    binding.bottomNav.setOnNavigationItemSelectedListener(this)

    /* by default observe first fragment */
    observeFragmentLiveData()

    if (notificationId.isNotEmpty()) {
      processNotification()
    }

    if (fragmentType.isNotNullOrEmpty() && fragmentType == "pod") {
      analyticsUtil.moEngageTrackEvent(
          EVENT_NAVIGATION_PODS
      )
      userPrefs.setPreviousScreen(this.javaClass.name)
      fragmentAction(NavigateHomeFragmentAction(PodFragment))
    }

    binding.profile.setOnClickListener {
      userPrefs.setPreviousScreen(this.javaClass.name)
      analyticsUtil.moEngageTrackEvent(
        EVENT_NAVIGATION_MY_PROFILE
      )
      navigationUtils.navigate(MyProfileActivity::class.java)
    }
      }
    })

      if (VERSION.SDK_INT >= VERSION_CODES.M) {
     when {
       ContextCompat.checkSelfPermission(
         this, Manifest.permission.POST_NOTIFICATIONS
       ) == PackageManager.PERMISSION_GRANTED -> {

       }
       shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
          Snackbar.make(
            binding.root,
            "Notification blocked",
            Snackbar.LENGTH_LONG
          ).setAction("Settings") {
            // Responds to click on the action
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
          }.show()
       }
       else -> {
         // The registered ActivityResultCallback gets the result of this request
        /* requestPermissionLauncher.launch(
           Manifest.permission.POST_NOTIFICATIONS
         )*/
       }
     }
   }
    if(userPrefs.recommendedUpdate){
      Snackbar.make(
        binding.root,
        "App update version available",
        Snackbar.LENGTH_LONG
      ).setAction("Update") {
        // Responds to click on the action
        checkForAppUpdate(false)
      }.show()

    }
  }
  private fun processDeepLink() {
    Log.d("noti", "$dplink_type $dplink_tid")
    if (dplink_type != "") {
      userPrefs.setPreviousScreen(this.javaClass.name)
      when(dplink_type){
        ROUTE_PREFERENCES_REDIRECT -> {
          startActivity(userRoutesIntent(this))
        }
        TEAM_MEMBERS_REDIRECT -> {
          startActivity(teamMembersIntent(this))
        }
        PAYMENT_SUMMARY_REDIRECT -> {
          startActivity(consolidatedPageIntent(this))
        }
        PHYSICAL_POD_PENDING_REDIRECT -> {
          userPrefs.dpLinkArg = "physicalPod"
          fragmentAction(NavigateHomeFragmentAction(PodFragment))
        }
        EPOD_PENDING_REDIRECT -> {
          userPrefs.dpLinkArg = "ePod"
          fragmentAction(NavigateHomeFragmentAction(PodFragment))
        }
        DOWNLOAD_LEDGER_POPUP_REDIRECT -> {
          startActivity(consolidatedPageIntent(this, true))
        }
        TRIP_DETAIL_REDIRECT -> {
          if (dplink_tid != "") {
            startActivity(tripDetailsIntent(dplink_tid, this))
          } else {
            fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
          }
        }
        LOAD_DETAIL_REDIRECT -> {
          if (dplink_tid != "") {
            startActivity(bidDetailsIntent(dplink_tid, this,source = VALUE_DEEPLINK, subSource = VALUE_VENDOR_SUBSOURCE))
          } else {
            fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
          }
        }

        SUPPLIER_LOAD_REDIRECT -> {
          if (dplink_tid != "") {
            startActivity(bidDetailsIntent(dplink_tid, this,source = VALUE_DEEPLINK, subSource = VALUE_INVENTORY_SUBSOURCE))
          } else {
            fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
          }
        }

        ADVANCE_PENDING_REDIRECT -> {
          userPrefs.startTime = Date().time
          analyticsUtil.trackEvent(
            EVENT_DEEP_LINK_ADD_FUEL_PAYMENT,
            mutableListOf(PROPERTY_USER_ID),
            mutableListOf(userPrefs.userId())
          )
          startActivity(userTripsIntent(this, "payment_view", 0))
        }
        ACTIVATE_TRUCK_REDIRECT ->{
          if(dplink_tid != "") {
            fromDeepLink = true
            vehicleNum = dplink_tid
            fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
          }
          else{
            fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
          }
        }
        MY_TRUCKS_REDIRECT -> {
          fromDeepLink = true
          fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
        }
        ADD_TRUCK_REDIRECT -> {
          fromDeepLink = true
          navigationUtils.navigate(TruckActivity::class.java)
        }
        KYC_REJECTION ->{
          navigationUtils.navigate(MyProfileActivity::class.java)
        }
        SHARE_RATE -> {
          navigationUtils.navigate(ShareRateGetRewardsActivity::class.java)
        }
        KYC_VERIFIED ->{
          fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
        }
        OFFER_APPROVED -> {
          fromDeepLink = true
          if(dplink_tid != ""){
            val pricingId = dplink_tid.split("_").get(0)
            val sortKey = dplink_tid.split("_").get(1)
          val bundle = Bundle()
          bundle.putString(ARGS_NOTIFICATION_TYPE, OFFER_APPROVED)
            analyticsUtil.trackEvent(EVENT_CLICKED_PRICE_NOTIFICATION,mutableListOf(PROPERTY_USER_ID,
              PROPERTY_PHONE_NO, PROPERTY_OFFER_ID,PROPERTY_NOTIFICATION_DETAIL, PROPERTY_NOTIFICATION_TYPE), mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",sortKey,
              OFFER_APPROVED,
              VALUE_DEEP_LINKING))
          navigationUtils.navigate(ShareRateGetRewardsActivity::class.java, false, bundle)
          }
        }
        OFFER_REJECTED -> {
          fromDeepLink = true
          if(dplink_tid != "") {
            val bundle = Bundle()
            bundle.putString(ARGS_NOTIFICATION_TYPE, OFFER_REJECTED)
            val pricingId = dplink_tid.split("_").get(0)
            val sortKey = dplink_tid.split("_").get(1)
            bundle.putString(ARGS_PRICING_ID,pricingId)
            bundle.putString(ARGS_PRICING_ID,sortKey)
               analyticsUtil.trackEvent(EVENT_CLICKED_PRICE_NOTIFICATION,mutableListOf(PROPERTY_USER_ID,
                 PROPERTY_PHONE_NO, PROPERTY_OFFER_ID,PROPERTY_NOTIFICATION_DETAIL, PROPERTY_NOTIFICATION_TYPE), mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",sortKey,
                 OFFER_REJECTED,
                 VALUE_DEEP_LINKING))
            navigationUtils.navigate(ShareRateActivity::class.java, false, bundle)
          }
        }
        CONTRACT_DETAILS -> {
          if(dplink_tid!="")
          startActivity(contractDetailsIntent(dplink_tid,this,VALUE_DEEP_LINKING))
        }
        CONTRACT_LIST -> {
          fromDeepLinkContract = true
          fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
        }
        else -> {
          fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
        }
      }
    }
  }
  private fun processNotification() {
    Log.d("noti", "$notificationType$notificationId $vehicleNumber$pricingId$pricingSortKey$pricingOfferId$notificationFrom")
    markNotificationRead()
    userPrefs.setPreviousScreen(this.javaClass.name)
    when (notificationType) {
      SUBMIT_POD_NOTIFICATION -> {
        if (!transactionIds.isNullOrEmpty() && transactionIds.size == 1) {
          startActivity(tripDetailsIntent(transactionIds[0], this))
        } else {
          fragmentAction(NavigateHomeFragmentAction(PodFragment))
        }
      }
      PREFERRED_SUPPLIER_NOTIFICATION -> {
        startActivity(bidDetailsIntent(preferredTransactionId, this,source = VALUE_PUSH_NOTIFICATION))
      }
      REJECT_POD_NOTIFICATION -> {
        startActivity(tripDetailsIntent(preferredTransactionId, this))
      }
      LOWEST_BID_NOTIFICATION -> {
        if (!transactionIds.isNullOrEmpty() && transactionIds.size == 1) {
          startActivity(bidDetailsIntent(transactionIds[0], this,source = VALUE_PUSH_NOTIFICATION))
        }
        else
        {
          fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
        }
      }
      LANE_PREFERENCE_UPDATE_NOTIFICATION -> {
        startActivity(userRoutesIntent(this))
      }
      REDIRECT_TO_TRIP -> {
        analyticsUtil.trackEvent(
          EVENT_DEEP_LINK_ADD_FUEL_PAYMENT,
          mutableListOf(PROPERTY_USER_ID),
          mutableListOf(userPrefs.userId())
        )
        startActivity(tripDetailsIntent(preferredTransactionId, this))
      }

      REDIRECT_TO_SUPPLIER_RECOMMENDATION -> {
        analyticsUtil.trackEvent(
                EVENT_SUPPLIER_RECOMMENDATION,
                mutableListOf(PROPERTY_SP_PHONE_NUMBER, PROPERTY_ORDER_ID),
                mutableListOf(userPrefs.phoneNumber.toString(), preferredTransactionId)
        )
        startActivity(bidDetailsIntent(preferredTransactionId, this))
      }

      REDIRECT_TO_LOAD -> {
        startActivity(bidDetailsIntent(preferredTransactionId,this,source = VALUE_PUSH_NOTIFICATION))
      }
      ACTIVATE_TRUCK_NOTIFICATION ->{
        fromNotification = true
        vehicleNum = vehicleNumber
        fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
      }
      REDIRECT_TO_TRUCKS -> {
        fromNotification = true
        fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
      }

      TRUCK_REACHED_NOTIFICATION -> {
        fromNotification = true
        vehicleNum = vehicleNumber
        fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
      }
      OFFER_LANE_UPLOADED -> {
        fromNotification = true
        analyticsUtil.trackEvent(EVENT_CLICKED_PRICE_NOTIFICATION,mutableListOf(PROPERTY_USER_ID,
          PROPERTY_PHONE_NO, PROPERTY_OFFER_ID,PROPERTY_NOTIFICATION_DETAIL, PROPERTY_NOTIFICATION_TYPE), mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",pricingOfferId,
          notificationFrom,
          VALUE_NOTIFICATION))
        val bundle = Bundle()
        bundle.putString(ARGS_NOTIFICATION_TYPE, OFFER_LANE_UPLOADED)
        bundle.putString(ARGS_OFFER_ID,pricingOfferId)
        navigationUtils.navigate(ShareRateActivity::class.java, false, bundle)
      }
      OFFER_APPROVED -> {
        fromNotification = true
        val bundle = Bundle()
        bundle.putString(ARGS_NOTIFICATION_TYPE, OFFER_APPROVED)
        analyticsUtil.trackEvent(EVENT_CLICKED_PRICE_NOTIFICATION,mutableListOf(PROPERTY_USER_ID,
          PROPERTY_PHONE_NO, PROPERTY_OFFER_ID,PROPERTY_NOTIFICATION_DETAIL, PROPERTY_NOTIFICATION_TYPE), mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",pricingSortKey,
          OFFER_APPROVED,
          VALUE_NOTIFICATION))
        navigationUtils.navigate(ShareRateGetRewardsActivity::class.java, false, bundle)
      }
      OFFER_REJECTED -> {
        fromNotification = true
        val bundle = Bundle()
        bundle.putString(ARGS_NOTIFICATION_TYPE, OFFER_REJECTED)
        bundle.putString(ARGS_PRICING_ID,pricingId)
        bundle.putString(ARGS_PRICING_SORT_KEY,pricingSortKey)
        analyticsUtil.trackEvent(EVENT_CLICKED_PRICE_NOTIFICATION,mutableListOf(PROPERTY_USER_ID,
          PROPERTY_PHONE_NO, PROPERTY_OFFER_ID,PROPERTY_NOTIFICATION_DETAIL, PROPERTY_NOTIFICATION_TYPE), mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",pricingSortKey,
          OFFER_REJECTED,
          VALUE_NOTIFICATION))
        navigationUtils.navigate(ShareRateActivity::class.java, false, bundle)
      }
      REDIRECT_TO_CONTRACT -> {
        startActivity(contractDetailsIntent(preferredTransactionId,this, VALUE_PUSH_NOTIFICATION))
      }
      REDIRECT_TO_CONTRACT_LIST -> {
        fromNotificationContract = true
        fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
      }
      else -> {
        fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
      }
    }
  }
  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_call, menu)
    return true
  }
  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.nav_call -> {
        //Capture Event
        analyticsUtil.trackEvent(
          EVENT_CALL_VENDOR_DESK,
          mutableListOf(PROPERTY_USER_ID , PROPERTY_PAGE_NAME),
          mutableListOf(userPrefs.userId() , FragmentName.fragmentName(binding.viewpager.currentItem).frgName)
        )
        callHelpline()
        true
      }
      else -> {
        super.onOptionsItemSelected(item)
      }
    }
  }
  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    notificationId = intent?.extras?.getString(ARGS_NOTIFICATION_ID) ?: ""
    val transactions = intent?.extras?.getString(ARGS_TRANSACTION_IDS) ?: ""
    notificationType = intent?.extras?.getString(ARGS_NOTIFICATION_TYPE) ?: ""
    preferredTransactionId = intent?.extras?.getString(ARGS_PREFERRED_TRANSACTION_ID) ?: ""
    //For Inventory
    vehicleNumber = intent?.extras?.getString(ARGS_VEHICLE_NUMBER) ?: ""

    pricingId = intent?.extras?.getString(ARGS_PRICING_ID) ?: ""
    pricingSortKey = intent?.extras?.getString(ARGS_PRICING_SORT_KEY) ?: ""
    notificationFrom = intent?.extras?.getString(ARGS_NOTIFICATION_FROM) ?: ""
    pricingOfferId = intent?.extras?.getString(ARGS_OFFER_ID) ?: ""
    /**
     * Get Deep Link Parameters*/
    dplink_tid = intent?.extras?.getString(ARGS_DEEPLINK_ID) ?:""
    dplink_type = intent?.extras?.getString(ARGS_DEEPLINK_TYPE) ?:""
    fromLink = false
    fromNotification=false
    fromDeepLink=false
    fromNotificationContract= false
    fromDeepLinkContract = false
    fragmentType = intent?.extras?.getString(IntentExtraFragmentTypeKey)
    if (fragmentType.isNotNullOrEmpty() && fragmentType == "pod") {
      analyticsUtil.moEngageTrackEvent(
        EVENT_NAVIGATION_PODS
      )
      userPrefs.setPreviousScreen(this.javaClass.name)
      fragmentAction(NavigateHomeFragmentAction(PodFragment))
    }
    processDeepLink()
    if (transactions.isNotEmpty())
      transactionIds = transactions.split(",")
        .map { it.trim() }
    viewModel.fromNotification = true
    if (notificationId.isNotEmpty()) {
      processNotification()
    }
  }

  private fun setUserAttributes() {

    userPrefs.userId().let {
      analyticsUtil.moEngageUserAttribute(USER_PROPERTY_UUID,it)
      analyticsUtil.moEngageUserAttribute(MoEConstants.USER_ATTRIBUTE_UNIQUE_ID,it)
    }

    userPrefs.phoneNumber?.let {
      analyticsUtil.moEngageUserAttribute(MoEConstants.USER_ATTRIBUTE_USER_MOBILE,it)
      analyticsUtil.moEngageUserAttribute(USER_PROPERTY_PHONE_NO,it)
    }
    userPrefs.cityName?.let {
      analyticsUtil.moEngageUserAttribute(USER_PROPERTY_BASE_CITY,it)
    }
    if(userPrefs.demandType.isNotNullOrEmpty()){
      analyticsUtil.moEngageUserAttribute(USER_PROPERTY_DEMAND_TYPE,userPrefs.demandType!!)
    }
    userPrefs.companyName?.let {
      if(userPrefs.companyName.isNotNullOrEmpty())
        analyticsUtil.moEngageUserAttribute(USER_PROPERTY_COMPANY_NAME,it)
    }
    userPrefs.ownTrucks?.let {
      analyticsUtil.moEngageUserAttribute(USER_PROPERTY_OWNS_TRUCKS,it.toString())
    }
    userPrefs.status?.let {
      if(userPrefs.status.isNotNullOrEmpty())
        analyticsUtil.moEngageUserAttribute(USER_PROPERTY_STATUS,it)
    }
    userPrefs.subStatus?.let{
      if(userPrefs.subStatus.isNotNullOrEmpty())
        analyticsUtil.moEngageUserAttribute(USER_PROPERTY_SUB_STATUS,it)
    }
    userPrefs.isKycVeriifed?.let {
      analyticsUtil.moEngageUserAttribute(USER_PROPERTY_IS_KYC_VERIFIED,it.toString())
    }
    userPrefs.receiveWhatsappNotifications?.let {
      analyticsUtil.moEngageUserAttribute(USER_PROPERTY_RECEIVE_WHATSAPP_NOTIFICATIONS,it.toString())
    }

    userPrefs.creationDate?.let {
      if(userPrefs.creationDate.isNotNullOrEmpty())
        analyticsUtil.moEngageUserAttribute(USER_PROPERTY_CREATION_DATE,DateUtils.getUtcToIstFormatTime(it)!!)
    }

    userPrefs.userName?.let {
      if(userPrefs.userName.isNotNullOrEmpty()){
        try {
          analyticsUtil.moEngageUserAttribute(USER_PROPERTY_NAME, it)
          analyticsUtil.moEngageUserAttribute(MoEConstants.USER_ATTRIBUTE_USER_NAME, it)
          analyticsUtil.moEngageUserAttribute(
            MoEConstants.USER_ATTRIBUTE_USER_FIRST_NAME,
            it.split(" ").get(0)
          )
          analyticsUtil.moEngageUserAttribute(
            MoEConstants.USER_ATTRIBUTE_USER_LAST_NAME,
            it.split(" ").get(1)
          )
        }catch (e:Exception){}
      }
    }
  }

  override fun onResume() {
    super.onResume()
    appUpdateManager
      .appUpdateInfo
      .addOnSuccessListener { appUpdateInfo ->

        // If the update is downloaded but not installed,
        // notify the user to complete the update.
        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
          popupSnackbarForCompleteUpdate()
        }

        //Check if Immediate update is required
        try {
          if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            // If an in-app update is already running, resume the update.

            appUpdateManager.startUpdateFlowForResult(
              appUpdateInfo,
              AppUpdateType.IMMEDIATE,
              this,
              APP_UPDATE_REQUEST_CODE)

          }
        } catch (e: IntentSender.SendIntentException) {
          e.printStackTrace()
        }
      }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == APP_UPDATE_REQUEST_CODE) {
      if (resultCode != Activity.RESULT_OK) {
        Toast.makeText(this, "App Update failed, please try again on the next app launch", Toast.LENGTH_SHORT).show() }
    }
  }

  override fun markNotificationRead() {
    super.markNotificationRead()
    analyticsUtil.trackEvent(
      EVENT_NOTIFICATION_OPEN,
      mutableListOf(PROPERTY_USER_ID, PROPERTY_NOTIFICATION_TYPE, PROPERTY_OVERALL_PERFORMANCE),
      mutableListOf(userPrefs.userId() , notificationType, userPrefs.userPerformance)
    )
    viewModel.markNotificationRead(notificationId)
  }
  private fun observeFragmentLiveData(pos: Int = 0) {
    val fragment = (pagerAdapter.getItem(pos) as HomeBaseFragment)
    val elevationLiveData: MutableLiveData<Float>? = fragment.toolbarElevationLiveData
    if (elevationLiveData == null) {
      /* default toolbar elevation */
      ViewCompat.setElevation(binding.toolbar, resources.getDimension(R.dimen.toolbar_elevation))
      binding.toolbarTitle.text = title
    } else {
      elevationLiveData.observe(this, Observer {
        binding.toolbarTitle.text = title
        ViewCompat.setElevation(
          binding.toolbar,
          it ?: resources.getDimension(R.dimen.toolbar_elevation)
        )
      })
    }
  }
  /**
   * Fragment action observer
   */
  fun fragmentAction(action: BaseHomeFragmentAction) {
    when (action.type) {
      /* navigate to fragment action */
      HomeFragmentActionType.Navigate -> {
        val fragmentType = (action as NavigateHomeFragmentAction).fragmentType
        binding.viewpager.setCurrentItem(fragmentType.position, true)
        binding.toolbarTitle.text = title
      }
    }
  }
  override fun onNavigationItemSelected(item: MenuItem) = HomeFragmentType.posById(item.itemId)
      .let{ pos ->
        count++
        when(pos){
          0->
            if(count==1){
              if(userPrefs.userPreviousScreen==StartRoutingActivity::class.java.name){
                userPrefs.previousNavigationTab= StartRoutingActivity::class.java.name
              } else if(userPrefs.userPreviousScreen==VendorPolicyActivity::class.java.name){
                userPrefs.previousNavigationTab= VendorPolicyActivity::class.java.name
              }else{
                userPrefs.previousNavigationTab= userPrefs.currentNavigationTab
              }
              userPrefs.currentNavigationTab = HomeLoadsFragment::class.java.name
              userPrefs.setPreviousScreen(userPrefs.previousNavigationTab)
            analyticsUtil.moEngageTrackEvent(
              EVENT_NAVIGATION_HOME,
                mutableListOf(PROPERTY_ORDER_COUNT),
                mutableListOf(userPrefs.loadCount))
            }
          1->
            if(count==1){
              userPrefs.previousNavigationTab= userPrefs.currentNavigationTab
              userPrefs.currentNavigationTab = HomeBidsFragment::class.java.name
              userPrefs.setPreviousScreen(userPrefs.previousNavigationTab)
              analyticsUtil.moEngageTrackEvent(
              EVENT_NAVIGATION_MY_BIDS,
                mutableListOf(PROPERTY_TOTAL_BIDS_COUNT, PROPERTY_ACTIVE_BIDS_COUNT,
                    PROPERTY_CONFIRMED_BIDS_COUNT, PROPERTY_LOST_BIDS_COUNT),
                mutableListOf(userPrefs.totalBidCount,userPrefs.activeBidCount,userPrefs.confirmedBidCount,userPrefs.lostBidCount)
            )

            }
          2->
            if(count==1){
              userPrefs.previousNavigationTab= userPrefs.currentNavigationTab
              userPrefs.currentNavigationTab = HomePodsFragment::class.java.name
              userPrefs.setPreviousScreen(userPrefs.previousNavigationTab)

              analyticsUtil.moEngageTrackEvent(
              EVENT_NAVIGATION_PODS
            )
            }
          3->
            if(count==1){
              userPrefs.previousNavigationTab= userPrefs.currentNavigationTab
              userPrefs.currentNavigationTab = HomeTripsFragment::class.java.name
              userPrefs.setPreviousScreen(userPrefs.previousNavigationTab)
              analyticsUtil.moEngageTrackEvent(
              EVENT_NAVIGATION_MY_TRIPS,
                mutableListOf(PROPERTY_AWAITING_ARRIVAL_COUNT),
                mutableListOf(userPrefs.awaitingArrivalCount)
            )
            }
        }
        if(count==2){
          count=0
        }
        binding.viewpager.apply {
          uiUtils.toggleKeyboard()
          if (pos != -1 && currentItem != pos) {
            this@HomeActivity.title = HomeFragmentType.pos(pos)
                ?.fragment?.title
            setCurrentItem(pos, true)
          }
          binding.toolbarTitle.text = title
        }
        pos != -1
      }

  override fun messageClicked(p0: InAppMessage, p1: Action) {
    val url: String? = p1.actionUrl
    val metadata: CampaignMetadata? = p0.campaignMetadata
    Log.d("parameters",url+metadata.toString())
    userPrefs.startTime = Date().time
    analyticsUtil.trackEvent(
      EVENT_DEEP_LINK_ADD_FUEL_PAYMENT,
      mutableListOf(PROPERTY_USER_ID),
      mutableListOf(userPrefs.userId())
    )
    userPrefs.setPreviousScreen(this.javaClass.name)
    startActivity(userTripsIntent(this, "payment_view", 0))
  }
}
/**
 * Provides title from all fragments to activity
 */
interface TitleProvider {
  val title: CharSequence
}
enum class FragmentName(
  val position: Int,
  val frgName: String
) {
  HomeFragment(0, "home_screen"),
  BidsFragment(1, "bids_screen"),
  TripsFragment(3, "trips_screen" ),
  ProfileFragment(4, "profile_screen"),
  PODFragment(2, "pod_screen"),
  Unknown(-1, "unknown");
  companion object {
    /**
     * Get FragmentName
     */
    fun fragmentName(pos: Int) = values().firstOrNull { it.position == pos }
      ?: Unknown
  }
}
private const val SUBMIT_POD_NOTIFICATION = "submit_pod_notification"
private const val PREFERRED_SUPPLIER_NOTIFICATION = "preferred_supplier_notification"
private const val REJECT_POD_NOTIFICATION = "reject_pod_notification"
private const val LOWEST_BID_NOTIFICATION = "lower_bid_notification"
private const val LANE_PREFERENCE_UPDATE_NOTIFICATION = "lane_preference_update"
private const val REDIRECT_TO_TRIP = "redirect_to_trip"
private const val REDIRECT_TO_LOAD ="redirect_to_load"
private const val REDIRECT_TO_CONTRACT ="redirect_to_contract"
private const val REDIRECT_TO_CONTRACT_LIST ="redirect_to_contract_list"
private const val ACTIVATE_TRUCK_NOTIFICATION = "vehicle_about_to_reach_destination_notification"
private const val TRUCK_REACHED_NOTIFICATION = "truck_reached_notification"
private const val REDIRECT_TO_TRUCKS = "truck_unloaded_notification"
const val OFFER_LANE_UPLOADED = "offer_lane_uploaded"
const val OFFER_APPROVED = "offer_approved"
const val OFFER_REJECTED = "offer_rejected"
private const val REDIRECT_TO_SUPPLIER_RECOMMENDATION = "supplier_recommendation_notification"


private const val ROUTE_PREFERENCES_REDIRECT = "rtprfs"
private const val TEAM_MEMBERS_REDIRECT = "tmbrs"
private const val PAYMENT_SUMMARY_REDIRECT = "pmtsmry"
private const val PHYSICAL_POD_PENDING_REDIRECT = "pylpodtrp"
private const val EPOD_PENDING_REDIRECT = "epodtrp"
private const val DOWNLOAD_LEDGER_POPUP_REDIRECT = "dnldldgr"
private const val TRIP_DETAIL_REDIRECT = "trpdtl"
private const val LOAD_DETAIL_REDIRECT = "biddtl"
private const val ADVANCE_PENDING_REDIRECT = "advpend"
private const val MY_TRUCKS_REDIRECT = "mytrucks"
private const val ACTIVATE_TRUCK_REDIRECT = "actvatrks"
private const val KYC_REJECTION = "kycrejected"
private const val KYC_VERIFIED = "kycverified"
private const val SUPPLIER_LOAD_REDIRECT = "rectransdtl"
private const val SHARE_RATE = "sharerate"
private const val ADD_TRUCK_REDIRECT = "add_truck"
private const val CONTRACT_LIST = "contractlst"
private const val CONTRACT_DETAILS = "contractdtl"



public const val OFF_SET_LIMIT = 500


var orderRank=0

/* intent keys */
private const val IntentExtraFragmentTypeKey = "fragment_type"
/**
 * Trip details intent
 */
fun homeActivityIntent(
  fragmentType: String,
  context: Context
) = Intent(context, HomeActivity::class.java).apply {
  putExtra(IntentExtraFragmentTypeKey, fragmentType)
}
