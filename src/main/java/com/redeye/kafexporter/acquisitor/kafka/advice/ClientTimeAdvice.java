package com.redeye.kafexporter.acquisitor.kafka.advice;

import java.util.concurrent.BlockingQueue;

import com.redeye.kafexporter.acquisitor.kafka.model.ClientTimeDTO;

/**
 * 
 * 
 * @author jmsohn
 */
public class ClientTimeAdvice {
	
	
	/** */
	public static BlockingQueue<ClientTimeDTO> queue;
	
	
	/**
	 * 초기화
	 *
	 * @param queue
	 */
	public static void init(BlockingQueue<ClientTimeDTO> queue) {
		ClientTimeAdvice.queue = queue;
	}
	
	/**
	 * 
	 * 
	 * @param consumer
	 */
	protected static void sendCurTime(Object consumer) {
		
		// 입력 값 및 큐 검사
		if(consumer == null || queue == null) {
			return;
		}
		
		try {
			
			// 클라이언트 아이디 획득
			String clientId = KafkaConsumerConstructorAdvice.getClientId(consumer);
			if(clientId == null) {
				return;
			}

			// 큐에 데이터 전송
			queue.put(new ClientTimeDTO(clientId, System.currentTimeMillis()));
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
