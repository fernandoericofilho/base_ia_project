package com.base.controllers.request

import jakarta.validation.constraints.NotBlank

data class HelloRequest(
    @field:NotBlank(message = "name is required")
    val name: String?
)

