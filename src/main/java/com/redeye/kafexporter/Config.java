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
	),
	
	/** 성능 정보를 수집 크론잡의 스케쥴 */
	METRICS_ACQUISITOR_SCHEDULE(
		"AGENT_METRICS_ACQUISITOR_SCHEDULE",
		"*/5 * * * * *",
		"성능 정보 수집 주기 설정, 디폴트: 5초 간격(*/5 * * * * *)"
	),
	
	//--- 로그 관련
	
	/** 로그 Writer */
	LOG_WRITER(
		"AGENT_LOG_WRITER",
		"com.redeye.appagent.logger.file.FileWriter",
		"로그 Writer(전체 클래스 명)"
	),
	
	/** 출력 로그 템플릿 */
	LOG_TEMPLATE(
		"AGENT_LOG_TEMPLATE",
		"${curTime}"
		+ "\t${pid}"
		+ "\t${type}"
		+ "\t${objId}"
		+ "\t${stackTrace}"
		+ "\t${message}",
		"출력 로그 템플릿"
	),
	
	/** 출력 로그 생성 실패시 로그 메시지 */
	LOG_TEMPLATE_FAIL_MESSAGE(
		"AGENT_LOG_TEMPLATE_FAIL_MESSAGE",
		"can't generate log message.",
		"Template 로그 생성 실패시 로그 메시지"
	),
	
	/** 로그 Writer(Logger 1개당 로그 Writer 1개임) 의 개수 */
	LOG_WRITER_COUNT(
		"AGENT_LOG_WRITER_COUNT",
		"5",
		"로그 Writer(Logger 1개당 로그 Writer 1개임) 의 개수"
	),
	
	/** 로그 큐의 최대 개수 - 주의) 최대 개수 이상이 큐에 있을 경우 로그가 쌓이지 않음 */
	LOG_MAX_QUEUE_COUNT(
		"AGENT_LOG_MAX_QUEUE_COUNT",
		"1000",
		"로그 큐의 최대 개수 - 주의) 최대 개수 이상이 큐에 있을 경우 로그가 쌓이지 않음"
	),
	
	/** 스택 트레이스 정보를 남길 package 목록 */
	LOG_TRACE_PACKAGES(
		"AGENT_LOG_TRACE_PACKAGES",
		"",
		"스택 트레이스 정보를 남길 package 목록"
	),
	
	/** 패키지 명 줄임 여부 */
	LOG_SHORT_PACKAGE_YN(
		"AGENT_LOG_SHORT_PACKAGE",
		"N",
		"패키지 명 줄임 여부"
	),
	
	//--- 로그 타입: 파일 관련 설정
	
	/** 로그 파일 명 */
	LOG_FILE_PATH(
		"AGENT_LOG_FILE_PATH",
		"./kafka.log",
		"로그 파일 명"
	),
	
	/** 로그 파일 관리자의 수행 주기(단위: 초) */
	LOG_FILE_MGR_PERIOD(
		"AGENT_LOG_FILE_MGR_PERIOD",
		"10",
		"로그 파일 관리자의 수행 주기(단위: 초)"
	),
	
	/**
	 * 로그 파일의 최대치(단위: MiB)<br>
	 * 최대치가 넘을 경우 현재 로그 파일은 백업함
	 */
	LOG_FILE_MGR_MAXSIZE(
		"AGENT_LOG_FILE_MGR_MAX_SIZE",
		"1024",
		"로그 파일의 최대치(단위: MiB)"
	),
	
	/** 유지할 백업 로그 파일의 개수 */
	LOG_FILE_MGR_BACKUP_COUNT(
		"AGENT_LOG_FILE_MGR_BACKUP_COUNT",
		"2",
		"유지할 백업 로그 파일의 개수"
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
