# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **This is a base/template project.** It exists to bootstrap new projects with an already-battle-tested set of
> conventions, agents and skills — extracted from real production work (the AgioMix credit-management system).
> When you start a new project from this template, **read through every section below and adjust the concrete
> details** (stack, domain nouns, thresholds) to fit the new project, then delete this callout. The *shapes* of the
> rules (layered architecture, no hardcoded values, document-every-rule-change, etc.) are meant to survive the copy;
> the *specifics* (Kotlin, PostgreSQL, 90% coverage) are defaults you're free to replace.

## Project

Didactic base project (`base_project`) — a minimal, executable "Hello World, persisted" example that demonstrates
the layered architecture and conventions below. Used both as classroom material and as a starting point for real
projects (`./bootstrap.sh` to run tests, `./bootstrap.sh --run` to also start the app).

## Commands

```bash
# Run (H2 in-memory by default)
./gradlew bootRun

# Tests
./gradlew test

# Build
./gradlew build

# Convenience script: runs tests, and with --run also starts the app
./bootstrap.sh
./bootstrap.sh --run
```

## Architecture

Strict layered architecture — each layer has one responsibility and may only depend on the layer below it:

```
Controller → Mapper → Service → Mapper → Repository → Model (JPA Entity)
```

- **Controller**: receives HTTP request, calls Mapper to produce a DTO, passes it to Service. On the way back, maps
  the DTO to a Response.
- **Service**: only receives and returns DTOs. Contains all business rules. Never handles Request/Response types
  directly.
- **Mapper**: the only class that knows about all representations (Request, DTO, Entity, Response). All conversions
  live here.
- **Repository**: `JpaRepository` interface only — no business logic.
- **GlobalExceptionHandler**: central error handling; maps domain exceptions to HTTP status codes (see table below).

Integration tests that need a real database should use Testcontainers and be tagged/excluded from the fast unit-test
task, running only via a dedicated task (e.g. `integrationTest`) — mirror whatever pattern the project already uses
once one exists.

## Mandatory Rules

These are the rules that, in practice, prevented the most regressions and re-work on past projects. Keep them as
hard defaults; only relax one if the new project has a documented reason to.

- **Unit tests for every Service/Repository modification** — every time you modify business logic in a Service or
  Repository class, create unit tests that simulate the exact scenario being fixed. The test must fail without the
  fix and pass with the fix. Document the scenario with a clear comment (e.g. `TEST-<AREA>-01: ...`). This is not
  optional — treat it as enforced by the build (wire a git hook or CI check once the project has enough history to
  justify it).
- **Constructor injection only** — field injection (`@Autowired` on a field) is forbidden.
- **Correct numeric types for sensitive values** — money, quantities that must never lose precision, etc. use
  `BigDecimal` (or the language's equivalent, e.g. `decimal`/`Decimal`) — never `Double`/`Float`. Define the
  precision/scale explicitly.
- **Soft delete for critical business entities** — records that represent business history (orders, contracts,
  transactions, ...) are never physically deleted; use an `active`/`ativo` flag with a `deactivated_at` timestamp.
  Records with no audit/history value (lookup tables, cache) can still be hard-deleted — decide per entity, not
  globally.
- **Sensitive identifiers stored normalized** — documents, phone numbers, card numbers, etc. are stored without
  formatting/masking characters; masking/formatting happens only at the presentation layer (API response, UI).
- **No hardcoded values** — configuration goes in `application.yml`/`.env`/config service, never inline in code.
- **POST responses** use `ResponseEntity.created(URI.create("/api/v1/<resource>/${saved.id}")).body(response)` — do
  not use framework helpers that infer the URI from the current request (e.g. `ServletUriComponentsBuilder`), since
  that couples the response to how the request happened to be routed.
- **Optimistic locking** on entities that can be updated concurrently — a `@Version` field, mapped to HTTP 409 on
  `ObjectOptimisticLockingFailureException` (or equivalent for the stack in use).
- **Idempotency keys** for any "create" operation that could be retried by a client or a retry policy (payments,
  orders, external-side-effect calls) — look up by key before creating; return the cached result if found.
- **Non-transactional retry facade for concurrent writes** — for any write that can hit optimistic-locking conflicts
  under real concurrency (payments, balance updates), separate a thin non-transactional facade that owns the retry
  policy from an inner service whose methods run in a fresh transaction per attempt (e.g. Spring's
  `Propagation.REQUIRES_NEW`) — retrying inside the same transaction that just failed is the usual way this goes
  wrong. See `.claude/agents/backend.agent.md` for the implementation pattern; `.claude/agents/techlead.agent.md` reviews for its presence.
- **Terminal-state guard on domain entities** — any entity with a "closed"/terminal status (settled, cancelled,
  archived) needs one single guard checked by every write path, throwing a domain exception mapped to 422, not a
  duplicated `if` per service method. Reopening the entity must remain possible; the guard blocks business writes,
  not the status transition itself. See `.claude/agents/backend.agent.md` for the implementation pattern; `.claude/agents/techlead.agent.md`
  reviews for its presence.
- **LAZY loading everywhere** — every `@ManyToOne`/`@OneToOne` (or ORM equivalent) uses lazy fetch. Never eager by
  default.
- **Coverage floor enforced by the build** — pick a number appropriate to the project (this template's own build
  used 90% line coverage as a starting point) and exclude only what genuinely shouldn't count (models, DTOs,
  controllers if they're pure pass-through, exceptions, config, adapters to third parties).

## Naming Conventions

- **Code in English** — all classes, methods, properties, variable names, and log keys, regardless of what natural
  language the team speaks. This keeps the codebase greppable and onboardable by anyone.
- **User-facing messages in the project's spoken language** — validation messages, exception messages surfaced to
  the caller, any text returned in the HTTP response body meant for end users. (AgioMix used Portuguese for this;
  pick whatever your users actually read.)
- **Database column names**: pick ONE convention (snake_case is the most portable across SQL dialects) and never mix
  it with the code's naming convention inconsistently — map explicitly via `@Column(name = "...")` rather than
  relying on an ORM's automatic name translation, so the mapping is visible in the code.
- **Documentation, comments, and SQL comments**: same spoken language as user-facing messages, for consistency.
- Log format: `action=verb_noun status=ok|error id={} field={}` — pick a structured format and use it everywhere;
  this one is a good default because it's greppable and trivially parseable by log aggregators.

## Database / Migrations

Migrations live in `src/main/resources/db/migration/` (Flyway) or the equivalent path for whatever migration tool
the project uses. Rules:

- File naming: follow the tool's convention exactly (Flyway: `V<n>__<description>.sql`, capital `V`, two
  underscores).
