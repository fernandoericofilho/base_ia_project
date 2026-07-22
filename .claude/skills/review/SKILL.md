---
description: Revisão multi-agente de código antes de merge — Tech Lead, QA, detecção de falhas silenciosas e análise de cobertura de testes
---

# /review — Revisão de Código Pre-Merge

## Quando usar
Invoque antes de qualquer merge, abertura de PR ou ao finalizar a implementação de uma funcionalidade.
O Claude deve sugerir proativamente quando o usuário indicar que terminou a implementação.

> Este skill é genérico — os checks de cada agente devem referenciar as regras obrigatórias do `CLAUDE.md` **deste projeto específico**, não as listadas aqui como exemplo.

## Agentes envolvidos

### Tech Lead (`agentType: "techlead"`)
- Verifica aderência à arquitetura em camadas definida no `CLAUDE.md` do projeto (ex.: Controller → Mapper → Service → Repository)
- Detecta violações das **regras obrigatórias** do `CLAUDE.md` (ex.: injeção de campo proibida, tipo de dado errado em valores sensíveis, DELETE físico onde deveria ser soft delete, hardcode de configuração)
- Avalia **trade-offs** e dívida técnica introduzida

### QA Engineer (`agentType: "qa"`)
- Verifica se os **testes cobrem as regras de negócio** implementadas
- Identifica **caminhos de erro não testados** (exceções, edge cases)
- Checa aderência ao limite de cobertura definido no projeto (ex.: JaCoCo >= X%)
- Aponta **riscos de regressão** em funcionalidades existentes

### Silent Failure Hunter (`agentType: "silent-failure-hunter"`)
- Detecta **catch blocks que engolem erros** sem logar ou relançar
- Identifica **fallbacks silenciosos** (ex.: `?: "valor_padrão"` em campos críticos, `catch { null }`)
- Verifica se logs seguem o formato estruturado definido no projeto (ex.: `action=verb_noun status=ok|error`)
- Busca `try/catch` com corpo vazio ou apenas comentários

### PR Test Analyzer (`agentType: "pr-test-analyzer"`)
- Analisa se os **testes cobrem os critérios de aceitação** da feature
- Verifica se os edge cases críticos do domínio (financeiro, datas, concorrência, etc.) estão testados
- Aponta lacunas entre o código implementado e os testes escritos

## Fluxo de execução

1. Execute um **Workflow** com os quatro agentes em paralelo sobre o diff/código em questão
2. Cada agente retorna uma lista de **achados** com severidade: `BLOCKER`, `WARNING`, `SUGGESTION`
3. Sintetize em uma tabela consolidada ordenada por severidade
4. Se houver qualquer `BLOCKER`: **não avance** — o código deve ser corrigido antes do merge
5. `WARNING` e `SUGGESTION` são apresentados mas não bloqueiam

## Regras

- BLOCKERs são violações das regras obrigatórias do `CLAUDE.md` do projeto ou bugs de lógica de negócio
- Nunca aprovar código que viole uma regra marcada como obrigatória no `CLAUDE.md`
- Nunca aprovar catch vazio ou fallback silencioso em fluxo crítico
- O resultado da revisão deve ser apresentado ANTES de qualquer commit/push
