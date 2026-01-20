package com.redeye.kafexporter.http.kafka;

import com.redeye.kafexporter.acquisitor.KafkaAcquisitor;
import com.redeye.kafexporter.util.JSONUtil;
import com.redeye.kafexporter.util.http.service.HttpMethod;
import com.redeye.kafexporter.util.http.service.annotation.Controller;
import com.redeye.kafexporter.util.http.service.annotation.RequestHandler;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
@Controller(basePath = "/kafka/metrics")
public class KafkaMetricsController {
	
	@RequestHandler(method = HttpMethod.GET)
	public static String getMetrics(HttpExchange exchange) throws Exception {
		return JSONUtil.toJSON(KafkaAcquisitor.acquireSystemMetrics());
	}
}
