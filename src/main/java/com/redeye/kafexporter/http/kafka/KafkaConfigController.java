package com.redeye.kafexporter.http.kafka;

import java.util.HashMap;
import java.util.Map;

import com.redeye.kafexporter.acquisitor.KafkaAcquisitor;
import com.redeye.kafexporter.util.JSONUtil;
import com.redeye.kafexporter.util.http.service.annotation.Controller;
import com.redeye.kafexporter.util.http.service.annotation.RequestHandler;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
@Controller(basePath = "/kafka/config")
public class KafkaConfigController {

	/**
	 * 
	 * 
	 * @param exchange
	 * @return
	 */
	@RequestHandler
	public String getConfig(HttpExchange exchange) {
		
		// 설정 값 메시지 생성
		Map<String, Object> configMap = new HashMap<>();

		configMap.put("producer", KafkaAcquisitor.getProducerConfigMap());
		configMap.put("consumer", KafkaAcquisitor.getConsumerConfigMap());
		
		return JSONUtil.toJSON(configMap);
	}
}
