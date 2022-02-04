package com.delhivery.axle.data

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
        @SerializedName("supplier_details") var supplier_details: SupplierModel?,
        @SerializedName("client_details") var client_details: ClientModel?,
        @SerializedName("uuid") var userId: String,
        @SerializedName("is_sp_enabled") var is_sp_enabled: Boolean = false,
        @SerializedName("is_location_enabled") var is_location_enabled: Boolean = false,
        @SerializedName("is_client_enabled") var is_client_enabled: Boolean = false,
        @SerializedName("is_user_verified") var is_user_verified: Boolean = false,
        @SerializedName("pan_number") var pan_number: String?,
        @SerializedName("phone_number") var phone_number: String?,
        @SerializedName("selection_change_count") var selection_change_count: Int?,
        @SerializedName("user_mode") var user_mode: String?,
        @SerializedName("user_role") var user_role: String?,
        @SerializedName("user_type") var user_type: String?,
        @SerializedName("user_name") var user_name: String?,
        @SerializedName("business_name") var business_name: String?,
        @SerializedName("referral_code") var referral_code: String?,
        @SerializedName("receive_whatsapp_notifications") var receive_whatsapp_notifications: Boolean?
 ) : BaseKeyTypeModel<String>(), Serializable {

  override fun key() = userId

  /**
   * User has selected routes or not
   */
  fun hasRoutes() = supplier_details?.routes != null && supplier_details?.routes?.isNotEmpty() ?: false

  /**
   * user routes as {routeModel}
   */
  fun userRoutes(): List<RouteModel> {
    val _routes = supplier_details?.routes?.toRoutes() ?: mutableListOf()
    _routes.sortWith(Comparator { o1, o2 -> o1.origin.city.compareTo(o2.origin.city) })
    return _routes
  }

  /**
   * Returns tds value for user
   */
  fun getTDSSubtractor() = when (supplier_details?.userType) {
    "individual" -> 99
    else -> 98
  }

  /**
   * @return encrypted [accountNo]]
   */
  fun accNumber() =
    if (supplier_details?.accountNo.isNotNullOrEmpty()) {
      val encrypted = StringBuilder()
      val maskLength = (supplier_details?.accountNo?.length ?: 4) - 4
      repeat((maskLength downTo 1).count()) { encrypted.append("*") }
      encrypted.append(supplier_details?.accountNo?.substring(maskLength))
      encrypted.toString()
    } else {
      "Not Available"
    }

  /**
   * @return if user is parent/admin user or not
   */
  fun isParent() = if (supplier_details?.designation.isNotNullOrEmpty()) {
    supplier_details?.designation.equals("ftl_sp_primary")
  } else {
    false
  }

  fun getDieselPreferences() :Boolean = supplier_details?.dieselCardPreferences == "yes"

}

data class ClientModel(
        @SerializedName("client_uuid") var client_uuid: String
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
        @SerializedName("pancard") var panCardNo: String?,
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
        @SerializedName("diesel_company") var dieselCompany : List<String>? = mutableListOf()
)