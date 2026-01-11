package com.redeye.kafexporter.util;

import java.util.ArrayList;
import java.util.List;

import com.redeye.kafexporter.acquisitor.jmx.JMXService;

/**
 * 
 * 
 * @author jmsohn
 */
public class KafkaUtil {
	
	
	/** */
	private static final String CLIENT_ID_QUERY = "kafka.producer:client-id=*,type=producer-metrics";
	
	
	/**
	 * 
	 * 
	 * @param svc JMX 서비스 객체
	 * @return
	 */
	public static List<String> getProducerClientIdList(JMXService svc) throws Exception {
		
		if(svc == null) {
			throw new IllegalArgumentException("svc(JMXService) is null.");
		}
		
		if(svc.isClosed() == true) {
			throw new IllegalArgumentException("svc(JMXService) is closed.");
		}
		
		// 클라이언트 아이디 목록 생성 후 반환
		List<String> clientIdList = new ArrayList<>();
		
		svc
			.getByQuery(CLIENT_ID_QUERY, "client-id")
			.forEach((key, clientId) -> {
				clientIdList.add(clientId.toString());
			});
		
		return clientIdList;
	}
}
