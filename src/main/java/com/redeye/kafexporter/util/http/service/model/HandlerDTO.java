package com.redeye.kafexporter.util.http.service.model;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.redeye.kafexporter.util.StringUtil;
import com.redeye.kafexporter.util.http.service.HttpMethod;
import com.sun.net.httpserver.HttpExchange;

import lombok.Getter;

/**
 * Http 요청 처리 핸들러 클래스
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
public class HandlerDTO {
	
	
	/**
	 * 요청 처리 핸들러 메소드 타입
	 * 
	 * @author jmsohn
	 */
	private enum MethodType {
		
		/** 파라미터 없는 타입 */
		NON_PARAM,
		
		/** 파라미터가 Http 요청/응답 객체만 있는 타입 */
		EXCHANGE_PARAM_ONLY,
		
		/** 파라미터가 패스 변수 목록만 있는 타입 */
		PATHLIST_PARAM_ONLY,
		
		/** 파라미터가 Http 요청/응답 객체, 패스 변수 목록 순으로 있는 타입 */
		EXCHANGE_PATHLIST_PARAM,
		
		/** 파라미터가 패스 변수 목록, Http 요청/응답 객체 순으로 있는 타입 */
		PATHLIST_EXCHANGE_PARAM;
	}
	
	
	/** 패스 패턴 원본 문자열 */
	@Getter
	private final String pathPatternStr;
	
	/** 패스 패턴 목록 */
	@Getter
	private final List<StringUtil.WildcardPattern> pathSegmentPatternList = new CopyOnWriteArrayList<>();
	
	/** Http 메소드 종류 */
	@Getter
	private final Set<HttpMethod> httpMethodSet;
	
	/** 요청 처리 핸들러 메소드 타입 */
	private final MethodType methodType;
	
	/** 요청 처리 핸들러 메소드 */
	@Getter
	private final Method method;
	
	
	/**
	 * 생성자
	 * 
	 * @param path 패스 패턴 - 전체 경로
	 * @param methodList
	 * @param method
	 */
	public HandlerDTO(String path, HttpMethod[] httpMethodList, Method method) throws Exception {
		
		// 0. 패스 패턴 저장
		this.pathPatternStr = path;
		
		// 1. 패스 세그먼트 패턴 목록 설정
		String[] pathSegmentAry = path.split("/");
		
		for(String pathSegment: pathSegmentAry) {
			
			// 패스 이름이 없는 경우 스킵
			if(StringUtil.isBlank(pathSegment) == true) {
				continue;
			}
			
			this.pathSegmentPatternList.add(StringUtil.WildcardPattern.create(pathSegment));
		}
				
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
	 * 주어진 메소드의 메소드 타입 반환
	 * 
	 * @param method 메소드 타입을 검사할 메소드
	 * @return 메소드 타입
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
			throw new RuntimeException(method +  " must have 0, 1, 2 params: " + parameterTypes.length);
		}

		return methodType;
    }

	/**
	 * Http 요청이 핸들러가 처리할 수 있는 요청인지 여부 반환
	 * 
	 * @param exchange Http 요청/응답 객체
	 * @return 요청 처리 가능 여부
	 */
	public boolean isMatched(HttpExchange exchange) {
		
		// 입력 값 검증
		if(exchange == null) {
			return false;
		}
		
		// 1. 패스 매칭 여부 검사
		String path = exchange.getRequestURI().getPath();
		String[] pathSegmentAry = path.split("/");
		
		// 입력된 패스 세그먼트의 수와 패치할 패스 세그먼트 패턴의 개수가 일치하지 않으면 false 반환
		if(pathSegmentAry.length != this.pathSegmentPatternList.size()) {
			return false;
		}
		
		// 각 패스 세그먼트의 패턴 일치 여부 검사
		// 하나라도 일치하지 않으면 false 반환
		for(int index = 0; index < pathSegmentAry.length; index++) {
			
			StringUtil.WildcardMatcher pathMatcher = this.pathSegmentPatternList
				.get(index)
				.match(pathSegmentAry[index]);
			
			if(pathMatcher.isMatch() == false) {
				return false;
			}
		}
		
		// 2. HTTP 메소드 매칭 여부 검사
		String httpMethodName = exchange.getRequestMethod();
		
		// 처리 가능한 HTTP 메소드 중 하나라도 일치하면 true 반환
		for(HttpMethod httpMethod: this.httpMethodSet) {
			if(httpMethod.name().equals(httpMethodName) == true) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Http 요청을 처리하기 위해 핸들러 메소드를 호출
	 * 
	 * @param obj 요청 처리 객체
	 * @param exchange Http 요청/응답 객체
	 * @return 처리 결과 메시지
	 */
	public String invoke(Object obj, HttpExchange exchange) throws Exception {
		
		// 요청 처리 후 응답 변수
		Object retrival = null;
		
		// 패스의 변수 값 목록 변수
		List<String> pathParamList = new ArrayList<>();
		
		// Http 요청 패스에서 변수 목록 추출
		String path = exchange.getRequestURI().getPath();
		String[] pathSegmentAry = path.split("/");
		
		if(pathSegmentAry.length != this.pathSegmentPatternList.size()) {
			throw new RuntimeException("unmatched path exception: " + path + ", " + this.pathPatternStr);
		}
		
		for(int index = 0; index < pathSegmentAry.length; index++) {
			
			StringUtil.WildcardMatcher pathMatcher = this.pathSegmentPatternList
				.get(index)
				.match(pathSegmentAry[index]);
			
			if(pathMatcher.isMatch() == false) {
				throw new RuntimeException("unmatched path exceptiuon: " + path + ", " + this.pathPatternStr);
			}
			
			pathParamList.addAll(pathMatcher.getGroups());
		}
		
		// 메소드의 타입에 따라 요청 처리 핸들러 메소드를 호출
		switch(this.methodType) {
		case NON_PARAM:
			retrival = this.method.invoke(obj);
			break;
			
		case EXCHANGE_PARAM_ONLY:
			retrival = this.method.invoke(obj, exchange);
			break;
			
		case PATHLIST_PARAM_ONLY:
			retrival = this.method.invoke(obj, pathParamList);
			break;
			
		case EXCHANGE_PATHLIST_PARAM:
			retrival = this.method.invoke(obj, exchange, pathParamList);
			break;
			
		case PATHLIST_EXCHANGE_PARAM:
			retrival = this.method.invoke(obj, pathParamList, exchange);
			break;
			
		default:
			throw new RuntimeException("unexpected type: " + this.methodType);
		}
		
		// 요청 처리 결과 반환
		return (retrival != null)?retrival.toString():"";
	}
}
