package com.delhivery.axle.ui.home.fragments.pod

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentHomeNewPodBinding
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

/**
 * New Home POD Fragment with ViewPager containing Pending POD and Submitted tabs
 */
class HomeNewPodFragment : HomeBaseFragment<FragmentHomeNewPodBinding, HomePodViewModel>() {

    var _title: String = "PODs"

    override val title: CharSequence
        get() = _title

    private var fragmentSetupTrace: Trace? = null
    private var isFirstResume = true

    companion object {
        val _instance: HomeNewPodFragment by lazy { HomeNewPodFragment() }
    }

    override fun getViewModelClass() = HomePodViewModel::class.java

    override fun layoutId() = R.layout.fragment_home_new_pod

    private lateinit var pagerAdapter: HomeNewPodFragmentsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomeNewPodFragment_SetupTime")
        fragmentSetupTrace?.start()

        pagerAdapter = HomeNewPodFragmentsAdapter(childFragmentManager)

        binding.viewPager.apply {
            offscreenPageLimit = HomeNewPodFragmentType.count()
            adapter = pagerAdapter
        }

        binding.tabLayout.setupWithViewPager(binding.viewPager)

        // Setup tab custom views with counts
        val tab1 = binding.tabLayout.getTabAt(0)
        val tab2 = binding.tabLayout.getTabAt(1)

        tab1?.text = "Pending POD"
        tab2?.text = "Submitted"

        // Set initial selected tab styling
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    updateTabStyle(it, true)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    updateTabStyle(it, false)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        // Set initial tab styling
        tab1?.let { updateTabStyle(it, true) }
        tab2?.let { updateTabStyle(it, false) }
        
        // Observe pod counts from ViewModel
        viewModel.podCountsLiveData.observe(viewLifecycleOwner, Observer { podCounts ->
            podCounts?.let {
                updateTabCounts(it.pending_tab_count, it.submitted_tab_count)
            }
        })
    }
    
    /**
     * Update tab text with counts
     */
    private fun updateTabCounts(pendingCount: Int, submittedCount: Int) {
        val tab1 = binding.tabLayout.getTabAt(0)
        val tab2 = binding.tabLayout.getTabAt(1)
        
        tab1?.text = "Pending POD ($pendingCount)"
        tab2?.text = "Submitted ($submittedCount)"
    }

    private fun updateTabStyle(tab: TabLayout.Tab, isSelected: Boolean) {
        val color = if (isSelected) {
            ContextCompat.getColor(requireContext(), R.color.colorDelhiveryRed)
        } else {
            ContextCompat.getColor(requireContext(), R.color.grey_500)
        }
        // For default tabs (without custom view), find TextView in the view hierarchy
        val tabView = tab.view
        val textView = tabView.findViewById<android.widget.TextView>(android.R.id.text1)
            ?: (tabView as? android.view.ViewGroup)?.getChildAt(1) as? android.widget.TextView
        textView?.setTextColor(color)
    }

    override fun onResume() {
        super.onResume()
        if (fragmentSetupTrace != null && isFirstResume) {
            fragmentSetupTrace?.stop()
            isFirstResume = false
        }
    }
}

/**
 * Fragment types for HomeNewPodFragment
 */
enum class HomeNewPodFragmentType(val position: Int) {
    PendingPod(0),
    Submitted(1);

    companion object {
        fun pos(position: Int) = values().firstOrNull { it.position == position }
        fun count() = values().size
    }

    val fragment: Fragment
        get() = when (this) {
            PendingPod -> PendingPodTabFragment.newInstance()
            Submitted -> SubmittedPodTabFragment.newInstance()
        }
}

/**
 * ViewPager adapter for HomeNewPodFragment
 */
class HomeNewPodFragmentsAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(
    fragmentManager
) {
    override fun getCount() = HomeNewPodFragmentType.count()

    override fun getItem(position: Int) = HomeNewPodFragmentType.pos(position)!!.fragment

    override fun getPageTitle(position: Int) = when (HomeNewPodFragmentType.pos(position)) {
        HomeNewPodFragmentType.PendingPod -> "Pending POD"
        HomeNewPodFragmentType.Submitted -> "Submitted"
        else -> ""
    }
}
