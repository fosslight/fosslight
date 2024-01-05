/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.config;

import java.util.List;
import java.util.Locale;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.resource.PathResourceResolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import oss.fosslight.common.CoConstDef;
import oss.fosslight.interceptor.AjaxInterceptor;
import springfox.documentation.spring.web.json.Json;

@Configuration
@EnableWebMvc
@EnableCaching
@ComponentScan(value=AppConstBean.APP_COMPONENT_SCAN_PACKAGE)
public class WebConfig implements WebMvcConfigurer {
	private final Gson gson = new GsonBuilder().registerTypeAdapter(Json.class, new SpringfoxJsonToGsonAdapter()).create();

	private static class SpringfoxJsonToGsonAdapter implements JsonSerializer<Json> {
		@SuppressWarnings("deprecation")
		@Override
		public JsonElement serialize(Json json, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
			final JsonParser parser = new JsonParser();

			return parser.parse(json.value());
		}
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
		gsonHttpMessageConverter.setGson(gson);
		converters.add(gsonHttpMessageConverter);
		converters.add(new ResourceHttpMessageConverter(true));
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
		registry.addInterceptor(new AjaxInterceptor()).excludePathPatterns("/error", "/error/**","/viewer","/viewer/**");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler( CoConstDef.STATIC_RESOURCES_URL_PATTERNS)
				.addResourceLocations(CoConstDef.CLASSPATH_RESOURCE_LOCATIONS)
				.setCachePeriod(60*60*24*7)// 60*60*24*7 => 1 week
				.resourceChain(true)
				.addResolver(new PathResourceResolver());

		registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
		resolver.setFallbackPageable(PageRequest.of(CoConstDef.DEFAULT_PAGE_NUMBER, CoConstDef.DEFAULT_PAGE_SIZE));
		argumentResolvers.add(resolver);
		WebMvcConfigurer.super.addArgumentResolvers(argumentResolvers);
	}

	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		// Search properties in "messages/message" below WEB-INF folder
		messageSource.setBasename("messages/message");
		messageSource.setDefaultEncoding("UTF-8");
		// # -1 : never reload, 0 always reload
		messageSource.setCacheSeconds(60);

		return messageSource;
	}

	/*
	localeResolver using cookie
	 */
	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver localeResolver = new CookieLocaleResolver();
		localeResolver.setDefaultLocale(Locale.ENGLISH);
		localeResolver.setCookieName("lang");
		return localeResolver;
	}

	/*
	when ?lang="country code" input query, change locale language
	 */
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("lang");
		return localeChangeInterceptor;
	}
}