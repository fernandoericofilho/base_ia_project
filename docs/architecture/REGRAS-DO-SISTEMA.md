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

---

## 3. CICLO DE VIDA DOS STATUS

Escreva aqui cada máquina de estados do domínio (status possíveis, transições permitidas, quem pode disparar cada
transição) assim que ela existir — nunca deixe o código divergir do que está escrito aqui.

---

## 4. BANCO DE DADOS

Escreva aqui as convenções de migration, nomenclatura de colunas e colunas de auditoria padrão assim que elas
existirem — nunca deixe o código divergir do que está escrito aqui. Detalhe de schema propriamente dito (tabelas,
colunas, índices) vive em `docs/data/SCHEMA-DO-BANCO.md`, não aqui.

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

Escreva aqui a separação entre testes unitários e de integração, a convenção de nomenclatura de cenário de teste e o
piso de cobertura exigido pelo build assim que existirem — nunca deixe o código divergir do que está escrito aqui.

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
