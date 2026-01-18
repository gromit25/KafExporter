package com.redeye.kafexporter.util.http.service;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.net.httpserver.HttpServer;

/**
 * 
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
public class HttpService {
	
	
	/** Http 서버 */
	private HttpServer server;
	
	/** */
	private final List<ControllerContext> controllerList = new CopyOnWriteArrayList<>();
	
	
	/**
	 * 생성자
	 * 
	 * @param host
	 * @param port
	 */
	public HttpService(String host, int port) throws Exception {
		
		this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
	}
	
	/**
	 * 
	 * 
	 * @param controller
	 * @return 현재 객체
	 */
	public HttpService addController(Object controller) throws Exception {
		
		this.controllerList.add(new ControllerContext(controller));
		return this;
	}
	
	/**
	 * 
	 * 
	 * @return 현재 객체
	 */
	public HttpService start() {
		
		for(ControllerContext controller: this.controllerList) {
			this.server.createContext(controller.getBasePath(), controller);
		}
		
		this.server.start();
		
		return this;
	}
	
	/**
	 * 
	 * 
	 * @return 현재 객체
	 */
	public HttpService stop() {
		
		this.server.stop(1000);
		
		return this;
	}
}
