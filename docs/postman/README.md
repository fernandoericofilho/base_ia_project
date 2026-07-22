# Convenção Postman

Este projeto mantém uma coleção Postman versionada em `docs/postman/base-project.postman_collection.json`, complementar ao Swagger/OpenAPI (`/swagger-ui.html`). O Swagger documenta o contrato; a coleção Postman documenta exemplos de uso prontos para rodar.

## Regra

Todo novo endpoint REST criado no projeto deve ganhar uma request correspondente na coleção, dentro de uma pasta (folder) por recurso — ex.: `Hello`, `Task`. Não crie um endpoint sem atualizar a coleção na mesma mudança.

## Variáveis de ambiente

A coleção usa a variável de coleção `{{baseUrl}}` (padrão `http://localhost:8080`) em vez de URLs fixas. Isso permite rodar a mesma coleção contra local, staging ou produção apenas trocando o valor da variável — nunca edite a URL dentro de cada request.

Se o projeto precisar de ambientes distintos (local/staging/prod), prefira criar Postman Environments que sobrescrevem `baseUrl`, em vez de duplicar a coleção.

## Corpos de exemplo

Os bodies de exemplo devem refletir os DTOs reais (`Request` classes em `controllers/request`), com valores plausíveis — nunca `{}` vazio. Isso permite que qualquer pessoa importe a coleção e já execute uma chamada válida sem precisar ler o código-fonte primeiro.

## Estrutura

```
docs/postman/
  README.md                          <- este arquivo
  base-project.postman_collection.json
```
