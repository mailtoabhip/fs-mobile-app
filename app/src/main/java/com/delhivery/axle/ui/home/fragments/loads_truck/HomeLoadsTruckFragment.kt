package com.delhivery.axle.ui.home.fragments.loads_truck

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentHomeLoadsTruckBinding
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.*
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFragment
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksFragment
import com.delhivery.axle.utils.EVENT_HOME_LOADS_TAB_CLICK
import com.delhivery.axle.utils.EVENT_HOME_MY_TRUCKS_TAB_CLICK
import com.delhivery.axle.utils.PROPERTY_INVENTORY_COUNT
import com.delhivery.axle.utils.PROPERTY_ORDER_COUNT
import com.delhivery.axle.utils.EVENT_VIEW_MY_TRUCK_OFFERS
import com.delhivery.axle.utils.PROPERTY_DATE
import com.delhivery.axle.utils.PROPERTY_NUMBER_OF_OFFERS
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.tabs.TabLayout
import java.util.Date
import javax.inject.Inject
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.delhivery.axle.api.repository.ContractType
import com.delhivery.axle.api.repository.DemandType
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsFragment
import com.delhivery.axle.utils.EVENT_HOME_CONTRACT_TAB_CLICK
import com.delhivery.axle.utils.PROPERTY_CONTRACT_TYPE
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

class HomeLoadsTruckFragment : HomeBaseFragment<FragmentHomeLoadsTruckBinding, HomeLoadsTruckViewModel>(),
    TitleProvider, UpdateTabCountAndBadgeInterface{

    var _title: String = "Loads"
    var vehicleNo = ""
    var fromNotification = false
    var fromDeepLink = false
    var fromNotificationContract = false
    var fromContractDeepLink = false
    private var fragmentSetupTrace: Trace? = null
    private var isFirstResume = true
    override val title: CharSequence
        get() = _title
    @Inject lateinit var userPrefs: UserPrefs


    override fun getViewModelClass() = HomeLoadsTruckViewModel::class.java

    override fun layoutId() = R.layout.fragment_home_loads_truck


    companion object {
        /* singleton instance */
        val _instance: HomeLoadsTruckFragment by lazy { HomeLoadsTruckFragment() }
    }

    /* home fragments pager adapter */
    private lateinit var pagerAdapter : HomeLoadsTruckFragmentsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomeLoadsTruckFragment_SetupTime")
        fragmentSetupTrace?.start()
        pagerAdapter= HomeLoadsTruckFragmentsAdapter(childFragmentManager)

        val activity: HomeActivity? = activity as HomeActivity?
        binding.viewpager.apply {
            offscreenPageLimit = HomeLoadsTruckFragmentType.count()
            adapter = pagerAdapter
            }
        binding.tabLayout.setupWithViewPager(binding.viewpager)
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
              if(tab?.position==1){
                    val c = Date()
                    val date = c.toString()
                    analyticsUtil.moEngageTrackEvent(
                        EVENT_VIEW_MY_TRUCK_OFFERS,
                        mutableListOf(
                            PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_NUMBER_OF_OFFERS,
                            PROPERTY_DATE
                        ),
                        mutableListOf(userPrefs.userId() , userPrefs.phoneNumber!!, userPrefs.bidOfferCount.toString(),date)
                    )
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        val tab1 =   binding.tabLayout.getTabAt(0)
        val tab2 =   binding.tabLayout.getTabAt(1)
        val tab3 =   binding.tabLayout.getTabAt(2)
        Log.d("observedCount1",userPrefs.fullLoadCount + userPrefs.contractCount.toString())

        tab1?.setCustomView(R.layout.badge_tab)?.setText("Loads")?.view?.isSelected = true
        if(tab1?.isSelected == true){
            tab1?.customView?.findViewById<TextView>(android.R.id.text1)?.let { textView ->
                textView.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorDelhiveryRed))
            }
            binding.tabLayout.getTabAt(0)?.customView?.findViewById<TextView>(R.id.tvCount)?.let { textView ->
                textView.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorDelhiveryRed))
            }
        }
//            ?.setIcon(R.drawable.ic_load_home_icon)

        tab2?.setCustomView(R.layout.badge_tab)?.setText("Contracts")
//            ?.setIcon(R.drawable.ic_contract_icon)

        tab3?.setCustomView(R.layout.badge_tab)?.setText("My Bids")
