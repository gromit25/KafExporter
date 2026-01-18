package com.redeye.kafexporter.util.http.service;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.redeye.kafexporter.util.http.service.annotation.Controller;
import com.redeye.kafexporter.util.http.service.annotation.RequestHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import lombok.Getter;

/**
 * 
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
class ControllerContext implements HttpHandler {
	
	
	/** */
	private final Object controller;

	/** */
	@Getter
	private final String basePath;
	
	/** */
	private final List<HandlerDTO> handlerList = new CopyOnWriteArrayList<>();

	
	/**
	 * 생성자
	 */
	public ControllerContext(Object controller) throws Exception {
		
		// 입력 값 검증
		if(controller == null) {
			throw new IllegalArgumentException("'controller' is null.");
		}
		
		Controller controllerAnnotation = controller.getClass().getAnnotation(Controller.class);
		if(controllerAnnotation == null) {
			throw new IllegalArgumentException("Controller Annotation not found: " + controller.getClass());
		}
		
		this.controller = controller;

		// 컨트롤러 어노테이션 내용 초기화
		this.basePath = controllerAnnotation.basePath();
		
		// 컨트롤러의 각 핸들러 메소드 초기화
		for(Method method: this.controller.getClass().getDeclaredMethods()) {
			
			RequestHandler handlerAnnotation = method.getAnnotation(RequestHandler.class);
			if(handlerAnnotation == null) {
				continue;
			}
			
			// Mapper에 설정된 path 변환
			String path = handlerAnnotation.path().trim(); 
			if(path.length() > 0 && path.charAt(0) != '/') {
				path = "/" + path;
			}
			
			// 핸들러 등록
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
			String response = handler.invoke(controller, exchange);
			
			// 응답 헤더 설정 (상태 코드 200, 본문 길이)
			exchange.sendResponseHeaders(200, response.length());
			
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
	 * 
	 * 
	 * @param exchange
	 * @return
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
	 * 
	 * 
	 * @author jmsohn
	 */
	public static class NotFoundException extends Exception {

		/** */
		private static final long serialVersionUID = -3843908037266338263L;
		
		/**
		 * 
		 * 
		 * @param message
		 */
		public NotFoundException(String message) {
			super(message);
		}
	}
}
