package de.cpg.oss.verita.configuration.mock;

import de.cpg.oss.verita.service.CommandBus;
import de.cpg.oss.verita.service.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.Assert.assertNotNull;

public class VeritaMockAutoConfigurationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @Before
    public void init() {
        EnvironmentTestUtils.addEnvironment(this.context,
                "verita.applicationId:VeritaTest");
        this.context.register(VeritaMockAutoConfiguration.class,
                        PropertyPlaceholderAutoConfiguration.class);
        this.context.refresh();
    }

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void defaultBeans() throws Exception {
        assertNotNull(this.context.getBean(CommandBus.class));
        assertNotNull(this.context.getBean(EventBus.class));
    }
}
