package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.*;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/** Separate real HTTP controller, with a test-only pause after production inspection preregistration. */
public final class RagRetrievalHttpControllerFixture {
    private RagRetrievalHttpControllerFixture() {}

    public static void main(String[] arguments) throws Exception {
        var properties=new Properties();
        try(var input=Files.newInputStream(Path.of(arguments[0]))) {properties.load(input);}
        var application=new SpringApplication(PersonalPrivateVaultApplication.class,CrashPoint.class);
        application.setLogStartupInfo(false);
        try {
            var context=application.run(properties.entrySet().stream().map(e->"--"+e.getKey()+"="+e.getValue()).toArray(String[]::new));
            Files.writeString(Path.of(properties.getProperty("fixture.port-file")),context.getEnvironment().getRequiredProperty("local.server.port"));
        } catch(Throwable failure) {
            Throwable root=failure;while(root.getCause()!=null && root.getCause()!=root) root=root.getCause();
            Files.writeString(Path.of(properties.getProperty("fixture.port-file")+".failure"),root.getClass().getName());
            throw failure;
        }
    }

    @TestConfiguration(proxyBeanMethods=false)
    static class CrashPoint {
        @Bean static BeanPostProcessor holdAfterPreregistration(ConfigurableApplicationContext context,Environment environment) {
            var once=new AtomicBoolean();
            return new BeanPostProcessor() {
                @Override public Object postProcessAfterInitialization(Object bean,String name) {
                    if(!(bean instanceof RagOriginalBytesStore)) return bean;
                    var proxy=new ProxyFactory(bean);proxy.setProxyTargetClass(true);
                    proxy.addAdvice((MethodInterceptor)call->{
                        String phase=environment.getRequiredProperty("fixture.phase");
                        boolean candidate=("prepare".equals(phase) && call.getMethod().getName().equals("verifyIntegrity"))
                                || ("reader".equals(phase) && call.getMethod().getName().equals("readRevisionBytes"));
                        if(candidate && !once.get()) {
                            var jdbc=context.getBean(JdbcTemplate.class);
                            int count=jdbc.queryForObject("SELECT count(*) FROM rag.retrieval_inspections WHERE affected_file_source_id=? AND state='UNRESOLVED' AND producer=?",
                                    Integer.class,call.getArguments()[0],"prepare".equals(phase)?"QUERY_PREPARE":"QUERY_EVIDENCE");
                            if(count>0 && once.compareAndSet(false,true)) {
                                Files.writeString(Path.of(environment.getRequiredProperty("fixture.pause-file")),"PREREGISTERED");
                                new CountDownLatch(1).await(); // The test kills this owned process; it never releases the pause.
                            }
                        }
                        return call.proceed();
                    });
                    return proxy.getProxy();
                }
            };
        }
    }
}
