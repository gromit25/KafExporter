package com.redeye.kafexporter.acquisitor.jmx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kafka 데이터 수집기 클래스
 * 
 * @author jsmohn
 */
public class KafkaJMXAcquisitor {
	
	
	/** Kafka JMX 데이터 수집 객체 */
	private JMXService svc;
	
	
	/**
	 * Kafka 데이터 수집기 초기화
	 */
	public KafkaJMXAcquisitor() throws Exception {
		this.svc = new JMXService();
	}

	/**
	 * Kafka JMX 성능 정보 수집
	 * 
	 * @return Kafka JMX 성능 정보
	 */
	public Map<String, Map<String, Object>> acquire() throws Exception {
		
		// JMX 메시지 수집
		Map<String, Map<String, Object>> nameAttrMap = new ConcurrentHashMap<>();
		
		// 시스템 성능 정보 수집
		nameAttrMap.putAll(this.acquireSystemMetrics());

		// Producer 성능 정보 수집
		nameAttrMap.putAll(this.acquireProducerMetrics());

		// Consumer 성능 정보 수집
		nameAttrMap.putAll(this.acquireConsumerMetrics());
		
		return nameAttrMap;
	}
	
	/**
	 * 시스템 JMX 성능 정보 수집
	 * 
	 * @return 수집된 JMX 성능 정보
	 */
	private Map<String, Map<String, Object>> acquireSystemMetrics() throws Exception {
		return this.svc.getByQuery(
			"java.lang:type=OperatingSystem",
			"SystemCpuLoad",
			"FreePhysicalMemorySize"
		);
	}

	/**
	 * Producer JMX 성능 정보 수집
	 * 
	 * @return 수집된 JMX 성능 정보
	 */
	private Map<String, Map<String, Object>> acquireProducerMetrics() throws Exception {
		return this.svc.getByQuery(
			"kafka.producer:client-id=*,type=producer-metrics",
			"request-latency-avg",
			"request-rate",
			"record-error-rate",
			"outgoing-byte-rate",
			"buffer-total-bytes",
			"buffer-available-bytes"
		);
	}
	
	/**
	 * Producer JMX 성능 정보 수집
	 * 
	 * @return 수집된 JMX 성능 정보
	 */
	private Map<String, Map<String, Object>> acquireConsumerMetrics() throws Exception {
		return this.svc.getByQuery(
			"kafka.consumer:client-id=*,type=consumer-coordinator-metrics",
			"join-rate",
			"sync-rate"
		);
	}
	
	/**
	 * Kafka JMX 객체 close
	 */
	public void close() throws Exception {
		this.svc.close();
	}
}
