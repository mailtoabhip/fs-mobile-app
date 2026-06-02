package com.delhivery.axle.injection.module

import android.app.Activity
import android.content.Context
import com.delhivery.axle.injection.qualifier.ActivityContext
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.ui.accountaction.AccountActionActivity
import com.delhivery.axle.ui.accountdetails.AccountDetailsActivity
import com.delhivery.axle.ui.accountrole.AccountRoleActivity
import com.delhivery.axle.ui.auth.AccountDeletionActivity
import com.delhivery.axle.ui.auth.AuthenticationActivity
import com.delhivery.axle.ui.auth.InvalidActivity
import com.delhivery.axle.ui.biddetails.MarketPlaceBidDetailsActivity
import com.delhivery.axle.ui.bids.TripsActivity
import com.delhivery.axle.ui.businessverification.BusinessVerificationActivity
import com.delhivery.axle.ui.comingsoon.ComingSoonActivity
import com.delhivery.axle.ui.contractDetails.ContractDetailsActivity
import com.delhivery.axle.ui.contractDetails.PlacementsContractDetailsActivity
import com.delhivery.axle.ui.fastag.fastag_details.FastagTransactionDetailsActivity
import com.delhivery.axle.ui.fastag.pending.PendingActionsActivity
import com.delhivery.axle.ui.fastag.pending.assign.AssignVehicleActivity
import com.delhivery.axle.ui.fastag.qdr.FastagDisputeIssuesActivity
import com.delhivery.axle.ui.fastag.qdr.FastagDynamicDisputeFormActivity
import com.delhivery.axle.ui.fastag.qdr.FastagRaiseDisputeActivity
import com.delhivery.axle.ui.fastag.qdr.FastagTransactionSelectionActivity
import com.delhivery.axle.ui.fastag.recharge.FastagRechargeActivity
import com.delhivery.axle.ui.fastag.trucks.FastagTrucksActivity
import com.delhivery.axle.ui.home.activity.bank.BankTransferActivity
import com.delhivery.axle.ui.home.activity.docket.DocketUpdateActivity
import com.delhivery.axle.ui.home.activity.fuel.ActiveTripsActivity
import com.delhivery.axle.ui.home.activity.fuelcard.CreateFuelCardActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.activity.transactiondetail.TransactionDetailActivity
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsActivity
import com.delhivery.axle.ui.home.activity.wallet.WalletOnboardingActivity
import com.delhivery.axle.ui.home.fragments.HomeFragmentsBindingModule
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeTruckLoadsFragmentBindingModule
import com.delhivery.axle.ui.invoicereview.InvoiceReviewActivity
import com.delhivery.axle.ui.kyc.aadhaar.AadhaarVerificationActivity
import com.delhivery.axle.ui.kyc.address.AddressActivity
import com.delhivery.axle.ui.kyc.address.CommunicationAddressActivity
import com.delhivery.axle.ui.kyc.documentverification.DocumentVerificationActivity
import com.delhivery.axle.ui.kyc.gst.GstVerificationActivity
import com.delhivery.axle.ui.kyc.identityverification.IdentityVerificationActivity
import com.delhivery.axle.ui.kyc.pan.PanVerificationActivity
import com.delhivery.axle.ui.ledger.ConsolidatedPageActivity
import com.delhivery.axle.ui.loadwallet.LoadWalletActivity
import com.delhivery.axle.ui.loadwallet.RechargeDetailsActivity
import com.delhivery.axle.ui.loadwallet.TransactionDetailsActivity
import com.delhivery.axle.ui.loadwallet.WalletFragmentBindingModule
import com.delhivery.axle.ui.onboarding.BasicDetailsActivity
import com.delhivery.axle.ui.onboarding.OnboardingActivity
import com.delhivery.axle.ui.payment.PaymentWebViewActivity
import com.delhivery.axle.ui.paymentdetails.PaymentDetailsActivity
import com.delhivery.axle.ui.paymentdetails.VendorPolicyActivity
import com.delhivery.axle.ui.placementdetails.PlacementDetailsActivity
import com.delhivery.axle.ui.profile.BankDetailsActivity
import com.delhivery.axle.ui.profile.HelpSupportActivity
import com.delhivery.axle.ui.profile.MyProfileActivity
import com.delhivery.axle.ui.profile.PlacementsActivity
import com.delhivery.axle.ui.profile.PlacementsFragmentsBindingModule
import com.delhivery.axle.ui.profile.kycdetails.ProfileKYCDetailsActivity
import com.delhivery.axle.ui.profile.kycdetails.fragments.ProfileKYCFragmentBindingModule
import com.delhivery.axle.ui.profile.profiledetails.ProfileDetailsActivity
import com.delhivery.axle.ui.profile.raterewards.ShareRateGetRewardsActivity
import com.delhivery.axle.ui.profile.raterewards.fragments.ShareRateGetRewardsFragmentBindingModule
import com.delhivery.axle.ui.searchCity.SearchCity
import com.delhivery.axle.ui.searchcitystate.SearchCityStateActivity
import com.delhivery.axle.ui.searchcitystate.SearchOriginCityActivity
import com.delhivery.axle.ui.searchload.SearchLoadActivity
import com.delhivery.axle.ui.searchload.fragments.SearchLoadFragmentsBindingModule
import com.delhivery.axle.ui.searchongoingtrip.SearchOngoingTripActivity
import com.delhivery.axle.ui.searchtrip.SearchActivity
import com.delhivery.axle.ui.selectroute.activity.SelectRouteActivity
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentsBindingModule
import com.delhivery.axle.ui.selectroutewelcome.SelectRouteWelcomeActivity
import com.delhivery.axle.ui.sharerate.ShareRateActivity
import com.delhivery.axle.ui.splash.StartRoutingActivity
import com.delhivery.axle.ui.team.TeamMembersActivity
import com.delhivery.axle.ui.tripdetails.ImageViewActivity
import com.delhivery.axle.ui.tripdetails.TripDetailsActivity
import com.delhivery.axle.ui.tripdetails.UploadImageActivity
import com.delhivery.axle.ui.trucks.AddTruckPathwayActivity
import com.delhivery.axle.ui.trucks.TruckActivity
import com.delhivery.axle.ui.userroutes.ManageRouteActivity
import com.delhivery.axle.ui.userroutes.UserRoutesActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity

