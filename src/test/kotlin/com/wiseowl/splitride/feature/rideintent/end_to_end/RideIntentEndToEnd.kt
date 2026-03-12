package com.wiseowl.splitride.feature.rideintent.end_to_end

import com.wiseowl.splitride.feature.auth.dto.AuthenticationResponseDTO
import com.wiseowl.splitride.feature.auth.dto.LoginRequestDTO
import com.wiseowl.splitride.feature.auth.dto.RegisterRequestDTO
import com.wiseowl.splitride.feature.response.SplitRideResponse
import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
import com.wiseowl.splitride.feature.rideintent.dto.JoinGroupResponseDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideGroupDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideIntentResponseDTO
import com.wiseowl.splitride.feature.rideintent.model.Direction
import com.wiseowl.splitride.feature.rideintent.model.RideGroupStatus
import com.wiseowl.splitride.feature.rideintent.model.RideIntentStatus
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.annotation.Rollback
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.Instant.parse

@SpringBootTest
@AutoConfigureRestTestClient
@Transactional
@Rollback
@AutoConfigureMockMvc
class RideIntentEndToEnd(@Autowired val restTestClient: RestTestClient) {

    private val createRideIntentRequestBody = CreateRideIntentRequestDTO(
        direction = Direction.HOME_TO_OFFICE,
        sourceLat = 28.124413,
        sourceLng = 78.124413,
        destinationLat = 28.12441,
        destinationLng = 78.124413,
        sourceArea = "DLFCyberHub",
        destinationArea = "TechPark",
        scheduleType = "2027-02-28T22:14:45.000Z",
        flexibleMinutes = 10
    )

    fun RestTestClient.RequestHeadersSpec<*>.exchangeAuthorized(): RestTestClient.ResponseSpec {
        return headers { it.setBearerAuth(getAccessToken(restTestClient)) }.exchange()
    }

    @Test
    fun createRideIntent() {
        val request = CreateRideIntentRequestDTO(
            direction = Direction.HOME_TO_OFFICE,
            sourceLat = 28.124413,
            sourceLng = 78.124413,
            destinationLat = 28.12441,
            destinationLng = 78.124413,
            sourceArea = "DLFCyberHub",
            destinationArea = "TechPark",
            scheduleType = Instant.now().plusSeconds(1000*60*15).toString(),
            flexibleMinutes = 10
        )

        restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .value { response ->
                assert(response?.data?.direction == request.direction)
                assert(response?.data?.status == RideIntentStatus.ACTIVE)
                assert(response?.data?.flexibleMinutes == request.flexibleMinutes)
                assert(parse(response?.data?.startTime) == parse(request.scheduleType))
            }
    }

    @Test
    fun createRideIntentWithInvalidSartTimeReturnBadRequest() {
        restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)

