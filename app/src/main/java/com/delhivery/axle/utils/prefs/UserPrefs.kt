package com.delhivery.axle.utils.prefs

import android.content.Context
import com.auth0.android.jwt.JWT
import com.delhivery.axle.api.request.AddAddressModel
import com.delhivery.axle.data.RouteMappingModel
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.injection.qualifier.ApplicationContext
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFragment
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
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

  companion object {
    // Cache TypeToken instances as static fields to prevent ProGuard/R8 obfuscation issues
    private val LIST_OF_ADD_ADDRESS_MODEL_TYPE: Type = object : TypeToken<List<AddAddressModel?>?>() {}.type
    private val LIST_OF_ROUTE_MAPPING_MODEL_TYPE: Type = object : TypeToken<List<RouteMappingModel?>?>() {}.type
  }

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
  var cityName: String?
    set(value) = editor.putString(PrefKeys.CityName, value)
      .apply()
    get() = prefs.getString(PrefKeys.CityName, null)

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
   *  Device FCM Token sent to moengage
   */
  var moengageFcmTokenGenerated: Boolean
    set(value) = editor.putBoolean(PrefKeys.MoengageFCMTokenGenerated, value)
      .apply()
    get() = prefs.getBoolean(PrefKeys.MoengageFCMTokenGenerated, false)

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

  var orderRank: Int
    set(value) = editor.putInt(PrefKeys.orderRank, value)
        .apply()
    get() = prefs.getInt(PrefKeys.orderRank, 0)

  /**
   *  Max cost per km
   */
  var maxCostPerKM: Int
    set(value) = editor.putInt(PrefKeys.MaxCostPerKM, value)
            .apply()
    get() = prefs.getInt(PrefKeys.MaxCostPerKM, Integer.MAX_VALUE)

  var bidOfferCount: Int
    set(value) = editor.putInt(PrefKeys.BidOfferCount, value)
        .apply()
    get() = prefs.getInt(PrefKeys.BidOfferCount, 0)

  var trucksOfferCount: Int
    set(value) = editor.putInt(PrefKeys.TrucksOfferCount, value)
        .apply()
    get() = prefs.getInt(PrefKeys.TrucksOfferCount, Integer.MAX_VALUE)

  var rateOfferCount: Int
    set(value) = editor.putInt(PrefKeys.RateOfferCount, value)
        .apply()
    get() = prefs.getInt(PrefKeys.RateOfferCount, Integer.MAX_VALUE)

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
   * Vendor type (fleet_owner or broker)
   */
  var vendorType: String?
    set(value) = editor.putString(PrefKeys.VendorType, value)
            .apply()
    get() = prefs.getString(PrefKeys.VendorType, null)

  /**
   * Route type (local or national)
   */
  var routeType: String?
    set(value) = editor.putString(PrefKeys.RouteType, value)
            .apply()
    get() = prefs.getString(PrefKeys.RouteType, null)

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
     * First time login with Vendor Entity as RP/BOTH
     */
    var firstLoginRPUser: Boolean
        set(value) = editor.putBoolean(PrefKeys.IsFirstLoginRPUser, value)
            .apply()
        get() = prefs.getBoolean(PrefKeys.IsFirstLoginRPUser, true)
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

    var parentId:String
        set(value) = editor.putString(PrefKeys.ParentId ,value)
            .apply()
        get() = prefs.getString(PrefKeys.ParentId, "") ?: ""

    var parentName:String
        set(value) = editor.putString(PrefKeys.ParentName ,value)
            .apply()
        get() = prefs.getString(PrefKeys.ParentName, "") ?: ""

    var parentDemandType:String?
        set(value) = editor.putString(PrefKeys.ParentDemandType ,value)
            .apply()
        get() = prefs.getString(PrefKeys.ParentDemandType, "") ?: ""
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
  var shareRateBannerH1: String
    set(value) = editor.putString(PrefKeys.ShareRateBannerH1, value)
        .apply()
    get() = prefs.getString(PrefKeys.ShareRateBannerH1 , " ") ?: ""
  var shareRateBannerH2: String
    set(value) = editor.putString(PrefKeys.ShareRateBannerH2, value)
        .apply()
    get() = prefs.getString(PrefKeys.ShareRateBannerH2 , " ") ?: ""
  var shareRateBannerH3: String
    set(value) = editor.putString(PrefKeys.ShareRateBannerH3, value)
        .apply()
    get() = prefs.getString(PrefKeys.ShareRateBannerH3 , " ") ?: ""

  var podAddress: String
    set(value) = editor.putString(PrefKeys.PodAddress, value)
        .apply()
    get() = prefs.getString(PrefKeys.PodAddress , " ") ?: ""


  /**
     * identity doc url
     */
    var identityDocUrl: String
        set(value) = editor.putString(PrefKeys.identityDocUrl ,value)
            .apply()
        get() = prefs.getString(PrefKeys.identityDocUrl, "") ?: ""
  var businessDocUrl: String
    set(value) = editor.putString(PrefKeys.businessDocUrl ,value)
        .apply()
    get() = prefs.getString(PrefKeys.businessDocUrl, "") ?: ""

  var paymentDocUrl: String
    set(value) = editor.putString(PrefKeys.paymentDocUrl ,value)
        .apply()
    get() = prefs.getString(PrefKeys.paymentDocUrl, "") ?: ""
  var ninteen4CDocUrl: String
    set(value) = editor.putString(PrefKeys.ninteen4CDocUrl ,value)
        .apply()
    get() = prefs.getString(PrefKeys.ninteen4CDocUrl, "") ?: ""


  /**
   * Is user verified
   */
  var isUserVerfied: Boolean
    set(value) = editor.putBoolean(PrefKeys.IsUserVerfied, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.IsUserVerfied, false)

