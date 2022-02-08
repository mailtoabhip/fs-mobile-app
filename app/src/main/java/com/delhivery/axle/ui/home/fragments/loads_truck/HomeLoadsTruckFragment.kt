package com.delhivery.axle.ui.home.fragments.loads_truck

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentHomeLoadsTruckBinding
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.*
import com.delhivery.axle.utils.extensions.onPageSelected
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.android.synthetic.main.fragment_home_loads_truck.*
import javax.inject.Inject


class HomeLoadsTruckFragment : HomeBaseFragment<FragmentHomeLoadsTruckBinding, HomeLoadsTruckViewModel>(),
    TitleProvider{

    var _title: String = "Home"

    override val title: CharSequence
        get() = _title

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

        if(activity!!.fromLink){
            binding.tabLayout.getTabAt(1)?.select()
            activity.fromLink = false
        }
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

