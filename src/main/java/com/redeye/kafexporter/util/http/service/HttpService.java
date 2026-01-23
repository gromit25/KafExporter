package com.redeye.kafexporter.util.http.service;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import lombok.Getter;

/**
 * Http 서비스 클래스
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
public class HttpService {
	
	
	/** Http 서버 */
	private HttpServer server;
	
	/** Http 서버명 */
	@Getter
	private String hostName;
	
	/** 포트 번호 */
	@Getter
	private int port;
	
	/** 컨트롤러 목록 */
	private final List<ControllerContext> controllerList = new CopyOnWriteArrayList<>();
	
	
	/**
	 * 생성자
	 * 
	 * @param host
	 * @param port
	 * @param threadCount
	 */
	public HttpService(String host, int port, int threadCount) throws Exception {
		
		// Http 서버 생성
		this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
		
		// 할당된 호스트명과 포트 번호 획득
		// port 번호가 0일 경우 임의의 포트가 할당됨
		InetSocketAddress addr = this.server.getAddress();
		
		this.hostName = addr.getHostName();
		this.port = addr.getPort();
		
		// 스레드 풀 설정
		if(threadCount > 0) {
			this.server.setExecutor(Executors.newFixedThreadPool(threadCount));
		} else {
			this.server.setExecutor(Executors.newCachedThreadPool());
		}
	}
	
	/**
	 * 컨트롤러 목록 추가
	 * 
	 * @param controller 추가할 컨트롤러
	 * @return 현재 객체
	 */
	public HttpService addController(Object controller) throws Exception {
		
		// 컨트롤러 목록 추가
		this.controllerList.add(new ControllerContext(controller));
		
		return this;
	}
	
	/**
	 * Http 서버 시작
	 * 
	 * @return 현재 객체
	 */
	public HttpService start() {
		
		// Http 서버에 컨트롤러 등록
		for(ControllerContext controller: this.controllerList) {
			this.server.createContext(controller.getBasePath(), controller);
		}
		
		// Http 서버 시작
		this.server.start();
		
		return this;
	}
	
	/**
	 * Http 서버 중지
	 * 
	 * @return 현재 객체
	 */
	public HttpService stop() {
		
		// Http 서버 중지
		this.server.stop(1000);
		
		return this;
	}
	
	/**
	 * Http 서버 호스트명:포트번호 반환
	 * 
	 * @return Http 서버 호스트명:포트번호
	 */
	public String getHostStr() {
		return this.getHostName() + ":" + this.getPort();
	}
}
