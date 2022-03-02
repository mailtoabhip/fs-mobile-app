package com.delhivery.axle.ui.profile.kycdetails.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import com.delhivery.axle.R
import com.delhivery.axle.databinding.FragmentYourKycDetailsBinding
import com.delhivery.axle.utils.NavigationUtils
import com.delhivery.axle.utils.StepKey
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

    @Inject lateinit var navigationUtils:NavigationUtils

    override fun getViewModelClass()= YourKYCDetailsViewModel::class.java

    override fun layoutId() = R.layout.fragment_your_kyc_details

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(userPrefs.verificationStatus.equals("failed")){
            binding.btnRetry.visibility = View.VISIBLE
        }else{
            binding.btnRetry.visibility = View.GONE
        }

        binding.btnRetry.setOnClickListener {
            userPrefs.retryVerification = true
            val bundle = Bundle()
            bundle.putInt(StepKey, 0)
            context?.let { it1 -> navigationUtils.navigateKyc(it1, true, bundle) }
        }

        if(userPrefs.pancard.isNotNullOrEmpty()){
            binding.textKycPanNumberProfile.text = userPrefs.pancard
            binding.labelPan.visibility = View.VISIBLE
            binding.panLay.visibility = View.VISIBLE
            if(userPrefs.panRejectReason.isNotNullOrEmpty() && !userPrefs.isUserVerfied){
                binding.panLay.isSelected = true
                binding.errorPan.visibility = View.VISIBLE
                binding.imageKycDetailPanVerified.setImageDrawable(resources.getDrawable(R.drawable.ic_vector_error))
                binding.textKycPanNumberProfile.setTextColor(resources.getColor(R.color.error_red))
                binding.errorPan.text = "Pan verification failed due to "+ userPrefs.panRejectReason
            }else{
                binding.panLay.isSelected = false
                binding.errorPan.visibility = View.GONE
                binding.imageKycDetailPanVerified.setImageDrawable(resources.getDrawable(R.drawable.ic_vector_circle_check))
                binding.textKycPanNumberProfile.setTextColor(resources.getColor(R.color.faded_black))
            }
        }else{
            binding.labelPan.visibility = View.GONE
            binding.panLay.visibility = View.GONE
            binding.errorPan.visibility = View.GONE
        }

        if (userPrefs.identityType.isNotNullOrEmpty()) {
           if (userPrefs.identityType.equals("gst")) {
                if( userPrefs.gstNumber.isNotNullOrEmpty()) {
                    binding.labelGst.text = "GST Number"
                    binding.textKycGstNumberProfile.text = userPrefs.gstNumber
                    setGstError("GST verification failed due to "+userPrefs.identityRejectReason)
                }else{
                    binding.labelGst.visibility = View.GONE
                    binding.gstLay.visibility = View.GONE
                    binding.errorGst.visibility = View.GONE
                }
            } else if (userPrefs.identityType.equals("aadhaar")) {
                if(userPrefs.aadhaarNumber.isNotNullOrEmpty()) {
                    binding.labelGst.text = "Aadhaar Number"
                    binding.textKycGstNumberProfile.text = userPrefs.aadhaarNumber
                    setGstError("Aadhaar verification failed due to " + userPrefs.identityRejectReason)
                }else{
                    binding.labelGst.visibility = View.GONE
                    binding.gstLay.visibility = View.GONE
                    binding.errorGst.visibility = View.GONE
                }
            } else if (userPrefs.identityType.equals("cin")) {
                if(userPrefs.cinNumber.isNotNullOrEmpty()) {
                    binding.labelGst.text = "CIN"
                    binding.textKycGstNumberProfile.text = userPrefs.cinNumber
                    setGstError("CIN verification failed due to " + userPrefs.identityRejectReason)
                }else{
                    binding.labelGst.visibility = View.GONE
                    binding.gstLay.visibility = View.GONE
                    binding.errorGst.visibility = View.GONE
                }
            } else if (userPrefs.identityType.equals("udhyog_aadhaar")) {
                if(userPrefs.udyogNumber.isNotNullOrEmpty()) {
                    binding.labelGst.text = "Udyog Aadhaar"
                    binding.textKycGstNumberProfile.text = userPrefs.udyogNumber
                    setGstError("Udyog Aadhaar verification failed due to " + userPrefs.identityRejectReason)
                }else{
                    binding.labelGst.visibility = View.GONE
                    binding.gstLay.visibility = View.GONE
                    binding.errorGst.visibility = View.GONE
                }
            } else if (userPrefs.identityType.equals("shop_establishment")) {
                if(userPrefs.shopNumber.isNotNullOrEmpty()) {
                    binding.labelGst.text = "Shop Establishment"
                    binding.textKycGstNumberProfile.text = userPrefs.shopNumber
                    setGstError("Shop Establishment verification failed due to " + userPrefs.identityRejectReason)
                }else{
                    binding.labelGst.visibility = View.GONE
                    binding.gstLay.visibility = View.GONE
                    binding.errorGst.visibility = View.GONE
                }
            }else{
                binding.labelGst.visibility = View.GONE
                binding.gstLay.visibility = View.GONE
                binding.errorGst.visibility = View.GONE
            }
        }else{
            binding.labelGst.visibility = View.GONE
            binding.gstLay.visibility = View.GONE
            binding.errorGst.visibility = View.GONE
        }

        if(userPrefs.businessAddress.isNotNullOrEmpty()){
                binding.textKycAddressProfile.text = userPrefs.businessAddress
               setAddressError()
        }else{
            if (userPrefs.getAddressList().isNullOrEmpty()){
                    binding.labelAddress.visibility = View.GONE
                    binding.addressLay.visibility = View.GONE
                    binding.errorAddress.visibility = View.GONE
            } else{
                    if(userPrefs.getAddressList()?.get(0)!=null){
                        binding.textKycAddressProfile.text = userPrefs.getAddressList()?.get(0)?.address
                        setAddressError()
                    }else{
                        binding.labelAddress.visibility = View.GONE
                        binding.addressLay.visibility = View.GONE
                        binding.errorAddress.visibility = View.GONE
                    }
            }
        }

        if(userPrefs.rcNumber.isEmpty()){
            binding.tvRc.visibility = View.GONE
            binding.truckRcLay.visibility = View.GONE
        }else{
            binding.tvRc.visibility = View.VISIBLE
            binding.truckRcLay.visibility = View.VISIBLE
            binding.textRc.text = userPrefs.rcNumber

            if(userPrefs.rcRejectReason.isNotNullOrEmpty() && !userPrefs.isUserVerfied){
                binding.truckRcLay.isSelected = true
                binding.errorTruck.visibility = View.VISIBLE
                binding.imageRc.setImageDrawable(resources.getDrawable(R.drawable.ic_vector_error))
                binding.textRc.setTextColor(resources.getColor(R.color.error_red))
                binding.errorTruck.text = "Truck RC verification failed due to "+ userPrefs.rcRejectReason
            }else{
                binding.truckRcLay.isSelected = false
                binding.errorTruck.visibility = View.GONE
                binding.imageRc.setImageDrawable(resources.getDrawable(R.drawable.ic_vector_circle_check))
                binding.textRc.setTextColor(resources.getColor(R.color.faded_black))
            }
        }
    }

    private fun setAddressError(){
        binding.labelAddress.visibility = View.VISIBLE
        binding.addressLay.visibility = View.VISIBLE
        if(userPrefs.addressRejectReason.isNotNullOrEmpty() && !userPrefs.isUserVerfied){
            binding.addressLay.isSelected = true
            binding.errorAddress.visibility = View.VISIBLE
            binding.imageKycDetailAddressVerified.setImageDrawable(resources.getDrawable(R.drawable.ic_vector_error))
            binding.textKycAddressProfile.setTextColor(resources.getColor(R.color.error_red))
            binding.errorAddress.text = "Address verification failed due to "+ userPrefs.addressRejectReason
        }else{
            binding.addressLay.isSelected = false
            binding.errorAddress.visibility = View.GONE
            binding.imageKycDetailAddressVerified.setImageDrawable(resources.getDrawable(R.drawable.ic_vector_circle_check))
            binding.textKycAddressProfile.setTextColor(resources.getColor(R.color.faded_black))
        }
    }

    private fun setGstError(message:String){
        binding.labelGst.visibility = View.VISIBLE
        binding.gstLay.visibility = View.VISIBLE
        if(userPrefs.identityRejectReason.isNotNullOrEmpty() && !userPrefs.isUserVerfied){
            binding.gstLay.isSelected = true
            binding.errorGst.visibility = View.VISIBLE
            binding.imageKycDetailGstVerified.setImageDrawable(resources.getDrawable(R.drawable.ic_vector_error))
            binding.textKycGstNumberProfile.setTextColor(resources.getColor(R.color.error_red))
            binding.errorGst.text = message
        }else{
            binding.gstLay.isSelected = false
            binding.errorGst.visibility = View.GONE
            binding.imageKycDetailGstVerified.setImageDrawable(resources.getDrawable(R.drawable.ic_vector_circle_check))
            binding.textKycGstNumberProfile.setTextColor(resources.getColor(R.color.faded_black))
        }
    }
}