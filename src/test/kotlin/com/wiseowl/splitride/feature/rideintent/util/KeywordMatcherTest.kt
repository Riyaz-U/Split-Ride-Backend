package com.wiseowl.splitride.feature.rideintent.util

import org.springframework.util.Assert
import kotlin.test.Test


class KeywordMatcherTest {
    val matcher = KeywordMatcher(AreaNormalizer(), KeywordExtractor())

    @Test
    fun `match keyword`() {
        val input = "Dlf cyber park"
        val availableKeywords = setOf(
            "Dlf",
            "cyber",
            "cyber pk",
            "shalimarbagh"
        )
         val score = matcher.getScore(input, availableKeywords)
        Assert.isTrue(score == 0.25f, { "Score $score should be 0.25" })
    }
}