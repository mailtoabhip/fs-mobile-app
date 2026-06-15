package com.dfd.delfin.data.home.trucks

/**
 * Data class representing FASTag statistics for truck inventory
 */
data class FastagStats(
    val totalTrucks: Int,
    val fastagTrucksCount: Int,
    val totalFastagBalance: Double
)
