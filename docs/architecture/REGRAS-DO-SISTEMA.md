# REGRAS DO SISTEMA — [nome do projeto]

**Fonte única de verdade para as regras de negócio e técnicas do projeto.**

Este documento nasce vazio. Ele existe para ser preenchido **no momento em que cada regra é criada ou alterada** —
nunca depois, nunca "quando der tempo". Ver a seção "COMO MANTER ESTE DOCUMENTO" no final antes de escrever a
primeira linha.

---

## 1. ESTRUTURA E FÓRMULAS DE NEGÓCIO

Escreva aqui a regra de negócio (campos, fórmulas, cálculos centrais do domínio) assim que ela existir — nunca deixe
o código divergir do que está escrito aqui.

---

## 2. REGRAS TÉCNICAS — CÓDIGO

Escreva aqui a regra técnica (arquitetura em camadas, injeção de dependência, tipos monetários, soft delete,
validações de DTO, convenções de resposta HTTP) assim que ela existir — nunca deixe o código divergir do que está
escrito aqui.

### 2.1 Guards de domínio (estado terminal / bloqueio de escrita)

**Task** (2026-07-23): `complete()` e `deactivate()` em `TaskService` são bloqueados se a Task já estiver em
status terminal (`DONE` ou `CANCELLED`). O guard é um único ponto de checagem — `TaskService.requireNotClosed()`
— chamado por ambos os métodos, nunca duplicado como `if` solto em cada um. Lança `TaskOperationException` → 422.
Qualquer novo write path adicionado a `TaskService` (ex.: um futuro `reopen()` ou `rename()`) deve chamar esse
mesmo guard em vez de reimplementar a checagem.

### 2.2 Idempotência e isolamento transacional

**Task** (2026-07-23): `POST /api/v1/tasks` **não** usa chave de idempotência. Decisão explícita, não omissão —
criar uma Task não é uma operação de alto risco de retry como pagamento/estorno (reenviar o mesmo POST duas vezes
por engano só cria uma segunda Task, sem efeito colateral financeiro ou de negócio grave). Se uma feature futura
tiver semântica de retry crítico, ela precisa de chave de idempotência própria — não herdar a ausência do Task
como padrão.

**Concorrência otimista**: `Task.version` (`@Version`) protege `complete()`/`deactivate()` contra escrita
concorrente. Conflito de versão (`ObjectOptimisticLockingFailureException`) é mapeado para HTTP 409 em
`GlobalExceptionHandler.handleOptimisticLockingException()`. Toda entidade que sofre escrita concorrente
(atualização de status, saldo, etc.) deve ter `@Version` e esse mapeamento já cobre qualquer conflito dela — não
é necessário um handler por entidade.

### 2.3 Gates de commit e de build (2026-10-22)

Regras que deixam de depender de revisão humana. Cada gate existe para pegar um erro que já custou retrabalho em projetos anteriores.

- **TESTING_RULE no pre-commit.** Commit que altera `src/main/kotlin/**/services/*Service.kt` ou `**/repositories/*Repository.kt` sem alterar um teste em `src/test/kotlin/**/services|repositories/` é bloqueado. O hook fica versionado em `.githooks/pre-commit` e é ativado por `./bootstrap.sh` (`git config core.hooksPath .githooks`). Bypass (`--no-verify`) só com justificativa escrita no commit.
- **`FetchType.EAGER` bloqueado** no pre-commit (`src/main/kotlin/**/models/*.kt`) e pelo teste de arquitetura.
- **Teste desabilitado bloqueado no pre-commit.** Adicionar `@Disabled` em `src/test` é bloqueado. Se for inevitável, abrir issue com data e dono antes.
- **Teste de arquitetura (ArchUnit)**, `src/test/kotlin/com/base/architecture/ArchitectureTest.kt`, roda no `./gradlew test`. Regras:
  - Sem injeção por campo (`@Autowired` em propriedade): só construtor.
  - Sem `Double` ou `Float` em `models` e `dtos`: valor sensível usa `BigDecimal`.
  - `@ManyToOne` e `@OneToOne` com `FetchType.LAZY`.
  - Controller não acessa Repository.
  - Service não depende de Controller.
