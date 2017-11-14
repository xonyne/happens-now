package org.xonyne.events.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.xonyne.events.service.LoadEventsService;

@Configuration
@EnableScheduling
@ComponentScan(basePackages={"org.xonyne"})
public class ConfigurationSource {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocations(
        		resourcePatternResolver.getResource("classpath:configuration.properties"));
        
        return propertySourcesPlaceholderConfigurer;
    }
    
    public LoadEventsService loadEventsService(){
    	return new LoadEventsService();
    }
    
}
