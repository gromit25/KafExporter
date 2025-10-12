package com.redeye.kafexporter.logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.redeye.appagent.Config;
import com.redeye.appagent.builtins.ContentsApp;
import com.redeye.appagent.util.StringUtil;
import com.redeye.textgen.TextGen;

/**
 * 로깅 클래스<br>
 * log4j 같은 외부라이브러리를 사용하면<br>
 * 측정하려는 어플리케이션에서 사용하는 log4j와 충돌이 일어날 수 있음
 * 
 * @author jmsohn
 */
public class Log {
	
	/** 로그 메시지 큐 - Logger 에게 로그메시지를 전달할 큐 객체 */
	private static BlockingQueue<String> outQ;
	
	/** Logger 목록 - Logger: 실제 로그를 저장 작업 수행 */
	private static List<Logger> loggers;
	
	/** log 생성 포맷 */
	private static TextGen logTemplate;
	
	/** 큐의 최대 로그 메시지 개수 */
	private static int maxLogCount;
	
	/** 스택 트레이스 정보를 남길 package 목록 */
	private static Set<String> tracePackages;
	
	/** 패키지 명 줄임 여부 */
	private static boolean isShortPackage;
	
	static {
		
		// --- 로깅 객체 생성 ---
		
		// 로그 메시지 큐 생성
		outQ = new LinkedBlockingQueue<String>();
		
		// Logger 목록 객체 생성
		loggers = new ArrayList<>();

		// 주어진 개수 만큼 Logger 생성 및 스레드 수행
		int loggerCount = 5;
		
		try {
			
			loggerCount = Integer.parseInt(Config.LOG_WRITER_COUNT.getValue());
			if(loggerCount <= 0) {
				throw new Exception();
			}
			
		} catch(Exception ex) {
			
			System.err.println("AGENT MESSAGE:");
			System.err.println("Invalid Value(LOG_WRITER_COUNT):" + Config.LOG_WRITER_COUNT.getValue());
			System.err.println("set default LOG_WRITER_COUNT = 5");
			
			// default 값 설정
			loggerCount = 5;
		}

		// 로그 writer 생성
		try {
			
			// 로그 writer 를 생성할 빌더 객체 생성
			LogWriterBuilder writerBuilder = new LogWriterBuilder(Config.LOG_WRITER.getValue());
			
			for(int index = 0; index < loggerCount; index++) {
				
				// Logger 객체 생성
				Logger logger = new Logger(outQ, writerBuilder.create());
				
				// Logger 스레드 생성 및 수행
				Thread loggerThread = new Thread(logger);
				loggerThread.setDaemon(true);	// 메인 프로그램 종료시 스레드 종료 설정
				loggerThread.start();
				
				// Logger 목록에 추가
				loggers.add(logger);
			}
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			
			// 실패시 모든 로그 객체 제거
			loggers.clear();
		}
		
		// 로그 큐의 최대 개수 설정
		try {
			
			maxLogCount = Integer.parseInt(Config.LOG_MAX_QUEUE_COUNT.getValue());
			if(maxLogCount <= 0) {
				throw new Exception();
			}
			
		} catch(Exception ex) {
			
			System.err.println("AGENT MESSAGE:");
			System.err.println("Invalid Value(LOG_MAX_QUEUE_COUNT):" + Config.LOG_MAX_QUEUE_COUNT.getValue());
			System.err.println("set default LOG_MAX_QUEUE_COUNT = 1000");
			
			maxLogCount = 1000;
		}
		
		// --- 로그 메시지 생성 관련 설정 --- 
		
		// 로그 템플릿 객체 생성
		try {
			
			logTemplate = TextGen.compile(Config.LOG_TEMPLATE.getValue());
			
		} catch(Exception ex) {
			
			System.err.println("AGENT MESSAGE:");
			System.err.println("Invalid Value(LOG_TEMPLATE):" + Config.LOG_TEMPLATE.getValue());
			
			logTemplate = null;
		}
		
		// 스택 트레이스 정보를 남길 package 목록 초기화
		tracePackages = new HashSet<>();
		
		if(StringUtil.isBlank(Config.LOG_TRACE_PACKAGES.getValue()) == false) {
			String[] tracePackageAry = Config.LOG_TRACE_PACKAGES.getValue().split("[ \\t]*,[ \\t]*");
			
			for(String tracePackage: tracePackageAry) {
				tracePackages.add(tracePackage);
			}
		}
		
		// 패키지 명 줄임 여부 설정
		if(Config.LOG_SHORT_PACKAGE_YN.getValue().equals("Y") == true) {
			isShortPackage = true;
		} else {
			isShortPackage = false;
		}
	}
	
