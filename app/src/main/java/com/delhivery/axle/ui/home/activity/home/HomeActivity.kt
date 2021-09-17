package com.delhivery.axle.ui.home.activity.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityHomeBinding
import com.delhivery.axle.fcm.*
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.biddetails.bidDetailsIntent
import com.delhivery.axle.ui.home.fragments.BaseHomeFragmentAction
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentActionType
import com.delhivery.axle.ui.home.fragments.HomeFragmentType
import com.delhivery.axle.ui.home.fragments.HomeFragmentType.LoadsFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType.PodFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentsAdapter
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.ui.userroutes.userRoutesIntent
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.extensions.onPageSelected
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener
import javax.inject.Inject

/**
 * Home screen
 */
class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(),
    OnNavigationItemSelectedListener {

  override fun getViewModelClass() = HomeViewModel::class.java

  override fun layoutId() = R.layout.activity_home

  override fun requireConnection() = true

  var fragmentType : String ?= ""

  var dplink_tid : String = ""
  var dplink_type : String = ""

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

    fragmentType = intent?.extras?.getString(IntentExtraFragmentTypeKey)

    dplink_tid = intent?.extras?.getString(ARGS_DEEPLINK_ID) ?:""
    dplink_type = intent?.extras?.getString(ARGS_DEEPLINK_TYPE) ?:""
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Load Requests"

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

    /**
     * Process Deep Link */
    processDeepLink()
  }

  private fun processDeepLink() {
    if (dplink_tid != "" && dplink_type != "") {
      startActivity(tripDetailsIntent(dplink_tid, this))
      }
  }

  private fun processNotification() {
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
        } else {
          fragmentAction(NavigateHomeFragmentAction(LoadsFragment))
        }
      }
      LANE_PREFERENCE_UPDATE_NOTIFICATION -> {
        startActivity(userRoutesIntent(this))
      }
      else -> {
        fragmentAction(NavigateHomeFragmentAction(LoadsFragment))
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_call, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    return when (item?.itemId) {
      R.id.nav_call -> {
        analyticsUtil.trackEvent(
                EVENT_CALL_VENDOR_DESK,
                mutableListOf(PROPERTY_USER_ID),
                mutableListOf(userPrefs.userId())
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

    /**
     * Get Deep Link Parameters*/
    dplink_tid = intent?.extras?.getString(ARGS_DEEPLINK_ID) ?:""
    dplink_type = intent?.extras?.getString(ARGS_DEEPLINK_TYPE) ?:""
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
    } else {
      elevationLiveData.observe(this, Observer {
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
        }
        pos != -1
      }
}

/**
 * Provides title from all fragments to activity
 */
interface TitleProvider {
  val title: CharSequence
}

private const val SUBMIT_POD_NOTIFICATION = "submit_pod_notification"
private const val PREFERRED_SUPPLIER_NOTIFICATION = "preferred_supplier_notification"
private const val REJECT_POD_NOTIFICATION = "reject_pod_notification"
private const val LOWEST_BID_NOTIFICATION = "lower_bid_notification"
private const val LANE_PREFERENCE_UPDATE_NOTIFICATION = "lane_preference_update"

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
