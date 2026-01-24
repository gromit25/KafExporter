package com.redeye.kafexporter.acquisitor.kafka.stat;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.redeye.kafexporter.acquisitor.kafka.model.ClientTimeDTO;
import com.redeye.kafexporter.util.daemon.QueueDaemon;
import com.redeye.kafexporter.util.stat.Parameter;

import lombok.Getter;

/**
 * 
 * 
 * @author jmsohn
 */
public class TimeStatDaemon {
	
	
	/** */
	private QueueDaemon<ClientTimeDTO> timeDaemon = null;
	
	/** */
	private final Map<String, Long> clientTimeMap = new ConcurrentHashMap<>();
	
	/** */
	private final Map<String, Parameter> clientTimeStatMap = new ConcurrentHashMap<>();

	/** polling 시간 수집 큐 */
	@Getter
	private final BlockingQueue<ClientTimeDTO> queue = new LinkedBlockingQueue<>();

	
	/**
	 * 
	 */
	public TimeStatDaemon() {
		
		//
		this.timeDaemon = new QueueDaemon<>(
			this.queue,
			data -> {
				
				// 기존 값 저장 
				Long prePollTime = clientTimeMap.get(data.getClientId());
				
				// 폴링 시간 저장
				clientTimeMap.put(data.getClientId(), data.getTime());
				
				// 통계 정보 저장
				Parameter pollTimeStat = clientTimeStatMap.computeIfAbsent(
					data.getClientId(), key -> new Parameter()
				);
				
				if(prePollTime != null) {
					long interval = data.getTime() - prePollTime;
					pollTimeStat.add(interval);
				}
				
				System.out.println("### STAT : \n" + pollTimeStat);
			}
		);
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public TimeStatDaemon start() {
		this.timeDaemon.run();
		return this;
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public TimeStatDaemon stop() {
		this.timeDaemon.stop();
		return this;
	}
}
