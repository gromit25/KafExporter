package com.redeye.kafexporter.acquisitor.model;

import lombok.Getter;

/**
 * 시간 DTO 클래스
 * 
 * @author jmsohn
 */
public class ClientTimeDTO {
	
	
	/** Kafka 클라이언트 아이디(Producer, Consumer) */
	@Getter
	private final String clientId;
	
	/** 시간 */
	@Getter
	private final long time;
	
	
	/**
	 * 생성자
	 * 
	 * @param clientId Kafka 클라이언트 아이디
	 * @param time 시간
	 */
	public ClientTimeDTO(String clientId, long time) {
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
