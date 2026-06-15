package com.dfd.delfin.ui.loadwallet

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class WalletFragmentBindingModule {
    @ContributesAndroidInjector
    internal abstract fun provideWalletTransactionsFragment(): WalletTransactionsFragment

    @ContributesAndroidInjector
    internal abstract fun provideWalletRechargesFragment(): WalletRechargesFragment
}
