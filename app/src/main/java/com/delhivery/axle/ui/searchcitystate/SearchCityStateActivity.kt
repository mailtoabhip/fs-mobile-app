package com.delhivery.axle.ui.searchcitystate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.CitySelected
import com.delhivery.axle.data.search.SearchTimeOutAction
import com.delhivery.axle.data.search.SearchWarningAction_NoResult
import com.delhivery.axle.databinding.ActivitySearchCityStateBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.utils.REQCODE_DESTINATION_SELECT_CITY
import com.delhivery.axle.utils.REQCODE_SELECT_CITY
import com.delhivery.axle.utils.extensions.addRxTextWatcher
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.io
import kotlinx.android.synthetic.main.spinner_item.text
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.ObservableSource
import io.reactivex.Observable
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

class SearchCityStateActivity : BaseActivity<ActivitySearchCityStateBinding, SearchCityStateViewModel>() ,
    SearchCityStateRVAdapterInterface {

    init {
        hasInlineProgress = true
    }


    override fun getViewModelClass() = SearchCityStateViewModel::class.java

    override fun layoutId()= R.layout.activity_search_city_state

    override fun requireConnection()= true

    var isLoadingData = true

    @Inject lateinit var userPrefs: UserPrefs

    init {
        hasInlineProgress = true
    }

    private val adapter by lazy { SearchCityStateRvAdapter(this) }

    private val selectedAdapter by lazy { SelectedCityStateRvAdapter(this) }

    private val popularAdapter by lazy { PopularCitiesAdapter(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.cityType = intent.getStringExtra(CityType) ?: ""
        viewModel.haveOldDestinations = intent.getBooleanExtra(HaveOldDestinations,false)

    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)
        title = if(viewModel.cityType == "origin") "Origin Location" else "Destination Location"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvCityItems.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@SearchCityStateActivity.adapter

        }
        binding.rvSelectedCityItems.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            adapter = this@SearchCityStateActivity.selectedAdapter

        }

        binding.rvPopularCityItems.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@SearchCityStateActivity.popularAdapter

        }

        viewModel.searchLiveData.observe(this, Observer {
            if (it != null) {
                    adapter.clearItems()
                    adapter.operation(it)
            }
        })

        binding.editQuery.addRxTextWatcher()
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (!TextUtils.isEmpty(it)) {
                    binding.popularLl.visibility= View.GONE
                    try {
                        viewModel.searchText = it?.trim().toString()
                        Log.d("prefix", it?.trim().toString())

                        if (viewModel.searchText.length in 2..10) {
                            refreshData()
                        } else {
                            adapter.clearItems()
                            binding.popularLl.visibility= View.VISIBLE
                        }
                    }  catch (e: Exception) {
                        binding.editQuery.error = e.message
                    }
                } else{
                    adapter.clearItems()
                    binding.popularLl.visibility= View.VISIBLE
                }
            }

        if (viewModel.searchText.length in 2..30) {
            refreshData()
        }
        viewModel.getPopularCities()
        viewModel.popularCitiesLiveData.observe(this, Observer {
            popularAdapter.operation(it)
        })

        binding.btnSaveDestination.setOnClickListener{
            setResult(REQCODE_DESTINATION_SELECT_CITY, Intent().apply {
                putExtra("City", selectedCityStates)
                putExtra(CityType, viewModel.cityType)
            })
            finish()
        }
         if(viewModel.haveOldDestinations) {
             if (selectedCityStates.size > 0) {
                 for (item in selectedCityStates) {
                     selectedAdapter.operation(SearchDataItem(item), DataRVAdapterOperationType.Add)
                 }
                 adapter.notifyDataSetChanged()
                 selectedAdapter.notifyDataSetChanged()
                 popularAdapter.notifyDataSetChanged()
                 binding.btnSaveDestination.isEnabled = true
             }
         }else{
             selectedCityStates.clear()
         }
    }

    override fun handleAction(
        actionId: String,
        item: BaseCityStateRVAdapterItem<*>) {
        when(actionId){
            CitySelected -> {
                val data = item.data as CityModel
                    if(selectedCityStates.contains(data)){
                    selectedCityStates.remove(data)
                        selectedAdapter.operation(item,DataRVAdapterOperationType.Remove)
                    }else{
                        selectedAdapter.operation(item,DataRVAdapterOperationType.Add)
                        selectedCityStates.add(data)
                    }
                    adapter.notifyDataSetChanged()
                    selectedAdapter.notifyDataSetChanged()
                    popularAdapter.notifyDataSetChanged()
                binding.btnSaveDestination.isEnabled = selectedCityStates.size>0
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
        adapter.clearItems()
        viewModel.searchCity(viewModel.searchText)
    }

}


private const val FromDialog = "from_dialog"
const val CityType = "city_type"
const val SelectedData = "selected_data"
const val HaveOldDestinations = "have_old_destinations"



/**
 * Search City Intent
 */
fun searchCityIntent(
    context: Context
) = Intent( context, SearchCityStateActivity::class.java)
var selectedCityStates = ArrayList<CityModel>()

