package com.redeye.kafexporter.http.kafka;

import java.util.HashMap;
import java.util.Map;

import com.redeye.kafexporter.acquisitor.KafkaAcquisitor;
import com.redeye.kafexporter.http.AbstractJSONHandler;
import com.redeye.kafexporter.util.JSONUtil;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
public class KafkaConfigHandler extends AbstractJSONHandler {

	@Override
	public String execute(HttpExchange exchange) {
		
		// 설정 값 메시지 생성
		Map<String, Object> configMap = new HashMap<>();

		configMap.put("producer", KafkaAcquisitor.getProducerConfigMap());
		configMap.put("consumer", KafkaAcquisitor.getConsumerConfigMap());
		
		return JSONUtil.toJSON(configMap);
	}
}
