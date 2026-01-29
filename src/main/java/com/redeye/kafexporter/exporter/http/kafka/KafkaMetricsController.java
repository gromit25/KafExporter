package com.redeye.kafexporter.exporter.http.kafka;

import java.util.HashMap;
import java.util.Map;

import com.epozen.emma.exporter.kafka.Collector;
import com.epozen.emma.exporter.kafka.util.JSONUtil;
import com.epozen.emma.exporter.kafka.util.http.annotation.Controller;
import com.epozen.emma.exporter.kafka.util.http.annotation.RequestHandler;

/**
 * Kafka 성능 정보 관련 컨트롤러
 * 
 * @author jmsohn
 */
@Controller(basePath = "/metrics")
public class KafkaMetricsController {
	
	/**
	 * Kafka 전체 성능 정보 반환
	 * 
	 * @return Kafka 전체 성능 정보
	 */
	@RequestHandler
	public static String getMetrics() throws Exception {
		
		Map<String, Object> metricsMap = new HashMap<>();
		
		metricsMap.put("system", Collector.getSystemMetrics());
		metricsMap.put("broker", Collector.getBrokerMetrics());
		metricsMap.put("producer", Collector.getProducerMetrics(null));
		metricsMap.put("consumer", Collector.getConsumerMetrics(null));
		
		return JSONUtil.toJSON(metricsMap);
	}
	
	/**
	 * 브로커 성능 정보 반환
	 * 
	 * @return 브로커 성능 정보
	 */
	@RequestHandler(path = "/broker")
	public static String getBrokerMetrics() throws Exception {
		return JSONUtil.toJSON(Collector.getBrokerMetrics());
	}
	
	/**
	 * 컨슈머 래그 성능 정보 반환
	 * 
	 * @return 컨슈머 래그 성능 정보
	 */
	@RequestHandler(path = "/broker/lag")
	public static String getConsumerGroupLag() throws Exception {
		return JSONUtil.toJSON(Collector.getConsumerLag());
	}
	
	/**
	 * 프로듀서 성능 정보 반환
	 * 
	 * @return 프로듀서 성능 정보
	 */
	@RequestHandler(path = "/producer")
	public static String getProducerMetrics() throws Exception {
		return JSONUtil.toJSON(Collector.getProducerMetrics("*"));
	}
	
	/**
	 * 컨슈머 성능 정보 반환
	 * 
	 * @return 컨슈머 성능 정보
	 */
	@RequestHandler(path = "/consumer")
	public static String getConsumerMetrics() throws Exception {
		return JSONUtil.toJSON(Collector.getConsumerMetrics("*"));
	}
}
