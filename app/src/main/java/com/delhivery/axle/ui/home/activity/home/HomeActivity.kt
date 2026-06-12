package com.delhivery.axle.ui.home.activity.home
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityHomeBinding
import com.delhivery.axle.fcm.ARGS_OFFER_ID
import com.delhivery.axle.fcm.ARGS_PREFERRED_TRANSACTION_ID
import com.delhivery.axle.fcm.ARGS_PRICING_ID
import com.delhivery.axle.fcm.ARGS_PRICING_SORT_KEY
import com.delhivery.axle.fcm.ARGS_TRANSACTION_IDS
import com.delhivery.axle.fcm.ARGS_VEHICLE_NUMBER
import com.delhivery.axle.ui.auth.AccountDeletionActivity
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.fastag.tagMapping.TagMappingActivity
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentsAdapter
import com.delhivery.axle.ui.home.fragments.HomeFragmentType
import com.delhivery.axle.ui.profile.MyProfileActivity
import com.delhivery.axle.utils.DateUtils
import com.delhivery.axle.utils.EVENT_CALL_VENDOR_DESK
import com.delhivery.axle.utils.EVENT_NAVIGATION_MY_PROFILE
import com.delhivery.axle.utils.PROPERTY_PAGE_NAME
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.USER_PROPERTY_BASE_CITY
import com.delhivery.axle.utils.USER_PROPERTY_COMPANY_NAME
import com.delhivery.axle.utils.USER_PROPERTY_CREATION_DATE
import com.delhivery.axle.utils.USER_PROPERTY_DEMAND_TYPE
import com.delhivery.axle.utils.USER_PROPERTY_IS_KYC_VERIFIED
import com.delhivery.axle.utils.USER_PROPERTY_NAME
import com.delhivery.axle.utils.USER_PROPERTY_OWNS_TRUCKS
import com.delhivery.axle.utils.USER_PROPERTY_PHONE_NO
import com.delhivery.axle.utils.USER_PROPERTY_RECEIVE_WHATSAPP_NOTIFICATIONS
import com.delhivery.axle.utils.USER_PROPERTY_STATUS
import com.delhivery.axle.utils.USER_PROPERTY_SUB_STATUS
import com.delhivery.axle.utils.USER_PROPERTY_UUID
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.snackbar.Snackbar
import com.moengage.core.internal.USER_ATTRIBUTE_UNIQUE_ID
import com.moengage.core.internal.USER_ATTRIBUTE_USER_FIRST_NAME
import com.moengage.core.internal.USER_ATTRIBUTE_USER_LAST_NAME
import com.moengage.core.internal.USER_ATTRIBUTE_USER_MOBILE
import com.moengage.core.internal.USER_ATTRIBUTE_USER_NAME
import javax.inject.Inject


/**
 * Home screen
 */
class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>() {
  override fun getViewModelClass() = HomeViewModel::class.java
  override fun layoutId() = R.layout.activity_home
  override fun requireConnection() = true
  var fragmentType : String ?= ""
  var vehicleNum =""
  var count =0
  @Inject lateinit var userPrefs : UserPrefs
  /* home fragments pager adapter */
  private val pagerAdapter: HomeFragmentsAdapter by lazy {
    HomeFragmentsAdapter(supportFragmentManager)
  }

