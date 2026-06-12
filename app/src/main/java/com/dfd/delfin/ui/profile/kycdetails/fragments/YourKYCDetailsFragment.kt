package com.dfd.delfin.ui.profile.kycdetails.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.dfd.delfin.R
import com.dfd.delfin.databinding.FragmentYourKycDetailsBinding
import com.dfd.delfin.utils.NavigationUtils
import com.dfd.delfin.utils.StepKey
import com.dfd.delfin.utils.extensions.isNotNullOrEmpty
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject


class YourKYCDetailsFragment: ProfileKYCBaseFragment<FragmentYourKycDetailsBinding, YourKYCDetailsViewModel>() {

    init {
        hasInlineProgress = true
    }

    companion object {
        fun newInstance(): YourKYCDetailsFragment = YourKYCDetailsFragment()
    }

    @Inject lateinit var userPrefs: UserPrefs

    @Inject lateinit var navigationUtils:NavigationUtils
    private var fragmentSetupTrace: Trace? = null
    private var isFirstResume = true
    override fun getViewModelClass()= YourKYCDetailsViewModel::class.java

    override fun layoutId() = R.layout.fragment_your_kyc_details

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("YourKYCDetailsFragment_SetupTime")
        fragmentSetupTrace?.start()

        if (userPrefs.verificationStatus.equals("failed")) {
            binding.btnRetry.visibility = View.VISIBLE
        } else {
            binding.btnRetry.visibility = View.GONE
        }
       if(userPrefs.noOfVerificationIssues.equals("1")&&userPrefs.isBankDetailsRejected==true){
           binding.btnRetry.isEnabled=false
       }else if(userPrefs.noOfVerificationIssues.equals("0")){
           binding.btnRetry.isEnabled=false
       }else
       {
           binding.btnRetry.isEnabled=true
       }

        binding.btnRetry.setOnClickListener {
            userPrefs.retryVerification = true
            userPrefs.retryVerificationOnBack = false
            val bundle = Bundle()
            bundle.putInt(StepKey, 0)
            context?.let { it1 -> navigationUtils.navigateKyc(it1, true, bundle) }
        }

        if (userPrefs.pancard.isNotNullOrEmpty()) {
            binding.textKycPanNumberProfile.text = userPrefs.pancard
            binding.labelPan.visibility = View.VISIBLE
            binding.panLay.visibility = View.VISIBLE
            if (userPrefs.panRejectReason.isNotNullOrEmpty() && !userPrefs.isUserVerfied) {
                binding.panLay.isSelected = true
                binding.errorPan.visibility = View.VISIBLE
                binding.imageKycDetailPanVerified.setImageDrawable(ContextCompat.getDrawable(activity as Context,R.drawable.ic_vector_error))
                binding.textKycPanNumberProfile.setTextColor(ContextCompat.getColor(activity as Context, R.color.error_red))
                if (userPrefs.panRejectReason.replace(" ", "").equals("Documentunderverification")) {
                    binding.errorPan.text = "Document under verification"
                } else {
                    binding.errorPan.text = "Pan verification failed due to " + userPrefs.panRejectReason
                }
            } else {
                binding.panLay.isSelected = false
                binding.errorPan.visibility = View.GONE
                binding.imageKycDetailPanVerified.setImageDrawable(ContextCompat.getDrawable(activity as Context,R.drawable.ic_vector_circle_check))
                binding.textKycPanNumberProfile.setTextColor(ContextCompat.getColor(activity as Context, R.color.faded_black))
            }
        } else {
            binding.labelPan.visibility = View.GONE
            binding.panLay.visibility = View.GONE
            binding.errorPan.visibility = View.GONE
        }

