package com.wiseowl.splitride.feature.rideintent.controller

import com.wiseowl.splitride.feature.response.SplitRideResponse
import com.wiseowl.splitride.feature.response.SplitRideResponse.Companion.createSuccessResponse
import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideIntentResponseDTO
import com.wiseowl.splitride.feature.rideintent.dto.toDTO
import com.wiseowl.splitride.feature.rideintent.model.Direction
import com.wiseowl.splitride.feature.rideintent.service.RideIntentService
import org.springframework.http.HttpStatus
import jakarta.websocket.server.PathParam
import org.apache.coyote.Response
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/ride-intents")
class RideIntentController(private val service: RideIntentService) {

    @PostMapping
    fun create(@RequestBody body: CreateRideIntentRequestDTO): SplitRideResponse<RideIntentResponseDTO>{
        val response = createSuccessResponse(
            data = service.create(body).toDTO(),
            status = HttpStatus.CREATED.value()
        )
        return response
    }

    @GetMapping("/search")
    fun search(
        @RequestParam direction: Direction,
        @RequestParam sourceArea: String,
        @RequestParam destinationArea: String,
        @RequestParam sourceLat: Double,
        @RequestParam sourceLng: Double,
        @RequestParam destinationLat: Double,
        @RequestParam destinationLng: Double,
        @RequestParam time: String
    ): SplitRideResponse<List<RideIntentResponseDTO>> {
        val response = createSuccessResponse(
            data = service.search(
                direction,
                sourceArea,
                destinationArea,
                sourceLat,
                sourceLng,
                destinationLat,
                destinationLng,
                time
            ).map { it.toDTO() },
            status = HttpStatus.OK.value()
        )
        return response
    }

    @GetMapping("/{id}")
    fun getRideIntent(@PathVariable id: UUID): SplitRideResponse<RideIntentResponseDTO> {
        val response = createSuccessResponse(
            data = service.getRideIntent(id),
            status = HttpStatus.FOUND.value()
        )
        return response
    }

    @PostMapping("/{id}/cancel")
    fun cancelRideIntent(
        @PathVariable id: UUID,
        @RequestParam userId: UUID //TODO: Remove when authentication is completed
    ): SplitRideResponse<Unit>{
        service.cancelRideIntent(id, userId)
        return createSuccessResponse(null, HttpStatus.OK.value())
    }
}