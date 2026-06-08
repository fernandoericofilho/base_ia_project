package com.base.services

import com.base.dtos.HelloDTO
import com.base.mappers.HelloMapper
import com.base.repositories.GreetingRecordRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HelloService(
    private val repository: GreetingRecordRepository,
    private val mapper: HelloMapper
) {

    private val log = LoggerFactory.getLogger(HelloService::class.java)

    @Transactional
    fun sayHello(dto: HelloDTO): HelloDTO {
        log.info("action=say_hello name={}", dto.name)
        val saved = repository.save(mapper.toEntity(dto))
        log.info("action=say_hello status=success id={}", saved.id)
        return mapper.toDto(saved)
    }
}

