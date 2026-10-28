package com.alertamujer;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Hexagonal architecture of the service's modules: the domain is plain Java, the use cases only know
 * ports, and the inbound adapters reach the outside world only through the use cases.
 */
@AnalyzeClasses(packages = "com.alertamujer", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domainIsPlainJava = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application..", "..adapter..",
                    "org.springframework..", "jakarta.persistence..", "lombok..")
            .because("the domain holds the business rules without depending on any technology");

    @ArchTest
    static final ArchRule useCasesOnlyKnowPorts = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..adapter..")
            .because("the use cases talk to the outside world through their ports");

    @ArchTest
    static final ArchRule inboundAdaptersUseTheUseCases = noClasses()
            .that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.out..")
            .because("controllers, schedulers and listeners go through the use cases, not the persistence");
}
