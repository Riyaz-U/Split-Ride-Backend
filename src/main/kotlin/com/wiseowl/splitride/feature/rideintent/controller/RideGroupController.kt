package com.wiseowl.splitride.feature.rideintent.controller

import com.wiseowl.splitride.feature.rideintent.dto.JoinGroupResponseDTO
import com.wiseowl.splitride.feature.rideintent.model.RideGroup
import com.wiseowl.splitride.feature.rideintent.service.RideIntentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/ride-groups")
class RideGroupController(private val service: RideIntentService) {

    @PostMapping("/join")
    fun joinGroup(
        @RequestParam rideIntentId: UUID
    ): JoinGroupResponseDTO {
        return service.joinGroup(rideIntentId)
    }

    @GetMapping("/info/{id}")
    fun getGroup(
        @PathVariable id: UUID
    ): RideGroup {
        return service.getGroup(id)
    }
}