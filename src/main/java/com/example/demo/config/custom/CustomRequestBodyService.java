package com.example.demo.config.custom;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.MethodAttributes;
import org.springdoc.core.ParameterInfo;
import org.springdoc.core.RequestBodyInfo;
import org.springdoc.core.RequestBodyService;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestPart;

public class CustomRequestBodyService extends RequestBodyService {

	private final CustomGenericParameterService parameterBuilder;

	/**
	 * Instantiates a new Request body builder.
	 * @param parameterBuilder the parameter builder
	 */
	public CustomRequestBodyService(CustomGenericParameterService parameterBuilder) {
		super(parameterBuilder);
		this.parameterBuilder = parameterBuilder;
	}

	@Override
	public void calculateRequestBodyInfo(Components components, MethodAttributes methodAttributes,
			ParameterInfo parameterInfo, RequestBodyInfo requestBodyInfo) {
		RequestBody requestBody = requestBodyInfo.getRequestBody();
		MethodParameter methodParameter = parameterInfo.getMethodParameter();
		// Get it from parameter level, if not present
		if (requestBody == null) {
			io.swagger.v3.oas.annotations.parameters.RequestBody requestBodyDoc = methodParameter
					.getParameterAnnotation(io.swagger.v3.oas.annotations.parameters.RequestBody.class);
			requestBody = this.buildRequestBodyFromDoc(requestBodyDoc, methodAttributes, components).orElse(null);
		}

		RequestPart requestPart = methodParameter.getParameterAnnotation(RequestPart.class);
		String paramName = null;
		if (requestPart != null) {
			paramName = StringUtils.defaultIfEmpty(requestPart.value(), requestPart.name());
			parameterInfo.setRequired(requestPart.required());
			parameterInfo.setRequestPart(true);
		}
		paramName = StringUtils.defaultIfEmpty(paramName, parameterInfo.getpName());
		parameterInfo.setpName(paramName);

		requestBody = buildRequestBody(requestBody, components, methodAttributes, parameterInfo, requestBodyInfo);
		requestBodyInfo.setRequestBody(requestBody);
	}

	private RequestBody buildRequestBody(RequestBody requestBody, Components components,
			MethodAttributes methodAttributes, ParameterInfo parameterInfo, RequestBodyInfo requestBodyInfo) {
		if (requestBody == null) {
			requestBody = new RequestBody();
			requestBodyInfo.setRequestBody(requestBody);
		}

		if (requestBody.getContent() == null) {
			Schema<?> schema = parameterBuilder.calculateSchema(components, parameterInfo, requestBodyInfo,
					methodAttributes.getJsonViewAnnotationForRequestBody());
			buildContent(requestBody, methodAttributes, schema);
		}
		else if (!methodAttributes.isWithResponseBodySchemaDoc()) {
			Schema<?> schema = parameterBuilder.calculateSchema(components, parameterInfo, requestBodyInfo,
					methodAttributes.getJsonViewAnnotationForRequestBody());
			mergeContent(requestBody, methodAttributes, schema);
		}
		return requestBody;
	}

	private void buildContent(RequestBody requestBody, MethodAttributes methodAttributes, Schema<?> schema) {
		Content content = new Content();
		buildContent(requestBody, methodAttributes, schema, content);
	}

	private void mergeContent(RequestBody requestBody, MethodAttributes methodAttributes, Schema<?> schema) {
		Content content = requestBody.getContent();
		buildContent(requestBody, methodAttributes, schema, content);
	}

	private void buildContent(RequestBody requestBody, MethodAttributes methodAttributes, Schema<?> schema,
			Content content) {
		for (String value : methodAttributes.getMethodConsumes()) {
			MediaType mediaTypeObject = new MediaType();
			mediaTypeObject.setSchema(schema);
			MediaType mediaType = content.get(value);
			if (mediaType != null) {
				if (mediaType.getExample() != null) {
					mediaTypeObject.setExample(mediaType.getExample());
				}
				if (mediaType.getExamples() != null) {
					mediaTypeObject.setExamples(mediaType.getExamples());
				}
				if (mediaType.getEncoding() != null) {
					mediaTypeObject.setEncoding(mediaType.getEncoding());
				}
			}
			content.addMediaType(value, mediaTypeObject);
		}
		requestBody.setContent(content);
	}

}
