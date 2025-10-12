package com.redeye.kafexporter.logger.file;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.redeye.kafexporter.Config;

import lombok.Getter;
import lombok.Setter;

/**
 * 로그 파일 관리자
 * 
 * @author jmsohn
 */
class FileManager extends Thread {
	
	/** 로그 파일 관리자 */
	private static FileManager logFileManager;
	
	/**
	 * 로그 파일 관리자의 수행 주기(단위: 밀리초)<br>
	 * 설정(LOG_FILE_MGR_PERIOD)은 초단위 이지만 여기에서는 밀리초 단위로 관리
	 */
	private long period; 
	
	/**
	 * 로그 파일 최대 크기(단위: 바이트)<br>
	 * 설정(LOG_FILE_MGR_MAX_SIZE)은 MiB 단위 이지만 여기에서는 바이트 단위로 관리
	 */
	private long maxSize;
	
	/**
	 * 로그 파일 최대 개수<br>
	 * 로그 파일 최대 개수를 넘을 경우 오래된 파일 삭제 처리
	 */
	private int fileCount;
	
	/** 로그 파일 검사 중단 여부 */
	@Getter
	@Setter
	private volatile boolean isStop;
	
	/** 현재 로그 파일 */
	private File logFile;
	
	//--------------------------------------
	
	/**
	 * 로그 파일 관리자 반환(싱글톤)
	 * 
	 * @return 로그 파일 관리자
	 */
	static FileManager getLogFileManager() throws Exception {

		// 로그 파일 관리자가 없을 경우 생성
		if(FileManager.logFileManager == null) {
			FileManager.logFileManager = new FileManager();
		}
		
		return FileManager.logFileManager;
	}
	
	/**
	 * 생성자<br>
	 * 외부에서 생성하지 못하도록 함
	 */
	private FileManager() throws Exception {
		
		// 로그 파일
		this.logFile = new File(Config.LOG_FILE_PATH.getValue());
		
		// 로그 파일 관리자의 수행 주기 설정
		this.period = Long.parseLong(Config.LOG_FILE_MGR_PERIOD.getValue()) * 1000;
		if(this.period < 1) {
			throw new Exception("LOG_FILE_MGR_PERIOD must be greater than 0:" + Config.LOG_FILE_MGR_PERIOD.getValue());
		}
		
		// 로그 파일 최대 개수 설정
		this.maxSize = Long.parseLong(Config.LOG_FILE_MGR_MAXSIZE.getValue()) * 1024 * 1024;
		if(this.maxSize < 1) {
			throw new Exception("LOG_FILE_MGR_MAX must be greater than 0:" + Config.LOG_FILE_MGR_MAXSIZE.getValue());
		}
		
		// 로그 파일 최대 개수 설정
		this.fileCount = Integer.parseInt(Config.LOG_FILE_MGR_BACKUP_COUNT.getValue());
		if(this.fileCount < 1) {
			throw new Exception("LOG_FILE_MGR_BACKUP_COUNT must be greater than 0:" + Config.LOG_FILE_MGR_BACKUP_COUNT.getValue());
		}
		
		// main 프로그램 종료시, 같이 종료되도록 Daemon 으로 설정
		this.setDaemon(true);
	}
	
	@Override
	public void run() {
		
		try {
			
			this.setStop(false);
			
			while(this.isStop() == false) {
				
				// 로그 파일 백업
				this.backupLog();
				
				// 백업된 로그 파일 정리
				this.removeOldLogFiles();
				
				// 설정된 주기 만큼 대기
				Thread.sleep(this.period);
			}
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * 현재 로그 파일 백업<br>
	 * ex) agent.log -> agent.log.20241208.183000
	 */
	private void backupLog() throws Exception {
		
		// ---- 파일 백업 실행 여부 확인
		
		// 로그 파일이 존재하지 않으면, 백업할 것이 없으므로 즉시 반환
		if(this.logFile == null || this.logFile.exists() == false) {
			return;
		}
		
		// 로그 파일 크기 획득
		long logSize = this.logFile.length();

		// 로그 파일 사이즈가 최대 크기를 넘지 않으면, 백업하지 않고 즉시 반환
		if(logSize < this.maxSize) {
			return;
		}
		
		// ---- 파일 백업 실행
		
		// 백업 파일명 생성
		StringBuilder logBackupFileName = new StringBuilder(
			Config.LOG_FILE_PATH.getValue()
		);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
		logBackupFileName.append(".").append(dateFormat.format(new Date()));

		// 파일 백업 수행
		this.logFile.renameTo(new File(logBackupFileName.toString()));
	}
	
	/**
	 * 오래된 로그 파일 삭제
	 */
	private void removeOldLogFiles() throws Exception {
		
		// 로그 파일이 설정되어 있지 않는 경우 즉시 반환
		if(this.logFile == null) {
			return;
		}
		
		// 백업 파일의 prefix 생성
		String backupPrefix = this.logFile.getName() + ".";
		
		Files
			// 로그파일 목록 추출
			.find(
				this.logFile.getParentFile().toPath(), // Start
				1, // MaxDepth, 현재 디렉토리의 파일만 정리함, 하위 디렉토리는 하지 않음
				(path, attr) -> {
					
					String fileName = path.toFile().getName();
					
					// 현재 로그 파일은 제외
					if(this.logFile.getName().equals(fileName)) {
						return false;
					}
					
					// 파일명이 로그 백업 파일인지 검사 및 반환 
					return fileName.startsWith(backupPrefix);
			})
			// 파일명 오름차순 정렬
			.sorted((path1, path2) -> {
				return path2.toString().compareTo(path1.toString());
			})
			// 상위 file count 개수 만큼만 남기고 나머지 삭제 
			.skip(this.fileCount)
			.forEach(path -> {
				try {
					Files.delete(path);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			});
	} // End of removeOldLogFiles method
}
