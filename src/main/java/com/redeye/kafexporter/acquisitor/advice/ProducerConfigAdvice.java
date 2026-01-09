package com.redeye.kafexporter.acquisitor.advice;

import java.lang.reflect.Method;
import java.util.Map;

import net.bytebuddy.asm.Advice;

/**
 * ProducerConfig 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class ProducerConfigAdvice {

	
	/** */
	private static Map<String, Map<String, Object>> configMap;


	/**
	 * 초기화
	 *
	 * @param configMap
	 */
	public static void init(Map<String, Map<String, Object>> configMap) {
		ProducerConfigAdvice.configMap = configMap;
	}
	
	/**
	 * Kafka ConsumerConfig 생성 이후 호출
	 * 
	 * @param config 생성된 Kafka ProducerConfig 객체
	 */
	@Advice.OnMethodExit
	public static void onPostProducerConfigConstructor(
		@Advice.This Object config
	) {

		try {

			//
			Method valuesMethod = config.getClass().getMethod("values");
			if(valuesMethod == null) {
				return;
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> configValues = (Map<String, Object>)valuesMethod.invoke(config);

			configMap.put(clientId, configValues);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
