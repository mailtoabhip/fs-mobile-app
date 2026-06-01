package com.delhivery.axle.injection.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.delhivery.axle.SyncOfferData.MyWorker
import com.delhivery.axle.injection.module.DaggerWorkerFactory.ChildWorkerFactory
import com.delhivery.axle.injection.scope.ViewModelScope
import com.delhivery.axle.injection.scope.WorkerKey
import com.delhivery.axle.tokenExpiryHandling.RefreshTokenWorker
import com.delhivery.axle.ui.accountaction.AccountActionViewModel
import com.delhivery.axle.ui.accountdetails.AccountDetailsViewModel
import com.delhivery.axle.ui.accountrole.AccountRoleViewModel
import com.delhivery.axle.ui.auth.AuthenticationViewModel
import com.delhivery.axle.ui.biddetails.MarketPlaceBidDetailsViewModel
import com.delhivery.axle.ui.bids.BidsViewModel
import com.delhivery.axle.ui.bids.TripsViewModel
import com.delhivery.axle.ui.businessverification.BusinessVerificationViewModel
import com.delhivery.axle.ui.contractDetails.ContractDetailsViewModel
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
import com.delhivery.axle.ui.home.fragments.contracts.HomeContractsViewModel
import com.delhivery.axle.ui.home.fragments.home.HomeFragmentViewModel
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckViewModel
import com.delhivery.axle.ui.home.fragments.placements.HomePlacementsViewModel
import com.delhivery.axle.ui.home.fragments.pod.HomePodViewModel
import com.delhivery.axle.ui.home.fragments.pod.PendingPodViewModel
import com.delhivery.axle.ui.home.fragments.pod.SubmittedPodViewModel
import com.delhivery.axle.ui.profile.HomeProfileViewModel
import com.delhivery.axle.ui.home.fragments.trips.HomeTripsViewModel
import com.delhivery.axle.ui.home.fragments.trucks.HomeTrucksViewModel
import com.delhivery.axle.ui.home.fragments.wallet.HomeWalletViewModel
import com.delhivery.axle.ui.kyc.gst.GstVerificationViewModel
import com.delhivery.axle.ui.kyc.aadhaar.AadhaarVerificationViewModel
import com.delhivery.axle.ui.kyc.address.CommunicationAddressViewModel
import com.delhivery.axle.ui.kyc.documentverification.DocumentVerificationViewModel
import com.delhivery.axle.ui.comingsoon.ComingSoonViewModel
import com.delhivery.axle.ui.fastag.issuance.SalesCodeViewModel
import com.delhivery.axle.ui.fastag.issuance.SelectFasTagViewModel
import com.delhivery.axle.ui.fastag.issuance.PaymentBreakupViewModel
import com.delhivery.axle.ui.fastag.issuance.FastagCollectionViewModel
import com.delhivery.axle.ui.fastag.issuance.FastagKycViewModel
import com.delhivery.axle.ui.fastag.issuance.AddVehicleViewModel
import com.delhivery.axle.ui.kyc.identityverification.IdentityVerificationViewModel
import com.delhivery.axle.ui.kyc.pan.PanVerificationViewModel
import com.delhivery.axle.ui.ledger.ConsolidatedPageViewModel
import com.delhivery.axle.ui.loadwallet.LoadWalletViewModel
import com.delhivery.axle.ui.loadwallet.TransactionDetailsViewModel
import com.delhivery.axle.ui.loadwallet.RechargeDetailsViewModel
import com.delhivery.axle.ui.onboarding.BasicDetailsViewModel
import com.delhivery.axle.ui.onboarding.OnboardingViewModel
import com.delhivery.axle.ui.paymentdetails.PaymentDetailsViewModel
import com.delhivery.axle.ui.payment.PaymentWebViewViewModel
import com.delhivery.axle.ui.placementdetails.PlacementDetailsViewModel
import com.delhivery.axle.ui.fastag.fastag_details.FastagTransactionDetailsViewModel
import com.delhivery.axle.ui.fastag.recharge.FastagRechargeViewModel
import com.delhivery.axle.ui.fastag.trucks.FastagTrucksViewModel
import com.delhivery.axle.ui.fastag.pending.PendingActionsViewModel
import com.delhivery.axle.ui.fastag.pending.assign.AssignVehicleViewModel
import com.delhivery.axle.ui.fastag.pending.assign.FastagAssignmentViewModel
import com.delhivery.axle.ui.fastag.pending.assign.VehicleImageUploadViewModel
import com.delhivery.axle.ui.fastag.pending.assign.FastagImageUploadViewModel
import com.delhivery.axle.ui.fastag.wallet.AddMoneyDialogViewmodel
import com.delhivery.axle.ui.fastag.qdr.FastagDisputeIssuesViewModel
import com.delhivery.axle.ui.fastag.qdr.FastagTransactionSelectionViewModel
import com.delhivery.axle.ui.fastag.qdr.FastagDynamicDisputeFormViewModel
import com.delhivery.axle.ui.profile.BankDetailsViewModel
import com.delhivery.axle.ui.searchCity.SearchCityViewModel
import com.delhivery.axle.ui.profile.profiledetails.ProfileDetailsViewModel
import com.delhivery.axle.ui.profile.kycdetails.ProfileKYCDetailsViewModel
import com.delhivery.axle.ui.profile.kycdetails.fragments.KYCDocumentsViewModel
import com.delhivery.axle.ui.profile.kycdetails.fragments.YourKYCDetailsViewModel
import com.delhivery.axle.ui.profile.raterewards.ShareRateGetRewardsViewModel
import com.delhivery.axle.ui.profile.raterewards.fragments.rewards.YourRewardsFragmentViewModel
import com.delhivery.axle.ui.profile.raterewards.fragments.sharerate.ShareRateFragmentViewModel
import com.delhivery.axle.ui.searchcitystate.SearchCityStateViewModel
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
import com.delhivery.axle.ui.sharerate.ShareRateViewModel
import com.delhivery.axle.ui.splash.SplashViewModel
import com.delhivery.axle.ui.team.TeamMembersViewModel
import com.delhivery.axle.ui.tripdetails.ImageViewModel
import com.delhivery.axle.ui.tripdetails.TripDetailsViewModel
import com.delhivery.axle.ui.tripdetails.UploadImageViewModel
import com.delhivery.axle.ui.trucks.TruckViewModel
import com.delhivery.axle.ui.userroutes.ManageRouteViewModel
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
  @ViewModelScope(HomeFragmentViewModel::class)
  abstract fun bindHomeFragmentViewModel(homeFragmentViewModel: HomeFragmentViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(HomePlacementsViewModel::class)
  abstract fun bindHomePlacementsViewModel(homePlacementsViewModel: HomePlacementsViewModel): ViewModel


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
  @ViewModelScope(PendingPodViewModel::class)
  abstract fun bindPendingPodViewModel(viewModel: PendingPodViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SubmittedPodViewModel::class)
  abstract fun bindSubmittedPodViewModel(viewModel: SubmittedPodViewModel): ViewModel

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
  @ViewModelScope(MarketPlaceBidDetailsViewModel::class)
  abstract fun bindMarketPlaceBidDetailsViewModel(marketPlaceBidDetailsViewModel: MarketPlaceBidDetailsViewModel): ViewModel

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
  @ViewModelScope(HomeLoadsTruckViewModel::class)
  abstract fun bindHomeLoadsTruckViewModel(homeLoadsTruckViewModel: HomeLoadsTruckViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(HomeTrucksViewModel::class)
  abstract fun bindHomeTrucksViewModel(homeTrucksViewModel: HomeTrucksViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(TruckViewModel::class)
  abstract fun bindTruckViewModel(truckViewModel: TruckViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SearchCityViewModel::class)
  abstract fun bindSearchCityViewModel(searchCityViewModel: SearchCityViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(ProfileDetailsViewModel::class)
  abstract fun bindProfileDetailsViewModel(profileDetailsViewModel: ProfileDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(ProfileKYCDetailsViewModel::class)
  abstract fun bindProfileKYCDetailsViewModel(profileKYCDetailsViewModel: ProfileKYCDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(KYCDocumentsViewModel::class)
  abstract fun bindKYCDocumentsViewModel(kycDocumentsViewModel: KYCDocumentsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(YourKYCDetailsViewModel::class)
  abstract fun bindYourKYCDetailsViewModel(yourKYCDetailsViewModel: YourKYCDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(PanVerificationViewModel::class)
  abstract fun bindPanVerificationViewModel(viewModel:PanVerificationViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(GstVerificationViewModel::class)
  abstract fun bindGstVerificationViewModel(viewModel:GstVerificationViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(AadhaarVerificationViewModel::class)
  abstract fun bindAadhaarVerificationViewModel(viewModel:AadhaarVerificationViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(CommunicationAddressViewModel::class)
  abstract fun bindCommunicationAddressViewModel(viewModel:CommunicationAddressViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(BusinessVerificationViewModel::class)
  abstract fun bindBusinessVerificationViewModel(viewModel:BusinessVerificationViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(AccountActionViewModel::class)
  abstract fun bindAccountActionViewModel(viewModel:AccountActionViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(AccountRoleViewModel::class)
  abstract fun bindAccountRoleViewModel(viewModel:AccountRoleViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(AccountDetailsViewModel::class)
  abstract fun bindAccountDetailsViewModel(viewModel:AccountDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(IdentityVerificationViewModel::class)
  abstract fun bindIdentityVerificationViewModel(viewModel:IdentityVerificationViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(PaymentDetailsViewModel::class)
  abstract fun bindPaymentDetailsViewModel(viewModel: PaymentDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(PaymentWebViewViewModel::class)
  abstract fun bindPaymentWebViewViewModel(viewModel: PaymentWebViewViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(BankDetailsViewModel::class)
  abstract fun bindBankDetailsViewModel(viewModel: BankDetailsViewModel): ViewModel
  @Binds
  @IntoMap
  @ViewModelScope(BasicDetailsViewModel::class)
  abstract fun bindBasicDetailsViewModel(viewModel:BasicDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SearchCityStateViewModel::class)
  abstract fun bindSearchCityStateViewModel(viewModel:SearchCityStateViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(ManageRouteViewModel::class)
  abstract fun bindManageRouteViewModel(viewModel:ManageRouteViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(ShareRateGetRewardsViewModel::class)
  abstract fun bindShareRateGetRewardsViewModel(viewModel: ShareRateGetRewardsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(ShareRateFragmentViewModel::class)
  abstract fun bindShareRateFragmentViewModel(viewModel: ShareRateFragmentViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(YourRewardsFragmentViewModel::class)
  abstract fun bindYourRewardsFragmentViewModel(viewModel: YourRewardsFragmentViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(ShareRateViewModel::class)
  abstract fun bindShareRateViewModel(viewModel: ShareRateViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(HomeContractsViewModel::class)
  abstract fun bindHomeContractsViewModel(viewModel: HomeContractsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(ContractDetailsViewModel::class)
  abstract fun bindContractDetailsViewModel(viewModel: ContractDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(PlacementDetailsViewModel::class)
  abstract fun bindPlacementDetailsViewModel(viewModel: PlacementDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(FastagTransactionDetailsViewModel::class)
  abstract fun bindFastagTransactionDetailsViewModel(viewModel: FastagTransactionDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(FastagRechargeViewModel::class)
  abstract fun bindFastagRechargeViewModel(viewModel: FastagRechargeViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(FastagTrucksViewModel::class)
  abstract fun bindFastagTrucksViewModel(viewModel: FastagTrucksViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(PendingActionsViewModel::class)
  abstract fun bindPendingActionsViewModel(viewModel: PendingActionsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(AssignVehicleViewModel::class)
  abstract fun bindAssignVehicleViewModel(viewModel: AssignVehicleViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(FastagAssignmentViewModel::class)
  abstract fun bindFastagAssignmentViewModel(viewModel: FastagAssignmentViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(VehicleImageUploadViewModel::class)
  abstract fun bindVehicleImageUploadViewModel(viewModel: VehicleImageUploadViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(FastagImageUploadViewModel::class)
  abstract fun bindFastagImageUploadViewModel(viewModel: FastagImageUploadViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(LoadWalletViewModel::class)
  abstract fun bindLoadWalletViewModel(viewModel: LoadWalletViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(TransactionDetailsViewModel::class)
  abstract fun bindTransactionDetailsViewModel(viewModel: TransactionDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(RechargeDetailsViewModel::class)
  abstract fun bindRechargeDetailsViewModel(viewModel: RechargeDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(FastagDisputeIssuesViewModel::class)
  abstract fun bindFastagDisputeIssuesViewModel(viewModel: FastagDisputeIssuesViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(FastagTransactionSelectionViewModel::class)
  abstract fun bindFastagTransactionSelectionViewModel(viewModel: FastagTransactionSelectionViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(FastagDynamicDisputeFormViewModel::class)
  abstract fun bindFastagDynamicDisputeFormViewModel(viewModel: FastagDynamicDisputeFormViewModel): ViewModel

  @Binds
  internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

  @Binds
  @IntoMap
  @WorkerKey(MyWorker::class)
  abstract fun bindTopArtistsUpdateWorker(factory: MyWorker.Factory):
          ChildWorkerFactory

  @Binds
  @IntoMap
  @WorkerKey(RefreshTokenWorker::class)
  abstract fun bindRefreshTokenWorker(factory: RefreshTokenWorker.Factory):
      ChildWorkerFactory

    @Binds
    @IntoMap
    @ViewModelScope(AddMoneyDialogViewmodel::class)
    abstract fun bindPaymentDialogViewModel(viewModel: AddMoneyDialogViewmodel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(DocumentVerificationViewModel::class)
  abstract fun bindDocumentVerificationViewModel(viewModel: DocumentVerificationViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(ComingSoonViewModel::class)
  abstract fun bindComingSoonViewModel(viewModel: ComingSoonViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SalesCodeViewModel::class)
  abstract fun bindSalesCodeViewModel(viewModel: SalesCodeViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(SelectFasTagViewModel::class)
  abstract fun bindSelectFasTagViewModel(viewModel: SelectFasTagViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(PaymentBreakupViewModel::class)
  abstract fun bindPaymentBreakupViewModel(viewModel: PaymentBreakupViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(FastagCollectionViewModel::class)
  abstract fun bindFastagCollectionViewModel(viewModel: FastagCollectionViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(AddVehicleViewModel::class)
  abstract fun bindAddVehicleViewModel(viewModel: AddVehicleViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(FastagKycViewModel::class)
  abstract fun bindFastagKycViewModel(viewModel: FastagKycViewModel): ViewModel
}