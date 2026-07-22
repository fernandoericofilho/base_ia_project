package com.base.mappers

import com.base.controllers.request.TaskRequest
import com.base.controllers.response.TaskResponse
import com.base.dtos.TaskDTO
import com.base.models.Task
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TaskMapper {

    fun toDto(request: TaskRequest): TaskDTO =
        TaskDTO(title = request.title!!.trim())

    fun toEntity(dto: TaskDTO): Task =
        Task(
            id = dto.id,
            title = dto.title,
            status = dto.status,
            active = dto.active,
            createdAt = dto.createdAt ?: LocalDateTime.now(),
            updatedAt = dto.updatedAt ?: LocalDateTime.now(),
            deactivatedAt = dto.deactivatedAt,
            version = dto.version
        )

    fun toDto(entity: Task): TaskDTO =
        TaskDTO(
            id = entity.id,
            title = entity.title,
            status = entity.status,
            active = entity.active,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            deactivatedAt = entity.deactivatedAt,
            version = entity.version
        )

    fun toResponse(dto: TaskDTO): TaskResponse =
        TaskResponse(
            id = dto.id!!,
            title = dto.title,
            status = dto.status,
            active = dto.active,
            createdAt = dto.createdAt!!,
            updatedAt = dto.updatedAt!!,
            deactivatedAt = dto.deactivatedAt
        )
}
