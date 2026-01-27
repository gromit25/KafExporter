package com.redeye.kafexporter.util;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

import com.redeye.kafexporter.util.jmx.JMXService;

/**
 * 
 * 
 * @author jmsohn
 */
public class KafkaUtil {
	
	
	/** JMX를 통해 카프카 프로듀서 아이디 목록을 질의하는 쿼리 */
	private static final String PRODUCER_CLIENT_ID_QUERY = "kafka.producer:client-id=*,type=producer-metrics";
	
	
	/**
	 * JMX를 통해 프로듀서 클라이언트 아이디 목록 반환
	 * 
	 * @param svc JMX 서비스 객체
	 * @return 프로듀서 클라이언트 아이디 목록
	 */
	public static List<String> getProducerClientIdList(JMXService svc) throws Exception {
		
		if(svc == null) {
			throw new IllegalArgumentException("svc(JMXService) is null.");
		}
		
		if(svc.isClosed() == true) {
			throw new IllegalArgumentException("svc(JMXService) is closed.");
		}
		
		// 클라이언트 아이디 목록 생성 후 반환
		List<String> clientIdList = new ArrayList<>();
		
		svc
			.getByQuery(PRODUCER_CLIENT_ID_QUERY, "client-id")
			.forEach((key, clientId) -> {
				clientIdList.add(clientId.toString());
			});
		
		return clientIdList;
	}
	
	/**
	 * 카프카 브로커 클래스 로딩 여부 검사<br>
	 * 현재 어플리케이션이 브로커인지 클라이언트인지 구분하기 위함
	 * 
	 * @param inst Java 인스트루먼트 객체
	 * @return 카프카 브로커 클래스 로딩 여부
	 */
	public static boolean isKafkaServerClassLoaded(Instrumentation inst) {
		
		// 입력값 검증
		if(inst == null) {
			throw new IllegalArgumentException("'inst' is null.");
		}
		
		// 로딩된 클래스 중에 카프카 브로커 클래스가 있는 지 여부 확인하여 반환
		for(Class<?> clazz : inst.getAllLoadedClasses()) {
			if("kafka.server.KafkaServer".equals(clazz.getName())) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * 컨슈머 그룹 목록 반환
	 * 
	 * @param adminClient Kafka 클라이언트 객체
	 * @return 컨슈머 그룹 목록
	 */
	public static List<String> getConsumerGroupList(AdminClient adminClient) throws Exception {

		// 컨슈머 그룹 목록 획득
		Collection<GroupListing> consumerGroupList =
			adminClient.listGroups().all().get();

		// 컨슈머 그룹 아이이디 목록 생성
		List<String> groupIdList = new ArrayList<>();
		
		for(GroupListing consumerGroup : consumerGroupList) {
			groupIdList.add(consumerGroup.groupId());
		}

		return groupIdList;
    }

	/**
	 * 컨슈머 래그 맵 반환<br>
	 * Key: 컨슈머 그룹 아이디, Value: 래그 값
	 * 
	 * @param adminClient Kafka 클라이언트 객체
	 * @return 컨슈머 래그 맵
	 */
	public static Map<String, Long> getConsumerLag(AdminClient adminClient) throws Exception {
		
		// 컨슈머 래그 맵 생성
		Map<String, Long> consumerLagMap = new HashMap<>();
		
		for(String consumerGroupId: getConsumerGroupList(adminClient)) {
			consumerLagMap.put(consumerGroupId, getConsumerLag(adminClient, consumerGroupId));
		}
		
		return consumerLagMap;
	}
	
	/**
	 * 컨슈머 그룹의 lag 계산 및 반환
	 *
	 * @param adminClient Kafka 클라이언트 객체
	 * @param groupId 그룹 아이디
	 * @return 
	 */
	public static long getConsumerLag(AdminClient adminClient, String groupId) throws Exception {

		// 1. 커밋된 오프셋 획득
		Map<TopicPartition, OffsetAndMetadata> committedOffsets =
			adminClient
				.listConsumerGroupOffsets(groupId)
				.partitionsToOffsetAndMetadata()
				.get();

		if(committedOffsets.isEmpty() == true) {
			return 0L;
		}

		// 2. request latest offsets
		Map<TopicPartition, OffsetSpec> offsetSpecs = new HashMap<>();
		for(TopicPartition tp : committedOffsets.keySet()) {
			offsetSpecs.put(tp, OffsetSpec.latest());
		}

		Map<TopicPartition, ListOffsetsResultInfo> endOffsets =
			adminClient
				.listOffsets(offsetSpecs)
				.all()
				.get();

		// 3. 컨슈머 래그 계산
		long lag = 0L;

		for(Map.Entry<TopicPartition, OffsetAndMetadata> entry : committedOffsets.entrySet()) {
			
			TopicPartition tp = entry.getKey();
			OffsetAndMetadata committed = entry.getValue();

			ListOffsetsResultInfo endOffsetInfo = endOffsets.get(tp);
			if(endOffsetInfo == null) {
				continue;
			}

			lag += Math.max(endOffsetInfo.offset() - committed.offset(), 0);
		}

		return lag;
	}
}
