package com.base.controllers

import com.base.controllers.request.TaskRequest
import com.base.controllers.response.TaskResponse
import com.base.mappers.TaskMapper
import com.base.services.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Task", description = "CRUD de referência: guard de status terminal, soft delete e optimistic locking")
class TaskController(
    private val service: TaskService,
    private val mapper: TaskMapper
) {

    @PostMapping
    @Operation(summary = "Cria uma Task no status OPEN")
    @ApiResponse(responseCode = "201", description = "Task criada")
    @ApiResponse(responseCode = "400", description = "Título ausente ou inválido")
    fun create(@Valid @RequestBody request: TaskRequest): ResponseEntity<TaskResponse> {
        val dto = mapper.toDto(request)
        val saved = service.create(dto)
        return ResponseEntity.created(URI.create("/api/v1/tasks/${saved.id}"))
            .body(mapper.toResponse(saved))
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Transiciona a Task de OPEN para DONE")
    @ApiResponse(responseCode = "200", description = "Task concluída")
    @ApiResponse(responseCode = "404", description = "Task não encontrada")
    @ApiResponse(responseCode = "422", description = "Task já está em status terminal (DONE ou CANCELLED)")
    fun complete(@PathVariable id: Long): ResponseEntity<TaskResponse> {
        val saved = service.complete(id)
        return ResponseEntity.ok(mapper.toResponse(saved))
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Transiciona a Task de OPEN para CANCELLED")
    @ApiResponse(responseCode = "200", description = "Task cancelada")
    @ApiResponse(responseCode = "404", description = "Task não encontrada")
    @ApiResponse(responseCode = "422", description = "Task já está em status terminal (DONE ou CANCELLED)")
    fun cancel(@PathVariable id: Long): ResponseEntity<TaskResponse> {
        val saved = service.cancel(id)
        return ResponseEntity.ok(mapper.toResponse(saved))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete: marca a Task como inativa sem apagar o registro")
    @ApiResponse(responseCode = "200", description = "Task desativada")
    @ApiResponse(responseCode = "404", description = "Task não encontrada")
    @ApiResponse(responseCode = "422", description = "Task já está em status terminal (DONE ou CANCELLED)")
    fun deactivate(@PathVariable id: Long): ResponseEntity<TaskResponse> {
        val saved = service.deactivate(id)
        return ResponseEntity.ok(mapper.toResponse(saved))
    }
}
