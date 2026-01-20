package com.redeye.kafexporter.exporter.http.kafka;

import java.util.HashMap;
import java.util.Map;

import com.redeye.kafexporter.acquisitor.kafka.KafkaAcquisitor;
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
@Controller(basePath = "/kafka/metrics")
public class KafkaMetricsController {
	
	@RequestHandler
	public static String getMetrics(HttpExchange exchange) throws Exception {
		
		Map<String, Object> metricsMap = new HashMap<>();
		
		metricsMap.put("system", KafkaAcquisitor.acquireSystemMetrics());
		metricsMap.put("producer", KafkaAcquisitor.acquireProducerMetrics());
		metricsMap.put("consumer", KafkaAcquisitor.acquireConsumerMetrics());
		
		return JSONUtil.toJSON(metricsMap);
	}
}
