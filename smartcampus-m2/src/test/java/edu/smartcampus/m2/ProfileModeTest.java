package edu.smartcampus.m2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

class ProfileModeTest {
    @Configuration @Import({ApiGuard.class,SessionIdentity.class,TokenIdentity.class,DemoAuthController.class})
    static class AuthConfig {@Bean ObjectMapper json(){return new ObjectMapper();}}
    @Test void mixedDemoMysqlMustFailClosed(){
        new ApplicationContextRunner().withUserConfiguration(AuthConfig.class)
            .withPropertyValues("spring.profiles.active=demo,mysql").run(context->assertThat(context).hasFailed());
    }
    @Test void mysqlUsesTokenAndHasNoDemoLogin(){
        new ApplicationContextRunner().withUserConfiguration(AuthConfig.class)
            .withPropertyValues("spring.profiles.active=mysql","M2_SHARED_TOKEN=synthetic-test-token-with-at-least-32-characters","M2_SHARED_STUDENT_ID=student1")
            .run(context->{assertThat(context).hasNotFailed();assertThat(context).doesNotHaveBean(DemoAuthController.class);assertThat(context).hasSingleBean(TokenIdentity.class);});
    }
}
