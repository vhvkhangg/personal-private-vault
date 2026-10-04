package com.vhvkhangg.personalprivatevault.collection;

import com.tngtech.archunit.core.domain.JavaClass;
import com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.NamedInterface;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionArchitectureTests {

    @Test
    @DisplayName("Verifies that module structure satisfies Spring Modulith constraints")
    void verifiesModularStructure() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        assertThat(modules).isNotNull();
        modules.verify();
    }

    @Test
    @DisplayName("Verifies collection parent facade exposes api and search named interfaces and no internals")
    void verifiesCollectionParentFacadeNamedInterface() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var collectionModule = modules.getModuleByName("collection").orElseThrow();

        assertThat(collectionModule.getNamedInterfaces().stream()
                .filter(NamedInterface::isNamed)
                .map(NamedInterface::getName))
                .containsExactlyInAnyOrder("api", "search");

        var apiInterface = collectionModule.getNamedInterfaces().getByName("api").orElseThrow();
        var apiClassNames = apiInterface.asJavaClasses()
                .map(JavaClass::getName)
                .toList();

        assertThat(apiClassNames).contains(
                "com.vhvkhangg.personalprivatevault.collection.api.CollectionOperations",
                "com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionMusicCommand",
                "com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionShoppingItemCommand",
                "com.vhvkhangg.personalprivatevault.collection.api.CreateCollectionSoftwareItemCommand",
                "com.vhvkhangg.personalprivatevault.collection.api.CollectionMusicView",
                "com.vhvkhangg.personalprivatevault.collection.api.CollectionShoppingItemView",
                "com.vhvkhangg.personalprivatevault.collection.api.CollectionSoftwareItemView"
        );

        for (var namedInterface : collectionModule.getNamedInterfaces()) {
            assertThat(namedInterface.asJavaClasses().map(JavaClass::getName))
                    .noneMatch(name -> name.contains(".internal."));
        }
    }

    @Test
    @DisplayName("Verifies nested collection modules are properly encapsulated with no internal exposure")
    void verifiesNestedCollectionModulesEncapsulation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);

        String[] nestedModuleNames = {
                "collection.music",
                "collection.shopping",
                "collection.software"
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
