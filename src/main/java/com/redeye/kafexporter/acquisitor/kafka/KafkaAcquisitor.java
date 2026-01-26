package com.redeye.kafexporter.acquisitor.kafka;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.redeye.kafexporter.acquisitor.kafka.stat.TimeStatDaemon;
import com.redeye.kafexporter.util.StringUtil;
import com.redeye.kafexporter.util.jmx.JMXService;

/**
 * Kafka 정보 수집기
 * 
 * @author jmsohn
 */
public class KafkaAcquisitor {


	/**
	 * kafka 클라이언트 타입
	 * 
	 * @author jmsohn
	 */
	public enum ClientType {
		NONE,
		PRODUCER,
		CONSUMER;
	}
	

	/** 프로듀스 설정 값 맵 (key: 클라이언트 아이디, value: 설정 값 맵) */
	static final Map<String, Map<String, Object>> producerConfigMap = new ConcurrentHashMap<>();

	/** 컨슈머 설정 값 맵 (key: 클라이언트 아이디, value: 설정 값 맵) */
	static final Map<String, Map<String, Object>> consumerConfigMap = new ConcurrentHashMap<>();

	
	/** 폴링 시간 통계 데몬 */
	final static TimeStatDaemon poolTimeStatDaemon = new TimeStatDaemon();
	
	/** 동기 커밋 시간 통계 데몬 */
	final static TimeStatDaemon commitSyncTimeStatDaemon = new TimeStatDaemon();
	
	/** 비동기 시간 통계 데몬 */
	final static TimeStatDaemon commitAsyncTimeStatDaemon = new TimeStatDaemon();

	
	/** Kafka JMX 데이터 수집 객체 */
	private static final JMXService svc = new JMXService();
	
	
	/**
	 * 초기화
	 */
	public static void init() {
		
		//
		poolTimeStatDaemon.start();
		commitSyncTimeStatDaemon.start();
		commitAsyncTimeStatDaemon.start();
	}

