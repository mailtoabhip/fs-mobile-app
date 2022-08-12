package com.delhivery.axle.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.delhivery.axle.database.entity.OffersEntity

@Dao
interface OffersDao {
  @Query("SELECT * FROM offers ORDER BY ID LIMIT 500 OFFSET :offset")
  fun getAllOffers(offset:Int?=0): LiveData<List<OffersEntity>>

  @Query("SELECT * FROM offers WHERE offer_id =:offerId")
  fun getOffers(offerId:String):LiveData<OffersEntity>

  @Query("SELECT COUNT(*) FROM offers")
  fun getOffersCount():LiveData<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun newOfferEntry(entry: OffersEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun saveoffers(listOffers:List<OffersEntity>)

  @Delete
  fun deleteOfferEntry(entry: OffersEntity)

  @Query("DELETE FROM offers")
  fun deleteOffers()
}