//            ?.setIcon(R.drawable.ic_home_truck_icon)

//            ?.setIcon(R.drawable.ic_home_truck_icon)


        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<TextView>(android.R.id.text1)?.let { textView ->
                    textView.setTextColor(ContextCompat.getColor(context!!,R.color.colorDelhiveryRed))
                }
                tab?.customView?.findViewById<TextView>(R.id.tvCount)?.let { textView ->
                    textView.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorDelhiveryRed))
                }
                if(tab?.position==0){
                    userPrefs.currentNavigationTab = HomeLoadsFragment::class.java.name
                    userPrefs.setPreviousScreen(HomeTrucksFragment::class.java.name)
                    analyticsUtil.moEngageTrackEvent(EVENT_HOME_LOADS_TAB_CLICK, mutableListOf(
                        PROPERTY_ORDER_COUNT),
                        mutableListOf(userPrefs.loadCount))
                }else if(tab?.position==1){
                    userPrefs.currentNavigationTab = HomeTrucksFragment::class.java.name
                    userPrefs.setPreviousScreen(HomeLoadsFragment::class.java.name)
                    analyticsUtil.moEngageTrackEvent(EVENT_HOME_MY_TRUCKS_TAB_CLICK,mutableListOf(
                        PROPERTY_INVENTORY_COUNT),
                        mutableListOf(userPrefs.inventoryCount))
                }else if(tab?.position==2){
                    userPrefs.currentNavigationTab = HomeContractsFragment::class.java.name
                    userPrefs.setPreviousScreen(HomeLoadsFragment::class.java.name)
                    analyticsUtil.moEngageTrackEvent(EVENT_HOME_CONTRACT_TAB_CLICK,mutableListOf(PROPERTY_USER_ID,
                        PROPERTY_PHONE_NO,
                        PROPERTY_CONTRACT_TYPE),
                        mutableListOf(userPrefs.userId(),userPrefs.phoneNumber?:"",if((userPrefs.demandType.contains(
                                DemandType.Internal.type)&&userPrefs.contractDemand)||userPrefs.demandType.contains(
                                DemandType.Others.type) ){ ContractType.FRC.type }else if (userPrefs.demandType.contains(
                                DemandType.Intracity.type)&& userPrefs.contractDemand){
                            ContractType.INTRACITY.type} else {
                            ContractType.FRC.type}))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<TextView>(android.R.id.text1)?.let { textView ->
                    textView.setTextColor(ContextCompat.getColor(context!!,R.color.color_hint))
                }
                tab?.customView?.findViewById<TextView>(R.id.tvCount)?.let { textView ->
                    textView.setTextColor(ContextCompat.getColor(requireContext(),R.color.color_hint))
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })


    }


    override fun onResume() {
        super.onResume()
        if (fragmentSetupTrace != null && isFirstResume) {
            fragmentSetupTrace?.stop()
            isFirstResume = false
        }
    }
    fun fragmentAction(action: BaseHomeFragmentAction) {
        when (action.type) {
            /* navigate to fragment action */
            HomeFragmentActionType.Navigate -> {
                val fragmentType = (action as NavigateHomeFragmentAction).fragmentType
                binding.viewpager.setCurrentItem(fragmentType.position, true)
            }
            else->{}
        }
    }

    override fun dataToUpdate(type: String,showBadge: Boolean, count: Int) {
        try {
            if (showBadge && type == "contracts") {
                if (binding.tabLayout.getTabAt(1)?.customView != null) {
                    binding.tabLayout.getTabAt(1)?.customView?.findViewById<TextView>(R.id.tvCount)?.text =
                       if(count>0) " ($count)" else ""
                    binding.tabLayout.getTabAt(1)?.customView?.findViewById<ImageView>(R.id.badge)?.visibility =
                        View.GONE
                }
            } else if (showBadge && type == "loads") {
                if (binding.tabLayout.getTabAt(0)?.customView != null) {
                    binding.tabLayout.getTabAt(0)?.customView?.findViewById<TextView>(R.id.tvCount)?.text =
                        if(count>0) " ($count)" else ""
                    binding.tabLayout.getTabAt(0)?.customView?.findViewById<ImageView>(R.id.badge)?.visibility =
                        View.GONE
                }
            }
        }catch (e:Exception){

        }
    }

}
interface UpdateTabCountAndBadgeInterface{

    fun dataToUpdate(type:String,showBadge:Boolean, count:Int)
}







