package com.delhivery.axle.data

/**
 * Data class for tuple with seven elements
 */
data class Septuple<out A, out B, out C, out D, out E, out F, out G>(
  val first: A,
  val second: B,
  val third: C,
  val fourth: D,
  val fifth: E,
  val sixth: F,
  val seventh: G
) {

  /**
   * Returns string representation of the [Septuple] including all seven values.
   */
  override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth, $seventh)"
}