- **Never modify an already-applied migration** — always create the next one.
- **Never physically drop a column in the same deploy as code that depends on the new structure** — ship additive,
  backward-compatible migrations; remove the old column in a later, separate deploy once nothing reads it.
- Standard audit columns worth defaulting to on every entity: `created_at`, `updated_at`, `deactivated_at`, `active`
  (rename to match the project's naming convention, but keep the four concepts — created, last-updated, when it was
  turned off, and whether it's currently on).

## Exception → HTTP Mapping

Adapt the concrete exception names to the project, but keep this shape — a single table, one line per exception
family, that both Controller-writers and reviewers can check code against:

| Exception | HTTP |
|---|---|
| `*NotFoundException` | 404 |
| `*AlreadyRegisteredException`, `ObjectOptimisticLockingFailureException` | 409 |
| `*OperationException`, `*InvalidStateException`, validation exceptions (`MethodArgumentNotValidException`, `MissingServletRequestParameterException`) | 422 |
| Any unmapped `Exception` | 500 (structured body, full stack trace in the log, never in the response) |

## Observability

- Health/metrics endpoints exposed (`/actuator/health`, `/actuator/prometheus` or the framework's equivalent).
- **Timers** and **counters** for every critical business operation (creation, payment/settlement, priority-queue
  computation, scheduled jobs) — name them `<domain>.<action>.timer` / `<domain>.<action>.count` consistently.
- **Trace/idempotency propagation via MDC or equivalent** — inject a trace id (and idempotency key, if applicable)
  into every log line; accept and propagate an incoming trace header (e.g. `X-Trace-Id`).

## Frontend Conventions (adapt framework specifics; keep the shape)

- **Standalone components + reactive local state** (Angular signals, React hooks, or equivalent) — avoid the
  framework's older "module" ceremony where a newer standalone pattern exists.
- **One formatting pipe/formatter per data type, used everywhere that type is displayed** — a date pipe, a money
  pipe, a sensitive-identifier (document/phone) pipe. Never output a raw ISO date string or an unformatted document
  number in a template; centralizing the formatter means fixing a display bug once instead of at every call site.
- **A masking directive/input-formatter for every sensitive input** (documents, phone numbers) so pasted or typed
  formatting characters are stripped/reformatted in real time, consistently between create/edit forms AND any
  search/filter inputs that accept the same kind of value — a filter input that looks different from the create
  form is a papercut users notice immediately.
- **Dialog pattern**: open with the framework's modal API passing structured `data`, react to the close event with a
  `done => done && reload()`-shaped callback — keeps every dialog's call site identical and easy to audit.
- **Shared "list card" pattern for list/index pages** — if the project has several list pages with a repeating card
  layout (item name, an id/code chip, status badges, a counters/legend row), put the shared CSS in ONE file and
  `@extend`/compose it from each page's specific stylesheet, rather than duplicating the rules per page. Two things
  that are easy to get wrong and worth checking explicitly:
  - Keep the counters/legend row aligned with the code/id chip (same side), not pinned to the opposite side — two
    consistent visual columns read better than everything crammed to one edge with dead space on the other.
  - If the same visual element (e.g. a legend counter) is sometimes a clickable `<button>` and sometimes a static
    `<span>` depending on the page, force `line-height` explicitly on the shared class — browsers apply different
    default line-height resets to `<button>` vs `<span>`, so the two variants silently end up different heights
    unless you pin it.
- **A single source of truth for the app's font** — one CSS custom property (e.g. `--app-font`) declared once,
  referenced everywhere (including re-pointing the UI framework's own internal font tokens/theme variables at it,
  if the framework bakes in a default font via its own CSS variables). Changing the typeface later should mean
  editing one line, not grepping the codebase for a hardcoded font name.
- **Mobile action buttons**: horizontal row on desktop (>600px), full-width vertical stack on mobile (≤600px) — a
  `.row-actions` class that flips at that breakpoint is enough; don't special-case each button.

## Logging

```kotlin
log.info("action=create_resource status=ok id={}", saved.id)
log.warn("action=handle_error status=conflict message={}", ex.message)
```

## 🚨 MANDATORY: Every Rule Change MUST Be Documented Immediately

**This is the single highest-leverage habit carried over from the source project.** Code and documentation diverge
by accident, every time, on every project — not because anyone decides to skip documenting, but because "I'll write
it up after" quietly becomes "never." The only fix that has actually worked in practice: write the rule down in the
project's single source-of-truth doc **before or at the moment you change the code**, not after.

Concretely, for a new project built from this template:

1. Create `docs/architecture/REGRAS-DO-SISTEMA.md` (or `docs/architecture/RULES.md` if the project is English-only)
   as **the one place** business and technical rules live. No duplicating rules into other markdown files, PR
   descriptions as the only record, or tribal knowledge in chat.
2. Create `docs/data/SCHEMA.md` as the one place the database schema is described in plain language, updated
   **right after** a migration is applied — not batched up for later.
3. Before implementing any rule change: check if the rule already exists in that doc. If not, write it first. If it
   exists but is unclear, fix the doc first.
4. After implementing: re-open the doc, confirm code and doc now say the same thing, add today's date next to the
   entry.
5. Never document a rule change anywhere else (another markdown file, an email, a chat message) — if it's not in
   the single source-of-truth doc, it doesn't count as documented.

This project's own `.claude/agents/techlead.agent.md` enforces this discipline in code review — don't remove that
check when adapting the agent to a new project; it's the mechanism that keeps this from silently lapsing after the
first few weeks.

## Agents, Skills and Plugins in this template

- **`.claude/agents/*.agent.md`** — nine specialist personas (Product Owner, Solution Architect, Backend, Frontend,
  DBA, QA, SRE, Tech Lead, AI Engineer), each a distilled rulebook from real project work. Use them as Claude Code
  subagents (`agentType: "backend"`, etc.) or as review checklists. Generalize the *domain nouns* per project;
  keep the *engineering principles* — that's the part that transfers.
- **`.claude/skills/*/SKILL.md`** — five multi-agent workflows that orchestrate the agents above:
  - `/refine` — PO + Tech Lead + Backend refine a new feature before any code is written.
  - `/review` — Tech Lead + QA + silent-failure detection + test-coverage analysis before merge.
  - `/db-review` — DBA reviews every new migration before it's applied.
  - `/security-audit` — adversarial OWASP-style review before shipping anything sensitive.
  - `/sre-check` — observability/resilience review after implementing a critical flow.
  Invoke these proactively at the point described in each skill's "Quando usar" section — don't wait to be asked.
- **`.claude/settings.json`** — enables the `superpowers` plugin (brainstorming, TDD, systematic debugging, and
  other process skills that complement the five above). Keep it enabled; it's what makes the "always refine before
  building, always review before merging" discipline actually happen instead of being aspirational documentation.
  Enabling it in settings only takes effect once the plugin is actually installed — `./bootstrap.sh` does this
  automatically (idempotent, skipped silently if the `claude` CLI isn't present). Manual equivalent, if needed:
  ```bash
  claude plugin marketplace add anthropics/claude-plugins-official
  claude plugin install superpowers@claude-plugins-official
  ```

## 📋 Where to Document Each Type of Change

| Type of change | Document |
|---|---|
| Business rule | `docs/architecture/REGRAS-DO-SISTEMA.md` |
| Technical/architecture rule | `docs/architecture/REGRAS-DO-SISTEMA.md` |
| Schema/table change | `docs/data/SCHEMA.md` |
| New migration | The migration file itself + update `docs/data/SCHEMA.md` |
| Architecture decision ("why") | `docs/architecture/REGRAS-DO-SISTEMA.md`, dedicated decisions section |

### ✅ Required Workflow

**Before starting:**
1. Open the rules doc.
2. Check if the rule already exists.
3. If not, write it first.
4. If it exists but is unclear, fix it first.

**While coding:** implement, test.

**After finishing:**
5. Re-open the rules doc, confirm code and doc match 100%.
6. Add today's date next to the rule.
7. Commit the doc update and the implementation as separate, clearly labeled commits (`docs: ...` then `feat:`/`fix: ...`).

### 🚫 Never

- Implement without documenting first.
- Document in a different file (another markdown, email, chat).
- Leave a rule stale (code ≠ doc).
- Scatter rules across multiple "status" or "analysis" documents.
- Delete a rule without marking it deprecated (with a date and reason).
