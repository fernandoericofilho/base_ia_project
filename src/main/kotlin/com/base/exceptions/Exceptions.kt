package com.base.exceptions

/**
 * Exceção base para erros de negócio
 * Permite retornar status HTTP customizado e mensagem clara
 */
open class BusinessException(
    val statusCode: Int = 400,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Exceção para validação de entrada
 */
class ValidationException(
    message: String,
    cause: Throwable? = null
) : BusinessException(
    statusCode = 400,
    message = message,
    cause = cause
)

/**
 * Exceção para recurso não encontrado
 */
class ResourceNotFoundException(
    message: String,
    cause: Throwable? = null
) : BusinessException(
    statusCode = 404,
    message = message,
    cause = cause
)

/**
 * Exceção para operações não autorizadas
 */
class UnauthorizedException(
    message: String,
    cause: Throwable? = null
) : BusinessException(
    statusCode = 401,
    message = message,
    cause = cause
)

/**
 * Exceção para operações proibidas
 */
class ForbiddenException(
    message: String,
    cause: Throwable? = null
) : BusinessException(
    statusCode = 403,
    message = message,
    cause = cause
)