  private var backPressedOnce = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        if (backPressedOnce) {
          finish()
          return
        }
        backPressedOnce = true
        Snackbar.make(
          binding.root,
          "Press back again to exit",
          com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
          backPressedOnce = false
        }, 2000)
      }
    })
    val transactions = intent?.extras?.getString(ARGS_TRANSACTION_IDS) ?: ""
    if (transactions.isNotEmpty())
      transactionIds = transactions.split(",")
        .map { it.trim() }
    preferredTransactionId = intent?.extras?.getString(ARGS_PREFERRED_TRANSACTION_ID) ?: ""
    //For inventory
    vehicleNumber = intent?.extras?.getString(ARGS_VEHICLE_NUMBER) ?: ""
      //For pricing
    pricingId = intent?.extras?.getString(ARGS_PRICING_ID) ?: ""
    pricingSortKey = intent?.extras?.getString(ARGS_PRICING_SORT_KEY) ?: ""
    pricingOfferId = intent?.extras?.getString(ARGS_OFFER_ID) ?: ""

    fragmentType = intent?.extras?.getString(IntentExtraFragmentTypeKey)
    viewModel.getUserDetails()
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup viewpager immediately so content is visible */
    binding.viewpager.apply {
      offscreenPageLimit = OFF_SET_LIMIT
      adapter = pagerAdapter
      disableScroll(true)  // Disable swipe since only HomeFragment is available
    }

    /* Setup profile click listener immediately - don't gate on API success */
    binding.profile.setOnClickListener {
      userPrefs.setPreviousScreen(this.javaClass.name)
      analyticsUtil.moEngageTrackEvent(
        EVENT_NAVIGATION_MY_PROFILE
      )
      navigationUtils.navigate(MyProfileActivity::class.java, false)
    }

    viewModel.userUpdateLiveData.observe(this, Observer {
      if(it) {
        if (userPrefs.requestedDeletion) {
          navigationUtils.navigate(AccountDeletionActivity::class.java,true)
        }else if(userPrefs.returningFromDeletion){
          userPrefs.clearPrefs()
          navigationUtils.logout("Please login to create account","fromUser")
        } else {
          setUserAttributes()
          navigationUtils.navigateOnboardingDetails()
          /* setup toolbar */
          setSupportActionBar(binding.toolbar)
          title = ""
//          if (!userPrefs.userName.isEmpty()) {
//            binding.profile.text = userPrefs.userName[0].uppercase().toString()
//          }
          supportActionBar?.setDisplayShowTitleEnabled(false)
          binding.toolbarTitle.text = title
          
          /* Handle window insets for edge-to-edge display (API 35+) */
          if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
          }

          observeFragmentLiveData()

          binding.profile.setOnClickListener {
            userPrefs.setPreviousScreen(this.javaClass.name)
            analyticsUtil.moEngageTrackEvent(
              EVENT_NAVIGATION_MY_PROFILE
            )
              navigationUtils.navigate(MyProfileActivity::class.java, false)
          }
        }
      }
    })

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

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_call, menu)
    return true
  }
  override fun onPrepareOptionsMenu(menu: Menu): Boolean {
    super.onPrepareOptionsMenu(menu)
      menu.findItem(R.id.nav_call).isVisible = true
      menu.findItem(R.id.nav_filter).isVisible = false
   // menu.findItem(R.id.nav_filter).isVisible = false
    return true
  }
  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.nav_call -> {
        //Capture Event
        analyticsUtil.moEngageTrackEvent(
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
    val transactions = intent?.extras?.getString(ARGS_TRANSACTION_IDS) ?: ""
    preferredTransactionId = intent?.extras?.getString(ARGS_PREFERRED_TRANSACTION_ID) ?: ""
    //For Inventory
    vehicleNumber = intent?.extras?.getString(ARGS_VEHICLE_NUMBER) ?: ""

    pricingId = intent?.extras?.getString(ARGS_PRICING_ID) ?: ""
    pricingSortKey = intent?.extras?.getString(ARGS_PRICING_SORT_KEY) ?: ""
    pricingOfferId = intent?.extras?.getString(ARGS_OFFER_ID) ?: ""
    fragmentType = intent?.extras?.getString(IntentExtraFragmentTypeKey)
    if (transactions.isNotEmpty())
      transactionIds = transactions.split(",")
        .map { it.trim() }
  }

  private fun setUserAttributes() {

    userPrefs.userId().let {
      analyticsUtil.moEngageUserAttribute(USER_PROPERTY_UUID,it)
      analyticsUtil.moEngageUserAttribute(USER_ATTRIBUTE_UNIQUE_ID,it)
    }

    userPrefs.phoneNumber?.let {
      analyticsUtil.moEngageUserAttribute(USER_ATTRIBUTE_USER_MOBILE,it)
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
          analyticsUtil.moEngageUserAttribute(USER_ATTRIBUTE_USER_NAME, it)
          analyticsUtil.moEngageUserAttribute(
            USER_ATTRIBUTE_USER_FIRST_NAME,
            it.split(" ").get(0)
          )
          analyticsUtil.moEngageUserAttribute(
            USER_ATTRIBUTE_USER_LAST_NAME,
            it.split(" ").get(1)
          )
        }catch (e:Exception){}
      }
    }
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

  override fun popupSnackbarForCompleteUpdate() {
    val snackbar = Snackbar.make(findViewById(android.R.id.content), "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE)
    snackbar.setAction("RESTART") { appUpdateManager.completeUpdate() }
    snackbar.show()
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
  ProfileFragment(5, "profile_screen"),
  Unknown(-1, "unknown");
  companion object {
    /**
     * Get FragmentName
     */
    fun fragmentName(pos: Int) = values().firstOrNull { it.position == pos }
      ?: Unknown
  }
}




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
