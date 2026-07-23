package com.base.services

import com.base.dtos.TaskDTO
import com.base.exceptions.ResourceNotFoundException
import com.base.exceptions.TaskOperationException
import com.base.mappers.TaskMapper
import com.base.models.Task
import com.base.models.TaskStatus
import com.base.repositories.TaskRepository
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TaskService(
    private val repository: TaskRepository,
    private val mapper: TaskMapper,
    private val meterRegistry: MeterRegistry
) {

    private val log = LoggerFactory.getLogger(TaskService::class.java)

    /**
     * Status terminais/fechados: nenhuma escrita adicional é aceita numa Task
     * já CANCELLED ou DONE (padrão de guard de status terminal do projeto).
     */
    private val closedTaskStatuses = setOf(TaskStatus.DONE, TaskStatus.CANCELLED)

    @Transactional
    fun create(dto: TaskDTO): TaskDTO = withMetrics("create") {
        log.info("action=create_task title={}", dto.title)
        val saved = repository.save(mapper.toEntity(dto))
        log.info("action=create_task status=ok id={}", saved.id)
        mapper.toDto(saved)
    }

    @Transactional
    fun complete(id: Long): TaskDTO = withMetrics("complete") {
        val task = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Task $id não encontrada") }
        requireNotClosed(task, action = "complete_task")

        val updated = task.copy(status = TaskStatus.DONE, updatedAt = LocalDateTime.now())
        val saved = repository.save(updated)
        log.info("action=complete_task status=ok id={}", saved.id)
        mapper.toDto(saved)
    }

    @Transactional
    fun cancel(id: Long): TaskDTO = withMetrics("cancel") {
        val task = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Task $id não encontrada") }
        requireNotClosed(task, action = "cancel_task")

        val updated = task.copy(status = TaskStatus.CANCELLED, updatedAt = LocalDateTime.now())
        val saved = repository.save(updated)
        log.info("action=cancel_task status=ok id={}", saved.id)
        mapper.toDto(saved)
    }

    @Transactional
    fun deactivate(id: Long): TaskDTO = withMetrics("deactivate") {
        val task = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Task $id não encontrada") }
        requireNotClosed(task, action = "deactivate_task")

        val updated = task.copy(
            active = false,
            deactivatedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val saved = repository.save(updated)
        log.info("action=deactivate_task status=ok id={}", saved.id)
        mapper.toDto(saved)
    }

    /**
     * Guard único de status terminal: nenhum write path (complete, deactivate, ou um futuro
     * método novo) pode duplicar esta checagem — todos devem chamar esta função.
     */
    private fun requireNotClosed(task: Task, action: String) {
        if (task.status in closedTaskStatuses) {
            log.warn("action={} status=conflict id={} currentStatus={}", action, task.id, task.status)
            throw TaskOperationException("Task ${task.id} não aceita esta operação no status ${task.status}")
        }
    }

    /**
     * Timer/counter únicos por operação (task.<action>.timer / task.<action>.count), na
     * convenção de observabilidade do CLAUDE.md — nenhum write path deve instrumentar métrica
     * na mão, todos passam por aqui.
     */
    private fun withMetrics(action: String, block: () -> TaskDTO): TaskDTO {
        val sample = Timer.start(meterRegistry)
        val outcome = try {
            block()
        } catch (ex: Exception) {
            meterRegistry.counter("task.$action.count", "status", "error").increment()
            throw ex
        } finally {
            sample.stop(meterRegistry.timer("task.$action.timer"))
        }
        meterRegistry.counter("task.$action.count", "status", "ok").increment()
        return outcome
    }
}
