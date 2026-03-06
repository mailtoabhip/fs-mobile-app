package com.delhivery.axle.ui.splash

import com.delhivery.axle.api.repository.AuthenticationRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.splash.SplashPostState.*
import com.delhivery.axle.utils.prefs.GlobalPrefs
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * View model for [StartRoutingActivity]]
 */
class SplashViewModel @Inject constructor(
  private val authenticationRepository: AuthenticationRepository,
  private val globalPrefs: GlobalPrefs,
  private val userPrefs: UserPrefs
) :
    BaseViewModel() {

  /**
   * Post splash state
   */
  fun postState() = when {
    authenticationRepository.authStatus() && userPrefs.hasLoggedIn && getOldUser() && userPrefs.getLanesPreference().isNullOrEmpty() && userPrefs.truckTypes.isNullOrEmpty() && userPrefs.onboardingStatus == "details_pending" && (userPrefs.vendorType.isNullOrEmpty() || userPrefs.routeType.isNullOrEmpty()) -> BasicDetails
    authenticationRepository.authStatus() && userPrefs.hasLoggedIn -> Home
    authenticationRepository.authStatus() && userPrefs.hasLoggedIn && getOldUser() && (userPrefs.userName.isEmpty() ||userPrefs.companyName.isEmpty())-> AccountDetails
    else -> Auth
  }

  /**
   * Save PMT values from Firebase Config
   */
  fun savePMTValidation(
    maxRate: Int,
    maxCostPerKM: Int
  ) {
    userPrefs.maxPMTRate = maxRate
    userPrefs.maxCostPerKM = maxCostPerKM
  }

  fun recommendedUpdate(update:Boolean){
    userPrefs.recommendedUpdate = update
  }

  fun saveLoadPostKycConfig(loadPostKyc:String){
    userPrefs.loadPostKyc = loadPostKyc
  }
  fun saveTruckPostKycConfig(truckPostKyc:String){
    userPrefs.truckPostKyc = truckPostKyc
  }
  fun saveShareBannerH1Config(bannerText:String){
    userPrefs.shareRateBannerH1 = bannerText
  }
  fun saveShareBannerH2Config(bannerText:String){
    userPrefs.shareRateBannerH2 = bannerText
  }
  fun saveShareBannerH3Config(bannerText:String){
    userPrefs.shareRateBannerH3 = bannerText
  }
  fun savePodAddress(podAddress:String){
    userPrefs.podAddress=podAddress
  }

  fun getOldUser():Boolean{
    return !(userPrefs.isLoadBoardClient== false || userPrefs.isLoadBoardSupplier == false)
  }
}