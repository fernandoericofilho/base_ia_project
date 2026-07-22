package com.base.dtos

import com.base.models.TaskStatus
import java.time.LocalDateTime

data class TaskDTO(
    val id: Long? = null,
    val title: String,
    val status: TaskStatus = TaskStatus.OPEN,
    val active: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val deactivatedAt: LocalDateTime? = null,
    val version: Long = 0
)
