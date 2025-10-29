package com.delhivery.axle.ui.home.fragments.placements

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentHomePlacementsV3Binding
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomePlacementsFragmentType
import com.delhivery.axle.ui.home.fragments.HomePlacementsFragmentsAdapter
import com.delhivery.axle.ui.home.fragments.UpdatePlacementBadgeAction
import com.delhivery.axle.utils.EVENT_HOME_PLACEMENT_FILTER
import com.delhivery.axle.utils.PROPERTY_PHONE_NO
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.tabs.TabLayout
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject


/**
 * All placements screen on home with view pager for Delayed and Expected tabs
 */
class HomePlacementsFragment : HomeBaseFragment<FragmentHomePlacementsV3Binding, HomePlacementsViewModel>(),
    TitleProvider {

    private var _title: String = "Placements"
    @Inject lateinit var userPrefs: UserPrefs
    private var fragmentSetupTrace: Trace? = null
    private var isFirstResume = true

    override val title: CharSequence
        get() = _title

    init {
        toolbarElevationLiveData = MutableLiveData()
        hasInlineProgress = true
    }

    companion object {
        /* singleton instance */
        val _instance: HomePlacementsFragment by lazy { HomePlacementsFragment() }
        private const val TAG = "HomePlacementsFragment"
    }

    override fun getViewModelClass() = HomePlacementsViewModel::class.java

    override fun layoutId() = R.layout.fragment_home_placements_v3

    /* home fragments pager adapter */
    private lateinit var pagerAdapter: HomePlacementsFragmentsAdapter

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomePlacementsFragment_SetupTime")
        fragmentSetupTrace?.start()

        //setup fragment result listener for child fragment's result
        setupFragmentListener()
        //
        pagerAdapter = HomePlacementsFragmentsAdapter(childFragmentManager)

        binding.viewPager.apply {
            offscreenPageLimit = HomePlacementsFragmentType.count()
            adapter = pagerAdapter
        }
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        // Setup tab custom views with counts
        val tab1 = binding.tabLayout.getTabAt(0)
        val tab2 = binding.tabLayout.getTabAt(1)

        tab1?.setCustomView(R.layout.badge_tab)?.setText("Delayed")
        tab2?.setCustomView(R.layout.badge_tab)?.setText("Expected")

        // Set initial selected tab styling
        if (tab1?.isSelected == true) {
            tab1.customView?.findViewById<TextView>(android.R.id.text1)?.let { textView ->
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDelhiveryRed))
            }
            tab1.customView?.findViewById<TextView>(R.id.tvCount)?.let { textView ->
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDelhiveryRed))
            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<TextView>(android.R.id.text1)?.let { textView ->
                    textView.setTextColor(ContextCompat.getColor(context!!, R.color.colorDelhiveryRed))
                }
                tab?.customView?.findViewById<TextView>(R.id.tvCount)?.let { textView ->
                    textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDelhiveryRed))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<TextView>(android.R.id.text1)?.let { textView ->
                    textView.setTextColor(ContextCompat.getColor(context!!, R.color.color_hint))
                }
                tab?.customView?.findViewById<TextView>(R.id.tvCount)?.let { textView ->
                    textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_hint))
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        // Observe placement counts and update tab counts
        Log.d(TAG, "Setting up observer for totalPlacementLiveData")
        viewModel.totalPlacementLiveData.observe(viewLifecycleOwner, Observer { placementData ->
            Log.d(TAG, "Observer triggered with data: $placementData")
            placementData?.let { (delayedCount, _, expectedCount) ->
                Log.d(TAG, "Updating tab counts - Delayed: $delayedCount, Expected: $expectedCount")
                updateTabCounts(delayedCount, expectedCount)
            }
        })

        // Trigger data fetch to get initial counts
        Log.d(TAG, "Triggering initial data fetch")
        viewModel.fetchPlacementLoads("Initial")
    }

    private fun setupFragmentListener() {
        // Listen for results from a child fragment
        childFragmentManager.setFragmentResultListener("tabCountDataKey", this) { requestKey, bundle ->
            // Handle the result here
            val result = bundle.getString("tabCountValue")?.split(":")
            val delayedCount = result?.get(0)?:""
            val expectedCount = result?.get(1)?:""

            if(delayedCount.isNotNullOrEmpty() && expectedCount.isNotNullOrEmpty()){
                //update tabs count
                updateTabCounts(delayedCount.toInt(), expectedCount.toInt())
            }

            Log.d("ParentFragment", "Received result from child: $result")
        }
    }

    override fun onResume() {
        super.onResume()
        if (fragmentSetupTrace != null && isFirstResume) {
            fragmentSetupTrace?.stop()
            isFirstResume = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_call, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        // Hide the menu item
        menu.findItem(R.id.nav_call).isVisible = true
    }

/*  override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_filter -> {
                analyticsUtil.moEngageTrackEvent(
                    EVENT_HOME_PLACEMENT_FILTER,
                    mutableListOf(
                        PROPERTY_USER_ID,
                        PROPERTY_PHONE_NO
                    ),
                    mutableListOf(
                        userPrefs.userId(),
                        userPrefs.phoneNumber ?: ""
                    )
                )
                // Filter functionality can be implemented in child fragments if needed
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }*/

    /**
     * Update tab counts for Delayed and Expected tabs
     */
    fun updateTabCounts(delayedCount: Int, expectedCount: Int) {
        try {
            Log.d(TAG, "updateTabCounts called with Delayed: $delayedCount, Expected: $expectedCount")
            
            // Update bottom navigation badge for placement icon
            action(UpdatePlacementBadgeAction(delayedCount))
            // Update Delayed tab count
            binding.tabLayout.getTabAt(0)?.customView?.findViewById<TextView>(R.id.tvCount)?.text =
                if (delayedCount > 0) " ($delayedCount)" else ""

            // Update Expected tab count
            binding.tabLayout.getTabAt(1)?.customView?.findViewById<TextView>(R.id.tvCount)?.text =
                if (expectedCount > 0) " ($expectedCount)" else ""
            
            Log.d(TAG, "Tab counts updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating tab counts", e)
        }
    }
}
