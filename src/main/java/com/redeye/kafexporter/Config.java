package com.redeye.kafexporter;

import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;

import lombok.Getter;

/**
 * AppAgent 설정 값<br>
 * 환경 변수에서 읽어와 설정함
 * 
 * @author jmsohn
 */
public enum Config {
	
	/** agent 의 패키지명 */
	AGENT_PACKAGE(
		"AGENT_PACKAGE",
		"com/redeye/appagent",
		"agent의 패키지명",
		false
	),

	/** system 구분자(id) */
	SYSTEM_ID(
		"AGENT_SYSTEM_ID",
		"N/A",
		"system 구분자"
	),

	/** system server name: 시스템에 설정되어 있는 값 */
	SYSTEM_NAME(
		"AGENT_SYSTEM_NAME",
		"N/A",
		"system server name",
		false
	),
	
	/** system process id */
	SYSTEM_PID(
		"AGENT_SYSTEM_PID",
		"N/A",
		"system process id",
		false
	),
	
	/** CHARACTER SET */
	SYSTEM_CHARSET(
		"AGENT_SYSTEM_CHARSET",
		Charset.defaultCharset().name(),
		"character set"
	);
	
	//---------------------------
	
	/**
	 * 설정 초기화 수행
	 */
	public static void init() throws Exception {
		
		// PID와 현재 서버명 설정
		// runtimeName = PID@현재서버명 형태로 되어 있음
		String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
		
		if(runtimeName != null) {
			
			String[] splitedRuntimeName = runtimeName.split("@");
			
			if(splitedRuntimeName.length > 1) {
				
				Config.SYSTEM_NAME.value = splitedRuntimeName[1];
				Config.SYSTEM_PID.value = splitedRuntimeName[0];
			}
		}
		
		// 환경 변수에서 설정 값을 읽어와 값을 설정
		Config[] configs = Config.values();
		
		for(Config config: configs) {
			
			// 환경 변수로 설정 불가인 변수인 경우 스킵함
			if(config.isConfigurable() == false) {
				continue;
			}
			
			// 환경 변수의 값을 가져옴
			String value = System.getenv(config.key);
			
			// 설정된 값이 있을 경우에만 설정
			if(value != null) {
				config.value = value;
			}
		}
	}
	
	/**
	 * 설정 가능한 환경변수 목록 문자열 반환
	 * 
	 * @return 설정 가능한 환경변수 목록 문자열
	 */
	public String showConfigurableEnv() {
		
		// 설정 가능한 환경변수 목록 문자열 생성 변수
		StringBuilder builder = new StringBuilder("");
		
		// 환경 변수 별로 문자열 생성
		Config[] configs = Config.values();
		
		for(Config config: configs) {
			
			// 환경 변수로 설정 불가인 변수인 경우 스킵함
			if(config.isConfigurable() == false) {
				continue;
			}
			
			// 환경 변수의 문자열 추가
			builder.append(config).append("\n");
		}
		
		return builder.toString();
	}
	
	//---------------------------
	
	/** 환경 변수 키 */
	protected String key;
	
	/** 환경 변수 값 */
	@Getter
	protected String value;
	
	/** 환경 변수 디폴트 값 */
	@Getter
	protected String defaultValue;
	
	/** 환경 변수 설명 */
	@Getter
	protected String desc;
	
	/** 환경 변수 설정 가능 여부 */
	@Getter
	protected boolean configurable;

	/**
	 * 생성자
	 * 
	 * @param key 환경 변수 키
	 * @param value 환경 변수 디폴트 값
	 * @param desc 환경 변수 설명
	 * @param configurable 환경 변수 설정 가능 여부
	 */
	private Config(String key, String defaultValue, String desc, boolean configurable) {
		
		this.key = key;
		this.value = defaultValue;
		this.defaultValue = defaultValue;
		this.desc = desc;
		this.configurable = configurable;
	}

	/**
	 * 생성자
	 * 
	 * @param key 환경 변수 키
	 * @param value 환경 변수 디폴트 값
	 * @param desc 환경 변수 설명
	 */
	private Config(String key, String defaultValue, String desc) {
		this(key, defaultValue, desc, true);
	}
	
	@Override
	public String toString() {
		return this.key + "(" + this.defaultValue + "):" + this.desc;
	}
}
