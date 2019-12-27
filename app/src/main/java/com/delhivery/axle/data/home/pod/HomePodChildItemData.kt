package com.delhivery.axle.data.home.pod

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.delhivery.axle.data.BaseKeyTypeModel

class HomePodChildItemData(
  val name: String
) : BaseKeyTypeModel<String>(), Parcelable {

  constructor(parcel: Parcel) : this(parcel.readString()!!)

  override fun key() = name
  override fun writeToParcel(
    parcel: Parcel,
    flags: Int
  ) {
    parcel.writeString(name)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Creator<HomePodChildItemData> {
    override fun createFromParcel(parcel: Parcel): HomePodChildItemData {
      return HomePodChildItemData(parcel)
    }

    override fun newArray(size: Int): Array<HomePodChildItemData?> {
      return arrayOfNulls(size)
    }
  }
}

const val HomePodChildAction = "pod_child_action"