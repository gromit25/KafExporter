package com.redeye.kafexporter.util.http.service;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.redeye.kafexporter.util.StringUtil;
import com.sun.net.httpserver.HttpExchange;

import lombok.Getter;

/**
 * 
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
public class HandlerDTO {
	
	
	/** */
	@Getter
	private final StringUtil.WildcardPattern pathPattern;
	
	/** */
	@Getter
	private final Set<HttpMethod> httpMethodSet;
	
	/** */
	@Getter
	private final Method method;
	
	
	/**
	 * 생성자
	 * 
	 * @param path
	 * @param methodList
	 * @param method
	 */
	public HandlerDTO(String path, HttpMethod[] httpMethodList, Method method) throws Exception {
		
		//
		this.pathPattern = StringUtil.WildcardPattern.create(path);
		
		//
		this.httpMethodSet = new HashSet<>();
		for(HttpMethod httpMethod: httpMethodList) {
			this.httpMethodSet.add(httpMethod);
		}
		
		//
		this.method = method;
	}

	/**
	 * 
	 * 
	 * @param exchange
	 * @return
	 */
	public boolean isMatched(HttpExchange exchange) {
		
		// 입력 값 검증
		if(exchange == null) {
			return false;
		}
		
		// path 매칭 여부 검사
		String path = exchange.getRequestURI().getPath();
		
		StringUtil.WildcardMatcher pathMatcher = this.pathPattern.match(path);
		if(pathMatcher.isMatch() == false) {
			return false;
		}
		
		// HTTP METHOD 매칭 여부 검사
		String httpMethodName = exchange.getRequestMethod();
		
		for(HttpMethod httpMethod: this.httpMethodSet) {
			if(httpMethod.name().equals(httpMethodName) == true) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * 
	 * @param obj
	 * @param exchange
	 * @return
	 */
	public String invoke(Object obj, HttpExchange exchange) throws Exception {
		
		//
		String path = exchange.getRequestURI().getPath();
		List<String> params = this.pathPattern.match(path).getGroups();
		
		//
		Object retrival = this.method.invoke(obj, exchange);
		
		//
		return (retrival != null)?retrival.toString():"";
	}
}
