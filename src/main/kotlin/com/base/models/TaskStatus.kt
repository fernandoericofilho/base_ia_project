package com.base.models

/**
 * Status possíveis de uma Task (item de tarefa).
 * Transições permitidas: OPEN -> DONE, OPEN -> CANCELLED.
 * DONE e CANCELLED são status terminais/fechados.
 */
enum class TaskStatus {
    OPEN,
    DONE,
    CANCELLED
}
