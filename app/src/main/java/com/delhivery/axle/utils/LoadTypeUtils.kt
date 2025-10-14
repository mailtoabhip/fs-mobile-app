package com.delhivery.axle.utils

import com.delhivery.axle.ui.home.fragments.placements.LoadTypes

object LoadTypeUtils {

    fun getLoadType(loadTypes: String): String {
        return when (loadTypes) {
            LoadTypes.ftlAdhoc.name -> "ftl_adhoc"
            LoadTypes.ftlRegular.name -> "ftl_regular"
            LoadTypes.intracityAdhoc.name -> "intracity_adhoc"
            LoadTypes.intracityRegular.name -> "intracity_regular"
            LoadTypes.orionFixed.name -> "orion_fixed"
            LoadTypes.orionSpot.name -> "orion_spot"
            else -> ""
        }
    }


    fun isContractCodeRequired(loadTypes: String): Boolean {
        return when (loadTypes) {
            LoadTypes.ftlRegular.name -> true
            LoadTypes.intracityAdhoc.name -> true
            LoadTypes.intracityRegular.name -> true
            else -> false
        }
    }

    fun isTransactionIdRequired(loadTypes: String): Boolean {
        return when (loadTypes) {
            LoadTypes.ftlAdhoc.name -> true
            LoadTypes.orionSpot.name -> true
            LoadTypes.orionFixed.name -> true
            else -> false
        }

    }

}