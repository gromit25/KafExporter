package com.redeye.kafexporter.exporter.http;

import com.redeye.kafexporter.exporter.http.kafka.KafkaClientController;
import com.redeye.kafexporter.exporter.http.kafka.KafkaConfigController;
import com.redeye.kafexporter.exporter.http.kafka.KafkaMetricsController;
import com.redeye.kafexporter.util.http.service.HttpService;

/**
 * 
 * 
 * @author jmsohn
 */
public class HttpExporter {


	/** */
	private HttpService service;
	
	
	/**
	 * 
	 * @param port
	 */
	public HttpExporter(String hostname, int port) throws Exception {
		this.init(hostname, port);
	}

	/**
	 * 
	 * 
	 * @param port
	 */
	private void init(String hostname, int port) throws Exception {
		
		// Http 서버 생성
		this.service = new HttpService(hostname, port);
		
		// kafka 컨트롤러 추가
		this.service.addController(new KafkaClientController());
		this.service.addController(new KafkaConfigController());
		this.service.addController(new KafkaMetricsController());
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public HttpExporter start() throws Exception {
		
		if(this.service != null) {
			this.service.start();
		}
		
		return this;
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public String getHostName() {
		return this.service.getHostName();
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public int getPort() {
		return this.service.getPort();
	}
	
	/**
	 * 
	 * @return
	 */
	public String getHostStr() {
		return this.service.getHostStr();
	}
}
