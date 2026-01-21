package com.redeye.kafexporter.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Web Service 관련 Utility 클래스
 * 
 * @author jmsohn
 */
public class WebUtil {
	
	/** html entity 변환 맵(replaceHtmlEntity) */ 
	private static Map<Character, String> htmlEntityMap;
	/** 유효한 확장자 모음 */
	private static String[] defaultValidExts;
	/** email 패턴 */
	private static Pattern emailP = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	
	static {
		
		/* html entity 변환 맵 초기화 */ 
		htmlEntityMap = new HashMap<Character, String>();
		
		htmlEntityMap.put('&', "&amp;");
		htmlEntityMap.put('<', "&lt;");
		htmlEntityMap.put('>', "&gt;");
		htmlEntityMap.put('"', "&quot;");
		htmlEntityMap.put('\'', "&#x27;");
		htmlEntityMap.put('/', "&#x2F;");
		htmlEntityMap.put('(', "&#x28;");
		htmlEntityMap.put(')', "&#x29;");
		
		/* 유효한 확장자 모음 초기화 */
		defaultValidExts = new String[] {

			// 텍스트 파일 확장자
			".txt", ".rtf",
	
			// 엑셀 파일 확장자
			".csv", ".xls", ".xlsx", ".xlt", ".xltx", ".xltm", ".xlw",
	
			// 파워포인트 파일 확장자
			".ppt", ".pptx",
	
			// 워드 파일 확장자
			".doc", ".docx", ".docm", ".dot", ".dotx", ".dotm",
	
			// 아래한글 파일 확장자
			".hwp", ".hwpx",
	
			// pdf 파일 확장자
			".pdf",
	
			// 이미지 파일 확장자
			".jpg", ".jpeg", ".tiff", ".gif", ".bmp", ".png",
	
			// 동영상 파일 확장자
			".mp3", ".mp4", ".mov", ".wmv", ".avi", ".mpeg"
		};
	}
	
	/**
	 * "호스트:포트" 형태의 문자열을 {"호스트", "포트"} 문자열 배열로 파싱하여 반환
	 * 
	 * @param hostStr "호스트:포트" 형태의 문자열
	 * @return {"호스트", "포트"} 문자열 배열
	 */
	public static String[] parseHostPort(String hostStr) throws Exception {
		
		String hostPatternStr = "(?<hostname>[a-zA-Z0-9\\-\\_]+(\\.[a-zA-Z0-9\\-\\_]+)*)\\:(?<port>[0-9]+)";
		
		Pattern hostPattern = Pattern.compile(hostPatternStr);
		Matcher hostMatcher = hostPattern.matcher(hostStr);
		
		if(hostMatcher.matches() == false) {
			throw new Exception("host string is not matched:" + hostStr);
		}
		
		String hostName = hostMatcher.group("hostname");
		String port = hostMatcher.group("hostname");
		
		return new String[] {hostName, port};
	}
	
	/**
	 * 문자열의 html 엔터티(<>& 등 -> &amp;lt;&amp;gt;&amp;amp; 등)를 변경 
	 * 
	 * @param contents 문자열
	 * @return 변경된 문자열
	 */
	public static String replaceHtmlEntity(String contents) throws Exception {
		
		if(contents == null || contents.isEmpty() == true) {
			return contents;
		}
		
		StringBuilder replacedContents = new StringBuilder("");
		for(int index = 0; index < contents.length(); index++) {
			
			char ch = contents.charAt(index);
			
			if(htmlEntityMap.containsKey(ch) == true) {
				replacedContents.append(htmlEntityMap.get(ch));
			} else {
				replacedContents.append(ch);
			}
		}
		
		return replacedContents.toString();
	}
	
	/**
	 * 문자열(contents) 내에 "(\r)\n" -> "&lt;br&gt;\r\n"로 변경하는 메소드  
	 * 
	 * @param contents 문자열
	 * @return 대체된 문자열
	 */
	public static String replaceEnterToBr(String contents) throws Exception {
		
		if(contents == null) {
			throw new NullPointerException("contents is null");
		}
		
		return contents.replaceAll("(\\r)?\\n", "<br>\r\n");
	}
	
