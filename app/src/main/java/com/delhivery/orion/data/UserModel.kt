package com.delhivery.orion.data

import com.google.gson.annotations.SerializedName

/**
 * User details model
 */
data class UserModel(
  @SerializedName("onboard_status") var onboardingStatus: String?,
  @SerializedName("uuid") var userId: String,
  @SerializedName("name") var name: String?,
  @SerializedName("base_city") var baseCity: String?,
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
  @SerializedName("acccount_no") var acccountNo: String?,
  @SerializedName("ifcs_code") var ifcsCode: String?,
  @SerializedName("payment_mode") var paymentMode: String?
) {

  /**
   * User has selected routes or not
   */
  fun hasRoutes() = routes != null || routes?.isEmpty() == true

  /**
   * user routes as {routeModel}
   */
  fun userRoutes() = routes?.toRoutes() ?: listOf()
}