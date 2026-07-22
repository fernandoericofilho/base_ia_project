package com.base.controllers.response

import com.base.models.TaskStatus
import java.time.LocalDateTime

data class TaskResponse(
    val id: Long,
    val title: String,
    val status: TaskStatus,
    val active: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val deactivatedAt: LocalDateTime?
)
