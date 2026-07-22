package com.base.api.error

import com.base.exceptions.ValidationException
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @Test
    fun `should return 400 with validation error for invalid request`() {
        val invalidRequest = """{"name": ""}"""

        mockMvc.perform(
            post("/api/v1/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `should return 200 with valid request`() {
        val validRequest = """{"name": "Maria"}"""

        mockMvc.perform(
            post("/api/v1/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("Maria"))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `should return 400 for missing required field`() {
        val missingNameRequest = """{} """

        mockMvc.perform(
            post("/api/v1/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .content(missingNameRequest)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }
}