/**
 * Activity Binding Modules
 *
 * All Activity specific modules should be declared here along with Abstract Activity Module
 */
@Module
abstract class ActivityBindingModule {

  /* Splash/Launch activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsSplashActivityModule::class])
  internal abstract fun bindSplashActivity(): StartRoutingActivity

  /* Authentication/Login activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsAuthenticationActivityModule::class])
  internal abstract fun bindAuthenticationActivityActivity(): AuthenticationActivity

  /* Select route welcome activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsSelectRouteWelcomeActivityModule::class])
  internal abstract fun bindSelectRouteWelcomeActivity(): SelectRouteWelcomeActivity

  /* Route selection fragments activity */
  @ActivityScope
  @ContributesAndroidInjector(
      modules = [AbsSelectRouteActivityModule::class, SelectRouteFragmentsBindingModule::class]
  )
  internal abstract fun bindSelectRouteActivity(): SelectRouteActivity

  /* Onboarding view pager activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsOnboardingActivityModule::class])
  internal abstract fun bindOnboardingActivity(): OnboardingActivity

  /* Home activity */
  @ActivityScope
  @ContributesAndroidInjector(
      modules = [AbsHomeActivityModule::class, HomeFragmentsBindingModule::class, HomeTruckLoadsFragmentBindingModule::class]
  )
  internal abstract fun bindHomeActivity(): HomeActivity

