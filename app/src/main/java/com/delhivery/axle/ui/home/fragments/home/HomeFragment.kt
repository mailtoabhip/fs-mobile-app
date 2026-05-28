package com.delhivery.axle.ui.home.fragments.home

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager.widget.ViewPager
import com.delhivery.axle.R
import com.delhivery.axle.api.response.ServiceGroup
import com.delhivery.axle.databinding.FragmentHomeBinding
import com.delhivery.axle.ui.comingsoon.ComingSoonActivity
import com.delhivery.axle.ui.common.UiEvent
import com.delhivery.axle.ui.common.UiState
import com.delhivery.axle.ui.fastag.issuance.BuyFasTagActivity
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.kyc.documentverification.DocumentVerificationActivity
import com.delhivery.axle.ui.profile.MyProfileActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import kotlinx.coroutines.launch

class HomeFragment : HomeBaseFragment<FragmentHomeBinding, HomeFragmentViewModel>(), TitleProvider {

    override val title: CharSequence
        get() = "Home"

    override fun getViewModelClass() = HomeFragmentViewModel::class.java

    override fun layoutId() = R.layout.fragment_home
    private var fragmentSetupTrace: Trace? = null
    private var currentUiState: KycUiState? = null
    private var isFirstResume = true

    private lateinit var serviceGroupAdapter: ServiceGroupAdapter

    companion object {
        val _instance: HomeFragment by lazy { HomeFragment() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomeFragment_SetupTime")
        fragmentSetupTrace?.start()
        setupViews()
        setupServiceGroupsRecyclerView()
        setupBannerCarousel()

        binding.fastagCard.setOnClickListener {
            startActivity(Intent(requireContext(), BuyFasTagActivity::class.java))
        }

        binding.gpsCard.setOnClickListener {
            startActivity(ComingSoonActivity.newIntent(requireContext(), ComingSoonActivity.TYPE_GPS))
        }

        binding.fuelCardAction.setOnClickListener {
            startActivity(ComingSoonActivity.newIntent(requireContext(), ComingSoonActivity.TYPE_FUEL_CARD))
        }

        binding.myTrucksCard.setOnClickListener {
            action(NavigateHomeFragmentAction(HomeFragmentType.TruckFragment))
        }

        binding.myLoadsCard.setOnClickListener {
            action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
        }

//        binding.serviceGroupsRetryBtn.setOnClickListener {
//            viewModel.retryServiceGroups()
//        }

        viewModel.kycUiModel.observe(viewLifecycleOwner) { model ->
            currentUiState = model.uiState
            applyKycCardColors(model.colors)
            applyKycCardTexts(model)
        }

        binding.btnGetStarted.setOnClickListener {
            when (currentUiState) {
                KycUiState.START,
                KycUiState.CONTINUE -> {
                    navigationUtils.navigateOnboardingSteps(true)
                }
                KycUiState.COMPLETED -> {
                    action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
                }
                else -> { navigationUtils.navigate(MyProfileActivity::class.java) }
            }
        }

        // Collect service groups state
        collectServiceGroupsState()
        collectEvents()

    }

    override fun onResume() {
        super.onResume()
        if (fragmentSetupTrace != null && isFirstResume) {
            fragmentSetupTrace?.stop()
            isFirstResume = false
        }
    }

    private fun setupViews() {
        binding.apply {
            viewModel = this@HomeFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    // ────────────────────────── Service Groups Setup ───────────────────

    private fun setupServiceGroupsRecyclerView() {
        serviceGroupAdapter = ServiceGroupAdapter { group ->
            onServiceGroupClicked(group)
        }
    }

    private fun collectServiceGroupsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.serviceGroupsState.collect { state ->
                    // Handle loading
//                    binding.serviceGroupsShimmer.visibility =
//                        if (state.isLoading) View.VISIBLE else View.GONE

                    // Handle data state
                    when (val uiState = state.uiState) {
                        is UiState.Success -> {
                            serviceGroupAdapter.submitList(uiState.data)
                        }
                        is UiState.Error -> {
                        }
                        is UiState.Empty -> {
                        }
                        else -> {
                            // Idle state — hide both, shimmer handles loading
                        }
                    }
                }
            }
        }
    }

