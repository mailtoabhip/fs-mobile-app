package com.delhivery.axle.injection.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.delhivery.axle.injection.scope.ViewModelScope
import com.delhivery.axle.ui.accountsetup.AccountSetupViewModel
import com.delhivery.axle.ui.auth.AuthenticationViewModel
import com.delhivery.axle.ui.biddetails.BidDetailsViewModel
import com.delhivery.axle.ui.bids.BidsViewModel
import com.delhivery.axle.ui.bids.TripsViewModel
import com.delhivery.axle.ui.home.activity.bank.BankTransferViewModel
import com.delhivery.axle.ui.home.activity.docket.DocketUpdateViewModel
import com.delhivery.axle.ui.home.activity.fuel.ActiveTripsViewModel
import com.delhivery.axle.ui.home.activity.fuelcard.CreateFuelCardViewModel
import com.delhivery.axle.ui.home.activity.home.HomeViewModel
import com.delhivery.axle.ui.home.activity.transactiondetail.TransactionDetailViewModel
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsViewModel
import com.delhivery.axle.ui.home.activity.wallet.WalletOnboardingViewModel
import com.delhivery.axle.ui.home.fragments.alerts.HomeAlertsViewModel
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsViewModel
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsViewModel
import com.delhivery.axle.ui.home.fragments.pod.HomePodViewModel
import com.delhivery.axle.ui.home.fragments.profile.HomeProfileViewModel
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsViewModel
import com.delhivery.axle.ui.home.fragments.wallet.HomeWalletViewModel
import com.delhivery.axle.ui.kyc.gst.GstVerificationViewModel
import com.delhivery.axle.ui.kyc.pan.PanVerificationViewModel
import com.delhivery.axle.ui.ledger.ConsolidatedPageViewModel
import com.delhivery.axle.ui.onboarding.OnboardingViewModel
import com.delhivery.axle.ui.searchload.SearchLoadViewModel
import com.delhivery.axle.ui.searchload.fragments.searchload.SearchLoadFragmentViewModel
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsViewModel
import com.delhivery.axle.ui.searchongoingtrip.SearchOngoingTripViewModel
import com.delhivery.axle.ui.searchtrip.SearchViewModel
import com.delhivery.axle.ui.selectroute.activity.SelectRouteViewModel
import com.delhivery.axle.ui.selectroute.fragments.destination.SelectRouteDestinationViewModel
import com.delhivery.axle.ui.selectroute.fragments.detail.SelectRouteDetailViewModel
import com.delhivery.axle.ui.selectroute.fragments.origincity.SelectRouteOriginCityViewModel
import com.delhivery.axle.ui.selectroute.fragments.routeslist.SelectRouteListViewModel
import com.delhivery.axle.ui.selectroutewelcome.SelectRouteWelcomeViewModel
import com.delhivery.axle.ui.splash.SplashViewModel
import com.delhivery.axle.ui.team.TeamMembersViewModel
import com.delhivery.axle.ui.tripdetails.ImageViewModel
import com.delhivery.axle.ui.tripdetails.TripDetailsViewModel
import com.delhivery.axle.ui.tripdetails.UploadImageViewModel
import com.delhivery.axle.ui.userroutes.UserRoutesViewModel
import com.delhivery.axle.utils.ViewModelFactory
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

  @Binds
  @IntoMap
  @ViewModelScope(HomeViewModel::class)
  abstract fun bindHomeViewModel(homeViewModel: HomeViewModel): ViewModel

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
  @ViewModelScope(HomeAlertsViewModel::class)
  abstract fun bindHomeAlertsViewModel(homeAlertsViewModel: HomeAlertsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(HomeProfileViewModel::class)
  abstract fun bindHomeProfileViewModel(homeProfileViewModel: HomeProfileViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(HomePodViewModel::class)
  abstract fun bindHomePodViewModel(viewModel: HomePodViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(BidsViewModel::class)
  abstract fun bindBidsViewModel(bidsViewModel: BidsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(TripsViewModel::class)
  abstract fun bindTripsViewModel(tripsViewModel: TripsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(BidDetailsViewModel::class)
  abstract fun bindBidDetailsViewModel(bidDetailsViewModel: BidDetailsViewModel): ViewModel

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
  @IntoMap
  @ViewModelScope(TripDetailsViewModel::class)
  abstract fun bindTripDetailsViewModel(tripDetailsViewModel: TripDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(ImageViewModel::class)
  abstract fun bindImageViewModel(imageViewModel: ImageViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(HomeWalletViewModel::class)
  abstract fun bindHomeWalletViewModel(viewModel: HomeWalletViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(TransactionsViewModel::class)
  abstract fun bindTransactionsViewModel(viewModel: TransactionsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(TransactionDetailViewModel::class)
  abstract fun bindTransactionsDetailiewModel(viewModel: TransactionDetailViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(BankTransferViewModel::class)
  abstract fun bindBankTransferViewwModel(viewModel: BankTransferViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(ActiveTripsViewModel::class)
  abstract fun bindTripsFuelCardViewwModel(viewModel: ActiveTripsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(CreateFuelCardViewModel::class)
  abstract fun bindCreateFuelCardViewwModel(viewModel: CreateFuelCardViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(WalletOnboardingViewModel::class)
  abstract fun bindWalletOnboardingViewwModel(viewModel: WalletOnboardingViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(UploadImageViewModel::class)
  abstract fun bindUploadImageViewModel(viewModel: UploadImageViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(DocketUpdateViewModel::class)
  abstract fun bindDocketUpdateViewModel(viewModel: DocketUpdateViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SearchViewModel::class)
  abstract fun bindSearchViewModel(viewModel: SearchViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(TeamMembersViewModel::class)
  abstract fun bindTeamMembersViewModel(viewModel: TeamMembersViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(UserRoutesViewModel::class)
  abstract fun bindUserRoutesViewModel(viewModel: UserRoutesViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(ConsolidatedPageViewModel::class)
  abstract fun bindConsolidatedPageViewModel(viewModel: ConsolidatedPageViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SearchOngoingTripViewModel::class)
  abstract fun bindSearchOngoingTripViewModel(viewModel: SearchOngoingTripViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(AccountSetupViewModel::class)
  abstract fun bindAccountSetupViewModel(viewModel:AccountSetupViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(PanVerificationViewModel::class)
  abstract fun bindPanVerificationViewModel(viewModel:PanVerificationViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(GstVerificationViewModel::class)
  abstract fun bindGstVerificationViewModel(viewModel:GstVerificationViewModel): ViewModel


  @Binds
  internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}