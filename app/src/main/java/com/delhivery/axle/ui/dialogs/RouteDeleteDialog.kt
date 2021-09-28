package com.delhivery.axle.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import com.delhivery.axle.data.home.routes.RouteModel
import com.delhivery.axle.databinding.DialogDeleteRouteBinding
import javax.inject.Inject

class RouteDeleteDialog @Inject constructor(
        context :Context,
        private val route: RouteModel,
        private val dialogInterface: RouteDeleteDialogInterface

) :AlertDialog(context){
    private lateinit var binding : DialogDeleteRouteBinding
    val states : MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )

        setCancelable(true)

        for( i in route.destinations){
            states.add(i.state.capitalizeWords())
        }

        /* dialog binding */
        binding = DialogDeleteRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            textFrom.text  = route.origin.city
            textTo.text = states.joinToString (", "){it}
        }

        binding.btnConfirmDelete.setOnClickListener {
            dialogInterface.deleteRoute(route)
            dismiss()
        }

        binding.btnCancelDelete.setOnClickListener { dismiss() }

    }

    fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalize() }
}
interface RouteDeleteDialogInterface{
    fun deleteRoute(route: RouteModel)
}