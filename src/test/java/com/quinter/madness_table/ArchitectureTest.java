package com.quinter.madness_table;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.quinter.madness_table", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule rulesEngineIsPlainJava = noClasses()
            .that().resideInAPackage("..rules..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta..");
}
