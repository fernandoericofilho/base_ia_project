package com.base.api.error

import com.base.exceptions.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

/**
 * Tratamento centralizado de exceções
 * Fornece respostas padronizadas para todos os erros da aplicação
 */
@ControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Trata erros de validação de input (Jakarta Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ValidationErrorResponse> {
        log.warn("action=validation_error status=invalid_input path={}", request.getDescription(false))

        val fieldErrors = ex.bindingResult.fieldErrors.map { error ->
            FieldError(
                field = error.field,
                message = error.defaultMessage ?: "Invalid value",
                rejectedValue = error.rejectedValue
            )
        }

        val response = ValidationErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "Validation failed for ${fieldErrors.size} field(s)",
            path = request.getDescription(false).removePrefix("uri="),
            fields = fieldErrors
        )

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    /**
     * Trata exceções de negócio customizadas
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        ex: BusinessException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn(
            "action=business_error status=error code={} message={} path={}",
            ex.statusCode,
            ex.message,
            request.getDescription(false)
        )

        val httpStatus = HttpStatus.resolve(ex.statusCode) ?: HttpStatus.INTERNAL_SERVER_ERROR

        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = ex.statusCode,
            error = httpStatus.reasonPhrase,
            message = ex.message ?: "An error occurred",
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity(response, httpStatus)
    }

    /**
     * Trata conflito de concorrência otimista (@Version) — ver docs/data/SCHEMA.md,
     * seção "Concorrência otimista"
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    fun handleOptimisticLockingException(
        ex: ObjectOptimisticLockingFailureException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn(
            "action=optimistic_lock_conflict status=conflict message={} path={}",
            ex.message,
            request.getDescription(false)
        )

        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value(),
            error = HttpStatus.CONFLICT.reasonPhrase,
            message = "O recurso foi modificado por outra operação. Recarregue e tente novamente.",
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity(response, HttpStatus.CONFLICT)
    }

    /**
     * Trata todas as outras exceções não mapeadas
     */
    @ExceptionHandler(Exception::class)
    fun handleGlobalException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        log.error(
            "action=unhandled_exception status=error message={} path={} stacktrace={}",
            ex.message,
            request.getDescription(false),
            ex.stackTraceToString(),
            ex
        )

        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = "An unexpected error occurred",
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

