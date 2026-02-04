package com.wiseowl.splitride.feature.auth

import com.wiseowl.splitride.feature.auth.dto.LoginRequestDTO
import com.wiseowl.splitride.feature.auth.dto.RegisterRequestDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient
import kotlin.test.Test

@SpringBootTest
@AutoConfigureRestTestClient
class AuthenticationTest(@Autowired val restTemplate: RestTestClient) {

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
            .expectStatus().isCreated
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
            .expectStatus().is5xxServerError
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
            ).exchange()
            .expectStatus().isOk
    }
}