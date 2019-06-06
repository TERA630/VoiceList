package com.example.voicelist

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

}

class RegexTest {
    val sampleData = listOf(
        "have:origin,had,had",
        "two:origin,give,gave,given",
        "three:origin,fire,blizzard,thunder",
        "four:origin,fire,fira,figa",
        "five"
    )

    @Test
    fun testOriginRegEx() {
        val titleRegex = "^(.+):origin,.*".toRegex()
        val result = mutableListOf<String>()
        for (i in sampleData.indices) {
            val matchResult = titleRegex.matchEntire(sampleData[i])
            matchResult?.destructured?.let { (header) ->
                result.add(header)
            }
        }
    }


}
