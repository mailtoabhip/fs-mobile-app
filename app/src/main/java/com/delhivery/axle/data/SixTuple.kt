package com.delhivery.axle.data

data class Sixtuple<out A, out B, out C, out D, out E,  out F>(
  val first: A,
  val second: B,
  val third: C,
  val fourth: D,
  val fifth: E,
  val sixth: F

) {


  override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}
