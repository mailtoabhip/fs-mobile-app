package com.delhivery.axle.utils.expandablerecyclerview.models

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.util.ArrayList

/**
 * The backing data object for an [ExpandableGroup]
 */
open class ExpandableGroup<T : Parcelable> : Parcelable {
  var title: String? = null
    private set
  var items: List<T>? = null
    private set

  val itemCount: Int
    get() = if (items == null) 0 else items!!.size

  constructor(
    title: String,
    items: List<T>
  ) {
    this.title = title
    this.items = items
  }

  override fun toString(): String {
    return "ExpandableGroup{" +
        "title='" + title + '\''.toString() +
        ", items=" + items +
        '}'.toString()
  }

  protected constructor(`in`: Parcel) {
    title = `in`.readString()
    val hasItems = `in`.readByte()
    val size = `in`.readInt()
    if (hasItems.toInt() == 0x01) {
      items = ArrayList(size)
      val type = `in`.readSerializable() as Class<*>?
      `in`.readList(items!!, type!!.classLoader)
    } else {
      items = null
    }
  }

  override fun describeContents(): Int {
    return 0
  }

  override fun writeToParcel(
    dest: Parcel,
    flags: Int
  ) {
    dest.writeString(title)
    if (items == null) {
      dest.writeByte(0x00.toByte())
      dest.writeInt(0)
    } else {
      dest.writeByte(0x01.toByte())
      dest.writeInt(items!!.size)
      val objectsType = items!![0].javaClass
      dest.writeSerializable(objectsType)
      dest.writeList(items)
    }
  }

  companion object CREATOR : Creator<ExpandableGroup<*>> {
    override fun createFromParcel(parcel: Parcel): ExpandableGroup<*> {
      return ExpandableGroup<Parcelable>(parcel)
    }

    override fun newArray(size: Int): Array<ExpandableGroup<*>?> {
      return arrayOfNulls(size)
    }
  }

}
