package com.wiseowl.splitride.feature.rideintent.util

import org.springframework.stereotype.Component
import kotlin.math.max

@Component
class KeywordMatcher(
    private val areaNormalizer: AreaNormalizer,
    private val keywordExtractor: KeywordExtractor
) {

    fun getScore(input: String, keywords: Set<String>): Float {
        val normalizedInput = areaNormalizer.normalize(input)
        val inputKeywords = keywordExtractor.extractKeywords(normalizedInput)
        return inputKeywords.count { keywords.contains(it) }.toFloat()/max(inputKeywords.size, keywords.size).toFloat()
    }
}