package com.base.controllers

import com.base.controllers.request.HelloRequest
import com.base.controllers.response.HelloResponse
import com.base.mappers.HelloMapper
import com.base.services.HelloService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/hello")
@Tag(name = "Hello", description = "Exemplo didático: saudação persistida")
class HelloController(
    private val service: HelloService,
    private val mapper: HelloMapper
) {

    @PostMapping
    @Operation(summary = "Cria uma saudação persistida para o nome informado")
    @ApiResponse(responseCode = "201", description = "Saudação criada")
    @ApiResponse(responseCode = "400", description = "Nome ausente ou inválido")
    fun sayHello(@Valid @RequestBody request: HelloRequest): ResponseEntity<HelloResponse> {
        val dto = mapper.toDto(request)
        val saved = service.sayHello(dto)
        return ResponseEntity.created(URI.create("/api/v1/hello/${saved.id}"))
            .body(mapper.toResponse(saved))
    }
}

