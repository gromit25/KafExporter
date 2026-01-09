package com.redeye.kafexporter.acquisitor;

import java.lang.instrument.Instrumentation;

import com.redeye.kafexporter.acquisitor.advice.ConsumerConfigAdvice;
import com.redeye.kafexporter.acquisitor.advice.KafkaConsumerAdvice;
import com.redeye.kafexporter.acquisitor.advice.ProducerConfigAdvice;
import com.redeye.kafexporter.acquisitor.jmx.KafkaJMXAcquisitor;
import com.redeye.kafexporter.util.cron.CronJob;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Kafka 정보 수집기
 * 
 * @author jmsohn
 */
public class KafkaAcquisitor {
	
	
	/** Kafka JMX 정보 수집기 */
	private KafkaJMXAcquisitor jmxAcquisitor  = new KafkaJMXAcquisitor();

	/** 프로듀스 설정 값 맵 (key: 클라이언트 아이디, Value: 설정 값 맵) */
	private Map<String, Map<String, Object>> producerConfigMap = new ConcurrentHashMap<>();

	/** 컨슈머 설정 값 맵 (key: 클라이언트 아이디, Value: 설정 값 맵) */
	private Map<String, Map<String, Object>> consumerConfigMap = new ConcurrentHashMap<>();
	
	
	/**
	 * 생성자
	 *
	 * @param inst Java 인스트루먼트 객체
	 */
	public KafkaAcquisitor(Instrumentation inst) throws Exception {
		
		//
		this.addTransformer(inst);
	}
	
	/**
	 * 
	 * 
	 * @param inst Java 인스트루먼트 객체
	 */
	private void addTransformer(Instrumentation inst) {
		
		// KafkaConsumer의 poll 메소드 호출 어드바이스 설정
		new AgentBuilder.Default()
			.type(ElementMatchers.named("org.apache.kafka.clients.consumer.KafkaConsumer"))
			.transform(
				(builder, typeDescription, classLoader, module, protectedDomain) -> {
					return builder
						.method(ElementMatchers.named("poll"))
						.intercept(Advice.to(KafkaConsumerAdvice.class));
				}
			)
	    	.installOn(inst);
		
		// Kafka ConsumerConfig 생성자 호출 어드바이스 설정
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
		
		// Kafka ProducerConfig 생성자 호출 어드바이스 설정
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
	}
}
