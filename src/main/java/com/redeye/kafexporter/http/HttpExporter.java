package com.redeye.kafexporter.http;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class HttpExporter {


	private HttpServer server;
	
	
	/**
	 * 
	 * @param port
	 */
	public HttpExporter(int port) throws Exception {
		this.init(port);
	}

	/**
	 * 
	 * 
	 * @param port
	 */
	private void init(int port) throws Exception {
		
		// Http 서버 생성
		this.server = HttpServer.create(new InetSocketAddress(port), 0);
		
		// url 별 컨텍스트 설정
		this.server.createContext("/kafka/config", new KafkaConfigHandler());
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public HttpExporter start() throws Exception {
		
		if(this.server!= null) {
			this.server.start();
		}
		
		return this;
	}
}
