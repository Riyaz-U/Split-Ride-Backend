package com.wiseowl.splitride.feature.rideintent.service

import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
import com.wiseowl.splitride.feature.rideintent.model.Direction
import com.wiseowl.splitride.feature.rideintent.model.RideIntent
import com.wiseowl.splitride.feature.rideintent.model.RideIntentStatus
import com.wiseowl.splitride.feature.rideintent.repository.RideIntentRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class RideIntentService(
    private val rideIntentRepository: RideIntentRepository
) {

    fun create(req: CreateRideIntentRequestDTO): RideIntent {
        val intent = RideIntent(
            userId = UUID.fromString(req.userId),
            direction = req.direction,
            sourceArea = req.sourceArea,
            destinationArea = req.destinationArea,
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

        return rideIntentRepository.findAllByDirection(direction)
            ?.filter {
                it.sourceArea.equals(sourceArea, ignoreCase = true) &&
                        it.destinationArea.equals(destinationArea, ignoreCase = true) &&
                        it.status == RideIntentStatus.ACTIVE &&
                        kotlin.math.abs(it.startTime.epochSecond - Instant.parse(time).epochSecond) <= it.flexibleMinutes * 60
            }.orEmpty()
    }
}
