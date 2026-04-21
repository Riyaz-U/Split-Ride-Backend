package com.wiseowl.splitride.feature.rideintent.controller

import com.wiseowl.splitride.config.model.AuthenticatedUser
import com.wiseowl.splitride.feature.response.SplitRideResponse
import com.wiseowl.splitride.feature.response.SplitRideResponse.Companion.createSuccessResponse
import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideIntentResponseDTO
import com.wiseowl.splitride.feature.rideintent.dto.ScheduleSearchResponse
import com.wiseowl.splitride.feature.rideintent.dto.SearchStatusResponse
import com.wiseowl.splitride.feature.rideintent.dto.toDTO
import com.wiseowl.splitride.feature.rideintent.model.RideSearchProcess
import com.wiseowl.splitride.feature.rideintent.service.RideIntentService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
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

    @PostMapping("create")
    fun create(
        @RequestBody body: CreateRideIntentRequestDTO,
        @AuthenticationPrincipal user: AuthenticatedUser
    ): SplitRideResponse<RideIntentResponseDTO>{
        val response = createSuccessResponse(
            data = service.create(user.userId, body).toDTO(),
            status = HttpStatus.CREATED.value()
        )
        return response
    }

    @GetMapping("/search/schedule")
    fun scheduleSearch(
        @RequestParam rideIntentId: UUID
    ): SplitRideResponse<ScheduleSearchResponse> {
        val searchProcessId = service.scheduleSearch(rideIntentId)
        val response = createSuccessResponse(
            data = ScheduleSearchResponse(searchProcessId),
            status = HttpStatus.PROCESSING.value()
        )
        return response
    }

    @GetMapping("/search/status")
    fun searchStatus(
        @RequestParam searchProcessId: UUID
    ): SplitRideResponse<SearchStatusResponse> {
        val status = service.searchStatus(searchProcessId)
        val response = createSuccessResponse(
            data = SearchStatusResponse(status),
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
        @AuthenticationPrincipal user: AuthenticatedUser //TODO: Remove when authentication is completed
    ): SplitRideResponse<Unit>{
        service.cancelRideIntent(id, user.userId)
        return createSuccessResponse(null, HttpStatus.OK.value())
    }
}