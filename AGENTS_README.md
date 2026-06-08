# Guia rápido — Agents e Roteiro para Aula

Este repositório contém um projeto-modelo didático (Hello World persistido) e um conjunto de agentes em `./agents` destinados a orientar code reviews, decisões arquiteturais e exercícios práticos.

Objetivo
- Fornecer um guia simples para alunos entenderem o fluxo Controller → DTO → Service → Entity → Repository
- Mostrar como usar arquivos de agente como checklists e prompts para revisão de código e decisões técnicas
- Permitir inicializar e testar rapidamente o projeto-modelo

Onde estão os agents
- `base_project/agents/`
  - `backend.agent.md` — Backend Principal Engineer
  - `techlead.agent.md` — Staff Tech Lead
  - `po.agent.md` — Product Owner Tech
  - `qa.agent.md` — Senior QA Engineer
  - `dba.agent.md` — Senior DBA
  - `architect.agent.md` — Solution Architect
  - `frontend.agent.md` — Frontend Principal Engineer
  - `sre.agent.md` — Platform SRE
  - `ai.agent.md` — AI Engineer

Como usar um agent (exemplo rápido)
1. Abra o arquivo do agent relevante, leia suas prioridades e checklist.
2. Monte um prompt curto para o review. Exemplo para backend:

```
You are the Backend Principal Engineer. Evaluate the following pull request focusing on: constructor injection, DTO↔Entity mapping, repository usage, transactions, naming, and tests. Provide a short checklist and 3 suggested fixes.
```

3. Peça ao agent que gere observações curtas e objetivas (bullet points) para a revisão.

Boas práticas ao usar agents
- Use os agents como *checklists*, não como substituto do raciocínio humano.
- Prefira respostas curtas e acionáveis (máx. 8 bullets).
- Proteja dados sensíveis; não envie credenciais ao agent.

Roteiro de aula sugerido (90 minutos)

1) 0–10 min — Introdução
  - Objetivos da aula e apresentação do projeto-modelo

2) 10–30 min — Arquitetura em camadas
  - Explicar fluxo: Request → Controller → DTO → Service → Entity → Repository
  - Mostrar exemplos de classes em `base_project/src/main/kotlin`

3) 30–50 min — Hands-on: executar testes e explorar o código
  - Rodar o script `bootstrap.sh` para executar testes
  - Localizar a entidade, repositório e service

4) 50–75 min — Exercício de code review com agents
  - Dividir em duplas
  - Cada dupla cria 2 PRs pequenos (ex.: adicionar validação, ajustar log)
  - Trocar PRs e usar agents apropriados (`backend`, `qa`, `dba`) para gerar checklist

5) 75–90 min — Debrief
  - Comparar sugestões dos agents com a revisão humana
  - Discussão sobre decisões e trade-offs

Exercício prático proposto
- Objetivo: adicionar uma pequena validação no Service e cobertura de testes.
- Passos:
  1. Implementar validação no `Service` (usar DTO)
  2. Mapear em `Mapper` para `Entity`
  3. Adicionar teste unitário (Mockito-Kotlin)
  4. Rodar `./bootstrap.sh` e corrigir falhas

Script de bootstrap
- `bootstrap.sh` (na raiz de `base_project`) realiza:
  - `./gradlew clean test` no módulo `base_project`
  - (opcional) `./gradlew bootRun` se chamado com `--run`

Uso rápido (na máquina dos alunos):

```bash
cd base_project
./bootstrap.sh          # roda testes
./bootstrap.sh --run    # roda testes e, se OK, inicia a aplicação
```

Observações finais
- Os agents são templates de orientação — ajuste os prompts conforme o contexto do PR.
- Evite expor dados sensíveis ao usar agents automatizados.
- Para aulas em que o projeto tocar DB real, prefira Testcontainers (já sugerido no guideline) ou executar apenas testes unitários.

Contato / dúvidas
- Abra uma issue no repositório principal ou envie mensagem ao instrutor com o título `Aula - Agents`.

