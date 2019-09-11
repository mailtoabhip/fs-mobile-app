package com.delhivery.axle.ui.selectroute.fragments

import com.delhivery.axle.ui.selectroute.fragments.destination.SelectRouteDestinationFragment
import com.delhivery.axle.ui.selectroute.fragments.detail.SelectRouteDetailFragment
import com.delhivery.axle.ui.selectroute.fragments.origincity.SelectRouteOriginCityFragment
import com.delhivery.axle.ui.selectroute.fragments.routeslist.SelectRouteListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SelectRouteFragmentsBindingModule {
  @ContributesAndroidInjector
  internal abstract fun provideSelectRouteOriginCityFragment(): SelectRouteOriginCityFragment

  @ContributesAndroidInjector
  internal abstract fun provideSelectRouteDestinationFragment(): SelectRouteDestinationFragment

  @ContributesAndroidInjector
  internal abstract fun provideSelectRouteListFragment(): SelectRouteListFragment

  @ContributesAndroidInjector
  internal abstract fun provideSelectRouteDetailFragment(): SelectRouteDetailFragment
}