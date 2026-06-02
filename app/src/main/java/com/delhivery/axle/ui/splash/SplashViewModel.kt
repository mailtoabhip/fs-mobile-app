package com.delhivery.axle.ui.splash

import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.FsAuthRepository
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.splash.SplashPostState.AccountDetails
import com.delhivery.axle.ui.splash.SplashPostState.Auth
import com.delhivery.axle.ui.splash.SplashPostState.Home
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View model for [StartRoutingActivity]]
 */
class SplashViewModel @Inject constructor(
  private val authenticationRepository: FsAuthRepository,
  private val userPrefs: UserPrefs
) :
    BaseViewModel() {

  private val _splashState = MutableStateFlow<SplashPostState?>(null)
  val splashState: StateFlow<SplashPostState?> = _splashState.asStateFlow()

  /**
   * Post splash state
   */
  fun determineState() {
    when {
      authenticationRepository.isTokenPresent() && userPrefs.isProfilePending() -> getProfileDetails()
      authenticationRepository.isTokenPresent() -> _splashState.value = Home
      else -> _splashState.value = Auth
    }
  }

  fun recommendedUpdate(update: Boolean) {
    userPrefs.recommendedUpdate = update
  }

  /**
   * Whether the user has valid tokens (logged in).
   */
  fun isUserLoggedIn(): Boolean = authenticationRepository.isTokenPresent()

  private fun getProfileDetails() {
    viewModelScope.launch {
      when (val result = authenticationRepository.getProfile()) {
        is Resource.Success -> {
          if (result.data?.firstName.isNotNullOrEmpty() && result.data?.lastName.isNotNullOrEmpty()) {
            _splashState.value = Home
          } else {
            _splashState.value = AccountDetails
          }
        }
        is Resource.Failure -> {
          _splashState.value = Auth
        }
        Resource.Loading -> { /* no-op */ }
      }
    }
  }

}