- **Piso de cobertura no build.** JaCoCo `jacocoTestCoverageVerification` faz parte do `check`. Piso de instrução: **80%** (`jacocoTestCoverageVerification`, mantido pelo remoto; ver §11). O piso sobe quando a cobertura sobe; nunca desce para passar build. Excluídos: `ModeloApplication`, `models`, `dtos`, `controllers`, `exceptions`, `config`.
- **Fins de linha.** `.gitattributes` força LF em `*.sh`, `gradlew` e `.githooks/*`. Script com CRLF quebra no Linux (`exit 127`).
- **CI.** `.github/workflows/ci.yml` roda `./gradlew build` e `./gradlew integrationTest` em todo push e pull request. CI não é desligado para passar: teste quebrado se corrige.

---

## 3. CICLO DE VIDA DOS STATUS

**Task** (2026-07-23, atualizado 2026-07-23): `TaskStatus` tem três valores — `OPEN` (inicial, toda Task nasce
assim), `DONE`, `CANCELLED`. Transições implementadas: `OPEN → DONE` via `POST /api/v1/tasks/{id}/complete`,
`OPEN → CANCELLED` via `POST /api/v1/tasks/{id}/cancel`. `DONE` e `CANCELLED` são terminais: nenhuma transição
sai deles (guard de 2.1 bloqueia, incluindo o próprio `cancel` — não dá pra cancelar uma Task já `DONE`).
Desativação (`DELETE /api/v1/tasks/{id}`, soft delete) é ortogonal ao `status` — não muda o status, só marca
`active = false`.

---

## 4. BANCO DE DADOS

- **Flyway + PostgreSQL (2026-10-22).** Flyway 10+ traz o suporte ao PostgreSQL no módulo `org.flywaydb:flyway-database-postgresql`, dependência do projeto. Sem ele, o `integrationTest` falha com `Unsupported Database: PostgreSQL 16.x`.

Convenções de migration, nomenclatura e checklist completo vivem em `docs/data/SCHEMA.md` (que também descreve o
schema atual tabela por tabela) — este documento não duplica aquele conteúdo.

**Profiles de execução** (2026-07-23, atualizado com `dev`/`test`/`prod` em 2026-07-23):
- **Default (dia a dia / aula)**: H2 em memória, `application.yml`, sem dependência externa.
- **`dev`**: mesmo H2 do default, só ativa log SQL do Hibernate (`application-dev.yml`).
- **`postgres`** (opcional): `docker compose up -d` sobe um Postgres 16 local (ver `docker-compose.yml`), e
  `./gradlew bootRun --args='--spring.profiles.active=postgres'` ativa `application-postgres.yml`, que só troca o
  datasource — as mesmas migrations Flyway (`V1`, `V2`, ...) rodam em todos os caminhos sem alteração.
