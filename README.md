# 🎓 Base IA Project

Projeto didático para aprender **arquitetura em camadas** com **Kotlin + Spring Boot** e técnicas de **revisão de código com IA**.

## 🚀 Quick Start (2 minutos)

```bash
# Clonar
git clone https://github.com/fernandoericofilho/base_ia_project.git
cd base_ia_project

# Compilar e rodar testes
./gradlew clean build

# Ou iniciar a aplicação
./gradlew bootRun
```

App disponível em `http://localhost:8080` (H2 em memória por padrão). Pra rodar contra Postgres real via Docker,
veja [`docs/guides/DEVELOPER_GUIDE.md`](./docs/guides/DEVELOPER_GUIDE.md#rodando-com-postgres-real-docker)
(`docker compose up -d` + `--spring.profiles.active=postgres`).

Documentação interativa da API (Swagger UI): `http://localhost:8080/swagger-ui/index.html`
(JSON cru em `/v3/api-docs`).

## 📚 Documentação

👉 **[Leia a Documentação em `docs/`](./docs/README.md)** — Índice completo com atalhos por perfil

**Atalhos rápidos**:
- 🧑‍💻 **Desenvolvedor?** Leia [`docs/guides/DEVELOPER_GUIDE.md`](./docs/guides/DEVELOPER_GUIDE.md)
- 🤖 **Quer usar Agents?** Vá para [`docs/agents/README.md`](./docs/agents/README.md)
- 📖 **Aula/Sala?** [`docs/guides/CLASSROOM_GUIDE.md`](./docs/guides/CLASSROOM_GUIDE.md)
- 🔍 **Análise Técnica?** [`docs/technical/ARCHITECTURE_AND_IMPROVEMENTS.md`](./docs/technical/ARCHITECTURE_AND_IMPROVEMENTS.md)
- ⚡ **Resumo (1 min)?** [`docs/summary/EXECUTIVE_SUMMARY.md`](./docs/summary/EXECUTIVE_SUMMARY.md)

## 🛠️ Stack

```
Kotlin 1.9.24 · Spring Boot 3.4.5 · H2 (PostgreSQL Dialect) ou Postgres real via Docker Compose
Flyway · JPA/Hibernate · JUnit 5 · Mockito-Kotlin · JaCoCo · springdoc-openapi (Swagger) · Gradle 9.2.1
```

## ✨ Destaques

- ✅ **Arquitetura**: camadas claras (Controller → Service → Repository), com guard de estado terminal e optimistic locking (`@Version` → 409) já demonstrados na feature de referência (`Task`)
- ✅ **Error Handling**: Global `@ControllerAdvice` profissional (validação, exceções de negócio, conflito de concorrência, fallback genérico — ver tabela em [`docs/architecture/REGRAS-DO-SISTEMA.md`](./docs/architecture/REGRAS-DO-SISTEMA.md))
- ✅ **Testes**: 22 testes unitários passando (`./gradlew test`) + testes de integração via Testcontainers (`./gradlew integrationTest`, requer Docker) — ver [`docs/guides/TESTING_RULE.md`](./docs/guides/TESTING_RULE.md)
- ✅ **Cobertura**: piso de 80% imposto pelo build via JaCoCo (`./gradlew build` falha abaixo disso); cobertura real atual: 91.7%
- ✅ **Postgres real opcional**: `docker compose up -d` + profile `postgres`, sem sair do H2 no dia a dia — ver [`docs/guides/DEVELOPER_GUIDE.md`](./docs/guides/DEVELOPER_GUIDE.md#rodando-com-postgres-real-docker)
- ✅ **OpenAPI/Swagger**: documentação interativa em `/swagger-ui/index.html`, com todos os endpoints anotados
- ✅ **CI**: GitHub Actions rodando build, testes e `integrationTest` a cada push/PR
- ✅ **Métricas de negócio**: timers e counters por operação crítica (`task.<ação>.timer`/`.count`) expostos em `/actuator/prometheus`
- ✅ **Documentação**: Prática e detalhada em `docs/`, com fonte única de verdade de regras em [`docs/architecture/REGRAS-DO-SISTEMA.md`](./docs/architecture/REGRAS-DO-SISTEMA.md)
- ✅ **Agents IA**: 9 personas para code review

## 📖 Como Começar

1. **Leia**: [`docs/guides/DEVELOPER_GUIDE.md`](./docs/guides/DEVELOPER_GUIDE.md) (15 min)
2. **Rode**: `./gradlew clean build`
3. **Code**: Siga os padrões no guide

## 🤖 Agents para Code Review

Todas as personas em `docs/agents/` com **prompts prontos** para copiar/colar:

```bash
cd docs/agents
cat README.md  # Guia completo + prompts para cada agent
```

**As 9 personas**:
Backend, TechLead, PO, QA, DBA, Architect, Frontend, SRE, AI

## 🧩 Integração com Claude Code

Além dos prompts manuais em `docs/agents/`, as mesmas 9 personas existem como **subagents reais do Claude Code**,
prontos para uso via `agentType` (não é preciso copiar/colar prompt nenhum):

- **`.claude/agents/*.agent.md`** — os 9 agents (Backend, Frontend, DBA, QA, SRE, TechLead, PO, Architect, AI),
  cada um um rulebook destilado de projetos reais (extraído do AgioMix e generalizado por cada especialista).
- **`.claude/skills/*/SKILL.md`** — 5 fluxos multi-agente que orquestram esses agents em paralelo:
  `/refine` (antes de implementar), `/review` (antes de merge), `/db-review` (antes de aplicar migration),
  `/security-audit` (antes de expor algo sensível), `/sre-check` (depois de um fluxo crítico).
- **`.claude/settings.json`** — habilita o plugin `superpowers` (brainstorming, TDD, debugging sistemático).
- **`CLAUDE.md`** — regras obrigatórias do projeto (arquitetura, testes, naming, observabilidade, e a disciplina de
  documentar toda mudança de regra imediatamente). Leia e adapte antes de começar um projeto novo a partir deste
  template.

O `./bootstrap.sh` já deixa isso pronto sozinho: registra o marketplace oficial e instala o plugin `superpowers`
automaticamente antes de rodar os testes (idempotente; pulado sem erro se o CLI `claude` não estiver instalado).
Instalação manual, se precisar:

```bash
claude plugin marketplace add anthropics/claude-plugins-official
claude plugin install superpowers@claude-plugins-official
```

Ou, dentro de uma sessão interativa do Claude Code:

```
/plugin marketplace add anthropics/claude-plugins-official
/plugin install superpowers@claude-plugins-official
```

## 📊 Status

| Item | Status |
|------|--------|
| Build | ✅ SUCCESS |
| Testes | ✅ 22/22 unitários passando |
| Cobertura | ✅ 91.7% (piso exigido: 80%, via JaCoCo) |
| Documentação | ✅ Completa em `docs/`, regras em `docs/architecture/REGRAS-DO-SISTEMA.md` |
| Error Handling | ✅ Profissional (inclui 409 de optimistic locking) |
| Postgres real | ✅ Opcional via `docker compose up -d` + profile `postgres` |
| CI | ✅ GitHub Actions (`.github/workflows/ci.yml`) — build, testes e integrationTest a cada push/PR |
| Code Review IA | ✅ 9 agentes (manual em `docs/agents/` + subagents reais em `.claude/agents/`) |

---

**Tudo em `docs/` — confira [`docs/README.md`](./docs/README.md) para navegação completa!** 📚
