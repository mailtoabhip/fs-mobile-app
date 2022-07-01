package com.delhivery.axle.ui.home.activity.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityHomeBinding
import com.delhivery.axle.fcm.*
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.bids.userTripsIntent
import com.delhivery.axle.ui.home.fragments.*
import com.delhivery.axle.ui.home.fragments.HomeFragmentType.*
import com.delhivery.axle.ui.ledger.consolidatedPageIntent
import com.delhivery.axle.ui.profile.MyProfileActivity
import com.delhivery.axle.ui.team.teamMembersIntent
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.ui.userroutes.userRoutesIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onPageSelected
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.FirebaseInAppMessagingClickListener
import com.google.firebase.inappmessaging.model.Action
import com.google.firebase.inappmessaging.model.CampaignMetadata
import com.google.firebase.inappmessaging.model.InAppMessage
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
  var vehicleNum =""
  @Inject lateinit var userPrefs : UserPrefs

  /* home fragments pager adapter */
  private val pagerAdapter: HomeFragmentsAdapter by lazy {
    HomeFragmentsAdapter(supportFragmentManager)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    notificationId = intent?.extras?.getString(ARGS_NOTIFICATION_ID) ?: ""
    val transactions = intent?.extras?.getString(ARGS_TRANSACTION_IDS) ?: ""
    if (transactions.isNotEmpty())
      transactionIds = transactions.split(",")
          .map { it.trim() }
    notificationType = intent?.extras?.getString(ARGS_NOTIFICATION_TYPE) ?: ""
    preferredTransactionId = intent?.extras?.getString(ARGS_PREFERRED_TRANSACTION_ID) ?: ""

    //For inventory
    vehicleNumber = intent?.extras?.getString(ARGS_VEHICLE_NUMBER) ?: ""

    fragmentType = intent?.extras?.getString(IntentExtraFragmentTypeKey)

    dplink_tid = intent?.extras?.getString(ARGS_DEEPLINK_ID) ?:""
    dplink_type = intent?.extras?.getString(ARGS_DEEPLINK_TYPE) ?:""

    fromLink = false
    fromNotification = false
    fromDeepLink = false

    viewModel.getUserDetails()

  }

  override fun onBackPressed() {
    super.onBackPressed()
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    viewModel.userUpdateLiveData.observe(this, Observer {
      if(it){
        navigationUtils.navigateOnboardingSteps(true)


    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Load Requests"

    if(!userPrefs.userName.isEmpty()) {
      binding.profile.text = userPrefs.userName[0].toUpperCase().toString()
    }
    supportActionBar?.setDisplayShowTitleEnabled(false)

    binding.toolbarTitle.text = title

    /* setup view pager */
    binding.viewpager.apply {
      offscreenPageLimit = HomeFragmentType.count()
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
      fragmentAction(NavigateHomeFragmentAction(PodFragment))
    }

    binding.profile.setOnClickListener {
      navigationUtils.navigate(MyProfileActivity::class.java)
    }

    /**
     * Process Deep Link */
    processDeepLink()
      }
    })
  }

  private fun processDeepLink() {

    Log.d("noti", "$dplink_type $dplink_tid")
    if (dplink_type != "") {
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
            startActivity(bidDetailsIntent(dplink_tid, this))
          } else {
            fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
          }
        }

        SUPPLIER_LOAD_REDIRECT -> {
          if (dplink_tid != "") {
            startActivity(bidDetailsIntent(dplink_tid, this))
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
        KYC_REJECTION ->{
          navigationUtils.navigate(MyProfileActivity::class.java)
        }
        KYC_VERIFIED ->{
          fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
        }
        else -> {
          fragmentAction(NavigateHomeFragmentAction(LoadsTruckFragment))
        }
      }
    }
  }

  private fun processNotification() {
    Log.d("noti", "$notificationType$notificationId $vehicleNumber")
    markNotificationRead()
    when (notificationType) {
      SUBMIT_POD_NOTIFICATION -> {
        if (!transactionIds.isNullOrEmpty() && transactionIds.size == 1) {
          startActivity(tripDetailsIntent(transactionIds[0], this))
        } else {
          fragmentAction(NavigateHomeFragmentAction(PodFragment))
        }
      }
      PREFERRED_SUPPLIER_NOTIFICATION -> {
        startActivity(bidDetailsIntent(preferredTransactionId, this))
      }
      REJECT_POD_NOTIFICATION -> {
        startActivity(tripDetailsIntent(preferredTransactionId, this))
      }
      LOWEST_BID_NOTIFICATION -> {
        if (!transactionIds.isNullOrEmpty() && transactionIds.size == 1) {
          startActivity(bidDetailsIntent(transactionIds[0], this))
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

      REDIRECT_TO_LOAD_DETAIL -> {
        analyticsUtil.trackEvent(
                EVENT_DEEP_LINK_ADD_FUEL_PAYMENT,
                mutableListOf(PROPERTY_USER_ID),
                mutableListOf(userPrefs.userId())
        )
        startActivity(bidDetailsIntent(preferredTransactionId,this))
      }

      REDIRECT_TO_LOAD -> {
        startActivity(bidDetailsIntent(preferredTransactionId,this))
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

    /**
     * Get Deep Link Parameters*/
    dplink_tid = intent?.extras?.getString(ARGS_DEEPLINK_ID) ?:""
    dplink_type = intent?.extras?.getString(ARGS_DEEPLINK_TYPE) ?:""

    fromLink = false
    fromNotification=false
    fromDeepLink=false
    processDeepLink()

    if (transactions.isNotEmpty())
      transactionIds = transactions.split(",")
          .map { it.trim() }
    viewModel.fromNotification = true
    if (notificationId.isNotEmpty()) {
      processNotification()
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
      .let { pos ->
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
private const val ACTIVATE_TRUCK_NOTIFICATION = "vehicle_about_to_reach_destination_notification"
private const val TRUCK_REACHED_NOTIFICATION = "truck_reached_notification"
private const val REDIRECT_TO_TRUCKS = "truck_unloaded_notification"
private const val REDIRECT_TO_LOAD_DETAIL = "supplier_recommendation_notification"


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
private const val SUPPLIER_LOAD_REDIRECT = "sldtl"


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
