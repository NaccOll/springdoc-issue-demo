package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.time.LocalDate;

@Configuration
@EnableOpenApi
public class SpringfoxConfig {
    @Bean
    Docket apiDoc() {
        return new Docket(DocumentationType.OAS_30).groupName("Demo Api").select()
                .apis(RequestHandlerSelectors.basePackage("com.example.demo.api"))
                .paths(PathSelectors.any()).build().directModelSubstitute(LocalDate.class,
                        String.class)
                .genericModelSubstitutes(ResponseEntity.class).useDefaultResponseMessages(false);
    }
}
