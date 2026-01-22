package com.redeye.kafexporter.acquisitor.kafka;

import java.lang.instrument.Instrumentation;

import com.redeye.kafexporter.acquisitor.kafka.advice.ConsumerConfigAdvice;
import com.redeye.kafexporter.acquisitor.kafka.advice.KafkaConsumerConstructorAdvice;
import com.redeye.kafexporter.acquisitor.kafka.advice.KafkaConsumerPollAdvice;
import com.redeye.kafexporter.acquisitor.kafka.advice.ProducerConfigAdvice;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * kafka 메소드 변환 클래스
 * 
 * @author jmsohn
 */
public class KafkaTransformer {
	
	/**
	 * 메소드 인터셉터 등록 - 바이트 코드 변환
	 * 
	 * @param inst Java 인스트루먼트 객체
	 */
	public static void addKafkaTransformer(Instrumentation inst) {

		// 입력값 검증
		if(inst == null) {
			throw new IllegalArgumentException("'inst' is null.");
		}
		
		// --- Kafka ProducerConfig 메소드 훅킹 설정
		
		// Kafka ProducerConfig 생성자 호출 어드바이스 설정
		ProducerConfigAdvice.init(KafkaAcquisitor.producerConfigMap);
			
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
		ConsumerConfigAdvice.init(KafkaAcquisitor.consumerConfigMap);
		
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
		KafkaConsumerPollAdvice.init(KafkaAcquisitor.pollTimeQueue);

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
}
