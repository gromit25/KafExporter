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
 * 
 * 
 * @author jmsohn
 */
public class AcquireJob {
	
	/** */
	private static KafkaJMXAcquisitor jmxAcquisitor;

	/**
	 * 
	 * 
	 * @param period
	 */
	public static void start(String period, Instrumentation inst) {
		
		try {
			
			//
			init(inst);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	private static void init(Instrumentation inst) throws Exception {
		
		//
		jmxAcquisitor = new KafkaJMXAcquisitor();
		
		//
		addTransformer(inst);
	}
	
	/**
	 * 
	 * 
	 * @param inst
	 */
	private static void addTransformer(Instrumentation inst) {
		
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
	
	/**
	 * 
	 */
	private static void acquire() throws Exception {
		
		System.out.println("  ******* Kafka JMX Metrics *******  ");
		System.out.println(jmxAcquisitor.acquireMetrics());
//		
//		System.out.println("  ******* DEBUG 202 *******  ");
//		System.out.println(KafkaConsumerAdvice.pollingTimeParameter);
//		System.out.println("K:" + KafkaConsumerAdvice.pollingTimeParameter.getKurtosis());
		
		//KafkaConsumerAdvice.reset();
	}
}
