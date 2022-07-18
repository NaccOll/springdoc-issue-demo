package com.example.demo.config.custom;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.api.annotations.ParameterObject;
import org.springdoc.core.DelegatingMethodParameter;
import org.springdoc.core.converters.AdditionalModelsConverter;
import org.springdoc.core.customizers.DelegatingMethodParameterCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class CustomDelegatingMethodParameter extends MethodParameter {

	/**
	 * The Delegate.
	 */
	private MethodParameter delegate;

	/**
	 * The Additional parameter annotations.
	 */
	private Annotation[] additionalParameterAnnotations;

	/**
	 * The Parameter name.
	 */
	private String parameterName;

	/**
	 * The Is parameter object.
	 */
	private boolean isParameterObject;

	/**
	 * The Is not required.
	 */
	private boolean isNotRequired;

	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingMethodParameter.class);

	/**
	 * Instantiates a new Delegating method parameter.
	 * @param delegate the delegate
	 * @param parameterName the parameter name
	 * @param additionalParameterAnnotations the additional parameter annotations
	 * @param isParameterObject the is parameter object
	 * @param isNotRequired the is required
	 */
	public CustomDelegatingMethodParameter(MethodParameter delegate, String parameterName,
			Annotation[] additionalParameterAnnotations, boolean isParameterObject, boolean isNotRequired) {
		super(delegate);
		this.delegate = delegate;
		this.additionalParameterAnnotations = additionalParameterAnnotations;
		this.parameterName = parameterName;
		this.isParameterObject = isParameterObject;
		this.isNotRequired = isNotRequired;
	}

	/**
	 * Customize method parameter [ ].
	 * @param pNames the p names
	 * @param parameters the parameters
	 * @param optionalDelegatingMethodParameterCustomizer the optional delegating method
	 * parameter customizer
	 * @return the method parameter [ ]
	 */
	public static MethodParameter[] customize(String[] pNames, MethodParameter[] parameters,
			Optional<DelegatingMethodParameterCustomizer> optionalDelegatingMethodParameterCustomizer,
			RequestMethod requestMethod) {
		List<MethodParameter> explodedParameters = new ArrayList<>();
		for (int i = 0; i < parameters.length; ++i) {
			MethodParameter p = parameters[i];
			Class<?> paramClass = AdditionalModelsConverter.getParameterObjectReplacement(p.getParameterType());

			if (!MethodParameterPojoExtractor.isSimpleType(paramClass)
					&& (p.hasParameterAnnotation(ParameterObject.class)
							|| AnnotatedElementUtils.isAnnotated(paramClass, ParameterObject.class))) {
				MethodParameterPojoExtractor.extractFrom(paramClass).forEach(methodParameter -> {
					optionalDelegatingMethodParameterCustomizer
							.ifPresent(customizer -> customizer.customize(p, methodParameter));
					explodedParameters.add(methodParameter);
				});
			}
			// TODO remove @ParameterObject
			else if (!MethodParameterPojoExtractor.isSimpleType(paramClass)
					&& !(p.hasParameterAnnotation(RequestBody.class)
							|| p.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestBody.class)
							|| p.hasParameterAnnotation(RequestPart.class))) {
				MethodParameterPojoExtractor.extractFrom(paramClass).forEach(methodParameter -> {
					optionalDelegatingMethodParameterCustomizer
							.ifPresent(customizer -> customizer.customize(p, methodParameter));
					explodedParameters.add(methodParameter);
				});
			}
			// TODO remove @ParameterObject END
			else {
				String name = pNames != null ? pNames[i] : p.getParameterName();
				explodedParameters.add(new CustomDelegatingMethodParameter(p, name, null, false, false));
			}
		}
		return explodedParameters.toArray(new MethodParameter[0]);
	}

	@Override
	@NonNull
	public Annotation[] getParameterAnnotations() {
		return ArrayUtils.addAll(delegate.getParameterAnnotations(), additionalParameterAnnotations);
	}

	@Override
	public String getParameterName() {
		return parameterName;
	}

	@Override
	public Method getMethod() {
		return delegate.getMethod();
	}

	@Override
	public Constructor<?> getConstructor() {
		return delegate.getConstructor();
	}

	@Override
	public Class<?> getDeclaringClass() {
		return delegate.getDeclaringClass();
	}

	@Override
	public Member getMember() {
		return delegate.getMember();
	}

	@Override
	public AnnotatedElement getAnnotatedElement() {
		return delegate.getAnnotatedElement();
	}

	@Override
	public Executable getExecutable() {
		return delegate.getExecutable();
	}

	@Override
	public MethodParameter withContainingClass(Class<?> containingClass) {
		return delegate.withContainingClass(containingClass);
	}

	@Override
	public Class<?> getContainingClass() {
		return delegate.getContainingClass();
	}

	@Override
	public Class<?> getParameterType() {
		return delegate.getParameterType();
	}

	@Override
	public Type getGenericParameterType() {
		return delegate.getGenericParameterType();
	}

	@Override
	public Class<?> getNestedParameterType() {
		return delegate.getNestedParameterType();
	}

	@Override
	public Type getNestedGenericParameterType() {
		return delegate.getNestedGenericParameterType();
	}

	@Override
	public void initParameterNameDiscovery(ParameterNameDiscoverer parameterNameDiscoverer) {
		delegate.initParameterNameDiscovery(parameterNameDiscoverer);
	}

	/**
	 * Is not required boolean.
	 * @return the boolean
	 */
	public boolean isNotRequired() {
		return isNotRequired;
	}

	/**
	 * Sets not required.
	 * @param notRequired the not required
	 */
	public void setNotRequired(boolean notRequired) {
		isNotRequired = notRequired;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		CustomDelegatingMethodParameter that = (CustomDelegatingMethodParameter) o;
		return Objects.equals(delegate, that.delegate)
				&& Arrays.equals(additionalParameterAnnotations, that.additionalParameterAnnotations)
				&& Objects.equals(parameterName, that.parameterName);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(super.hashCode(), delegate, parameterName);
		result = 31 * result + Arrays.hashCode(additionalParameterAnnotations);
		return result;
	}

	/**
	 * Is parameter object boolean.
	 * @return the boolean
	 */
	public boolean isParameterObject() {
		return isParameterObject;
	}

	/**
	 * Return a variant of this {@code MethodParameter} which refers to the given
	 * containing class.
	 * @param containingClass a specific containing class (potentially a subclass of the
	 * declaring class, e.g. substituting a type variable) A copy of spring
	 * withContainingClass, to keep compatibility with older spring versions
	 * @see #getParameterType()
	 */
	public static MethodParameter changeContainingClass(MethodParameter methodParameter,
			@Nullable Class<?> containingClass) {
		MethodParameter result = methodParameter.clone();
		try {
			Field containingClassField = FieldUtils.getDeclaredField(result.getClass(), "containingClass", true);
			containingClassField.set(result, containingClass);
			Field parameterTypeField = FieldUtils.getDeclaredField(result.getClass(), "parameterType", true);
			parameterTypeField.set(result, null);
		}
		catch (IllegalAccessException e) {
			LOGGER.warn(e.getMessage());
		}
		return result;
	}

}