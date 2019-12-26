package com.delhivery.axle.utils.expandablerecyclerview.models

import android.widget.ExpandableListView
import java.util.ArrayList

/**
 * Exact copy of android.widget.ExpandableListPosition because
 * android.widget.ExpandableListPosition has package local scope
 *
 *
 * ExpandableListPosition can refer to either a group's position or a child's
 * position. Referring to a child's position requires both a group position (the
 * group containing the child) and a child position (the child's position within
 * that group). To create objects, use [.obtainChildPosition] or
 * [.obtainGroupPosition].
 */

class ExpandableListPosition private constructor() {

  /**
   * The position of either the group being referred to, or the parent
   * group of the child being referred to
   */
  var groupPos: Int = 0

  /**
   * The position of the child within its parent group
   */
  var childPos: Int = 0

  /**
   * The position of the item in the flat list (optional, used internally when
   * the corresponding flat list position for the group or child is known)
   */
  internal var flatListPos: Int = 0

  /**
   * What type of position this ExpandableListPosition represents
   */
  var type: Int = 0

  val packedPosition: Long
    get() = if (type == CHILD) {
      ExpandableListView.getPackedPositionForChild(groupPos, childPos)
    } else {
      ExpandableListView.getPackedPositionForGroup(groupPos)
    }

  private fun resetState() {
    groupPos = 0
    childPos = 0
    flatListPos = 0
    type = 0
  }

  /**
   * Do not call this unless you obtained this via ExpandableListPosition.obtain().
   * PositionMetadata will handle recycling its own children.
   */
  fun recycle() {
    synchronized(sPool) {
      if (sPool.size < MAX_POOL_SIZE) {
        sPool.add(this)
      }
    }
  }

  override fun equals(o: Any?): Boolean {
    if (this === o) return true
    if (o == null || javaClass != o.javaClass) return false

    val that = o as ExpandableListPosition?

    if (groupPos != that!!.groupPos) return false
    if (childPos != that.childPos) return false
    return if (flatListPos != that.flatListPos) false else type == that.type

  }

  override fun hashCode(): Int {
    var result = groupPos
    result = 31 * result + childPos
    result = 31 * result + flatListPos
    result = 31 * result + type
    return result
  }

  override fun toString(): String {
    return "ExpandableListPosition{" +
        "groupPos=" + groupPos +
        ", childPos=" + childPos +
        ", flatListPos=" + flatListPos +
        ", type=" + type +
        '}'.toString()
  }

  companion object {

    private val MAX_POOL_SIZE = 5
    private val sPool = ArrayList<ExpandableListPosition>(MAX_POOL_SIZE)

    /**
     * This data type represents a child position
     */
    val CHILD = 1

    /**
     * This data type represents a group position
     */
    val GROUP = 2

    internal fun obtainGroupPosition(groupPosition: Int): ExpandableListPosition {
      return obtain(GROUP, groupPosition, 0, 0)
    }

    internal fun obtainChildPosition(
      groupPosition: Int,
      childPosition: Int
    ): ExpandableListPosition {
      return obtain(CHILD, groupPosition, childPosition, 0)
    }

    internal fun obtainPosition(packedPosition: Long): ExpandableListPosition? {
      if (packedPosition == ExpandableListView.PACKED_POSITION_VALUE_NULL) {
        return null
      }

      val elp = recycledOrCreate
      elp.groupPos = ExpandableListView.getPackedPositionGroup(packedPosition)
      if (ExpandableListView.getPackedPositionType(
              packedPosition
          ) == ExpandableListView.PACKED_POSITION_TYPE_CHILD
      ) {
        elp.type = CHILD
        elp.childPos = ExpandableListView.getPackedPositionChild(packedPosition)
      } else {
        elp.type = GROUP
      }
      return elp
    }

    fun obtain(
      type: Int,
      groupPos: Int,
      childPos: Int,
      flatListPos: Int
    ): ExpandableListPosition {
      val elp = recycledOrCreate
      elp.type = type
      elp.groupPos = groupPos
      elp.childPos = childPos
      elp.flatListPos = flatListPos
      return elp
    }

    private val recycledOrCreate: ExpandableListPosition
      get() {
        val elp: ExpandableListPosition
        synchronized(sPool) {
          if (sPool.size > 0) {
            elp = sPool.removeAt(0)
          } else {
            return ExpandableListPosition()
          }
        }
        elp.resetState()
        return elp
      }
  }
}

