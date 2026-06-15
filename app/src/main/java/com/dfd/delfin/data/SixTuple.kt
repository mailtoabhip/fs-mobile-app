package com.dfd.delfin.data
/**
 * Data class for tuple with six elements
 */
data class SixTuple<out A, out B, out C, out D, out E,  out F>(
  val first: A,
  val second: B,
  val third: C,
  val fourth: D,
  val fifth: E,
  val sixth: F

) {

  /**
   * Returns string representation of the [SixTuple] including its [first], [second], [third], [fourth], [fifth] and [sixth]values.
   */
  override fun toString(): String = "($first, $second, $third, $fourth, $fifth, $sixth)"
}
