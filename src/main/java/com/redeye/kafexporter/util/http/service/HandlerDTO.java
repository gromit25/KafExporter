package com.redeye.kafexporter.util.http.service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
class HandlerDTO {
	
	
	/**
	 * 
	 */
	private enum MethodType {
		ONE_PARAM,
		TWO_PARAM;
	}
	
	
	/** */
	@Getter
	private final StringUtil.WildcardPattern pathPattern;
	
	/** */
	@Getter
	private final Set<HttpMethod> httpMethodSet;
	
	/** */
	private final MethodType methodType;
	
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
		
		// 1. Path 설정
		this.pathPattern = StringUtil.WildcardPattern.create(path);
		
		// 2. HTTP 메소드 설정
		this.httpMethodSet = new HashSet<>();
		for(HttpMethod httpMethod: httpMethodList) {
			this.httpMethodSet.add(httpMethod);
		}
		
		// 3. 메소드 타입 및 메소드 설정
		this.methodType = getMethodType(method);
		this.method = method;
	}
	
	/**
	 * 
	 * 
	 * @param method
	 * @return
	 */
	private static MethodType getMethodType(Method method) {
		
		// 1. public 여부 확인
		if(Modifier.isPublic(method.getModifiers()) == false) {
			throw new RuntimeException(method +  " is not public.");
		}

		// 2. 반환 타입이 String인지 확인
		if(method.getReturnType().equals(String.class) == false) {
			throw new RuntimeException(method +  " return type must be String.");
		}

		// 3. 파라미터 개수 및 타입 확인
		MethodType methodType = null;
		
		Class<?>[] parameterTypes = method.getParameterTypes();
		if(parameterTypes.length == 1) {
			methodType = MethodType.ONE_PARAM;
		} else if(parameterTypes.length == 2) {
			methodType = MethodType.TWO_PARAM;
		} else {
			throw new RuntimeException(method +  " must have 1 or 2 params.");
		}

		// 첫 번째 파라미터: HttpExchange
		if(parameterTypes[0].equals(HttpExchange.class) == false) {
			throw new RuntimeException(method +  " first param must be HttpExchange.");
		}

		// 두 번째 파라미터: List
		if(methodType == MethodType.TWO_PARAM) {
			if(parameterTypes[1].equals(List.class) == false) {
				throw new RuntimeException(method +  " second param must be List.");
			}
		}

		return methodType;
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
		Object retrival = null;
		
		if(this.methodType == MethodType.ONE_PARAM) {
			
			retrival = this.method.invoke(obj, exchange);
			
		} else if(this.methodType == MethodType.TWO_PARAM) {
			
			String path = exchange.getRequestURI().getPath();
			List<String> params = this.pathPattern.match(path).getGroups();
			
			retrival = this.method.invoke(obj, exchange, params);
		}
		
		//
		return (retrival != null)?retrival.toString():"";
	}
}
