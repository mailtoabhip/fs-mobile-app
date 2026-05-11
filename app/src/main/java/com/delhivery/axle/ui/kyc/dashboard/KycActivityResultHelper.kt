package com.delhivery.axle.ui.kyc.dashboard

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.delhivery.axle.ui.businessverification.BusinessVerificationActivity
import com.delhivery.axle.ui.kyc.aadhaar.AadhaarVerificationActivity
import com.delhivery.axle.ui.kyc.address.AddressActivity
import com.delhivery.axle.ui.kyc.address.CommunicationAddressActivity
import com.delhivery.axle.ui.kyc.gst.GstVerificationActivity
import com.delhivery.axle.ui.kyc.identityverification.IdentityVerificationActivity
import com.delhivery.axle.ui.kyc.pan.PanVerificationActivity
import com.delhivery.axle.ui.paymentdetails.PaymentDetailsActivity
import com.delhivery.axle.ui.paymentdetails.VendorPolicyActivity

/**
 * Helper class to manage ActivityResultLaunchers for KYC document verification screens.
 * Must be initialized in onCreate() before the activity is started.
 */
class KycActivityResultHelper(
    private val activity: AppCompatActivity,
    private val onResult: (KycDocType, Boolean) -> Unit
) {
    
    // Launchers for each document type
    lateinit var panLauncher: ActivityResultLauncher<Intent>
    lateinit var aadhaarLauncher: ActivityResultLauncher<Intent>
    lateinit var gstLauncher: ActivityResultLauncher<Intent>
    lateinit var msmeLauncher: ActivityResultLauncher<Intent>
    lateinit var businessProofLauncher: ActivityResultLauncher<Intent>
    lateinit var clientListLauncher: ActivityResultLauncher<Intent>
    lateinit var truckProofLauncher: ActivityResultLauncher<Intent>
    lateinit var paymentLauncher: ActivityResultLauncher<Intent>
    lateinit var declaration194CLauncher: ActivityResultLauncher<Intent>
    lateinit var vendorPolicyLauncher: ActivityResultLauncher<Intent>
    lateinit var itrLauncher: ActivityResultLauncher<Intent>
    
    // Additional launchers for testing different user flows
    lateinit var identityVerificationLauncher: ActivityResultLauncher<Intent>
    lateinit var addressGstLauncher: ActivityResultLauncher<Intent>
    lateinit var addressNonGstLauncher: ActivityResultLauncher<Intent>

    /**
     * Register all launchers. Must be called in onCreate() before activity is started.
     */
    fun registerLaunchers() {
        panLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.PAN_CARD, result) }

        aadhaarLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.AADHAAR_CARD, result) }

        gstLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.GST_DETAILS, result) }

        msmeLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.MSME_CERTIFICATE, result) }

        businessProofLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.BUSINESS_PROOF, result) }

        clientListLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.CLIENT_LIST, result) }

        truckProofLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.TRUCK_BUSINESS_PROOF, result) }

        paymentLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.PAYMENT_DETAILS, result) }

        declaration194CLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.DECLARATION_194C, result) }

        vendorPolicyLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.VENDOR_POLICY, result) }

        itrLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.ITR, result) }
        
        // Additional launchers for testing different user flows
        identityVerificationLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.IDENTITY_BUSINESS_PAN_NO_GST, result) }
        
        addressGstLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.ADDRESS_GST_USER, result) }
        
        addressNonGstLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result -> handleResult(KycDocType.ADDRESS_NON_GST_USER, result) }
    }

    private fun handleResult(docType: KycDocType, result: ActivityResult) {
        val isSuccess = result.resultCode == Activity.RESULT_OK
        onResult(docType, isSuccess)
    }

    /**
     * Launch the appropriate activity for the given document type
     */
    fun launchForDocument(docType: KycDocType) {
        when (docType) {
            KycDocType.PAN_CARD -> {
                panLauncher.launch(Intent(activity, PanVerificationActivity::class.java))
            }
            KycDocType.AADHAAR_CARD -> {
                aadhaarLauncher.launch(Intent(activity, AadhaarVerificationActivity::class.java))
            }
            KycDocType.GST_DETAILS -> {
                gstLauncher.launch(Intent(activity, GstVerificationActivity::class.java))
            }
            KycDocType.MSME_CERTIFICATE -> {
                msmeLauncher.launch(Intent(activity, IdentityVerificationActivity::class.java))
            }
            KycDocType.BUSINESS_PROOF -> {
                businessProofLauncher.launch(Intent(activity, CommunicationAddressActivity::class.java))
            }
            KycDocType.CLIENT_LIST -> {
                // TODO: Create ClientListVerificationActivity
                // clientListLauncher.launch(Intent(activity, ClientListVerificationActivity::class.java))
            }
            KycDocType.TRUCK_BUSINESS_PROOF -> {
                truckProofLauncher.launch(Intent(activity, BusinessVerificationActivity::class.java))
            }
            KycDocType.PAYMENT_DETAILS -> {
//                paymentLauncher.launch(PaymentDetailsActivity.intentForPayment(activity))
            }
            KycDocType.DECLARATION_194C -> {
//                declaration194CLauncher.launch(PaymentDetailsActivity.intentFor194C(activity))
            }
            KycDocType.VENDOR_POLICY -> {
                vendorPolicyLauncher.launch(Intent(activity, VendorPolicyActivity::class.java))
            }
            KycDocType.ITR -> {
                // TODO: Create ItrVerificationActivity
                // itrLauncher.launch(Intent(activity, ItrVerificationActivity::class.java))
            }
            
            // Identity verification flows based on PAN type
            KycDocType.IDENTITY_PERSONAL_PAN_NO_GST -> {
                // Personal PAN + No GST -> Aadhaar verification
                aadhaarLauncher.launch(Intent(activity, AadhaarVerificationActivity::class.java))
            }
            KycDocType.IDENTITY_BUSINESS_PAN_NO_GST -> {
                // Business PAN + No GST -> Identity verification (CIN/Udyog/Shop)
                identityVerificationLauncher.launch(Intent(activity, IdentityVerificationActivity::class.java))
            }
            KycDocType.IDENTITY_HAS_GST -> {
                // Has GST -> GST verification
                gstLauncher.launch(Intent(activity, GstVerificationActivity::class.java))
            }
            
            // Address verification flows
            KycDocType.ADDRESS_GST_USER -> {
                // GST User -> AddressActivity (auto-filled from GST)
                addressGstLauncher.launch(Intent(activity, AddressActivity::class.java))
            }
            KycDocType.ADDRESS_NON_GST_USER -> {
                // Non-GST User -> CommunicationAddressActivity (manual entry)
                addressNonGstLauncher.launch(Intent(activity, CommunicationAddressActivity::class.java))
            }
        }
    }
}

/**
 * Enum for document types used in result handling
 */
enum class KycDocType(val displayName: String, val isImplemented: Boolean) {
    PAN_CARD("PAN Card", true),
    AADHAAR_CARD("Aadhaar Card", true),
    GST_DETAILS("GST Details", true),
    MSME_CERTIFICATE("MSME Certificate", true),
    BUSINESS_PROOF("Business Proof", true),
    CLIENT_LIST("Client List", false),
    TRUCK_BUSINESS_PROOF("Truck Business Proof", true),
    PAYMENT_DETAILS("Payment Details", true),
    DECLARATION_194C("194C Declaration", true),
    VENDOR_POLICY("Vendor Policy", true),
    ITR("ITR", false),
    
    // Identity verification based on PAN type (for testing)
    IDENTITY_PERSONAL_PAN_NO_GST("Identity (Personal PAN + No GST)", true),
    IDENTITY_BUSINESS_PAN_NO_GST("Identity (Business PAN + No GST)", true),
    IDENTITY_HAS_GST("Identity (Has GST)", true),
    
    // Address verification based on user type (for testing)
    ADDRESS_GST_USER("Address (GST User)", true),
    ADDRESS_NON_GST_USER("Address (Non-GST User)", true)
}