        if (userPrefs.identityType.isNotNullOrEmpty()) {
            if (userPrefs.identityType.equals("gst")) {
                if (userPrefs.gstNumber.isNotNullOrEmpty()) {
                    binding.labelGst.text = "GST Number"
                    binding.textKycGstNumberProfile.text = userPrefs.gstNumber
                    if (userPrefs.identityRejectReason.isNotNullOrEmpty() && userPrefs.identityRejectReason.replace(" ", "").equals("Documentunderverification")) {
                        setGstError("Document under verification")
                    } else {
                        setGstError("GST verification failed due to " + userPrefs.identityRejectReason)
                    }
                } else {
                    binding.labelGst.visibility = View.GONE
                    binding.gstLay.visibility = View.GONE
                    binding.errorGst.visibility = View.GONE
                }
            } else if (userPrefs.identityType.equals("aadhaar")) {
                if (userPrefs.aadhaarNumber.isNotNullOrEmpty()) {
                    binding.labelGst.text = "Aadhaar Number"
                    binding.textKycGstNumberProfile.text = userPrefs.aadhaarNumber
                    if (userPrefs.identityRejectReason.isNotNullOrEmpty() && userPrefs.identityRejectReason.replace(" ", "").equals("Documentunderverification")) {
                        setGstError("Document under verification")
                    } else {
                        setGstError("Aadhaar verification failed due to " + userPrefs.identityRejectReason)
                    }
                } else {
                    binding.labelGst.visibility = View.GONE
                    binding.gstLay.visibility = View.GONE
                    binding.errorGst.visibility = View.GONE
                }
            } else if (userPrefs.identityType.equals("cin")) {
                if (userPrefs.cinNumber.isNotNullOrEmpty()) {
                    binding.labelGst.text = "CIN"
                    binding.textKycGstNumberProfile.text = userPrefs.cinNumber
                    if (userPrefs.identityRejectReason.isNotNullOrEmpty() && userPrefs.identityRejectReason.replace(" ", "").equals("Documentunderverification")) {
                        setGstError("Document under verification")
                    } else {
                        setGstError("CIN verification failed due to " + userPrefs.identityRejectReason)
                    }
                } else {
                    binding.labelGst.visibility = View.GONE
                    binding.gstLay.visibility = View.GONE
                    binding.errorGst.visibility = View.GONE
                }
            } else if (userPrefs.identityType.equals("udyog_aadhaar")) {
                if (userPrefs.udyogNumber.isNotNullOrEmpty()) {
                    binding.labelGst.text = "Udyog Aadhaar"
                    binding.textKycGstNumberProfile.text = userPrefs.udyogNumber
                    if (userPrefs.identityRejectReason.isNotNullOrEmpty() && userPrefs.identityRejectReason.replace(" ", "").equals("Documentunderverification")) {
                        setGstError("Document under verification")
                    } else {
                        setGstError("Udyog Aadhaar verification failed due to " + userPrefs.identityRejectReason)
                    }
                } else {
                    binding.labelGst.visibility = View.GONE
                    binding.gstLay.visibility = View.GONE
                    binding.errorGst.visibility = View.GONE
                }
            } else if (userPrefs.identityType.equals("shop_establishment")) {
                if (userPrefs.shopNumber.isNotNullOrEmpty()) {
                    binding.labelGst.text = "Shop Establishment"
                    binding.textKycGstNumberProfile.text = userPrefs.shopNumber
                    if (userPrefs.identityRejectReason.isNotNullOrEmpty() && userPrefs.identityRejectReason.replace(" ", "").equals("Documentunderverification")) {
                        setGstError("Document under verification")
                    } else {
                        setGstError("Shop Establishment verification failed due to " + userPrefs.identityRejectReason)
                    }
                } else {
                    binding.labelGst.visibility = View.GONE
                    binding.gstLay.visibility = View.GONE
                    binding.errorGst.visibility = View.GONE
                }
            } else {
                binding.labelGst.visibility = View.GONE
                binding.gstLay.visibility = View.GONE
                binding.errorGst.visibility = View.GONE
            }
        } else {
            binding.labelGst.visibility = View.GONE
            binding.gstLay.visibility = View.GONE
            binding.errorGst.visibility = View.GONE
        }

        if (userPrefs.businessAddress.isNotNullOrEmpty()) {
            binding.textKycAddressProfile.text = userPrefs.businessAddress
            setAddressError()
        } else {
            if (userPrefs.getAddressList().isNullOrEmpty()) {
                binding.labelAddress.visibility = View.GONE
                binding.addressLay.visibility = View.GONE
                binding.errorAddress.visibility = View.GONE
            } else {
                if (userPrefs.getAddressList()?.get(0) != null) {
                    binding.textKycAddressProfile.text = userPrefs.getAddressList()?.get(0)?.address
                    setAddressError()
                } else {
                    binding.labelAddress.visibility = View.GONE
                    binding.addressLay.visibility = View.GONE
                    binding.errorAddress.visibility = View.GONE
                }
            }
        }

