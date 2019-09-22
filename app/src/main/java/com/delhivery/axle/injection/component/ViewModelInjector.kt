package com.delhivery.axle.injection.component

import com.delhivery.axle.injection.module.NetworkModule
import com.delhivery.axle.ui.auth.AuthenticationViewModel
import com.delhivery.axle.ui.biddetails.BidDetailsViewModel
import com.delhivery.axle.ui.bids.BidsViewModel
import com.delhivery.axle.ui.home.activity.bank.BankTransferViewModel
import com.delhivery.axle.ui.home.activity.home.HomeViewModel
import com.delhivery.axle.ui.home.activity.transactiondetail.TransactionDetailViewModel
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsViewModel
import com.delhivery.axle.ui.home.fragments.alerts.HomeAlertsViewModel
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsViewModel
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsViewModel
import com.delhivery.axle.ui.home.fragments.payment.HomePaymentViewModel
import com.delhivery.axle.ui.home.fragments.profile.HomeProfileViewModel
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsViewModel
import com.delhivery.axle.ui.home.fragments.wallet.HomeWalletViewModel
import com.delhivery.axle.ui.onboarding.OnboardingViewModel
import com.delhivery.axle.ui.searchload.SearchLoadViewModel
import com.delhivery.axle.ui.searchload.fragments.searchload.SearchLoadFragmentViewModel
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsViewModel
import com.delhivery.axle.ui.selectroute.activity.SelectRouteViewModel
import com.delhivery.axle.ui.selectroute.fragments.destination.SelectRouteDestinationViewModel
import com.delhivery.axle.ui.selectroute.fragments.origincity.SelectRouteOriginCityViewModel
import com.delhivery.axle.ui.selectroute.fragments.routeslist.SelectRouteListViewModel
import com.delhivery.axle.ui.selectroutewelcome.SelectRouteWelcomeViewModel
import com.delhivery.axle.ui.splash.SplashViewModel
import com.delhivery.axle.ui.tripdetails.TripDetailsViewModel
import dagger.Component
import javax.inject.Singleton

/**
 * Component providing inject() methods for presenters
 */
@Singleton
@Component(modules = [NetworkModule::class])
interface ViewModelInjector {

  /**
   *
   * Injects required dependencies into the specified
   *
   */

  fun inject(viewModel: SplashViewModel)

  fun inject(viewModel: AuthenticationViewModel)

  fun inject(viewModel: OnboardingViewModel)

  fun inject(viewModel: SelectRouteWelcomeViewModel)

  fun inject(viewModel: SelectRouteViewModel)

  fun inject(viewModel: SelectRouteOriginCityViewModel)

  fun inject(viewModel: SelectRouteDestinationViewModel)

  fun inject(viewModel: SelectRouteListViewModel)

  fun inject(viewModel: HomeViewModel)

  fun inject(viewModel: HomeBidsViewModel)

  fun inject(viewModel: HomeLoadsViewModel)

  fun inject(viewModel: HomeTripsViewModel)

  fun inject(viewModel: HomePaymentViewModel)

  fun inject(viewModel: HomeAlertsViewModel)

  fun inject(viewModel: HomeProfileViewModel)

  fun inject(viewModel: HomeWalletViewModel)

  fun inject(viewModel: BidsViewModel)

  fun inject(viewModel: BidDetailsViewModel)

  fun inject(viewModel: SearchLoadViewModel)

  fun inject(viewModel: SearchLoadFragmentViewModel)

  fun inject(viewModel: SearchResultsViewModel)

  fun inject(viewModel: TripDetailsViewModel)

  fun inject(viewModel: TransactionDetailViewModel)

  fun inject(viewModel: TransactionsViewModel)

  fun inject(viewModel: BankTransferViewModel)

  @Component.Builder
  interface Builder {
    fun build(): ViewModelInjector

    fun networkModule(networkModule: NetworkModule): Builder
  }
}