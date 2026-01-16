package com.redeye.kafexporter.acquisitor.advice;

import java.util.concurrent.BlockingQueue;

import com.redeye.kafexporter.acquisitor.model.TimeDTO;

import net.bytebuddy.asm.Advice;

/**
 * KafkaConsumer 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class KafkaConsumerPollAdvice {

	
	/** */
	public static BlockingQueue<TimeDTO> queue;
	
	
	/**
	 * 초기화
	 *
	 * @param queue
	 */
	public static void init(BlockingQueue<TimeDTO> queue) {
		KafkaConsumerPollAdvice.queue = queue;
	}
	
	/**
	 * KafkaConsumer.poll 진입 전
	 *
	 * @param consumer 컨슈머 객체
	 */
	@Advice.OnMethodEnter
	public static void onEnter(@Advice.This Object consumer) {
		
		System.out.println("#### POLL ON ENTER #2: ");

		// 입력 값 및 큐 검사
		if(consumer == null || queue == null) {
			return;
		}
		
		// 클라이언트 아이디 획득
		String clientId = KafkaConsumerConstructorAdvice.getClientId(consumer);
		if(clientId == null) {
			return;
		}

		try {
			
			System.out.println("#### PUT INTERVAL: " + clientId);
			queue.put(new TimeDTO(clientId, System.currentTimeMillis()));
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
