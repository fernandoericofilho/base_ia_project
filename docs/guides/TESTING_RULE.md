# 📋 REGRA DE TESTE PARA MUDANÇAS EM SERVIÇOS

**Status:** MANDATÓRIO

---

## A Regra

**Toda vez que você mexer em um Service ou Repository que altera lógica de negócio, você DEVE:**

1. ✅ **Criar testes unitários** que simulam o cenário que foi corrigido/alterado
2. ✅ **Adicionar documentação** aos testes explicando o cenário e o comportamento esperado
3. ✅ **Rodar os testes** para garantir que a correção funciona (`./gradlew test`)
4. ✅ **Documentar no commit** qual cenário foi testado

---

## Por Quê?

- **Regredir é fácil:** Sem testes, alguém pode acidentalmente reverter a correção
- **Documentar o cenário:** O teste serve como documentação viva do comportamento esperado
- **Confiança:** Antes de colocar em produção, sabemos que o cenário funciona

---

## Exemplos de Mudanças que Exigem Testes

- ❌ Mudança em lógica de status (`ex.: PENDING → IN_PROGRESS → DONE`)
- ❌ Mudança em cálculos ou agregações
- ❌ Mudança em filtros ou queries
- ❌ Mudança em validações de guard
- ❌ Mudança em lógica de cancelamento/reversão
- ❌ Mudança em fluxos de transação

---

## Anatomia de um Bom Teste

### ✅ BOM - Claro e bem documentado

```kotlin
// TEST-<AREA>-01: descreva aqui o cenário e o comportamento esperado
@Test
fun `doSomething - edge case should keep expected state`() {
    // SETUP: monte o cenário mínimo que reproduz o bug/mudança
    val entity = someEntity(10L).copy(
        someField = someValue,
        status = SomeStatus.EDGE_CASE
    )

    // ACTION: execute a operação sob teste
    val result = service.doSomething(10L, someDto())

    // ASSERT: valide exatamente o comportamento esperado, nada a mais
    val captor = argumentCaptor<Entity>()
    verify(repository).save(captor.capture())
    assertEquals(SomeStatus.EDGE_CASE, captor.firstValue.status)
}
```

### ❌ RUIM - Sem documentação, não claro

```kotlin
@Test
fun `test1`() {
    val i = someEntity().copy(status = SomeStatus.EDGE_CASE)
    service.doSomething(1L, someDto())
    // ...
}
```

---

## Passos Práticos

### 1. Você encontra um bug/mexe em lógica
```kotlin
// SomeService.kt linha 97-101
// BUG: não respeita status X ao executar a operação Y
```

### 2. Crie o teste ANTES de consertar (TDD)
```kotlin
@Test
fun `doSomething - edge case should keep expected state`() {
    // Teste vai FALHAR aqui (red)
}
```

### 3. Conserte o código
```kotlin
// Linha 97-101: adicionar lógica para respeitar o status X
val newStatus = when {
    condicaoPrincipal -> RESULTADO_A
    entity.status == STATUS_X -> STATUS_X  // ← FIX
    else -> RESULTADO_B
}
```

### 4. Teste passa (green)
```bash
./gradlew test

SomeServiceTest > doSomething - edge case should keep expected state PASSED ✅
```

### 5. Commit com referência ao teste
```bash
git commit -m "fix: respeitar status X ao executar operação Y

Problema: operação Y sobrescrevia o status X indevidamente

Solução: adicionar lógica para manter o status X quando aplicável

Testes adicionados:
- TEST-AREA-01: cenário principal
- TEST-AREA-02: cenário de borda relacionado

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Cenários Testados

Mantenha aqui uma tabela viva com os cenários cobertos, à medida que forem adicionados:

| Scenario | Test | File | Status |
|---|---|---|---|
| _(exemplo)_ Operação Y não deve sobrescrever status X | TEST-AREA-01 | SomeServiceTest.kt | ✅ |

---

## Executar Testes

```bash
# Todos os testes unitários (rápidos, sem infraestrutura real)
./gradlew test

# Apenas um arquivo
./gradlew test --tests SomeServiceTest

# Apenas um teste
./gradlew test --tests SomeServiceTest.doSomething*edgeCase*

# Testes de integração (Postgres real via Testcontainers, requer Docker)
./gradlew integrationTest
```

---

## Checklist Antes de Fazer Commit

- [ ] Identifiquei o cenário que mudou
- [ ] Criei teste unitário que simula o cenário
- [ ] Teste falha sem a correção
- [ ] Corrigi o código
- [ ] Teste passa com a correção
- [ ] Documentei o cenário no comentário do teste
- [ ] Rodei `./gradlew test` e tudo passou
- [ ] Cobertura de testes >= threshold do projeto
- [ ] Mencionei os testes no commit message