	/**
	 * 클라이언트 아이디의 타입을 반환
	 * 
	 * @param clientId 클라이언트 아이디
	 * @return 클라이언트 타입
	 */
	public static ClientType getClientType(String clientId) {
		
		// 프로듀서 클라이언트 아이디일 경우
		if(
			Collector.producerConfigMap != null
			&& Collector.producerConfigMap.containsKey(clientId) == true
		) {
			return ClientType.PRODUCER;
		}
		
		// 컨슈머 클라이언트 아이디일 경우
		if(
			Collector.consumerConfigMap != null
			&& Collector.consumerConfigMap.containsKey(clientId) == true
		) {
			return ClientType.CONSUMER;
		}
		
		// 둘다 아닐 경우
		return ClientType.NONE;
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public static Map<String, Map<String, Object>> getProducerConfigMap() {
		return producerConfigMap;
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public static Map<String, Map<String, Object>> getConsumerConfigMap() {
		return consumerConfigMap;
	}
	
	/**
	 * 
	 * @return
	 */
	public static Set<String> getProducerClientIdList() {
		return getProducerConfigMap().keySet();
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public static Set<String> getConsumerClientIdList() {
		return getConsumerConfigMap().keySet();
	}
	
	/**
	 * 
	 * 
	 * @param clientId
	 * @return
	 */
	public static Map<String, Object> getConfig(String clientId) {
		
		if(producerConfigMap != null && producerConfigMap.containsKey(clientId) == true) {
			return producerConfigMap.get(clientId);
		}
		
		if(consumerConfigMap != null && consumerConfigMap.containsKey(clientId) == true) {
			return consumerConfigMap.get(clientId);
		}
		
		return Map.of();
	}
	
	/**
	 * 설정 속성 값 반환
	 * 
	 * @param clientId 클라이언트 아이디
	 * @param propName 설정 속성 명
	 * @return 설정 속성 값
	 */
	public static Object getConfigValue(String clientId, String propName) {
		
		if(StringUtil.isBlank(clientId) == true || StringUtil.isBlank(propName) == true) {
			return null;
		}
		
		return getConfig(clientId).get(propName);
	}

	/**
	 * 설정 속성 값 문자열 반환<br>
	 * toString 한 결과
	 * 
	 * @param clientId 클라이언트 아이디
	 * @param propName 설정 속성 명
	 * @return 설정 속성 값 문자열
	 */
	public static String getConfigStr(String clientId, String propName) {
		
		Object value = getConfigValue(clientId, propName);
		
		if(value == null) {
			return null;
		} else {
			return value.toString();
		}
	}
	
	/**
	 * 시스템 JMX 성능 정보 수집
	 * 
	 * @return 수집된 JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> getSystemMetrics() throws Exception {
		
		return Collector.svc.getByQuery(
			"java.lang:type=OperatingSystem",
			"SystemCpuLoad",
			"FreePhysicalMemorySize"
		);
	}
	
	/**
	 * Broker JMX 성능 정보 수집
	 * 
	 * @return Broker JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> getBrokerMetrics() throws Exception {
		
		List<String> queryList = List.of(
			"kafka.log:type=*,name=*,topic=*,partition=*",
			"kafka.server:type=*,name=*",
			"kafka.controller:type=*,name=*",
			"kafka.coordinator.group:type=*,name=*",
			"kafka.network:type=*,name=*"
		);
		
		Map<String, Map<String, Object>> metricsMap = new HashMap<>();
		
		for(String query: queryList) {
			metricsMap.putAll(Collector.svc.getByQuery(query));
		}
		
		return metricsMap;
	}
	
	/**
	 * JMX 성능 정보 수집
	 * 
	 * @param clientId 클라이언트 아이디
	 * @return JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> getMetrics(String clientId) throws Exception {
		
		ClientType clientType = getClientType(clientId);
		
		if(ClientType.PRODUCER == clientType) {
			return getProducerMetrics(clientId);
		} else if(ClientType.CONSUMER == clientType) {
			return getConsumerMetrics(clientId);
		}
		
		return Map.of();
	}
	
	/**
	 * Producer JMX 성능 정보 수집
	 * 
	 * @param clientId 클라이언트 아이디
	 * @return JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> getProducerMetrics(String clientId) throws Exception {
		
		// 클라이언트 아이디가 공란이거나 null 이면 모든 클라이언트에 대해 조회
		if(StringUtil.isBlank(clientId) == true) {
			clientId = "*";
		}
		
		// 조회 쿼리 목록
		List<String> queryList = List.of(
			"kafka.producer:type=producer-metrics,client-id=" + clientId
		);
		
		// 성능 조회
		Map<String, Map<String, Object>> metricsMap = new HashMap<>();
		
		for(String query: queryList) {
			metricsMap.putAll(Collector.svc.getByQuery(query));
		}
		
		return metricsMap;
	}
	
	/**
	 * Producer JMX 성능 정보 수집<br>
	 * 클라이언트 아이디가 공란이거나 null 이면 모든 클라이언트에 대해 조회
	 * 
	 * @param clientId 클라이언트 아이디
	 * @return JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> getConsumerMetrics(String clientId) throws Exception {
		
		// 클라이언트 아이디가 공란이거나 null 이면 모든 클라이언트에 대해 조회
		if(StringUtil.isBlank(clientId) == true) {
			clientId = "*";
		}
		
		// 조회 쿼리 목록
		List<String> queryList = List.of(
			"kafka.consumer:type=consumer-metrics,client-id=" + clientId,
			"kafka.consumer:type=consumer-fetch-manager-metrics,client-id=" + clientId,
			"kafka.consumer:type=consumer-topic-metrics,client-id=" + clientId,
			"kafka.consumer:type=consumer-coordinator-metrics,client-id=" + clientId
		);
		
		// 성능 조회
		Map<String, Map<String, Object>> metricsMap = new HashMap<>();
		
		for(String query: queryList) {
			metricsMap.putAll(Collector.svc.getByQuery(query));
		}
		
		return metricsMap;
	}
}
