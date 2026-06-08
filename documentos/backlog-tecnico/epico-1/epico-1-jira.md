# EPICO 1 - Exemplo de Cadastro Simples

Objetivo tecnico do epico

Implementar um fluxo simples de cadastro para demonstrar a arquitetura em camadas do projeto modelo.

Escopo tecnico
- Request, Controller, DTO, Service, Entity e Repository
- Fluxo com regra simples
- Persistencia em banco local de exemplo
- Logs estruturados

Historias tecnicas

## E1-US1 - Cadastro de saudacao

Descricao:
Como estudante, preciso enviar meu nome para receber uma saudacao persistida em memoria para entender o fluxo do sistema.

Criterios de aceite:
1. Deve aceitar um nome obrigatorio.
2. Deve retornar uma mensagem "Hello World" com o nome informado.
3. Deve persistir um registro simples.
4. Deve ter teste unitario do service.
5. Deve ter teste de integracao do controller.

Tasks:
1. Criar request, DTO e response
2. Criar entity e repository
3. Criar mapper
4. Criar service
5. Criar controller
6. Criar migration Flyway
7. Criar testes

