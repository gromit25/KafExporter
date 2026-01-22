package com.redeye.kafexporter.acquisitor.kafka.advice;

import net.bytebuddy.asm.Advice;

/**
 * KafkaConsumer commitAsync 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class KafkaConsumerCommitSyncAdvice {
	
	/**
	 * 
	 * 
	 * @param consumer
	 */
	@Advice.OnMethodExit
	public static void onExit(@Advice.This Object consumer) {
		
		System.out.println("### INVOKE commitSync DEBUG 000");
		
		// 클라이언트 아이디 획득
		String clientId = KafkaConsumerConstructorAdvice.getClientId(consumer);
		if(clientId == null) {
			return;
		}
		
		System.out.println("### INVOKE commitSync DEBUG 100: " + clientId);
	}
}