  /* Advance/Balance/InTransit/Completed Trips activity*/
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsTripsActivityModule::class])
  internal abstract fun bindTripsActivity(): TripsActivity


  /* MarketPlace Bid Details activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsMarketPlaceBidDetailsActivityModule::class])
  internal abstract fun bindMarketPlaceBidDetailsActivity(): MarketPlaceBidDetailsActivity

  /* Search Load activity */
  @ActivityScope
  @ContributesAndroidInjector(
      modules = [AbsSearchLoadActivityModule::class, SearchLoadFragmentsBindingModule::class]
  )
  internal abstract fun bindSearchLoadActivity(): SearchLoadActivity

  /* Trip details activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsTripDetailsActivityModule::class])
  internal abstract fun bindTripDetailsActivity(): TripDetailsActivity

  /* Image view activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsImageViewActivityModule::class])
  internal abstract fun bindImageViewActivity(): ImageViewActivity

  /* Transactions activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsTransactionsActivityModule::class])
  internal abstract fun bindTransactionsActivity(): TransactionsActivity

  /* Transaction Detail activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsTransactionDetailActivityModule::class])
  internal abstract fun bindTransactionDetailActivity(): TransactionDetailActivity

  /* Bank Transfer activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsBankTransferActivityModule::class])
  internal abstract fun bindBankTransferActivity(): BankTransferActivity

  /* Fuel Card activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsFuelCardActivityModule::class])
  internal abstract fun bindFuelCardActivity(): ActiveTripsActivity

  /* Create Fuel Card activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsCreateFuelCardActivityModule::class])
  internal abstract fun bindCreateFuelCardActivity(): CreateFuelCardActivity

  /* Wallet onboarding activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsWalletOnboardingActivityModule::class])
  internal abstract fun bindWalletOnboardingActivity(): WalletOnboardingActivity

  /* Upload image activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsUploadImageActivityModule::class])
  internal abstract fun bindUploadImageActivity(): UploadImageActivity /* Upload image activity */

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsDocketUpdateActivityModule::class])
  internal abstract fun bindDocketUpdateActivity(): DocketUpdateActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsSearchActivityModule::class])
  internal abstract fun bindSearchActivity(): SearchActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsTeamMembersActivityModule::class])
  internal abstract fun bindTeamMembersActivity(): TeamMembersActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsUserRoutesActivityModule::class])
  internal abstract fun bindUserRoutesActivity(): UserRoutesActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsSearchOngoingTripActivityModule::class])
  internal abstract fun bindSearchOngoingTripActivity(): SearchOngoingTripActivity

  /* Consolidated page activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsConsolidatedPageActivityModule::class])
  internal abstract fun bindConsolidatedPageActivity(): ConsolidatedPageActivity

  /* Truck activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsTruckActivityModule::class])
  internal abstract fun bindTruckActivity(): TruckActivity

  /* Search City Activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsSearchCityModule::class])
  internal abstract fun bindSearchCityActivity() : SearchCity

  /*Add Truck pathway activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsAddTruckPathwayActivityModule::class])
  internal abstract fun bindAddTruckPathwayActivity(): AddTruckPathwayActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsInvalidModule::class])
  internal abstract fun bindModule(): InvalidActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsAccountDeletionModule::class])
  internal abstract fun bindDeletionModule(): AccountDeletionActivity

  /* HelpSupport activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsHelpSupportModule::class])
  internal abstract fun bindHelpSupportActivity(): HelpSupportActivity

  /*Profile Details Activity*/
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsProfileDetailsActivityModule::class])
  internal abstract fun bindProfileDetailsActivity() : ProfileDetailsActivity

  /*Bank Details Activity*/
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsBankDetailsActivityModule::class])
  internal abstract fun bindBankDetailsActivity() : BankDetailsActivity

  /*Profile KYC Details Activity*/
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsProfileKYCDetailsActivityModule::class, ProfileKYCFragmentBindingModule::class])
  internal abstract fun  bindProfileKYCDetailsActivity() : ProfileKYCDetailsActivity

  /* Pan Verification page activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsPanVerificationActivityModule::class])
  internal abstract fun bindPanVerificationActivity(): PanVerificationActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsBusinessVerificationActivityModule::class])
  internal abstract fun bindBusinessVerificationActivity(): BusinessVerificationActivity

  /* Aadhar Verification page activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsAadhaarVerificationActivityModule::class])
  internal abstract fun bindAadhaarVerificationActivity(): AadhaarVerificationActivity

  /* Communication Address page activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsCommunicationAddressActivityModule::class])
  internal abstract fun bindCommunicationAddressActivity(): CommunicationAddressActivity

  /* alternate Address page activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsAddressActivityModule::class])
  internal abstract fun bindAddressActivity(): AddressActivity

  /* Gst Verification page activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbGstVerificationActivityModule::class])
  internal abstract fun bindGstVerificationActivity(): GstVerificationActivity

  /* Account Action page activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsAccountActionActivityModule::class])
  internal abstract fun bindAccountActionActivity(): AccountActionActivity

  /* Account Action page activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsAccountRoleActivityModule::class])
  internal abstract fun bindAccountRoleActivity(): AccountRoleActivity

  /* Account Action page activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsAccountDetailsActivityModule::class])
  internal abstract fun binAccountDetailsActivity(): AccountDetailsActivity

  /* MyProfileActivity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsMyProfileActivityModule::class])
  internal abstract fun binMyProfileActivity(): MyProfileActivity

  /* Account Action page activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsIdentityVerificationActivityModule::class])
  internal abstract fun bindIdentityVerificationActivity(): IdentityVerificationActivity

  /* payment details page Activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsPaymentDetailsActivityModule::class])
  internal abstract fun bindPaymentDetailsActivity(): PaymentDetailsActivity

  /* policy details page Activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsVendorPolicyActivityModule::class])
  internal abstract fun bindVendorPolicyActivity(): VendorPolicyActivity

  /* Payment WebView Activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsPaymentWebViewActivityModule::class])
  internal abstract fun bindPaymentWebViewActivity(): PaymentWebViewActivity

  /* Basic Details page activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsBasicDetailsActivityModule::class])
  internal abstract fun bindBasicDetailsActivity(): BasicDetailsActivity

  /* Search City page activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsSearchCityStateActivityModule::class])
  internal abstract fun bindSearchCityStateActivity(): SearchCityStateActivity

  /* Search Origin City page activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsSearchOriginCityActivityModule::class])
  internal abstract fun bindSearchOriginCityActivity(): SearchOriginCityActivity

  /* Manage Route */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsMangeRouteActivityModule::class])
  internal abstract fun bindManageRouteActivity(): ManageRouteActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsShareRateGetRewardsActivityModule::class, ShareRateGetRewardsFragmentBindingModule::class])
  internal abstract fun bindShareRateGetRewardsActivity(): ShareRateGetRewardsActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsShareRateActivityModule::class])
  internal abstract fun bindShareRateActivity(): ShareRateActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsContractDetailsActivityModule::class])
  internal abstract fun bindContractDetailsActivity(): ContractDetailsActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsPlacementsContractDetailsActivityModule::class])
  internal abstract fun bindPlacementsContractDetailsActivity(): PlacementsContractDetailsActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsPlacementDetailsActivityModule::class])
  internal abstract fun bindPlacementDetailsActivity(): PlacementDetailsActivity

  /* FASTag Transaction Details activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsFastagTransactionDetailsActivityModule::class])
  internal abstract fun bindFastagTransactionDetailsActivity(): FastagTransactionDetailsActivity

  /* Load Wallet activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsLoadWalletActivityModule::class, WalletFragmentBindingModule::class])
  internal abstract fun bindLoadWalletActivity(): LoadWalletActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsTransactionDetailsActivityModule::class])
  internal abstract fun bindTransactionDetailsActivity(): TransactionDetailsActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsRechargeDetailsActivityModule::class])
  internal abstract fun bindRechargeDetailsActivity(): RechargeDetailsActivity

  /* FASTag Recharge activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsFastagRechargeActivityModule::class])
  internal abstract fun bindFastagRechargeActivity(): FastagRechargeActivity

  /* FASTag Trucks activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsFastagTrucksActivityModule::class])
  internal abstract fun bindFastagTrucksActivity(): FastagTrucksActivity

  /* FASTag Pending Actions activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsPendingActionsActivityModule::class])
  internal abstract fun bindPendingActionsActivity(): PendingActionsActivity

  /* Assign Vehicle activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsAssignVehicleActivityModule::class])
  internal abstract fun bindAssignVehicleActivity(): AssignVehicleActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsPlacementsActivityModule::class, PlacementsFragmentsBindingModule::class])
  internal abstract fun bindPlacementsActivity(): PlacementsActivity

  /* FASTag Transaction Detail activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsFastagTransactionDetailActivityModule::class])
  internal abstract fun bindFastagTransactionDetailActivity(): FastagRaiseDisputeActivity

  /* FASTag Dispute Issues activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsFastagDisputeIssuesActivityModule::class])
  internal abstract fun bindFastagDisputeIssuesActivity(): FastagDisputeIssuesActivity

  /* FASTag Transaction Selection activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsFastagTransactionSelectionActivityModule::class])
  internal abstract fun bindFastagTransactionSelectionActivity(): FastagTransactionSelectionActivity

  /* FASTag Dynamic Dispute Form activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsFastagDynamicDisputeFormActivityModule::class])
  internal abstract fun bindFastagDynamicDisputeFormActivity(): FastagDynamicDisputeFormActivity

  /* Invoice Review activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsInvoiceReviewActivityModule::class])
  internal abstract fun bindInvoiceReviewActivity(): InvoiceReviewActivity

  /* Document Verification activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsDocumentVerificationActivityModule::class])
  internal abstract fun bindDocumentVerificationActivity(): DocumentVerificationActivity

  /* Coming Soon activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsComingSoonActivityModule::class])
  internal abstract fun bindComingSoonActivity(): ComingSoonActivity
}



/**
 * Activity common modules,
 * should be created for each activity
 *
 * Activity specific modules should be created separately
 *
 */
