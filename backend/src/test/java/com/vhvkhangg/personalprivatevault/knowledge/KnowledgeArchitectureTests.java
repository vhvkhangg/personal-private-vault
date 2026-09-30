package com.vhvkhangg.personalprivatevault.knowledge;

import com.tngtech.archunit.core.domain.JavaClass;
import com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.NamedInterface;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeArchitectureTests {

    @Test
    @DisplayName("Verifies that module structure satisfies Spring Modulith constraints")
    void verifiesModularStructure() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        assertThat(modules).isNotNull();
        modules.verify();
    }

    @Test
    @DisplayName("Verifies knowledge parent facade exposes only api named interface and no internals")
    void verifiesKnowledgeParentFacadeNamedInterface() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var knowledgeModule = modules.getModuleByName("knowledge").orElseThrow();

        assertThat(knowledgeModule.getNamedInterfaces().stream()
                .filter(NamedInterface::isNamed)
                .map(NamedInterface::getName))
                .containsExactly("api");

        var apiInterface = knowledgeModule.getNamedInterfaces().getByName("api").orElseThrow();
        var apiClassNames = apiInterface.asJavaClasses()
                .map(JavaClass::getName)
                .toList();

        assertThat(apiClassNames).contains(
                "com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeOperations",
                "com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeStudyItemCommand",
                "com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeInformationItemCommand",
                "com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeVocabularyItemCommand",
                "com.vhvkhangg.personalprivatevault.knowledge.api.CreateKnowledgeNoteCommand",
                "com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyItemView",
                "com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationItemView",
                "com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyItemView",
                "com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeNoteView"
        );

        for (var namedInterface : knowledgeModule.getNamedInterfaces()) {
            assertThat(namedInterface.asJavaClasses().map(JavaClass::getName))
                    .noneMatch(name -> name.contains(".internal."));
        }
    }

    @Test
    @DisplayName("Verifies nested knowledge modules are properly encapsulated with no internal exposure")
    void verifiesNestedKnowledgeModulesEncapsulation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);

        String[] nestedModuleNames = {
                "knowledge.study",
                "knowledge.information",
                "knowledge.vocabulary",
                "knowledge.note"
        };

        for (String moduleName : nestedModuleNames) {
            var module = modules.getModuleByName(moduleName).orElseThrow(
                    () -> new AssertionError("Expected nested module: " + moduleName));

            for (var namedInterface : module.getNamedInterfaces()) {
                assertThat(namedInterface.asJavaClasses().map(JavaClass::getName))
                        .noneMatch(name -> name.contains(".internal."));
            }
        }
    }
}
