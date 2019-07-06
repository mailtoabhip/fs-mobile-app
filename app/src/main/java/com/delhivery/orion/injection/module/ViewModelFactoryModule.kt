package com.delhivery.orion.injection.module

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.delhivery.orion.injection.scope.ViewModelScope
import com.delhivery.orion.ui.auth.AuthenticationViewModel
import com.delhivery.orion.ui.biddetails.BidDetailsViewModel
import com.delhivery.orion.ui.bids.BidsViewModel
import com.delhivery.orion.ui.bids.TripsViewModel
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
import com.delhivery.orion.ui.selectroute.fragments.detail.SelectRouteDetailViewModel
import com.delhivery.orion.ui.selectroute.fragments.origincity.SelectRouteOriginCityViewModel
import com.delhivery.orion.ui.selectroute.fragments.routeslist.SelectRouteListViewModel
import com.delhivery.orion.ui.selectroutewelcome.SelectRouteWelcomeViewModel
import com.delhivery.orion.ui.splash.SplashViewModel
import com.delhivery.orion.ui.tripdetails.ImageViewModel
import com.delhivery.orion.ui.tripdetails.TripDetailsViewModel
import com.delhivery.orion.utils.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * View Model Factory Module
 *
 * Each ViewModel should be declared here as bind<#view_model>
 * else [IllegalArgumentException] will be thrown with "Unknown model class $modelClass"
 */
@Module
abstract class ViewModelFactoryModule {

  /**
   * Onboarding
   */
  @Binds
  @IntoMap
  @ViewModelScope(SplashViewModel::class)
  abstract fun bindSplashViewModel(splashViewModel: SplashViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(AuthenticationViewModel::class)
  abstract fun bindAuthenticationViewModel(authenticationViewModel: AuthenticationViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(OnboardingViewModel::class)
  abstract fun bindOnboardingViewMdel(onboardingViewModel: OnboardingViewModel): ViewModel

  /* Select route */
  @Binds
  @IntoMap
  @ViewModelScope(SelectRouteWelcomeViewModel::class)
  abstract fun bindSelectRouteWelcomeViewModel(selectRouteWelcomeViewModel: SelectRouteWelcomeViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SelectRouteViewModel::class)
  abstract fun bindSelectRouteViewModel(selectRouteViewModel: SelectRouteViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SelectRouteOriginCityViewModel::class)
  abstract fun bindSelectRouteOriginCityViewModel(selectRouteOriginCityViewModel: SelectRouteOriginCityViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SelectRouteDestinationViewModel::class)
  abstract fun bindSelectRouteDestinationViewModel(selectRouteDestinationViewModel: SelectRouteDestinationViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SelectRouteListViewModel::class)
  abstract fun bindSelectRouteListViewModel(selectRouteListViewModel: SelectRouteListViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SelectRouteDetailViewModel::class)
  abstract fun bindRouteDetailViewModel(selectRouteDetailViewModel: SelectRouteDetailViewModel): ViewModel

  /* Home */
  @Binds
  @IntoMap
  @ViewModelScope(HomeViewModel::class)
  abstract fun bindHomeViewModel(homeViewModel: HomeViewModel): ViewModel

  /* Home fragments */
  @Binds
  @IntoMap
  @ViewModelScope(HomeBidsViewModel::class)
  abstract fun bindHomeBidsViewModel(homeBidsViewModel: HomeBidsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(HomeLoadsViewModel::class)
  abstract fun bindHomeLoadsViewModel(homeLoadsViewModel: HomeLoadsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(HomeTripsViewModel::class)
  abstract fun bindHomeTripsViewModel(homeTripsViewModel: HomeTripsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(HomePaymentViewModel::class)
  abstract fun bindHomePaymentViewModel(homePaymentViewModel: HomePaymentViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(HomeAlertsViewModel::class)
  abstract fun bindHomeAlertsViewModel(homeAlertsViewModel: HomeAlertsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(HomeProfileViewModel::class)
  abstract fun bindHomeProfileViewModel(homeProfileViewModel: HomeProfileViewModel): ViewModel

  /* Bids */
  @Binds
  @IntoMap
  @ViewModelScope(BidsViewModel::class)
  abstract fun bindBidsViewModel(bidsViewModel: BidsViewModel): ViewModel

  /* Bids */
  @Binds
  @IntoMap
  @ViewModelScope(TripsViewModel::class)
  abstract fun bindTripsViewModel(tripsViewModel: TripsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(BidDetailsViewModel::class)
  abstract fun bindBidDetailsViewModel(bidDetailsViewModel: BidDetailsViewModel): ViewModel

  /* Search load */
  @Binds
  @IntoMap
  @ViewModelScope(SearchLoadViewModel::class)
  abstract fun bindSearchLoadViewModel(searchLoadViewModel: SearchLoadViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SearchLoadFragmentViewModel::class)
  abstract fun bindSearchLoadFragmentViewModel(searchLoadFragmentViewModel: SearchLoadFragmentViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SearchResultsViewModel::class)
  abstract fun bindSearchResultsViewModel(searchResultsViewModel: SearchResultsViewModel): ViewModel

  /* Trip details */
  @Binds
  @IntoMap
  @ViewModelScope(TripDetailsViewModel::class)
  abstract fun bindTripDetailsViewModel(tripDetailsViewModel: TripDetailsViewModel): ViewModel

  /* Trip details */
  @Binds
  @IntoMap
  @ViewModelScope(ImageViewModel::class)
  abstract fun bindImageViewModel(imageViewModel: ImageViewModel): ViewModel

  @Binds
  internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}