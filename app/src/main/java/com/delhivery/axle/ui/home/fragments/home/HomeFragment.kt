package com.delhivery.axle.ui.home.fragments.home

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.api.response.ServiceGroup
import com.delhivery.axle.databinding.FragmentHomeBinding
import com.delhivery.axle.ui.common.UiEvent
import com.delhivery.axle.ui.common.UiState
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.profile.MyProfileActivity
import com.delhivery.axle.ui.kyc.documentverification.DocumentVerificationActivity
import com.delhivery.axle.ui.searchload.searchLoadContractsIntent
import com.delhivery.axle.utils.EVENT_HOME_SCREEN_FASTAG_TAP
import com.delhivery.axle.utils.EVENT_HOME_SCREEN_SHOWN
import com.delhivery.axle.utils.PROPERTY_PAGE_NAME
import com.delhivery.axle.utils.PROPERTY_USER_ID
import com.delhivery.axle.utils.VALUE_HOME_PAGE
import com.delhivery.axle.ui.comingsoon.ComingSoonActivity
import com.google.android.material.snackbar.Snackbar
import com.delhivery.axle.ui.comingsoon.ComingSoonActivity
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
        setupStaticServiceCards()

        binding.loadsCard.setOnClickListener {
            action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
        }

        binding.editStickySearch.setOnClickListener {
            context?.let {
                startActivity(
                    Intent(searchLoadContractsIntent(it, "load"))
                )
            }
        }
        binding.loadServicesCard.setOnClickListener {
            startActivity(Intent(requireContext(), DocumentVerificationActivity::class.java))
        }

        binding.fastagCard.setOnClickListener {
            val (keys, values) = getCommonEventProperties()
            analyticsUtil.moEngageTrackEvent(
                EVENT_HOME_SCREEN_FASTAG_TAP,
                keys,
                values
            )
            action(NavigateHomeFragmentAction(HomeFragmentType.TruckFragment))
        }

        binding.gpsCard.setOnClickListener {
            startActivity(ComingSoonActivity.newIntent(requireContext(), ComingSoonActivity.TYPE_GPS))
        }

        binding.fuelCardAction.setOnClickListener {
            startActivity(ComingSoonActivity.newIntent(requireContext(), ComingSoonActivity.TYPE_FUEL_CARD))
        }

        binding.serviceGroupsRetryBtn.setOnClickListener {
            viewModel.retryServiceGroups()
        }

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

        // Fetch service groups from API
        viewModel.fetchServiceGroups()
    }

    override fun onResume() {
        super.onResume()

        if (isFirstResume) {
            val (keys, values) = getCommonEventProperties()
            analyticsUtil.moEngageTrackEvent(
                EVENT_HOME_SCREEN_SHOWN,
                keys,
                values
            )

        }
        viewModel.refreshKycStatus()
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

    private fun setupStaticServiceCards() {
        binding.loadServicesCount.text = getString(R.string.label_services_available, 5)
        binding.onboardingBannerText.text = getString(R.string.label_onboarding_in_progress, 2)
        binding.financialServicesCount.text = getString(R.string.label_services_available, 2)
    }

    // ────────────────────────── Service Groups Setup ───────────────────

    private fun setupServiceGroupsRecyclerView() {
        serviceGroupAdapter = ServiceGroupAdapter { group ->
            onServiceGroupClicked(group)
        }
        binding.serviceGroupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = serviceGroupAdapter
        }
    }

    private fun collectServiceGroupsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.serviceGroupsState.collect { state ->
                    // Handle loading
                    binding.serviceGroupsShimmer.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE

                    // Handle data state
                    when (val uiState = state.uiState) {
                        is UiState.Success -> {
                            binding.serviceGroupsRecyclerView.visibility = View.VISIBLE
                            binding.serviceGroupsError.visibility = View.GONE
                            serviceGroupAdapter.submitList(uiState.data)
                        }
                        is UiState.Error -> {
                            binding.serviceGroupsRecyclerView.visibility = View.GONE
                            binding.serviceGroupsError.visibility = View.VISIBLE
                            binding.serviceGroupsErrorMessage.text = uiState.message
                        }
                        is UiState.Empty -> {
                            binding.serviceGroupsRecyclerView.visibility = View.GONE
                            binding.serviceGroupsError.visibility = View.VISIBLE
                            binding.serviceGroupsErrorMessage.text = uiState.message
                            binding.serviceGroupsRetryBtn.visibility = View.GONE
                        }
                        else -> {
                            // Idle state — hide both, shimmer handles loading
                            binding.serviceGroupsRecyclerView.visibility = View.GONE
                            binding.serviceGroupsError.visibility = View.GONE
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

    private fun getCommonEventProperties(): Pair<MutableList<String>, MutableList<String>> {
        return Pair(
            mutableListOf(
                PROPERTY_USER_ID,
                PROPERTY_PAGE_NAME
            ),
            mutableListOf(
                (activity as? HomeActivity)?.userPrefs?.userId() ?: "",
                VALUE_HOME_PAGE
            )
        )
    }
}
