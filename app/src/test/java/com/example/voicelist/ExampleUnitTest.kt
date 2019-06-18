package com.example.voicelist

import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.experimental.theories.suppliers.TestedOn
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}

@RunWith(Theories::class)
class RegexTest {
    val sampleData = listOf(
        "one,knight,mage",
        "two,Firion,Maria,Ricard,Minwu",
        "three,Monk,White Mage,Thief,Dragoon,Summoner",
        "four,Cecil,Kain,Rydia,Rosa,Edge",
        "five,Bartz,Faris,Galuf,Lenna,Krile",
        "six,Terra,Locke,Celes,Shadow,seven",
        "seven,Cloud,Tifa,Aeris,eight",
        "eight,Squall,Rinoa,Quistis",
        "nine,Zidane,Vivi,Garnet,Freya",
        "ten"
    )
    val sampleIndexToExpected = mapOf(
        0 to listOf("knight", "mage"),
        1 to listOf("Firion", "Maria", "Ricard", "Minwu"),
        2 to listOf("Monk", "White Mage", "Thief", "Dragoon", "Summoner"),
        3 to listOf("Cecil", "Kain", "Rydia", "Rosa", "Edge"),
        4 to listOf("Bartz", "Faris", "Galuf", "Lenna", "Krile"),
        5 to listOf("Terra", "Locke", "Celes", "Shadow", "seven")
    )
    @Theory
    fun testChildList(@TestedOn(ints = [0, 1, 2, 3, 4]) int: Int) {
        val headAndChildCSV = sampleData[int]
        val list = headAndChildCSV.split(",")
        val result = list.drop(1)
        assertThat(result).isEqualTo(sampleIndexToExpected[int])
    }

    @Test
    fun deletePopTest() {
        val result = mutableListOf<String>()
        sampleData.forEachIndexed { index, value ->
            val itemToDelete = StringBuilder("$index:")
                .append(value)
                .toString()
            result.add(itemToDelete)
        }
        val expectedResult = mutableListOf(
            "0:one,knight,mage",
            "1:two,Firion,Maria,Ricard,Minwu",
            "2:three,Monk,White Mage,Thief,Dragoon,Summoner",
            "3:four,Cecil,Kain,Rydia,Rosa,Edge",
            "4:five,Bartz,Faris,Galuf,Lenna,Krile",
            "5:six,Terra,Locke,Celes,Shadow,seven",
            "6:seven,Cloud,Tifa,Aeris,eight",
            "7:eight,Squall,Rinoa,Quistis",
            "8:nine,Zidane,Vivi,Garnet,Freya",
            "9:ten"
        )
        assertThat(result).isEqualTo(expectedResult)
    }


}
