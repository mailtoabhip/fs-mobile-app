package com.delhivery.orion.ui.home

import com.delhivery.orion.ui.home.fragments.bids.HomeBidsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class HomeFragmentsBindingModule {
  @ContributesAndroidInjector
  internal abstract fun provideBidsFragment(): HomeBidsFragment
}