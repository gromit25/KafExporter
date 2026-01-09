package com.redeye.kafexporter.acquisitor.advice;

import org.apache.kafka.clients.consumer.ConsumerRecords;

import com.redeye.kafexporter.util.stat.Parameter;

import net.bytebuddy.asm.Advice;

/**
 * KafkaConsumer 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class ConsumerAdvice {

	
	/** */
	private static BlockingQueue<IntervalDTO> queue;
	

	/**
	 * 초기화
	 *
	 * @param queue
	 */
	public static void init(BlockingQueue<IntervalDTO> queue) {
		ConsumerAdvice.queue = queue;
	}
	
	/**
	 * KafkaConsumer.poll 진입 전
	 *
	 * @param clientId 컨슈머 클라이언트 아이디
	 */
	@Advice.OnMethodEnter
	public static void onEnter(@Advice.FieldValue("clientId") String clientId) {
		
		if(queue == null) {
			return;
		}

		queue.put(new IntervalDTO(clientId, System.currentTimeMillis()));
	}
}
