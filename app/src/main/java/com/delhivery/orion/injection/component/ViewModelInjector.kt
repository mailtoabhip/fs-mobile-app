package com.delhivery.orion.injection.component

import com.delhivery.orion.injection.module.NetworkModule
import com.delhivery.orion.ui.auth.AuthenticationViewModel
import com.delhivery.orion.ui.biddetails.BidDetailsViewModel
import com.delhivery.orion.ui.bids.BidsViewModel
import com.delhivery.orion.ui.home.HomeViewModel
import com.delhivery.orion.ui.home.fragments.alerts.HomeAlertsViewModel
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsViewModel
import com.delhivery.orion.ui.home.fragments.payment.HomePaymentViewModel
import com.delhivery.orion.ui.home.fragments.profile.HomeProfileViewModel
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsViewModel
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
  /* onboarding */
  fun inject(splashViewModel: SplashViewModel)

  fun inject(authenticationViewModel: AuthenticationViewModel)

  fun inject(selectRouteWelcomeViewModel: SelectRouteWelcomeViewModel)

  fun inject(selectRouteViewModel: SelectRouteViewModel)

  fun inject(onboardingViewModel: OnboardingViewModel)

  /* Home */
  fun inject(homeViewModel: HomeViewModel)

  fun inject(homeBidsViewModel: HomeBidsViewModel)

  fun inject(homeTripsViewModel: HomeTripsViewModel)

  fun inject(homePaymentViewModel: HomePaymentViewModel)

  fun inject(homeAlertsViewModel: HomeAlertsViewModel)

  fun inject(homeProfileViewModel: HomeProfileViewModel)

  /* Bids */
  fun inject(bidsViewModel: BidsViewModel)

  fun inject(bidDetailsViewModel: BidDetailsViewModel)

  @Component.Builder
  interface Builder {
    fun build(): ViewModelInjector

    fun networkModule(networkModule: NetworkModule): Builder
  }
}