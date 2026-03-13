package com.delhivery.axle.ui.loadwallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityLoadWalletBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.dialogs.PaymentStatus
import com.delhivery.axle.ui.fastag.wallet.AddMoneyDialogFragment
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

/**
 * Delhivery Wallet screen - displays wallet balance and transaction history
 */
class LoadWalletActivity : BaseActivity<ActivityLoadWalletBinding, LoadWalletViewModel>() {

    @Inject
    lateinit var userPrefs: UserPrefs

    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true
    var currentFilter = WalletFilter(dateRange = FilterDateRange.LAST_90_DAYS)

    override fun getViewModelClass() = LoadWalletViewModel::class.java

    override fun layoutId() = R.layout.activity_load_wallet

    override fun requireConnection() = true

    private val pagerAdapter: WalletPagerAdapter by lazy {
        WalletPagerAdapter(supportFragmentManager)
    }

    private var walletCreatingBottomSheet: WalletCreatingBottomSheet? = null

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        activitySetupTrace =
            FirebasePerformance.getInstance().newTrace("LoadWalletActivity_SetupTime")
        activitySetupTrace?.start()

        setSupportActionBar(binding.toolbar)

        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
        }
        title = "Delhivery Wallet"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                userPrefs.setPreviousScreen(this.javaClass.name)
                finish()
            }
        })

        viewModel.walletBalance.observe(this) {
            binding.textBalance.text = it
        }

        // Show shimmer and fetch wallet details on load
        showBalanceShimmer()
        viewModel.fetchWalletDetails()

        // Observe wallet existence state
        viewModel.walletExistsLiveData.observe(this) { exists ->
            hideBalanceShimmer()
            if (exists) {
                showWalletActiveState()
                // Dismiss creating bottom sheet if it was showing
                walletCreatingBottomSheet?.dismiss()
                walletCreatingBottomSheet = null
                // Now that wallet is confirmed, load transactions and recharges
                val transactionsFragment = pagerAdapter.getItem(0) as? WalletTransactionsFragment
                transactionsFragment?.applyFilter(currentFilter)
                val rechargesFragment = pagerAdapter.getItem(1) as? WalletRechargesFragment
                rechargesFragment?.refreshData()
            } else {
                showWalletNotExistState()
            }
        }

        viewModel.walletErrorLiveData.observe(this) { error ->
            if (error != null) {
                hideBalanceShimmer()
                walletCreatingBottomSheet?.dismiss()
                walletCreatingBottomSheet = null
                dialogUtils.showErrorDialog(error, 3L)
                viewModel.walletErrorLiveData.postValue(null)
            }
        }

        binding.btnRefreshBalance.setOnClickListener {
            showBalanceShimmer()
            viewModel.fetchWalletDetails()
        }

        binding.btnAddMoney.setOnClickListener {
            if (viewModel.walletExistsLiveData.value == true) {
                AddMoneyDialogFragment.newInstance(
                    redirectUrl = "https://www.delhivery.com/",
                    onPaymentResult = {}
                ).show(supportFragmentManager, "AddMoney")
            } else {
                // Show "creating wallet" bottom sheet and call create API
                walletCreatingBottomSheet = WalletCreatingBottomSheet.newInstance()
                walletCreatingBottomSheet?.show(supportFragmentManager, "WalletCreatingBottomSheet")
                viewModel.createWallet()
            }
        }

        binding.btnFilter.setOnClickListener {
            showFilterBottomSheet()
        }

        // Setup ViewPager + Tabs
        binding.viewpager.apply {
            offscreenPageLimit = WalletTabType.count()
            adapter = pagerAdapter
        }
        binding.tabLayout.setupWithViewPager(binding.viewpager)
        // Block swipe gestures on the tab strip — only content area swipe should work
        binding.tabLayout.setOnTouchListener { _, _ -> true }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                updateActiveFilterChips(position)
                when (position) {
                    0 -> {
                        val fragment = pagerAdapter.getItem(0) as? WalletTransactionsFragment
                        fragment?.applyFilter(currentFilter)
                    }
                    1 -> {
                        val fragment = pagerAdapter.getItem(1) as? WalletRechargesFragment
                        fragment?.refreshData()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        updateActiveFilterChips(0)

        // Listen for the dialog dismissal result
        supportFragmentManager.setFragmentResultListener("PaymentStatusResult", this) { _, bundle ->
            val status = bundle.getString("STATUS")
            if (status == PaymentStatus.SUCCESS.name) {
                // Trigger the refresh API
                showBalanceShimmer()
                viewModel.fetchWalletDetails()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

    private fun showFilterBottomSheet() {
        val isRechargesTab = binding.viewpager.currentItem == 1
        val bottomSheet = WalletFilterBottomSheet.newInstance(
            currentFilter = currentFilter,
            showTypeFilter = !isRechargesTab
        ) { filter ->
            currentFilter = filter
            updateActiveFilterChips(binding.viewpager.currentItem)
            if (isRechargesTab) {
                val rechargesFragment = pagerAdapter.getItem(1) as? WalletRechargesFragment
                rechargesFragment?.refreshData()
            } else {
                val transactionsFragment = pagerAdapter.getItem(0) as? WalletTransactionsFragment
                transactionsFragment?.applyFilter(filter)
            }
        }
        bottomSheet.show(supportFragmentManager, "WalletFilterBottomSheet")
    }

    private fun updateActiveFilterChips(tabPosition: Int = binding.viewpager.currentItem) {
        binding.activeFiltersGroup.removeAllViews()

        val isRechargesTab = tabPosition == 1
        val showType = !isRechargesTab && currentFilter.type != null
        val hasFilters = showType || currentFilter.dateRange != null

        if (!hasFilters) {
            binding.activeFiltersGroup.visibility = View.GONE
            return
        }

        binding.activeFiltersGroup.visibility = View.VISIBLE

        // Add type filter chip (only on Transactions tab)
        if (showType) {
            currentFilter.type?.let { type ->
                addActiveFilterChip(type.label, closeable = true) {
                    currentFilter = currentFilter.copy(type = null)
                    onFilterChanged()
                }
            }
        }

        // Add date range filter chip (no close icon — date range is mandatory)
        currentFilter.dateRange?.let { dateRange ->
            addActiveFilterChip(dateRange.label, closeable = false) {}
        }
    }

    private fun addActiveFilterChip(label: String, closeable: Boolean = true, onClose: () -> Unit) {
        val chip = layoutInflater.inflate(
            R.layout.item_active_filter_chip,
            binding.activeFiltersGroup,
            false
        ) as Chip
        chip.text = label
        if (closeable) {
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                onClose()
            }
        } else {
            chip.isCloseIconVisible = false
        }
        binding.activeFiltersGroup.addView(chip)
    }

    private fun onFilterChanged() {
        updateActiveFilterChips(binding.viewpager.currentItem)
        val transactionsFragment = pagerAdapter.getItem(0) as? WalletTransactionsFragment
        transactionsFragment?.applyFilter(currentFilter)
    }

    private fun showWalletActiveState() {
        binding.balanceShimmer.visibility = View.GONE
        binding.labelWalletBalance.visibility = View.VISIBLE
        binding.balanceContainer.visibility = View.VISIBLE
        binding.textWalletHint.visibility = View.GONE
        binding.balanceSection.setPadding(
            binding.balanceSection.paddingLeft, 0,
            binding.balanceSection.paddingRight,
            resources.getDimensionPixelSize(R.dimen.size_40dp)
        )
        binding.tabLayout.visibility = View.VISIBLE
        binding.viewpager.visibility = View.VISIBLE
        binding.noWalletEmptyState.visibility = View.GONE
        updateActiveFilterChips(binding.viewpager.currentItem)
    }

    private fun showWalletNotExistState() {
        binding.balanceShimmer.visibility = View.GONE
        binding.labelWalletBalance.visibility = View.GONE
        binding.balanceContainer.visibility = View.GONE
        binding.textWalletHint.visibility = View.VISIBLE
        binding.balanceSection.setPadding(
            binding.balanceSection.paddingLeft,
            resources.getDimensionPixelSize(R.dimen.size_48dp),
            binding.balanceSection.paddingRight,
            resources.getDimensionPixelSize(R.dimen.size_40dp)
        )
        binding.tabLayout.visibility = View.GONE
        binding.activeFiltersGroup.visibility = View.GONE
        binding.viewpager.visibility = View.GONE
        binding.noWalletEmptyState.visibility = View.VISIBLE
    }

    private fun showBalanceShimmer() {
        binding.balanceContainer.visibility = View.GONE
        binding.balanceShimmer.visibility = View.VISIBLE
        binding.labelWalletBalance.visibility = View.VISIBLE
        // Center the label since balance_container (its alignStart anchor) is GONE
        val labelParams = binding.labelWalletBalance.layoutParams as android.widget.RelativeLayout.LayoutParams
        labelParams.removeRule(android.widget.RelativeLayout.ALIGN_START)
        labelParams.addRule(android.widget.RelativeLayout.CENTER_HORIZONTAL)
        binding.labelWalletBalance.layoutParams = labelParams
        // Re-anchor Add Money button below shimmer
        val params = binding.btnAddMoney.layoutParams as android.widget.RelativeLayout.LayoutParams
        params.addRule(android.widget.RelativeLayout.BELOW, R.id.balance_shimmer)
        binding.btnAddMoney.layoutParams = params
    }

    private fun hideBalanceShimmer() {
        binding.balanceShimmer.visibility = View.GONE
        // Restore label alignment based on whether balance_container is visible
        val labelParams = binding.labelWalletBalance.layoutParams as android.widget.RelativeLayout.LayoutParams
        if (binding.balanceContainer.visibility == View.VISIBLE) {
            labelParams.removeRule(android.widget.RelativeLayout.CENTER_HORIZONTAL)
            labelParams.addRule(android.widget.RelativeLayout.ALIGN_START, R.id.balance_container)
        } else {
            labelParams.removeRule(android.widget.RelativeLayout.ALIGN_START)
            labelParams.addRule(android.widget.RelativeLayout.CENTER_HORIZONTAL)
        }
        binding.labelWalletBalance.layoutParams = labelParams
        // Re-anchor Add Money button below balance_container
        val params = binding.btnAddMoney.layoutParams as android.widget.RelativeLayout.LayoutParams
        params.addRule(android.widget.RelativeLayout.BELOW, R.id.balance_container)
        binding.btnAddMoney.layoutParams = params
    }
}

/**
 * Load Wallet intent
 */
fun loadWalletIntent(
    context: Context
) = Intent(context, LoadWalletActivity::class.java)
