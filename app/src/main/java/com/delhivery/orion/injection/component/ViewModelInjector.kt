package com.delhivery.orion.injection.component

import com.delhivery.orion.injection.module.NetworkModule
import com.delhivery.orion.ui.auth.AuthenticationViewModel
import com.delhivery.orion.ui.onboarding.OnboardingViewModel
import com.delhivery.orion.ui.selectroute.SelectRouteViewModel
import com.delhivery.orion.ui.selectroutewelcome.SelectRouteWelcomeViewModel
import com.delhivery.orion.ui.splash.SplashViewModel
import dagger.Component
import javax.inject.Singleton

/**
 * Component providing inject() methods for presenters
 */
@Singleton
@Component(modules = [NetworkModule::class])
interface ViewModelInjector {

  /**
   * Injects required dependencies into the specified
   */
  fun inject(splashViewModel: SplashViewModel)

  fun inject(authenticationViewModel: AuthenticationViewModel)

  fun inject(selectRouteWelcomeViewModel: SelectRouteWelcomeViewModel)

  fun inject(selectRouteViewModel: SelectRouteViewModel)

  fun inject(onboardingViewModel: OnboardingViewModel)

  @Component.Builder
  interface Builder {
    fun build(): ViewModelInjector

    fun networkModule(networkModule: NetworkModule): Builder
  }
}