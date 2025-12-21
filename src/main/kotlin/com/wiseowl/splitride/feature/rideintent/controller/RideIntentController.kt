package com.wiseowl.splitride.feature.rideintent.controller

import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
import com.wiseowl.splitride.feature.rideintent.dto.JoinGroupRequestDTO
import com.wiseowl.splitride.feature.rideintent.dto.JoinGroupResponseDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideIntentResponseDTO
import com.wiseowl.splitride.feature.rideintent.dto.toDTO
import com.wiseowl.splitride.feature.rideintent.model.Direction
import com.wiseowl.splitride.feature.rideintent.service.RideIntentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ride-intents")
class RideIntentController(private val service: RideIntentService) {

    @PostMapping
    fun create(@RequestBody body: CreateRideIntentRequestDTO): RideIntentResponseDTO{
        return service.create(body).toDTO()
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
    ): List<RideIntentResponseDTO> {
        return service.search(
            direction,
            sourceArea,
            destinationArea,
            sourceLat,
            sourceLng,
            destinationLat,
            destinationLng,
            time
        ).map { it.toDTO() }
    }

    @PostMapping("api/ride-groups/join")
    fun joinGroup(
        @RequestBody joinGroupRequestDTO: JoinGroupRequestDTO
    ): JoinGroupResponseDTO {
        return service.joinGroup(joinGroupRequestDTO)
    }
}