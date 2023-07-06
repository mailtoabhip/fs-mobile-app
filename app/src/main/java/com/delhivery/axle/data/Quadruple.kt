package com.delhivery.axle.data

/**
 * Holder for four items
 */
data class Quadruple<out A, out B, out C, out D>(
  val first: A,
  val second: B,
  val third: C,
  val fourth: D
) {

  /**
 * Returns string representation of the [Quadruple] including its [first], [second], [third] and [fourth] values.
   */
  override fun toString(): String = "($first, $second, $third, $fourth)"
}

/**
 * Converts this Quadruple into a list.
 */
fun <T> Quadruple<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)