	/**
	 * 파일명이 유효한지 검증하는 메소드<br>
	 * 유효할 경우 true
	 * 
	 * @param fileName 검사할 파일명
	 * @param length 파일명의 최대 길이
	 * @param validExts 유효한 확장자 목록
	 * @return 파일명의 유효성 여부
	 */
	public static boolean isValidFileName(String fileName, int length, String... validExts) throws Exception {

		// 파일명이 null 일경우 false 반환
		if(fileName == null) {
			return false;
		}

		// 유효한 확장자가 없으면 false 반환
		if(validExts == null || validExts.length == 0) {
			return false;
		}

		// 파일명에 null(\0)가 있는 경우 false 반환
		// null을 중간에 삽입하여 확장자 체크를 우회하는 방법을 차단함
		// 정상 사용자가 파일명에 null을 넣을 이유가 없음
		if(StringUtil.hasNull(fileName) == true) {
			return false;
		}
		
		// 파일명이 주어진 길이보다 길 경우 false 반환
		// Overflow 방법등을 사전 차단
		// 단, length가 음수일 경우 체크하지 않음
		if(length >= 0 && fileName.length() > length) {
			return false;
		}
		
		// 확장자 체크
		// 유효한 확장자가 있는지 확인
		// 파일명과 확장자명을 뒤집어서 체크
		// 만일 유효한 확장자가 있다면, 위치는 0이 될 것임
		String rFileName = StringUtil.reverse(fileName);
		
		String[] rValidExts = new String[validExts.length];
		for(int index = 0; index < validExts.length; index++) {
			rValidExts[index] = StringUtil.reverse(validExts[index]);
		}
		
		for(int loc: StringUtil.find(rFileName, true, rValidExts)) {
			if(loc == 0) {
				return true;
			}
		}
		
		// 유효한 확장자 목록에 없으면 false를 반환
		return false;
	}
	
	/**
	 * 파일명이 유효한지 검증하는 메소드<br>
	 * 유효할 경우 true
	 * 
	 * @param fileName 검사할 파일명
	 * @param validExts 유효한 확장자 목록
	 * @return 파일명의 유효성 여부
	 */
	public static boolean isValidFileName(String fileName, String... validExts) throws Exception {
		return isValidFileName(fileName, -1, validExts);
	}
	
	/**
	 * 파일명이 유효한지 검증하는 메소드<br>
	 * 유효한 확장자 모음(WebUtil.defaultValidExts)에 있는 확장자인지 검사함<br>
	 * 유효할 경우 true
	 * 
	 * @param fileName 검사할 파일명
	 * @param length 파일명의 최대 길이
	 * @return 파일명의 유효성 여부
	 */
	public static boolean isValidFileName(String fileName, int length) throws Exception {
		return isValidFileName(fileName, length, defaultValidExts);
	}
	
	/**
	 * 파일명이 유효한지 검증하는 메소드<br>
	 * 유효한 확장자 모음(WebUtil.defaultValidExts)에 있는 확장자인지 검사함<br>
	 * 유효할 경우 true
	 * 
	 * @param fileName 검사할 파일명
	 * @return 파일명의 유효성 여부
	 */
	public static boolean isValidFileName(String fileName) throws Exception {
		return isValidFileName(fileName, -1);
	}
	
	/**
	 * 주어진 문자열이 이메일 패턴인지 여부 반환
	 * 
	 * @param email 검사할 문자열
	 * @return 이메일 패턴 여부 - 이메일 패턴일 경우 true 반환
	 */
	public static boolean isEmailPattern(String email) {
		
		// 입력값이 빈값일 경우 false를 반환
		if(StringUtil.isBlank(email) == true) {
			return false;
		}
		
		// 주어진 문자열이 이메일 패턴인지 검사하여 반환
		Matcher emailM = emailP.matcher(email);
		return emailM.matches();
	}
}
