package com.delhivery.axle.ui.profile.kycdetails.fragments

import com.delhivery.axle.ui.base.BaseFragment

enum class ProfileKYCFragmentType(
    val position: Int,
    val fragment: BaseFragment<*, *>,
    val title: String
) {
    DetailsFragment(0, YourKYCDetailsFragment._instance, "My Details"),
    DocumentFragment( 1, KycDocumentsFragment._instance, "Documents");

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