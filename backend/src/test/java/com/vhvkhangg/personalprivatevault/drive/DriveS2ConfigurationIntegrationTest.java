package com.vhvkhangg.personalprivatevault.drive;

import com.vhvkhangg.personalprivatevault.drive.internal.config.DriveProperties;
import com.vhvkhangg.personalprivatevault.drive.internal.files.config.DriveFilesConfiguration;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileResources;
import com.vhvkhangg.personalprivatevault.drive.internal.web.controller.DriveFileController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.env.MockEnvironment;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.*;

class DriveS2ConfigurationIntegrationTest {
    @Test void absentFilesFlagRegistersNoFileBeans() {
        new ApplicationContextRunner().withUserConfiguration(DriveFilesConfiguration.class).run(context -> {
            assertThat(context).doesNotHaveBean(DriveFileController.class).doesNotHaveBean(DriveFileResources.class);
        });
        var defaults=new DriveProperties();assertThat(defaults.isEnabled()).isFalse();assertThat(defaults.getFiles().isEnabled()).isFalse();
    }
    @Test void filesEnabledWithoutDriveFailsBeforeCredentialsOrProviderConstruction() {
        new ApplicationContextRunner().withUserConfiguration(DriveFilesConfiguration.class)
                .withPropertyValues("ppv.drive.enabled=false","ppv.drive.files.enabled=true").run(context -> {
                    assertThat(context).hasFailed();assertThat(context.getStartupFailure()).hasStackTraceContaining("Drive files require");
                });
    }
    @Test void enabledFilesRequireAnExternalExistingWritableSpool() throws Exception {
        var config=new DriveFilesConfiguration(new MockEnvironment().withProperty("ppv.drive.enabled","true"));
        var props=new DriveProperties();assertThatThrownBy(() -> config.driveFileResources(props)).hasMessageContaining("spool-dir is required");
        props.getFiles().setSpoolDir(Path.of(".").toAbsolutePath().toString());
        assertThatThrownBy(() -> config.driveFileResources(props)).hasMessageContaining("external");
        Path directory=Files.createTempDirectory("s2-config-spool-");
        try {
            props.getFiles().setSpoolDir(directory.resolve("missing").toString());
            assertThatThrownBy(() -> config.driveFileResources(props)).hasMessageContaining("existing");
            props.getFiles().setSpoolDir(directory.toString());
            try(var resources=config.driveFileResources(props)){assertThat(resources.activeOperations()).isZero();}
        }finally{Files.deleteIfExists(directory);}
    }
    @Test void crashOrphansCountAgainstBothSpoolCountAndByteReservation() throws Exception {
        Path dir=Files.createTempDirectory("s2-orphan-");Path orphan=dir.resolve("ppv-gdi26-upload-"+java.util.UUID.randomUUID()+".tmp");
        try {
            Files.write(orphan,new byte[]{1});
            try(var resources=new DriveFileResources(dir,DriveFileResources.Limits.production());var first=resources.open(true)){
                assertThatThrownBy(() -> resources.open(true)).isInstanceOf(com.vhvkhangg.personalprivatevault.drive.internal.files.application.DriveFileException.class);
                assertThat(resources.activeOperations()).isEqualTo(1);
            }
            assertThat(Files.exists(orphan)).isTrue();
        }finally{Files.deleteIfExists(orphan);Files.deleteIfExists(dir);}
    }
}
