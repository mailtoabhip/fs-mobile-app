package com.dfd.delfin.injection.component

import com.dfd.delfin.injection.module.NetworkModule
import com.dfd.delfin.ui.auth.AuthenticationViewModel
import com.dfd.delfin.ui.bids.BidsViewModel
import com.dfd.delfin.ui.businessverification.BusinessVerificationViewModel
import com.dfd.delfin.ui.contractDetails.ContractDetailsViewModel
import com.dfd.delfin.ui.home.activity.bank.BankTransferViewModel
import com.dfd.delfin.ui.home.activity.home.HomeViewModel
import com.dfd.delfin.ui.home.activity.transactiondetail.TransactionDetailViewModel
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionsViewModel
import com.dfd.delfin.ui.home.activity.wallet.WalletOnboardingViewModel
import com.dfd.delfin.ui.home.fragments.alerts.HomeAlertsViewModel
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsViewModel
import com.dfd.delfin.ui.home.fragments.contracts.HomeContractsViewModel
import com.dfd.delfin.ui.home.fragments.loads_truck.HomeLoadsTruckViewModel
import com.dfd.delfin.ui.home.fragments.placements.HomePlacementsViewModel
import com.dfd.delfin.ui.home.fragments.pod.HomePodViewModel
import com.dfd.delfin.ui.profile.HomeProfileViewModel
import com.dfd.delfin.ui.home.fragments.trips.HomeTripsViewModel
import com.dfd.delfin.ui.home.fragments.trucks.HomeTrucksViewModel
import com.dfd.delfin.ui.home.fragments.wallet.HomeWalletViewModel
import com.dfd.delfin.ui.kyc.aadhaar.AadhaarVerificationViewModel
import com.dfd.delfin.ui.kyc.address.CommunicationAddressViewModel
import com.dfd.delfin.ui.kyc.identityverification.IdentityVerificationViewModel
import com.dfd.delfin.ui.kyc.pan.PanVerificationViewModel
import com.dfd.delfin.ui.onboarding.BasicDetailsViewModel
import com.dfd.delfin.ui.onboarding.OnboardingViewModel
import com.dfd.delfin.ui.paymentdetails.PaymentDetailsViewModel
import com.dfd.delfin.ui.placementdetails.PlacementDetailsViewModel
import com.dfd.delfin.ui.profile.BankDetailsViewModel
import com.dfd.delfin.ui.profile.profiledetails.ProfileDetailsViewModel
import com.dfd.delfin.ui.profile.kycdetails.ProfileKYCDetailsViewModel
import com.dfd.delfin.ui.profile.kycdetails.fragments.KYCDocumentsViewModel
import com.dfd.delfin.ui.profile.kycdetails.fragments.YourKYCDetailsViewModel
import com.dfd.delfin.ui.profile.raterewards.ShareRateGetRewardsViewModel
import com.dfd.delfin.ui.profile.raterewards.fragments.rewards.YourRewardsFragmentViewModel
import com.dfd.delfin.ui.profile.raterewards.fragments.sharerate.ShareRateFragmentViewModel
import com.dfd.delfin.ui.searchCity.SearchCityViewModel
import com.dfd.delfin.ui.searchload.SearchLoadViewModel
import com.dfd.delfin.ui.searchload.fragments.searchload.SearchLoadFragmentViewModel
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsViewModel
import com.dfd.delfin.ui.searchongoingtrip.SearchOngoingTripViewModel
import com.dfd.delfin.ui.selectroute.activity.SelectRouteViewModel
import com.dfd.delfin.ui.selectroute.fragments.destination.SelectRouteDestinationViewModel
import com.dfd.delfin.ui.selectroute.fragments.origincity.SelectRouteOriginCityViewModel
import com.dfd.delfin.ui.selectroute.fragments.routeslist.SelectRouteListViewModel
import com.dfd.delfin.ui.selectroutewelcome.SelectRouteWelcomeViewModel
import com.dfd.delfin.ui.splash.SplashViewModel
import com.dfd.delfin.ui.team.TeamMembersViewModel
import com.dfd.delfin.ui.tripdetails.TripDetailsViewModel
import com.dfd.delfin.ui.tripdetails.UploadImageViewModel
import com.dfd.delfin.ui.trucks.TruckViewModel
import com.dfd.delfin.ui.userroutes.ManageRouteViewModel
import com.dfd.delfin.ui.userroutes.UserRoutesViewModel
import dagger.Component
import javax.inject.Singleton

/**
 * Component providing inject() methods for presenters
 */
@Singleton
@Component(modules = [NetworkModule::class])
interface ViewModelInjector {

  /**
   * Injects [SplashViewModel]
   */
  fun inject(viewModel: SplashViewModel)

  /**
   * Injects [AuthenticationViewModel]
   */
  fun inject(viewModel: AuthenticationViewModel)


  /**
   * Injects [OnboardingViewModel]
   */
  fun inject(viewModel: OnboardingViewModel)

  /**
   * Injects [SelectRouteWelcomeViewModel]
   */
  fun inject(viewModel: SelectRouteWelcomeViewModel)

  /**
   * Injects [SelectRouteViewModel]
   */
  fun inject(viewModel: SelectRouteViewModel)

  /**
   * Injects [SelectRouteOriginCityViewModel]
   */
  fun inject(viewModel: SelectRouteOriginCityViewModel)

  /**
   * Injects [SelectRouteDestinationViewModel]
   */
  fun inject(viewModel: SelectRouteDestinationViewModel)

  /**
   * Injects [SelectRouteListViewModel]
   */
  fun inject(viewModel: SelectRouteListViewModel)

