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

Convenções de migration, nomenclatura e checklist completo vivem em `docs/data/SCHEMA.md` (que também descreve o
schema atual tabela por tabela) — este documento não duplica aquele conteúdo.

**Dois caminhos de execução** (2026-07-23):
- **Default (dia a dia / aula)**: H2 em memória, `application.yml`, sem dependência externa.
- **Postgres real (opcional)**: `docker compose up -d` sobe um Postgres 16 local (ver `docker-compose.yml`), e
  `./gradlew bootRun --args='--spring.profiles.active=postgres'` ativa `application-postgres.yml`, que só troca o
  datasource — as mesmas migrations Flyway (`V1`, `V2`, ...) rodam em ambos os caminhos sem alteração. Ver
  `docs/guides/DEVELOPER_GUIDE.md`, seção "Rodando com Postgres real (Docker)".
- Testes de integração (`./gradlew integrationTest`) usam um terceiro caminho, Postgres efêmero via Testcontainers
  — independente do container do `docker-compose.yml`.

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

Escreva aqui os padrões de componente, pipes/formatters obrigatórios, diretivas de máscara, padrão de dialogs e
convenções visuais (espaçamento, cards de listagem, tipografia) assim que existirem — nunca deixe o código divergir
do que está escrito aqui.

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

Escreva aqui onde vive o rastreamento de features (pendente/concluído), a obrigação de atualizá-lo antes de cada
commit e o formato padrão de cada item assim que essa regra existir — nunca deixe o código divergir do que está
escrito aqui.

---

## 14. DECISÕES DE ARQUITETURA

Escreva aqui o "porquê" de decisões arquiteturais relevantes (ex.: por que um Strategy Pattern, por que um campo
interno existe, por que uma abordagem foi escolhida em vez de outra) assim que existirem — nunca deixe o código
divergir do que está escrito aqui.

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
