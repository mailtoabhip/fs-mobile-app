package com.delhivery.orion.ui.selectroute.fragments.origincity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.databinding.DialogSelectRouteNearByLocationsBinding
import com.delhivery.orion.databinding.ViewSelectNearbyCityItemBinding

class SelectRouteOriginNearByLocationsDialog(
  context: Context,
  private val selectedLocation: CityModel,
  private val locations: List<CityModel>,
  private val dialogInterface: SelectRouteOriginNearByLocationsDialogInterface
) : AlertDialog(context) {

  /* dialog binding */
  private lateinit var binding: DialogSelectRouteNearByLocationsBinding

  /* selected cities list */
  private var selectedCities: MutableSet<CityModel> = mutableSetOf()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setCancelable(false)

    /* dialog binding */
    binding = DialogSelectRouteNearByLocationsBinding.inflate(layoutInflater)
    setContentView(binding.root)

    /* add locations */
    addLocationChecks()

    binding.btnConfirm.setOnClickListener {
      dialogInterface.nearByLocationsSelected(
          selectedLocation, selectedCities.toList()
      )
      dismiss()
    }
  }

  /**
   * Add location check boxes
   */
  private fun addLocationChecks() {
    binding.containerCities.removeAllViews()
    locations.forEach {
      val itemBinding = ViewSelectNearbyCityItemBinding.inflate(
          layoutInflater, binding.containerCities, false
      )

      itemBinding.city = it
      selectedCities.add(it)
      itemBinding.check.isChecked = true
      itemBinding.check.setOnCheckedChangeListener { _, checked ->
        if (checked) {
          selectedCities.add(it)
        } else {
          selectedCities.remove(it)
        }
      }
      binding.containerCities.addView(itemBinding.root)
    }
  }
}

/**
 * Dialog interface
 */
interface SelectRouteOriginNearByLocationsDialogInterface {
  fun nearByLocationsSelected(
    selectedLocation: CityModel,
    locations: List<CityModel>
  )
}