package com.delhivery.axle.ui.profile.fragments

import android.os.Bundle
import android.view.View
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentYourKycDetailsBinding

class YourKYCDetailsFragment: ProfileKYCBaseFragment<FragmentYourKycDetailsBinding, YourKYCDetailsViewModel>() {

    init {
        hasInlineProgress = true
    }

    companion object {
        /* singleton instance */
        val _instance: YourKYCDetailsFragment by lazy { YourKYCDetailsFragment() }
    }

    override fun getViewModelClass()= YourKYCDetailsViewModel::class.java

    override fun layoutId() = R.layout.fragment_your_kyc_details

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}