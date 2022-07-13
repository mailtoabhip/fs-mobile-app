package com.delhivery.axle.ui.home.fragments.loads_truck

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
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
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_home_loads_truck.*
import kotlinx.android.synthetic.main.view_home_summary_item.loads_count
import javax.inject.Inject

class HomeLoadsTruckFragment : HomeBaseFragment<FragmentHomeLoadsTruckBinding, HomeLoadsTruckViewModel>(),
    TitleProvider{

    var _title: String = "Home"
    var vehicleNo = ""
    var fromNotification = false
    var fromDeepLink = false
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

        pagerAdapter= HomeLoadsTruckFragmentsAdapter(childFragmentManager)

        val activity: HomeActivity? = activity as HomeActivity?
        binding.viewpager.apply {
            offscreenPageLimit = HomeLoadsTruckFragmentType.count()
            adapter = pagerAdapter
            }
        binding.tabLayout.setupWithViewPager(binding.viewpager)

        binding.tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_loads_home)
        binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_my_truck)

        if(activity!!.fromDeepLink){
            binding.tabLayout.getTabAt(1)?.select()
            activity.fromDeepLink = false
            if(activity.vehicleNum.isNotEmpty())
               vehicleNo = activity.vehicleNum
            fromDeepLink=true
        }

        if(activity.fromNotification){
            binding.tabLayout.getTabAt(1)?.select()
            activity.fromNotification = false
            if(activity.vehicleNum.isNotEmpty())
                vehicleNo = activity.vehicleNum
            fromNotification=true
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab?.position==0){
                    userPrefs.setPreviousScreen(HomeTrucksFragment::class.java.name)
                    analyticsUtil.moEngageTrackEvent(EVENT_HOME_LOADS_TAB_CLICK, mutableListOf(
                        PROPERTY_ORDER_COUNT),
                        mutableListOf(userPrefs.loadCount))
                }else if(tab?.position==1){
                    userPrefs.setPreviousScreen(HomeLoadsFragment::class.java.name)
                    analyticsUtil.moEngageTrackEvent(EVENT_HOME_MY_TRUCKS_TAB_CLICK,mutableListOf(
                        PROPERTY_INVENTORY_COUNT),
                        mutableListOf(userPrefs.inventoryCount))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }


    fun fragmentAction(action: BaseHomeFragmentAction) {
        when (action.type) {
            /* navigate to fragment action */
            HomeFragmentActionType.Navigate -> {
                val fragmentType = (action as NavigateHomeFragmentAction).fragmentType
                binding.viewpager.setCurrentItem(fragmentType.position, true)
            }
        }
    }

}






