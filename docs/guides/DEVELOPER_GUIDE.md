# 🛠️ Developer Guide

Guia completo para desenvolver, testar e fazer PR neste projeto.

## Quick Start (2 minutos)

```bash
# Clonar e executar testes
git clone <repo>
cd base_ia_project
./gradlew clean build
```

Ou use o script:
```bash
./bootstrap.sh          # Apenas testes
./bootstrap.sh --run    # Testes + inicia app
```

App estará em `http://localhost:8080`

## Arquitetura

```
Request
  ↓
HelloController  (@RestController)
  ↓
HelloService  (@Service, @Transactional)
  ↓
GreetingRecordRepository  (JpaRepository)
  ↓
GreetingRecord  (@Entity)
  ↓
Database (H2/PostgreSQL)
```

## Stack

- **Language**: Kotlin 1.9.24
- **Framework**: Spring Boot 3.4.5
- **Database**: H2 (development), PostgreSQL (production)
- **Migrations**: Flyway
- **Testing**: JUnit 5, Mockito-Kotlin 5.3.1
- **Build**: Gradle 9.2.1

## Estrutura do Projeto

```
src/
├── main/
│   ├── kotlin/com/base/
│   │   ├── ModeloApplication.kt          # Main
│   │   ├── controllers/
│   │   │   ├── HelloController.kt        # REST endpoint
│   │   │   ├── request/HelloRequest.kt
│   │   │   └── response/HelloResponse.kt
│   │   ├── services/
│   │   │   └── HelloService.kt
│   │   ├── repositories/
│   │   │   └── GreetingRecordRepository.kt
│   │   ├── models/
│   │   │   └── GreetingRecord.kt         # @Entity
│   │   ├── dtos/
│   │   │   └── HelloDTO.kt
│   │   └── mappers/
│   │       └── HelloMapper.kt
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           └── V1__create_greeting_record.sql
└── test/
    └── kotlin/com/base/
        ├── services/HelloServiceTest.kt
        └── mappers/HelloMapperTest.kt
```

## Padrões Obrigatórios

### 1. Constructor Injection

```kotlin
@Service
class HelloService(
    private val repository: GreetingRecordRepository,
    private val mapper: HelloMapper
) { }
```

### 2. Logs Estruturados

```kotlin
log.info("action=say_hello status=success id={}", saved.id)
log.error("action=say_hello status=error error={}", exception.message)
```

### 3. DTOs para Requests/Responses

```kotlin
// Controller recebe request
@PostMapping
fun sayHello(@Valid @RequestBody request: HelloRequest): ResponseEntity<HelloResponse>

// Service use DTO interno
private fun process(dto: HelloDTO)

// Response bem tipada
data class HelloResponse(val id: Long, val name: String, val message: String)
```

### 4. Validação com Jakarta Validation

```kotlin
data class HelloRequest(
    @field:NotBlank(message = "name is required")
    val name: String?
)
```

### 5. Transações no Service

```kotlin
@Service
class HelloService {
    @Transactional
    fun sayHello(dto: HelloDTO): HelloDTO {
        // Salvar em BD
    }
}
```

## Como Adicionar uma Feature

### Exemplo: Adicionar validação de tamanho mínimo

#### 1. Atualizar Request

```kotlin
data class HelloRequest(
    @field:NotBlank(message = "name is required")
    @field:Size(min = 3, max = 100, message = "name must be 3-100 chars")
    val name: String?
)
```

#### 2. Atualizar Service com lógica de negócio (não em Mapper)

```kotlin
@Transactional
fun sayHello(dto: HelloDTO): HelloDTO {
    log.info("action=say_hello name={}", dto.name)
    
    // Validação de regra de negócio
    if (dto.name.length < 3) {
        throw IllegalArgumentException("Name too short")
    }
    
    val saved = repository.save(mapper.toEntity(dto))
    log.info("action=say_hello status=success id={}", saved.id)
    return mapper.toDto(saved)
}
```

#### 3. Adicionar Teste Unitário

```kotlin
@Test
fun `sayHello - should reject names shorter than 3 chars`() {
    assertThrows<IllegalArgumentException> {
        service.sayHello(HelloDTO(name = "ab", message = "..."))
    }
}
```

#### 4. Rodar testes

```bash
./gradlew test
# ou
./bootstrap.sh
```

### Exemplo: Adicionar coluna ao banco

#### 1. Criar migration Flyway

```sql
-- src/main/resources/db/migration/V2__add_age_to_greeting_record.sql
ALTER TABLE greeting_record ADD COLUMN age INT;
```

#### 2. Atualizar Entity

```kotlin
@Entity
@Table(name = "greeting_record")
data class GreetingRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "name", nullable = false, length = 100)
    val name: String,
    
    @Column(name = "age", nullable = true)
    val age: Int? = null,
    
    // ... resto do código
)
```

