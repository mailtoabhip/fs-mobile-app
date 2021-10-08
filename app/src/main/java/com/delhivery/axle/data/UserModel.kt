package com.delhivery.axle.data

import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.prefs.AbstractPreferences

/**
 * User details model
 */
data class UserModel(
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
) : BaseKeyTypeModel<String>(), Serializable {

  override fun key() = userId

  /**
   * User has selected routes or not
   */
  fun hasRoutes() = routes != null && routes?.isNotEmpty() ?: false

  /**
   * user routes as {routeModel}
   */
  fun userRoutes(): List<RouteModel> {
    val _routes = routes?.toRoutes() ?: mutableListOf()
    _routes.sortWith(Comparator { o1, o2 -> o1.origin.city.compareTo(o2.origin.city) })
    return _routes
  }

  /**
   * Returns tds value for user
   */
  fun getTDSSubtractor() = when (userType) {
    "individual" -> 99
    else -> 98
  }

  /**
   * @return encrypted [accountNo]]
   */
  fun accNumber() =
    if (accountNo.isNotNullOrEmpty()) {
      val encrypted = StringBuilder()
      val maskLength = (accountNo?.length ?: 4) - 4
      repeat((maskLength downTo 1).count()) { encrypted.append("*") }
      encrypted.append(accountNo?.substring(maskLength))
      encrypted.toString()
    } else {
      "Not Available"
    }

  /**
   * @return if user is parent/admin user or not
   */
  fun isParent() = if (designation.isNotNullOrEmpty()) {
    designation.equals("ftl_sp_primary")
  } else {
    false
  }

  fun getDieselPreferences() :Boolean = dieselCardPreferences == "yes"

}