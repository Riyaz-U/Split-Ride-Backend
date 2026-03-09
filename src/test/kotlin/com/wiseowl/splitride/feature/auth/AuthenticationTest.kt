package com.wiseowl.splitride.feature.auth

import com.wiseowl.splitride.feature.auth.dto.AuthenticationResponseDTO
import com.wiseowl.splitride.feature.auth.dto.LoginRequestDTO
import com.wiseowl.splitride.feature.auth.dto.RegisterRequestDTO
import com.wiseowl.splitride.feature.auth.repository.UserRepository
import com.wiseowl.splitride.feature.response.SplitRideResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.annotation.Rollback
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestTestClient
@Rollback
@Transactional
class AuthenticationTest(@Autowired val restTemplate: RestTestClient, @Autowired val userRepository: UserRepository) {

    @AfterEach
    fun cleanUp(){
        userRepository.deleteAll()
    }

    @Test
    fun registerUserCreatesNewUser(){
        restTemplate.post()
            .uri("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                RegisterRequestDTO(
                    firstName = "John",
                    lastName = "Doe",
                    email = "Jone@gmail.com",
                    password = "somepassword"
                )
            ).exchange()
            .expectBody<SplitRideResponse<Unit>>()
            .value { result ->
                assertNotNull(result)
                assert(result.status == HttpStatus.CREATED.value())
            }
    }

    @Test
    fun registeringWithExistingEmailReturnsError(){
        restTemplate.post()
            .uri("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                RegisterRequestDTO(
                    firstName = "John",
                    lastName = "Doe",
                    email = "Jone@gmail.com",
                    password = "somepassword"
                )
            ).exchange()

        restTemplate.post()
            .uri("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                RegisterRequestDTO(
                    firstName = "John1",
                    lastName = "Doe1",
                    email = "Jone@gmail.com",
                    password = "somepassword1"
                )
            ).exchange()
            .expectBody<SplitRideResponse<Unit>>()
            .value { result ->
                assertNotNull(result)
                assertFalse(result.success)
                assert(result.status == HttpStatus.BAD_REQUEST.value())
            }
    }

    @Test
    fun loginInWithExistingUserReturnsSuccess(){
        val email = "Jone@gmail.com"
        val password = "somepassword"

        restTemplate.post()
            .uri("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                RegisterRequestDTO(
                    firstName = "John",
                    lastName = "Doe",
                    email = email,
                    password = password
                )
            ).exchange()

        restTemplate.post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                LoginRequestDTO(
                    email = email,
                    password = password
                )
            )
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun unAuthenticatedUserCannotAccessAuthenticatedRoute(){
        restTemplate.get()
            .uri("/")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun authenticatedUserCanAccessAuthenticatedRoute(){
        val email = "John@gmail.com"
        val password = "johnPassword"
        restTemplate.post()
            .uri("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                RegisterRequestDTO(
                    firstName = "John",
                    lastName = "Doe",
                    email = email,
                    password = password
                )
            ).exchange()

        val loginResponse = restTemplate.post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                LoginRequestDTO(
                    email = email,
                    password = password
                )
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectBody<SplitRideResponse<AuthenticationResponseDTO>>()
            .returnResult()
            .responseBody

        val accessToken = loginResponse?.data?.accessToken!!

        restTemplate.get()
            .uri("/")
            .headers { it.setBearerAuth(accessToken) }
            .exchange()
            .expectStatus().isNotFound
    }
}