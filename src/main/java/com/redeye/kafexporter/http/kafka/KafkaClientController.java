package com.redeye.kafexporter.http.kafka;

import com.redeye.kafexporter.util.http.service.Controller;
import com.redeye.kafexporter.util.http.service.HttpMethod;
import com.redeye.kafexporter.util.http.service.RequestHandler;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
@Controller(basePath = "/kafka/client")
public class KafkaClientController {

	@RequestHandler(method = HttpMethod.GET)
	protected String execute(HttpExchange exchange) {
		return null;
	}
}
