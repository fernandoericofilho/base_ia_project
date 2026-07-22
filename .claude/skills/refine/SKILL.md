---
description: Refinamento multi-agente de nova funcionalidade — PO, Tech Lead e Backend discutem antes de qualquer implementação
---

# /refine — Fluxo de Refinamento

## Quando usar
Invoque SEMPRE antes de iniciar qualquer nova funcionalidade, endpoint, entidade ou mudança arquitetural relevante.
O Claude deve tomar a iniciativa de invocar este skill sem esperar ser solicitado quando o usuário descrever algo novo.

## Agentes e papéis

### PO (Product Owner)
- Escreve o **épico**: objetivo de negócio, problema que resolve, quem usa
- Define **user stories** no formato "Como [papel], quero [ação], para [benefício]"
- Lista **critérios de aceitação** mensuráveis
- Aponta **riscos de negócio** e dependências com outras funcionalidades

### Tech Lead
- Avalia o **impacto arquitetural**: novas entidades? mudança em existentes? nova camada?
- Identifica **riscos técnicos**: concorrência, performance, migrações destrutivas
- Decide se precisa de **nova migration** de banco e qual estratégia (backward-compatible?)
- Define **padrões obrigatórios** a seguir do `CLAUDE.md` do projeto
- Aponta **dívidas técnicas** que esta feature pode gerar ou resolver

### Backend Developer
- Detalha o **plano de implementação** passo a passo (adapte à arquitetura real do projeto, ex.):
  1. Migrations de banco
  2. Models/Entities
  3. DTOs / Requests / Responses
  4. Repository
  5. Service (regras de negócio)
  6. Mapper
  7. Controller
  8. Testes obrigatórios
- Identifica **o que reusar** vs o que criar do zero
- Estima **complexidade** (baixa / média / alta)

## Fluxo de execução

Ao receber o `/refine` com uma descrição de funcionalidade:

1. Execute um **Workflow** com três agentes em paralelo usando `agentType` dos agentes do projeto:
   - PO: `agentType: "po"` — épico e user stories
   - Tech Lead: `agentType: "techlead"` — impacto arquitetural e riscos técnicos
   - Backend: `agentType: "backend"` — plano de implementação passo a passo
2. Sintetize os outputs em um documento estruturado com seções: Épico, User Stories, Critérios de Aceitação, Impacto Técnico, Plano de Implementação
3. Apresente ao usuário e **aguarde aprovação** antes de qualquer linha de código
4. Se o usuário pedir ajustes, rode apenas os agentes afetados e ressintetize
5. Com aprovação, implemente seguindo exatamente o plano aprovado

## Regras

- Nunca pule o refinamento para "agilizar"
- Se o usuário descrever algo novo diretamente sem pedir refinamento, sinalize e pergunte se quer refinar antes
- O épico deve sempre referenciar as regras obrigatórias do `CLAUDE.md` relevantes
- Testes são parte do plano, não um afterthought
