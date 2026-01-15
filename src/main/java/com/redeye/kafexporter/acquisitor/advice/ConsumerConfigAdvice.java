package com.redeye.kafexporter.acquisitor.advice;

import java.lang.reflect.Method;
import java.util.Map;

import com.redeye.kafexporter.acquisitor.Constants;

import net.bytebuddy.asm.Advice;

/**
 * ConsumerConfig 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class ConsumerConfigAdvice {

	// 아래의 멤버 변수는 public 이어야 함 - SpringBoot 클래스로더에서 문제가 생김
	
	/**
	 * 컨슈머 설정 맵<br>
	 * Key: 클라이언트 아이디, Value: 컨슈머 설정 맵
	 */
	public static Map<String, Map<String, Object>> configMap;

	/** 클라이언트 아이디 */
	public static ThreadLocal<String> clientIdContext = ThreadLocal.withInitial(() -> Constants.DEFAULT_CLIENT_ID);

	
	/**
	 * 초기화
	 *
	 * @param configMap
	 */
	public static void init(Map<String, Map<String, Object>> configMap) {
		ConsumerConfigAdvice.configMap = configMap;
	}
	
	/**
	 * Kafka ConsumerConfig 생성 이후 호출
	 * 
	 * @param config 생성된 Kafka ConsumerConfig 객체
	 */
	@Advice.OnMethodExit
	public static void onConstructorExit(@Advice.This Object config) {

		try {

			// 설정 값을 가져옴
			Method valuesMethod = config.getClass().getMethod("values");
			if(valuesMethod == null) {
				return;
			}
			
			@SuppressWarnings("unchecked")
			Map<String, Object> configValueMap = (Map<String, Object>)valuesMethod.invoke(config);

			// 클라이언트 아이디 획득
			if(configValueMap == null || configValueMap.containsKey(Constants.CLIENT_ID) == false) {
				return;
			}

			String clientId = configValueMap.get(Constants.CLIENT_ID).toString();
			
			// 프로듀서 설정 맵에 추가
			configMap.put(clientId, configValueMap);
			
			// 클라이언트 아이디 설정
			clientIdContext.set(clientId);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 현재 스레드에 설정된 클라이언트 아이디를 반환<br>
	 * 주의! 한번 호출되면 호출 이후 데이터 삭제됨
	 * 
	 * @return 클라이언트 아이디
	 */
	public static String getClientId() {
		
		String clientId = clientIdContext.get();
		clientIdContext.remove();
		
		return clientId;
	}
}
