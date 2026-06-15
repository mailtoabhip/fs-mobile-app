package com.dfd.delfin.ui.profile.kycdetails.fragments

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ProfileKYCFragmentBindingModule {
    @ContributesAndroidInjector
    internal abstract fun provideYourKYCDetailsFragment(): YourKYCDetailsFragment

    @ContributesAndroidInjector
    internal abstract fun provideKycDocumentsFragment(): KycDocumentsFragment
}