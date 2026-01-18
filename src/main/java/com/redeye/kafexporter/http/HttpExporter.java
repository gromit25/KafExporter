package com.redeye.kafexporter.http;

import com.redeye.kafexporter.http.kafka.KafkaConfigController;
import com.redeye.kafexporter.util.http.service.HttpService;

@SuppressWarnings("restriction")
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
		
		// 컨트롤러 추가
		this.service.addController(new KafkaConfigController());
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
}
