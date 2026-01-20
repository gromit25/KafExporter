package com.redeye.kafexporter.http.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redeye.kafexporter.acquisitor.kafka.KafkaAcquisitor;
import com.redeye.kafexporter.util.JSONUtil;
import com.redeye.kafexporter.util.http.service.annotation.Controller;
import com.redeye.kafexporter.util.http.service.annotation.RequestHandler;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * 
 * @author jmsohn
 */
@SuppressWarnings("restriction")
@Controller(basePath = "/kafka/client")
public class KafkaClientController {

	/**
	 * 
	 * 
	 * @param exchange
	 * @return
	 */
	@RequestHandler
	public String getClientIdList(HttpExchange exchange) {
		
		Map<String, Set<String>> clientIdMap = new HashMap<>();
		
		clientIdMap.put("producer", KafkaAcquisitor.getProducerClientIdList());
		clientIdMap.put("consumer", KafkaAcquisitor.getConsumerClientIdList());
		
		return JSONUtil.toJSON(clientIdMap);
	}
	
	/**
	 * 
	 * 
	 * @param exchange
	 * @param params
	 * @return
	 */
	@RequestHandler(path = "/*/config")
	public String getClientConfig(HttpExchange exchange, List<String> params) {
		
		return JSONUtil.toJSON(
			KafkaAcquisitor.getConfig(
				params.get(0)	// Client Id
			)
		); 
	}
}