//communication address is as same as Gst
    var isSameAsGst: Boolean
        set(value) = editor.putBoolean(PrefKeys.isSameAsGst, value)
            .apply()
        get() = prefs.getBoolean(PrefKeys.isSameAsGst, false)

    /**
   * gst number
   */
  var gstNumber: String
    set(value) = editor.putString(PrefKeys.gstNumber ,value)
            .apply()
    get() = prefs.getString(PrefKeys.gstNumber, "") ?: ""

  /**
   * aadhaar number
   */
  var aadhaarNumber: String
    set(value) = editor.putString(PrefKeys.aadhaarNumber ,value)
            .apply()
    get() = prefs.getString(PrefKeys.aadhaarNumber, "") ?: ""

  /**
   * business address
   */
  var businessAddress: String
    set(value) = editor.putString(PrefKeys.businessAddress ,value)
            .apply()
    get() = prefs.getString(PrefKeys.businessAddress, "") ?: ""

    /**
   * gst address
   */

  fun setAddressList(addlist: List<AddAddressModel>?){
    val gson = Gson()
    val json = gson.toJson(addlist)
    editor.putString(PrefKeys.gstAddress,json)
            .apply()
  }


  fun getAddressList(): List<AddAddressModel?>? {
    var arrayItems: List<AddAddressModel?>? = null
    val serializedObject: String? = prefs.getString(PrefKeys.gstAddress, null)
    if (serializedObject != null) {
      val gson = Gson()
      arrayItems = gson.fromJson<List<AddAddressModel>>(serializedObject, LIST_OF_ADD_ADDRESS_MODEL_TYPE)
    }
    return arrayItems
  }

  fun setLanesPreferences(lanesList: List<RouteMappingModel>?){
    val gson = Gson()
    val json = gson.toJson(lanesList)
    editor.putString(PrefKeys.lanesPreference,json)
      .apply()
  }


  fun getLanesPreference(): List<RouteMappingModel?>? {
    var arrayItems: List<RouteMappingModel?>? = null
    val serializedObject: String? = prefs.getString(PrefKeys.lanesPreference, null)
    if (serializedObject != null) {
      val gson = Gson()
      arrayItems = gson.fromJson<List<RouteMappingModel>>(serializedObject, LIST_OF_ROUTE_MAPPING_MODEL_TYPE)
    }
    return arrayItems
  }


  /**
   *  pan verified
   */
  var isPanVerfied: Boolean
    set(value) = editor.putBoolean(PrefKeys.isPanVerified, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.isPanVerified, false)

  /**
   *  gst verified
   */
  var isGstVerfied: Boolean
    set(value) = editor.putBoolean(PrefKeys.isGstVerified, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.isGstVerified, false)

    /**
     *  is gst verification step not bypassed
     */
    var isGstNotBypassed: Boolean
        set(value) = editor.putBoolean(PrefKeys.isGstNotBypassed, value)
            .apply()
        get() = prefs.getBoolean(PrefKeys.isGstNotBypassed, isGstVerfied)

  /**
   *  aadhaar verified
   */
  var isAadhaartVerfied: Boolean
    set(value) = editor.putBoolean(PrefKeys.isAadhaarVerified, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.isAadhaarVerified, false)

  /**
   *  rc verified
   */
  var isRcVerfied: Boolean
    set(value) = editor.putBoolean(PrefKeys.isRcVerified, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.isRcVerified, false)

  /**
   * rc number
   */
  var rcNumber: String
    set(value) = editor.putString(PrefKeys.rcNumber,value)
            .apply()
    get() = prefs.getString(PrefKeys.rcNumber, "") ?: ""

  var businessDocType: String
    set(value) = editor.putString(PrefKeys.businessDocType,value)
        .apply()
    get() = prefs.getString(PrefKeys.businessDocType, "") ?: ""

  var panName: String
    set(value) = editor.putString(PrefKeys.panName,value)
        .apply()
    get() = prefs.getString(PrefKeys.panName, "") ?: ""

  /**
   * verificationStatus
   */
  var verificationStatus: String
    set(value) = editor.putString(PrefKeys.verificationStatus,value)
            .apply()
    get() = prefs.getString(PrefKeys.verificationStatus, "") ?: ""

  /**
   *  profile url
   */
  var profileImageUrl: String
    set(value) = editor.putString(PrefKeys.profileImageUrl, value)
            .apply()
    get() = prefs.getString(PrefKeys.profileImageUrl, "") ?: ""


  /**
   *  can View third Party Loads
   */
  var canViewThirdPartyLoads: Boolean
    set(value) = editor.putBoolean(PrefKeys.canViewThirdPartyLoads, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.canViewThirdPartyLoads, false)

  /**
   *  own trucks
   */
  var ownTrucks: Boolean
    set(value) = editor.putBoolean(PrefKeys.ownsTrucks, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.ownsTrucks, false)
    /**
     * cin number
     */
    var cinNumber: String
        set(value) = editor.putString(PrefKeys.cinNumber,value)
            .apply()
        get() = prefs.getString(PrefKeys.cinNumber, "") ?: ""
    /**
     * udyog number
     */
    var udyogNumber: String
        set(value) = editor.putString(PrefKeys.udyogNumber,value)
            .apply()
        get() = prefs.getString(PrefKeys.udyogNumber, "") ?: ""
    /**
     * shop number
     */
    var shopNumber: String
        set(value) = editor.putString(PrefKeys.shopNumber,value)
            .apply()
        get() = prefs.getString(PrefKeys.shopNumber, "") ?: ""

  var paymentAccountNumber: String
    set(value) = editor.putString(PrefKeys.paymentAccountNumber,value)
        .apply()
    get() = prefs.getString(PrefKeys.paymentAccountNumber, "") ?: ""
  var paymentAccountName: String
    set(value) = editor.putString(PrefKeys.paymentAccountName,value)
        .apply()
    get() = prefs.getString(PrefKeys.paymentAccountName, "") ?: ""
  var paymentIFSCCode: String
    set(value) = editor.putString(PrefKeys.paymentIFSCCode,value)
        .apply()
    get() = prefs.getString(PrefKeys.paymentIFSCCode, "") ?: ""


  /**
   *  load board supplier
   */
  var isLoadBoardSupplier: Boolean
    set(value) = editor.putBoolean(PrefKeys.isLoadBoardSupplier, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.isLoadBoardSupplier, false)

  /**
   *  load board client
   */
  var isLoadBoardClient: Boolean
    set(value) = editor.putBoolean(PrefKeys.isLoadBoardClient, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.isLoadBoardClient, false)

  /**
   *  identity needed
   */
  var isGstsByPanNotRegistered: Boolean
    set(value) = editor.putBoolean(PrefKeys.isGstsByPanNotRegistered, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.isGstsByPanNotRegistered, false)

    /**
     *  trucking document uploaded
     */
    var isTruckingDocumentUploaded: Boolean
        set(value) = editor.putBoolean(PrefKeys.isTruckingDocumentUploaded, value)
            .apply()
        get() = prefs.getBoolean(PrefKeys.isTruckingDocumentUploaded, false)

    /**
   *  identity verified
   */
  var isIdentityVerified: Boolean
    set(value) = editor.putBoolean(PrefKeys.isIdentityVerified, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.isIdentityVerified, false)

  var isCommunicationAddressVerified: Boolean
  set(value) = editor.putBoolean(PrefKeys.isCommunicationAddressVerified, value)
          .apply()
    get() = prefs.getBoolean(PrefKeys.isCommunicationAddressVerified, false)


  var panRejectReason: String
    set(value) = editor.putString(PrefKeys.panRejectReason,value)
            .apply()
    get() = prefs.getString(PrefKeys.panRejectReason, "") ?: ""

  var identityRejectReason: String
    set(value) = editor.putString(PrefKeys.identityRejectReason,value)
            .apply()
    get() = prefs.getString(PrefKeys.identityRejectReason, "") ?: ""


  var addressRejectReason : String
    set(value) = editor.putString(PrefKeys.addressRejectReason,value)
            .apply()
    get() = prefs.getString(PrefKeys.addressRejectReason, "") ?: ""

  var paymentRejectReason : String
    set(value) = editor.putString(PrefKeys.paymentRejectReason,value)
        .apply()
    get() = prefs.getString(PrefKeys.paymentRejectReason, "") ?: ""


  var rcRejectReason: String
    set(value) = editor.putString(PrefKeys.rcRejectReason,value)
            .apply()
    get() = prefs.getString(PrefKeys.rcRejectReason, "") ?: ""

  var identityType: String
    set(value) = editor.putString(PrefKeys.identityType,value)
            .apply()
    get() = prefs.getString(PrefKeys.identityType, "") ?: ""


  var noOfVerificationIssues: String
    set(value) = editor.putString(PrefKeys.noOfVerificationIssues ,value)
            .apply()
    get() = prefs.getString(PrefKeys.noOfVerificationIssues, "") ?: ""

  var retryVerification: Boolean
    set(value) = editor.putBoolean(PrefKeys.retryVerification, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.retryVerification, false)

    var retryVerificationOnBack: Boolean
        set(value) = editor.putBoolean(PrefKeys.retryVerificationOnBack, value)
            .apply()
        get() = prefs.getBoolean(PrefKeys.retryVerificationOnBack, false)

  var businessType: String
    set(value) = editor.putString(PrefKeys.businessType,value)
            .apply()
    get() = prefs.getString(PrefKeys.businessType, "") ?: ""

  var ownedTruck: String
    set(value) = editor.putString(PrefKeys.ownedTruck,value)
        .apply()
    get() = prefs.getString(PrefKeys.ownedTruck, "") ?: ""

  var attachedTruck: String
    set(value) = editor.putString(PrefKeys.attachedTruck,value)
        .apply()
    get() = prefs.getString(PrefKeys.attachedTruck, "") ?: ""

  var rcManualverificationreq: Boolean
    set(value) = editor.putBoolean(PrefKeys.rcManualVerificationReq,value)
        .apply()
    get() = prefs.getBoolean(PrefKeys.rcManualVerificationReq, false)

  var vendorPolicyAccepted: Boolean
    set(value) = editor.putBoolean(PrefKeys.vendorPolicyAccepted, value)
      .apply()
    get() = prefs.getBoolean(PrefKeys.vendorPolicyAccepted, false)

  var aadhaarPolicyAccepted: Boolean
    set(value) = editor.putBoolean(PrefKeys.aadhaarPolicyAccepted, value)
      .apply()
    get() = prefs.getBoolean(PrefKeys.aadhaarPolicyAccepted, false)

  var receiveWhatsappNotifications: Boolean
    set(value) = editor.putBoolean(PrefKeys.receiveWhatsappNotifications, value)
      .apply()
    get() = prefs.getBoolean(PrefKeys.receiveWhatsappNotifications, false)

  var isBankDetailsRejected: Boolean
    set(value) = editor.putBoolean(PrefKeys.isBankDetailsRejected, value)
        .apply()
    get() = prefs.getBoolean(PrefKeys.isBankDetailsRejected, false)

  var isFirstOpenRate: Boolean
    set(value) = editor.putBoolean(PrefKeys.isFirstOpenrate, value)
            .apply()
    get() = prefs.getBoolean(PrefKeys.isFirstOpenrate, false)


  var status: String
    set(value) = editor.putString(PrefKeys.status,value)
      .apply()
    get() = prefs.getString(PrefKeys.status, "") ?: ""
  var subStatus: String
    set(value) = editor.putString(PrefKeys.subStatus,value)
      .apply()
    get() = prefs.getString(PrefKeys.subStatus, "") ?: ""
  var creationDate: String
    set(value) = editor.putString(PrefKeys.creationDate,value)
      .apply()
    get() = prefs.getString(PrefKeys.creationDate, "") ?: ""
  var isKycVeriifed: Boolean
    set(value) = editor.putBoolean(PrefKeys.isKycVerified, value)
      .apply()
    get() = prefs.getBoolean(PrefKeys.isKycVerified, false)

  var contractDemand: Boolean
    set(value) = editor.putBoolean(PrefKeys.contractDemand, value)
      .apply()
    get() = prefs.getBoolean(PrefKeys.contractDemand, false)

  var recommendedUpdate: Boolean
    set(value) = editor.putBoolean(PrefKeys.RecommendedUpdate, value)
      .apply()
    get() = prefs.getBoolean(PrefKeys.RecommendedUpdate, false)

  var requestedDeletion: Boolean
    set(value) = editor.putBoolean(PrefKeys.RequestedDeletion, value)
      .apply()
    get() = prefs.getBoolean(PrefKeys.RequestedDeletion, false)

  var returningFromDeletion: Boolean
    set(value) = editor.putBoolean(PrefKeys.ReturningFromDeletion, value)
      .apply()
    get() = prefs.getBoolean(PrefKeys.ReturningFromDeletion, false)

  var lastLoggedInUserId: String
      set(value) = editor.putString(PrefKeys.LastLoggedInUserId, value)
            .apply()
      get() = prefs.getString(PrefKeys.LastLoggedInUserId, "") ?: ""


    var userPreviousScreen: String
    set(value) = editor.putString(PrefKeys.userPreviousScreen,value)
      .apply()
    get() = prefs.getString(PrefKeys.userPreviousScreen, "") ?: ""

  var loadCount: String
    set(value) = editor.putString(PrefKeys.totalLoadCount,value)
      .apply()
    get() = prefs.getString(PrefKeys.totalLoadCount, "") ?: "0"

    var fullLoadCount: String
        set(value) = editor.putString(PrefKeys.fullLoadCount,value)
            .apply()
        get() = prefs.getString(PrefKeys.fullLoadCount, "") ?: "0"
    var contractCount: String
        set(value) = editor.putString(PrefKeys.totalContractCount,value)
            .apply()
        get() = prefs.getString(PrefKeys.totalContractCount, "") ?: "0"

  var activeBidCount: String
    set(value) = editor.putString(PrefKeys.activeBidCount,value)
        .apply()
    get() = prefs.getString(PrefKeys.activeBidCount, "") ?: "0"
  var confirmedBidCount: String
    set(value) = editor.putString(PrefKeys.confirmedBidCount,value)
        .apply()
    get() = prefs.getString(PrefKeys.confirmedBidCount, "") ?: "0"
  var lostBidCount: String
    set(value) = editor.putString(PrefKeys.lostBidCount,value)
        .apply()
    get() = prefs.getString(PrefKeys.lostBidCount, "") ?: "0"
  var totalBidCount: String
    set(value) = editor.putString(PrefKeys.totalBidCount,value)
        .apply()
    get() = prefs.getString(PrefKeys.totalBidCount, "") ?: "0"
  var awaitingArrivalCount: String
    set(value) = editor.putString(PrefKeys.expectingArrivalCount,value)
        .apply()
    get() = prefs.getString(PrefKeys.expectingArrivalCount, "") ?: "0"

  var inventoryCount: String
    set(value) = editor.putString(PrefKeys.totalInventoryCount,value)
      .apply()
    get() = prefs.getString(PrefKeys.totalInventoryCount, "") ?: "0"

  var previousNavigationTab: String
    set(value) = editor.putString(PrefKeys.previousNavigationTab,value)
      .apply()
    get() = prefs.getString(PrefKeys.previousNavigationTab, HomeLoadsFragment::class.java.name) ?:""

  var currentNavigationTab: String
    set(value) = editor.putString(PrefKeys.currentNavigationTab,value)
      .apply()
    get() = prefs.getString(PrefKeys.currentNavigationTab, HomeLoadsFragment::class.java.name) ?:""

  fun setPreviousScreen(previousScreen:String){
    userPreviousScreen =previousScreen
  }

  var walletId: String
    set(value) = editor.putString(PrefKeys.WalletId, value).apply()
    get() = prefs.getString(PrefKeys.WalletId, "") ?: ""
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
    editor.remove(PrefKeys.CityName)
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
    editor.remove(PrefKeys.VendorType)
            .apply()
    editor.remove(PrefKeys.RouteType)
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
    editor.remove(PrefKeys.UserMode)
            .apply()
    editor.remove(PrefKeys.UserRole)
            .apply()
    editor.remove(PrefKeys.IsUserVerfied)
            .apply()
    editor.remove(PrefKeys.businessAddress)
            .apply()
    editor.remove(PrefKeys.gstNumber)
            .apply()
    editor.remove(PrefKeys.aadhaarNumber)
            .apply()
    editor.remove(PrefKeys.isAadhaarVerified)
            .apply()
    editor.remove(PrefKeys.isPanVerified)
            .apply()
    editor.remove(PrefKeys.isGstVerified)
            .apply()
    editor.remove(PrefKeys.isRcVerified)
            .apply()
    editor.remove(PrefKeys.alternateAddress)
            .apply()
    editor.remove(PrefKeys.gstAddress)
            .apply()
    editor.remove(PrefKeys.rcNumber)
            .apply()
      editor.remove(PrefKeys.cinNumber)
          .apply()
      editor.remove(PrefKeys.udyogNumber)
          .apply()
      editor.remove(PrefKeys.shopNumber)
          .apply()
    editor.remove(PrefKeys.verificationStatus)
            .apply()
    editor.remove(PrefKeys.profileImageUrl)
            .apply()
    editor.remove(PrefKeys.canViewThirdPartyLoads)
            .apply()
    editor.remove(PrefKeys.ownsTrucks)
            .apply()
    editor.remove(PrefKeys.isLoadBoardClient)
            .apply()
    editor.remove(PrefKeys.isLoadBoardSupplier)
            .apply()
      editor.remove(PrefKeys.isGstsByPanNotRegistered)
              .apply()
    editor.remove(PrefKeys.isIdentityVerified)
            .apply()
      editor.remove(PrefKeys.isCommunicationAddressVerified)
          .apply()
      editor.remove(PrefKeys.isTruckingDocumentUploaded)
          .apply()
      editor.remove(PrefKeys.isSameAsGst)
          .apply()
      editor.remove(PrefKeys.isGstNotBypassed)
          .apply()

    editor.remove(PrefKeys.panRejectReason)
            .apply()
    editor.remove(PrefKeys.paymentRejectReason)
        .apply()
    editor.remove(PrefKeys.addressRejectReason)
            .apply()
    editor.remove(PrefKeys.rcRejectReason)
            .apply()
    editor.remove(PrefKeys.identityType)
            .apply()
    editor.remove(PrefKeys.identityRejectReason)
            .apply()
    editor.remove(PrefKeys.noOfVerificationIssues)
            .apply()
    editor.remove(PrefKeys.retryVerification)
            .apply()
        editor.remove(PrefKeys.retryVerificationOnBack)
            .apply()
    editor.remove(PrefKeys.identityDocUrl)
          .apply()
    editor.remove(PrefKeys.businessType)
            .apply()
    editor.remove(PrefKeys.lanesPreference)
      .apply()
    editor.remove(PrefKeys.ParentId)
      .apply()
    editor.remove(PrefKeys.ParentName)
      .apply()
    editor.remove(PrefKeys.ParentDemandType)
      .apply()
    editor.remove(PrefKeys.ownedTruck)
        .apply()
    editor.remove(PrefKeys.attachedTruck)
        .apply()
    editor.remove(PrefKeys.vendorPolicyAccepted)
      .apply()
    editor.remove(PrefKeys.paymentAccountName)
        .apply()
    editor.remove(PrefKeys.paymentAccountNumber)
        .apply()
    editor.remove(PrefKeys.paymentIFSCCode)
        .apply()
    editor.remove(PrefKeys.rcManualVerificationReq)
        .apply()
    editor.remove(PrefKeys.aadhaarPolicyAccepted)
      .apply()
    editor.remove(PrefKeys.receiveWhatsappNotifications)
      .apply()
    editor.remove(PrefKeys.panName)
        .apply()
    editor.remove(PrefKeys.isBankDetailsRejected)
        .apply()
    editor.remove(PrefKeys.isKycVerified)
      .apply()
    editor.remove(PrefKeys.creationDate)
      .apply()
    editor.remove(PrefKeys.status)
      .apply()
    editor.remove(PrefKeys.subStatus)
      .apply()
    editor.remove(PrefKeys.orderRank)
        .apply()
    editor.remove(PrefKeys.isFirstOpenrate)
            .apply()
    editor.remove(PrefKeys.businessDocUrl)
      .apply()
    editor.remove(PrefKeys.identityDocUrl)
      .apply()
    editor.remove(PrefKeys.ninteen4CDocUrl)
      .apply()
    editor.remove(PrefKeys.paymentDocUrl)
      .apply()
    editor.remove(PrefKeys.MoengageFCMTokenGenerated)
      .apply()
    editor.remove(PrefKeys.contractDemand)
      .apply()
    editor.remove(PrefKeys.RecommendedUpdate)
      .apply()
    editor.remove(PrefKeys.RequestedDeletion)
      .apply()
    editor.remove(PrefKeys.ReturningFromDeletion)
      .apply()
    editor.commit()
  }

  fun clearKycPreferenceDataBasedOnSteps(step:String){
    if(step=="pan"){
      editor.remove(PrefKeys.panName)
        .apply()
      editor.remove(PrefKeys.Pancard)
        .apply()
      editor.remove(PrefKeys.isGstsByPanNotRegistered)
        .apply()
      editor.remove(PrefKeys.gstNumber)
        .apply()
      editor.remove(PrefKeys.gstAddress)
        .apply()
      editor.remove(PrefKeys.aadhaarNumber)
        .apply()
      editor.remove(PrefKeys.aadhaarPolicyAccepted)
        .apply()
      editor.remove(PrefKeys.businessAddress)
        .apply()
      editor.remove(PrefKeys.cinNumber)
        .apply()
      editor.remove(PrefKeys.shopNumber)
        .apply()
      editor.remove(PrefKeys.udyogNumber)
        .apply()
      editor.remove(PrefKeys.identityDocUrl)
        .apply()
      editor.remove(PrefKeys.rcNumber)
        .apply()
      editor.remove(PrefKeys.attachedTruck)
        .apply()
      editor.remove(PrefKeys.ownedTruck)
        .apply()
      editor.remove(PrefKeys.rcManualVerificationReq)
        .apply()
      editor.remove(PrefKeys.isTruckingDocumentUploaded)
        .apply()
      editor.remove(PrefKeys.isSameAsGst)
        .apply()
      editor.remove(PrefKeys.isGstNotBypassed)
        .apply()
      editor.remove(PrefKeys.paymentAccountName)
        .apply()
      editor.remove(PrefKeys.paymentAccountNumber)
        .apply()
      editor.remove(PrefKeys.paymentIFSCCode)
        .apply()
      editor.remove(PrefKeys.IfscCode)
        .apply()
      editor.remove(PrefKeys.businessDocUrl)
        .apply()
      editor.remove(PrefKeys.identityDocUrl)
        .apply()
      editor.remove(PrefKeys.ninteen4CDocUrl)
        .apply()
      editor.remove(PrefKeys.paymentDocUrl)
        .apply()
    }else if(step=="gst"){
      if(isSameAsGst){
      editor.remove(PrefKeys.gstAddress)
        .apply()
      editor.remove(PrefKeys.businessAddress)
        .apply()
      editor.remove(PrefKeys.isSameAsGst)
        .apply()
      }
    }
  }

  fun saveUser(user: UserModel) {
    userName = user.userName?:""
    onboardingStatus = user.supplierDetails?.onboardingStatus ?: "na"
    supplierEnabled = user.supplierDetails?.supplierEnabled?:false
    isTestUser = user.supplierDetails?.testUser == true
    tdsRate = user.getTDSSubtractor()
    updatedTdsRate =
            if (user.getTDSSubtractor() == 99) user.getTDSSubtractor() + 0.25 else user.getTDSSubtractor() + 0.5
    bankName = user.supplierDetails?.bank ?: ""
    companyName = user.businessName ?: ""
    phoneNumber = if(user.phoneNumber.isNotNullOrEmpty())user.phoneNumber else phoneNumber
    ifscCode = user.supplierDetails?.ifscCode ?: ""
    pancard = user.supplierDetails?.panNumber ?: ""
    accNumber = user.accNumber()
    paymentAccountNumber= user.supplierDetails?.accountNo?:""
    paymentAccountName=user.supplierDetails?.accountHolderName?:""
    cinNumber=user.supplierDetails?.cInNumber?:""
    shopNumber=user.supplierDetails?.shopEstablishment?:""
    udyogNumber=user.supplierDetails?.udyogAadhar?:""
    cityName = user.supplierDetails?.baseCity
    cityCode = user.supplierDetails?.baseCityCode
    isBankDetailsRejected=user.isBankDetailsRejected?:false
    isParent = user.isParent()
    userType = user.userType ?: ""
    truckTypes = if (user.isParent()) {
      user.supplierDetails?.truckTypes?.joinToString(separator = ",") {it}
    } else {
      user.supplierDetails?.parentDetails?.truckTypes?.joinToString(separator = ",") {it}
    }
    demandType = user.supplierDetails?.demandType?.joinToString(separator = ",") {it}.toString()
    userPerformance = user.supplierDetails?.overallPerformance ?: ""
    vendorEntity = user.supplierDetails?.vendorEntity ?: ""
    parentId = if (user.isParent()) {
      user.userId
    } else {
      user.supplierDetails?.parentDetails?.userId ?: ""
    }
    parentName = if (user.isParent()) {
      user.userName?:""
    } else {
      user.supplierDetails?.parentDetails?.name ?: ""
    }
    parentDemandType = if (user.isParent()) {
      user.supplierDetails?.demandType?.joinToString(separator = ",") { it }
    } else {
      user.supplierDetails?.parentDetails?.demandType?.joinToString(separator = ",") { it }
    }
    userMode = user.userMode?: ""
    userRole = user.userRole?: ""
    isUserVerfied = user.isUserVerified
    aadhaarNumber = user.supplierDetails?.aadhaarNumber?: ""
    gstNumber = user.supplierDetails?.gstNumber?: ""
    rcNumber = user.supplierDetails?.rcNumber?: ""
    panName = user.supplierDetails?.panHolderName?: ""
    businessAddress = user.businessAddress?: ""
    setAddressList(user.otherAddress)
    isPanVerfied = user.isPanVerified?: false
    isGstVerfied= user.isGstVerified?: false
    isRcVerfied = user.isRcVerified?: false
    isIdentityVerified = user.isIdentityVerified?: false
    isGstsByPanNotRegistered = user.isGstsByPanNotRegistered?: true
    isTruckingDocumentUploaded = user.isTruckingDocumentUploaded?: false
    isAadhaartVerfied = user.isAadhaarVerified?: false
    verificationStatus = user.verificationStatus?: ""
    profileImageUrl = user.profileImageUrl?:""
    canViewThirdPartyLoads = user.canViewThirdPartyLoads?: false
    ownTrucks = user.supplierDetails?.ownsTrucks?: false
    isLoadBoardSupplier = user.supplierDetails?.isLoadBoardSupplier?: false
    isLoadBoardClient = user.clientDetails?.isLoadBoardClient?: false
    noOfVerificationIssues =if(user.noOfVerificationIssues.isNotNullOrEmpty() || user.noOfVerificationIssues?.equals("0.0")==false) {user.noOfVerificationIssues?.split(".")?.get(0) ?:""}else {""}
    identityDocUrl = user.supplierDetails?.identity_doc_url?:""
    setLanesPreferences(user.supplierDetails?.routes)
    vendorPolicyAccepted = user.supplierDetails?.vendorPolicyAccepted?:false
    aadhaarPolicyAccepted= user.supplierDetails?.aadhaarPolicyAccepted?:false
    businessDocUrl= user.supplierDetails?.businessDocUrl?:""
    paymentDocUrl=user.supplierDetails?.accountProofUrl?:""
    ninteen4CDocUrl=user.supplierDetails?.sec194DeclarationUrl?:""
    ownedTruck = user.supplierDetails?.numberOfOwnedTrucks?:""
    attachedTruck = user.supplierDetails?.numberOfAttachedTrucks?:""
    isSameAsGst= user.isAddressSameAsGST?:false

    receiveWhatsappNotifications = user.supplierDetails?.receiveWhatsappNotifications?:false
    status = user.supplierDetails?.status?:""
    subStatus = user.supplierDetails?.subStatus?:""
    creationDate = user.supplierDetails?.creationDate?:""
    isKycVeriifed = user.supplierDetails?.isKycVerified?:false
    contractDemand = user.supplierDetails?.contractDemand?:false
    requestedDeletion = user.requestedDeletion?:false
    returningFromDeletion = if(requestedDeletion) true else returningFromDeletion
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
  fun userId(): String {
    return try {
      jwtToken?.let { token ->
        if (token.isBlank()) {
          // Clear invalid empty token
          this.jwtToken = null
          ""
        } else {
          JWT(token).let { jwt ->
            jwt.claims["sub"]?.asString() ?: ""
          }
        }
      } ?: ""
    } catch (e: Exception) {
      // Handle any JWT parsing errors (corrupted token, invalid format, etc.)
      // Clear the invalid token to prevent future crashes
      this.jwtToken = null
      ""
    }
  }

  /**
   * Pref keys
   */
  internal object PrefKeys {
    const val JWTToken = "jwt_token"
    const val CityCode = "city_code"
    const val CityName = "city_name"
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
    const val MoengageFCMTokenGenerated = "fcm_token_shared_to_moengage"
    const val WalletActive = "wallet_active"
    const val FromNotification = "from_notification"
    const val MaxPMTRate = "max_pmt_rate"
    const val MaxCostPerKM = "max_cost_per_km"
    const val IsParent = "is_parent"
    const val UserType = "user_type"
    const val TruckTypes = "truck_types"
    const val VendorType = "vendor_type"
    const val RouteType = "route_type"
    const val DemandType = "demand_type"
    const val LogoutStatus = "logout_status"
    const val StartTime = "start_time"
    const val LastLoginTime = "last_login_time"
    const val IsFirstRoute = "first_route"
    const val UserOverallPerformance = "overall_performance"
    const val VendorEntity = "vendor_entity"
    const val DeepLinkArg = "deep_link_argument"
    const val IsFirstLoginRPUser = "first_login_RP"
    const val ParentId = "parent_id"
    const val ParentName = "parent_name"
    const val ParentDemandType = "parent_demand_type"
    const val LoadPostKyc = "post_load"
    const val TruckPostKyc = "post_truck"
    const val ShareRateBannerH1 = "share_rate_banner_h1"
    const val ShareRateBannerH2 = "share_rate_banner_h2"
    const val ShareRateBannerH3 = "share_rate_banner_h3"
    const val PodAddress = "pod_address"
    const val UserRole = "user_role"
    const val UserMode = "user_mode"
    const val IsUserVerfied = "is_user_verified"
    const val isSameAsGst = "is_same_as_gst"
    const val isGstNotBypassed = "is_gst_not_bypassed"
    const val gstNumber = "gst_number"
    const val aadhaarNumber = "aadhaar_number"
    const val businessAddress = "business_address"
    const val gstAddress = "gst_address"
    const val alternateAddress = "alternate_address"
    const val isPanVerified = "is_pan_verified"
    const val isAadhaarVerified = "is_aadhaar_verified"
    const val isRcVerified = "is_rc_verified"
    const val isGstVerified = "is_gst_verified"
    const val rcNumber = "rc_number"
    const val businessDocType = "businessDocType"
    const val panName = "pan_name"
    const val verificationStatus = "verification_status"
    const val profileImageUrl = "profile_image_url"
    const val  canViewThirdPartyLoads = "can_view_third_party_loads"
    const val  ownsTrucks = "owns_trucks"
    const val  isLoadBoardSupplier = "is_load_board_supplier"
    const val  isLoadBoardClient = "is_load_board_client"
    const val cinNumber = "cin_number"
    const val udyogNumber = "udyog_number"
    const val shopNumber = "shop_number"
    const val isGstsByPanNotRegistered = "is_gsts_by_pan_not_registered"
    const val isIdentityVerified = "is_identity_verified"
    const val isCommunicationAddressVerified = "is_communication_address_verified"
    const val isTruckingDocumentUploaded = "is_trucking_document_uploaded"

    const val panRejectReason = "pan_reject_reason"
    const val identityRejectReason = "identity_reject_reason"
    const val rcRejectReason = "rc_reject_reason"
    const val addressRejectReason = "address_reject_reason"
    const val paymentRejectReason = "payment_reject_reason"
    const val identityType = "identity_type"
    const val businessType = "business_type"
    const val ownedTruck = "owned_truck"
    const val attachedTruck = "attached_truck"
    const val rcManualVerificationReq = "rc_manual_verification_req"
    const val paymentAccountNumber = "payment_account_number"
    const val paymentAccountName = "payment_account_name"
    const val paymentIFSCCode = "payment_ifsc_code"

    const val noOfVerificationIssues = "no_of_verification_issues"
    const val retryVerification = "retry_verification"
    const val retryVerificationOnBack = "retry_verification_on_back"
    const val identityDocUrl = "identity_doc_url"
    const val businessDocUrl = "business_doc_url"
    const val paymentDocUrl = "payment_doc_url"
    const val ninteen4CDocUrl = "ninteen4c_doc_url"
    const val lanesPreference= "lanes_preference"
    const val vendorPolicyAccepted= "agreedTermCondition"
    const val aadhaarPolicyAccepted= "aadhaarPolicyAccepted"
    const val isBankDetailsRejected= "is_bank_details_rejected"
    const val isFirstOpenrate= "is_first_open_rate"
    const val BidOfferCount= "bid_offer_count"
    const val TrucksOfferCount= "truck_offer_count"
    const val RateOfferCount= "rate_offer_count"


    const val receiveWhatsappNotifications= "receive_whatsapp_notifications"
    const val creationDate= "creation_date"
    const val status= "status"
    const val subStatus= "sub_status"
    const val isKycVerified= "is_kyc_verified"
    const val userPreviousScreen= "previous_screen"
    const val totalLoadCount= "load_count"
      const val fullLoadCount= "tota_load_count"
      const val totalContractCount= "load_count"
      const val activeBidCount= "active_bid_count"
    const val confirmedBidCount= "confirmed_bid_count"
    const val lostBidCount= "lost_bid_count"
    const val totalBidCount= "total_bid_count"
    const val expectingArrivalCount= "expecting_arrival_count"
    const val totalInventoryCount= "invnetory_count"
    const val orderRank = "order_rank"
    const val previousNavigationTab = "previous_navigation_tab"
    const val currentNavigationTab = "current_navigation_tab"
    const val contractDemand= "contract_demand"
    const val RecommendedUpdate= "recommended_update"
    const val RequestedDeletion= "requested_deletion"
    const val ReturningFromDeletion= "returningFromDeletion"
    const val LastLoggedInUserId= "last_logged_in_userID"
    const val WalletId = "wallet_id"
  }
}

const val DISABLED = 1
const val UNAPPROVED = 2
const val APPROVED = 3
