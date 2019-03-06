package com.delhivery.orion.injection.module

import android.app.Activity
import android.content.Context
import com.delhivery.orion.injection.qualifier.ActivityContext
import com.delhivery.orion.injection.scope.ActivityScope
import com.delhivery.orion.ui.auth.AuthenticationActivity
import com.delhivery.orion.ui.home.HomeActivity
import com.delhivery.orion.ui.home.HomeFragmentsBindingModule
import com.delhivery.orion.ui.onboarding.OnboardingActivity
import com.delhivery.orion.ui.selectroute.SelectRouteActivity
import com.delhivery.orion.ui.selectroutewelcome.SelectRouteWelcomeActivity
import com.delhivery.orion.ui.splash.SplashActivity
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
  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsSplashActivityModule::class])
  internal abstract fun bindSplashActivity(): SplashActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsAuthenticationActivityModule::class])
  internal abstract fun bindAuthenticationActivityActivity(): AuthenticationActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsSelectRouteWelcomeActivityModule::class])
  internal abstract fun bindSelectRouteWelcomeActivity(): SelectRouteWelcomeActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsSelectRouteActivityModule::class])
  internal abstract fun bindSelectRouteActivity(): SelectRouteActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [AbsOnboardingActivityModule::class])
  internal abstract fun bindOnboardingActivity(): OnboardingActivity

  @ActivityScope
  @ContributesAndroidInjector(
      modules = [AbsHomeActivityModule::class, HomeFragmentsBindingModule::class]
  )
  internal abstract fun bindHomeActivity(): HomeActivity
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