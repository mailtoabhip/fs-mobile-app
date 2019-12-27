package com.delhivery.axle.ui.home.fragments.pod

import com.delhivery.axle.data.home.pod.HomePodChildItemData
import com.delhivery.axle.data.home.pod.HomePodParentItemData
import java.util.Arrays

object GenreDataFactory {

  fun makeGenres(): List<HomePodParentItemData> {
    return listOf(
        makeRockGenre(),
        makeJazzGenre(),
        makeClassicGenre(),
        makeSalsaGenre(),
        makeBluegrassGenre()
    )
  }

  fun makeRockGenre(): HomePodParentItemData {
    return HomePodParentItemData("Rock", makeRockArtists())
  }

  fun makeRockArtists(): List<HomePodChildItemData> {
    val queen = HomePodChildItemData("Queen")
    val styx = HomePodChildItemData("Styx")
    val reoSpeedwagon = HomePodChildItemData("REO Speedwagon")
    val boston = HomePodChildItemData("Boston")

    return listOf(queen, styx, reoSpeedwagon, boston)
  }

  fun makeJazzGenre(): HomePodParentItemData {
    return HomePodParentItemData("Jazz", makeJazzArtists())
  }

  fun makeJazzArtists(): List<HomePodChildItemData> {
    val milesDavis = HomePodChildItemData("Miles Davis")
    val ellaFitzgerald = HomePodChildItemData("Ella Fitzgerald")
    val billieHoliday = HomePodChildItemData("Billie Holiday")

    return listOf(milesDavis, ellaFitzgerald, billieHoliday)
  }

  fun makeClassicGenre(): HomePodParentItemData {
    return HomePodParentItemData("Classic", makeClassicArtists())
  }

  fun makeClassicArtists(): List<HomePodChildItemData> {
    val beethoven = HomePodChildItemData("Ludwig van Beethoven")
    val bach = HomePodChildItemData("Johann Sebastian Bach")
    val brahms = HomePodChildItemData("Johannes Brahms")
    val puccini = HomePodChildItemData("Giacomo Puccini")

    return listOf(beethoven, bach, brahms, puccini)
  }

  fun makeSalsaGenre(): HomePodParentItemData {
    return HomePodParentItemData("Salsa", makeSalsaArtists())
  }

  fun makeSalsaArtists(): List<HomePodChildItemData> {
    val hectorLavoe = HomePodChildItemData("Hector Lavoe")
    val celiaCruz = HomePodChildItemData("Celia Cruz")
    val willieColon = HomePodChildItemData("Willie Colon")
    val marcAnthony = HomePodChildItemData("Marc Anthony")

    return Arrays.asList(hectorLavoe, celiaCruz, willieColon, marcAnthony)
  }

  fun makeBluegrassGenre(): HomePodParentItemData {
    return HomePodParentItemData("Bluegrass", makeBluegrassArtists())
  }

  fun makeBluegrassArtists(): List<HomePodChildItemData> {
    val billMonroe = HomePodChildItemData("Bill Monroe")
    val earlScruggs = HomePodChildItemData("Earl Scruggs")
    val osborneBrothers = HomePodChildItemData("Osborne Brothers")
    val johnHartford = HomePodChildItemData("John Hartford")

    return listOf(billMonroe, earlScruggs, osborneBrothers, johnHartford)
  }
}

