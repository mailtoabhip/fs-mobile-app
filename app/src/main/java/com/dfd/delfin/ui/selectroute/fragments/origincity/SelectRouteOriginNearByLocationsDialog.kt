package com.dfd.delfin.ui.selectroute.fragments.origincity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.dfd.delfin.data.CityModel
import com.dfd.delfin.databinding.DialogSelectRouteNearByLocationsBinding
import com.dfd.delfin.databinding.ViewSelectNearbyCityItemBinding

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