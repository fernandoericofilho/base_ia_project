package com.base.dtos

import java.time.LocalDateTime

data class HelloDTO(
    val id: Long? = null,
    val name: String,
    val message: String,
    val createdAt: LocalDateTime? = null
)

