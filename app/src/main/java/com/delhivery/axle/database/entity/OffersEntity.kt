package com.delhivery.axle.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.delhivery.axle.data.CityModel

@Entity(tableName = "offers")
data class OffersEntity(
    @ColumnInfo(name = "origin_city_code") var occ: String?,
    @ColumnInfo(name = "origin_city") var oc: String?,
    @ColumnInfo(name = "destination_city_code") var dcc: String? ,
    @ColumnInfo(name = "destination_city") var dc: String?,
    @ColumnInfo(name = "truck_display_name") var tdn: String?,
    @ColumnInfo(name = "start_date") var sd: String?,
    @ColumnInfo(name = "end_date") var ed: String?,
    @ColumnInfo(name = "amount") var amt: Double?,
    @ColumnInfo(name = "status") var status: String?,
    @ColumnInfo(name = "offer_type") var offerType: String?,
    @ColumnInfo(name = "offer_id") var offerId: String?,
    @PrimaryKey(autoGenerate = true) var id: Long? = null
)
