# Fundação do template — pendências (2026-10-22)

Pendências de fundação do template. Concluído sai daqui, com data.

- [ ] 🔥 **Smoke versionado de startup** (`scripts/smoke.sh`): sobe a aplicação, faz login e lista com dado; sai com código ≠ 0 em qualquer falha. Hoje o CI só compila e testa, então um startup quebrado passaria.
- [ ] 🟡 **Decidir consolidação das cópias de agentes.** `.claude/agents/` (usada pelo Claude Code), `agents/` e `docs/agents/` (aula) já divergiram. Decisão de manter as três sincronizadas ou deixar uma única fonte e gerar as outras.
- [ ] 🟡 **Subir o piso de cobertura para 90%** (meta do CLAUDE.md). Hoje 70% (medido 71,7%). Subir junto com testes de Service, nunca baixar.
- [ ] 🟡 **Probes de liveness e readiness separados** (Actuator): health de processo não depende do banco; health de readiness depende.
- [ ] 🟢 **Desligamento gracioso**: `server.shutdown: graceful` e `spring.lifecycle.timeout-per-shutdown-phase` no `application.yml`.
- [ ] 🟢 **Teste de consistência entre `application.yml` e `.env.example`** quando houver variável obrigatória. Não se aplica enquanto o template não tiver `.env`.
