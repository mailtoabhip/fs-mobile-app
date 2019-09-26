package com.delhivery.axle.injection.module

import android.app.Activity
import android.content.Context
import com.delhivery.axle.injection.qualifier.ActivityContext
import com.delhivery.axle.injection.scope.ActivityScope
import com.delhivery.axle.ui.auth.AuthenticationActivity
import com.delhivery.axle.ui.biddetails.BidDetailsActivity
import com.delhivery.axle.ui.bids.BidsActivity
import com.delhivery.axle.ui.bids.TripsActivity
import com.delhivery.axle.ui.home.activity.bank.BankTransferActivity
import com.delhivery.axle.ui.home.activity.fuel.ActiveTripsActivity
import com.delhivery.axle.ui.home.activity.fuelcard.CreateFuelCardActivity
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.ui.home.activity.transactiondetail.TransactionDetailActivity
import com.delhivery.axle.ui.home.activity.transactionlist.TransactionsActivity
import com.delhivery.axle.ui.home.fragments.HomeFragmentsBindingModule
import com.delhivery.axle.ui.onboarding.OnboardingActivity
import com.delhivery.axle.ui.searchload.SearchLoadActivity
import com.delhivery.axle.ui.searchload.fragments.SearchLoadFragmentsBindingModule
import com.delhivery.axle.ui.selectroute.activity.SelectRouteActivity
import com.delhivery.axle.ui.selectroute.fragments.SelectRouteFragmentsBindingModule
import com.delhivery.axle.ui.selectroutewelcome.SelectRouteWelcomeActivity
import com.delhivery.axle.ui.splash.SplashActivity
import com.delhivery.axle.ui.tripdetails.ImageViewActivity
import com.delhivery.axle.ui.tripdetails.TripDetailsActivity
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
  internal abstract fun bindSplashActivity(): SplashActivity

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
      modules = [AbsHomeActivityModule::class, HomeFragmentsBindingModule::class]
  )
  internal abstract fun bindHomeActivity(): HomeActivity

  /* Active/Lost/Confirm Bids activity*/
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsBidsActivityModule::class])
  internal abstract fun bindBidsActivity(): BidsActivity

  /* Advance/Balance/InTransit/Completed Trips activity*/
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsTripsActivityModule::class])
  internal abstract fun bindTripsActivity(): TripsActivity

  /* Bid Details activity */
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsBidDetailsActivityModule::class])
  internal abstract fun bindBidDetailsActivity(): BidDetailsActivity

  /* Search Load activity */
  @ActivityScope
  @ContributesAndroidInjector(
      modules = [AbsSearchLoadActivityModule::class, SearchLoadFragmentsBindingModule::class]
  )
  internal abstract fun bindSearchLoadActivity(): SearchLoadActivity

  /* Trip details activity */
  @ActivityScope
  @ContributesAndroidInjector(
      modules = [AbsTripDetailsActivityModule::class]
  )
  internal abstract fun bindTripDetailsActivity(): TripDetailsActivity

  /* Image view activity */
  @ActivityScope
  @ContributesAndroidInjector(
      modules = [AbsImageViewActivityModule::class]
  )
  internal abstract fun bindImageViewActivity(): ImageViewActivity

  /* Transactions activity */
  @ActivityScope
  @ContributesAndroidInjector(
      modules = [AbsTransactionsActivityModule::class]
  )
  internal abstract fun bindTransactionsActivity(): TransactionsActivity

  /* Transaction Detail activity */
  @ActivityScope
  @ContributesAndroidInjector(
      modules = [AbsTransactionDetailActivityModule::class]
  )
  internal abstract fun bindTransactionDetailActivity(): TransactionDetailActivity

  /* Bank Transfer activity */
  @ActivityScope
  @ContributesAndroidInjector(
      modules = [AbsBankTransferActivityModule::class]
  )
  internal abstract fun bindBankTransferActivity(): BankTransferActivity

  /* Fuel Card activity */
  @ActivityScope
  @ContributesAndroidInjector(
      modules = [AbsFuelCardActivityModule::class]
  )
  internal abstract fun bindFuelCardActivity(): ActiveTripsActivity

  /* Create Fuel Card activity */
  @ActivityScope
  @ContributesAndroidInjector(
      modules = [AbsCreateFuelCardActivityModule::class]
  )
  internal abstract fun bindCreateFuelCardActivity(): CreateFuelCardActivity
}

/**
 * Activity common modules,
 * should be created for each activity
 *
 * Activity specific modules should be created separately
 *
 */
@Module
internal abstract class AbsSplashActivityModule : ActivityModule<SplashActivity>()

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
internal abstract class AbsBidsActivityModule : ActivityModule<BidsActivity>()

@Module
internal abstract class AbsTripsActivityModule : ActivityModule<TripsActivity>()

@Module
internal abstract class AbsBidDetailsActivityModule : ActivityModule<BidDetailsActivity>()

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