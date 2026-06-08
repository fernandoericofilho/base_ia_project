package com.base.controllers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloControllerIT {

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `POST hello - should persist and return hello world`() {
        val response = restTemplate.postForEntity(
            "http://localhost:$port/api/v1/hello",
            mapOf("name" to "Joao"),
            Map::class.java
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("Hello World, Joao!", response.body?.get("message"))
    }
}

