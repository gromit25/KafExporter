package com.redeye.kafexporter.util.deamon;

/**
 * 데몬 프로그램을 위한 추상 클래스
 * 
 * @author jmsohn
 */
public abstract class AbstractDaemon {
	
	
	/** 데몬 스레드 */
	private volatile Thread daemonThread;
	
	
	/**
	 * 업무 처리
	 * 
	 * @throws InterruptedException 인터럽트 호출시 발생되는 예외
	 */
	protected abstract void process() throws InterruptedException;
	
	/**
	 * 시작시 호출됨(콜백)<br>
	 * 필요시 Override 하여 사용
	 */
	protected void prepare() {
		// Do nothing
	}
	
	/**
	 * 종료시 호출됨(콜백)<br>
	 * 필요시 Override 하여 사용
	 */
	protected void exit() {
		// Do nothing
	}
	
	/**
	 * 데몬 스레드 생성 및 실행
	 */
	public synchronized void run() {
		
		// 이미 실행 중이면 반환
		if(this.isAlive() == true) {
			return;
		}
		
		// 데몬 스레드 생성
		this.daemonThread = new Thread(
			new Runnable() {
				public void run() {
					
					final Thread currentThread = Thread.currentThread();
					
					// 시작시 호출
					prepare();
					
					try {
						
						// 인터럽트 발생시까지 무한 루프
						while(currentThread.isInterrupted() == false) {
						
							try {
								process();
							} catch(InterruptedException iex) {
								currentThread.interrupt();
							} catch(Exception ex) {
								ex.printStackTrace();
							}
						}
						
					} finally {
					
						// 종료시 호출
						exit();
					}
				}
			}
		);
		
		// 데몬 스레드 설정
		this.daemonThread.setDaemon(true);
		
		// 데몬 스레드 실행
		this.daemonThread.start();
	}

	/**
	 * 데몬 스레드 중단
	 */
	public synchronized void stop() {
		
		if(this.daemonThread == null || this.daemonThread.isAlive() == false) {
			return;
		}
		
		this.daemonThread.interrupt();
	}
	
	/**
	 * 데몬이 동작 중인지 여부 반환
	 * 
	 * @return 데몬 동작 여부
	 */
	public boolean isAlive() {
		return this.daemonThread != null && this.daemonThread.isAlive() == true;
	}
}