    private fun collectEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is UiEvent.ShowSnackbar -> {
                            val snackbar = Snackbar.make(
                                binding.root, event.message, Snackbar.LENGTH_LONG
                            )
                            event.action?.let { actionText ->
                                snackbar.setAction(actionText) {
                                    event.onActionClick?.invoke()
                                }
                            }
                            snackbar.show()
                        }
                        is UiEvent.ShowToast -> {
                            // Handle toast if needed
                        }
                        is UiEvent.Navigate -> {
                            // Handle navigation if needed
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle service group card click.
     * Routes to the appropriate screen based on group_id.
     */
    private fun onServiceGroupClicked(group: ServiceGroup) {
        when (group.groupId) {
            "grp_loads" -> {
                startActivity(Intent(requireContext(), DocumentVerificationActivity::class.java))
            }
            "grp_finance" -> {
                action(NavigateHomeFragmentAction(HomeFragmentType.TruckFragment))
            }
            else -> {
                // Default: navigate to document verification for unknown groups
                startActivity(Intent(requireContext(), DocumentVerificationActivity::class.java))
            }
        }
    }

    // ────────────────────────── Banner Carousel Setup ────────────────────

    private fun setupBannerCarousel() {
        // Static banners - replace with API-driven banners when available
        val banners = listOf(
            HomeBannerItem(imageRes = R.drawable.bg_banner_dark, deepLink = "loads"),
            HomeBannerItem(imageRes = R.drawable.bg_banner_dark, deepLink = "trucks"),
            HomeBannerItem(imageRes = R.drawable.bg_banner_dark, deepLink = "fastag")
        )

        val adapter = HomeBannerPagerAdapter(banners) { banner ->
            when (banner.deepLink) {
                "loads" -> action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
                "trucks" -> action(NavigateHomeFragmentAction(HomeFragmentType.TruckFragment))
                else -> { /* no-op */ }
            }
        }

        binding.bannerViewPager.adapter = adapter
        setupBannerDots(banners.size)

        binding.bannerViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) {
                updateBannerDots(position)
            }
        })
    }

    private fun setupBannerDots(count: Int) {
        binding.bannerDotsContainer.removeAllViews()
        for (i in 0 until count) {
            val dot = View(requireContext())
            val params = LinearLayout.LayoutParams(
                if (i == 0) resources.getDimensionPixelSize(R.dimen.size_20dp)
                else resources.getDimensionPixelSize(R.dimen.size_8dp),
                resources.getDimensionPixelSize(R.dimen.size_8dp)
            )
            params.setMargins(
                resources.getDimensionPixelSize(R.dimen.size_4dp), 0,
                resources.getDimensionPixelSize(R.dimen.size_4dp), 0
            )
            dot.layoutParams = params
            dot.setBackgroundResource(
                if (i == 0) R.drawable.banner_dot_active else R.drawable.banner_dot_inactive
            )
            binding.bannerDotsContainer.addView(dot)
        }
    }

    private fun updateBannerDots(selectedPosition: Int) {
        for (i in 0 until binding.bannerDotsContainer.childCount) {
            val dot = binding.bannerDotsContainer.getChildAt(i)
            val params = dot.layoutParams as LinearLayout.LayoutParams
            if (i == selectedPosition) {
                params.width = resources.getDimensionPixelSize(R.dimen.size_20dp)
                dot.setBackgroundResource(R.drawable.banner_dot_active)
            } else {
                params.width = resources.getDimensionPixelSize(R.dimen.size_8dp)
                dot.setBackgroundResource(R.drawable.banner_dot_inactive)
            }
            dot.layoutParams = params
        }
    }

    // ────────────────────────── KYC UI Helpers ─────────────────────────

    private fun applyKycCardColors(colors: KycUiColors) {
        context?.let { ctx ->
            binding.buttonCl.setBackgroundColor(
                ContextCompat.getColor(ctx, colors.cardColorLight)
            )
            binding.kycStatusLl.setBackgroundColor(
                ContextCompat.getColor(ctx, colors.cardColorDark)
            )
            binding.progressBar.progressTintList = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, colors.progressColor)
            )
            binding.progressBar.progressBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, colors.progressBgColor)
            )
            binding.bidHammerIv.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(ctx, colors.progressColor))
        }
    }

    private fun applyKycCardTexts(model: KycUiModel) {
        context?.let { ctx ->
            binding.kycHeading.text = ctx.getString(model.strings.headingResId)
            binding.kycMessage.text = ctx.getString(model.strings.messageResId)
            binding.btnTv.text = ctx.getString(model.strings.buttonTextResId)
            binding.progressBar.progress = model.progress
            binding.progressBar.visibility = if (model.progress != 0) View.VISIBLE else View.GONE
        }
    }
}