@Module
internal abstract class AbsSplashActivityModule : ActivityModule<StartRoutingActivity>()

@Module
internal abstract class AbsAuthenticationActivityModule : ActivityModule<AuthenticationActivity>()

@Module
internal abstract class AbsSelectRouteWelcomeActivityModule : ActivityModule<SelectRouteWelcomeActivity>()

@Module
internal abstract class AbsSelectRouteActivityModule : ActivityModule<SelectRouteActivity>()

@Module
internal abstract class AbsOnboardingActivityModule : ActivityModule<OnboardingActivity>()

@Module
internal abstract class AbsHomeActivityModule : ActivityModule<HomeActivity>()

@Module
internal abstract class AbsTripsActivityModule : ActivityModule<TripsActivity>()



@Module
internal abstract class AbsPlacementsContractDetailsActivityModule : ActivityModule<PlacementsContractDetailsActivity>()

@Module
internal abstract class AbsMarketPlaceBidDetailsActivityModule : ActivityModule<MarketPlaceBidDetailsActivity>()

@Module
internal abstract class AbsSearchLoadActivityModule : ActivityModule<SearchLoadActivity>()

@Module
internal abstract class AbsTripDetailsActivityModule : ActivityModule<TripDetailsActivity>()

@Module
internal abstract class AbsImageViewActivityModule : ActivityModule<ImageViewActivity>()

