package com.example.demo.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringdocConfig {
    @Bean
    public GroupedOpenApi cmsAdminApiDoc() {
        return GroupedOpenApi.builder().group("Demo Api")
                .packagesToScan("com.example.demo.api").build();
    }
}
