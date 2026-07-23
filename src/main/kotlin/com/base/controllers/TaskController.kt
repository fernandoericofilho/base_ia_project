package com.base.controllers

import com.base.controllers.request.TaskRequest
import com.base.controllers.response.TaskResponse
import com.base.mappers.TaskMapper
import com.base.services.TaskService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/tasks")
class TaskController(
    private val service: TaskService,
    private val mapper: TaskMapper
) {

    @PostMapping
    fun create(@Valid @RequestBody request: TaskRequest): ResponseEntity<TaskResponse> {
        val dto = mapper.toDto(request)
        val saved = service.create(dto)
        return ResponseEntity.created(URI.create("/api/v1/tasks/${saved.id}"))
            .body(mapper.toResponse(saved))
    }

    @PostMapping("/{id}/complete")
    fun complete(@PathVariable id: Long): ResponseEntity<TaskResponse> {
        val saved = service.complete(id)
        return ResponseEntity.ok(mapper.toResponse(saved))
    }

    @PostMapping("/{id}/cancel")
    fun cancel(@PathVariable id: Long): ResponseEntity<TaskResponse> {
        val saved = service.cancel(id)
        return ResponseEntity.ok(mapper.toResponse(saved))
    }

    @DeleteMapping("/{id}")
    fun deactivate(@PathVariable id: Long): ResponseEntity<TaskResponse> {
        val saved = service.deactivate(id)
        return ResponseEntity.ok(mapper.toResponse(saved))
    }
}
