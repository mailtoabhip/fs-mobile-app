package com.dfd.delfin.data

import androidx.room.Ignore
import com.dfd.delfin.utils.extensions.safeEquals
import java.io.Serializable

/**
 * Base Key-Type model contains a key for unique identification
 *
 * @param key Model key, can be [Any] type defined by [KT]
 */
abstract class BaseKeyTypeModel<KT : Any> : Serializable {
  @Ignore
  abstract fun key(): KT

  /**
   * Filter model
   */
  @Ignore
  open fun filter(query: String) = key().safeEquals(query)
}

/**
 * Find item Index by id
 */
fun List<BaseKeyTypeModel<*>>.indexById(id: Any): Int {
  var itemIndex = -1
  forEachIndexed { i, item ->
    if (item.key().safeEquals(id)) {
      itemIndex = i
      return@forEachIndexed
    }
  }
  return itemIndex
}

/**
 * Filter by id
 */
fun List<BaseKeyTypeModel<*>>.itemById(id: Any) = filter {
  it.key()
      .safeEquals(id)
}.firstOrNull()