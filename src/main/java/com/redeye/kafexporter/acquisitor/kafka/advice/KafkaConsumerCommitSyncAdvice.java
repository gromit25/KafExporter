package com.redeye.kafexporter.acquisitor.kafka.advice;

import net.bytebuddy.asm.Advice;

/**
 * KafkaConsumer commitAsync 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class KafkaConsumerCommitSyncAdvice extends ClientTimeAdvice {
	
	/**
	 * 
	 * 
	 * @param consumer
	 */
	@Advice.OnMethodExit
	public static void onExit(@Advice.This Object consumer) {
		sendCurTime(consumer);
	}
}
