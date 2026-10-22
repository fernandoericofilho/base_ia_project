package com.base.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaField
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

// Regras de arquitetura que quebram o build (REGRAS-DO-SISTEMA.md §2.3, 2026-10-22).
// Só roda no `test` (sem Docker).
class ArchitectureTest {

    private val classes: JavaClasses = ClassFileImporter()
        .withImportOption(ImportOption.DoNotIncludeTests())
        .importPackages("com.base")

    // TEST-ARCH-01: injeção por campo é proibida; dependência entra só pelo construtor.
    @Test
    fun `TEST-ARCH-01 - no field may be annotated with Autowired`() {
        noFields().should().beAnnotatedWith(Autowired::class.java).check(classes)
    }

    // TEST-ARCH-02: valor sensível usa BigDecimal, nunca Double/Float (primitivo ou boxed).
    @Test
    fun `TEST-ARCH-02 - models and dtos must not use floating point fields`() {
        noFields()
            .that().areDeclaredInClassesThat().resideInAnyPackage("..models..", "..dtos..")
            .should().haveRawType("double")
            .orShould().haveRawType("java.lang.Double")
            .orShould().haveRawType("float")
            .orShould().haveRawType("java.lang.Float")
            .check(classes)
    }

    // TEST-ARCH-03: toda associação JPA é LAZY e declarada explicitamente.
    // O default de @ManyToOne/@OneToOne é EAGER, então a anotação sem `fetch` também viola.
    // allowEmptyShould: o template ainda não tem associações; a regra vale assim que a primeira existir.
    @Test
    fun `TEST-ARCH-03 - ManyToOne and OneToOne must be explicitly LAZY`() {
        fields()
            .that().areAnnotatedWith(ManyToOne::class.java)
            .or().areAnnotatedWith(OneToOne::class.java)
            .should(beLazy)
            .allowEmptyShould(true)
            .check(classes)
    }

    // TEST-ARCH-04: camadas. Controller passa pelo Service; nunca acessa Repository.
    @Test
    fun `TEST-ARCH-04 - controllers must not depend on repositories`() {
        noClasses()
            .that().resideInAPackage("..controllers..")
            .should().dependOnClassesThat().resideInAPackage("..repositories..")
            .check(classes)
    }

    // TEST-ARCH-05: camadas. Service recebe e devolve DTO; não conhece Controller nem Request/Response.
    @Test
    fun `TEST-ARCH-05 - services must not depend on controllers`() {
        noClasses()
            .that().resideInAPackage("..services..")
            .should().dependOnClassesThat().resideInAPackage("..controllers..")
            .check(classes)
    }

    private val beLazy = object : ArchCondition<JavaField>("be annotated with fetch = LAZY") {
        override fun check(item: JavaField, events: ConditionEvents) {
            val fetch: FetchType = when {
                item.isAnnotatedWith(ManyToOne::class.java) -> item.getAnnotationOfType(ManyToOne::class.java).fetch
                item.isAnnotatedWith(OneToOne::class.java) -> item.getAnnotationOfType(OneToOne::class.java).fetch
                else -> return
            }
            if (fetch != FetchType.LAZY) {
                events.add(SimpleConditionEvent.violated(item, "${item.fullName} uses fetch = $fetch"))
            }
        }
    }
}
