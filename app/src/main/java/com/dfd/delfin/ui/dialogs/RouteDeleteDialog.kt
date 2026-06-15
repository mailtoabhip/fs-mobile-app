package com.dfd.delfin.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import com.dfd.delfin.data.home.routes.RouteModel
import com.dfd.delfin.databinding.DialogDeleteRouteBinding
import com.dfd.delfin.utils.UiUtils
import javax.inject.Inject

class RouteDeleteDialog @Inject constructor(
        context :Context,
        private val route: RouteModel,
        private val dialogInterface: RouteDeleteDialogInterface,
        private val uiUtils: UiUtils

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
            uiUtils.showProgress("Updating Routes")
        }

        binding.btnCancelDelete.setOnClickListener { dismiss() }

    }

    fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalize() }
}
interface RouteDeleteDialogInterface{
    fun deleteRoute(route: RouteModel)
}