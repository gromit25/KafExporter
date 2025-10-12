package com.redeye.kafexporter.logger;

/**
 * 로그 저장 인터페이스
 * 
 * @author jmsohn
 */
public interface LogWriter {
	
	/**
	 * 로그 저장 메소드
	 * 
	 * @param msg 저장할 로그 메시지
	 */
	public void write(String msg) throws Exception;
}
