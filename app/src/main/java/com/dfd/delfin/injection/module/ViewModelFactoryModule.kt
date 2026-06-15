package com.dfd.delfin.injection.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dfd.delfin.SyncOfferData.MyWorker
import com.dfd.delfin.injection.module.DaggerWorkerFactory.ChildWorkerFactory
import com.dfd.delfin.injection.scope.ViewModelScope
import com.dfd.delfin.injection.scope.WorkerKey
import com.dfd.delfin.tokenExpiryHandling.RefreshTokenWorker
import com.dfd.delfin.ui.accountaction.AccountActionViewModel
import com.dfd.delfin.ui.accountdetails.AccountDetailsViewModel
import com.dfd.delfin.ui.accountrole.AccountRoleViewModel
import com.dfd.delfin.ui.auth.AuthenticationViewModel
import com.dfd.delfin.ui.biddetails.MarketPlaceBidDetailsViewModel
import com.dfd.delfin.ui.bids.BidsViewModel
import com.dfd.delfin.ui.bids.TripsViewModel
import com.dfd.delfin.ui.businessverification.BusinessVerificationViewModel
import com.dfd.delfin.ui.contractDetails.ContractDetailsViewModel
import com.dfd.delfin.ui.home.activity.bank.BankTransferViewModel
import com.dfd.delfin.ui.home.activity.docket.DocketUpdateViewModel
import com.dfd.delfin.ui.home.activity.fuel.ActiveTripsViewModel
import com.dfd.delfin.ui.home.activity.fuelcard.CreateFuelCardViewModel
import com.dfd.delfin.ui.home.activity.home.HomeViewModel
import com.dfd.delfin.ui.home.activity.transactiondetail.TransactionDetailViewModel
import com.dfd.delfin.ui.home.activity.transactionlist.TransactionsViewModel
import com.dfd.delfin.ui.home.activity.wallet.WalletOnboardingViewModel
import com.dfd.delfin.ui.home.fragments.alerts.HomeAlertsViewModel
import com.dfd.delfin.ui.home.fragments.bids.HomeBidsViewModel
import com.dfd.delfin.ui.home.fragments.contracts.HomeContractsViewModel
import com.dfd.delfin.ui.home.fragments.home.HomeFragmentViewModel
import com.dfd.delfin.ui.home.fragments.loads_truck.HomeLoadsTruckViewModel
import com.dfd.delfin.ui.home.fragments.placements.HomePlacementsViewModel
import com.dfd.delfin.ui.home.fragments.pod.HomePodViewModel
import com.dfd.delfin.ui.home.fragments.pod.PendingPodViewModel
import com.dfd.delfin.ui.home.fragments.pod.SubmittedPodViewModel
import com.dfd.delfin.ui.profile.HomeProfileViewModel
import com.dfd.delfin.ui.home.fragments.trips.HomeTripsViewModel
import com.dfd.delfin.ui.home.fragments.trucks.HomeTrucksViewModel
import com.dfd.delfin.ui.home.fragments.wallet.HomeWalletViewModel
import com.dfd.delfin.ui.kyc.gst.GstVerificationViewModel
import com.dfd.delfin.ui.kyc.aadhaar.AadhaarVerificationViewModel
import com.dfd.delfin.ui.kyc.address.CommunicationAddressViewModel
import com.dfd.delfin.ui.kyc.documentverification.DocumentVerificationViewModel
import com.dfd.delfin.ui.comingsoon.ComingSoonViewModel
import com.dfd.delfin.ui.fastag.issuance.SalesCodeViewModel
import com.dfd.delfin.ui.fastag.issuance.SelectFasTagViewModel
import com.dfd.delfin.ui.fastag.issuance.PaymentBreakupViewModel
import com.dfd.delfin.ui.fastag.issuance.FastagCollectionViewModel
import com.dfd.delfin.ui.fastag.issuance.FastagKycViewModel
import com.dfd.delfin.ui.fastag.issuance.AddVehicleViewModel
import com.dfd.delfin.ui.kyc.identityverification.IdentityVerificationViewModel
import com.dfd.delfin.ui.kyc.pan.PanVerificationViewModel
import com.dfd.delfin.ui.ledger.ConsolidatedPageViewModel
import com.dfd.delfin.ui.loadwallet.LoadWalletViewModel
import com.dfd.delfin.ui.loadwallet.TransactionDetailsViewModel
import com.dfd.delfin.ui.loadwallet.RechargeDetailsViewModel
import com.dfd.delfin.ui.onboarding.BasicDetailsViewModel
import com.dfd.delfin.ui.onboarding.OnboardingViewModel
import com.dfd.delfin.ui.paymentdetails.PaymentDetailsViewModel
import com.dfd.delfin.ui.payment.PaymentWebViewViewModel
import com.dfd.delfin.ui.placementdetails.PlacementDetailsViewModel
import com.dfd.delfin.ui.fastag.fastag_details.FastagTransactionDetailsViewModel
import com.dfd.delfin.ui.fastag.recharge.FastagRechargeViewModel
import com.dfd.delfin.ui.fastag.trucks.FastagTrucksViewModel
import com.dfd.delfin.ui.fastag.tagAssignment.pendingActions.PendingActionsViewModel
import com.dfd.delfin.ui.fastag.tagAssignment.pendingActions.AssignVehicleViewModel
import com.dfd.delfin.ui.fastag.tagAssignment.assign.RCUploadViewModel
import com.dfd.delfin.ui.fastag.tagAssignment.assign.VehicleImageUploadViewModel
import com.dfd.delfin.ui.fastag.tagAssignment.assign.kyv.KYVFastagImageUploadViewModel
import com.dfd.delfin.ui.fastag.tagAssignment.assign.VehicleDetailsViewModel
import com.dfd.delfin.ui.fastag.wallet.AddMoneyDialogViewmodel
import com.dfd.delfin.ui.fastag.qdr.FastagDisputeIssuesViewModel
import com.dfd.delfin.ui.fastag.qdr.FastagTransactionSelectionViewModel
import com.dfd.delfin.ui.fastag.qdr.FastagDynamicDisputeFormViewModel
import com.dfd.delfin.ui.fastag.tagMapping.TagMappingViewModel
import com.dfd.delfin.ui.profile.BankDetailsViewModel
import com.dfd.delfin.ui.searchCity.SearchCityViewModel
import com.dfd.delfin.ui.profile.profiledetails.ProfileDetailsViewModel
import com.dfd.delfin.ui.profile.kycdetails.ProfileKYCDetailsViewModel
import com.dfd.delfin.ui.profile.kycdetails.fragments.KYCDocumentsViewModel
import com.dfd.delfin.ui.profile.kycdetails.fragments.YourKYCDetailsViewModel
import com.dfd.delfin.ui.profile.raterewards.ShareRateGetRewardsViewModel
import com.dfd.delfin.ui.profile.raterewards.fragments.rewards.YourRewardsFragmentViewModel
import com.dfd.delfin.ui.profile.raterewards.fragments.sharerate.ShareRateFragmentViewModel
import com.dfd.delfin.ui.searchcitystate.SearchCityStateViewModel
import com.dfd.delfin.ui.searchload.SearchLoadViewModel
import com.dfd.delfin.ui.searchload.fragments.searchload.SearchLoadFragmentViewModel
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsViewModel
import com.dfd.delfin.ui.searchongoingtrip.SearchOngoingTripViewModel
import com.dfd.delfin.ui.searchtrip.SearchViewModel
import com.dfd.delfin.ui.selectroute.activity.SelectRouteViewModel
import com.dfd.delfin.ui.selectroute.fragments.destination.SelectRouteDestinationViewModel
import com.dfd.delfin.ui.selectroute.fragments.detail.SelectRouteDetailViewModel
import com.dfd.delfin.ui.selectroute.fragments.origincity.SelectRouteOriginCityViewModel
import com.dfd.delfin.ui.selectroute.fragments.routeslist.SelectRouteListViewModel
import com.dfd.delfin.ui.selectroutewelcome.SelectRouteWelcomeViewModel
import com.dfd.delfin.ui.sharerate.ShareRateViewModel
import com.dfd.delfin.ui.splash.SplashViewModel
import com.dfd.delfin.ui.team.TeamMembersViewModel
import com.dfd.delfin.ui.tripdetails.ImageViewModel
import com.dfd.delfin.ui.tripdetails.TripDetailsViewModel
import com.dfd.delfin.ui.tripdetails.UploadImageViewModel
import com.dfd.delfin.ui.trucks.TruckViewModel
import com.dfd.delfin.ui.userroutes.ManageRouteViewModel
import com.dfd.delfin.ui.userroutes.UserRoutesViewModel
import com.dfd.delfin.utils.ViewModelFactory
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
  @ViewModelScope(RCUploadViewModel::class)
  abstract fun bindFastagAssignmentViewModel(viewModel: RCUploadViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(VehicleImageUploadViewModel::class)
  abstract fun bindVehicleImageUploadViewModel(viewModel: VehicleImageUploadViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(KYVFastagImageUploadViewModel::class)
  abstract fun bindFastagImageUploadViewModel(viewModel: KYVFastagImageUploadViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelScope(VehicleDetailsViewModel::class)
  abstract fun bindVehicleDetailsViewModel(viewModel: VehicleDetailsViewModel): ViewModel

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

  @Binds
  @IntoMap
  @ViewModelScope(TagMappingViewModel::class)
  abstract fun bindTagMappingViewModel(viewModel: TagMappingViewModel): ViewModel
}