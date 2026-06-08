package com.base.mappers

import com.base.controllers.request.HelloRequest
import com.base.controllers.response.HelloResponse
import com.base.dtos.HelloDTO
import com.base.models.GreetingRecord
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class HelloMapper {

    fun toDto(request: HelloRequest): HelloDTO =
        HelloDTO(
            name = request.name!!.trim(),
            message = "Hello World, ${request.name.trim()}!"
        )

    fun toEntity(dto: HelloDTO): GreetingRecord =
        GreetingRecord(
            id = dto.id,
            name = dto.name,
            message = dto.message,
            createdAt = dto.createdAt ?: LocalDateTime.now()
        )

    fun toDto(entity: GreetingRecord): HelloDTO =
        HelloDTO(
            id = entity.id,
            name = entity.name,
            message = entity.message,
            createdAt = entity.createdAt
        )

    fun toResponse(dto: HelloDTO): HelloResponse =
        HelloResponse(
            id = dto.id!!,
            name = dto.name,
            message = dto.message,
            createdAt = dto.createdAt!!
        )
}

