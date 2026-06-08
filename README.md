# 🎓 Base IA Project

Projeto didático para aprender **arquitetura em camadas** com **Kotlin + Spring Boot** e técnicas de **revisão de código com IA**.

## 🚀 Quick Start (2 minutos)

```bash
# Clonar
git clone <repo>
cd base_ia_project

# Compilar e rodar testes
./gradlew clean build

# Ou iniciar a aplicação
./gradlew bootRun
```

App disponível em `http://localhost:8080`

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
Kotlin 1.9.24 · Spring Boot 3.4.5 · H2 (PostgreSQL Dialect)
Flyway · JPA/Hibernate · JUnit 5 · Mockito-Kotlin · Gradle 9.2.1
```

## ✨ Destaques

✅ **Arquitetura**: camadas claras (Controller → Service → Repository)  
✅ **Error Handling**: Global `@ControllerAdvice` profissional  
✅ **Testes**: 7 testes passando (~45% cobertura)  
✅ **Documentação**: Prática e detalhada em `docs/`  
✅ **Agents IA**: 9 personas para code review  
✅ **Build**: ✅ SUCCESS (18s)  

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

## 📊 Status

| Item | Status |
|------|--------|
| Build | ✅ SUCCESS |
| Testes | ✅ 7/7 passando |
| Documentação | ✅ Completa em `docs/` |
| Error Handling | ✅ Profissional |
| Code Review IA | ✅ 9 agentes |

---

**Tudo em `docs/` — confira [`docs/README.md`](./docs/README.md) para navegação completa!** 📚


