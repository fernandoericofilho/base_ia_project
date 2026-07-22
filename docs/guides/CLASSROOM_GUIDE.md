# Guia Rápido para a Aula (1 página)

Objetivo: localizar rapidamente os artefatos do projeto-modelo e seguir um fluxo mínimo para exercícios em sala.

Onde procurar
- Código didático: `src/main/kotlin/com/base/` (Controller, DTO, Mapper, Service, Entity, Repository)
- Migrations: `src/main/resources/db/migration/` (Flyway)
- Configuração (H2 por padrão): `src/main/resources/application.yml`
- Script de execução e testes: `bootstrap.sh`
- Agents (personas): `.claude/agents/` (ex.: `.claude/agents/backend.agent.md`, `.claude/agents/qa.agent.md`)
- Prompts prontos: `docs/agents/README.md` (em PT-BR)
- Playbook de PR (roteiro): `docs/guides/PLAYBOOK_PR.md` (em PT-BR)
- Guia rápido dos agents: `docs/agents/README.md`

Fluxo mínimo para um exercício (20–30 min)
1. Criar branch: `git checkout -b aula/NOME-SOBRENOME`  
2. Implementar mudança simples (ex.: validação no Service)  
3. Rodar testes localmente: `./bootstrap.sh`  
4. Abrir PR seguindo o modelo de `docs/guides/PLAYBOOK_PR.md` (título, descrição, checklist)  
5. Usar agents para revisão: copiar um prompt de `docs/agents/README.md` e executar (em sala, o instrutor, `/review`, ou a ferramenta de LLM)  
6. Corrigir, rodar `./bootstrap.sh` novamente e merge quando OK

Checklist rápido do autor antes de abrir PR
- Código compila e testes passam (`./bootstrap.sh`)  
- Não há hardcode (configs em `application.yml`)  
- Injeção por construtor usada  
- Logs estruturados onde aplicável (`action=`, `status=`)  
- Se alterou schema: adicionou migration Flyway

Dicas operacionais para o instrutor
- Use os prompts prontos em `docs/agents/README.md` para treinar alunos a escrever prompts objetivos (máx. 2–3 sentenças).  
- Peça aos alunos para rodar apenas testes unitários se a máquina estiver limitada (remova integração pesada).  
- Mantenha os agents em `.claude/agents/` (local) para que os alunos possam editar e adaptar as personas.

Comandos úteis (copiar/colar)
```bash
cd base_project
./bootstrap.sh          # roda testes
./bootstrap.sh --run    # testa e inicia a app (se desejar)
git checkout -b aula/jose-silva
```

Material adicional
- `docs/agents/README.md` — visão completa de como usar os agents em sala, com prompts de exemplo prontos em PT-BR
- `docs/guides/PLAYBOOK_PR.md` — roteiro detalhado de PR e revisão

