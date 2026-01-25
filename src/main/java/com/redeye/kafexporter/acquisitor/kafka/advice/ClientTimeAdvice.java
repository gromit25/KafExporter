package com.redeye.kafexporter.acquisitor.kafka.advice;

import java.util.concurrent.BlockingQueue;

import com.redeye.kafexporter.acquisitor.kafka.model.ClientTimeDTO;
import com.redeye.kafexporter.acquisitor.kafka.stat.TimeStatDaemon;

/**
 * kafka 클라이언트의 시간 처리 관련 어드바이스 상위(공통) 클래스
 * 
 * @author jmsohn
 */
public class ClientTimeAdvice {
	
	
	/** 시간 데이터 전송용 큐 */
	public static BlockingQueue<ClientTimeDTO> queue;
	
	
	/**
	 * 초기화
	 *
	 * @param queue
	 */
	public static void init(TimeStatDaemon timeStatDaemon) {
		ClientTimeAdvice.queue = timeStatDaemon.getQueue();
	}
	
	/**
	 * 현재 시간을 처리자에게 전송
	 * 
	 * @param consumer 클라이언트 객체
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