@Module
internal abstract class AbsTransactionsActivityModule : ActivityModule<TransactionsActivity>()

@Module
internal abstract class AbsTransactionDetailActivityModule : ActivityModule<TransactionDetailActivity>()

@Module
internal abstract class AbsBankTransferActivityModule : ActivityModule<BankTransferActivity>()

@Module
internal abstract class AbsFuelCardActivityModule : ActivityModule<ActiveTripsActivity>()

@Module
internal abstract class AbsCreateFuelCardActivityModule : ActivityModule<CreateFuelCardActivity>()

@Module
internal abstract class AbsWalletOnboardingActivityModule : ActivityModule<WalletOnboardingActivity>()

@Module
internal abstract class AbsUploadImageActivityModule : ActivityModule<UploadImageActivity>()

@Module
internal abstract class AbsDocketUpdateActivityModule : ActivityModule<DocketUpdateActivity>()

@Module
internal abstract class AbsSearchActivityModule : ActivityModule<SearchActivity>()

@Module
internal abstract class AbsTeamMembersActivityModule: ActivityModule<TeamMembersActivity>()

@Module
internal abstract class AbsUserRoutesActivityModule: ActivityModule<UserRoutesActivity>()

@Module
internal abstract class AbsConsolidatedPageActivityModule : ActivityModule<ConsolidatedPageActivity>()

@Module
internal abstract class AbsSearchOngoingTripActivityModule : ActivityModule<SearchOngoingTripActivity>()

@Module
internal abstract class AbsTruckActivityModule : ActivityModule<TruckActivity>()

@Module
internal abstract class AbsSearchCityModule : ActivityModule<SearchCity>()

@Module
internal abstract class AbsAddTruckPathwayActivityModule : ActivityModule<AddTruckPathwayActivity>()

@Module
internal abstract class AbsInvalidModule : ActivityModule<InvalidActivity>()

@Module
internal abstract class AbsAccountDeletionModule : ActivityModule<AccountDeletionActivity>()

@Module
internal abstract class AbsHelpSupportModule : ActivityModule<HelpSupportActivity>()

@Module
internal abstract class  AbsProfileDetailsActivityModule: ActivityModule<ProfileDetailsActivity>()

@Module
internal abstract class AbsBankDetailsActivityModule : ActivityModule<BankDetailsActivity>()

@Module
internal abstract class AbsProfileKYCDetailsActivityModule : ActivityModule<ProfileKYCDetailsActivity>()

@Module
internal abstract class AbsPanVerificationActivityModule : ActivityModule<PanVerificationActivity>()

@Module
internal abstract class AbGstVerificationActivityModule : ActivityModule<GstVerificationActivity>()

@Module
internal abstract class AbsAadhaarVerificationActivityModule : ActivityModule<AadhaarVerificationActivity>()

@Module
internal abstract class AbsCommunicationAddressActivityModule : ActivityModule<CommunicationAddressActivity>()

@Module
internal abstract class AbsAddressActivityModule : ActivityModule<AddressActivity>()

@Module
internal abstract class AbsBusinessVerificationActivityModule : ActivityModule<BusinessVerificationActivity>()

