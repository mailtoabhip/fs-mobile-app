package com.delhivery.axle.ui.home.fragments.home

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentHomeBinding
import com.delhivery.axle.ui.home.activity.home.TitleProvider
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.HomeFragmentType
import com.delhivery.axle.ui.home.fragments.NavigateHomeFragmentAction
import com.delhivery.axle.ui.profile.MyProfileActivity
import com.delhivery.axle.ui.searchload.searchLoadContractsIntent
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

class HomeFragment : HomeBaseFragment<FragmentHomeBinding, HomeFragmentViewModel>(), TitleProvider {

    override val title: CharSequence
        get() = "Home"

    override fun getViewModelClass() = HomeFragmentViewModel::class.java

    override fun layoutId() = R.layout.fragment_home
    private var fragmentSetupTrace: Trace? = null
    private var currentUiState: HomeFragmentViewModel.KycUiState? = null
    private var isFirstResume = true
    companion object {
        val _instance: HomeFragment by lazy { HomeFragment() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("HomeFragment_SetupTime")
        fragmentSetupTrace?.start()
        setupViews()
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
        binding.fastagCard.setOnClickListener {
            action(NavigateHomeFragmentAction(HomeFragmentType.TruckFragment))
        }

        viewModel.kycUiModel.observe(viewLifecycleOwner) { model ->
            currentUiState = model.uiState
            applyKycCardColors(model.colors)
            applyKycCardTexts(model)
        }

        binding.btnGetStarted.setOnClickListener {
            when (currentUiState) {
                HomeFragmentViewModel.KycUiState.START,
                HomeFragmentViewModel.KycUiState.CONTINUE -> {
                    navigationUtils.navigateOnboardingSteps(true)
                }
                HomeFragmentViewModel.KycUiState.COMPLETED -> {
                    action(NavigateHomeFragmentAction(HomeFragmentType.LoadsTruckFragment))
                }
                else -> { navigationUtils.navigate(MyProfileActivity::class.java) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
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

    private fun applyKycCardColors(colors: HomeFragmentViewModel.KycUiColors) {
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

    private fun applyKycCardTexts(model: HomeFragmentViewModel.KycUiModel) {
        context?.let { ctx ->
            binding.kycHeading.text = ctx.getString(model.headingResId)
            binding.kycMessage.text = ctx.getString(model.messageResId)
            binding.btnTv.text = ctx.getString(model.buttonTextResId)
            binding.progressBar.progress = model.progress
            binding.progressBar.visibility = if (model.progress != 0) View.VISIBLE else View.GONE
        }
    }
}
