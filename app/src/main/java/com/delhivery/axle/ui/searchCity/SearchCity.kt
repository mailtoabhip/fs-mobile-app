package com.delhivery.axle.ui.searchCity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil.setContentView
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivitySearchCityBinding
import com.delhivery.axle.ui.base.BaseActivity

class SearchCity : BaseActivity<ActivitySearchCityBinding, SearchCityViewModel>() {

    init {
        hasInlineProgress = true
    }


    override fun getViewModelClass() = SearchCityViewModel::class.java

    override fun layoutId()= R.layout.activity_search_city

    override fun requireConnection()= true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }
}


private const val FromPage = "from_page"

/**
 * Search City Intent
  */
fun searchCityIntent(
    context: Context,
    fromPage: Boolean = false
) = Intent( context, SearchCity::class.java).apply {
    putExtra(FromPage, fromPage)
}