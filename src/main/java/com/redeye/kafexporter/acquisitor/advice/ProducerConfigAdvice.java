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
	
	/**
	 * Kafka ConsumerConfig 생성 이후 호출
	 * 
	 * @param consumer
	 */
	@Advice.OnMethodExit
	public static void onPostProducerConfigConstructor(
		@Advice.This Object config
	) {

		try {

			Method values = config.getClass().getMethod("values");

			@SuppressWarnings("unchecked")
			Map<String, Object> configMap = (Map<String, Object>)values.invoke(config);

			System.out.println(configMap);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
