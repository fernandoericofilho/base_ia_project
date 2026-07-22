package com.base.exceptions

/**
 * Exceção de domínio lançada quando uma operação é solicitada em uma Task
 * que já está em status terminal/fechado (DONE ou CANCELLED).
 * Mapeada para HTTP 422 pelo GlobalExceptionHandler (via BusinessException.statusCode).
 */
class TaskOperationException(
    message: String,
    cause: Throwable? = null
) : BusinessException(
    statusCode = 422,
    message = message,
    cause = cause
)
