package com.vhvkhangg.personalprivatevault.drive.internal.files.config;

import com.vhvkhangg.personalprivatevault.drive.internal.config.DriveProperties;
import com.vhvkhangg.personalprivatevault.drive.internal.crypto.DriveCredentialCrypto;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.web.*;
import com.vhvkhangg.personalprivatevault.drive.internal.web.controller.DriveFileController;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.*;
import com.vhvkhangg.personalprivatevault.drive.internal.persistence.DriveConnectionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import java.nio.file.Path;
import java.time.Clock;

@Configuration(proxyBeanMethods=false)
@ConditionalOnProperty(prefix="ppv.drive.files",name="enabled",havingValue="true")
@EnableConfigurationProperties(DriveProperties.class)
public class DriveFilesConfiguration {
    public DriveFilesConfiguration(Environment env) {
        if(!env.getProperty("ppv.drive.enabled",Boolean.class,false))throw new IllegalStateException("Drive files require ppv.drive.enabled=true");
    }
    @Bean public DriveFileResources driveFileResources(DriveProperties properties) {
        String spool=properties.getFiles().getSpoolDir();
        if(spool==null || spool.isBlank())throw new IllegalStateException("ppv.drive.files.spool-dir is required when file operations are enabled");
        return new DriveFileResources(Path.of(spool),DriveFileResources.Limits.production());
    }
    @Bean public DriveFileAuthority driveFileAuthority(DriveConnectionRepository connections,DriveFolderRootRepository roots,DriveCredentialCrypto crypto,
            SubjectLifecycleManager lifecycle,DriveOAuthClient oauth,Clock clock,PlatformTransactionManager transactions) {
        return new DriveFileAuthority(connections,roots,crypto,lifecycle,oauth,clock,transactions);
    }
    @Bean public DriveFileHttpClient driveFileHttpClient(DriveFileAuthority authority){return new DriveFileHttpClient(authority);}
    @Bean public DriveFolderService driveFolderService(DriveFileAuthority authority,DriveFileHttpClient provider,DriveFolderRootRepository roots,Clock clock){return new DriveFolderService(authority,provider,roots,clock);}
    @Bean public DriveFileCursors driveFileCursors(Clock clock){return new DriveFileCursors(clock);}
    @Bean public DriveUploadIntentStore driveUploadIntentStore(DriveFileAuthority authority,DriveFolderRootRepository roots,DriveUploadIntentRepository intents,Clock clock){return new DriveUploadIntentStore(authority,roots,intents,clock);}
    @Bean public DriveFileService driveFileService(DriveFileAuthority authority,DriveFolderService folders,DriveFileHttpClient provider,DriveFileCursors cursors,DriveUploadIntentStore intents){return new DriveFileService(authority,folders,provider,cursors,intents);}
    @Bean public DriveFileController driveFileController(DriveFileService service){return new DriveFileController(service);}
    @Bean public DriveFileFilter driveFileFilter(DriveFileResources resources){return new DriveFileFilter(resources);}
    @Bean public FilterRegistrationBean<DriveFileFilter> driveFileFilterRegistration(DriveFileFilter filter) {
        var registration=new FilterRegistrationBean<>(filter);registration.setOrder(0);registration.setAsyncSupported(true);
        registration.addUrlPatterns("/api/v1/drive/connections/*");return registration;
    }
}
