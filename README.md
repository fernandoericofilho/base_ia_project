<!--
  README do projeto-modelo (base_project)
  Idioma: Português (PT-BR)
  Objetivo: material didático para Controller → Service → Repository
-->

# Projeto-modelo (base_project)

Este repositório é um projeto didático mínimo que ilustra a arquitetura em camadas adotada no AgioMix e serve como base para exercícios em sala de aula.

## Principais objetivos

- Fornecer um exemplo executável (Hello World persistido) com camadas bem definidas
- Disponibilizar *agents* (templates de revisão) para exercícios de code review e refinamento
- Facilitar execução local (H2 em memória) e execução automática de testes com Gradle

## Fluxo didático (simplificado)

Request → Controller → DTO → Service → Entity → Repository

## Conteúdo do projeto

- `src/main/kotlin/...` — implementação didática (Controller, DTO, Mapper, Service, Entity, Repository)
- `src/main/resources/db/migration/` — migration Flyway inicial (V1__create_greeting_record.sql)
- `src/main/resources/application.yml` — configuração: H2 in-memory por padrão para aulas
- `bootstrap.sh` — script para rodar testes e opcionalmente iniciar a aplicação
- `agents/` — agents para revisão (PO, Backend, QA, DBA, SRE, TechLead, Frontend, AI), uso didático/manual
- `.claude/agents/` — os mesmos 9 agents como subagents reais do Claude Code (`agentType: "backend"`, etc.)
- `.claude/skills/` — 5 skills que orquestram os agents em fluxos multi-agente (`/refine`, `/review`, `/db-review`, `/security-audit`, `/sre-check`)
- `.claude/settings.json` — habilita o plugin `superpowers` (brainstorming, TDD, debugging sistemático)
- `CLAUDE.md` — regras obrigatórias do projeto (arquitetura, testes, naming, observabilidade) — leia e adapte antes de começar um projeto novo a partir deste template
- `AGENTS_PROMPTS.md` — prompts prontos em português para cada agent
- `PLAYBOOK_PR.md` — playbook de PR com checklist e fluxo para exercícios em sala

## Como executar (rápido)

Abra um terminal na pasta `base_project` e execute:

```bash
cd base_project
./bootstrap.sh          # executa os testes (falha se houver erro)
./bootstrap.sh --run    # executa testes e, se OK, inicia a aplicação (bootRun)
```

Observação: por padrão o projeto usa H2 em memória (ver `application.yml`). Para usar PostgreSQL local, ajuste as propriedades do datasource e do Flyway.

## Testes

- Execute `./gradlew test` ou use `./bootstrap.sh`.
- O projeto contém testes unitários e uma integração simples para exemplificar o fluxo.

## Uso dos agents em sala de aula

- Os agents ficam em `base_project/agents/` e funcionam como personas para gerar checklists e prompts de revisão.
- Leia `AGENTS_README.md` para orientações e `AGENTS_PROMPTS.md` para prompts prontos.
- Use o `PLAYBOOK_PR.md` como roteiro para exercícios: criação de branch, implementação, execução de testes, uso dos agents e merge.

## Convenções recomendadas (aplicáveis ao exercício)

- Código e nomes técnicos: inglês (classes, propriedades, colunas, endpoints)
- Documentação e comentários de domínio: português (PT-BR)
- Injeção por construtor (constructor injection)
- Logs estruturados (ex.: `action=... status=...`) para facilitar observabilidade
- Configurações em `application.yml` ou variáveis de ambiente; evitar hardcode

## Git e commits

- O diretório `base_project` contém um repositório Git local inicializado e um commit inicial com os arquivos relevantes. `build/`, caches e IDE files estão ignorados via `.gitignore`.

## Integração com Claude Code

Este projeto foi enriquecido com o que funcionou de fato em produção (extraído do projeto AgioMix) para servir de
base para qualquer projeto novo, não só para aulas:

- **`CLAUDE.md`** — leia primeiro. Documenta as regras obrigatórias (arquitetura em camadas, injeção por construtor,
  tipos numéricos corretos para valores sensíveis, soft delete, migrations backward-compatible, convenções de
  naming, observabilidade) e, mais importante, a disciplina de **documentar toda mudança de regra imediatamente**
  em `docs/architecture/REGRAS-DO-SISTEMA.md` — o hábito que mais evitou retrabalho no projeto de origem.
- **`.claude/agents/*.agent.md`** — os 9 agents em formato de subagent real do Claude Code, prontos para uso via
  `agentType`. Ajuste os nomes de domínio ao seu projeto; os princípios de engenharia já são genéricos.
- **`.claude/skills/*/SKILL.md`** — os fluxos multi-agente que usam esses agents em paralelo:
  - `/refine` antes de implementar qualquer feature nova
  - `/review` antes de qualquer merge
  - `/db-review` antes de aplicar qualquer migration
  - `/security-audit` antes de expor algo sensível
  - `/sre-check` depois de implementar um fluxo crítico
- **`.claude/settings.json`** — habilita o plugin `superpowers`, que complementa os skills acima com
  brainstorming, TDD e debugging sistemático.

Ao começar um projeto novo a partir deste template: leia o `CLAUDE.md` de ponta a ponta e ajuste os detalhes
concretos (stack, idioma, threshold de cobertura) ao novo contexto — as *formas* das regras tendem a se manter, os
*valores* específicos são só um ponto de partida.

## Material recomendado para começar

1. `AGENTS_README.md` — visão geral dos agents e roteiro de aula
2. `PLAYBOOK_PR.md` — roteiro de PR e checklist para exercícios
3. `AGENTS_PROMPTS.md` — prompts prontos para cada agent
4. `src/main/kotlin/com/base_project/modelo/` — código didático (HelloController, HelloService, Mapper, Entity)


