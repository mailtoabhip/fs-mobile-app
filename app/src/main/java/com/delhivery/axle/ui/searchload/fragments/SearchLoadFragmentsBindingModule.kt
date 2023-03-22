package com.delhivery.axle.ui.searchload.fragments

import com.delhivery.axle.ui.searchload.fragments.searchload.SearchLoadFragment
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SearchLoadFragmentsBindingModule {
  @ContributesAndroidInjector
  internal abstract fun provideSearchLoadFragment(): SearchLoadFragment

  @ContributesAndroidInjector
  internal abstract fun provideIntracitySearchLoadFragment(): IntracitySearchLoadFragment

  @ContributesAndroidInjector
  internal abstract fun provideSearchResultsFragment(): SearchResultsFragment
}