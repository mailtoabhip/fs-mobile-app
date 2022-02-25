package com.delhivery.axle.ui.profile.kycdetails.fragments

import android.os.Bundle
import android.view.View
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentYourKycDetailsBinding
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class YourKYCDetailsFragment: ProfileKYCBaseFragment<FragmentYourKycDetailsBinding, YourKYCDetailsViewModel>() {

    init {
        hasInlineProgress = true
    }

    companion object {
        /* singleton instance */
        val _instance: YourKYCDetailsFragment by lazy { YourKYCDetailsFragment() }
    }

    @Inject lateinit var userPrefs: UserPrefs

    override fun getViewModelClass()= YourKYCDetailsViewModel::class.java

    override fun layoutId() = R.layout.fragment_your_kyc_details

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(userPrefs.pancard.isNotNullOrEmpty()){
            binding.textKycPanNumberProfile.text = userPrefs.pancard
            binding.labelPan.visibility = View.VISIBLE
            binding.panLay.visibility = View.VISIBLE
        }else{
            binding.labelPan.visibility = View.GONE
            binding.panLay.visibility = View.GONE
        }

        if(userPrefs.aadhaarNumber.isNotNullOrEmpty()){
            binding.textAadhar.text = userPrefs.aadhaarNumber
            binding.labelAadhaar.visibility = View.VISIBLE
            binding.aadharLay.visibility = View.VISIBLE
        }else{
            binding.labelAadhaar.visibility = View.GONE
            binding.aadharLay.visibility = View.GONE
        }

        if(userPrefs.businessAddress.isNotNullOrEmpty()){
                binding.textKycAddressProfile.text = userPrefs.businessAddress
                binding.labelAddress.visibility = View.VISIBLE
                binding.addressLay.visibility = View.VISIBLE
        }else{
            if (userPrefs.getAddressList().isNullOrEmpty()){
                    binding.labelAddress.visibility = View.GONE
                    binding.addressLay.visibility = View.GONE
            } else{
                    if(userPrefs.getAddressList()?.get(0)!=null){
                        binding.textKycAddressProfile.text = userPrefs.getAddressList()?.get(0)?.address
                        binding.labelAddress.visibility = View.VISIBLE
                        binding.addressLay.visibility = View.VISIBLE
                    }else{
                        binding.labelAddress.visibility = View.GONE
                        binding.addressLay.visibility = View.GONE
                    }
                }
        }

        if(userPrefs.gstNumber.isEmpty()){
            binding.labelGst.visibility = View.GONE
            binding.gstLay.visibility = View.GONE
        }else{
            binding.labelGst.visibility = View.VISIBLE
            binding.gstLay.visibility = View.VISIBLE
            binding.textKycGstNumberProfile.text = userPrefs.gstNumber
        }

        if(userPrefs.rcNumber.isEmpty()){
            binding.tvRc.visibility = View.GONE
            binding.truckRcLay.visibility = View.GONE
        }else{
            binding.tvRc.visibility = View.VISIBLE
            binding.truckRcLay.visibility = View.VISIBLE
            binding.textRc.text = userPrefs.rcNumber
        }

        if(userPrefs.cinNumber.isEmpty()){
            binding.labelCin.visibility = View.GONE
            binding.cinLay.visibility = View.GONE
        }else{
            binding.labelCin.visibility = View.VISIBLE
            binding.cinLay.visibility = View.VISIBLE
            binding.textKycCinNumberProfile.text = userPrefs.cinNumber
        }
    }
}