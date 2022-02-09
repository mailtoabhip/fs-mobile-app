package com.delhivery.axle.utils.prefs

import android.content.Context
import com.auth0.android.jwt.JWT
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.injection.qualifier.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User preferences
 */
@Singleton
class UserPrefs @Inject constructor(@ApplicationContext private val context: Context) : BasePrefs(
    context
) {
  override fun prefsName() = PrefNames.UserPrefs

  /**
   *  JWT Token
   */
  var jwtToken: String?
    set(value) = editor.putString(PrefKeys.JWTToken, value)
        .apply()
    get() = prefs.getString(PrefKeys.JWTToken, null)

  /**
   *  Base/Origin City Code
   */
  var cityCode: String?
    set(value) = editor.putString(PrefKeys.CityCode, value)
        .apply()
    get() = prefs.getString(PrefKeys.CityCode, null)

  /**
   *  Base/Origin City Code
   */
  var gnCityCode: String?
    set(value) = editor.putString(PrefKeys.GNCityCode, value)
        .apply()
    get() = prefs.getString(PrefKeys.GNCityCode, null)

  /**
   *  User phone number
   */
  var phoneNumber: String?
    set(value) = editor.putString(PrefKeys.PhoneNumber, value)
        .apply()
    get() = prefs.getString(PrefKeys.PhoneNumber, "")

  /**
   * Routes update flag
   */
  var routeUpdate: Boolean
    set(value) = editor.putBoolean(PrefKeys.RouteUpdate, value)
        .apply()
    get() = prefs.getBoolean(PrefKeys.RouteUpdate, false)

  /**
   *  LoggedIn flag
   */
  var hasLoggedIn: Boolean
    set(value) = editor.putBoolean(PrefKeys.HasLoggedIn, value)
        .apply()
    get() = prefs.getBoolean(PrefKeys.HasLoggedIn, false)

  /**
   *  TDS rate
   */
  var tdsRate: Int
    set(value) = editor.putInt(PrefKeys.TdsRate, value)
        .apply()
    get() = prefs.getInt(PrefKeys.TdsRate, 99)

  /**
   * updated tds rate
   */
  var updatedTdsRate: Double
    set(value) = editor.putFloat(PrefKeys.UpdateTdsRate, value.toFloat())
        .apply()
    get() = prefs.getFloat(PrefKeys.UpdateTdsRate, 99.25F)
        .toDouble()

  /**
   *  Username
   */
  var userName: String
    set(value) = editor.putString(PrefKeys.UserName, value)
        .apply()
    get() = prefs.getString(PrefKeys.UserName, "") ?: ""

  /**
   *Bank name
   */
  var bankName: String
    set(value) = editor.putString(PrefKeys.BankName, value)
        .apply()
    get() = prefs.getString(PrefKeys.BankName, "") ?: ""

  /**
   *  Pancard
   */
  var pancard: String
    set(value) = editor.putString(PrefKeys.Pancard, value)
        .apply()
    get() = prefs.getString(PrefKeys.Pancard, "") ?: ""

  /**
   *  Ifsc code
   */
  var ifscCode: String
    set(value) = editor.putString(PrefKeys.IfscCode, value)
        .apply()
    get() = prefs.getString(PrefKeys.IfscCode, "") ?: ""

  /**
   *  Company Name
   */
  var companyName: String
    set(value) = editor.putString(PrefKeys.CompanyName, value)
        .apply()
    get() = prefs.getString(PrefKeys.CompanyName, "") ?: ""

  /**
   *  Account Number
   */
  var accNumber: String
    set(value) = editor.putString(PrefKeys.AccountNumber, value)
        .apply()
    get() = prefs.getString(PrefKeys.AccountNumber, "") ?: ""

  /**
   * User type
   */
  var userType: String
    set(value) = editor.putString(PrefKeys.UserType, value).apply()
    get() = prefs.getString(PrefKeys.UserType, "") ?: ""

  /**
   * User Performance
   */
  var userPerformance: String
    set(value) = editor.putString(PrefKeys.UserOverallPerformance, value).apply()
    get() = prefs.getString(PrefKeys.UserOverallPerformance, "") ?: ""


  /**
   *  Has edited routes flag
   */
  var hasEditedRoute: Boolean
    set(value) = editor.putBoolean(PrefKeys.HadEditedRoutes, value)
        .apply()
    get() = prefs.getBoolean(PrefKeys.HadEditedRoutes, false)

  /**
   *  Supplier onbaording status
   */
  var onboardingStatus: String
    set(value) = editor.putString(PrefKeys.OnboardingStatus, value)
        .apply()
    get() = prefs.getString(PrefKeys.OnboardingStatus, "na") ?: "na"

  /**
   *  Supplier enabled flag
   */
  var supplierEnabled: Boolean
    set(value) = editor.putBoolean(PrefKeys.SupplierEnabled, value)
        .apply()
    get() = prefs.getBoolean(PrefKeys.SupplierEnabled, false)

  /**
   *  Is test user flag
   */
  var isTestUser: Boolean
    set(value) = editor.putBoolean(PrefKeys.IsTestUser, value)
        .apply()
    get() = prefs.getBoolean(PrefKeys.IsTestUser, false)

  /**
   *  Device FCM Token
   */
  var fcmTokenGenerated: Boolean
    set(value) = editor.putBoolean(PrefKeys.FCMTokenGenerated, value)
        .apply()
    get() = prefs.getBoolean(PrefKeys.FCMTokenGenerated, false)

  /**
   *  Wallet opted in
   */
  var walletActivated: Boolean
    set(value) = editor.putBoolean(PrefKeys.WalletActive, value)
        .apply()
    get() = prefs.getBoolean(PrefKeys.WalletActive, false)

  /**
   *  Opened from notification flag
   */
  var fromNotification: Boolean
    set(value) = editor.putBoolean(PrefKeys.FromNotification, value)
        .apply()
    get() = prefs.getBoolean(PrefKeys.FromNotification, false)

  /**
   *  Max PMT rate
   */
  var maxPMTRate: Int
    set(value) = editor.putInt(PrefKeys.MaxPMTRate, value)
        .apply()
    get() = prefs.getInt(PrefKeys.MaxPMTRate, Integer.MAX_VALUE)

  /**
   *  Max cost per km
   */
  var maxCostPerKM: Int
    set(value) = editor.putInt(PrefKeys.MaxCostPerKM, value)
        .apply()
    get() = prefs.getInt(PrefKeys.MaxCostPerKM, Integer.MAX_VALUE)

  /**
   * Is logged in user parent or not
   */
  var isParent: Boolean
    set(value) = editor.putBoolean(PrefKeys.IsParent, value)
        .apply()
    get() = prefs.getBoolean(PrefKeys.IsParent, false)

  /**
   * Vendor truck types
   */
  var truckTypes: String?
    set(value) = editor.putString(PrefKeys.TruckTypes, value)
        .apply()
    get() = prefs.getString(PrefKeys.TruckTypes, "")

  /**
   * Vendor type (Fleet / Orion / Internal)
   */
  var demandType: String
    set(value) = editor.putString(PrefKeys.DemandType, value)
        .apply()
    get() = prefs.getString(PrefKeys.DemandType, "")!!

  var logoutStatus: String
    set(value) = editor.putString(PrefKeys.LogoutStatus, value)
            .apply()
    get() = prefs.getString(PrefKeys.LogoutStatus, "") ?: ""

  /**
   *  Start Time
   */
  var startTime: Long
    set(value) = editor.putLong(PrefKeys.StartTime, value)
            .apply()
    get() = prefs.getLong(PrefKeys.StartTime , 0)

  /**
   *  Start Time
   */
  var lastLoginTime: Long
    set(value) = editor.putLong(PrefKeys.LastLoginTime, value)
            .apply()
    get() = prefs.getLong(PrefKeys.LastLoginTime , Date().time)

  var firstRoute: Boolean
    set(value) = editor.putBoolean(PrefKeys.IsFirstRoute, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.IsFirstRoute, false)

  /**
   * Vendor Entity
   */
  var vendorEntity: String
    set(value) = editor.putString(PrefKeys.VendorEntity, value)
            .apply()
    get() = prefs.getString(PrefKeys.VendorEntity , " ") ?: ""

  var dpLinkArg: String
    set(value) = editor.putString(PrefKeys.DeepLinkArg ,value)
            .apply()
    get() = prefs.getString(PrefKeys.DeepLinkArg, "") ?: ""

  /**
   *  Account set up
   */
  var accountSetup: Boolean
    set(value) = editor.putBoolean(PrefKeys.AccountSetup, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.AccountSetup, false)

  /**
   *  User role
   */
  var userRole: String
    set(value) = editor.putString(PrefKeys.UserRole, value)
            .apply()
    get() = prefs.getString(PrefKeys.UserRole, "") ?: ""

  /**
   *  User mode
   */
  var userMode: String
    set(value) = editor.putString(PrefKeys.UserMode, value)
            .apply()
    get() = prefs.getString(PrefKeys.UserMode, "") ?: ""


    /**
     *  Kyc for load post
     */
    var loadPostKyc: String
        set(value) = editor.putString(PrefKeys.LoadPostKyc, value)
            .apply()
        get() = prefs.getString(PrefKeys.LoadPostKyc , " ") ?: ""

    /**
     *  Kyc for load post
     */
    var truckPostKyc: String
        set(value) = editor.putString(PrefKeys.TruckPostKyc, value)
            .apply()
        get() = prefs.getString(PrefKeys.TruckPostKyc , " ") ?: ""

    /**
     * Is user verified
     */
    var isUserVerfied: Boolean
        set(value) = editor.putBoolean(PrefKeys.IsUserVerfied, value)
            .apply()
        get() = prefs.getBoolean(PrefKeys.IsUserVerfied, false)

  /**
   * Clear all preferences
   */
  fun clearPrefs() {
    editor.remove(PrefKeys.JWTToken)
        .apply()
    editor.remove(PrefKeys.OnboardingStatus)
        .apply()
    editor.remove(PrefKeys.SupplierEnabled)
        .apply()
    editor.remove(PrefKeys.IsTestUser)
        .apply()
    editor.remove(PrefKeys.RouteUpdate)
        .apply()
    editor.remove(PrefKeys.PhoneNumber)
        .apply()
    editor.remove(PrefKeys.HasLoggedIn)
        .apply()
    editor.remove(PrefKeys.TdsRate)
        .apply()
    editor.remove(PrefKeys.UpdateTdsRate)
        .apply()
    editor.remove(PrefKeys.UserName)
        .apply()
    editor.remove(PrefKeys.BankName)
        .apply()
    editor.remove(PrefKeys.AccountNumber)
        .apply()
    editor.remove(PrefKeys.CompanyName)
        .apply()
    editor.remove(PrefKeys.PhoneNumber)
        .apply()
    editor.remove(PrefKeys.IfscCode)
        .apply()
    editor.remove(PrefKeys.Pancard)
        .apply()
    editor.remove(PrefKeys.CityCode)
        .apply()
    editor.remove(PrefKeys.GNCityCode)
        .apply()
    editor.remove(PrefKeys.MaxPMTRate)
        .apply()
    editor.remove(PrefKeys.MaxCostPerKM)
        .apply()
    editor.remove(PrefKeys.IsParent)
        .apply()
    editor.remove(PrefKeys.TruckTypes)
        .apply()
    editor.remove(PrefKeys.DemandType)
    editor.remove(PrefKeys.LogoutStatus)
        .apply()
    editor.remove(PrefKeys.StartTime)
        .apply()
    editor.remove((PrefKeys.LastLoginTime))
        .apply()
    editor.remove(PrefKeys.IsFirstRoute)
        .apply()
    editor.remove(PrefKeys.UserOverallPerformance)
        .apply()
    editor.remove(PrefKeys.VendorEntity)
        .apply()
    editor.remove(PrefKeys.DeepLinkArg)
        .apply()
      editor.remove(PrefKeys.LoadPostKyc)
          .apply()
    editor.remove(PrefKeys.AccountSetup)
            .apply()
    editor.remove(PrefKeys.UserMode)
            .apply()
    editor.remove(PrefKeys.UserRole)
            .apply()
      editor.remove(PrefKeys.IsUserVerfied)
          .apply()
    editor.commit()
  }

  fun saveUser(user: UserModel) {
    userName = user.userName?:""
    onboardingStatus = user.supplierDetails?.onboardingStatus ?: "na"
    supplierEnabled = user.isSpEnabled
    isTestUser = user.supplierDetails?.testUser == true
    tdsRate = user.getTDSSubtractor()
    updatedTdsRate =
      if (user.getTDSSubtractor() == 99) user.getTDSSubtractor() + 0.25 else user.getTDSSubtractor() + 0.5
    bankName = user.supplierDetails?.bank ?: ""
    companyName = user.businessName ?: ""
    phoneNumber = user.phoneNumber
    ifscCode = user.supplierDetails?.ifscCode ?: ""
    pancard = user.panNumber ?: ""
    accNumber = user.accNumber()
    cityCode = user.supplierDetails?.baseCityCode
    isParent = user.isParent()
    userType = user.userType ?: ""
    truckTypes = if (user.isParent()) {
      user.supplierDetails?.truckTypes?.joinToString(separator = ",") {it}
    } else {
      user.supplierDetails?.parentDetails?.supplierDetails?.truckTypes?.joinToString(separator = ",") {it}
    }
    demandType = user.supplierDetails?.demandType?.joinToString(separator = ",") {it}.toString()
    userPerformance = user.supplierDetails?.overallPerformance ?: ""
    vendorEntity = user.supplierDetails?.vendorEntity ?: ""

    userMode = user.userMode?: ""
    userRole = user.userRole?: ""
      isUserVerfied = user.isUserVerified

  }

  fun canBid() = if (supplierEnabled) {
    when (onboardingStatus) {
      "approved" -> APPROVED
      else -> UNAPPROVED
    }
  } else {
    DISABLED
  }


  /**
   * Current user id
   */
  fun userId() = jwtToken?.let { JWT(it).let { (it.claims["sub"]?.asString()!!) } } ?: ""

  /**
   * Pref keys
   */
  internal object PrefKeys {
    const val JWTToken = "jwt_token"
    const val CityCode = "city_code"
    const val GNCityCode = "gn_city_code"
    const val RouteUpdate = "route_update"
    const val PhoneNumber = "phone_number"
    const val HasLoggedIn = "has_logged_in"
    const val TdsRate = "tds_rate"
    const val UpdateTdsRate = "updated_tds_rate"
    const val UserName = "user_name"
    const val Pancard = "pan_card"
    const val BankName = "bank_name"
    const val IfscCode = "ifsc"
    const val CompanyName = "business_name"
    const val AccountNumber = "acc_num"
    const val HadEditedRoutes = "has_edited_routes"
    const val OnboardingStatus = "onboarding_status"
    const val SupplierEnabled = "supplier_enabled"
    const val IsTestUser = "test_user"
    const val FCMTokenGenerated = "fcm_token_generated"
    const val WalletActive = "wallet_active"
    const val FromNotification = "from_notification"
    const val MaxPMTRate = "max_pmt_rate"
    const val MaxCostPerKM = "max_cost_per_km"
    const val IsParent = "is_parent"
    const val UserType = "user_type"
    const val TruckTypes = "truck_types"
    const val DemandType = "demand_type"
    const val LogoutStatus = "logout_status"
    const val StartTime = "start_time"
    const val LastLoginTime = "last_login_time"
    const val IsFirstRoute = "first_route"
    const val UserOverallPerformance = "overall_performance"
    const val VendorEntity = "vendor_entity"
    const val DeepLinkArg = "deep_link_argument"
    const val LoadPostKyc = "load_post"
    const val TruckPostKyc = "truck_post"
    const val AccountSetup = "account_set_up"
    const val UserRole = "user_role"
    const val UserMode = "user_mode"
      const val IsUserVerfied = "is_user_verified"

  }
}

const val DISABLED = 1
const val UNAPPROVED = 2
const val APPROVED = 3