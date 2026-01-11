package com.redeye.kafexporter;

import java.lang.instrument.Instrumentation;

import com.redeye.kafexporter.acquisitor.KafkaAcquisitor;

/**
 * Kafka 수집 에이전트 메인 클래스
 * 
 * @author jmsohn
 */
public class KafAgent {
	
	/**
	 * Java VM의 Kafka JMX 데이터를 읽어오는 스레드 실행
	 * 
	 * @param args 입력 Argument
	 * @param inst 현재 VM의 Instrument
	 */
	public static void premain(String args, Instrumentation inst) {
		
		// 수집기 기동
		KafkaAcquisitor.getInstance().init(inst);
	}
}
