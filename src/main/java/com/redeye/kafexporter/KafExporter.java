package com.redeye.kafexporter;

import java.lang.instrument.Instrumentation;

import com.redeye.kafexporter.acquisitor.kafka.KafkaAcquisitor;
import com.redeye.kafexporter.acquisitor.kafka.KafkaTransformer;
import com.redeye.kafexporter.exporter.http.HttpExporter;
import com.redeye.kafexporter.util.StringUtil;
import com.redeye.kafexporter.util.WebUtil;

/**
 * kafka 정보 수집기 클래스
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

			// ----- kafka 메소드 변환 클래스
			KafkaTransformer.addKafkaTransformer(inst);
			
			// ----- kafka 정보 수집기 초기화
			KafkaAcquisitor.init();
			
			// ----- exporter 서버 기동
			
			// export 서버명
			String host = "localhost";
			
			// export 서버 포트 
			int port = 0; // 설정 값이 없는 경우, 서버에서 비어 있는 랜덤 포트를 사용
			
			// exporter 호스트 및 포트 번호 획득
			if(StringUtil.isBlank(args) == false) {

				if(args.matches("[0-9]+") == true) {
					port = Integer.parseInt(args);
				} else {
					
					String[] hostPort = WebUtil.parseHostPort(args);
					
					host = hostPort[0];
					port = Integer.parseInt(hostPort[1]);
				}
			}
			
			// exporter 서버의 스레드 개수 설정
			int threadCount = Integer
				.parseInt(
					getEnv("AGENT_EXPORTER_THREAD_COUNT", "-1")
				);

			// exporter 서버 기동
			HttpExporter exporterServer = new HttpExporter(host, port, threadCount);
			exporterServer.start();
			
			System.out.println("exporter server(" + exporterServer.getHostStr() + ") is started.");
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 환경 변수 설정 값 반환
	 * 
	 * @param name 환경 변수 명
	 * @param defaultValue 환경 변수 미설정시 반환할 값
	 * @return 환경 변수 설정 값
	 */
	private static String getEnv(String name, String defaultValue) {
		
		String value = System.getenv(name);
		
		if(StringUtil.isBlank(value) == true) {
			return defaultValue;
		} else {
			return value;
		}
	}
}
