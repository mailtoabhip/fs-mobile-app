package com.delhivery.orion.ui.home.fragments

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Home view pager fragments adapter
 */
class HomeFragmentsAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(
    fragmentManager
) {
  override fun getItem(position: Int) = HomeFragmentType.pos(position)!!.fragment

  override fun getCount() = HomeFragmentType.count()

  override fun getPageTitle(position: Int) = HomeFragmentType.pos(position)!!.title
}