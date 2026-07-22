package com.base

import org.junit.jupiter.api.Tag
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Base para testes de integracao "reais": sobe um Postgres de verdade via
 * Testcontainers (Docker) e aponta o Spring Context para ele, em vez do H2
 * usado no dia a dia. Estender esta classe sempre que o teste precisar
 * validar comportamento que só existe com um banco real: migrations,
 * constraints, queries JPA, round-trip HTTP -> banco.
 *
 * Estes testes são marcados com @Tag("integration") e por isso ficam de
 * fora do `./gradlew test` (rápido, sem Docker). Rodar via:
 *   ./gradlew integrationTest
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
abstract class AbstractIntegrationTest {

    companion object {
        private val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("base_project_test")
            withUsername("test")
            withPassword("test")
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureDataSource(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "validate" }
            registry.add("spring.jpa.properties.hibernate.dialect") { "org.hibernate.dialect.PostgreSQLDialect" }
        }
    }
}
