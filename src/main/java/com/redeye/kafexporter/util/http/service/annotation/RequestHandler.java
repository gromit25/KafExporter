package com.redeye.kafexporter.util.http.service.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.redeye.kafexporter.util.http.service.HttpMethod;

/**
 * 
 * 
 * @author jmsohn
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface RequestHandler {
	
	/** */
	String path() default "";
	
	/** */
	HttpMethod[] method();
}
