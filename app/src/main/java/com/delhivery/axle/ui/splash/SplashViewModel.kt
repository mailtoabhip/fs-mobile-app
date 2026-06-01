package com.delhivery.axle.ui.splash

import com.delhivery.axle.api.repository.FsAuthRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.splash.SplashPostState.AccountDetails
import com.delhivery.axle.ui.splash.SplashPostState.Auth
import com.delhivery.axle.ui.splash.SplashPostState.Home
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * View model for [StartRoutingActivity]]
 */
class SplashViewModel @Inject constructor(
  private val authenticationRepository: FsAuthRepository,
  private val userPrefs: UserPrefs
) :
    BaseViewModel() {

  /**
   * Post splash state
   */
  fun postState() = when {
    authenticationRepository.authStatus() && userPrefs.hasLoggedIn && userPrefs.isNewUser -> AccountDetails
    authenticationRepository.authStatus() && userPrefs.hasLoggedIn -> Home
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

}