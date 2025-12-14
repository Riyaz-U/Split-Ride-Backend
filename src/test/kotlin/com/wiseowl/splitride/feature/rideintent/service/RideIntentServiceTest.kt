    package com.wiseowl.splitride.feature.rideintent.service

    import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
    import com.wiseowl.splitride.feature.rideintent.model.Direction
    import com.wiseowl.splitride.feature.rideintent.model.RideIntent
    import com.wiseowl.splitride.feature.rideintent.repository.RideIntentRepository
    import com.wiseowl.splitride.feature.rideintent.util.AreaNormalizer
    import com.wiseowl.splitride.feature.rideintent.util.GeoCalculator
    import com.wiseowl.splitride.feature.rideintent.util.KeywordExtractor
    import com.wiseowl.splitride.feature.rideintent.util.KeywordMatcher
    import org.junit.jupiter.api.Assertions.assertTrue
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

        val keywordExtractor = KeywordExtractor()
        val areaNormalizer = AreaNormalizer()
        val keywordMatcher = KeywordMatcher(areaNormalizer, keywordExtractor)
        val geoDistanceCalculator = GeoCalculator()

        @Mock lateinit var repo: RideIntentRepository
        lateinit var service: RideIntentService

        val sourceArea = "cyber city"
        val destinationArea = "cyber park"
        val sourceLat = 28.6315
        val sourceLng = 77.2090
        val destinationLat = 28.6225
        val destinationLng = 77.2210
        private val createRideIntentDTO: CreateRideIntentRequestDTO = CreateRideIntentRequestDTO(
            "48b7ee6d-8f7c-4056-89c3-85557237bce4",
            Direction.HOME_TO_OFFICE,
            sourceArea,
            destinationArea,
            sourceLat = sourceLat,
            sourceLng = sourceLng,
            destinationLat = destinationLat,
            destinationLng = destinationLng,
            Instant.now().toString(),
            10
        )
        val normalizedSource = "cybercity"
        val normalizedDestination = "cyber pk"

        val sourceKeywords = setOf("cyber", "city")
        val destinationKeywords = setOf("cyber", "pk")

        val rideIntent = RideIntent(
            userId = UUID.fromString(createRideIntentDTO.userId),
            direction = Direction.HOME_TO_OFFICE,
            sourceArea = sourceArea,
            destinationArea = destinationArea,
            normalizedSource = normalizedSource,
            normalizedDestination = normalizedDestination,
            sourceLat = sourceLat,
            sourceLng = sourceLng,
            destinationLat = destinationLat,
            destinationLng = destinationLng,
            sourceKeywords = sourceKeywords.joinToString(","),
            destinationKeywords = destinationKeywords.joinToString(","),
            startTime = Instant.now(),
            flexibleMinutes = 10
        )

        @BeforeEach
        fun setUp() {
            service = RideIntentService(
                repo,
                areaNormalizer,
                keywordExtractor,
                keywordMatcher,
                geoDistanceCalculator
            )
            given(repo.findAllByDirection(any()))
                .willReturn(listOf(rideIntent))
        }

        @Test
        fun `create ride intent`() {
            given(repo.save(any())).willReturn(rideIntent)
            service.create(createRideIntentDTO)
            val result = service.search(
                Direction.HOME_TO_OFFICE,
                sourceArea = sourceArea,
                destinationArea = destinationArea,
                sourceLat = sourceLat,
                sourceLng = sourceLng,
                destinationLat = destinationLat,
                destinationLng = destinationLng,
                time = Instant.now().toString()
            )

            kotlin.test.assertEquals(1, result.size)
        }

        @Test
        fun `search ride intent with no matching return empty`() {
            val result = service.search(
                Direction.OFFICE_TO_HOME,
                sourceArea,
                destinationArea,
                sourceLat+0.1,
                sourceLng+0.1,
                destinationLat,
                destinationLng,
                Instant.now().toString()
            )
            assertTrue(result.isEmpty())
        }

        @Test
        fun `search ride intent with matching return ride intent`() {
            val result = service.search(
                Direction.HOME_TO_OFFICE,
                sourceArea = sourceArea,
                destinationArea = destinationArea,
                sourceLat,
                sourceLng,
                destinationLat,
                destinationLng,
                Instant.now().toString()
            )

            assertTrue(result.isNotEmpty())
        }
    }
