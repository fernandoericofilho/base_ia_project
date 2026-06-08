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
- `agents/` — agents para revisão (PO, Backend, QA, DBA, SRE, TechLead, Frontend, AI)
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

## Material recomendado para começar

1. `AGENTS_README.md` — visão geral dos agents e roteiro de aula
2. `PLAYBOOK_PR.md` — roteiro de PR e checklist para exercícios
3. `AGENTS_PROMPTS.md` — prompts prontos para cada agent
4. `src/main/kotlin/com/base_project/modelo/` — código didático (HelloController, HelloService, Mapper, Entity)


