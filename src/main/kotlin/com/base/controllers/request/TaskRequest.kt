package com.base.controllers.request

import jakarta.validation.constraints.NotBlank

data class TaskRequest(
    @field:NotBlank(message = "title is required")
    val title: String?
)
