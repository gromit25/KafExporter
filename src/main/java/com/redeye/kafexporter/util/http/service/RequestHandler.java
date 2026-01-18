package com.redeye.kafexporter.util.http.service;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
