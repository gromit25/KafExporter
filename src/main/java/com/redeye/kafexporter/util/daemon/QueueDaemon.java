package com.redeye.kafexporter.util.daemon;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

/**
 * 큐에서 데이터를 받아 처리하는 데몬 클래스
 * 
 * @param <T> 큐 데이터 타입
 * @author jmsohn
 */
public class QueueDaemon<T> extends AbstractDaemon {
	
	
	/** 데이터 입력 큐 */
	private BlockingQueue<T> queue;
	
	/** 큐 데이터 대기 시간 - 단위: ms */
	@Getter
	private long pollingInterval = 10 * 1000;
	
	/** 스레드 풀 */
	private ExecutorService svc;
	
	/** 스레드 풀의 스레드 수, 0이하일 경우 cached 스레드 풀 사용 */
	@Setter
	private int threadCount = -1;
	
	/** 큐 데이터를 처리할 컨슈머 */
	private Consumer<T> consumer;


	/**
	 * 생성자
	 * 
	 * @param queue 데이터 입력 큐
	 * @param consumer 데이터 처리 컨슈머
	 */
	public QueueDaemon(BlockingQueue<T> queue, Consumer<T> consumer) {
		
		// 입력값 검증
		if(queue == null) {
			throw new IllegalArgumentException("'queue' is null.");
		}
		
		if(consumer == null) {
			throw new IllegalArgumentException("'consumer' is null.");
		}
		
		// 큐 설정
		this.queue = queue;
		
		// 컨슈머 설정
		this.consumer = consumer;
	}
	
	@Override
	protected void process() throws InterruptedException {
		
		// 큐애서 데이터 수신
		T data = this.queue.poll(this.pollingInterval, TimeUnit.MILLISECONDS);
		if(data == null) {
			return;
		}
		
		// 컨슈머 수행
		this.svc.execute(new Runnable() {
			
			@Override
			public void run() {
				consumer.accept(data);
			}
		});
	}
	
	@Override
	protected void prepare() {
		
		// 스레드 풀 설정
		if(this.threadCount < 1) {
			this.svc = Executors.newCachedThreadPool();
		} else {
			this.svc = Executors.newFixedThreadPool(this.threadCount);
		}
	}
	
	@Override
	protected void exit() {
		this.svc.shutdown();
	}

	/**
	 * 큐 대기 시간 설정
	 * 
	 * @param pollingInterval 큐 대기 시간 - 단위: ms
	 */
	public void setPollingInterval(long pollingInterval) {
		
		if(pollingInterval <= 0) {
			throw new IllegalArgumentException("'pollingInterval' must be greater than 0: " + pollingInterval);
		}
		
		this.pollingInterval = pollingInterval;
	}
}
