package com.redeye.kafexporter.logger.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.redeye.kafexporter.Config;
import com.redeye.kafexporter.logger.logger.LogWriter;

/**
 * 파일 로그 Writer
 * 
 * @author jmsohn
 */
public class FileWriter implements LogWriter {
	
	/** 로그 파일 */
	private Path logFile;
	
	/**
	 * 생성자
	 */
	public FileWriter() {
		
		// 로그 파일 객체 생성
		this.logFile = Paths.get(Config.LOG_FILE_PATH.getValue());
	}

	@Override
	public void write(String msg) throws Exception {
		
		// 메시지를 로그 파일에 write
		Files.write(
			this.logFile,
			msg.getBytes(),
			StandardOpenOption.CREATE, StandardOpenOption.APPEND
		);
	}
}
