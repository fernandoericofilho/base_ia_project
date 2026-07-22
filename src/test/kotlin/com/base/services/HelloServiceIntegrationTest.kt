package com.base.services

import com.base.AbstractIntegrationTest
import com.base.dtos.HelloDTO
import com.base.repositories.GreetingRecordRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * TEST-HELLO-01: round-trip completo contra um Postgres real (Testcontainers).
 *
 * Diferente dos testes unitarios de HelloService (que mockam o repository),
 * este teste sobe o contexto Spring inteiro com um banco Postgres real e
 * confirma que o registro criado por HelloService.sayHello() é de fato
 * persistido e fica visível para uma leitura subsequente via
 * GreetingRecordRepository — validando o fluxo ponta a ponta e a migration
 * V1__create_greeting_record.sql.
 */
class HelloServiceIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var helloService: HelloService

    @Autowired
    private lateinit var greetingRecordRepository: GreetingRecordRepository

    @Test
    fun `sayHello should persist greeting record readable via repository`() {
        // ACTION: chama o service real, que grava no Postgres do container
        val saved = helloService.sayHello(HelloDTO(name = "Fernando", message = "Hello World, Fernando!"))

        // ASSERT: o id foi gerado e o registro existe de fato no banco
        assertNotNull(saved.id)
        val persisted = greetingRecordRepository.findById(saved.id!!).orElse(null)
        assertNotNull(persisted, "registro deveria estar persistido no Postgres real")
        assertEquals("Fernando", persisted.name)
        assertEquals("Hello World, Fernando!", persisted.message)
    }
}
