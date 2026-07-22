# TASK-01 — Gerenciamento de Tarefas (Task Management)

**Status:** 🟡 TODO — pronto para implementação
**Épico:** Gerenciamento de Tarefas
**Complexidade estimada:** MÉDIA (6-8h)

Este arquivo é um **exemplo de referência** do que um bom output de `/refine`
deve conter, seguindo o formato definido em `.claude/agents/po.agent.md`. Use-o
como modelo ao refinar qualquer nova funcionalidade neste projeto.

---

## Objetivo

**Problema:** hoje o usuário não tem nenhuma forma estruturada de registrar e
acompanhar tarefas dentro do sistema — pedidos e lembretes circulam por fora
(chat, papel, memória), gerando esquecimento e retrabalho.

**Valor esperado:** permitir que qualquer usuário crie, visualize, atualize e
conclua tarefas próprias diretamente no sistema, reduzindo o uso de canais
paralelos para controle de trabalho.

**Impacto:** usuários passam a ter um local único e confiável para saber
"o que falta fazer"; a operação ganha rastreabilidade sobre o que foi
prometido e o que foi entregue.

**Resultado mensurável esperado:** redução do uso de canais informais para
controle de pendências e aumento da proporção de tarefas concluídas dentro do
prazo combinado.

---

## Histórias

### História 1 — Criar tarefa

Como usuário
Quero registrar uma nova tarefa com título, descrição opcional e prazo opcional
Para não depender de memória ou anotações externas para controlar meu trabalho

### História 2 — Listar e filtrar tarefas

Como usuário
Quero visualizar minhas tarefas e filtrá-las por status (pendente, em andamento, concluída)
Para saber rapidamente o que ainda precisa ser feito

### História 3 — Concluir tarefa

Como usuário
Quero marcar uma tarefa como concluída
Para que ela saia da minha lista de pendências e fique registrada como entregue

---

## Critérios de Aceite

**História 1 — Criar tarefa**
- Uma tarefa só é criada com título preenchido; título vazio é rejeitado com mensagem clara.
- Descrição e prazo são opcionais.
- Toda tarefa criada nasce com status "pendente".
- A tarefa criada passa a aparecer imediatamente na listagem do usuário dono.

**História 2 — Listar e filtrar tarefas**
- O usuário só visualiza as próprias tarefas, nunca as de outro usuário.
- É possível filtrar a listagem por um status específico (pendente, em andamento, concluída) ou ver todas.
- Tarefas com prazo vencido e ainda não concluídas são identificáveis na listagem.

**História 3 — Concluir tarefa**
- Uma tarefa só pode ser concluída pelo usuário dono.
- Ao concluir, a tarefa recebe uma data de conclusão registrada.
- Uma tarefa já concluída não pode ser concluída novamente (ação idempotente ou bloqueada com mensagem clara).
- É possível reabrir uma tarefa concluída, voltando ao status anterior (pendente ou em andamento).

---

## Escopo

### Dentro do escopo
- Criação, listagem, filtro por status e conclusão/reabertura de tarefas.
- Cada tarefa pertence a exatamente um usuário (o criador).
- Validação de campos obrigatórios (título) e opcionais (descrição, prazo).

### Fora do escopo
- Compartilhamento de tarefas entre usuários ou atribuição a terceiros.
- Subtarefas, categorias, etiquetas ou anexos.
- Notificações/lembretes automáticos de prazo.
- Priorização, ordenação manual ou quadro Kanban.

---

## Dependências e Riscos

### Bloqueantes
- Existência de um usuário autenticado/identificável ao qual a tarefa será vinculada.

### Desejáveis
- Um mecanismo de busca por texto no título/descrição (melhora a usabilidade, mas não impede a entrega mínima).

### Opcionais
- Suporte a prazos recorrentes ou tarefas modelo — evolução futura, sem justificativa de negócio imediata.

### Riscos
- **Operacional:** sem limite de tamanho para título/descrição, entradas muito longas podem degradar a listagem — mitigar com limite de caracteres.
- **Usuário:** ausência de qualquer forma de desfazer a conclusão frustra o usuário que concluiu por engano — coberto pelo critério de reabertura acima.
- **Risco não coberto neste escopo:** múltiplos usuários dividindo a mesma tarefa exigiria regra de propriedade/permissão adicional, propositalmente fora do escopo mínimo.

---

## Métricas de Sucesso

- **Volume:** número de tarefas criadas por usuário/semana.
- **Taxa:** percentual de tarefas concluídas dentro do prazo informado.
- **Tempo:** tempo médio entre criação e conclusão de uma tarefa.
- **Redução de esforço operacional:** queda no uso de canais informais (chat/papel) para controle de pendências, medida por pesquisa simples com usuários após a entrega.

---

## Fluxo

Não aplicável — um único ator (usuário dono da tarefa), sem aprovações,
integrações externas ou exceções que justifiquem um fluxo dedicado.

---

**Nota para quem for implementar:** este documento define **o quê** e **por quê**,
nunca o **como**. Arquitetura, modelo de dados, endpoints e stack ficam a
cargo do Tech Leader e do Backend, em etapas seguintes do fluxo de refino.
