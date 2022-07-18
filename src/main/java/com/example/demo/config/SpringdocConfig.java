package com.example.demo.config;

import com.example.demo.config.custom.CustomDataRestDelegatingMethodParameterCustomizer;
import com.example.demo.config.custom.CustomGenericParameterService;
import com.example.demo.config.custom.CustomRequestService;
import org.springdoc.core.*;
import org.springdoc.core.customizers.DelegatingMethodParameterCustomizer;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.core.providers.RepositoryRestConfigurationProvider;
import org.springdoc.core.providers.SpringDataWebPropertiesProvider;
import org.springdoc.core.providers.WebConversionServiceProvider;
import org.springdoc.webmvc.core.RequestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.util.List;
import java.util.Optional;

@Configuration
public class SpringdocConfig {
    @Bean
    public GroupedOpenApi cmsAdminApiDoc() {
        return GroupedOpenApi.builder().group("Demo Api")
                .packagesToScan("com.example.demo.api").build();
    }

    @Bean
    @Lazy(false)
    RequestService requestBuilder(GenericParameterService parameterBuilder, RequestBodyService requestBodyService,
                                  OperationService operationService, Optional<List<ParameterCustomizer>> parameterCustomizers,
                                  LocalVariableTableParameterNameDiscoverer localSpringDocParameterNameDiscoverer) {
        return new CustomRequestService(parameterBuilder, requestBodyService, operationService, parameterCustomizers,
                localSpringDocParameterNameDiscoverer);
    }

    @Bean
    @Lazy(false)
    RequestBodyService requestBodyBuilder(GenericParameterService parameterBuilder) {
        return new RequestBodyService(parameterBuilder);
    }

    @Bean
    @Lazy(false)
    GenericParameterService parameterBuilder(PropertyResolverUtils propertyResolverUtils,
                                             Optional<DelegatingMethodParameterCustomizer> optionalDelegatingMethodParameterCustomizer,
                                             Optional<WebConversionServiceProvider> optionalWebConversionServiceProvider,
                                             ObjectMapperProvider objectMapperProvider) {
        return new CustomGenericParameterService(propertyResolverUtils, optionalDelegatingMethodParameterCustomizer,
                optionalWebConversionServiceProvider, objectMapperProvider);
    }

    @Bean
    @Lazy(false)
    public CustomDataRestDelegatingMethodParameterCustomizer delegatingMethodParameterCustomizer(
            Optional<SpringDataWebPropertiesProvider> optionalSpringDataWebPropertiesProvider,
            Optional<RepositoryRestConfigurationProvider> optionalRepositoryRestConfiguration) {
        return new CustomDataRestDelegatingMethodParameterCustomizer(optionalSpringDataWebPropertiesProvider,
                optionalRepositoryRestConfiguration);
    }
}
