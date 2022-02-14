package com.delhivery.axle.ui.profile.fragments

import android.os.Bundle
import android.view.View
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentKycDocumentsBinding


class KycDocumentsFragment : ProfileKYCBaseFragment<FragmentKycDocumentsBinding, KYCDocumentsViewModel>() {

    init {
        hasInlineProgress = true
    }

    companion object {
        /* singleton instance */
        val _instance: KycDocumentsFragment by lazy { KycDocumentsFragment() }
    }

    override fun getViewModelClass()= KYCDocumentsViewModel::class.java

    override fun layoutId() = R.layout.fragment_kyc_documents

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


}