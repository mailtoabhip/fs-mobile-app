package com.delhivery.axle.injection.component

import android.view.inputmethod.BaseInputConnection
import com.delhivery.axle.injection.module.NetworkModule
import com.delhivery.axle.ui.auth.AuthenticationViewModel
import com.delhivery.axle.ui.biddetails.BidDetailsViewModel
import com.delhivery.axle.ui.bids.BidsViewModel
import com.delhivery.axle.ui.businessverification.BusinessVerificationViewModel
import com.delhivery.axle.ui.home.activity.bank.BankTransferViewModel
import com.delhivery.axle.ui.home.activity.home.HomeViewModel
import com.delhivery.axle.ui.home.activity.transactiondetail.TransactionDetailViewModel
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsViewModel
import com.delhivery.axle.ui.home.activity.wallet.WalletOnboardingViewModel
import com.delhivery.axle.ui.home.fragments.alerts.HomeAlertsViewModel
import com.delhivery.axle.ui.home.fragments.bids.HomeBidsViewModel
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsViewModel
import com.delhivery.axle.ui.home.fragments.pod.HomePodViewModel
import com.delhivery.axle.ui.profile.HomeProfileViewModel
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsViewModel
import com.delhivery.axle.ui.home.fragments.wallet.HomeWalletViewModel
import com.delhivery.axle.ui.kyc.aadhaar.AadhaarVerificationViewModel
import com.delhivery.axle.ui.kyc.address.CommunicationAddressViewModel
import com.delhivery.axle.ui.kyc.identityverification.IdentityVerificationViewModel
import com.delhivery.axle.ui.kyc.pan.PanVerificationViewModel
import com.delhivery.axle.ui.onboarding.BasicDetailsActivity
import com.delhivery.axle.ui.onboarding.BasicDetailsViewModel
import com.delhivery.axle.ui.onboarding.OnboardingViewModel
import com.delhivery.axle.ui.paymentdetails.PaymentDetailsViewModel
import com.delhivery.axle.ui.profile.BankDetailsViewModel
import com.delhivery.axle.ui.profile.profiledetails.ProfileDetailsViewModel
import com.delhivery.axle.ui.profile.kycdetails.ProfileKYCDetailsViewModel
import com.delhivery.axle.ui.profile.kycdetails.fragments.KYCDocumentsViewModel
import com.delhivery.axle.ui.profile.kycdetails.fragments.YourKYCDetailsViewModel
import com.delhivery.axle.ui.searchload.SearchLoadViewModel
import com.delhivery.axle.ui.searchload.fragments.searchload.SearchLoadFragmentViewModel
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsViewModel
import com.delhivery.axle.ui.searchongoingtrip.SearchOngoingTripViewModel
import com.delhivery.axle.ui.selectroute.activity.SelectRouteViewModel
import com.delhivery.axle.ui.selectroute.fragments.destination.SelectRouteDestinationViewModel
import com.delhivery.axle.ui.selectroute.fragments.origincity.SelectRouteOriginCityViewModel
import com.delhivery.axle.ui.selectroute.fragments.routeslist.SelectRouteListViewModel
import com.delhivery.axle.ui.selectroutewelcome.SelectRouteWelcomeViewModel
import com.delhivery.axle.ui.splash.SplashViewModel
import com.delhivery.axle.ui.team.TeamMembersViewModel
import com.delhivery.axle.ui.tripdetails.TripDetailsViewModel
import com.delhivery.axle.ui.tripdetails.UploadImageViewModel
import com.delhivery.axle.ui.userroutes.UserRoutesViewModel
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
   * Injects [HomeLoadsViewModel]
   */
  fun inject(viewModel: HomeLoadsViewModel)

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
   * Injects [BidDetailsViewModel]
   */
  fun inject(viewModel: BidDetailsViewModel)

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

  @Component.Builder
  interface Builder {
    fun build(): ViewModelInjector

    fun networkModule(networkModule: NetworkModule): Builder
  }
}