	/**
	 * Agent 자체 로그 저장
	 * 
	 * @param logFormat 로그 형식
	 * @param params 로그 파라미터
	 */
	public static void writeAgentLog(String logFormat, Object... params) {
		
		write(
			"AGENT", "AGENT",
			(long)0,
			logFormat, params
		);
	}
	
	/**
	 * 로그 저장
	 * 
	 * @param apiType 호출 API 종류
	 * @param obj 호출 객체
	 * @param logFormat 로그 형식
	 * @param params 로그 파라미터
	 */
	public static void write(
		String apiType,
		Object obj,
		String logFormat,
		Object... params
	) {
		
		write(
			apiType, obj,
			(long)0,
			logFormat, params
		);
	}

	/**
	 * 로그 저장
	 * 
	 * @param apiType 호출 API 종류
	 * @param obj 호출 객체
	 * @param elapsedTime 호출 시간 - 만일 메소드 호출 전 이라면 0
	 * @param logFormat 로그 형식
	 * @param params 로그 파라미터
	 */
	public static void write(
		String apiType,
		Object obj,
		long elapsedTime,
		String logFormat,
		Object... params
	) {
		
		// 입력값 검사
		if(logFormat == null) return;
		
		// Logger 가 없는 경우 로그를 남기지 않고 반환
		if(loggers == null || loggers.size() == 0) return;

		try {
			
			// 메시지 생성
			String logMsg = genLogMsg(apiType, obj, elapsedTime, logFormat, params);
			
			// 만일 큐가 정해진 숫자 이상이면
			// 큐에 로그를 추가하지 않음
			if(outQ.size() > maxLogCount) {
				return;
			}
			
			// 큐에 메시지 추가
			outQ.put(logMsg);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 로그 메시지 생성 후 반환
	 * 
	 * @param apiType 호출 API 종류
	 * @param obj 호출 객체
	 * @param elapsedTime 호출 시간 - 만일 메소드 호출 전 이라면 0
	 * @param logFormat 로그 형식
	 * @param params 로그 파라미터
	 * @return 생성된 로그 메시지
	 */
	private static String genLogMsg(
		String apiType,
		Object obj,
		long elapsedTime,
		String logFormat,
		Object[] params
	) {
		
		// 현재시간을 가져옴
		long curTime = System.currentTimeMillis();
		
		// 트랜잭션 ID가 없으면 만듦
		if(ContentsApp.getTxId() == null) {
			String newTxId = System.currentTimeMillis() + "_" + Thread.currentThread().getId();
			ContentsApp.setTxId(newTxId);
		}
		
		// 호출API별메시지 조립
		String logMsg = logFormat;
		if(params != null && params.length > 0) {
			logMsg = String.format(logMsg, params);
		}
		
		// 객체식별자를 만듦
		String objId = makeObjId(obj);
		
		// 스택트레이스를 가져옴
		String stackTraceMsg = makeStackTraceMsg(Thread.currentThread());
		
		// 로그 템플릿에서 사용할 데이터 설정
		Map<String, Object> valueMap = new HashMap<>();
		
		valueMap.put("curTime", Long.toString(curTime));
		valueMap.put("elapsedTime", (Long)elapsedTime);
		valueMap.put("pid", Config.SYSTEM_PID.getValue());
		valueMap.put("txId", ContentsApp.getTxId());
		valueMap.put("apiType", apiType);
		valueMap.put("objId", objId);
		valueMap.put("message", logMsg);
		valueMap.put("stackTrace", stackTraceMsg);
		
		// 로그 템플릿을 통해 로그 메시지 생성
		String log = null;
		
		try {
			
			log = logTemplate.gen(valueMap);
			
		} catch(Exception ex) {
			
			// 오류 발생시 표시할 메시지
			log = Config.LOG_TEMPLATE_FAIL_MESSAGE.getValue();
		}
		
		// 로그 종료 문자열([ASCII 코드 RS(Record Separator)] + "\r\n") 추가
		log += "\u001E\r\n";
		
		return log;
	}
	
	/**
	 * 객체식별자 생성 및 반환<br>
	 * 객체식별자 = 객체해시값@클래스명<br>
	 * 클래스명은 패키지명을 포함하지 않음<br>
	 * 만일 객체가 스트링 타입일 경우 그냥 그대로 사용 : STATIC, CREATE 등의 경우<br>
	 * 만일 객체가 null 일 경우 N/A로 표시<br>
	 * 만일 클래스가 Agent의 Class일 경우(ex. Wrapper), Agent 클래스가 아닌 상위 클래스를 표기<br>
	 *    최상위까지 왔는데 클래스가 없는 경우 NONE으로 표시
	 *    
	 * @param obj 객체식별자를 생성할 객체
	 * @return 객체 아이디
	 */
	private static String makeObjId(Object obj) {
		
		String objId = "N/A";
		if(obj != null) {
			
			if(obj instanceof String) {
				objId = obj.toString();
			} else {
				
				String agentPackageName = Config.AGENT_PACKAGE.getValue();
				
				String className = "NONE";
				Class<?> curClass = obj.getClass(); 
				while(curClass != null) {
					
					if(curClass.getName().startsWith(agentPackageName) == false) {
						className = curClass.getSimpleName();
						break;
					}
					curClass = curClass.getSuperclass();
				}
				
				objId = obj.hashCode() + "@" + className;
			}
		}
		
		return objId;
	}
	
	/**
	 * 주어진 스레드의 스택 트레이스 메시지 생성 및 반환
	 * 
	 * @param t 스택 트레이스 메시지를 생성할 스레드
	 * @return 생성된 스택 트레이스 메시지 
	 */
	private static String makeStackTraceMsg(Thread t) {
		
		// 트레이싱 정보를 추가함
		// 트레이싱 패키지가 없을 경우 즉시 공백 문자 반환
		if(tracePackages.size() == 0) return "";
		
		StringBuilder stackBuilder = new StringBuilder("");
		StackTraceElement[] stacks = t.getStackTrace();
		
		// 스택 목록의 스택 정보를 하나씩 추가함
		for(StackTraceElement stack : stacks) {
			
			for(String tracePackage : tracePackages) {

				// stack의 클래스 전체 이름이
				// 설정된 trace package의 이름으로 시작되거나,
				// LOG_TRACE_PACKAGES 설정값이 "*" 이면 트레이싱 정보 추가
				if(stack.getClassName().startsWith(tracePackage) == true
					|| Config.LOG_TRACE_PACKAGES.getValue().equals("*") == true) {
					
					// 이전 스택 정보가 있으면,
					// 꺽쇠(">") 추가
					if(stackBuilder.length() != 0) {
						stackBuilder.append(">");
					}
					
					// 클래스명 획득
					String className = stack.getClassName();
					
					// 패키지명 축약 설정되어 있으면, 클래스명의 패키지명을 축약형으로 만듦
					// ex) com.redeye.Test -> c.r.Test
					if(isShortPackage == true) {
						
						String[] packageNames = className.split("\\.");
						StringBuilder classNameBuilder = new StringBuilder("");
						
						for(int index = 0; index < packageNames.length - 1; index++) {
							
							String packageName = packageNames[index];
							classNameBuilder.append(packageName.charAt(0)).append(".");
						}
						
						classNameBuilder.append(packageNames[packageNames.length - 1]);
						
						className = classNameBuilder.toString();
					}
					
					// 스택 클래스명과 메소드 명, 라인 수 추가
					stackBuilder.append(className)
						.append(".")
						.append(stack.getMethodName())
						.append(":")
						.append(stack.getLineNumber());
					
					break;
				}
			}
		}
		
		return stackBuilder.toString();
	}
	
	/**
	 * 출력 큐가 비어 있는지 여부 반환
	 * 
	 * @return 출력 큐가 비어 있는지 여부
	 */
	public static boolean isEmpty() {
		
		if(outQ == null) {
			return true;
		}
		
		return outQ.isEmpty();
	}
}
