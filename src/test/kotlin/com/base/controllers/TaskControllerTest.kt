package com.base.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    private fun createTask(title: String = "Comprar leite"): Long {
        val response = mockMvc.perform(
            post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title": "$title"}""")
        )
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

        return (objectMapper.readTree(response).get("id")).asLong()
    }

    @Test
    fun `create - should return 201 with OPEN status`() {
        mockMvc.perform(
            post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title": "Estudar Kotlin"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("Estudar Kotlin"))
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(jsonPath("$.active").value(true))
    }

    @Test
    fun `create - blank title should return 400`() {
        mockMvc.perform(
            post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title": ""}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `complete - OPEN task should return 200 with DONE status`() {
        val id = createTask()

        mockMvc.perform(post("/api/v1/tasks/$id/complete"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("DONE"))
    }

    @Test
    fun `complete - unknown id should return 404`() {
        mockMvc.perform(post("/api/v1/tasks/999999/complete"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `cancel - OPEN task should return 200 with CANCELLED status`() {
        val id = createTask()

        mockMvc.perform(post("/api/v1/tasks/$id/cancel"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CANCELLED"))
    }

    @Test
    fun `cancel - unknown id should return 404`() {
        mockMvc.perform(post("/api/v1/tasks/999999/cancel"))
            .andExpect(status().isNotFound)
    }

    // TEST-TASK-04: completar uma Task duas vezes deve responder 422 na segunda tentativa,
    // pelo mesmo guard de status terminal validado a nível de Service em TaskServiceTest —
    // aqui garantimos que o guard também se reflete corretamente no HTTP status do Controller.
    @Test
    fun `complete - already DONE task should return 422`() {
        val id = createTask()
        mockMvc.perform(post("/api/v1/tasks/$id/complete")).andExpect(status().isOk)

        mockMvc.perform(post("/api/v1/tasks/$id/complete"))
            .andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `deactivate - OPEN task should return 200 with active false`() {
        val id = createTask()

        mockMvc.perform(delete("/api/v1/tasks/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.active").value(false))
    }

    @Test
    fun `deactivate - unknown id should return 404`() {
        mockMvc.perform(delete("/api/v1/tasks/999999"))
            .andExpect(status().isNotFound)
    }

    // TEST-TASK-05: desativar uma Task já concluída deve ser bloqueada (422) — mesma regra de
    // guard de status terminal usada em complete(), agora exercitada via Controller real.
    @Test
    fun `deactivate - already DONE task should return 422`() {
        val id = createTask()
        mockMvc.perform(post("/api/v1/tasks/$id/complete")).andExpect(status().isOk)

        mockMvc.perform(delete("/api/v1/tasks/$id"))
            .andExpect(status().isUnprocessableEntity)
    }
}
