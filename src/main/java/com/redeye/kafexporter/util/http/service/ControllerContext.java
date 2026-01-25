package com.redeye.kafexporter.util.http.service;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.redeye.kafexporter.util.http.service.annotation.Controller;
import com.redeye.kafexporter.util.http.service.annotation.RequestHandler;
import com.redeye.kafexporter.util.http.service.model.HandlerDTO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import lombok.Getter;

/**
 * 컨트롤러 컨텍스트 클래스
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
class ControllerContext implements HttpHandler {
	
	
	/** 컨트롤러 객체 */
	private final Object controller;

	/** 컨트롤러 기본 패스 */
	@Getter
	private final String basePath;
	
	/** 요청 핸들러 목록 */
	private final List<HandlerDTO> handlerList = new CopyOnWriteArrayList<>();

	
	/**
	 * 생성자
	 * 
	 * @param controller 컨트롤러 객체
	 */
	public ControllerContext(Object controller) throws Exception {
		
		// 입력 값 검증
		if(controller == null) {
			throw new IllegalArgumentException("'controller' is null.");
		}
		
		// 컨트롤러 여부 확인 및 설정
		Controller controllerAnnotation = controller.getClass().getAnnotation(Controller.class);
		if(controllerAnnotation == null) {
			throw new IllegalArgumentException("Controller Annotation not found: " + controller.getClass());
		}
		
		this.controller = controller;

		// 컨트롤러 어노테이션 내용 초기화
		this.basePath = controllerAnnotation.basePath();
		
		// 컨트롤러의 각 핸들러 메소드 초기화
		for(Method method: this.controller.getClass().getDeclaredMethods()) {
			
			// 요청 핸들러 여부 확인 및 설정
			RequestHandler handlerAnnotation = method.getAnnotation(RequestHandler.class);
			if(handlerAnnotation == null) {
				continue;
			}
			
			// Mapper에 설정된 path 변환
			String path = handlerAnnotation.path().trim(); 
			if(path.length() > 0 && path.charAt(0) != '/') {
				path = "/" + path;
			}
			
			// 요청 핸들러 등록
			this.handlerList.add(
				new HandlerDTO(
					this.basePath + path,
					handlerAnnotation.method(),
					method
				)
			);
		}
	}
	
	@Override
	public final void handle(HttpExchange exchange) throws IOException {
		
		try {
			
			// 매치되는 핸들러 획득
			HandlerDTO handler = this.getMatchedHandler(exchange);
			
			// 핸들러 메소드 호출
			String response = null;
			int code = 200;
			
			try {
				
				response = handler.invoke(controller, exchange);
				
			} catch(Exception ex) {
				
				code = 500;
				response = "Internal Server Error.";
				
				ex.printStackTrace();
			}
			
			// 응답 헤더 설정 (상태 코드, 본문 길이)
			exchange.sendResponseHeaders(code, response.length());
			
			// 응답 본문 전송
			try(OutputStream os = exchange.getResponseBody()) {
				os.write(response.getBytes());
			}
			
		} catch(NotFoundException ex) {
			
			// 매치되는 핸들러가 없을 경우 처리
			
			// 응답 헤더 설정 (상태 코드 404, 본문 길이)
			exchange.sendResponseHeaders(404, ex.getMessage().length());

			// 응답 본문 전송
			try(OutputStream os = exchange.getResponseBody()) {
				os.write(ex.getMessage().getBytes());
			}
			
		} catch(IOException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new IOException(ex);
		}
	}
	
	/**
	 * Http 요청에 매치되는 요청 처리 핸들러 반환
	 * 
	 * @param exchange Http 요청
	 * @return 요청 처리 핸들러
	 * @throws NotFoundException 매치되는 핸들러가 없을 경우 발생
	 */
	private HandlerDTO getMatchedHandler(HttpExchange exchange) throws NotFoundException {
		
		for(HandlerDTO handler: handlerList) {
			
			// 핸들러 매치가 되지 않을 경우 다음 핸들러 반환
			if(handler.isMatched(exchange) == true) {
				return handler;
			}
		}
		
		throw new NotFoundException(exchange.getRequestURI().getPath() + " is not found.");
	}
	
	/**
	 * 요청 처리 핸들러를 찾지 못했을 경우 발생하는 예외 클래스
	 * 
	 * @author jmsohn
	 */
	public static class NotFoundException extends Exception {

		/** 클래스 시리얼 번호 */
		private static final long serialVersionUID = -3843908037266338263L;
		
		/**
		 * 생성자
		 * 
		 * @param message 예외 메시지
		 */
		public NotFoundException(String message) {
			super(message);
		}
	}
}