#### 3. Atualizar DTO e Mapper

```kotlin
data class HelloDTO(
    val id: Long? = null,
    val name: String,
    val message: String,
    val createdAt: LocalDateTime? = null,
    val age: Int? = null  // novo
)

// No mapper
fun toEntity(dto: HelloDTO): GreetingRecord =
    GreetingRecord(
        id = dto.id,
        name = dto.name,
        message = dto.message,
        createdAt = dto.createdAt ?: LocalDateTime.now(),
        age = dto.age  // novo
    )
```

#### 4. Rodar testes + validate schema

```bash
./bootstrap.sh
```

## Checklist Antes de Fazer PR

- [ ] Código compila: `./gradlew build`
- [ ] Testes passam: `./bootstrap.sh`
- [ ] Nenhum hardcode (configs em `application.yml`)
- [ ] Injeção por construtor usada (`@Autowired` = ❌)
- [ ] Logs estruturados com `action=`, `status=`, `id=`
- [ ] DTOs separadas (Request/Response/DTO interno)
- [ ] Se alterou schema: migration Flyway adicionada
- [ ] Novos testes adicionados
- [ ] Sem `TODO` comentários não endereçados

## Testing

### Rodar todos os testes

```bash
./gradlew test
```

### Rodar testes de um arquivo

```bash
./gradlew test --tests HelloServiceTest
```

### Rodar com cobertura

```bash
./gradlew test jacocoTestReport
# Report em: build/reports/jacoco/test/html/index.html
```

## Agents (Code Review)

Disponível em `.claude/agents/` para orientar revisões (também usáveis como subagents reais do Claude Code via `agentType`):

- **backend.agent.md** — Kotlin, Spring, design patterns
- **qa.agent.md** — Testes e casos de borda
- **dba.agent.md** — Schema e migrations
- **techlead.agent.md** — Arquitetura e trade-offs
- **architect.agent.md** — Design de solução
- **sre.agent.md** — Operacional e observabilidade

### Como usar um agent em PR

Copie o prompt abaixo, substitua pelo seu código/PR, envie a um LLM:

**Backend Review Example**:
```
Você é o Backend Principal Engineer. Revise o código focando em:
- Injeção por construtor
- Mapeamento DTO↔Entity
- Validação
- Logs estruturados
- Testes

[CÓDIGO AQUI]

Retorne: 1 checklist (6 itens) + até 3 correções concretas.
```

## Convenções Recomendadas

### Código e nomes técnicos: Inglês

```kotlin
class HelloController { }
fun sayHello() { }
val firstName: String
```

### Documentação e comentários de domínio: Português

```kotlin
// Valida se o nome tem tamanho mínimo de negócio
fun validateBusinessName(name: String) { }
```

### Branches

```bash
git checkout -b feature/adicionar-validacao-nome
git checkout -b bugfix/fix-mapper-nullpointer
git checkout -b docs/melhorar-readme
```

### Commits

```
feature: add name validation in HelloService

- Validate minimum name length
- Add test case for validation
- Update Mapper to use service validation
```

## Troubleshooting

### Build falha com "A problem occurred evaluating root project"

```bash
./gradlew clean
./gradlew build
```

### Testes passam localmente mas falham em CI

Verifique:
- [ ] Está usando H2 dialect PostgreSQL? (sim, está em `application.yml`)
- [ ] Migrations estão em `src/main/resources/db/migration/`?
- [ ] Testes estão limpando dados entre execuções?

### Erro de validação em request

Verifique:
- [ ] `@Valid` está no `@RequestBody`?
- [ ] Request DTO tem annotations de validação?
- [ ] Sem tratamento global de erro? → Ver seção "Global Error Handler" em TODO

## Dependências Principais

```gradle
implementation("org.springframework.boot:spring-boot-starter-web")
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
implementation("org.springframework.boot:spring-boot-starter-validation")
implementation("org.flywaydb:flyway-core")
runtimeOnly("com.h2database:h2")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
```

For Swagger (future improvement):
```gradle
implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.0+")
```

## Próximos Passos

1. ✅ Adicionar validações mais robustas
2. ⏳ Adicionar Global Error Handler (@ControllerAdvice)
3. ⏳ Adicionar OpenAPI/Swagger
4. ⏳ Adicionar profiles (dev, test, prod)
5. ⏳ Adicionar métricas e health checks

## Links úteis

- Spring Boot docs: https://spring.io/projects/spring-boot
- Kotlin docs: https://kotlinlang.org/docs
- JPA docs: https://spring.io/projects/spring-data-jpa
- Flyway docs: https://flywaydb.org/

## Dúvidas?

Abra uma issue ou envie mensagem ao seu Tech Lead.

