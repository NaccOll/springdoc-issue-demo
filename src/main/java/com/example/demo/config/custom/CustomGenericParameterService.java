package com.example.demo.config.custom;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.ReturnTypeParser;
import org.springdoc.core.*;
import org.springdoc.core.customizers.DelegatingMethodParameterCustomizer;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.core.providers.WebConversionServiceProvider;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.web.context.request.RequestScope;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomGenericParameterService extends GenericParameterService {

	/**
	 * The constant FILE_TYPES.
	 */
	private static final List<Class<?>> FILE_TYPES = new ArrayList<>();

	/**
	 * The Optional delegating method parameter customizer.
	 */
	private final Optional<DelegatingMethodParameterCustomizer> optionalDelegatingMethodParameterCustomizer;

	/**
	 * The Web conversion service.
	 */
	private final Optional<WebConversionServiceProvider> optionalWebConversionServiceProvider;

	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericParameterService.class);

	static {
		FILE_TYPES.add(MultipartFile.class);
		FILE_TYPES.add(Resource.class);
		FILE_TYPES.add(MultipartRequest.class);
	}

	/**
	 * The Property resolver utils.
	 */
	private final PropertyResolverUtils propertyResolverUtils;

	/**
	 * The Expression context.
	 */
	private BeanExpressionContext expressionContext;

	/**
	 * The Configurable bean factory.
	 */
	private ConfigurableBeanFactory configurableBeanFactory;

	/**
	 * Instantiates a new Generic parameter builder.
	 * @param propertyResolverUtils the property resolver utils
	 * @param optionalDelegatingMethodParameterCustomizer the optional delegating method
	 * parameter customizer
	 * @param optionalWebConversionServiceProvider
	 */
	public CustomGenericParameterService(PropertyResolverUtils propertyResolverUtils,
			Optional<DelegatingMethodParameterCustomizer> optionalDelegatingMethodParameterCustomizer,
			Optional<WebConversionServiceProvider> optionalWebConversionServiceProvider,
			ObjectMapperProvider objectMapperProvider) {
		super(propertyResolverUtils, optionalDelegatingMethodParameterCustomizer, optionalWebConversionServiceProvider,
				objectMapperProvider);
		this.propertyResolverUtils = propertyResolverUtils;
		this.optionalDelegatingMethodParameterCustomizer = optionalDelegatingMethodParameterCustomizer;
		this.optionalWebConversionServiceProvider = optionalWebConversionServiceProvider;
		this.configurableBeanFactory = propertyResolverUtils.getFactory();
		this.expressionContext = (configurableBeanFactory != null
				? new BeanExpressionContext(configurableBeanFactory, new RequestScope()) : null);

	}

	Schema calculateSchema(Components components, ParameterInfo parameterInfo, RequestBodyInfo requestBodyInfo,
			JsonView jsonView) {
		Schema schemaN;
		String paramName = parameterInfo.getpName();
		MethodParameter methodParameter = parameterInfo.getMethodParameter();

		if (parameterInfo.getParameterModel() == null || parameterInfo.getParameterModel().getSchema() == null) {
			Type type = ReturnTypeParser.getType(methodParameter);
			if (type instanceof Class && optionalWebConversionServiceProvider.isPresent()) {
				WebConversionServiceProvider webConversionServiceProvider = optionalWebConversionServiceProvider.get();
				if (!MethodParameterPojoExtractor.isSwaggerPrimitiveType((Class) type) && methodParameter
						.getParameterType().getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class) == null) {
					type = webConversionServiceProvider.getSpringConvertedType(methodParameter.getParameterType());
				}
			}
			schemaN = SpringDocAnnotationsUtils.extractSchema(components, type, jsonView,
					methodParameter.getParameterAnnotations());
		}
		else {
			schemaN = parameterInfo.getParameterModel().getSchema();
		}

		if (requestBodyInfo != null) {
			schemaN = calculateRequestBodySchema(components, parameterInfo, requestBodyInfo, schemaN, paramName);
		}

		return schemaN;
	}

	public Schema calculateRequestBodySchema(Components components, ParameterInfo parameterInfo,
			RequestBodyInfo requestBodyInfo, Schema schemaN, String paramName) {
		if (schemaN != null && StringUtils.isEmpty(schemaN.getDescription())
				&& parameterInfo.getParameterModel() != null) {
			String description = parameterInfo.getParameterModel().getDescription();
			if (schemaN.get$ref() != null && schemaN.get$ref().contains(AnnotationsUtils.COMPONENTS_REF)) {
				String key = schemaN.get$ref().substring(21);
				Schema existingSchema = components.getSchemas().get(key);
				if (!StringUtils.isEmpty(description)) {
					existingSchema.setDescription(description);
				}
			}
			else {
				schemaN.setDescription(description);
			}
		}

		if (requestBodyInfo.getMergedSchema() != null) {
			requestBodyInfo.getMergedSchema().addProperty(paramName, schemaN);
			schemaN = requestBodyInfo.getMergedSchema();
		}
		else if (parameterInfo.isRequestPart() || schemaN instanceof FileSchema
				|| schemaN instanceof ArraySchema && ((ArraySchema) schemaN).getItems() instanceof FileSchema) {
			schemaN = new ObjectSchema().addProperty(paramName, schemaN);
			requestBodyInfo.setMergedSchema(schemaN);
		}
		else {
			requestBodyInfo.addProperties(paramName, schemaN);
		}

		if (requestBodyInfo.getMergedSchema() != null && parameterInfo.isRequired()) {
			requestBodyInfo.getMergedSchema().addRequiredItem(parameterInfo.getpName());
		}

		return schemaN;
	}

}
