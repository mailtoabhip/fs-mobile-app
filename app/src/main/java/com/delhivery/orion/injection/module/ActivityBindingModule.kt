package com.delhivery.orion.injection.module

import android.app.Activity
import android.content.Context
import com.delhivery.orion.injection.qualifier.ActivityContext
import com.delhivery.orion.injection.scope.ActivityScope
import com.delhivery.orion.ui.auth.AuthenticationActivity
import com.delhivery.orion.ui.biddetails.BidDetailsActivity
import com.delhivery.orion.ui.bids.BidsActivity
import com.delhivery.orion.ui.home.HomeActivity
import com.delhivery.orion.ui.home.fragments.HomeFragmentsBindingModule
import com.delhivery.orion.ui.onboarding.OnboardingActivity
import com.delhivery.orion.ui.searchload.SearchLoadActivity
import com.delhivery.orion.ui.searchload.fragments.SearchLoadFragmentsBindingModule
import com.delhivery.orion.ui.selectroute.SelectRouteActivity
import com.delhivery.orion.ui.selectroute.fragments.SelectRouteFragmentsBindingModule
import com.delhivery.orion.ui.selectroutewelcome.SelectRouteWelcomeActivity
import com.delhivery.orion.ui.splash.SplashActivity
import com.delhivery.orion.ui.tripdetails.TripDetailsActivity
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
  /* Spalsh/Launch activity */
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
internal abstract class AbsBidDetailsActivityModule : ActivityModule<BidDetailsActivity>()

@Module
internal abstract class AbsSearchLoadActivityModule : ActivityModule<SearchLoadActivity>()

@Module
internal abstract class AbsTripDetailsActivityModule : ActivityModule<TripDetailsActivity>()

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