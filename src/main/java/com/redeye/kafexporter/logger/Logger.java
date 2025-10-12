package com.redeye.kafexporter.logger;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;

/**
 * 입력 큐의 로그 메시지를 로그 writer 에게 전달
 * 
 * @author jmsohn
 */
class Logger implements Runnable {
	
	/** 로그 메시지 입력 큐 */
	private BlockingQueue<String> inQ;
	
	/** 로그 writer */
	private List<LogWriter> writers;
	
	/** 중단 여부 */
	@Getter
	@Setter
	private boolean stop;
	
	/**
	 * 생성자
	 *
	 * @param inQ 로그 메시지 입력 큐
	 * @param writers 로그 Writer 목록
	 */
	Logger(BlockingQueue<String> inQ, List<LogWriter> writers) {
		
		this.inQ = inQ;
		this.writers = writers;
	}

	@Override
	public void run() {

		// 로그 메시지 변수
		String logMsg = null;

		// 중단 플래그가 없을 경우 무한 반복
		while(this.isStop() == false) {
			
			try {
				
				// 큐에서 로그 메시지 획득
				while(logMsg == null) {
					logMsg = this.inQ.poll(10, TimeUnit.MILLISECONDS);
				}
				
				// 로그 메시지 write
				for(LogWriter writer: this.writers) { 
					writer.write(logMsg);
				}
				
				// 초기화
				logMsg = null;
				
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		} // End of while
	}
}
