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

Escreva aqui todo guard que bloqueia escrita em entidades com status terminal (ex.: contrato fechado, pedido
cancelado) assim que ele existir — nunca deixe o código divergir do que está escrito aqui.

### 2.2 Idempotência e isolamento transacional

Escreva aqui toda regra de idempotência (chaves, constraints) e de isolamento transacional (retry, propagação de
transação) para operações de escrita crítica assim que ela existir — nunca deixe o código divergir do que está
escrito aqui.

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
- **Piso de cobertura no build.** JaCoCo `jacocoTestCoverageVerification` faz parte do `check`. Piso de linha: **70%** (`gradle.properties`, medido em 2026-10-22, ver §11). O piso sobe quando a cobertura sobe; nunca desce para passar build. Excluídos: `ModeloApplication`, `models`, `dtos`, `controllers`, `exceptions`, `config`.
- **Fins de linha.** `.gitattributes` força LF em `*.sh`, `gradlew` e `.githooks/*`. Script com CRLF quebra no Linux (`exit 127`).
- **CI.** `.github/workflows/ci.yml` roda `./gradlew build` e `./gradlew integrationTest` em todo push e pull request. CI não é desligado para passar: teste quebrado se corrige.

---

## 3. CICLO DE VIDA DOS STATUS

Escreva aqui cada máquina de estados do domínio (status possíveis, transições permitidas, quem pode disparar cada
transição) assim que ela existir — nunca deixe o código divergir do que está escrito aqui.

---

## 4. BANCO DE DADOS

- **Flyway + PostgreSQL (2026-10-22).** Flyway 10+ traz o suporte ao PostgreSQL no módulo `org.flywaydb:flyway-database-postgresql`, que é dependência de runtime do projeto. Sem ele, o `integrationTest` falha com `Unsupported Database: PostgreSQL 16.x`. Versão gerenciada pelo Spring Boot BOM.
- **Política de migration (2026-10-22):** baseline + `V(n+1)`. Migration aplicada nunca é editada. Enquanto não houver dado real, consolidar é permitido; depois, só migration nova. Antes de consolidar, contar linhas (inclusive seeds de catálogo) antes e depois.
- **Dado de teste e demo fora de `db/migration`** (`scripts/seed/` ou endpoint).
- **Dinheiro**: `NUMERIC(19,2)`, nunca `FLOAT`/`DOUBLE PRECISION`. Ver §2.3 e o teste de arquitetura.

Escreva aqui as convenções de migration, nomenclatura de colunas e colunas de auditoria padrão assim que elas
existirem — nunca deixe o código divergir do que está escrito aqui. Detalhe de schema propriamente dito (tabelas,
colunas, índices) vive em `docs/data/SCHEMA.md`, não aqui.

---

## 5. TRATAMENTO DE ERROS

Escreva aqui a tabela de mapeamento exceção → HTTP status → mensagem assim que ela existir — nunca deixe o código
divergir do que está escrito aqui.

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

Escreva aqui os endpoints de health/métricas expostos, os timers e counters obrigatórios por operação crítica, o
mecanismo de rastreamento (trace id, MDC) e os jobs agendados existentes (cron, escopo, o que cada um faz) assim que
existirem — nunca deixe o código divergir do que está escrito aqui.

---

## 11. TESTES

- **Dois tipos, dois comandos.** `./gradlew test` roda unitário e teste de arquitetura, sem Docker. `./gradlew integrationTest` roda teste com Postgres real via Testcontainers (exige Docker). Integração **não** entra no `test`.
- **Nome de cenário**: todo teste que reproduz uma correção ou regra tem comentário `// TEST-<AREA>-NN: cenário e comportamento esperado`. Deve falhar sem a correção e passar com ela.
- **Mudança em Service ou Repository exige teste no mesmo commit** (gate do pre-commit, ver §2.3). Política completa em `TESTING_RULE.md`.
- **Piso de cobertura de linha: 70%**, em `gradle.properties` (`coverageFloor`), verificado pelo `check` (JaCoCo). Medido em 2026-10-22: 114 de 159 linhas cobertas (71,7%), sem `controllers`, `models`, `dtos`, `exceptions`, `config` e `ModeloApplication` (ver `build.gradle.kts`). Meta do CLAUDE.md: 90%. O piso sobe junto com a cobertura e nunca desce para passar build.
- **Teste de arquitetura** (ArchUnit) faz parte do unitário. Ver §2.3 para a lista de regras.
- **Teste desabilitado não entra na branch principal** sem issue com data e dono.

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
