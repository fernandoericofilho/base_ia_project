package com.base.api.error

import java.time.LocalDateTime

/**
 * Response padrão para erros da API
 */
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null
)

/**
 * Erro de validação de campo específico
 */
data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null
)

/**
 * Response detalhado para erros de validação
 */
data class ValidationErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null,
    val fields: List<FieldError> = emptyList()
)

