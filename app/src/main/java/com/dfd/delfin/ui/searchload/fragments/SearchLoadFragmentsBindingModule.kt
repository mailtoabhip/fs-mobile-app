package com.dfd.delfin.ui.searchload.fragments

import com.dfd.delfin.ui.searchload.fragments.searchload.SearchLoadFragment
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SearchLoadFragmentsBindingModule {
  @ContributesAndroidInjector
  internal abstract fun provideSearchLoadFragment(): SearchLoadFragment

  @ContributesAndroidInjector
  internal abstract fun provideSearchResultsFragment(): SearchResultsFragment
}