package com.wiseowl.splitride.feature.rideintent.util

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class KeywordExtractorTest {

    val keywordExtractor = KeywordExtractor()

    @Test
    fun `extracts all keywords`(){
        val keywords = arrayListOf(
            "some",
            "area",
            "i",
            "want",
            "to",
            "go"
        )
        val input = keywords.joinToString(" ")

        val extractedKeywords = keywordExtractor.extractKeywords(input)
        assertEquals(keywords.size, extractedKeywords.size)
    }

    @Test
    fun `extractKeywords should remove special characters`() {
        val input = "Phase-2@Park#CyberCity!!"
        val result = keywordExtractor.extractKeywords(input)

        assertEquals(
            setOf("ph", "2", "pk", "cyber"),
            result
        )
    }
}