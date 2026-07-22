package com.base.services

import com.base.dtos.TaskDTO
import com.base.exceptions.ResourceNotFoundException
import com.base.exceptions.TaskOperationException
import com.base.mappers.TaskMapper
import com.base.models.TaskStatus
import com.base.repositories.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TaskService(
    private val repository: TaskRepository,
    private val mapper: TaskMapper
) {

    private val log = LoggerFactory.getLogger(TaskService::class.java)

    /**
     * Status terminais/fechados: nenhuma escrita adicional é aceita numa Task
     * já CANCELLED ou DONE (padrão de guard de status terminal do projeto).
     */
    private val closedTaskStatuses = setOf(TaskStatus.DONE, TaskStatus.CANCELLED)

    @Transactional
    fun create(dto: TaskDTO): TaskDTO {
        log.info("action=create_task title={}", dto.title)
        val saved = repository.save(mapper.toEntity(dto))
        log.info("action=create_task status=ok id={}", saved.id)
        return mapper.toDto(saved)
    }

    @Transactional
    fun complete(id: Long): TaskDTO {
        val task = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Task $id não encontrada") }

        if (task.status in closedTaskStatuses) {
            log.warn("action=complete_task status=conflict id={} currentStatus={}", id, task.status)
            throw TaskOperationException("Task $id não aceita esta operação no status ${task.status}")
        }

        val updated = task.copy(status = TaskStatus.DONE, updatedAt = LocalDateTime.now())
        val saved = repository.save(updated)
        log.info("action=complete_task status=ok id={}", saved.id)
        return mapper.toDto(saved)
    }

    @Transactional
    fun deactivate(id: Long): TaskDTO {
        val task = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Task $id não encontrada") }

        if (task.status in closedTaskStatuses) {
            log.warn("action=deactivate_task status=conflict id={} currentStatus={}", id, task.status)
            throw TaskOperationException("Task $id não aceita esta operação no status ${task.status}")
        }

        val updated = task.copy(
            active = false,
            deactivatedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val saved = repository.save(updated)
        log.info("action=deactivate_task status=ok id={}", saved.id)
        return mapper.toDto(saved)
    }
}
