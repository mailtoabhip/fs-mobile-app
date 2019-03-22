package com.delhivery.orion.ui.searchload.fragments

import com.delhivery.orion.ui.searchload.fragments.searchload.SearchLoadFragment
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SearchLoadFragmentsBindingModule {
  @ContributesAndroidInjector
  internal abstract fun provideSearchLoadFragment(): SearchLoadFragment

  @ContributesAndroidInjector
  internal abstract fun provideSearchResultsFragment(): SearchResultsFragment
}