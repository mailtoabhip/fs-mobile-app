package com.delhivery.axle.ui.home.fragments.trucks

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.delhivery.axle.R
import com.delhivery.axle.data.home.trucks.*
import com.delhivery.axle.databinding.DialogBottomTruckDeactivateBinding
import com.delhivery.axle.databinding.DialogBottomTruckOptionsBinding
import com.delhivery.axle.databinding.FragmentHomeTrucksBinding
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsRVAdapter
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckBaseFragment
import com.delhivery.axle.ui.home.fragments.loads_truck.HomeLoadsTruckFragment
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class HomeTrucksFragment : HomeLoadsTruckBaseFragment<FragmentHomeTrucksBinding, HomeTrucksViewModel>(),
        HomeTrucksRVAdapterInterface
{
    override fun getViewModelClass() = HomeTrucksViewModel::class.java
    override fun layoutId() = R.layout.fragment_home_trucks

    companion object {
        /* singleton instance */
        val _instance: HomeTrucksFragment by lazy { HomeTrucksFragment() }
    }

    /* RV adapter */
    private val adapter: HomeTrucksRVAdapter by lazy {
        HomeTrucksRVAdapter(this)
    }

    @Inject lateinit var dialogUtils: DialogUtils
    @Inject lateinit var fcmUtils: FCMUtils
    @Inject lateinit var userPrefs: UserPrefs

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun refreshData() {
        adapter.resetStaticData()

    }

    override fun handleAction(actionId: String, item: BaseHomeTrucksRVAdapterItem<*>) {
        when(actionId){
            HomeTrucksVehicleFilterAction -> {
                showVehicleFilterDialog()
            }

            HomeTrucksAvailabilityFilterAction->{
                showAvailabilityFilterDialog()
            }

            HomeTrucksSizeFilterAction -> {
                showSizeFilterDialog()
            }
        }
    }


    override fun handleAction(
        actionId: String,
        item: BaseHomeTrucksRVAdapterItem<*>,
        position: Int
    ) {
        //handle action here
        when(actionId){
            HomeTrucksRequestAction_EditTruck ->{
                showOptionsDialog(item.data as HomeTrucksRequestItemData , position)
            }

            HomeTrucksRequestAction_ActivateTruck -> {
                analyticsUtil.trackEvent(
                    EVENT_ACTIVATE_TRUCK,
                    mutableListOf(PROPERTY_USER_ID),
                    mutableListOf(userPrefs.userId())
                )
            }
        }
    }

    private fun showOptionsDialog(homeTrucksRequestItemData: HomeTrucksRequestItemData, position: Int) {
        val dialog = Dialog(context!!)
        val bindingDialog= DialogBottomTruckOptionsBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.editTruckLayout.setOnClickListener {

        }
        bindingDialog.deactivateTruckLayout.setOnClickListener {
            showDeactivateDialog()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)

    }

    private fun showDeactivateDialog() {
        val dialog = Dialog(context!!)
        val bindingDialogDeactivate= DialogBottomTruckDeactivateBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialogDeactivate.root)

        bindingDialogDeactivate.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialogDeactivate.btnDeactivate.setOnClickListener {
            var reason: String = ""
            if (bindingDialogDeactivate.otherSource.isChecked){
                reason = bindingDialogDeactivate.otherSource.text.toString()
            }
             else if( bindingDialogDeactivate.other.isChecked) {
                 reason = bindingDialogDeactivate.other.text.toString()
            }

            analyticsUtil.trackEvent(
                EVENT_DEACTIVATE_TRUCK,
                mutableListOf(PROPERTY_USER_ID),
                mutableListOf(userPrefs.userId())
            )

            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    private fun showSizeFilterDialog() {
        lateinit var dialog: AlertDialog

        // Initialize an array of vehicles
        val arrayVehicle = arrayOf("open","closed","trailer","all")

        val arrayChecked = booleanArrayOf(false,false,false,false)

        val builder = AlertDialog.Builder(context)

        builder.setTitle("-- Select Size --")

        builder.setMultiChoiceItems(arrayVehicle, arrayChecked) { _, which, isChecked ->
            arrayChecked[which] = isChecked
        }

        builder.setPositiveButton("Filter") { _, _ ->

        }

        builder.setNegativeButton("Cancel") {_, _ ->
            dialog.dismiss()
        }

        dialog = builder.create()
        dialog.show()
    }

    private fun showAvailabilityFilterDialog() {
        lateinit var dialog: AlertDialog

        // Initialize an array of vehicles
        val arrayVehicle = arrayOf("Available","Not Available")

        val arrayChecked = booleanArrayOf(false,false)

        val builder = AlertDialog.Builder(context)

        builder.setTitle("-- Select Availability --")

        builder.setMultiChoiceItems(arrayVehicle, arrayChecked) { _, which, isChecked ->
            arrayChecked[which] = isChecked
        }

        builder.setPositiveButton("Filter") { _, _ ->

        }

        builder.setNegativeButton("Cancel") {_, _ ->
            dialog.dismiss()
        }

        dialog = builder.create()
        dialog.show()
    }

    private fun showVehicleFilterDialog() {
        lateinit var dialog: AlertDialog

        // Initialize an array of vehicles
        val arrayVehicle = arrayOf("open","closed","trailer","all")

        val arrayChecked = booleanArrayOf(false,false,false,false)

        val builder = AlertDialog.Builder(context)

        builder.setTitle("-- Select vehicle types --")

        builder.setMultiChoiceItems(arrayVehicle, arrayChecked) { _, which, isChecked ->
            arrayChecked[which] = isChecked
        }

        builder.setPositiveButton("Filter") { _, _ ->

        }

        builder.setNegativeButton("Cancel") {_, _ ->
            dialog.dismiss()
        }

        dialog = builder.create()
        dialog.show()
    }


}
