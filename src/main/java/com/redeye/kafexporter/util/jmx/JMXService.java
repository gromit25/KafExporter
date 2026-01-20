package com.redeye.kafexporter.util.jmx;

import java.io.Closeable;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.redeye.kafexporter.util.StringUtil;

import lombok.Getter;

/**
 * JMX 서비스 객체
 * 
 * @author jmsohn
 */
public class JMXService implements Closeable {

	
	/**
	 * JMX 연결 타입 enum
	 */
	private enum JMXConnectionType {
		INTERNAL,
		EXTERNAL
	}
	
	
	/** JMX 연결 타입 */
	private JMXConnectionType connType;
	
	/** JMX 연결 */
	private JMXConnector jmxConnector;
	
	/** 연결 종료 여부 */
	@Getter
	private volatile boolean closed;
	

	/**
	 * 생성자 - 외부 JMX 서버 연결용
	 * 
	 * @param host JMX 호스트 정보
	 * @param port JMX 포트 번호
	 * @param username 접속용 사용자 명
	 * @param password 접속용 패스워드
	 */
	public JMXService(String host, int port, String username, String password) throws Exception {
		
		// jmx 타입 설정
		this.connType = JMXConnectionType.EXTERNAL;
		
		// jmx url 생성
		String url = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", host, port);
		JMXServiceURL jmxUrl = new JMXServiceURL(url);
        
		// 접속용 환경 변수 설정
		Map<String, Object> env = new HashMap<>();
        
		// 아이디/패스워드가 있을 경우 환경 변수에 설정
		if(StringUtil.isBlank(username) == false && StringUtil.isBlank(password) == false) {
			String[] credentials = new String[] {username, password};
			env.put(JMXConnector.CREDENTIALS, credentials);
		}

		// Connector 생성
		this.jmxConnector = JMXConnectorFactory.connect(jmxUrl, env);
		this.closed = false;
	}
	
	/**
	 * 생성자 - 외부 JMX 서버 연결용
	 * 
	 * @param host JMX 호스트 정보
	 * @param port JMX 포트 번호
	 */
	public JMXService(String host, int port) throws Exception {
		this(host, port, null, null);
	}
	
	/**
	 * 생성자 - 내부 VM JXM 연결용
	 */
	public JMXService() {
		this.connType = JMXConnectionType.INTERNAL;
		this.closed = false;
	}
	
	/**
	 * MBean Server Connection 객체 반환<br>
	 * 내부/외부 접속에 따라 반환함
	 * 
	 * @return MBean Server Connection 객체
	 */
	private MBeanServerConnection getMBeanConnection() throws Exception {
		
		if(this.connType == JMXConnectionType.INTERNAL) {
			
			return ManagementFactory.getPlatformMBeanServer();
			
		} else if(this.connType == JMXConnectionType.EXTERNAL){
			
			if(this.jmxConnector != null) {
				return this.jmxConnector.getMBeanServerConnection();
			} else {
				throw new Exception("'jmxConnector' is null.");
			}
			
		} else {
			throw new Exception("'connType' is invalid.");
		}
	}

	/**
	 * 주어진 ObjectName Pattern과 일치하는 Object Name 목록을 반환
	 * 
	 * @param namePattern 검색할 ObjectName의 패턴
	 * @return Object Name 목록
	 */
	public List<String> findObjectName(String namePatternStr) throws Exception {
		
		// 입력 값 검증
		if(StringUtil.isBlank(namePatternStr) == true) {
			throw new Exception("'pattern' is null or blank.");
		}
		
		// 검색 수행
        ObjectName query = new ObjectName(namePatternStr);
        Set<ObjectName> mbeans = this.getMBeanConnection().queryNames(query, null);
        
        // Object Name 목록 생성 및 반환
        List<String> nameList = new ArrayList<String>();
        for(ObjectName mbean : mbeans) {
        	nameList.add(mbean.getCanonicalName());
        }
        
        return nameList;
	}

	/**
	 * JMX 객체의 속성 값 반환 
	 * 
	 * @param objectNameStr 객체 명
	 * @param attrNameStr 속성 명
	 * @return 속성 값
	 */
	public Object get(String objectNameStr, String attrNameStr) throws Exception {

		// 입력 값 검증
		if(StringUtil.isBlank(objectNameStr) == true) {
			throw new IllegalArgumentException("'objectNameStr' is null or blank.");
		}
		
		if(StringUtil.isBlank(attrNameStr) == true) {
			throw new IllegalArgumentException("'attrNameStr' is null or blank.");
		}

		// ObjectName 설정
		ObjectName objectName = new ObjectName(objectNameStr);

		// JMX 값 획득 및 반환
		return this.getMBeanConnection().getAttribute(objectName, attrNameStr);
	}
	
	/**
	 * JMX 객체의 속성 값 반환 
	 * 
	 * @param <T> 속성 값 타입
	 * @param objectNameStr 객체 명
	 * @param attrNameStr 속성 명
	 * @param returnType 속성 값 반환 타입
	 * @return 속성 값
	 */
	public <T> T get(String objectNameStr, String attrNameStr, Class<T> returnType) throws Exception {

		// 입력 값 검증
		if(returnType == null) {
			throw new IllegalArgumentException("'returnType' is null.");
		}

		// JMX 값 획득 및 반환
		Object value = this.get(objectNameStr, attrNameStr);
		return returnType.cast(value);
	}

	/**
	 * 주어진 ObjectName Pattern과 일치하는 Object Name의 속성 값 맵을 반환
	 * 
	 * @param query 검색할 ObjectName 쿼리
	 * @param attrName 속성 명 목록 
	 * @return 속성 값 맵 반환(K: Object Name, V: 속성명-값 맵)
	 */
	public Map<String, Map<String, Object>> getByQuery(String query, String... attrNameAry) throws Exception {
		
		// 입력 값 검증
		if(StringUtil.isBlank(query) == true) {
			throw new IllegalArgumentException("'query' is null or blank.");
		}
		
		if(attrNameAry == null || attrNameAry.length == 0) {
			throw new IllegalArgumentException("'attrNameAry' is null or length is 0.");
		}
		
		// ObjectName 맵 변수
		Map<String, Map<String, Object>> nameAttrMap = new HashMap<>();
		
		List<String> nameList = this.findObjectName(query);
		for(String name: nameList) {
			
			// 속성 명 - 값 맵 객체 생성
			Map<String, Object> attrMap = new HashMap<>();
			for(String attrName: attrNameAry) {
				attrMap.put(attrName, this.get(name, attrName));
			}
			
			// ObjectName 맵에 추가
			nameAttrMap.put(name, attrMap);
		}
		
		return nameAttrMap;
	}

	@Override
	public synchronized void close() throws IOException {
		
		// 이미 닫힌 경우는 반환
		if(this.closed == true) {
			return;
		}
		
		if(this.jmxConnector == null) {
			return;
		}

		// 외부 서버 연결이 아니면 아무것도 하지 않고 반환
		if(this.connType == JMXConnectionType.INTERNAL) {
			return;
		}
		
		this.jmxConnector.close();
		this.closed = true;
	}
}
