package com.redeye.kafexporter;

import java.lang.instrument.Instrumentation;

import com.redeye.kafexporter.acquisitor.kafka.KafkaAcquisitor;
import com.redeye.kafexporter.exporter.http.HttpExporter;

/**
 * 카프카 정보 수집기 클래스
 * 
 * @author jmsohn
 */
public class KafExporter {
	
	/**
	 * 메인 메소드
	 * 
	 * @param args javaagent 옵션 문자열
	 * @param inst java 인스트루먼트 클래스
	 */
	public static void premain(String args, Instrumentation inst) {
		
		try {

			// kafka 정보 수집기 초기화
			KafkaAcquisitor.init(inst);
			
			// exporter 서버 기동
			HttpExporter exporterServer = new HttpExporter("localhost", 5551);
			exporterServer.start();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
