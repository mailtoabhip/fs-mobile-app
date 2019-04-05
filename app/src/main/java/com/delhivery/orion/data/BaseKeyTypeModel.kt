package com.delhivery.orion.data

import android.arch.persistence.room.Ignore
import com.delhivery.orion.utils.extensions.safeEquals

/**
 * Base Key-Type model contains a key for unique identification
 *
 * @param key Model key, can be [Any] type defined by [KT]
 */
abstract class BaseKeyTypeModel<KT : Any> {
  @Ignore
  abstract fun key(): KT
}

/**
 * Find item Index by id
 */
fun List<BaseKeyTypeModel<*>>.indexById(id: Any): Int {
  var itemIndex = -1
  forEachIndexed { i, item ->
    if (item.key().safeEquals(id)) {
      itemIndex = i
    }
  }
  return itemIndex
}