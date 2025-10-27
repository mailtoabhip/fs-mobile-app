package com.delhivery.axle.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.delhivery.axle.R
import com.delhivery.axle.api.response.TruckResponseArray
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.databinding.DialogBottomAddTruckBinding
import com.delhivery.axle.ui.searchCity.searchCityIntent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.util.Log
import com.delhivery.axle.ui.trucks.TruckSizeAdapter
import com.delhivery.axle.ui.trucks.TruckViewModel
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.getSerializable
import com.delhivery.axle.utils.prefs.UserPrefs
import dagger.android.support.DaggerAppCompatActivity
import java.util.regex.Pattern

class AddTruckBottomSheetDialog(
    private val context: Context,
    private val viewModelFactory: ViewModelProvider.Factory,
    private val userPrefs: UserPrefs,
    private val autoCompleteUtils: AutoCompleteUtils,
    private val onTruckAdded: (String) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogBottomAddTruckBinding
    private lateinit var viewModel: TruckViewModel
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val city = intent.getStringExtra("city_type")
            val data = intent.getSerializable("City", CityModel::class.java)
            if (city != null && data != null) {
                if (city == "origin") {
                    viewModel.truckCity = data
                    binding.textCurrentCity.text = data.cityName().trim()
                } else if (city == "destination") {
                    viewModel.truckDestination = data
                    binding.textUnloadingDestination.text = data.cityName().trim()
                }
            }
        }
    }

    private var truckItems = mutableListOf<TruckResponseArray>()
    private var capacityArr = mutableListOf<String>()
    private var sourcedAs: String = ""
    private val adapter: TruckSizeAdapter by lazy { TruckSizeAdapter() }

    companion object {
        private const val REQCODE_SELECT_CITY = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DialogBottomAddTruckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(context as DaggerAppCompatActivity, viewModelFactory).get(TruckViewModel::class.java)

        setupDialog()
        setupClickListeners()
        setupObservers()
        fetchTruckTypes()

        // Register broadcast receiver for city selection
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver, IntentFilter("get_selected_city"))
    }

    private fun setupDialog() {
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setGravity(Gravity.BOTTOM)

        // Add progress bar setup
        binding.progressBar.visibility = View.GONE
    }

    private fun setupClickListeners() {
        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.editCurrentCity.setOnClickListener {
            context.startActivity(searchCityIntent(context, "origin", true))
        }

        binding.editUnloadingDestination.setOnClickListener {
            context.startActivity(searchCityIntent(context, "destination", true))
        }

        binding.editTruckSize.setOnClickListener {
            if (truckItems.isNotEmpty()) {
                val type = when {
                    binding.btnRadioContainer.isChecked -> "closed"
                    binding.btnRadioOpen.isChecked -> "open"
                    binding.btnRadioTrailer.isChecked -> "trailer"
                    else -> ""
                }
                if (type != "") {
                    showTruckSizeDialog(type)
                } else {
                    Toast.makeText(context, "Select body type first", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "No Truck Types Found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.editTruckCapacity.setOnClickListener {
            if (binding.textTruckSize.text.isNotEmpty()) {
                showTruckCapacityDialog()
            } else {
                Toast.makeText(context, "Select Truck Size First", Toast.LENGTH_SHORT).show()
            }
        }

        binding.bodyGroup.setOnCheckedChangeListener { radioGroup, i ->
            binding.textTruckSize.text = ""
            binding.textTruckCapacity.text = ""
            sourcedAs = ""
        }

        binding.btnAddTruck.setOnClickListener {
            validateFieldsAndAddTruck()
        }
    }

    private fun setupObservers() {
        viewModel.truckGetLiveData.observe(context as DaggerAppCompatActivity, Observer {
            if (it != null) {
                truckItems.addAll(it)
            }
        })

        viewModel.addTruckLiveData.observe(context as DaggerAppCompatActivity, Observer {
            if (it == true) {
                hideProgress()
                // Toast.makeText(context, "Truck added successfully", Toast.LENGTH_SHORT).show()
                onTruckAdded(viewModel.truckNumber)
                //
                dismiss()
            } else if (it == false) {
                hideProgress()
                Toast.makeText(context, "Failed to add truck", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchTruckTypes() {
        viewModel.fetchTruckType()
    }

    private fun showProgress(message: String = "Adding truck...") {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnAddTruck.isEnabled = false
        binding.btnAddTruck.text = "Add Truck"
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
        binding.btnAddTruck.isEnabled = true
        binding.btnAddTruck.text = "Add Truck"
    }

    private fun validateFieldsAndAddTruck() {
        viewModel.truckOwnership = if (binding.btnRadioMarketTruck.isChecked) "market_truck" else "owns_truck"
        viewModel.truckCapacity = if (binding.textTruckCapacity.text.isNotEmpty())
            binding.textTruckCapacity.text.toString().split("\\s+".toRegex())[0].toDouble() else 0.0
        viewModel.truckSize = if (binding.textTruckSize.text.isNotEmpty()) binding.textTruckSize.text.toString() else ""
        viewModel.truckNumber = if (binding.editTruckNumber.text != null && binding.editTruckNumber.text.toString().isNotEmpty())
            binding.editTruckNumber.text.toString() else ""
        viewModel.truckType = when {
            binding.btnRadioContainer.isChecked -> "closed"
            binding.btnRadioOpen.isChecked -> "open"
            binding.btnRadioTrailer.isChecked -> "trailer"
            else -> ""
        }
        viewModel.truckPrice = if (binding.editPriceAddTruck.text != null && binding.editPriceAddTruck.text.toString().isNotEmpty())
            binding.editPriceAddTruck.text.toString().toInt().toDouble() else 0.0

        var flag = true

        if (viewModel.truckType.isEmpty()) {
            flag = false
            Toast.makeText(context, "Select body type", Toast.LENGTH_SHORT).show()
        }

        if (viewModel.truckCapacity != 0.0) {
            binding.capacityError.visibility = View.GONE
        } else {
            binding.capacityError.text = "This field is required"
            binding.capacityError.visibility = View.VISIBLE
            flag = false
        }

        if (viewModel.truckNumber.isNotEmpty() && validateTruckNumber(viewModel.truckNumber)) {
            binding.numberError.visibility = View.GONE
        } else if (viewModel.truckNumber.isNotEmpty() && !validateTruckNumber(viewModel.truckNumber)) {
            binding.numberError.text = "Invalid vehicle number"
            binding.numberError.visibility = View.VISIBLE
            flag = false
        } else {
            binding.numberError.text = "This field is required"
            binding.numberError.visibility = View.VISIBLE
            flag = false
        }

        if (viewModel.truckSize.isNotEmpty()) {
            binding.sizeError.visibility = View.GONE
        } else {
            binding.sizeError.text = "This field is required"
            binding.sizeError.visibility = View.VISIBLE
            flag = false
        }

        if (viewModel.truckCity != null) {
            binding.originError.visibility = View.GONE
        } else {
            binding.originError.text = "This field is required"
            binding.originError.visibility = View.VISIBLE
            flag = false
        }

        if (viewModel.truckDestination != null) {
            binding.destinationError.visibility = View.GONE
        } else {
            binding.destinationError.text = "This field is required"
            binding.destinationError.visibility = View.VISIBLE
            flag = false
        }

        if (flag) {
            showProgress("Adding truck...")
            viewModel.addNewTruck(sourcedAs.uppercase())
        }
    }

    private fun validateTruckNumber(number: String): Boolean {
        val pattern = Pattern.compile(
            "[a-zA-Z]{2}((([0-9]{1,2}|[1-9]{1}[0-9]{1})[a-zA-Z]{1,3})|(0[1-9]{1}|[1-9]{1}[0-9]{1}))[0-9]{4}\$|^[a-zA-Z]{3}[0-9]{4}"
        )
        return pattern.matcher(number).matches()
    }

    private fun showTruckSizeDialog(type: String) {
        val dialog = Dialog(context)
        val bindingDialog = com.delhivery.axle.databinding.DialogBottomTruckValueBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.selectText.text = "Select Truck Size"
        bindingDialog.closeBtn.setOnClickListener { dialog.dismiss() }

        val truckSizeList = mutableListOf<TruckResponseArray>()
        for (truck in truckItems.sortedByDescending { it.truckUuid }.reversed().sortedBy { it.defaultMG }) {
            if (truck.truckType == type) {
                truckSizeList.add(truck)
            }
        }

        adapter.setItems(truckSizeList)
        bindingDialog.truckList.adapter = adapter
        bindingDialog.truckList.setOnItemClickListener { parent, view, position, id ->
            binding.textTruckSize.text = adapter.getItem(position).truckUuid
            sourcedAs = adapter.getItem(position).sourcedAs ?: ""
            val min = adapter.getItem(position).minCapacity
            val max = adapter.getItem(position).maxCapacity
            viewModel.truckCapacity = 0.0
            binding.textTruckCapacity.text = ""
            capacityArr.clear()
            if (min != null && max != null) {
                var currentMin = min
                while (currentMin <= max) {
                    capacityArr.add("$currentMin MT")
                    currentMin += 1.0
                }
            }
            dialog.dismiss()
        }

        adapter.notifyDataSetChanged()
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun showTruckCapacityDialog() {
        val dialog = Dialog(context)
        val bindingDialog = com.delhivery.axle.databinding.DialogBottomTruckValueBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.selectText.text = "Select Truck Capacity"
        bindingDialog.closeBtn.setOnClickListener { dialog.dismiss() }

        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, capacityArr)
        bindingDialog.truckList.adapter = adapter
        bindingDialog.truckList.setOnItemClickListener { parent, view, position, id ->
            binding.textTruckCapacity.text = adapter.getItem(position)
            dialog.dismiss()
        }
        adapter.notifyDataSetChanged()

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver)
    }
}
