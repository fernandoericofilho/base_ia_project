# Design: fechar a conformidade do CRUD de referência (Task) com o CLAUDE.md

**Data**: 2026-07-22
**Status**: aprovado, aguardando plano de implementação

## Contexto

Este projeto é um template didático: alunos vão pedir ao Claude Code para gerar features novas (ex.: "cadastro
de alunos") usando os agents (`.claude/agents/backend.agent.md`) e skills (`/refine`) já configurados. A feature
Task (`TaskController` → `TaskService` → `TaskRepository`, migration `V2__create_task.sql`) é o **molde de
referência** que essas gerações vão copiar como padrão — não é "a aplicação" em si.

Uma auditoria encontrou pontos onde o molde diverge do que o próprio `CLAUDE.md` e `docs/data/SCHEMA.md`
prometem, mais uma lacuna de arcabouço (Postgres real via Docker). Como toda feature nova gerada a partir do
molde herda seus defeitos, corrigir esses pontos agora evita que o problema se multiplique a cada CRUD gerado
depois.

## Escopo

1. **JaCoCo com piso de cobertura de 80%** — hoje não existe nenhum plugin de cobertura no `build.gradle.kts`,
   apesar do `CLAUDE.md` prometer "coverage floor enforced by the build".
2. **Optimistic locking sem mapeamento HTTP 409** — `Task` tem `@Version`, e `docs/data/SCHEMA.md` (linha 89) já
   documenta "conflito de versão deve mapear para HTTP 409", mas `GlobalExceptionHandler` não tem handler para
   `ObjectOptimisticLockingFailureException`. Hoje esse conflito cairia no handler genérico → 500.
3. **Guard de status terminal duplicado** — `TaskService.complete()` e `.deactivate()` repetem o mesmo
   `if (task.status in closedTaskStatuses)`, violando a regra explícita do `CLAUDE.md`: "not a duplicated `if`
   per service method".
4. **`docs/architecture/REGRAS-DO-SISTEMA.md` vazio** — ainda é só o template (placeholders "escreva aqui..."),
   mesmo a feature Task já implementando guards, soft delete, e (depois deste trabalho) optimistic locking e
   cobertura mínima. É a violação mais grave porque é a disciplina que o `CLAUDE.md` mais enfatiza, e o primeiro
   documento que os agents/skills devem consultar antes de gerar uma feature nova.
5. **Sem Docker Compose / Postgres real para desenvolvimento** — hoje Postgres só aparece via Testcontainers, só
   durante `integrationTest`. Não existe um jeito de o aluno subir Postgres real localmente e rodar a aplicação
   (`bootRun`) contra ele — só H2 em memória. Um template "modelo" que vai virar CRUDs reais gerados por pedido do
   aluno deveria ter esse arcabouço completo desde o início (Flyway + Postgres real via `docker compose`), não só
   H2 + Postgres-só-em-teste.

Fora de escopo: idempotência de criação (`TaskController.create`) — decisão explícita de **não implementar**,
documentada como tal no item 4 (Task não é uma operação de alto risco de retry como pagamento; a doc vai registrar
o motivo em vez de deixar a ausência implícita).

## Componentes e mudanças

### 1. JaCoCo (`build.gradle.kts`)
- Plugin `jacoco` (versão default do Gradle/Spring Boot, sem pin explícito de versão salvo se necessário).
- Task `jacocoTestReport` rodando depois de `test` (não de `integrationTest`, que depende de Docker).
- Task `jacocoTestCoverageVerification` com regra `minimum = 0.80` sobre `INSTRUCTION` (cobertura de linha/instrução).
- `tasks.check` passa a depender de `jacocoTestCoverageVerification`, então `./gradlew build` falha se a cobertura
  cair abaixo do piso.
- Exclusões de pacote/arquivo (sem lógica de negócio a testar): `**/models/**`, `**/dtos/**`,
  `**/controllers/request/**`, `**/controllers/response/**`, `**/config/**`, `**/exceptions/**`,
  `ModeloApplication.kt`. Controllers e services permanecem dentro da régua.

### 2. Handler de optimistic locking (`GlobalExceptionHandler.kt`)
- Novo `@ExceptionHandler(ObjectOptimisticLockingFailureException::class)`, log `action=optimistic_lock_conflict
  status=conflict`, retorna `ErrorResponse` com `status = 409`.
- Novo teste em `GlobalExceptionHandlerTest.kt`: `TEST-ERROR-04` — simula
  `ObjectOptimisticLockingFailureException` lançada por um controller de teste e verifica HTTP 409 + corpo
  `ErrorResponse`.

### 3. Guard único em `TaskService.kt`
- Extrair função privada (ex.: `private fun Task.requireOpen()` ou `private fun guardNotClosed(task: Task)`) que
  contém o `if (task.status in closedTaskStatuses) throw TaskOperationException(...)`.
- `complete()` e `deactivate()` chamam essa função única antes de aplicar a mudança de estado.
- Sem mudança de comportamento observável — testes existentes de `TaskServiceTest` (que já cobrem os cenários de
  conflito em ambos os métodos) continuam passando sem alteração de asserts.

### 4. `docs/architecture/REGRAS-DO-SISTEMA.md`
Preencher as seções do template com o que a feature Task já implementa (ou passa a implementar neste trabalho):
- **2.1 Guards de domínio**: guard de status terminal da Task (`OPEN`/`DONE`/`CANCELLED`), único ponto de checagem
  (`requireOpen`/`guardNotClosed`), `TaskOperationException` → 422.
- **2.2 Idempotência e isolamento transacional**: registrar a decisão de **não** usar chave de idempotência em
  `POST /api/v1/tasks` — não é uma operação de retry de alto risco (ao contrário de pagamento/estorno) — e o
  optimistic locking (`@Version`) como mecanismo de proteção contra escrita concorrente em `complete`/`deactivate`.
- **3. Ciclo de vida dos status**: máquina de estados `OPEN → DONE`, `OPEN → CANCELLED` (via desativação),
  nenhuma transição permitida a partir de `DONE`/`CANCELLED`.
- **5. Tratamento de erros**: tabela local replicando (ou referenciando) a tabela do `CLAUDE.md`, com a entrada
  nova `ObjectOptimisticLockingFailureException → 409` marcada como implementada nesta mudança.
- **11. Testes**: piso de cobertura 80% via JaCoCo, separação `test`/`integrationTest`, convenção `TEST-<AREA>-NN`.
- **4. Banco de dados**: registrar que existem dois caminhos de execução — H2 em memória (default, `application.yml`)
  e Postgres real via `docker compose up -d` + profile `postgres` — e que Flyway roda em ambos.

Cada entrada recebe a data `2026-07-22` ao lado, conforme o workflow do próprio documento.

### 5. Docker Compose + profile Postgres (`docker-compose.yml`, `application-postgres.yml`)
- `docker-compose.yml` na raiz: um serviço `postgres` (imagem `postgres:16-alpine`), variáveis de ambiente
  `POSTGRES_DB=modelo`, `POSTGRES_USER=app`, `POSTGRES_PASSWORD=app`, porta `5432:5432`, volume nomeado para
  persistir dados entre `docker compose down`/`up`.
- Novo `src/main/resources/application-postgres.yml`: profile Spring que sobrescreve `spring.datasource.*` para
  apontar pro Postgres do compose (`jdbc:postgresql://localhost:5432/modelo`, driver `org.postgresql.Driver`,
  dialect `PostgreSQLDialect`), mantendo `spring.flyway.enabled=true` e as mesmas `locations` — as migrations
  (`V1`, `V2`) já são portáveis entre H2 (modo PostgreSQL) e Postgres real, sem mudança nelas.
- Adiciona `runtimeOnly("org.postgresql:postgresql")` em `dependencies` no `build.gradle.kts` — hoje o driver
  Postgres só existe em `testRuntimeOnly` (usado pelo Testcontainers); precisa também em runtime principal para
  este profile funcionar fora de teste.
- `docs/guides/DEVELOPER_GUIDE.md` ganha uma seção "Rodando com Postgres real (Docker)": `docker compose up -d` →
  `./gradlew bootRun --args='--spring.profiles.active=postgres'` → Flyway aplica as migrations no Postgres real
  automaticamente na subida.
- `README.md`: uma linha no Quick Start mencionando a opção Postgres real, apontando pro guia.
- `bootstrap.sh`: sem mudança nesta rodada — fica documentado como passo manual; automatizar um flag `--postgres`
  fica pra depois, se algum aluno pedir (evita inflar o script agora).

## Testes

- `TaskServiceTest`: sem novo cenário obrigatório (guard já testado nos dois métodos); só refatoração interna.
- `GlobalExceptionHandlerTest`: `TEST-ERROR-04` novo, cobrindo o 409 de optimistic locking.
- Rodar `./gradlew build` ao final: deve compilar, testes passarem, e `jacocoTestCoverageVerification` passar com
  a cobertura real do módulo ≥ 80% — se não passar, cobrir o gap com testes adicionais antes de finalizar (não
  abaixar o piso para acomodar o código existente).
- Verificação manual do item 5 (não é teste automatizado — Postgres via Docker não roda no `./gradlew test`):
  `docker compose up -d`, depois `./gradlew bootRun --args='--spring.profiles.active=postgres'`, confirmar nos
  logs que o Flyway aplicou `V1` e `V2` no Postgres real, e que `POST /api/v1/tasks` funciona contra ele.

## Ordem de implementação sugerida

1. Guard único em `TaskService` (menor risco, isolado).
2. Handler de optimistic locking + teste.
3. JaCoCo + rodar para ver cobertura real; adicionar testes que faltarem para bater 80%.
4. Docker Compose + profile `postgres` + driver runtime + doc no `DEVELOPER_GUIDE.md`/`README.md`.
5. Preencher `REGRAS-DO-SISTEMA.md` por último, documentando o estado final já implementado (não o planejado),
   incluindo a seção 4 (banco de dados) com os dois caminhos de execução.

## Fora de escopo (não fazer neste trabalho)

- Testar o fluxo de geração de uma feature nova do zero (ex.: pedir pro Claude gerar "cadastro de alunos") — foi
  descartado no brainstorming anterior; pode virar um design separado depois.
- OpenAPI/Swagger, profiles de ambiente dev/test/prod completos (o profile `postgres` deste design troca só o
  backend de banco, não é um profile de ambiente de deploy), CI/CD — backlog já registrado em
  `docs/technical/ARCHITECTURE_AND_IMPROVEMENTS.md`, não relacionado a este design.
