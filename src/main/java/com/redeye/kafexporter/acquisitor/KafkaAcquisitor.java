package com.redeye.kafexporter.acquisitor;

import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.redeye.kafexporter.util.StringUtil;
import com.redeye.kafexporter.acquisitor.advice.ConsumerConfigAdvice;
import com.redeye.kafexporter.acquisitor.advice.KafkaConsumerPollAdvice;
import com.redeye.kafexporter.acquisitor.advice.KafkaConsumerConstructorAdvice;
import com.redeye.kafexporter.acquisitor.advice.ProducerConfigAdvice;
import com.redeye.kafexporter.acquisitor.jmx.JMXService;
import com.redeye.kafexporter.acquisitor.model.TimeDTO;
import com.redeye.kafexporter.util.daemon.QueueDaemon;
import com.redeye.kafexporter.util.stat.Parameter;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Kafka 정보 수집기
 * 
 * @author jmsohn
 */
public class KafkaAcquisitor {
	

	/** 프로듀스 설정 값 맵 (key: 클라이언트 아이디, value: 설정 값 맵) */
	private static final Map<String, Map<String, Object>> producerConfigMap = new ConcurrentHashMap<>();

	/** 컨슈머 설정 값 맵 (key: 클라이언트 아이디, value: 설정 값 맵) */
	private static final Map<String, Map<String, Object>> consumerConfigMap = new ConcurrentHashMap<>();

	/** polling 시간 수집 큐 */
	private static final BlockingQueue<TimeDTO> pollTimeQueue = new LinkedBlockingQueue<>();

	/** */
	private static QueueDaemon<TimeDTO> pollTimeDaemon = null;
	
	/** */
	private static final Map<String, Parameter> pollTimeStatMap = new ConcurrentHashMap<>();
	
	/** (key: 클라이언트 아이디, value: 마지막 poll 호출 시간) */
	private static final Map<String, Long> pollTimeMap = new ConcurrentHashMap<>();
	
	
	/** Kafka JMX 데이터 수집 객체 */
	private static JMXService jmxSvc = new JMXService();


	/**
	 * 초기화
	 *
	 * @param inst Java 인스트루먼트 객체
	 */
	public static void init(Instrumentation inst) {

		// 입력값 검증
		if(inst == null) {
			throw new IllegalArgumentException("'inst' is null.");
		}
		
		// Kafka 메소드 인터셉터 등록
		addKafkaTransformer(inst);
		
		//
		pollTimeDaemon = new QueueDaemon<>(
			pollTimeQueue,
			(data) -> {
				
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
	 * 메소드 인터셉터 등록 - 바이트 코드 변환
	 * 
	 * @param inst Java 인스트루먼트 객체
	 */
	private static void addKafkaTransformer(Instrumentation inst) {
		
		// --- Kafka ProducerConfig 메소드 훅킹 설정
		
		// Kafka ProducerConfig 생성자 호출 어드바이스 설정
		ProducerConfigAdvice.init(producerConfigMap);
			
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.clients.producer.ProducerConfig"))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(ProducerConfigAdvice.class));
				}
			)
        	.installOn(inst);
		
		// --- Kafka ConsumerConfig 메소드 훅킹 설정
		
		// Kafka ConsumerConfig 생성자 호출 어드바이스 설정
		ConsumerConfigAdvice.init(consumerConfigMap);
		
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.clients.consumer.ConsumerConfig"))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(ConsumerConfigAdvice.class));
				}
			)
			.installOn(inst);
		
		// --- KafkaConsumer 메소드 훅킹 설정
		
		// 초기화
		KafkaConsumerPollAdvice.init(pollTimeQueue);

		// KafkaConsumer의 생성자 호출 어드바이스 설정
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.clients.consumer.KafkaConsumer"))
			.transform(
				(builder, typeDescription, classLoader, module, protectionDomain) -> { 
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(KafkaConsumerConstructorAdvice.class))
						.visit(
							Advice
								.to(KafkaConsumerPollAdvice.class)
								.on(ElementMatchers.named("poll")
								.and(ElementMatchers.takesArguments(1)))
						);
				}
			)
			.installOn(inst);
		
		// 스프링부트의 KafkaConsumer의 생성자 호출 어드바이스 설정
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.springframework.kafka.core.DefaultKafkaConsumerFactory$ExtendedKafkaConsumer"))
			.transform(
				(builder, typeDescription, classLoader, module, protectionDomain) -> { 
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(KafkaConsumerConstructorAdvice.class));
				}
			)
			.installOn(inst);
	}
	
	/**
	 * JMX 성능 정보 반환
	 * 
	 * @param query JMX 쿼리
	 * @return 성능 정보 반환
	 */
	public Map<String, Map<String, Object>> getJMXMetrics(String query) throws Exception {
		
		if(StringUtil.isBlank(query) == true) {
			throw new IllegalArgumentException("'query' is null.");
		}
		
		return jmxSvc.getByQuery(query);
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
		
		if(producerConfigMap != null && producerConfigMap.containsKey(clientId) == true) {
			return producerConfigMap.get(clientId).get(propName);
		}
		
		if(consumerConfigMap != null && consumerConfigMap.containsKey(clientId) == true) {
			return consumerConfigMap.get(clientId).get(propName);
		}
		
		return null;
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
}
