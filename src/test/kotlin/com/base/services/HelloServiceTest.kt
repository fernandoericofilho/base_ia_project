package com.base.services

import com.base.dtos.HelloDTO
import com.base.mappers.HelloMapper
import com.base.models.GreetingRecord
import com.base.repositories.GreetingRecordRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import kotlin.test.assertEquals

class HelloServiceTest {

    private val repository: GreetingRecordRepository = mock()
    private val mapper = HelloMapper()
    private val service = HelloService(repository, mapper)

    @Test
    fun `sayHello - should save greeting and return dto`() {
        whenever(repository.save(any<GreetingRecord>())).thenAnswer { invocation ->
            val entity = invocation.arguments[0] as GreetingRecord
            entity.copy(id = 1L)
        }

        val result = service.sayHello(
            HelloDTO(name = "Maria", message = "Hello World, Maria!", createdAt = LocalDateTime.now())
        )

        assertEquals(1L, result.id)
        assertEquals("Maria", result.name)
    }
}

