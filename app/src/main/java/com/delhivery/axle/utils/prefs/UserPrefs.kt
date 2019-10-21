package com.delhivery.axle.utils.prefs

import android.content.Context
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.injection.qualifier.ApplicationContext
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
    set(value) = editor.putString(PrefKeys.JWTToken, value).apply()
    get() = prefs.getString(PrefKeys.JWTToken, null)

  /**
   *  Base/Origin City Code
   */
  var cityCode: String?
    set(value) = editor.putString(PrefKeys.CityCode, value).apply()
    get() = prefs.getString(PrefKeys.CityCode, null)

  var phoneNumber: String?
    set(value) = editor.putString(PrefKeys.PhoneNumber, value).apply()
    get() = prefs.getString(PrefKeys.PhoneNumber, "")

  /**
   * Routes update flag
   */
  var routeUpdate: Boolean
    set(value) = editor.putBoolean(PrefKeys.RouteUpdate, value).apply()
    get() = prefs.getBoolean(PrefKeys.RouteUpdate, false)

  /**
   *  LoggedIn flag
   */
  var hasLoggedIn: Boolean
    set(value) = editor.putBoolean(PrefKeys.HasLoggedIn, value).apply()
    get() = prefs.getBoolean(PrefKeys.HasLoggedIn, false)

  /**
   *  TDS rate
   */
  var tdsRate: Int
    set(value) = editor.putInt(PrefKeys.TdsRate, value).apply()
    get() = prefs.getInt(PrefKeys.TdsRate, 99)

  /**
   *  Username
   */
  var userName: String
    set(value) = editor.putString(PrefKeys.UserName, value).apply()
    get() = prefs.getString(PrefKeys.UserName, "") ?: ""

  /* Bank name */
  var bankName: String
    set(value) = editor.putString(PrefKeys.BankName, value).apply()
    get() = prefs.getString(PrefKeys.BankName, "") ?: ""

  /* Pancard */
  var pancard: String
    set(value) = editor.putString(PrefKeys.Pancard, value).apply()
    get() = prefs.getString(PrefKeys.Pancard, "") ?: ""

  /* Ifsc code */
  var ifscCode: String
    set(value) = editor.putString(PrefKeys.IfscCode, value).apply()
    get() = prefs.getString(PrefKeys.IfscCode, "") ?: ""

  /* Company Name */
  var companyName: String
    set(value) = editor.putString(PrefKeys.CompanyName, value).apply()
    get() = prefs.getString(PrefKeys.CompanyName, "") ?: ""

  /* Account Number */
  var accNumber: String
    set(value) = editor.putString(PrefKeys.AccountNumber, value).apply()
    get() = prefs.getString(PrefKeys.AccountNumber, "") ?: ""

  /**
   *  Has edited routes flag
   */
  var hasEditedRoute: Boolean
    set(value) = editor.putBoolean(PrefKeys.HadEditedRoutes, value).apply()
    get() = prefs.getBoolean(PrefKeys.HadEditedRoutes, false)

  /**
   *  Supplier onbaording status
   */
  var onboardingStatus: String
    set(value) = editor.putString(PrefKeys.OnboardingStatus, value).apply()
    get() = prefs.getString(PrefKeys.OnboardingStatus, "na") ?: "na"

  /**
   *  Supplier enabled flag
   */
  var supplierEnabled: Boolean
    set(value) = editor.putBoolean(PrefKeys.SupplierEnabled, value).apply()
    get() = prefs.getBoolean(PrefKeys.SupplierEnabled, false)

  /**
   *  Is test user flag
   */
  var isTestUser: Boolean
    set(value) = editor.putBoolean(PrefKeys.IsTestUser, value).apply()
    get() = prefs.getBoolean(PrefKeys.IsTestUser, false)

  /**
   *  Device FCM Token
   */
  var fcmTokenGenerated: Boolean
    set(value) = editor.putBoolean(PrefKeys.FCMTokenGenerated, value).apply()
    get() = prefs.getBoolean(PrefKeys.FCMTokenGenerated, false)

  /* Wallet opted in */
  var walletActivated: Boolean
    set(value) = editor.putBoolean(PrefKeys.WalletActive, value).apply()
    get() = prefs.getBoolean(PrefKeys.WalletActive, false)

  /**
   *  Opened from notification flag
   */
  var fromNotification: Boolean
    set(value) = editor.putBoolean(PrefKeys.FromNotification, value).apply()
    get() = prefs.getBoolean(PrefKeys.FromNotification, false)

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
  }

  fun saveUser(user: UserModel) {
    userName = user.name
    onboardingStatus = user.onboardingStatus ?: "na"
    supplierEnabled = user.supplierEnabled
    isTestUser = user.testUser
    tdsRate = user.getTDS()
    bankName = user.bank ?: ""
    companyName = user.companyName ?: ""
    phoneNumber = user.phoneNo
    ifscCode = user.ifscCode ?: ""
    pancard = user.panCardNo ?: ""
    accNumber = user.accNumber()
    cityCode = if (user.hasRoutes()) {
      user.userRoutes()[0].origin.cityId
    } else {
      user.baseCityCode
    }
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
   * Pref keys
   */
  internal object PrefKeys {
    const val JWTToken = "jwt_token"
    const val CityCode = "city_code"
    const val RouteUpdate = "route_update"
    const val PhoneNumber = "phone_number"
    const val HasLoggedIn = "has_logged_in"
    const val TdsRate = "tds_rate"
    const val UserName = "user_name"
    const val Pancard = "pan_card"
    const val BankName = "bank_name"
    const val IfscCode = "ifsc"
    const val CompanyName = "company_name"
    const val AccountNumber = "acc_num"
    const val HadEditedRoutes = "has_edited_routes"
    const val OnboardingStatus = "onboarding_status"
    const val SupplierEnabled = "supplier_enabled"
    const val IsTestUser = "test_user"
    const val FCMTokenGenerated = "fcm_token_generated"
    const val WalletActive = "wallet_active"
    const val FromNotification = "from_notification"
  }
}

const val DISABLED = 1
const val UNAPPROVED = 2
const val APPROVED = 3