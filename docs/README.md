# 📚 Documentação

Índice completo da documentação do projeto. Escolha por interesse:

## 🚀 Começar Agora

👉 **[DEVELOPER_GUIDE.md](guides/DEVELOPER_GUIDE.md)** — Guia prático para desenvolver  
Arquitetura, padrões, exemplos de como adicionar features, checklist pré-PR.

## 📖 Guias

- **[DEVELOPER_GUIDE.md](guides/DEVELOPER_GUIDE.md)** — Como desenvolver neste projeto
- **[CLASSROOM_GUIDE.md](guides/CLASSROOM_GUIDE.md)** — Rápido (1 página), bom para aula
- **[PLAYBOOK_PR.md](guides/PLAYBOOK_PR.md)** — Fluxo de PR com checklist
- **[TESTING_RULE.md](guides/TESTING_RULE.md)** — Regra mandatória de testes para mudanças em Service/Repository

## 🤖 Agents (Code Review IA)

- **[Agents README](agents/README.md)** — Como usar agents para code review
  - Guia rápido
  - Roteiro de aula (90 min)
  - Exercício prático
  - **Prompts prontos** para cada persona

**Agents disponíveis** (em `.claude/agents/*.agent.md`):
- Backend Principal Engineer
- Staff Tech Lead  
- Product Owner Tech
- Senior QA Engineer
- Senior DBA
- Solution Architect
- Frontend Principal Engineer
- Platform SRE
- AI Engineer

## 📊 Análise e Decisões Técnicas

- **[ARCHITECTURE_AND_IMPROVEMENTS.md](technical/ARCHITECTURE_AND_IMPROVEMENTS.md)** — Análise técnica completa
  - Problemas encontrados
  - Soluções propostas
  - Roadmap de melhorias
  - Comparativo antes/depois

## ✨ Resumos Executivos

- **[EXECUTIVE_SUMMARY.md](summary/EXECUTIVE_SUMMARY.md)** — Resumo visual (1 página)

## 📁 Estrutura de Documentação

```
docs/
├── README.md (este arquivo!)
├── guides/
│   ├── DEVELOPER_GUIDE.md      (Como programar)
│   ├── CLASSROOM_GUIDE.md      (Para sala de aula)
│   ├── PLAYBOOK_PR.md          (Fluxo de PR)
│   └── TESTING_RULE.md         (Regra mandatória de testes)
├── technical/
│   └── ARCHITECTURE_AND_IMPROVEMENTS.md  (Análise técnica)
├── agents/
│   ├── README.md                (Guia + prompts)
│   └── *.agent.md               (9 personas)
└── summary/
    └── EXECUTIVE_SUMMARY.md     (Resumo gráfico)
```

## 🎯 Escolha por Perfil

### Sou Desenvolvedor 👨‍💻

1. Leia: [DEVELOPER_GUIDE.md](guides/DEVELOPER_GUIDE.md)
2. Rode: `./gradlew clean build`
3. Code: Siga os padrões no guide

### Vou Revisar um PR 👀

1. Escolha agent relevante em [Agents README](agents/README.md)
2. Copie o prompt pronto dele
3. Envie código + prompt para LLM
4. Receba recomendações estruturadas

### Estou em uma Aula 🎓

1. Leia: [CLASSROOM_GUIDE.md](guides/CLASSROOM_GUIDE.md) (1 página)
2. Rode: `./bootstrap.sh`
3. Exerça: Siga [PLAYBOOK_PR.md](guides/PLAYBOOK_PR.md)

### Quero Entender Melhorias 🔍

1. Leia: [ARCHITECTURE_AND_IMPROVEMENTS.md](technical/ARCHITECTURE_AND_IMPROVEMENTS.md)
2. Veja: Roadmap priorizado
3. Implemente: Próximos steps

### Quero Resumo Visual (1 min) ⚡

1. Leia: [EXECUTIVE_SUMMARY.md](summary/EXECUTIVE_SUMMARY.md)

---

## ⚡ Links Rápidos

| Documento | Tempo | Uso |
|-----------|-------|-----|
| [DEVELOPER_GUIDE.md](guides/DEVELOPER_GUIDE.md) | 15 min | Referência técnica |
| [CLASSROOM_GUIDE.md](guides/CLASSROOM_GUIDE.md) | 5 min | Resumo rápido |
| [PLAYBOOK_PR.md](guides/PLAYBOOK_PR.md) | 10 min | Checklist PR |
| [Agents README](agents/README.md) | 10 min | Como usar agents |
| [ARCHITECTURE_AND_IMPROVEMENTS.md](technical/ARCHITECTURE_AND_IMPROVEMENTS.md) | 20 min | Análise técnica |
| [EXECUTIVE_SUMMARY.md](summary/EXECUTIVE_SUMMARY.md) | 5 min | Visual resumido |

---

## 💡 Dica

**Primeira vez?** Comece por:
1. [README.md](../README.md) — na raiz (o que é o projeto?)
2. [DEVELOPER_GUIDE.md](guides/DEVELOPER_GUIDE.md) — como desenvolver
3. `./gradlew clean build` — compile e rode os testes

Bora! 🚀

