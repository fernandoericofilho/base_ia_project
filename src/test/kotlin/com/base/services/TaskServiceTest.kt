package com.base.services

import com.base.dtos.TaskDTO
import com.base.exceptions.ResourceNotFoundException
import com.base.exceptions.TaskOperationException
import com.base.mappers.TaskMapper
import com.base.models.Task
import com.base.models.TaskStatus
import com.base.repositories.TaskRepository
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TaskServiceTest {

    private val repository: TaskRepository = mock()
    private val mapper = TaskMapper()
    private val meterRegistry = SimpleMeterRegistry()
    private val service = TaskService(repository, mapper, meterRegistry)

    private fun taskEntity(id: Long, status: TaskStatus) = Task(
        id = id,
        title = "Comprar leite",
        status = status,
        active = true,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
        deactivatedAt = null,
        version = 0
    )

    @Test
    fun `create - should save task and return dto`() {
        whenever(repository.save(any<Task>())).thenAnswer { invocation ->
            val entity = invocation.arguments[0] as Task
            entity.copy(id = 1L)
        }

        val result = service.create(TaskDTO(title = "Comprar leite"))

        assertEquals(1L, result.id)
        assertEquals(TaskStatus.OPEN, result.status)
    }

    // TEST-TASK-08: toda operação de TaskService deve incrementar task.<action>.count e
    // registrar task.<action>.timer (regra de observabilidade do CLAUDE.md) — tanto no
    // caminho de sucesso quanto no de erro, com a tag status correta em cada caso.
    @Test
    fun `create - should record success metric task_create_count`() {
        whenever(repository.save(any<Task>())).thenAnswer { (it.arguments[0] as Task).copy(id = 1L) }

        service.create(TaskDTO(title = "Comprar leite"))

        assertEquals(1.0, meterRegistry.counter("task.create.count", "status", "ok").count())
        assertNotNull(meterRegistry.find("task.create.timer").timer())
    }

    @Test
    fun `complete - not found should record error metric task_complete_count`() {
        whenever(repository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> { service.complete(99L) }

        assertEquals(1.0, meterRegistry.counter("task.complete.count", "status", "error").count())
    }

    @Test
    fun `complete - OPEN task should transition to DONE`() {
        val open = taskEntity(1L, TaskStatus.OPEN)
        whenever(repository.findById(1L)).thenReturn(Optional.of(open))
        whenever(repository.save(any<Task>())).thenAnswer { it.arguments[0] as Task }

        val result = service.complete(1L)

        assertEquals(TaskStatus.DONE, result.status)
    }

    @Test
    fun `complete - task not found should throw ResourceNotFoundException`() {
        whenever(repository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> { service.complete(99L) }
    }

    // TEST-TASK-01: completar uma Task já CANCELLED deve lançar TaskOperationException.
    // Sem o guard de status terminal (closedTaskStatuses) no TaskService, o serviço
    // sobrescreveria silenciosamente o status CANCELLED para DONE, perdendo a intenção
    // original do usuário de cancelar a tarefa. Este teste falha se o guard for removido.
    @Test
    fun `complete - already CANCELLED task must throw TaskOperationException`() {
        val cancelled = taskEntity(1L, TaskStatus.CANCELLED)
        whenever(repository.findById(1L)).thenReturn(Optional.of(cancelled))

        assertThrows<TaskOperationException> { service.complete(1L) }

        verify(repository, never()).save(any())
    }

    // TEST-TASK-02: completar uma Task já DONE também deve ser bloqueada (idempotência de
    // negócio: não é permitido "completar" duas vezes).
    @Test
    fun `complete - already DONE task must throw TaskOperationException`() {
        val done = taskEntity(1L, TaskStatus.DONE)
        whenever(repository.findById(1L)).thenReturn(Optional.of(done))

        assertThrows<TaskOperationException> { service.complete(1L) }

        verify(repository, never()).save(any())
    }

    // TEST-TASK-06: cancelar uma Task OPEN deve transicionar para CANCELLED. Antes desta
    // mudança não existia nenhum caminho no sistema capaz de produzir CANCELLED, apesar do
    // enum TaskStatus já prever esse valor e o guard de status terminal já tratá-lo como
    // fechado — a máquina de estados documentada em REGRAS-DO-SISTEMA.md ficava incompleta.
    @Test
    fun `cancel - OPEN task should transition to CANCELLED`() {
        val open = taskEntity(1L, TaskStatus.OPEN)
        whenever(repository.findById(1L)).thenReturn(Optional.of(open))
        whenever(repository.save(any<Task>())).thenAnswer { it.arguments[0] as Task }

        val result = service.cancel(1L)

        assertEquals(TaskStatus.CANCELLED, result.status)
    }

    @Test
    fun `cancel - task not found should throw ResourceNotFoundException`() {
        whenever(repository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> { service.cancel(99L) }
    }

    // TEST-TASK-07: cancelar uma Task já DONE deve ser bloqueada pelo mesmo guard de status
    // terminal usado em complete()/deactivate() — DONE não pode virar CANCELLED depois do fato.
    @Test
    fun `cancel - already DONE task must throw TaskOperationException`() {
        val done = taskEntity(1L, TaskStatus.DONE)
        whenever(repository.findById(1L)).thenReturn(Optional.of(done))

        assertThrows<TaskOperationException> { service.cancel(1L) }

        verify(repository, never()).save(any())
    }

    @Test
    fun `deactivate - OPEN task should be soft deleted`() {
        val open = taskEntity(1L, TaskStatus.OPEN)
        whenever(repository.findById(1L)).thenReturn(Optional.of(open))
        whenever(repository.save(any<Task>())).thenAnswer { it.arguments[0] as Task }

        val result = service.deactivate(1L)

        assertTrue(result.active == false)
        assertNotNull(result.deactivatedAt)
    }

    // TEST-TASK-03: soft delete de uma Task já CANCELLED deve lançar TaskOperationException,
    // seguindo o mesmo guard de status terminal usado em complete().
    @Test
    fun `deactivate - already CANCELLED task must throw TaskOperationException`() {
        val cancelled = taskEntity(1L, TaskStatus.CANCELLED)
        whenever(repository.findById(1L)).thenReturn(Optional.of(cancelled))

        assertThrows<TaskOperationException> { service.deactivate(1L) }

        verify(repository, never()).save(any())
    }
}
