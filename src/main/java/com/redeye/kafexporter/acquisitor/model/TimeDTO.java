package com.redeye.kafexporter.acquisitor.model;

import lombok.Data;

/**
 * 시간 DTO 클래스
 * 
 * @author jmsohn
 */
@Data
public class TimeDTO {
	
	
	/** Kafka 클라이언트 아이디(Producer, Consumer) */
	private final String clientId;
	
	/** 시간 */
	private final long time;
	
	
	/**
	 * 생성자
	 * 
	 * @param clientId Kafka 클라이언트 아이디
	 * @param time 시간
	 */
	public TimeDTO(String clientId, long time) {
		this.clientId = clientId;
		this.time = time;
	}
	
	/**
	 * 객체 정보를 문자열로 변환하여 반환
	 */
	@Override
	public String toString() {
		
		return new StringBuilder()
			.append(this.clientId)
			.append(": ")
			.append(this.time)
			.toString();
	}
}
