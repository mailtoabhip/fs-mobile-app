package com.delhivery.orion.injection.module

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.delhivery.orion.injection.scope.ViewModelScope
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
import com.delhivery.orion.ui.searchload.SearchLoadViewModel
import com.delhivery.orion.ui.searchload.fragments.searchload.SearchLoadFragmentViewModel
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsViewModel
import com.delhivery.orion.ui.selectroute.SelectRouteViewModel
import com.delhivery.orion.ui.selectroutewelcome.SelectRouteWelcomeViewModel
import com.delhivery.orion.ui.splash.SplashViewModel
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
   * Sample ViewModel, should be removed before moving to production
   */
  /* Onboarding */
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
  @ViewModelScope(SelectRouteWelcomeViewModel::class)
  abstract fun bindSelectRouteWelcomeViewModel(selectRouteWelcomeViewModel: SelectRouteWelcomeViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SelectRouteViewModel::class)
  abstract fun bindSelectRouteViewModel(selectRouteViewModel: SelectRouteViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(OnboardingViewModel::class)
  abstract fun bindOnboardingViewMdel(onboardingViewModel: OnboardingViewModel): ViewModel

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

  @Binds
  internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}