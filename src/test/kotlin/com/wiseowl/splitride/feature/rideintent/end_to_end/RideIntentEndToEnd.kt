package com.wiseowl.splitride.feature.rideintent.end_to_end

import com.wiseowl.splitride.feature.response.SplitRideResponse
import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
import com.wiseowl.splitride.feature.rideintent.dto.JoinGroupResponseDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideGroupDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideIntentResponseDTO
import com.wiseowl.splitride.feature.rideintent.model.Direction
import com.wiseowl.splitride.feature.rideintent.model.RideIntentStatus
import com.wiseowl.splitride.feature.rideintent.repository.RideGroupMemberRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import java.time.Instant
import java.time.Instant.parse

@SpringBootTest
@AutoConfigureRestTestClient
class RideIntentEndToEnd(@Autowired val restTestClient: RestTestClient, @Autowired val repository: RideGroupMemberRepository) {

    @AfterEach
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun createRideIntent() {
        val request = CreateRideIntentRequestDTO(
            userId = "48b7ee6d-8f7c-4056-89c3-85557237bc21",
            direction = Direction.HOME_TO_OFFICE,
            sourceLat = 28.124413,
            sourceLng = 78.124413,
            destinationLat = 28.12441,
            destinationLng = 78.124413,
            sourceArea = "DLFCyberHub",
            destinationArea = "TechPark",
            startTime = Instant.now().plusSeconds(60*15).toString(),
            flexibleMinutes = 10
        )

        restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .value { response ->
                assert(response?.data?.userId == request.userId)
                assert(response?.data?.direction == request.direction)
                assert(response?.data?.status == RideIntentStatus.ACTIVE)
                assert(response?.data?.flexibleMinutes == request.flexibleMinutes)
                assert(parse(response?.data?.startTime) == parse(request.startTime))
            }
    };

    @Test
    fun createRideIntentWithInvalidSartTimeReturnBadRequest() {
        val request = CreateRideIntentRequestDTO(
            userId = "48b7ee6d-8f7c-4056-89c3-85557237bc21",
            direction = Direction.HOME_TO_OFFICE,
            sourceLat = 28.124413,
            sourceLng = 78.124413,
            destinationLat = 28.12441,
            destinationLng = 78.124413,
            sourceArea = "DLFCyberHub",
            destinationArea = "TechPark",
            startTime = Instant.now().minusSeconds(60).toString(),
            flexibleMinutes = 10
        )

        restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun joinRideIntentGroup() {
        val request = CreateRideIntentRequestDTO(
            userId = "48b7ee6d-8f7c-4056-89c3-85557237bc21",
            direction = Direction.HOME_TO_OFFICE,
            sourceLat = 28.124413,
            sourceLng = 78.124413,
            destinationLat = 28.12441,
            destinationLng = 78.124413,
            sourceArea = "DLFCyberHub",
            destinationArea = "TechPark",
            startTime = "2026-02-28T22:14:45.000Z",
            flexibleMinutes = 10
        )
        val createdRideIntent = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .returnResult().responseBody?.data

        restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent?.id}")
            .exchange()
            .expectBody<JoinGroupResponseDTO>()
            .value { response ->
                assert(response?.rideGroupId!=null)
                assert(response?.isGroupFull==false)
                assert(response?.currentMembers?.size == 1)
            }
    }

    @Test
    fun cancelRideIntent() {
        val request = CreateRideIntentRequestDTO(
            userId = "48b7ee6d-8f7c-4056-89c3-85557237bc21",
            direction = Direction.HOME_TO_OFFICE,
            sourceLat = 28.124413,
            sourceLng = 78.124413,
            destinationLat = 28.12441,
            destinationLng = 78.124413,
            sourceArea = "DLFCyberHub",
            destinationArea = "TechPark",
            startTime = "2026-02-28T22:14:45.000Z",
            flexibleMinutes = 10
        )

        val createdRideIntent = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .value { response ->
                assert(response?.data?.userId == request.userId)
                assert(response?.data?.direction == request.direction)
                assert(response?.data?.status == RideIntentStatus.ACTIVE)
                assert(response?.data?.flexibleMinutes == request.flexibleMinutes)
                assert(parse(response?.data?.startTime) == parse(request.startTime))
            }.returnResult().responseBody?.data

        restTestClient.post().uri("/api/ride-intents/${createdRideIntent?.id}/cancel?userId=${createdRideIntent?.userId}")
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun cancellingRideIntentThatIsAlreadyGroupedRemovesAssociatedGroupMemberFromThGroup() {
        val request = CreateRideIntentRequestDTO(
            userId = "48b7ee6d-8f7c-4056-89c3-85557237bc21",
            direction = Direction.HOME_TO_OFFICE,
            sourceLat = 28.124413,
            sourceLng = 78.124413,
            destinationLat = 28.12441,
            destinationLng = 78.124413,
            sourceArea = "DLFCyberHub",
            destinationArea = "TechPark",
            startTime = "2026-02-28T22:14:45.000Z",
            flexibleMinutes = 10
        )

        val createdRideIntent1 = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .returnResult().responseBody?.data

        val createdRideIntent2 = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .returnResult().responseBody?.data

        restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent1?.id}")
            .exchange()
            .expectBody<JoinGroupResponseDTO>()
            .value { response ->
                print(response)
                assert(response?.rideGroupId!=null)
                assert(response?.isGroupFull==false)
                assert(response?.currentMembers?.size == 1)
            }

        val groupedResponse = restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent2?.id}")
            .exchange()
            .expectBody<JoinGroupResponseDTO>()
            .returnResult().responseBody

        restTestClient.get().uri("http://localhost:8080/api/ride-intents/${createdRideIntent1?.id}/cancel?userId=${request.userId}")

        restTestClient.get().uri("/api/ride-groups/info/${groupedResponse?.rideGroupId}")
            .exchange()
            .expectBody<SplitRideResponse<RideGroupDTO>>()
            .value { response ->
                assert(response?.data?.members?.any { it.rideIntentId.toString()!=createdRideIntent1?.id }==true)
                assert(response?.data?.members?.any { it.rideIntentId.toString()==createdRideIntent2?.id }==true)
            }
    }
}