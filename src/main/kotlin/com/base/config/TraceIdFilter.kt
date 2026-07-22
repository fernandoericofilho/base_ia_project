package com.base.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * OBS-01: injeta um traceId no MDC para correlacionar todas as linhas de log emitidas
 * durante uma mesma requisição HTTP. Reaproveita o header de entrada `X-Trace-Id` quando
 * presente (permite correlação ponta a ponta entre serviços), ou gera um novo UUID.
 * Propaga o valor de volta no header de resposta `X-Trace-Id` e limpa o MDC ao final
 * para não vazar o valor entre requisições em threads reaproveitadas por pool.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TraceIdFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val traceId = request.getHeader(TRACE_ID_HEADER)?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
        try {
            MDC.put(TRACE_ID_MDC_KEY, traceId)
            response.setHeader(TRACE_ID_HEADER, traceId)
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY)
        }
    }

    companion object {
        const val TRACE_ID_HEADER = "X-Trace-Id"
        const val TRACE_ID_MDC_KEY = "traceId"
    }
}
