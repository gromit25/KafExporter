package com.redeye.kafexporter.logger;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Vector;

import com.redeye.kafexporter.util.StringUtil;

import lombok.Getter;

/**
 * 로그 writer 빌더
 * 
 * @author jmsohn
 */
class LogWriterBuilder {
	
	/** 생성할 로그 writer 타입명 */
	@Getter
	private String types;
	
	/** 로그 writer 생성자 목록 */
	private List<Constructor<?>> writerConstructors;
	
	/**
	 * 생성자
	 * 
	 * @param types 로그 Writer 클래스명 들(, 로 분리)
	 */
	LogWriterBuilder(String types) throws Exception {
		
		this.types = types;
		this.writerConstructors = new Vector<>();
		
		// 로그 Writer 클래스명이 비어 있을 경우 종료
		if(StringUtil.isEmpty(types) == true) {
			return;
		}
		
		// 로그 Writer type 생성
		for(String typeStr: types.split("[ \\t]*\\,[ \\t]*")) {
			
			// LogWriter class 로딩
			Class<?> type = Class.forName(typeStr);
			
			// LogWriter type 검사
			if(type == null) {
				throw new Exception("can't load log class:" + typeStr);
			}
			
			if(LogWriter.class.isAssignableFrom(type) == false) {
				throw new Exception("log class is not LogWriter type:" + typeStr);
			}
			
			// 디폴트 생성자 획득
			Constructor<?> writerConstructor = type.getDeclaredConstructor();
			if(writerConstructor == null) {
				throw new Exception("log class's default constructor is not found:" + typeStr);
			}
			
			// LogWriter 생성자 추가
			this.writerConstructors.add(writerConstructor);
		}
	}
	
	/**
	 * 로그 writer 타입 명에 따라 로그 writer 생성 및 반환
	 * 
	 * @return 생성된 로그 writer 목록
	 */
	List<LogWriter> create() throws Exception {
		
		// 로그 writer 목록 객체 생성 
		List<LogWriter> writers = new Vector<>();
		
		// 설정된 로그 writer type 이 없으면, 빈 목록 반환
		if(this.writerConstructors == null) {
			return writers;
		}
		
		// 설정된 로그 writer type 별로 생성하여 반환
		for(Constructor<?> writerConstructor: this.writerConstructors) {
			
			// 로그 writer 생성
			Object obj = writerConstructor.newInstance();
			
			// 로그 writer 추가, LogWriter 클래스일 경우에만
			if(obj instanceof LogWriter) {
				writers.add((LogWriter)obj);
			}
		}
		
		// 로그 writer 목록 반환
		return writers;
	}
}
