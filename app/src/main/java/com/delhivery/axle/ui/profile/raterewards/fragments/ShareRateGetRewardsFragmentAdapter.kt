package com.delhivery.axle.ui.profile.raterewards.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.delhivery.axle.R

class ShareRateGetRewardsFragmentAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(
  fragmentManager
) {
  override fun getItem(position: Int) = ShareRateGetRewardsFragmentType.pos(position)!!.fragment

  override fun getCount() = ShareRateGetRewardsFragmentType.count()

  override fun getPageTitle(position: Int) = ShareRateGetRewardsFragmentType.pos(position)!!.title

  fun getTabView(position: Int, context: Context): View? {
    val v: View = LayoutInflater.from(context).inflate(R.layout.kyc_tab, null)
    val tv = v.findViewById(R.id.custom_tab_textView_count) as TextView
    tv.setText(getPageTitle(position))
    return v
  }
}