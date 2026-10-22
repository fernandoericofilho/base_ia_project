# 📋 Backlog

Rastreamento de épicos e itens de trabalho, organizados por status.

## 📂 Estrutura

```
docs/backlog/
├── README.md            (este arquivo)
├── EXEMPLO-EPICO.md      (épico de referência — leia antes do primeiro /refine)
├── concluido/            ✅ Itens já implementados e validados
└── todo/                 🔴🔥🟡🟢 Itens a implementar
```

## 🎯 Como usar

1. Antes de pedir uma feature nova, rode `/refine` — o Product Owner (`.claude/agents/po.agent.md`)
   transforma o pedido em objetivo, história, critérios de aceite, escopo e riscos
   **antes** de qualquer código ser escrito.
2. O resultado do refino vira um arquivo em `todo/{area}.md` (ou um arquivo de épico
   dedicado, como em `EXEMPLO-EPICO.md`), com uma lista de tarefas em checkbox.
3. Ao concluir e testar a implementação, mova o item de `todo/{area}.md` para
   `concluido/{area}.md`, marcando `[x]` com a data (YYYY-MM-DD).

Veja `EXEMPLO-EPICO.md` para um exemplo completo do formato esperado.
