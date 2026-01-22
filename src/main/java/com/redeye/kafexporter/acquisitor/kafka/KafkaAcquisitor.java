package com.redeye.kafexporter.acquisitor.kafka;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.redeye.kafexporter.acquisitor.kafka.model.ClientTimeDTO;
import com.redeye.kafexporter.util.StringUtil;
import com.redeye.kafexporter.util.daemon.QueueDaemon;
import com.redeye.kafexporter.util.jmx.JMXService;
import com.redeye.kafexporter.util.stat.Parameter;

/**
 * Kafka 정보 수집기
 * 
 * @author jmsohn
 */
public class KafkaAcquisitor {
	

	/** 프로듀스 설정 값 맵 (key: 클라이언트 아이디, value: 설정 값 맵) */
	static final Map<String, Map<String, Object>> producerConfigMap = new ConcurrentHashMap<>();

	/** 컨슈머 설정 값 맵 (key: 클라이언트 아이디, value: 설정 값 맵) */
	static final Map<String, Map<String, Object>> consumerConfigMap = new ConcurrentHashMap<>();

	/** polling 시간 수집 큐 */
	static final BlockingQueue<ClientTimeDTO> pollTimeQueue = new LinkedBlockingQueue<>();
 
	
	/** */
	private static QueueDaemon<ClientTimeDTO> pollTimeDaemon = null;
	
	/** */
	private static final Map<String, Parameter> pollTimeStatMap = new ConcurrentHashMap<>();
	
	/** (key: 클라이언트 아이디, value: 마지막 poll 호출 시간) */
	private static final Map<String, Long> pollTimeMap = new ConcurrentHashMap<>();
	
	/** Kafka JMX 데이터 수집 객체 */
	private static final JMXService svc = new JMXService();
	
	
	/**
	 * 초기화
	 */
	public static void init() {

		//
		pollTimeDaemon = new QueueDaemon<>(
			pollTimeQueue,
			data -> {
				
				// 기존 값 저장 
				Long prePollTime = pollTimeMap.get(data.getClientId());
				
				// 폴링 시간 저장
				pollTimeMap.put(data.getClientId(), data.getTime());
				
				// 통계 정보 저장
				Parameter pollTimeStat = pollTimeStatMap.computeIfAbsent(
					data.getClientId(), key -> new Parameter()
				);
				
				if(prePollTime != null) {
					long interval = data.getTime() - prePollTime;
					pollTimeStat.add(interval);
				}
				
				System.out.println("### STAT : \n" + pollTimeStat);
			}
		);
		
		pollTimeDaemon.run();
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
	 * @param attrs 속성명
	 * @return 수집된 JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> acquireProducerMetrics(List<String> attrs) throws Exception {
		return svc.getByQuery(
			"kafka.producer:client-id=*,type=producer-metrics",
			attrs.toArray(new String[0])
		);
	}
	
	/**
	 * Producer JMX 성능 정보 수집
	 * 
	 * @return 수집된 JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> acquireProducerMetrics() throws Exception {
		return acquireProducerMetrics(
			List.of(
				"record-send-rate",
				"record-error-rate",
				"request-latency-avg",
				"request-latency-max",
				"request-error-rate",
				"request-error-total",
				"buffer-total-bytes",
				"buffer-available-bytes"
			)
		);
	}
	
	/**
	 * Producer JMX 성능 정보 수집
	 * 
	 * @param attrs 속성명
	 * @return 수집된 JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> acquireConsumerMetrics(List<String> attrs) throws Exception {
		return svc.getByQuery(
			"kafka.consumer:client-id=*,type=consumer-coordinator-metrics",
			attrs.toArray(new String[0])
		);
	}
	
	/**
	 * Producer JMX 성능 정보 수집
	 * 
	 * @return 수집된 JMX 성능 정보
	 */
	public static Map<String, Map<String, Object>> acquireConsumerMetrics() throws Exception {
		return acquireConsumerMetrics(
			List.of(
				"join-rate",
				"sync-rate"
			)
		);
	}
}
