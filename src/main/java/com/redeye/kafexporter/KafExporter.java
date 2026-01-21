package com.redeye.kafexporter;

import java.lang.instrument.Instrumentation;

import com.redeye.kafexporter.acquisitor.kafka.KafkaAcquisitor;
import com.redeye.kafexporter.exporter.http.HttpExporter;
import com.redeye.kafexporter.util.StringUtil;
import com.redeye.kafexporter.util.WebUtil;

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

			// 1. kafka 정보 수집기 초기화
			KafkaAcquisitor.init(inst);
			
			// 2. exporter 서버 기동
			
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

			// exporter 서버 기동
			HttpExporter exporterServer = new HttpExporter(host, port);
			exporterServer.start();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
