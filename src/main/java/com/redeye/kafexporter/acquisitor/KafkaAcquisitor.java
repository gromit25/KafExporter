package com.redeye.kafexporter.acquisitor;

import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.redeye.kafexporter.util.StringUtil;
import com.redeye.kafexporter.acquisitor.advice.ConsumerConfigAdvice;
import com.redeye.kafexporter.acquisitor.advice.KafkaConsumerAdvice;
import com.redeye.kafexporter.acquisitor.advice.KafkaConsumerConstructorAdvice;
import com.redeye.kafexporter.acquisitor.advice.ProducerConfigAdvice;
import com.redeye.kafexporter.acquisitor.jmx.JMXService;
import com.redeye.kafexporter.acquisitor.model.TimeDTO;
import com.redeye.kafexporter.util.daemon.QueueDaemon;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Kafka 정보 수집기
 * 
 * @author jmsohn
 */
public class KafkaAcquisitor {
	

	/** 싱글톤 용 Kafka 수집기 객체 */
	private static KafkaAcquisitor acquisitor;

	/** 프로듀스 설정 값 맵 (key: 클라이언트 아이디, Value: 설정 값 맵) */
	private static Map<String, Map<String, Object>> producerConfigMap = new ConcurrentHashMap<>();

	/** 컨슈머 설정 값 맵 (key: 클라이언트 아이디, Value: 설정 값 맵) */
	private static Map<String, Map<String, Object>> consumerConfigMap = new ConcurrentHashMap<>();

	/** polling 시간 수집 큐 */
	private static BlockingQueue<TimeDTO> pollTimeQueue = new LinkedBlockingQueue<>();

	
	/** */
	private QueueDaemon<TimeDTO> pollTimeDaemon;
	
	/** Kafka JMX 데이터 수집 객체 */
	private JMXService jmxSvc = new JMXService();


	/**
	 * Kafka 수집기 인스턴스 반환 - 싱글톤
	 */
	public static KafkaAcquisitor getInstance() {

		if(acquisitor == null) {
			acquisitor = new KafkaAcquisitor();
		}

		return acquisitor;
	}
	
	/**
	 * 생성자
	 */
	private KafkaAcquisitor() {
	}

	/**
	 * 초기화
	 *
	 * @param inst Java 인스트루먼트 객체
	 */
	public KafkaAcquisitor init(Instrumentation inst) {

		// 입력값 검증
		if(inst == null) {
			throw new IllegalArgumentException("'inst' is null.");
		}
		
		// Kafka 메소드 인터셉터 등록
		this.addKafkaTransformer(inst);
		
		//
		this.pollTimeDaemon = new QueueDaemon<>(
			pollTimeQueue,
			(data) -> {
				System.out.println("DEBUG 100: " + data);
			}
		);
		
		this.pollTimeDaemon.run();
		
		return this;
	}
	
	/**
	 * 메소드 인터셉터 등록 - 바이트 코드 변환
	 * 
	 * @param inst Java 인스트루먼트 객체
	 */
	private void addKafkaTransformer(Instrumentation inst) {
		
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

		// KafkaConsumer의 생성자 호출 어드바이스 설정
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.clients.consumer.KafkaConsumer"))
			.transform(
				(builder, typeDescription, classLoader, module, protectionDomain) -> { 
					return builder
						.constructor(ElementMatchers.any())
						.intercept(Advice.to(KafkaConsumerConstructorAdvice.class));
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
		
		// KafkaConsumer의 poll 메소드 호출 어드바이스 설정
		KafkaConsumerAdvice.init(pollTimeQueue);
		
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.clients.consumer.KafkaConsumer"))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder.visit(
						Advice
							.to(KafkaConsumerAdvice.class)
							.on(ElementMatchers.named("poll")
							.and(ElementMatchers.takesArguments(1)))
					);
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
		
		return this.jmxSvc.getByQuery(query);
	}
}