@Module
internal abstract class AbsAccountRoleActivityModule : ActivityModule<AccountRoleActivity>()

@Module
internal abstract class AbsAccountActionActivityModule : ActivityModule<AccountActionActivity>()

@Module
internal abstract class AbsAccountDetailsActivityModule : ActivityModule<AccountDetailsActivity>()

@Module
internal abstract class AbsMyProfileActivityModule : ActivityModule<MyProfileActivity>()

@Module
internal abstract class AbsIdentityVerificationActivityModule : ActivityModule<IdentityVerificationActivity>()

@Module
internal abstract class AbsPaymentDetailsActivityModule : ActivityModule<PaymentDetailsActivity>()

@Module
internal abstract class AbsVendorPolicyActivityModule : ActivityModule<VendorPolicyActivity>()

@Module
internal abstract class AbsPaymentWebViewActivityModule : ActivityModule<PaymentWebViewActivity>()

@Module
internal abstract class AbsBasicDetailsActivityModule : ActivityModule<BasicDetailsActivity>()

@Module
internal abstract class AbsSearchCityStateActivityModule : ActivityModule<SearchCityStateActivity>()

@Module
internal abstract class AbsSearchOriginCityActivityModule : ActivityModule<SearchOriginCityActivity>()

@Module
internal abstract class AbsMangeRouteActivityModule : ActivityModule<ManageRouteActivity>()

@Module
internal abstract class AbsShareRateActivityModule : ActivityModule<ShareRateActivity>()

@Module
internal abstract class AbsShareRateGetRewardsActivityModule : ActivityModule<ShareRateGetRewardsActivity>()

@Module
internal abstract class AbsContractDetailsActivityModule : ActivityModule<ContractDetailsActivity>()

@Module
internal abstract class AbsPlacementDetailsActivityModule : ActivityModule<PlacementDetailsActivity>()

@Module
internal abstract class AbsFastagTransactionDetailsActivityModule : ActivityModule<FastagTransactionDetailsActivity>()

@Module
internal abstract class AbsLoadWalletActivityModule : ActivityModule<LoadWalletActivity>()

@Module
internal abstract class AbsTransactionDetailsActivityModule : ActivityModule<TransactionDetailsActivity>()

@Module
internal abstract class AbsRechargeDetailsActivityModule : ActivityModule<RechargeDetailsActivity>()

@Module
internal abstract class AbsFastagRechargeActivityModule : ActivityModule<FastagRechargeActivity>()

@Module
internal abstract class AbsFastagTrucksActivityModule : ActivityModule<FastagTrucksActivity>()

@Module
internal abstract class AbsPendingActionsActivityModule : ActivityModule<PendingActionsActivity>()

@Module
internal abstract class AbsAssignVehicleActivityModule : ActivityModule<AssignVehicleActivity>()

@Module
internal abstract class AbsPlacementsActivityModule : ActivityModule<PlacementsActivity>()

@Module
internal abstract class AbsFastagDisputeIssuesActivityModule : ActivityModule<FastagDisputeIssuesActivity>()

@Module
internal abstract class AbsFastagTransactionSelectionActivityModule : ActivityModule<FastagTransactionSelectionActivity>()

@Module
internal abstract class AbsFastagTransactionDetailActivityModule : ActivityModule<FastagRaiseDisputeActivity>()

@Module
internal abstract class AbsFastagDynamicDisputeFormActivityModule : ActivityModule<FastagDynamicDisputeFormActivity>()

@Module
internal abstract class AbsInvoiceReviewActivityModule : ActivityModule<InvoiceReviewActivity>()

@Module
internal abstract class AbsDocumentVerificationActivityModule : ActivityModule<DocumentVerificationActivity>()

@Module
internal abstract class AbsComingSoonActivityModule : ActivityModule<ComingSoonActivity>()

/**
 * Activity Binds Module
 *
 */
@Module(includes = [BaseActivityModule::class])
internal abstract class ActivityModule<in T : DaggerAppCompatActivity> {
  @Binds
  @ActivityScope
  internal abstract fun bindDaggerAppCompatActivity(activity: T): DaggerAppCompatActivity

  @Binds
  @ActivityScope
  internal abstract fun bindActivity(activity: T): Activity

  @Binds
  @ActivityScope
  @ActivityContext
  internal abstract fun bindContext(activity: T): Context
}

/**
 * Activity Specific common dependencies are provided from here
 */
@Module
class BaseActivityModule {
  //todo - provide Common dependencies like - UIUtils, Navigation Utils etc...
}