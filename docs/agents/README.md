# 🤖 Agents — Guia e Prompts para Code Review

Este documento contém tudo que você precisa saber para usar agents IA para revisar código neste projeto.

## O que são Agents?

Agents são **personas IA configuradas** (arquivos em `agents/`) que representam diferentes papéis em um time de engenharia:
- Backend Engineer
- QA Engineer
- DBA
- Tech Lead
- Architect
- SRE
- Frontend Engineer
- Product Owner
- AI Engineer

## Como Funcionam

1. Você tem um código/PR para revisar
2. Escolhe o agent mais relevante
3. Copia o **prompt pronto** deste documento
4. Envia o código + prompt para um LLM (ChatGPT, Claude, etc)
5. Recebe recomendações estruturadas

## Prompts Prontos (Copie e Cole)

### Backend Principal Engineer
```
Você é o Backend Principal Engineer. Revise as mudanças focando em: 
- Injeção por construtor
- Mapeamento DTO↔Entity
- Uso de repositório
- Transações (@Transactional)
- Logs estruturados (action=, status=)
- Validação
- Testes

Retorne um checklist de 6 itens e até 3 correções concretas.

[CÓDIGO AQUI]
```

### Staff Tech Lead
```
Você é o Staff Tech Lead. Avalie impacto arquitetural, compatibilidade retroativa, 
deployabilidade e acoplamento. 

Forneça: 
- Decisão (APROVADO/REPROVADO)
- 2–3 justificativas concisas
- Risco principal
- Mitigações

[CÓDIGO AQUI]
```

### Product Owner Tech
```
Você é o Product Owner Tech. Valide o escopo, critérios de aceite e valor de negócio 
desta mudança. 

Liste:
- Critérios de aceite (3–6)
- Dependências
- Métrica de sucesso mensurável

[MUDANÇA AQUI]
```

### Senior QA Engineer
```
Você é o Senior QA Engineer. Produza os casos de teste de maior risco.

Liste:
- 2 testes unitários mínimos
- 1 teste de integração
- Casos de borda críticos (idempotência, recuperação)
- Cenários de erro

[CÓDIGO AQUI]
```

### Senior DBA
```
Você é o Senior DBA. Revise alterações de schema e migrations. 

Liste:
- Índices necessários
- Constraints FK
- Riscos da migration
- Plano de rollback
- Impacto em performance

[MUDANÇA DE SCHEMA AQUI]
```

### Solution Architect
```
Você é o Solution Architect. Para a solução proposta:

Apresente:
- 3 alternativas viáveis (bullets)
- Solução recomendada
- Trade-offs
- Matriz de impacto (acoplamento, custo, complexidade)

[PROBLEMA/SOLUÇÃO AQUI]
```

### Frontend Principal Engineer
```
Você é o Frontend Principal Engineer. Revise limites de componentes, 
gerenciamento de estado e contratos de API.

Retorne um checklist:
- Acessibilidade
- Estados de erro
- Estados de loading
- Tipagem
- Sugestões de correção

[CÓDIGO FRONTEND AQUI]
```

### Platform SRE
```
Você é o Platform SRE. Para a mudança proposta:

Liste:
- Observabilidade necessária
- Impacto em SLI/SLO
- Alertas requeridos
- Passos do runbook para incidentes

[MUDANÇA AQUI]
```

### AI Engineer
```
Você é o AI Engineer. Avalie se a mudança exige ML/embeddings ou uma busca simples.

Se RAG for proposto, indique:
- Estratégia de chunking
- Escolha de armazenamento vetorial
- Estimativa de custo de tokens

[PROBLEMA/SOLUÇÃO AQUI]
```

---

## Onde Encontrar os Agents

Todos os agents em arquivo separado:
```
agents/
├── backend.agent.md
├── techlead.agent.md
├── po.agent.md
├── qa.agent.md
├── dba.agent.md
├── architect.agent.md
├── frontend.agent.md
├── sre.agent.md
└── ai.agent.md
```

Cada arquivo contém a jornada completa, prioridades e critérios de decisão do agent.

---

## Roteiro de Aula Sugerido (90 minutos)

### 1) 0–10 min — Introdução
- Objetivos da aula
- Apresentação do projeto-modelo

### 2) 10–30 min — Arquitetura em camadas
- Explicar fluxo: Request → Controller → DTO → Service → Entity → Repository
- Mostrar exemplos em `src/main/kotlin/com/base/`

### 3) 30–50 min — Hands-on: executar e explorar
- Rodar: `./bootstrap.sh`
- Localizar: Entidade, repositório, service
- Entender: Fluxo de dados

### 4) 50–75 min — Exercício de code review com agents
- Dividir em duplas
- Cada dupla cria 2 PRs pequenos:
  - Adicionar validação no Service
  - Ajustar logs estruturados
- Trocar PRs entre duplas
- Usar agents (`backend`, `qa`, `dba`) para gerar checklist
- Comparar sugestões do agent com revisão humana

### 5) 75–90 min — Debrief
- O que o agent capturou bem?
- O que faltou?
- Como usar agents no dia a dia?

---

## Exercício Prático Proposto

### Objetivo
Adicionar uma pequena validação no Service e cobertura de testes.

### Passos

1. **Implementar validação**
   - Adicionar no `HelloService`
   - Exemplo: validar tamanho mínimo de nome

2. **Mapear em Mapper**
   - Adicionar em `HelloMapper`
   - Validar conversão DTO ↔ Entity

3. **Adicionar teste**
   - Criar `test/kotlin/com/base/services/HelloServiceValidationTest.kt`
   - Usar Mockito-Kotlin

4. **Rodar testes**
   - `./bootstrap.sh`
   - Corrigir falhas

5. **Usar agent para revisar**
   - Copie o prompt **Backend Principal Engineer**
   - Envie seu código
   - Receba feedback estruturado

---

## Boas Práticas ao Usar Agents

✅ **Use agente como checklists**, não como substituto do raciocínio humano
✅ **Prefira respostas curtas** e acionáveis (máx. 8 bullets)
✅ **Ajuste prompts** conforme seu contexto específico
✅ **Proteja dados sensíveis** — não envie credenciais
✅ **Compare** sugestões do agent com revisão humana

❌ **Não aceite cegamente** recomendações do agent
❌ **Não confundir** qualidade de code review com qualidade de código
❌ **Não aprofundar** em tecnologia sem justificativa do agent

---

## Fluxo Recomendado para PRs

1. Você implementa mudança
2. Você roda: `./bootstrap.sh` (localmente)
3. Você abre PR
4. Você escolhe 2-3 agents relevantes
5. Para cada agent:
   - Copia prompt deste documento
   - Envia código + prompt para LLM
   - Coloca resposta no comentário do PR
6. Você ajusta código baseado em feedback
7. Merge quando OK

---

## Observações Finais

- Os agents são **templates de orientação** — ajuste conforme seu contexto
- Para projetos grandes, considere **rodar agentes em paralelo** (backend, QA, DBA)
- Para aulas, **distribua agentes por dupla** (cada grupo pega um)
- **Integre com seu workflow de PR** — adicione comentários automáticos

---

## Contato / Dúvidas

- Abra issue no repositório
- Ou envie mensagem ao Tech Lead com título: `Aula - Agents`

---

Bora revisar código com IA! 🚀