            .body(
                createRideIntentRequestBody
                    .copy(
                        scheduleType = Instant.now().minusSeconds(60 * 15).toString(),
                    )
            )
            .exchangeAuthorized()
            .expectStatus().isBadRequest
    }

    @Test
    fun joinRideIntentGroup() {
        val createdRideIntent = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(createRideIntentRequestBody)
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .returnResult().responseBody?.data

        restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent?.id}")
            .exchangeAuthorized()
            .expectBody<JoinGroupResponseDTO>()
            .value { response ->
                assert(response?.rideGroupId != null)
                assert(response?.isGroupFull == false)
                assert(response?.currentMembers?.size == 1)
            }
    }

    @Test
    fun cancelRideIntent() {
        val createdRideIntent = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(createRideIntentRequestBody)
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .value { response ->
                assert(response?.data?.direction == createRideIntentRequestBody.direction)
                assert(response?.data?.status == RideIntentStatus.ACTIVE)
                assert(response?.data?.flexibleMinutes == createRideIntentRequestBody.flexibleMinutes)
                assert(parse(response?.data?.startTime) == parse(createRideIntentRequestBody.scheduleType))
            }.returnResult().responseBody?.data

        restTestClient.post()
            .uri("/api/ride-intents/${createdRideIntent?.id}/cancel")
            .exchangeAuthorized()
            .expectStatus()
            .isOk
    }

    @Test
    fun cancellingRideIntentThatIsAlreadyGroupedRemovesAssociatedGroupMemberFromThGroup() {
        val createdRideIntent1 = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)

            .body(createRideIntentRequestBody)
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .returnResult().responseBody?.data

        val createdRideIntent2 = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(createRideIntentRequestBody)
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .returnResult().responseBody?.data

        restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent1?.id}")
            .exchangeAuthorized()
            .expectBody<JoinGroupResponseDTO>()
            .value { response ->
                print(response)
                assert(response?.rideGroupId != null)
                assert(response?.isGroupFull == false)
                assert(response?.currentMembers?.size == 1)
            }

        val groupedResponse = restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent2?.id}")
            .exchangeAuthorized()
            .expectBody<JoinGroupResponseDTO>()
            .returnResult().responseBody

        restTestClient.get()
            .uri("/api/ride-intents/${createdRideIntent1?.id}/cancel")
            

        restTestClient.get().uri("/api/ride-groups/info/${groupedResponse?.rideGroupId}")
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideGroupDTO>>()
            .value { response ->
                assert(response?.data?.members?.any { it.rideIntentId.toString() != createdRideIntent1?.id } == true)
                assert(response?.data?.members?.any { it.rideIntentId.toString() == createdRideIntent2?.id } == true)
            }
    }

    @Test
    fun groupFullOneIntentCancelsGroupBecomesOpen() {
        val createdRideIntent1 = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(createRideIntentRequestBody)
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .returnResult().responseBody?.data

        val createdRideIntent2 = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(createRideIntentRequestBody)
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .returnResult().responseBody?.data

        val createdRideIntent3 = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(createRideIntentRequestBody)
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .returnResult().responseBody?.data

        restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent1?.id}")
            .exchangeAuthorized()

        restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent2?.id}")
            .exchangeAuthorized()

        val grouped3rResponse =
            restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent3?.id}")
                .exchangeAuthorized()
                .expectBody<JoinGroupResponseDTO>()
                .returnResult().responseBody

        restTestClient.get().uri("/api/ride-groups/info/${grouped3rResponse?.rideGroupId}")
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideGroupDTO>>()
            .value {
                assert(it?.data?.status == RideGroupStatus.FULL)
            }

        restTestClient.post()
            .uri("/api/ride-intents/${createdRideIntent2?.id}/cancel?userId=${createdRideIntent2?.userId}")
            .exchangeAuthorized()
            .expectStatus()
            .isOk

        restTestClient.get().uri("/api/ride-groups/info/${grouped3rResponse?.rideGroupId}")
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideGroupDTO>>()
            .value {
                assert(it?.data?.status == RideGroupStatus.OPEN)
            }
    }

    @Test
    fun groupHasSpaceForOne_OneRideIntentJoins_GroupBecomesFull() {
        val createdRideIntent1 = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(createRideIntentRequestBody)
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .returnResult().responseBody?.data

        val createdRideIntent2 = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(createRideIntentRequestBody)
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .returnResult().responseBody?.data

        val createdRideIntent3 = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(createRideIntentRequestBody)
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .returnResult().responseBody?.data

        restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent1?.id}")
            .exchangeAuthorized()

        restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent2?.id}")
            .exchangeAuthorized()

        val rideGroupId = restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent3?.id}")
            .exchangeAuthorized()
            .expectBody<JoinGroupResponseDTO>()
            .returnResult().responseBody?.rideGroupId

        restTestClient.get().uri("/api/ride-groups/info/$rideGroupId")
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideGroupDTO>>()
            .value {
                print(it)
                assert(it?.data?.status == RideGroupStatus.FULL)
            }
    }

    fun getAccessToken(restTestClient: RestTestClient): String{
        val registerDTO = RegisterRequestDTO(
            "Riyaz",
            "Uddin",
            "riyazps@gmail.com",
            "dummypass"
        )
        restTestClient.post().uri("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .body(registerDTO)
            .exchange()

        val loginDTO = LoginRequestDTO(
            "riyazps@gmail.com",
            "dummypass"
        )

        val loginResult = restTestClient.post().uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(loginDTO)
            .exchange()
            .expectBody<SplitRideResponse<AuthenticationResponseDTO>>()
            .returnResult().responseBody?.data

        return loginResult?.accessToken!!
    }

    @Test
    fun groupCancelWhenLastMemberLeaves() {
        val accessToken = getAccessToken(restTestClient)
        print(accessToken)
        val createdRideIntent1 = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            
            .body(createRideIntentRequestBody)
            .exchangeAuthorized()
            .returnResult()

        print(createdRideIntent1)

//        val createdRideIntent2 = restTestClient.post().uri("/api/ride-intents")
//            .contentType(MediaType.APPLICATION_JSON)
//            .body(createRideIntentRequestBody)
//            .exchangeAuthorized()
//            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
//            .returnResult().responseBody?.data
//
//        val rideGroupId = restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent1?.id}")
//            .contentType(MediaType.APPLICATION_JSON)
//            .exchangeAuthorized()
//            .expectBody<JoinGroupResponseDTO>()
//            .returnResult()
//            .responseBody?.rideGroupId


//        restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent2?.id}")
//            .contentType(MediaType.APPLICATION_JSON)
//            .exchangeAuthorized()
//
//        restTestClient.post()
//            .uri("/api/ride-intents/${createdRideIntent1?.id}/cancel?userId=${createdRideIntent1?.userId}")
//            .contentType(MediaType.APPLICATION_JSON)
//            .exchangeAuthorized()
//
//        restTestClient.post()
//            .uri("/api/ride-intents/${createdRideIntent2?.id}/cancel?userId=${createdRideIntent2?.userId}")
//            .contentType(MediaType.APPLICATION_JSON)
//            .exchangeAuthorized()
//
//        restTestClient.get().uri("/api/ride-groups/info/$rideGroupId")
//            .exchangeAuthorized()
//            .expectBody<SplitRideResponse<RideGroupDTO>>()
//            .value {
//                assert(it?.data?.status == RideGroupStatus.CANCELLED)
//            }
    }

    @Test
    fun groupIntentRejectsRejoin() {
        val rideIntentBody = createRideIntentRequestBody.copy(
            scheduleType = Instant.now().plusSeconds(10).toString()
        )
        val createdRideIntent1 = restTestClient.post().uri("/api/ride-intents")
            .contentType(MediaType.APPLICATION_JSON)
            .body(rideIntentBody)
            .exchangeAuthorized()
            .expectBody<SplitRideResponse<RideIntentResponseDTO>>()
            .returnResult().responseBody?.data


        restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent1?.id}")
            .exchangeAuthorized()

        restTestClient.post().uri("/api/ride-groups/join?rideIntentId=${createdRideIntent1?.id}")
            .exchangeAuthorized()
            .expectStatus()
            .isBadRequest
    }
}