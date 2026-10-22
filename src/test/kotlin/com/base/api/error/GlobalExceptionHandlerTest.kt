package com.base.api.error

import com.base.exceptions.ValidationException
import com.base.models.Task
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.web.context.request.WebRequest
import kotlin.test.assertEquals

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

    // TEST-ERROR-04: conflito de concorrência otimista (@Version, ex.: Task) deve responder
    // 409, não 500 — sem este handler, ObjectOptimisticLockingFailureException cai no handler
    // genérico de Exception e o cliente nunca sabe que o conflito era de concorrência.
    @Test
    fun `optimistic lock conflict should return 409`() {
        val handler = GlobalExceptionHandler()
        val request: WebRequest = mock()
        whenever(request.getDescription(false)).thenReturn("uri=/api/v1/tasks/1")
        val ex = ObjectOptimisticLockingFailureException(Task::class.java, 1L)

        val response = handler.handleOptimisticLockingException(ex, request)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals(409, response.body?.status)
        assertEquals("/api/v1/tasks/1", response.body?.path)
    }
}

