package com.redeye.kafexporter.http;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
public class KafkaHttpHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		System.out.println("DEBUG in KafkaHttpHandler 100: " + exchange.getRequestURI().getPath());
		System.out.println("DEBUG in KafkaHttpHandler 200: " + exchange.getRequestURI().getQuery());
	}
}
