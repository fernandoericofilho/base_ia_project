package com.base.controllers.response

import java.time.LocalDateTime

data class HelloResponse(
    val id: Long,
    val name: String,
    val message: String,
    val createdAt: LocalDateTime
)

