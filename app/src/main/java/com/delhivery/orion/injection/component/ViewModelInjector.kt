package com.delhivery.orion.injection.component

import com.delhivery.orion.injection.module.NetworkModule
import com.delhivery.orion.ui.auth.AuthenticationViewModel
import com.delhivery.orion.ui.biddetails.BidDetailsViewModel
import com.delhivery.orion.ui.bids.BidsViewModel
import com.delhivery.orion.ui.home.HomeViewModel
import com.delhivery.orion.ui.home.fragments.alerts.HomeAlertsViewModel
import com.delhivery.orion.ui.home.fragments.bids.HomeBidsViewModel
import com.delhivery.orion.ui.home.fragments.loads.HomeLoadsViewModel
import com.delhivery.orion.ui.home.fragments.payment.HomePaymentViewModel
import com.delhivery.orion.ui.home.fragments.profile.HomeProfileViewModel
import com.delhivery.orion.ui.home.fragments.trips.HomeTripsViewModel
import com.delhivery.orion.ui.onboarding.OnboardingViewModel
import com.delhivery.orion.ui.searchload.SearchLoadViewModel
import com.delhivery.orion.ui.searchload.fragments.searchload.SearchLoadFragmentViewModel
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsViewModel
import com.delhivery.orion.ui.selectroute.activity.SelectRouteViewModel
import com.delhivery.orion.ui.selectroute.fragments.destination.SelectRouteDestinationViewModel
import com.delhivery.orion.ui.selectroute.fragments.origincity.SelectRouteOriginCityViewModel
import com.delhivery.orion.ui.selectroute.fragments.routeslist.SelectRouteListViewModel
import com.delhivery.orion.ui.selectroutewelcome.SelectRouteWelcomeViewModel
import com.delhivery.orion.ui.splash.SplashViewModel
import com.delhivery.orion.ui.tripdetails.TripDetailsViewModel
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

  fun inject(onboardingViewModel: OnboardingViewModel)

  /* select route */
  fun inject(selectRouteWelcomeViewModel: SelectRouteWelcomeViewModel)

  fun inject(selectRouteViewModel: SelectRouteViewModel)

  fun inject(selectRouteOriginCityViewModel: SelectRouteOriginCityViewModel)

  fun inject(selectRouteDestinationViewModel: SelectRouteDestinationViewModel)

  fun inject(selectRouteListViewModel: SelectRouteListViewModel)

  /* Home */
  fun inject(homeViewModel: HomeViewModel)

  fun inject(homeBidsViewModel: HomeBidsViewModel)

  fun inject(homeLoadsViewModel: HomeLoadsViewModel)

  fun inject(homeTripsViewModel: HomeTripsViewModel)

  fun inject(homePaymentViewModel: HomePaymentViewModel)

  fun inject(homeAlertsViewModel: HomeAlertsViewModel)

  fun inject(homeProfileViewModel: HomeProfileViewModel)

  /* Bids */
  fun inject(bidsViewModel: BidsViewModel)

  fun inject(bidDetailsViewModel: BidDetailsViewModel)

  /* Search Load */
  fun inject(searchLoadViewModel: SearchLoadViewModel)

  fun inject(searchLoadFragmentViewModel: SearchLoadFragmentViewModel)

  fun inject(searchResultsViewModel: SearchResultsViewModel)

  /* Trip details */
  fun inject(tripDetailsViewModel: TripDetailsViewModel)

  @Component.Builder
  interface Builder {
    fun build(): ViewModelInjector

    fun networkModule(networkModule: NetworkModule): Builder
  }
}