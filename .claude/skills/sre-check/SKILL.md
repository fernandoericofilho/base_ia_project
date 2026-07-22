---
description: Revisão de observabilidade e operação pelo SRE após implementar nova funcionalidade — logs, alertas, health, resiliência e SLI/SLO
---

# /sre-check — Revisão de Observabilidade pelo SRE

## Quando usar
Invoque após finalizar a implementação de um novo endpoint, serviço ou fluxo crítico (pagamentos, integrações externas, processamento assíncrono).
O Claude deve sugerir proativamente quando o usuário indicar que terminou de implementar algo que envolva fluxo crítico de negócio ou integração externa.

## Agente envolvido

### Platform SRE (`agentType: "sre"`)
Responsável pela revisão de observabilidade e operação:

**Logging**
- Todos os caminhos de sucesso têm log de sucesso (`action=... status=ok ...`)?
- Todos os caminhos de erro têm log de warning/erro?
- Formato estruturado do projeto respeitado (ex.: `action=verb_noun status=ok|error id={} campo={}`)
- Dados sensíveis não estão sendo logados (documentos completos, valores sigilosos, senhas)
- Stack traces não estão sendo logados em nível `info` (apenas `error`/`warn`)

**Resiliência**
- Integrações externas (adapters, notificações) têm tratamento de falha sem derrubar o fluxo principal
- Operações críticas são atômicas — falha parcial não deixa estado inconsistente
- Timeouts configurados para chamadas externas

**Operação**
- Novo endpoint aparece corretamente na documentação de API (Swagger/OpenAPI ou equivalente)
- Migrações foram testadas com dados reais (não apenas schema vazio)
- Endpoint de health não foi afetado
- Sem `println`/`console.log` de debug esquecidos no código

**SLI/SLO (para fluxos críticos)**
- Identifica quais métricas seriam relevantes para monitorar o novo fluxo
- Aponta se alguma operação poderia ser lenta em produção (N+1 queries, full table scan)
- Sugere índices de banco ausentes que impactariam performance em escala

## Fluxo de execução

1. Execute o agente `sre` sobre o código implementado (controllers, services, adapters)
2. O agente retorna achados com severidade: `BLOCKER`, `WARNING`, `SUGGESTION`
3. Apresente ao usuário com priorização por impacto operacional
4. BLOCKERs devem ser corrigidos antes do deploy

## Regras

- BLOCKER: operação crítica sem log de sucesso/falha, dado sensível logado, estado inconsistente possível
- WARNING: endpoint sem documentação de API, log de stack trace em nível errado
- SUGGESTION: oportunidade de métrica, índice de performance
- Nunca aceitar log de debug esquecido em código de produção
