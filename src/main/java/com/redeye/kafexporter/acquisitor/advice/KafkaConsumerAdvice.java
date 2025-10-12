package com.redeye.kafexporter.acquisitor.advice;

import org.apache.kafka.clients.consumer.ConsumerRecords;

import com.redeye.kafexporter.util.stat.Parameter;

import net.bytebuddy.asm.Advice;

/**
 * KafkaConsumer 어드바이스 클래스
 * 
 * @author jmsohn
 */
public class KafkaConsumerAdvice {
	
	// !주의 어드바이스 메소드에서 static 멤버 변수를 참조할 때는 무조건 public 이어야함
	// 같은 클래스 내에 있더라도 private 이면 bytebuddy에서 접근 오류를 발생함 - 이유는 확인 불가
	
	/** polling time 통계 객체 */
	public static Parameter pollingTimeParameter = new Parameter();
	
	/** record count 통계 객체 */
	public static Parameter recordCountParameter = new Parameter();
	
	/** 최근 polling 시간 */
	public static long  pollingLast = -1;

	
	/**
	 * 초기화
	 */
	public synchronized static void reset() {
		pollingTimeParameter.reset();
		recordCountParameter.reset();
		pollingLast = -1;
	}
	
	/**
	 * KafkaConsumer.poll 진입 전
	 */
	@Advice.OnMethodEnter
	public static void onEnter() {
		
		long now = System.currentTimeMillis();

		if(pollingLast != -1) {

			long interval = now - pollingLast;
			pollingTimeParameter.add((double)interval);
		}
		
		pollingLast = now;
	}

	/**
	 * KafkaConsumer.poll 반환 후
	 * 
	 * @param result poll 반환 값
	 */
	@Advice.OnMethodExit
	public static void onExit(@Advice.Return ConsumerRecords<?, ?> result) {
    	
    	int recordCount = (result != null) ? result.count() : 0;
    	
    	if(pollingTimeParameter.getCount() != 0) {
    		recordCountParameter.add((double)recordCount);
    	}
    }
}
