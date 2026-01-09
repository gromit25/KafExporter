package com.redeye.kafexporter.acquisitor.advice;

import java.lang.reflect.Method;
import java.util.Map;

import net.bytebuddy.asm.Advice;

/**
 * ConsumerConfig 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class ConsumerConfigAdvice {

	
	/** */
	private static Map<String, Map<String, Object>> configMap;

	
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
	public static void onPostConsumerConfigConstructor(
		@Advice.This Object config,
	) {

		try {

			Method valuesMethod = config.getClass().getMethod("values");
			if(valuesMethod == null) {
				return null;
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> configValues = (Map<String, Object>)valuesMethod.invoke(config);

			//TODO client id 로 변경해야 함
			ConsumerConfigAdvice.configMap.put(config.toString(), configValues);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
