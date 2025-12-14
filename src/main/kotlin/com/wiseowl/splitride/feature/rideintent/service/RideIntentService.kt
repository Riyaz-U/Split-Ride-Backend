package com.wiseowl.splitride.feature.rideintent.service

import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
import com.wiseowl.splitride.feature.rideintent.model.Direction
import com.wiseowl.splitride.feature.rideintent.model.RideIntent
import com.wiseowl.splitride.feature.rideintent.model.RideIntentStatus
import com.wiseowl.splitride.feature.rideintent.repository.RideIntentRepository
import com.wiseowl.splitride.feature.rideintent.util.AreaNormalizer
import com.wiseowl.splitride.feature.rideintent.util.KeywordExtractor
import com.wiseowl.splitride.feature.rideintent.util.KeywordMatcher
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import kotlin.math.abs

@Service
class RideIntentService(
    private val rideIntentRepository: RideIntentRepository,
    private val areaNormalizer: AreaNormalizer,
    private val keywordExtractor: KeywordExtractor,
    private val keywordMatcher: KeywordMatcher
) {

    fun create(req: CreateRideIntentRequestDTO): RideIntent {
        val normalizedSource = areaNormalizer.normalize(req.sourceArea)
        val normalizedDestination = areaNormalizer.normalize(req.destinationArea)
        val sourceKeyword = keywordExtractor.extractKeywords(normalizedSource).joinToString(",")
        val destinationKeyword = keywordExtractor.extractKeywords(normalizedDestination).joinToString(",")
        val intent = RideIntent(
            userId = UUID.fromString(req.userId),
            direction = req.direction,
            sourceArea = req.sourceArea,
            destinationArea = req.destinationArea,
            normalizedSource = normalizedSource,
            normalizedDestination = normalizedDestination,
            sourceKeywords = sourceKeyword,
            destinationKeywords = destinationKeyword,
            startTime = Instant.parse(req.startTime),
            flexibleMinutes = req.flexibleMinutes
        )
        return rideIntentRepository.save(intent)
    }

    fun search(
        direction: Direction,
        sourceArea: String,
        destinationArea: String,
        time: String
    ): List<RideIntent> {
        val normSource = areaNormalizer.normalize(sourceArea)
        val normDest = areaNormalizer.normalize(destinationArea)
        val requestedTime = Instant.parse(time)

        return rideIntentRepository.findAllByDirection(direction)
            .filter {
                val sourceAreaScore = keywordMatcher.getScore(normSource, it.sourceKeywords.split(",").toSet())
                val destinationAreaScore = keywordMatcher.getScore(normDest, it.destinationKeywords.split(",").toSet())
                it.status == RideIntentStatus.ACTIVE &&
                        sourceAreaScore >= 0.4f &&
                        destinationAreaScore >= 0.4f &&
                        abs(it.startTime.epochSecond - requestedTime.epochSecond) <= it.flexibleMinutes * 60
            }
    }
}
