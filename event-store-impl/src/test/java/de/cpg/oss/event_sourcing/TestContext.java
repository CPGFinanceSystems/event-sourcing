package de.cpg.oss.event_sourcing;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestContext {

    @Bean
    public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertyPlaceholderConfigurer();
    }
}