  /**
   * Injects [HomeViewModel]
   */
  fun inject(viewModel: HomeViewModel)

  /**
   * Injects [HomeBidsViewModel]
   */
  fun inject(viewModel: HomeBidsViewModel)

  /**
   * Injects [HomeTripsViewModel]
   */
  fun inject(viewModel: HomeTripsViewModel)

  /**
   * Injects [HomeAlertsViewModel]
   */
  fun inject(viewModel: HomeAlertsViewModel)

  /**
   * Injects [HomeProfileViewModel]
   */
  fun inject(viewModel: HomeProfileViewModel)

  /**
   * Injects [HomeWalletViewModel]
   */
  fun inject(viewModel: HomeWalletViewModel)

  /**
   * Injects [HomePodViewModel]
   */
  fun inject(viewModel: HomePodViewModel)

  /**
   * Injects [BidsViewModel]
   */
  fun inject(viewModel: BidsViewModel)

  /**
   * Injects [SearchLoadViewModel]
   */
  fun inject(viewModel: SearchLoadViewModel)

  /**
   * Injects [SearchLoadFragmentViewModel]
   */
  fun inject(viewModel: SearchLoadFragmentViewModel)

  /**
   * Injects [SearchResultsViewModel]
   */
  fun inject(viewModel: SearchResultsViewModel)

  /**
   * Injects [TripDetailsViewModel]
   */
  fun inject(viewModel: TripDetailsViewModel)

  /**
   * Injects [TransactionDetailViewModel]
   */
  fun inject(viewModel: TransactionDetailViewModel)

  /**
   * Injects [TransactionsViewModel]
   */
  fun inject(viewModel: TransactionsViewModel)

  /**
   * Injects [BankTransferViewModel]
   */
  fun inject(viewModel: BankTransferViewModel)

  /**
   * Injects [WalletOnboardingViewModel]
   */
  fun inject(viewModel: WalletOnboardingViewModel)

  /**
   * Injects [UploadImageViewModel]
   */
  fun inject(viewModel: UploadImageViewModel)

  /**
   * Injects [TeamMembersViewModel]
   */
  fun inject(viewModel: TeamMembersViewModel)

  /**
   * Injects [UserRoutesViewModel]
   */
  fun inject(viewModel: UserRoutesViewModel)

  /**
   * Injects [SearchOngoingTripViewModel]
   */
  fun inject(viewModel: SearchOngoingTripViewModel)

  /**
   * Injects [ProfileDetailsViewModel]
   */
  fun inject(viewModel: ProfileDetailsViewModel)

  /**
   * Injects [ProfileKYCDetailsViewModel]
   */
  fun inject(viewModel: ProfileKYCDetailsViewModel)

  /**
   * Injects [KYCDocumentsViewModel]
   */
  fun inject(viewModel: KYCDocumentsViewModel)

  /**
   * Injects [YourKYCDetailsViewModel]
   */
  fun inject(viewModel: YourKYCDetailsViewModel)

  /**
   * Injects [PanVerificationViewModel]
   */
  fun inject(viewModel: PanVerificationViewModel)

  /**
   * Injects [BusinessVeificationViewModel]
   */
  fun inject(viewModel: BusinessVerificationViewModel)

  /**
   * Injects [AadhaarVerificationViewModel]
   */
  fun inject(viewModel: AadhaarVerificationViewModel)

  /**
   * Injects [CommunicationAddressViewModel]
   */
  fun inject(viewModel: CommunicationAddressViewModel)

  /**
   * Injects [IdentityVerificationViewModel]
   */
  fun inject(viewModel: IdentityVerificationViewModel)


  fun inject(viewModel: PaymentDetailsViewModel)

  fun inject(viewModel: BankDetailsViewModel)


  /**
   * Injects [BasicDetailsViewModel]
   */
  fun inject(viewModel: BasicDetailsViewModel)

  /**
   * Injects [ManageRouteViewModel]
   */
  fun inject(viewModel: ManageRouteViewModel)

  /**
   * Injects [HomeLoadsTruckViewModel]
   */
  fun inject(viewModel: HomeLoadsTruckViewModel)

  /**
   * Injects [HomeTrucksViewModel]
   */
  fun inject(viewModel: HomeTrucksViewModel)

  /**
   * Injects [TruckViewModel]
   */
  fun inject(viewModel: TruckViewModel)

  /**
   * Injects [SearchCityViewModel]
   */
  fun inject(viewModel: SearchCityViewModel)

  /**
   * Injects [ShareRateGetRewardsViewModel]
   */
  fun inject(viewModel: ShareRateGetRewardsViewModel)

  /**
   * Injects [ShareRateFragmentViewModel]
   */
  fun inject(viewModel: ShareRateFragmentViewModel)

  /**
   * Injects [YourRewardsFragmentViewModel]
   */
  fun inject(viewModel: YourRewardsFragmentViewModel)

  /**
   * Injects [HomeContractsViewModel]
   */
  fun inject(viewModel: HomeContractsViewModel)

  /**
   * Injects [ContractDetailsViewModel]
   */
  fun inject(viewModel: ContractDetailsViewModel)

  /**
   * Injects [HomePlacementsViewmodel]
   */
  fun inject(viewModel: HomePlacementsViewModel)

  /**
   * Injects [PlacementDetailsViewModel]
   */
  fun inject(viewModel: PlacementDetailsViewModel)

  @Component.Builder
  interface Builder {
    fun build(): ViewModelInjector

    fun networkModule(networkModule: NetworkModule): Builder
  }
}