- **`test`**: mesmo H2, log enxuto — pensado para subir a aplicação (não os testes JUnit) num pipeline de CI.
- **`prod`**: Postgres via variáveis de ambiente obrigatórias (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`,
  `DB_PASSWORD`) — `application-prod.yml` não define nenhum valor default de credencial, seguindo a regra "No
  hardcoded values". `management.endpoint.health.show-details` vira `never` neste profile (nunca expor detalhe
  de health check publicamente em produção).
- Testes de integração (`./gradlew integrationTest`) usam um caminho à parte, Postgres efêmero via Testcontainers
  — independente do container do `docker-compose.yml`. **Limitação conhecida**: falha em Windows com Docker
  Desktop usando `npipe` nativo (erro de negociação de versão de API) — ver troubleshooting em
  `docs/guides/DEVELOPER_GUIDE.md`.

---

## 5. TRATAMENTO DE ERROS

Mapeamento aplicado por `GlobalExceptionHandler` (`src/main/kotlin/com/base/api/error/GlobalExceptionHandler.kt`),
atualizado em 2026-07-23:

| Exceção | HTTP | Observação |
|---|---|---|
| `MethodArgumentNotValidException` (Bean Validation) | 400 | resposta detalhada por campo (`ValidationErrorResponse`) |
| `ResourceNotFoundException` (`BusinessException`, statusCode=404) | 404 | ex.: Task não encontrada |
| `TaskOperationException` (`BusinessException`, statusCode=422) | 422 | guard de status terminal (seção 2.1) |
| `ObjectOptimisticLockingFailureException` | 409 | conflito de `@Version` (seção 2.2) |
| Qualquer outra `Exception` não mapeada | 500 | stack trace só no log, nunca no corpo da resposta |

Toda nova exceção de domínio deve estender `BusinessException` com o `statusCode` correto — não criar um
`@ExceptionHandler` novo a menos que o tipo não seja uma `BusinessException` (como no caso do optimistic lock).

---

## 6. OPERAÇÕES COMUNS DE DOMÍNIO

Escreva aqui o fluxo detalhado de cada operação de escrita relevante do domínio (criação, atualização, mudança de
status, estorno/cancelamento) — entradas, guards, efeitos colaterais, eventos gerados — assim que ela existir —
nunca deixe o código divergir do que está escrito aqui.

---

## 7. CAMPOS OBRIGATÓRIOS E IMUTÁVEIS

Escreva aqui, em formato de tabela, todo campo obrigatório, seu limite de tamanho, sua validação de backend e seu
comportamento de frontend, além de todo campo que se torna imutável após a criação de um registro — assim que essa
regra existir — nunca deixe o código divergir do que está escrito aqui.

---

## 8. FRONTEND

Não há frontend neste template. A referência completa está em `docs/frontend/FRONTEND.md`, com CSS de referência em
`docs/frontend/styles/`. Regras que valem para qualquer app Angular criado a partir daqui (2026-10-22):

- **Fonte única**: `--app-font` é a única declaração do nome da fonte. Cor de status vem de `--app-success/danger/warning/info`.
- **Erro de validação inline** (`mat-error`). Toast é só para resultado de ação.
- **Data**: nunca `<input type="date">`. Campo de data com calendário e digitação `dd/mm/aaaa`; valor interno `AAAA-MM-DD`.
- **Contêiner `.page`** definido uma vez (global). Nenhuma tela redefine largura.
- **Listagem em cards de 3 linhas** (nome + status / código + valor / legenda). Sem `mat-table` em lista de cadastro.
- **Botão de ícone sempre com tooltip.** `confirm()` nativo do navegador é proibido.
- **Ação proibida não aparece** (estado terminal ou permissão). O backend continua bloqueando.
- **IDs vindos da API** comparados como string (`String(a) === String(b)`).

---

## 9. NOMENCLATURA E LOGGING

Escreva aqui a tabela de convenção de idioma por contexto (código, mensagens de usuário, colunas de banco,
comentários) e o formato padrão de log assim que existirem — nunca deixe o código divergir do que está escrito
aqui.

---

## 10. OBSERVABILIDADE

**Endpoints** (2026-07-23): `/actuator/health`, `/actuator/prometheus`, `/actuator/metrics` — expostos em
`application.yml`. Em `prod`, `management.endpoint.health.show-details` é `never` (nunca detalhar publicamente).

**Timers e counters por operação** (2026-07-23): toda operação de escrita de `TaskService` passa por
`withMetrics(action, block)`, que registra `task.<action>.timer` (duração) e `task.<action>.count` (contador,
tag `status=ok|error`) — `task.create.*`, `task.complete.*`, `task.cancel.*`, `task.deactivate.*`. Nenhum método
deve instrumentar métrica manualmente fora desse helper único (mesmo princípio do guard único da seção 2.1: um
ponto de instrumentação, não um por método). Verificado manualmente em `/actuator/prometheus`
(`task_create_count_total`, `task_create_timer_seconds`, etc.).

**Rastreamento**: `TraceIdFilter` (`com.base.config`) injeta `traceId` no MDC a partir do header `X-Trace-Id`
recebido (ou gera um novo), presente em toda linha de log via `logback-spring.xml`.

**Jobs agendados**: nenhum ainda.

---

## 11. TESTES

**Gates de teste** (2026-10-22):
- **Mudança em Service ou Repository exige teste no mesmo commit** (gate do pre-commit, ver §2.3). Política em `TESTING_RULE.md`.
- **Teste de arquitetura** (ArchUnit, `ArchitectureTest`) faz parte do `./gradlew test`. Regras em §2.3.
- **Teste desabilitado não entra na branch principal** sem issue com data e dono. O pre-commit bloqueia `@Disabled` novo.

**Separação** (2026-07-23): `./gradlew test` roda testes unitários (exclui a tag `integration`);
`./gradlew integrationTest` roda os testes de integração com Postgres real via Testcontainers (requer Docker,
não bloqueia `./gradlew build`/`check`).

**Convenção de cenário**: comentário `TEST-<AREA>-NN` acima do teste, descrevendo o cenário e por que ele existe
(qual comportamento quebra se o teste for removido) — ex.: `TEST-TASK-01`, `TEST-ERROR-04`.

**Piso de cobertura**: 80% de instrução (`INSTRUCTION`/`COVEREDRATIO`), verificado por
`jacocoTestCoverageVerification` (JaCoCo), ligado à task `check` — `./gradlew build` falha se a cobertura cair
abaixo do piso. Excluídos da régua (sem lógica de negócio a testar): `models`, `dtos`, `controllers/request`,
`controllers/response`, `config`, `exceptions`, `ModeloApplication`, e os DTOs de resposta de erro
(`ErrorResponse`, `FieldError`, `ValidationErrorResponse`). Cobertura real em 2026-07-23: 91.7%.

---

## 12. SEGURANÇA

Escreva aqui as regras de credenciais, segredos e variáveis de ambiente (o que nunca pode ir para o repositório, o
que é exigido em produção) assim que existirem — nunca deixe o código divergir do que está escrito aqui.

---

## 13. BACKLOG E HISTÓRICO DO DESENVOLVIMENTO

- **Pendências** vivem em `docs/backlog/todo/`. Item concluído sai de lá e vai para `docs/backlog/concluido/` com a data (`YYYY-MM-DD`).
- **"Concluído" = código + teste + doc de regra**, no mesmo commit ou PR (2026-10-22). Entrega sem teste ou sem doc não é concluída.
- **Lições de projetos anteriores** entram como regras gerais nas personas (`.claude/agents/`) e nas skills, com data. Regra de domínio de outro projeto não entra neste template.

---

## 14. DECISÕES DE ARQUITETURA

- **2026-10-22 — Lições de projetos anteriores viram regras gerais deste template** (personas, skills, gates), e não artefatos copiados. Por quê: projetos novos partem do que já custou caro, sem herdar regra de domínio alheia. Alternativa descartada: copiar documentos de processo de outro projeto.
- **2026-10-22 — Gates no build e no commit em vez de convenção escrita** (§2.3). Por quê: regra que depende de lembrança é quebrada sem ninguém perceber. Hook versionado e teste de arquitetura quebram o build.
- **2026-10-22 — Duplicação de agentes.** Existem três cópias das personas: `.claude/agents/` (usada pelo Claude Code), `agents/` e `docs/agents/` (material de aula). As cópias já divergiram. `.claude/agents/` é a fonte de verdade para o agente; as outras duas são para aula e precisam de decisão de consolidação (pendência em `docs/backlog/todo/`).

---

## COMO MANTER ESTE DOCUMENTO

Este é o único arquivo de regras do projeto. Se o projeto crescer a ponto de precisar separar regras por público
(ex.: negócio vs. técnico) ou por subdomínio, documente aqui a decisão de dividir e onde cada parte passou a viver —
nunca crie um documento paralelo sem registrar isso.

### Antes de Implementar
1. Procure se a regra já existe neste documento.
2. Se não existir, **escreva-a aqui primeiro**.
3. Se existir mas estiver pouco clara, corrija-a primeiro.

### Enquanto Codifica
4. Implemente.
5. Teste.

### Depois de Implementar
6. Reabra este documento e confirme que ele reflete exatamente o que foi codificado.
7. Adicione a data (YYYY-MM-DD) ao lado da regra nova ou alterada.
8. Faça o commit da atualização deste documento e o commit da implementação **separadamente**, com prefixos claros
   (`docs: ...` e depois `feat:`/`fix: ...`).

### Nunca
- Implemente uma mudança de regra sem documentar primeiro.
- Documente em outro arquivo (outro markdown, e-mail, mensagem de chat).
- Deixe uma regra desatualizada (código ≠ documento).
- Apague uma regra sem marcá-la como descontinuada (com data e motivo).
