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
	private static Map<String, Map<String, Object>> configMap = new ConcurrentHashMap<>();
	
	
	/**
	 * Kafka ConsumerConfig 생성 이후 호출
	 * 
	 * @param config 생성된 Kafka ProviderConfig 객체
	 */
	@Advice.OnMethodExit
	public static void onPostProducerConfigConstructor(
		@Advice.This Object config
	) {

		try {

			//
			Method valuesMethod = config.getClass().getMethod("values");
			if(valuesMethod == null) {
				return null;
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> configValues = (Map<String, Object>)valuesMethod.invoke(config);

			this.configMap(config.toString(), configValues);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
