package com.redeye.kafexporter.exporter.http.kafka;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.epozen.emma.exporter.kafka.Collector;
import com.epozen.emma.exporter.kafka.util.JSONUtil;
import com.epozen.emma.exporter.kafka.util.http.annotation.Controller;
import com.epozen.emma.exporter.kafka.util.http.annotation.RequestHandler;

/**
 * Kafka 클라이언트 정보 관련 컨트롤러
 * 
 * @author jmsohn
 */
@Controller(basePath = "/client")
public class KafkaClientController {

	/**
	 * Kafka 클라이언트 아이디 목록 반환
	 * 
	 * @return Kafka 클라이언트 아이디 목록
	 */
	@RequestHandler
	public String getClientIdList() {
		
		Map<String, Set<String>> clientIdMap = new HashMap<>();
		
		clientIdMap.put("producer", Collector.getProducerClientIdList());
		clientIdMap.put("consumer", Collector.getConsumerClientIdList());
		
		return JSONUtil.toJSON(clientIdMap);
	}
	
	/**
	 * 클라이언트 설정 정보 반환
	 * 
	 * @param pathParamList 패스 파라미터 목록
	 * @return 클라이언트 설정 정보
	 */
	@RequestHandler(path = "/*/config")
	public String getClientConfigMap(List<String> pathParamList) {
		
		return JSONUtil.toJSON(
			Collector.getClientConfigMap(
				pathParamList.get(0)	// Client Id
			)
		);
	}
	
	/**
	 * 클라이언트 성능 정보 반환
	 * 
	 * @param pathParamList 패스 파라미터 목록
	 * @return 클라이언트 성능 정보
	 */
	@RequestHandler(path = "/*/metrics")
	public String getClientMetrics(List<String> pathParamList) throws Exception {
		
		return JSONUtil.toJSON(
			Collector.getMetrics(
				pathParamList.get(0)	// Client Id
			)
		);
	}
}
