package com.redeye.kafexporter.http;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
public abstract class AbstractJSONHandler implements HttpHandler {

	/**
	 * 
	 * 
	 * @param exchange
	 * @return
	 */
	protected abstract String execute(HttpExchange exchange);

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		//
		String response = this.execute(exchange);
		
		// 응답 헤더 설정 (상태 코드 200, 본문 길이)
        exchange.sendResponseHeaders(200, response.length());
        
        // 응답 본문 전송
        try(OutputStream os = exchange.getResponseBody()) {
        	os.write(response.getBytes());
        }
	}
}
