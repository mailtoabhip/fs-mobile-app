package com.delhivery.axle.data

import com.delhivery.axle.api.request.AddAddressModel
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.prefs.AbstractPreferences

/**
 * User details model
 */

data class UserRespone(
        @SerializedName("count") var count: Int,
        @SerializedName("data") var userModel: List<UserModel>
)
data class UserModel(
        @SerializedName("supplier_details") var supplierDetails: SupplierModel?,
        @SerializedName("client_details") var clientDetails: ClientModel?,
        @SerializedName("uuid") var userId: String,
        @SerializedName("is_sp_enabled") var isSpEnabled: Boolean = false,
        @SerializedName("is_location_enabled") var isLocationEnabled: Boolean = false,
        @SerializedName("is_client_enabled") var isClientEnabled: Boolean = false,
        @SerializedName("is_user_verified") var isUserVerified: Boolean = false,
        @SerializedName("phone_number") var phoneNumber: String?,
        @SerializedName("selection_change_count") var selectionChangeCount: Int?,
        @SerializedName("user_mode") var userMode: String?,
        @SerializedName("user_role") var userRole: String?,
        @SerializedName("user_type") var userType: String?,
        @SerializedName("user_name") var userName: String?,
        @SerializedName("business_name") var businessName: String?,
        @SerializedName("referral_code") var referralCode: String?,
        @SerializedName("other_addresses") var otherAddress: List<AddAddressModel>?,
        @SerializedName("business_address") var businessAddress: String?,
        @SerializedName("is_pan_verified") var isPanVerified: Boolean?,
        @SerializedName("is_aadhaar_verified") var isAadhaarVerified: Boolean?,
        @SerializedName("is_gst_verified") var isGstVerified: Boolean?,
        @SerializedName("is_rc_verified") var isRcVerified: Boolean?,
        @SerializedName("verification_status") var verificationStatus: String?,
        @SerializedName("profile_image_url") var profileImageUrl: String?,
        @SerializedName("phone_no") var phoneNo: String?,
        @SerializedName("name") var name: String?,
        @SerializedName("demand_type") var demandType : List<String>,
        @SerializedName("diesel_card_preference") var dieselCardPreferences: String? = "no",
        @SerializedName("diesel_company") var dieselCompany : List<String>? = mutableListOf(),
        @SerializedName("can_view_third_party_loads") var canViewThirdPartyLoads: Boolean?,
        @SerializedName("is_identity_verified") var isIdentityVerified: Boolean?,
        @SerializedName("is_gsts_by_pan_not_registered") var isGstsByPanNotRegistered: Boolean?,
        @SerializedName("is_trucking_business_doc_uploaded") var isTruckingDocumentUploaded: Boolean?,
        @SerializedName("no_of_verification_issues") var noOfVerificationIssues: String?,
        @SerializedName("identity_doc_url") var identity_doc_url: List<String>? = mutableListOf(),
        @SerializedName("account_holder_name") var accountHolderName: String?

    ) : BaseKeyTypeModel<String>(), Serializable {

  override fun key() = userId

  /**
   * User has selected routes or not
   */
  fun hasRoutes() = supplierDetails?.routes != null && supplierDetails?.routes?.isNotEmpty() ?: false

  /**
   * user routes as {routeModel}
   */
  fun userRoutes(): List<RouteModel> {
    val _routes = supplierDetails?.routes?.toRoutes() ?: mutableListOf()
    _routes.sortWith(Comparator { o1, o2 -> o1.origin.city.compareTo(o2.origin.city) })
    return _routes
  }

  /**
   * Returns tds value for user
   */
  fun getTDSSubtractor() = when (supplierDetails?.userType) {
    "individual" -> 99
    else -> 98
  }

  /**
   * @return encrypted [accountNo]]
   */
  fun accNumber() =
    if (supplierDetails?.accountNo.isNotNullOrEmpty()) {
      val encrypted = StringBuilder()
      val maskLength = (supplierDetails?.accountNo?.length ?: 4) - 4
      repeat((maskLength downTo 1).count()) { encrypted.append("*") }
      encrypted.append(supplierDetails?.accountNo?.substring(maskLength))
      encrypted.toString()
    } else {
      "Not Available"
    }

  /**
   * @return if user is parent/admin user or not
   */
  fun isParent() = if (supplierDetails?.designation.isNotNullOrEmpty()) {
    supplierDetails?.designation.equals("ftl_sp_primary")
  } else {
    false
  }


  fun getDieselPreferences() :Boolean = supplierDetails?.dieselCardPreferences == "yes"

}

