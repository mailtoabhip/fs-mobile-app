package com.delhivery.axle.ui.splash

import com.delhivery.axle.api.repository.AuthenticationRepository
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.splash.SplashPostState.*
import com.delhivery.axle.utils.prefs.GlobalPrefs
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * View model for [SplashActivity]]
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
    !globalPrefs.isOnboardingCompleted -> Onboarding
    authenticationRepository.authStatus() && userPrefs.hasLoggedIn -> Home
    authenticationRepository.authStatus() && userPrefs.userMode.isEmpty() && userPrefs.accountSetup-> AccountAction
    authenticationRepository.authStatus() && userPrefs.userRole.isEmpty()&& userPrefs.accountSetup-> AccountRole
    authenticationRepository.authStatus() && userPrefs.accountSetup && (userPrefs.userName.isEmpty() ||userPrefs.companyName.isEmpty())-> AccountDetails
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

  fun saveLoadPostKycConfig(loadPostKyc:String){
    userPrefs.loadPostKyc = loadPostKyc
  }
  fun saveTruckPostKycConfig(truckPostKyc:String){
    userPrefs.truckPostKyc = truckPostKyc
  }
}