package com.redeye.kafexporter.acquisitor.model;

import lombok.Data;

/**
 * 수행 시간 DTO 클래스
 * 
 * @author jmsohn
 */
@Data
public class IntervalDTO {
	
	
	/** Kafka 클라이언트 아이디(Producer, Consumer) */
	private final String clientId;
	
	/** 수행 시간 */
	private final long interval;
	
	
	/**
	 * 생성자
	 * 
	 * @param clientId Kafka 클라이언트 아이디
	 * @param interval 수행 시간
	 */
	public IntervalDTO(String clientId, long interval) {
		this.clientId = clientId;
		this.interval = interval;
	}
	
	/**
	 * 객체 정보를 문자열로 변환하여 반환
	 */
	@Override
	public String toString() {
		
		return new StringBuilder()
			.append(this.clientId)
			.append(": ")
			.append(this.interval)
			.toString();
	}
}
