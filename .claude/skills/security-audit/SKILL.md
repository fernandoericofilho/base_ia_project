---
description: Auditoria de segurança adversarial — OWASP Top 10, falhas silenciosas, injeção, exposição de dados sensíveis e vulnerabilidades em fluxos críticos
---

# /security-audit — Auditoria de Segurança

## Quando usar
Invoque em dois cenários principais:
1. Antes de colocar em produção qualquer funcionalidade que manipule dados sensíveis (financeiros, documentos pessoais, credenciais) ou autenticação
2. Como auditoria periódica do codebase (trimestral ou antes de grandes releases)

O Claude deve sugerir proativamente quando o usuário implementar endpoints que recebam dados sensíveis ou realizem operações críticas.

## Agentes envolvidos

### Security Auditor (`agentType: "security-auditor"`)
Revisão adversarial com foco em:

**OWASP Top 10 no contexto do stack do projeto**
- SQL/JPQL/HQL Injection via parâmetros não parametrizados
- Mass assignment (campos não filtrados no mapeamento Request → Entity)
- Broken access control (endpoint sem validação de ownership — ex.: usuário acessando recurso de outro usuário)
- Exposição de dados sensíveis em responses (documentos completos, números de conta, senhas em hash fraco)
- Injeção via campos string sem sanitização (XML/JSON/comando)

**Dados sensíveis**
- Documentos/identificadores pessoais retornados sem mascaramento em API pública, quando a regra do projeto exige mascaramento
- Valores financeiros com arredondamento incorreto (tipo de ponto flutuante causando perda de precisão)
- Logs contendo dados pessoais ou credenciais

**Validação de entrada**
- Campos sem validação adequada (`@NotNull`/`@NotBlank`/`@Size` ou equivalente do stack)
- Valores negativos em campos que não deveriam aceitar (sem `@Positive`/`@DecimalMin` ou equivalente)
- Datas/estados inválidos aceitos onde não deveriam

**Dependências**
- Dependências com CVEs conhecidos (verificar arquivo de build/lockfile do projeto)

### Silent Failure Hunter (`agentType: "silent-failure-hunter"`)
Complementa a auditoria focando em:
- Exceções de fluxo crítico sendo engolidas silenciosamente
- Fallbacks que mascaram falhas de pagamento, contabilidade ou qualquer operação irreversível
- Catch blocks que retornam `null`/valor padrão onde uma exceção seria mais correta

## Fluxo de execução

1. Execute os dois agentes em paralelo via Workflow
2. Deduplicação: mergear achados sobrepostos dos dois agentes
3. Classificar por severidade: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`
4. `CRITICAL` e `HIGH` bloqueiam deploy — devem ser corrigidos imediatamente
5. `MEDIUM` e `LOW` viram itens de backlog com prazo

## Regras

- CRITICAL: acesso sem autenticação a dados sensíveis, SQL injection, dado pessoal em texto puro em log
- HIGH: mass assignment, tipo de ponto flutuante em campo financeiro, falha silenciosa em fluxo crítico
- Nunca avançar com `CRITICAL` ou `HIGH` abertos
- Relatório deve incluir o snippet de código vulnerável + exemplo de exploit + sugestão de correção
