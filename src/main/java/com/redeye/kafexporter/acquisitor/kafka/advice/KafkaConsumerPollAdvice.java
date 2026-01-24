package com.redeye.kafexporter.acquisitor.kafka.advice;

import net.bytebuddy.asm.Advice;

/**
 * KafkaConsumer poll 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class KafkaConsumerPollAdvice extends ClientTimeAdvice {

	/**
	 * KafkaConsumer.poll 진입 전
	 *
	 * @param consumer 컨슈머 객체
	 */
	@Advice.OnMethodExit
	public static void onExit(@Advice.This Object consumer) {
		sendCurTime(consumer);
	}
}
