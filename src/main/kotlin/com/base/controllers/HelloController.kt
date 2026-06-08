package com.base.controllers

import com.base.controllers.request.HelloRequest
import com.base.controllers.response.HelloResponse
import com.base.mappers.HelloMapper
import com.base.services.HelloService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/hello")
class HelloController(
    private val service: HelloService,
    private val mapper: HelloMapper
) {

    @PostMapping
    fun sayHello(@Valid @RequestBody request: HelloRequest): ResponseEntity<HelloResponse> {
        val dto = mapper.toDto(request)
        val saved = service.sayHello(dto)
        return ResponseEntity.created(URI.create("/api/v1/hello/${saved.id}"))
            .body(mapper.toResponse(saved))
    }
}

