package com.wiseowl.splitride.feature.rideintent.service

import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
import com.wiseowl.splitride.feature.rideintent.model.Direction
import com.wiseowl.splitride.feature.rideintent.model.RideIntent
import com.wiseowl.splitride.feature.rideintent.model.RideIntentStatus
import com.wiseowl.splitride.feature.rideintent.repository.RideIntentRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import kotlin.math.abs

@Service
class RideIntentService(
    private val rideIntentRepository: RideIntentRepository,
    private val areaNormalizer: AreaNormalizer
) {

    fun create(req: CreateRideIntentRequestDTO): RideIntent {
        val intent = RideIntent(
            userId = UUID.fromString(req.userId),
            direction = req.direction,
            sourceArea = req.sourceArea,
            destinationArea = req.destinationArea,
            normalizedSource = areaNormalizer.normalize(req.sourceArea),
            normalizedDestination = areaNormalizer.normalize(req.destinationArea),
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
                it.normalizedSource == normSource &&
                        it.normalizedDestination == normDest &&
                        it.status == RideIntentStatus.ACTIVE &&
                        abs(it.startTime.epochSecond - requestedTime.epochSecond) <= it.flexibleMinutes * 60
            }
    }
}
