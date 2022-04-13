package com.delhivery.axle.ui.searchcitystate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.CitySelected
import com.delhivery.axle.data.search.SearchTimeOutAction
import com.delhivery.axle.data.search.SearchWarningAction_NoResult
import com.delhivery.axle.databinding.ActivitySearchOriginCityBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.REQCODE_DESTINATION_SELECT_CITY
import com.delhivery.axle.utils.REQCODE_SELECT_CITY
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class SearchOriginCityActivity : BaseActivity<ActivitySearchOriginCityBinding, SearchCityStateViewModel>() ,
    SearchCityStateRVAdapterInterface{

    init {
        hasInlineProgress = true
    }

    override fun getViewModelClass() = SearchCityStateViewModel::class.java

    override fun layoutId()= R.layout.activity_search_origin_city

    override fun requireConnection()= true

    var isLoadingData = true

    @Inject
    lateinit var userPrefs: UserPrefs

    init {
        hasInlineProgress = true
    }

    private val adapter by lazy { SearchOriginCityRvAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.cityType = intent.getStringExtra(CityType) ?: ""
        viewModel.selectedData = intent.getStringExtra(SelectedData)?:""
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)
        title = if(viewModel.cityType == "origin") "Origin Location" else "Destination Location"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvCityItems.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@SearchOriginCityActivity.adapter

        }

        viewModel.searchLiveData.observe(this, Observer {
            adapter.resetStaticData()
            if (it != null) {
                adapter.operation(it)
            }
        })

        binding.editQuery.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s != null) {
                    // binding.historyLayout.visibility= View.GONE
                    try {
                        viewModel.searchText = s.trim().toString()
                        Log.d("prefix", s.trim().toString())

                        if (viewModel.searchText.length in 3..10) {
                            refreshData()
                        } else {
                            adapter.resetStaticData()
                        }
                    }  catch (e: Exception) {
                        binding.editQuery.error = e.message
                    }
                }
            }
        })

        if (viewModel.searchText.length in 3..10) {
            refreshData()
        }

        if(viewModel.selectedData.isNotEmpty()){
            binding.editQuery.setText(viewModel.selectedData)
        }
    }
    override fun handleAction(
        actionId: String,
        item: BaseCityStateRVAdapterItem<*>) {
        when(actionId){
            CitySelected -> {
                val data = item.data as CityModel
                if(viewModel.cityType.equals("origin")){
                    setResult(REQCODE_SELECT_CITY, Intent().apply {
                        putExtra("City", data)
                        putExtra(CityType, viewModel.cityType)
                    })
                }
                finish()
            }
            SearchWarningAction_NoResult -> {
                refreshData()
            }

            SearchTimeOutAction -> {
                refreshData()
            }
        }
    }

    private fun refreshData() {
        adapter.resetStaticData()
        viewModel.searchCity(viewModel.searchText)
    }

}

/**
 * Search City Intent
 */
fun searchOriginCityIntent(
    context: Context
) = Intent( context, SearchOriginCityActivity::class.java)