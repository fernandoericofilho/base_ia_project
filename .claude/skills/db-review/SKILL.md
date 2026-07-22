---
description: Revisão de migrations e schema pelo DBA antes de aplicar — índices, integridade, performance e backward-compatibility
---

# /db-review — Revisão de Migration pelo DBA

## Quando usar
Invoque sempre que uma nova migration de banco for escrita, antes de rodar a aplicação ou fazer merge de código que contenha migrations.
O Claude deve sugerir proativamente ao detectar arquivos novos na pasta de migrations do projeto (ex.: `db/migration/`, `migrations/`).

## Agente envolvido

### Senior DBA (`agentType: "dba"`)
Responsável pela revisão completa da migration com foco em:

**Nomenclatura e convenções**
- Seguir a convenção de nomenclatura de tabelas/colunas definida no `CLAUDE.md` do projeto
- Colunas de auditoria obrigatórias, se o projeto adotar o padrão (ex.: `ativo`, `criado_em`, `atualizado_em`, `desativado_em`)
- Arquivo de migration segue o padrão de nomenclatura da ferramenta usada (ex.: Flyway `V<n>__<descricao>.sql`)

**Integridade e constraints**
- Foreign keys com a estratégia correta de `ON DELETE` (nunca `CASCADE` implícito em entidade que não deveria perder histórico)
- `NOT NULL` nos campos obrigatórios
- `UNIQUE` constraints onde necessário (documentos, chaves de negócio, etc.)
- `CHECK` constraints para enums ou intervalos numéricos

**Performance**
- Índices necessários (FK, campos de busca frequente, colunas usadas em `WHERE`/`ORDER BY`)
- Tipos de dado adequados (`NUMERIC`/`DECIMAL` para valores monetários — nunca `FLOAT`/`DOUBLE`)
- Evitar índices desnecessários em tabelas pequenas

**Segurança e backward-compatibility**
- Novos campos devem ter `DEFAULT` ou ser nullable quando a tabela já tem dados (nunca `NOT NULL` sem `DEFAULT` em tabela existente)
- Nunca fazer `DROP COLUMN` na mesma migration que adiciona código que depende da nova estrutura
- Nunca modificar uma migration já aplicada — sempre criar a próxima
- Sem dados sensíveis em comentários SQL

**Ferramenta de migration**
- Verifica que o número/versão da migration não colide com migrations existentes
- Valida que a migration é idempotente onde possível

## Fluxo de execução

1. Leia as migrations existentes para ter contexto do schema atual
2. Execute o agente `dba` sobre a(s) nova(s) migration(s) com o contexto do schema completo
3. O agente retorna achados com severidade: `BLOCKER`, `WARNING`, `SUGGESTION`
4. Se houver `BLOCKER`: corrija antes de aplicar
5. Apresente a revisão completa ao usuário com justificativa de cada achado

## Regras

- BLOCKER inclui: `NOT NULL` sem `DEFAULT` em tabela existente com dados, tipo de ponto flutuante em campo monetário, `DROP COLUMN` com código dependente, migration com número/versão duplicado
- Nunca aplicar uma migration sem essa revisão em schema que tenha dados de produção
- Se a migration criar nova entidade de domínio, verificar se o model correspondente foi criado corretamente (colunas de auditoria mapeadas, se aplicável)
