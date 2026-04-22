    package com.wiseowl.splitride.feature.rideintent.service

    import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
    import com.wiseowl.splitride.feature.rideintent.model.RideIntent
    import com.wiseowl.splitride.feature.rideintent.model.ScheduleType
    import com.wiseowl.splitride.feature.rideintent.repository.RideIntentRepository
    import com.wiseowl.splitride.feature.rideintent.util.GeoCalculator
    import com.wiseowl.splitride.feature.rideintent.repository.RideGroupRepository
    import com.wiseowl.splitride.feature.rideintent.repository.RideGroupMemberRepository
    import com.wiseowl.splitride.feature.rideintent.repository.RideSearchProcessRepository
    import org.mockito.Mock
    import org.junit.jupiter.api.BeforeEach
    import org.junit.jupiter.api.extension.ExtendWith
    import org.mockito.BDDMockito.given
    import org.mockito.junit.jupiter.MockitoExtension
    import org.mockito.kotlin.any
    import java.time.Instant
    import java.util.UUID
    import kotlin.test.Test

    @ExtendWith(MockitoExtension::class)
    class RideIntentServiceTest {


        val geoDistanceCalculator = GeoCalculator()

        @Mock lateinit var repo: RideIntentRepository
        @Mock lateinit var rideGroupRepository: RideGroupRepository
        @Mock lateinit var rideSearchProcessRepository: RideSearchProcessRepository
        @Mock lateinit var rideGroupMemberRepository: RideGroupMemberRepository
        lateinit var service: RideIntentService

        val sourceLat = 28.6315
        val sourceLng = 77.2090
        val destinationLat = 28.6225
        val destinationLng = 77.2210
        private val createRideIntentDTO: CreateRideIntentRequestDTO = CreateRideIntentRequestDTO(
            sourceLat = sourceLat,
            sourceLng = sourceLng,
            destinationLat = destinationLat,
            destinationLng = destinationLng,
            ScheduleType.Future(Instant.now().plusSeconds(100).toString()),
            10
        )

        val rideIntent = RideIntent(
            userId = UUID.randomUUID(),
            sourceLat = sourceLat,
            sourceLng = sourceLng,
            destinationLat = destinationLat,
            destinationLng = destinationLng,
            scheduleType = ScheduleType.Future(Instant.now().plusSeconds(100).toString()),
            flexibleMinutes = 10
        )

        @BeforeEach
        fun setUp() {
            service = RideIntentService(
                repo,
                rideGroupRepository,
                rideGroupMemberRepository,
                rideSearchProcessRepository,
                geoDistanceCalculator
            )
            given(repo.findByIdAndStatus(any(), any()))
                .willReturn(rideIntent)
        }

        @Test
        fun `create ride intent`() {
            val userId = "16918286-89c0-459a-92e5-8955f1b2c2bb"
            given(repo.save(any())).willReturn(rideIntent)
            val rideIntentId = service.createIntent(userId, createRideIntentDTO).id
            val searchProcessId = service.scheduleSearch(rideIntentId!!)

            kotlin.test.assertNotNull(searchProcessId)
        }
    }