        if (userPrefs.businessType.isNotNullOrEmpty()) {

            if(userPrefs.rcRejectReason.isNotNullOrEmpty() && !userPrefs.isUserVerfied){
                binding.truckRcLay.isSelected = true
                binding.errorTruck.visibility = View.VISIBLE
                binding.imageRc.setImageDrawable(ContextCompat.getDrawable(activity as Context,R.drawable.ic_vector_error))
                binding.textRc.setTextColor(ContextCompat.getColor(activity as Context, R.color.error_red))
                if(userPrefs.rcRejectReason.replace(" ", "").equals("Documentunderverification")){
                    binding.errorTruck.text = "Document under verification"
                }else {
                    binding.errorTruck.text = "Business verification failed due to " + userPrefs.rcRejectReason
                }
            }else{
                binding.truckRcLay.isSelected = false
                binding.errorTruck.visibility = View.GONE
                binding.imageRc.setImageDrawable(ContextCompat.getDrawable(activity as Context,R.drawable.ic_vector_circle_check))
                binding.textRc.setTextColor(ContextCompat.getColor(activity as Context, R.color.faded_black))
            }

            if (userPrefs.businessType.equals("rc")) {
                if(userPrefs.rcNumber.isEmpty()){
                    binding.tvRc.visibility = View.GONE
                    binding.truckRcLay.visibility = View.GONE
                    binding.businessRadio.visibility = View.GONE
                    binding.errorTruck.visibility = View.GONE
                }else{
                    binding.businessRadio.visibility = View.VISIBLE
                    binding.tvRc.visibility = View.VISIBLE
                    binding.truckRcLay.visibility = View.VISIBLE
                    binding.textRc.text = userPrefs.rcNumber
                    binding.rcRadio.isChecked = true
                    binding.rcRadio.isEnabled = true
                    binding.lrRadio.isEnabled = false
                }
            }else{
                binding.tvRc.visibility = View.VISIBLE
                binding.truckRcLay.visibility = View.GONE
                binding.lrRadio.isChecked = true
                binding.lrRadio.isEnabled = true
                binding.rcRadio.isEnabled = false
            }

        }else{
            binding.tvRc.visibility = View.GONE
            binding.truckRcLay.visibility = View.GONE
            binding.businessRadio.visibility = View.GONE
            binding.errorTruck.visibility = View.GONE
        }

    }

    override fun onResume() {
        super.onResume()
        if (fragmentSetupTrace != null && isFirstResume) {
            fragmentSetupTrace?.stop()
            isFirstResume = false
        }
    }

    private fun setAddressError(){
        binding.labelAddress.visibility = View.VISIBLE
        binding.addressLay.visibility = View.VISIBLE
        if(userPrefs.addressRejectReason.isNotNullOrEmpty() && !userPrefs.isUserVerfied){
            binding.addressLay.isSelected = true
            binding.errorAddress.visibility = View.VISIBLE
            binding.imageKycDetailAddressVerified.setImageDrawable(ContextCompat.getDrawable(activity as Context, R.drawable.ic_vector_error))
            binding.textKycAddressProfile.setTextColor(ContextCompat.getColor(activity as Context, R.color.error_red))
            if(userPrefs.addressRejectReason.replace(" ", "").equals("Documentunderverification")){
                binding.errorAddress.text = "Document under verification"
            }else {
                binding.errorAddress.text = "Address verification failed due to " + userPrefs.addressRejectReason
            }
        }else{
            binding.addressLay.isSelected = false
            binding.errorAddress.visibility = View.GONE
            binding.imageKycDetailAddressVerified.setImageDrawable(ContextCompat.getDrawable(activity as Context, R.drawable.ic_vector_circle_check))
            binding.textKycAddressProfile.setTextColor(ContextCompat.getColor(activity as Context, R.color.faded_black))
        }
    }

    private fun setGstError(message: String){
        binding.labelGst.visibility = View.VISIBLE
        binding.gstLay.visibility = View.VISIBLE
        if(userPrefs.identityRejectReason.isNotNullOrEmpty() && !userPrefs.isUserVerfied){
            binding.gstLay.isSelected = true
            binding.errorGst.visibility = View.VISIBLE
            binding.imageKycDetailGstVerified.setImageDrawable(ContextCompat.getDrawable(activity as Context, R.drawable.ic_vector_error))
            binding.textKycGstNumberProfile.setTextColor(ContextCompat.getColor(activity as Context, R.color.error_red))
            binding.errorGst.text = message
        }else{
            binding.gstLay.isSelected = false
            binding.errorGst.visibility = View.GONE
            binding.imageKycDetailGstVerified.setImageDrawable(ContextCompat.getDrawable(activity as Context, R.drawable.ic_vector_circle_check))
            binding.textKycGstNumberProfile.setTextColor(ContextCompat.getColor(activity as Context, R.color.faded_black))
        }
    }
}