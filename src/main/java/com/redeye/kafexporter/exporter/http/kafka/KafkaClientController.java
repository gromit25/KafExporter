package com.redeye.kafexporter.exporter.http.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redeye.kafexporter.acquisitor.kafka.KafkaAcquisitor;
import com.redeye.kafexporter.util.JSONUtil;
import com.redeye.kafexporter.util.http.service.annotation.Controller;
import com.redeye.kafexporter.util.http.service.annotation.RequestHandler;

/**
 * 
 * 
 * @author jmsohn
 */
@Controller(basePath = "/kafka/client")
public class KafkaClientController {

	/**
	 * 
	 * 
	 * @return
	 */
	@RequestHandler
	public String getClientIdList() {
		
		Map<String, Set<String>> clientIdMap = new HashMap<>();
		
		clientIdMap.put("producer", KafkaAcquisitor.getProducerClientIdList());
		clientIdMap.put("consumer", KafkaAcquisitor.getConsumerClientIdList());
		
		return JSONUtil.toJSON(clientIdMap);
	}
	
	/**
	 * 
	 * 
	 * @param exchange
	 * @param pathParamList
	 * @return
	 */
	@RequestHandler(path = "/*/config")
	public String getClientConfig(List<String> pathParamList) {
		
		return JSONUtil.toJSON(
			KafkaAcquisitor.getConfig(
				pathParamList.get(0)	// Client Id
			)
		); 
	}
}
