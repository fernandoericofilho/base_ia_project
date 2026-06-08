 # Playbook: Exemplo de PR + Checklist

Use este playbook como modelo para exercícios de PR em sala de aula. Inclui descrição do PR, checklist do autor, checklist do revisor e prompts para os agents em português.

Título do PR (exemplo)
- feat: adicionar validação simples em HelloService

Descrição curta
- Adiciona validação mínima no DTO do HelloService e um teste unitário cobrindo o caso de falha.

Checklist do autor (antes de abrir o PR)
- [ ] Código compila e testes unitários passam
- [ ] Sem valores hardcoded; configuração em `application.yml`
- [ ] Injeção por construtor usada
- [ ] Logs estruturados com `action=` e `status=` onde aplicável
- [ ] Novo código possui testes unitários

Checklist do revisor humano
- [ ] Convenções de nomenclatura e inglês seguidas (nomes de classes/colunas em inglês)
- [ ] Service contém regras de negócio; controller delega ao service
- [ ] Mapper realiza apenas conversão DTO↔Entity
- [ ] Acesso ao repositório é transacional quando necessário
- [ ] Não há alterações de schema sem script de migration

Prompts sugeridos para usar com os agents (peça um por vez)

- Revisão Backend:
```
Você é o Backend Principal Engineer. Revise o PR #X focando em: injeção por construtor, mapeamento DTO↔Entity, uso de repositório, transações, logs e testes. Retorne um checklist de 6 itens e até 3 correções concretas.
```

- Revisão QA:
```
Você é o Senior QA Engineer. Para o PR #X, liste os 5 casos de teste mais importantes a adicionar e especifique quais devem ser unitários e quais de integração.
```

- Revisão DBA (se aplicável):
```
Você é o Senior DBA. Revise os scripts de migration no PR #X e indique riscos, índices ou constraints faltantes e um plano de rollback.
```

Como executar o exercício na sala
1. Fork do repositório ou crie uma branch
2. Implemente a mudança (máx. 20 minutos)
3. Rode `./bootstrap.sh` localmente
4. Abra o PR e execute os agents um a um usando os prompts acima
5. Complete a revisão humana e faça o merge

