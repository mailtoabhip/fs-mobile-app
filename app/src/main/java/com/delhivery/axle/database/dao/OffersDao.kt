package com.delhivery.axle.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.delhivery.axle.api.request.UpdatePriceRequest
import com.delhivery.axle.database.entity.OffersEntity

@Dao
interface OffersDao {
  @Query("SELECT * FROM offers")
  fun getAllOffers(): LiveData<List<OffersEntity>>

  @Query("SELECT * FROM offers WHERE offer_id =:offerId")
  fun getOffers(offerId:String):LiveData<OffersEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun newOfferEntry(entry: OffersEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun saveoffers(listOffers:List<OffersEntity>)

  @Delete
  fun deleteOfferEntry(entry: OffersEntity)

  @Query("DELETE FROM offers")
  fun deleteOffers()
}