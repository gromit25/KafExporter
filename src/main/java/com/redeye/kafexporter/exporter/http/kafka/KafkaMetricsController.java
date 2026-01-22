package com.redeye.kafexporter.exporter.http.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redeye.kafexporter.acquisitor.kafka.KafkaAcquisitor;
import com.redeye.kafexporter.util.JSONUtil;
import com.redeye.kafexporter.util.http.service.annotation.Controller;
import com.redeye.kafexporter.util.http.service.annotation.RequestHandler;

/**
 * 
 * 
 * @author jmsohn
 */
@Controller(basePath = "/kafka/metrics")
public class KafkaMetricsController {
	
	/**
	 * 성능 정보 반환
	 * 
	 * @return
	 */
	@RequestHandler
	public static String getMetrics() throws Exception {
		
		Map<String, Object> metricsMap = new HashMap<>();
		
		metricsMap.put("system", KafkaAcquisitor.acquireSystemMetrics());
		metricsMap.put("producer", KafkaAcquisitor.acquireProducerMetrics());
		metricsMap.put("consumer", KafkaAcquisitor.acquireConsumerMetrics());
		
		return JSONUtil.toJSON(metricsMap);
	}
	
	/**
	 * 특정 속성에 대한 성능 정보 반환
	 * 
	 * @param pathList
	 * @return
	 */
	@RequestHandler(path = "/*")
	public static String getTypeMetrics(List<String> pathList) throws Exception {
		
		String type = pathList.get(0);
		
		Map<String, Object> metricsMap = new HashMap<>();

		if("producer".equals(type) == true) {
			metricsMap.putAll(KafkaAcquisitor.acquireProducerMetrics());
		} else if("consumer".equals(type) == true) {
			metricsMap.putAll(KafkaAcquisitor.acquireConsumerMetrics());
		}
		
		return JSONUtil.toJSON(metricsMap);
	}
	
	/**
	 * 특정 속성에 대한 성능 정보 반환
	 * 
	 * @param pathList
	 * @return
	 */
	@RequestHandler(path = "/*/*")
	public static String getAttrMetrics(List<String> pathList) throws Exception {
		
		String type = pathList.get(0);
		String attr = pathList.get(1);
		
		Map<String, Object> metricsMap = new HashMap<>();

		if("producer".equals(type) == true) {
			metricsMap.putAll(KafkaAcquisitor.acquireProducerMetrics(List.of(attr)));
		} else if("consumer".equals(type) == true) {
			metricsMap.putAll(KafkaAcquisitor.acquireConsumerMetrics(List.of(attr)));
		}
		
		return JSONUtil.toJSON(metricsMap);
	}
}
