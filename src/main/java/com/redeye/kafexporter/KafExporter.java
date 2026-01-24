package com.redeye.kafexporter;

import java.lang.instrument.Instrumentation;

import com.redeye.kafexporter.acquisitor.kafka.KafkaAcquisitor;
import com.redeye.kafexporter.acquisitor.kafka.KafkaTransformer;
import com.redeye.kafexporter.exporter.http.kafka.KafkaClientController;
import com.redeye.kafexporter.exporter.http.kafka.KafkaConfigController;
import com.redeye.kafexporter.exporter.http.kafka.KafkaMetricsController;
import com.redeye.kafexporter.util.StringUtil;
import com.redeye.kafexporter.util.WebUtil;
import com.redeye.kafexporter.util.http.service.HttpService;

/**
 * kafka 정보 수집기 클래스
 * 
 * @author jmsohn
 */
public class KafExporter {
	
	
	/** http exporter 서비스*/
	private static HttpService service;
	
	
	/**
	 * 메인 메소드
	 * 
	 * @param args javaagent 옵션 문자열
	 * @param inst java 인스트루먼트 클래스
	 */
	public static void premain(String args, Instrumentation inst) {
		
		try {

			// kafka 메소드 변환
			KafkaTransformer.addKafkaTransformer(inst);
			
			// kafka 정보 수집기 초기화
			KafkaAcquisitor.init();
			
			// exporter 서비스 기동
			startHttpExporterService(args);
			System.out.println("http exporter server(" + service.getHostStr() + ") is started.");
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * http exporter 서비스 기동
	 * 
	 * @param hostPortArgs 
	 */
	private static void startHttpExporterService(String hostPortArgs) throws Exception {
		
		// export 서버명 변수
		String host = "localhost";
		
		// export 서버 포트 변수
		int port = 0; // 설정 값이 없는 경우, 서버에서 비어 있는 랜덤 포트를 사용
		
		// exporter 호스트 및 포트 번호 획득
		// 없을 경우 기본 설정 값 사용
		if(StringUtil.isBlank(hostPortArgs) == false) {

			if(hostPortArgs.matches("[0-9]+") == true) {
				
				port = Integer.parseInt(hostPortArgs);
				
			} else {
				
				String[] hostPort = WebUtil.parseHostPort(hostPortArgs);
				
				host = hostPort[0];
				port = Integer.parseInt(hostPort[1]);
			}
		}
		
		// exporter 서버의 스레드 개수 설정
		int threadCount = Integer
			.parseInt(
				getEnv("AGENT_EXPORTER_THREAD_COUNT", "-1")
			);
		
		// -----------------------------
		
		// Http 서버 생성
		service = new HttpService(host, port, threadCount);
		
		// kafka 컨트롤러 추가
		service.addController(new KafkaClientController());
		service.addController(new KafkaConfigController());
		service.addController(new KafkaMetricsController());
		
		// Http 서버 기동
		service.start();
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
