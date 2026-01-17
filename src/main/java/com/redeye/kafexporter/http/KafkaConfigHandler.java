package com.redeye.kafexporter.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.redeye.kafexporter.acquisitor.KafkaAcquisitor;
import com.redeye.kafexporter.util.JSONUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
public class KafkaConfigHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		// 설정 값 메시지 생성
		Map<String, Object> configMap = new HashMap<>();

		configMap.put("producer", KafkaAcquisitor.getProducerConfigMap());
		configMap.put("consumer", KafkaAcquisitor.getConsumerConfigMap());
		
		String response = JSONUtil.toJSON(configMap);
		
		// 응답 헤더 설정 (상태 코드 200, 본문 길이)
        exchange.sendResponseHeaders(200, response.length());
        
        // 응답 본문 전송
        try(OutputStream os = exchange.getResponseBody()) {
        	os.write(response.getBytes());
        }
	}
}
