package com.redeye.kafexporter.acquisitor.advice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.bytebuddy.asm.Advice;

/**
 * 
 * 
 * @author jmsohn
 */
public class KafkaConsumerConstructorAdvice {

	
	/** Key: 프로듀서/컨슈머 toString 값, Value: 클라이언트 아이디 값 */
	public static Map<String, String> clientIdMap = new ConcurrentHashMap<>();
	

	/**
	 * KafkaConsumer 생성자 호출 후
	 * 
	 * @param consumer 컨슈머 객체
	 */
	@Advice.OnMethodExit
	public static void onConstructorExit(@Advice.This Object consumer) {
		
		// 입력 값 검증
		if(consumer == null) {
			return;
		}
		
		String clientId = ConsumerConfigAdvice.getClientId();
		if(clientId == null) {
			return;
		}

		// 클라이언트 아이디 맵에 저장
		System.out.println("### PUT CLIENT ID: " + consumer.toString() + "," + clientId);
		clientIdMap.put(consumer.toString(), clientId);
	}
	
	/**
	 * 컨슈머 객체에 해당하는 클라이언트 아이디 반환
	 * 
	 * @param consumer 컨슈머 객체
	 * @return 클라이언트 아이디 반환
	 */
	public static String getClientId(Object consumer) {
		
		if(consumer == null) {
			return null;
		}
		
		return clientIdMap.get(consumer.toString());
	}
}
