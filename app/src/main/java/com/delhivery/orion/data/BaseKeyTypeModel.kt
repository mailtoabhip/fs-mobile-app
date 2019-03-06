package com.delhivery.orion.data

import android.arch.persistence.room.Ignore

/**
 * Base Key-Type model contains a key for unique identification
 *
 * @param key Model key, can be [Any] type defined by [KT]
 */
abstract class BaseKeyTypeModel<KT : Any> {
  @Ignore
  abstract fun key(): KT
}