package com.redeye.kafexporter.util.http.service.model;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.redeye.kafexporter.util.StringUtil;
import com.redeye.kafexporter.util.http.service.HttpMethod;
import com.sun.net.httpserver.HttpExchange;

import lombok.Getter;

/**
 * 
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
public class HandlerDTO {
	
	
	/**
	 * 
	 */
	private enum MethodType {
		NON_PARAM,
		EXCHANGE_PARAM_ONLY,
		PATHLIST_PARAM_ONLY,
		EXCHANGE_PATHLIST_PARAM,
		PATHLIST_EXCHANGE_PARAM;
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
		
		if(parameterTypes.length == 0) {
			
			methodType = MethodType.NON_PARAM;
			
		} else if(parameterTypes.length == 1) {
			
			if(parameterTypes[0].equals(HttpExchange.class) == true) {
				methodType = MethodType.EXCHANGE_PARAM_ONLY;
			} else if(parameterTypes[0].equals(List.class) == true) {
				methodType = MethodType.PATHLIST_PARAM_ONLY;
			} else {
				throw new RuntimeException("unexpected param type: " + parameterTypes[0]);
			}
			
		} else if(parameterTypes.length == 2) {
			
			if(parameterTypes[0].equals(HttpExchange.class) == true && parameterTypes[1].equals(List.class) == true) {
				methodType = MethodType.EXCHANGE_PARAM_ONLY;
			} else if(parameterTypes[0].equals(List.class) == true && parameterTypes[1].equals(HttpExchange.class) == true) {
				methodType = MethodType.PATHLIST_PARAM_ONLY;
			} else {
				throw new RuntimeException("unexpected param type: " + parameterTypes[0] + ", " + parameterTypes[1]);
			}
			
		} else {
			throw new RuntimeException(method +  " must have 0, 1, 2 params.");
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
		
		//
		String path = exchange.getRequestURI().getPath();
		List<String> pathList = this.pathPattern.match(path).getGroups();
		
		switch(this.methodType) {
		case NON_PARAM:
			retrival = this.method.invoke(obj);
			break;
			
		case EXCHANGE_PARAM_ONLY:
			retrival = this.method.invoke(obj, exchange);
			break;
			
		case PATHLIST_PARAM_ONLY:
			retrival = this.method.invoke(obj, pathList);
			break;
			
		case EXCHANGE_PATHLIST_PARAM:
			retrival = this.method.invoke(obj, exchange, pathList);
			break;
			
		case PATHLIST_EXCHANGE_PARAM:
			retrival = this.method.invoke(obj, pathList, exchange);
			break;
			
		default:
			throw new RuntimeException("unexpected type: " + this.methodType);
		}
		
		//
		return (retrival != null)?retrival.toString():"";
	}
}
