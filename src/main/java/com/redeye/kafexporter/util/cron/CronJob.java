package com.redeye.kafexporter.util.cron;


import lombok.Builder;
import lombok.Getter;

/**
 * 크론잡 수행 클래스
 * 
 * @author jmsohn
 */
public class CronJob {
	
	/** 크론 시간 표현식 */
	private CronExp cronExp;
	/** 수행할 잡 */
	private Runnable job;
	
	/** 크론 쓰레드 객체 */
	private Thread cronThread;
	/** 잡 쓰레드 객체 */
	private Thread jobThread;
	
	/** 다음 작업 시간: 중지되어 있을 경우 -1 */
	@Getter
	private long nextTime;
	
	/**
	 * 생성자
	 * 
	 * @param cronExp 크론 시간 표현식
	 * @param job 수행할 잡
	 */
	@Builder
	public CronJob(String cronExp, Runnable job) throws Exception {
		this.setCronExp(cronExp);
		this.setJob(job);
	}
	
	/**
	 * cron job 수행
	 */
	public void run() {
		
		this.cronThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while(true) {
					
					// 다음 수행 시간까지 대기
					try {
						nextTime = cronExp.getNextTimeInMillis();
						Thread.sleep(nextTime - System.currentTimeMillis());
					} catch(InterruptedException iex) {
						break;
					}
					
					// 잡 수행
					jobThread = new Thread(job);
					jobThread.start();
				}
			}
		});
		
		this.cronThread.setDaemon(true);
		this.cronThread.start();
	}

	/**
	 * 현재 작업 중지, 예약도 중지됨 
	 */
	public void stop() {
		
		if(this.jobThread != null) {
			this.jobThread.interrupt();
		}
		
		if(this.cronThread != null) {
			this.cronThread.interrupt();
		}
		
		this.nextTime = -1;
	}
	
	/**
	 * 크론 시간 표현식 설정
	 * 
	 * @param cronExp 크론 시간 표현식 문자열
	 */
	public void setCronExp(String cronExp) throws Exception {
		this.cronExp = CronExp.create(cronExp);
	}
	
	/**
	 * 설정된 크론 시간 표현식 반환
	 * 
	 * @return 설정된 시간 표현식
	 */
	public String getCronExp() {
		return this.cronExp.getCronExp();
	}
	
	/**
	 * 수행할 잡 설정
	 * 
	 * @param job 수행할 잡
	 */
	public void setJob(Runnable job) throws Exception {
		
		if(job == null) {
			throw new NullPointerException("job is null");
		}
		
		this.job = job;
	}
	
	/**
	 * CronJob 객체의 정보 문자열 반환
	 * 
	 * @return CronJob 객체의 정보 문자열
	 */
	public String toString() {
		
		StringBuilder builder = new StringBuilder("");
		
		builder
			.append(this.getCronExp())
			.append(" ")
			.append(this.job.getClass().getCanonicalName());
		
		return builder.toString();
	}
}

