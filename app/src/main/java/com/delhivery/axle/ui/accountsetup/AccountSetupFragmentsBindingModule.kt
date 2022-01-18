package com.delhivery.axle.ui.accountsetup

import com.delhivery.axle.ui.accountsetup.fragments.AccountDetailsFragment
import com.delhivery.axle.ui.accountsetup.fragments.AccountRoleFragment
import com.delhivery.axle.ui.accountsetup.fragments.PrimaryActionFragment
import com.delhivery.axle.ui.searchload.fragments.searchload.SearchLoadFragment
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AccountSetupFragmentsBindingModule {
  @ContributesAndroidInjector
  internal abstract fun providePrimaryActionFragment(): PrimaryActionFragment

  @ContributesAndroidInjector
  internal abstract fun provideAccountRoleFragment(): AccountRoleFragment

  @ContributesAndroidInjector
  internal abstract fun provideAccountDetailsFragment(): AccountDetailsFragment

}