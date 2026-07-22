package com.base.services

import com.base.dtos.TaskDTO
import com.base.exceptions.ResourceNotFoundException
import com.base.exceptions.TaskOperationException
import com.base.mappers.TaskMapper
import com.base.models.Task
import com.base.models.TaskStatus
import com.base.repositories.TaskRepository
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
    private val service = TaskService(repository, mapper)

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
