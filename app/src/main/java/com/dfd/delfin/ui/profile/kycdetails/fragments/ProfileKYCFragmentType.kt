package com.dfd.delfin.ui.profile.kycdetails.fragments

import com.dfd.delfin.ui.base.BaseFragment

enum class ProfileKYCFragmentType(
    val position: Int,
    val creator: () -> BaseFragment<*, *>,
    val title: String
) {
    DetailsFragment(0, { YourKYCDetailsFragment.newInstance() }, "Details"),
    DocumentFragment(1, { KycDocumentsFragment.newInstance() }, "Documents");

    companion object {

        /**
         * Get [ProfileKYCFragmentType] by position
         */
        fun pos(position: Int) = values().firstOrNull { it.position == position }

        /**
         * Count
         */
        fun count() = values().size
    }
}