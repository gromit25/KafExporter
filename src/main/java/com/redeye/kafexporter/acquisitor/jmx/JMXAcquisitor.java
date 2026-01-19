package com.redeye.kafexporter.acquisitor.jmx;

import java.util.Map;

/**
 * Kafka 데이터 수집기 클래스
 * 
 * @author jsmohn
 */
public class JMXAcquisitor {
	
	
	/** Kafka JMX 데이터 수집 객체 */
	private static JMXService svc = new JMXService();
	
	
	/**
	 * 시스템 JMX 성능 정보 수집
	 * 
	 * @return 수집된 JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> acquireSystemMetrics() throws Exception {
		return svc.getByQuery(
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
	public static Map<String, Map<String, Object>> acquireProducerMetrics() throws Exception {
		return svc.getByQuery(
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
	public static Map<String, Map<String, Object>> acquireConsumerMetrics() throws Exception {
		return svc.getByQuery(
			"kafka.consumer:client-id=*,type=consumer-coordinator-metrics",
			"join-rate",
			"sync-rate"
		);
	}
	
	/**
	 * Kafka JMX 객체 close
	 */
	public static void close() throws Exception {
		svc.close();
	}
}
