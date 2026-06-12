package com.dfd.delfin.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.dfd.delfin.R
import com.dfd.delfin.databinding.ActivityPlacementsBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.home.fragments.HomePlacementsFragmentType
import com.dfd.delfin.ui.home.fragments.HomePlacementsFragmentsAdapter
import com.dfd.delfin.ui.home.fragments.placements.HomePlacementsViewModel
import com.dfd.delfin.utils.WindowInsetsUtils
import com.google.android.material.tabs.TabLayout

class PlacementsActivity : BaseActivity<ActivityPlacementsBinding, HomePlacementsViewModel>() {

    private lateinit var pagerAdapter: HomePlacementsFragmentsAdapter

    companion object {
        private const val TAG = "PlacementsActivity"
    }

    override fun getViewModelClass() = HomePlacementsViewModel::class.java

    override fun layoutId() = R.layout.activity_placements

    override fun requireConnection() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTitle.text = "Placements"

        // Handle window insets for edge-to-edge display (API 35+)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applySystemWindowInsets(binding.main)
        }

        // Setup ViewPager with adapter
        pagerAdapter = HomePlacementsFragmentsAdapter(supportFragmentManager)

        binding.viewPager.offscreenPageLimit = HomePlacementsFragmentType.count()
        binding.viewPager.adapter = pagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        // Setup tab custom views with counts
        val tab1 = binding.tabLayout.getTabAt(0)
        val tab2 = binding.tabLayout.getTabAt(1)

        tab1?.setCustomView(R.layout.badge_tab)
        tab1?.text = "Delayed"
        tab2?.setCustomView(R.layout.badge_tab)
        tab2?.text = "Expected"

        // Set initial selected tab styling
        if (tab1?.isSelected == true) {
            val textView1 = tab1.customView?.findViewById<TextView>(android.R.id.text1)
            textView1?.setTextColor(ContextCompat.getColor(this, R.color.colorDelhiveryRed))
            val textView2 = tab1.customView?.findViewById<TextView>(R.id.tvCount)
            textView2?.setTextColor(ContextCompat.getColor(this, R.color.colorDelhiveryRed))
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val textView1 = tab?.customView?.findViewById<TextView>(android.R.id.text1)
                textView1?.setTextColor(ContextCompat.getColor(this@PlacementsActivity, R.color.colorDelhiveryRed))
                val textView2 = tab?.customView?.findViewById<TextView>(R.id.tvCount)
                textView2?.setTextColor(ContextCompat.getColor(this@PlacementsActivity, R.color.colorDelhiveryRed))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val textView1 = tab?.customView?.findViewById<TextView>(android.R.id.text1)
                textView1?.setTextColor(ContextCompat.getColor(this@PlacementsActivity, R.color.color_hint))
                val textView2 = tab?.customView?.findViewById<TextView>(R.id.tvCount)
                textView2?.setTextColor(ContextCompat.getColor(this@PlacementsActivity, R.color.color_hint))
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        setupFragmentListener()
        viewModel.totalPlacementLiveData.observe(this, Observer { placementData ->
            placementData?.let { (delayedCount, _, expectedCount) ->
                updateTabCounts(delayedCount, expectedCount)
            }
        })
        viewModel.fetchPlacementLoads("Initial")
    }

    private fun setupFragmentListener() {
        supportFragmentManager.setFragmentResultListener("tabCountDataKey", this) { _, bundle ->
            val result = bundle.getString("tabCountValue")?.split(":")
            val delayedCount = result?.getOrNull(0) ?: ""
            val expectedCount = result?.getOrNull(1) ?: ""

            if (delayedCount.isNotEmpty() && expectedCount.isNotEmpty()) {
                updateTabCounts(delayedCount.toInt(), expectedCount.toInt())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_call, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.nav_call).isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_call -> {
                callHelpline()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Update tab counts for Delayed and Expected tabs
     */
    fun updateTabCounts(delayedCount: Int, expectedCount: Int) {
        try {
            val delayedTextView = binding.tabLayout.getTabAt(0)?.customView?.findViewById<TextView>(R.id.tvCount)
            delayedTextView?.text = if (delayedCount > 0) " ($delayedCount)" else ""
            val expectedTextView = binding.tabLayout.getTabAt(1)?.customView?.findViewById<TextView>(R.id.tvCount)
            expectedTextView?.text = if (expectedCount > 0) " ($expectedCount)" else ""
        } catch (e: Exception) {
            Log.e(TAG, "Error updating tab counts", e)
        }
    }
}

/**
 * Intent to launch PlacementsActivity
 */
fun placementsActivityIntent(context: Context) = Intent(context, PlacementsActivity::class.java)