package com.delhivery.axle.ui.trucks

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import com.delhivery.axle.R
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.databinding.ActivityTruckBinding
import com.delhivery.axle.databinding.DialogBottomTruckUnloadingDetailsBinding
import com.delhivery.axle.databinding.DialogBottomTruckValueBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.searchCity.searchCityIntent
import com.delhivery.axle.utils.AutoCompleteUtils
import com.delhivery.axle.utils.REQCODE_SELECT_CITY
import kotlinx.android.synthetic.main.view_home_loads_progress_item.*
import javax.inject.Inject


class TruckActivity : BaseActivity<ActivityTruckBinding, TruckViewModel>() {

    init {
        hasInlineProgress = true
    }

    override fun getViewModelClass()= TruckViewModel::class.java

    override fun layoutId() = R.layout.activity_truck

    override fun requireConnection() = true

    @Inject lateinit var autoCompleteUtils: AutoCompleteUtils



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* get intent keys */
        viewModel.truckType = intent.getStringExtra(TruckType) ?: ""
        viewModel.truckCapacity = intent.getStringExtra(TruckCapacity) ?: ""
        viewModel.truckSize = intent.getStringExtra(TruckSize) ?: ""

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        /* setup toolbar */
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Enter Truck Details"

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
        }

        binding.editTruckCapacity.setOnClickListener{
            showTruckCapacityDialog()
        }

        binding.editTruckSize.setOnClickListener {
            showTruckSizeDialog()
        }

        binding.editCurrentCity.setOnClickListener {
            startActivityForResult(searchCityIntent(this), REQCODE_SELECT_CITY)

        }
        binding.editUnloadingDestinations.setOnClickListener {
            showUnloadingLocationsDialog()
        }

        binding.btnAddTruck.setOnClickListener{
            validateFieldsAndAddTruck()
        }

    }

    private fun validateFieldsAndAddTruck() {
        viewModel.truckOwnership = if(binding.btnRadioMarketTruck.isChecked)
            "market_truck" else "owns_truck"

        viewModel.truckCapacity =  if(binding.textTruckCapacity.text.isNotEmpty())
            binding.textTruckCapacity.text.toString() else ""

        viewModel.truckSize = if(binding.textTruckSize.text.isNotEmpty()) binding.textTruckSize.text.toString() else ""

        viewModel.truckNumber = if(binding.editTruckNumber.text !=null && binding.editTruckNumber.text.toString() != "" )
            binding.editTruckNumber.text.toString() else ""

        viewModel.truckType = if(binding.btnRadioContainer.isChecked) "closed" else if(binding.btnRadioOpen.isChecked)  "open" else "trailer"

        if(viewModel.truckCapacity!= "" && viewModel.truckNumber!= "" && viewModel.truckSize!= "" && viewModel.truckCity!=null &&
            viewModel.truckDestination!= null) {

            viewModel.addNewTruck()
        }
        else{
            uiUtils.showSnackbar("Fields are Empty")
        }
    }

    private fun showUnloadingLocationsDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogBottomTruckUnloadingDetailsBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        autoCompleteUtils.autoCompleteCity(bindingDialog.editUnloadingCity){
            binding.textUnloadingDestination.text = it.city
        }

        bindingDialog.closeBtn.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)



    }

    private fun showTruckSizeDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogBottomTruckValueBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.selectText.text = getString(R.string.label_select_truck_size)

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        val listItems = mutableListOf<String>()
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        bindingDialog.truckList.adapter = adapter
        bindingDialog.truckList.setOnItemClickListener { parent, view, position, id ->
            binding.textTruckSize.text = adapter.getItem(position)
            dialog.dismiss()
        }
        listItems.add("Aakash")
        listItems.add("Aak")
        listItems.add("Ah")
        listItems.add("Aash")

        adapter.notifyDataSetChanged();

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    private fun showTruckCapacityDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogBottomTruckValueBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.selectText.text = getString(R.string.label_select_truck_capacity)

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        val listItems = mutableListOf<String>()
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        bindingDialog.truckList.adapter = adapter
        bindingDialog.truckList.setOnItemClickListener { parent, view, position, id ->
            binding.textTruckCapacity.text = adapter.getItem(position)
            dialog.dismiss()
        }
        listItems.add("Aakash")
        listItems.add("Aak")
        listItems.add("Ah")
        listItems.add("Aash")

        adapter.notifyDataSetChanged();

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQCODE_SELECT_CITY ->{
                if(data != null) {
                    val city = data.getSerializableExtra("City") as CityModel
                    viewModel.truckCity = city
                    binding.textCurrentCity.text = city.cityName().trim()
                }
            }
        }
    }


}

/* intent Keys */
private const val TruckType = "truck_type"
private const val TruckSize = "truck_size"
private const val TruckCapacity = "truck_capacity"


/**
 * Truck intent
 */
fun truckIntent(
    context: Context,
    truckType: String = "",
    truckSize: String= "",
    truckCapacity: String= ""
) = Intent(context, TruckActivity::class.java).apply {
    putExtra(TruckType, truckType)
    putExtra(TruckSize, truckSize)
    putExtra(TruckCapacity, truckCapacity)

}