data class ClientModel(
        @SerializedName("client_uuid") var client_uuid: String,
        @SerializedName("is_load_board_client") var isLoadBoardClient: Boolean = false,
        @SerializedName("enabled_for_marketplace") var enabledForMarketplace: Boolean = false,
        @SerializedName("enabled_for_orion") var enabledForOrion: Boolean = false,
        @SerializedName("is_deleted") var isDeleted: Boolean = false
)

data class SupplierModel(
        @SerializedName("onboard_status") var onboardingStatus: String?,
        @SerializedName("uuid") var userId: String,
        @SerializedName("name") var name: String,
        @SerializedName("base_city") var baseCity: String,
        @SerializedName("base_city_code") var baseCityCode: String,
        @SerializedName("base_gn_city_code") var baseCityGnCode: String,
        @SerializedName("user_type") var userType: String?,
        @SerializedName("phone_no") var phoneNo: String?,
        @SerializedName("company_name") var companyName: String?,
        @SerializedName("owns_trucks") var ownsTrucks: Boolean,
        @SerializedName("truck_types") var truckTypes: List<String>?,
        @SerializedName("lane_preferences") var routes: List<RouteMappingModel>?,
        @SerializedName("advance_required") var advanceRequired: Boolean,
        @SerializedName("advance_percentage") var advancePercentage: String?,
        @SerializedName("account_proof_url") var accountProofUrl: String?,
        @SerializedName("pancard_url") var panCardUrl: String?,
        @SerializedName("adhaar_url") var adhaarUrl: String?,
        @SerializedName("sec_194_declaration") var sec194DeclarationUrl: String?,
        @SerializedName("gst_number") var gstNumber: String?,
        @SerializedName("aadhaar_number") var aadhaarNumber: String?,
        @SerializedName("rc_number") var rcNumber: String?,
        @SerializedName("pancard") var panNumber: String?,
        @SerializedName("acccount_no") var accountNo: String?,
        @SerializedName("ifcs_code") var ifscCode: String?,
        @SerializedName("bank_name") var bank: String?,
        @SerializedName("payment_mode") var paymentMode: String?,
        @SerializedName("is_supplier_enabled") var supplierEnabled: Boolean = false,
        @SerializedName("is_deleted") var isDeleted: Boolean = false,
        @SerializedName("test_user") var testUser: Boolean = false,
        @SerializedName("designation") var designation: String?,
        @SerializedName("parent_details") var parentDetails: UserModel?,
        @SerializedName("overall_performance") var overallPerformance: String? = "",
        @SerializedName("demand_type") var demandType : List<String>,
        @SerializedName("entity") var vendorEntity: String? = "",
        @SerializedName("diesel_card_preference") var dieselCardPreferences: String? = "no",
        @SerializedName("diesel_company") var dieselCompany : List<String>? = mutableListOf(),
        @SerializedName("is_load_board_supplier") var isLoadBoardSupplier: Boolean = false,
        @SerializedName("enabled_for_marketplace") var enabledForMarketplace: Boolean = false,
        @SerializedName("enabled_for_orion") var enabledForOrion: Boolean = false,
        @SerializedName("vendor_policy_accepted") var vendorPolicyAccepted: Boolean?,
        @SerializedName("aadhaar_policy_accepted") var aadhaarPolicyAccepted: Boolean?,
        @SerializedName("trucking_business_proof_url") var businessDocUrl: String?=null,
        @SerializedName("receive_whatsapp_notifications") var receiveWhatsappNotifications: Boolean,
        @SerializedName("number_of_owned_trucks") var numberOfOwnedTrucks: String?,
        @SerializedName("number_of_attached_trucks") var numberOfAttachedTrucks: String?,
        @SerializedName("cin_number") var cInNumber: String?,
        @SerializedName("udyog_aadhaar_number") var udyogAadhar: String?,
        @SerializedName("shop_establishment_number") var shopEstablishment: String?,
        @SerializedName("pan_holder_name") var panHolderName: String?,
        @SerializedName("beneficiary_name") var accountHolderName: String?


)