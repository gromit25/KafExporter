package com.redeye.kafexporter.util.http.service;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * 
 * @author jmsohn
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Controller {
	
	/** */
	String basePath();
}
