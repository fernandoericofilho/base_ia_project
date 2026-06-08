package com.base.mappers

import com.base.controllers.request.HelloRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HelloMapperTest {

    private val mapper = HelloMapper()

    @Test
    fun `toDto - should trim name and build hello world message`() {
        val dto = mapper.toDto(HelloRequest(name = "  Maria  "))
        assertEquals("Maria", dto.name)
        assertEquals("Hello World, Maria!", dto.message)
    }
}

