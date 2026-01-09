package com.redeye.kafexporter;

import java.lang.instrument.Instrumentation;

import com.redeye.kafexporter.acquisitor.AcquireJob;

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
		
		// 수집 주기 설정
		String portNumStr = System.getenv("KAF_EXPORTER_PORT");
		if(portNumStr == null) {
			// 환경변수가 없는 경우 디폴트 값 설정
			portNumStr = "1234";
		}
		
		// 수집기 기동
		AcquireJob.start(period, inst);
	}
}
