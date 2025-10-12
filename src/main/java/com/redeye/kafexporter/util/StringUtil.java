package com.redeye.kafexporter.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Data;

/**
 * 문자열 처리 관련 Utility 클래스
 * 
 * @author jmsohn
 */
public class StringUtil {
	
	/**
	 * 주어진 문자열에 대한 이스케이프 처리
	 * 
	 * @param str 주어진 문자열
	 * @return 이스케이프 처리된 문자열
	 */
	public static String escape(String str) throws Exception {
		
		// 입력값 검증
		if(str == null) {
			return null;
		}
		
		// 이스케이프 처리된 문자열 변수
		StringBuilder escapedStr = new StringBuilder("");
		
		// 유니코드 임시 저장 변수
		StringBuilder unicodeStr = new StringBuilder(""); 
		
		// 이스케이프 처리를 위한 상태 변수
		// 0: 문자열, 1: 이스케이프 문자,
		// 11:유니코드 1번째 문자, 12:유니코드 2번째 문자, 13:유니코드 3번째 문자, 14:유니코드 4번째 문자 
		int status = 0;
		
		for(int index = 0; index < str.length(); index++) {
			
			char ch = str.charAt(index);
			
			if(status == 0) {
				
				if(ch == '\\') {
					status = 1;
				} else {
					escapedStr.append(ch);
				}
				
			} else if(status == 1) {
				
				// 상태를 일반 문자열 상태로 설정
				// 먼저 상태를 변경하는 이유는 Unicode 시작시 상태가 10으로 변경하기 때문에
				// 마지막에 상태를 변경하면 안됨
				status = 0;
				
				if(ch == '0') {
					escapedStr.append('\0'); // ASCII 0 추가
				} else if(ch == 'b') {
					escapedStr.append('\b');
				} else if(ch == 'f') {
					escapedStr.append('\f');
				} else if(ch == 'n') {
					escapedStr.append('\n');
				} else if(ch == 'r') {
					escapedStr.append('\r');
				} else if(ch == 't') {
					escapedStr.append('\t');
				} else if(ch == 'u') {
					// Unicode 시작
					status = 11;
				} else {
					// 없을 경우 해당 문자를 그냥 추가함
					// ex) \' 인경우 '를 추가
					escapedStr.append(ch);
				}
				
			} else if(status >= 10 && status <= 14) {
				
				// ch가 16진수 값(0-9, A-F, a-f) 인지 확인
				if(isHex(ch) == false) {
					throw new Exception("unicode value is invalid:" + ch);
				}
				
				// unicode 버퍼에 ch추가
				unicodeStr.append(ch);

				// 상태값을 하나 올림
				// ex) 10:유니코드 시작 -> 11:유니코드 1번째 문자
				status++;
				
				// Unicode escape가 종료(status가 15 이상)되면
				// Unicode를 추가하고, 상태를 일반문자열 상태로 변경함
				if(status >= 15) {
					
					char unicodeCh = (char)Integer.parseInt(unicodeStr.toString(), 16);
					escapedStr.append(unicodeCh);
					
					unicodeStr.delete(0, unicodeStr.length());
					status = 0;
				}
				
			} else {
				throw new Exception("Unexpected status: " + status);
			}
		} // End of for
		
		return escapedStr.toString();
	}
	
	/**
	 * 주어진 문자(ch)가 16진수 값(0-9, A-F, a-f) 인지 확인
	 * 
	 * @param ch 검사할 문자
	 * @return 16진수 값 여부(16진수 값일 경우 true, 아닐 경우 false)
	 */
	private static boolean isHex(char ch) {
		return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
	}
	
	/**
	 * 두 문자가 동일한지 비교<br>
	 * 대소문자 구분 여부를 확인하여 두 문자를 비교함
	 * 
	 * @param ch1 비교할 문자 1 
	 * @param ch2 비교할 문자 2
	 * @param ignoreCase 대소문자 구분 여부(true-구분하지 않음, false-구분함)
	 * @return 동일 여부
	 */
	public static boolean isEqualChar(char ch1, char ch2, boolean ignoreCase) {
		
		if(ignoreCase == false) {
			return ch1 == ch2;
		} else {
			return Character.toLowerCase(ch1) == Character.toLowerCase(ch2);
		}
	}
	
	/**
	 * 문자열 내 여러 문자열을 검색하는 메소드<br>
	 * -> 문자열을 한번만 읽어 수행 속도 향상 목적
	 * 
	 * @param contents 문자열
	 * @param ignoreCase 대소문자 구분 여부(true - 구분하지 않음, false - 구분함)  
	 * @param findStrs 검색할 문자열들
	 * @return 최초로 발견된 위치 목록(못찾은 경우 -1)
	 */
	public static int[] find(String contents, boolean ignoreCase, String... findStrs) throws Exception {
		
		// 입력값 검증
		if(contents == null) {
			throw new NullPointerException("contents is null");
		}
		
		if(findStrs == null) {
			throw new NullPointerException("findStrs is null");
		}
		
		if(findStrs.length == 0) {
			return new int[]{};
		}
		
		// 검색 문자열들에 대한 정보 객체 변수 선언 및 초기화 수행
		ArrayList<FindStr> findStrObjs = new ArrayList<FindStr>(findStrs.length);
		for(int index = 0; index < findStrs.length; index++) {
			findStrObjs.add(new FindStr(findStrs[index], ignoreCase));
		}
		
		// 대상 문자열을 한문자씩 읽어서 검색 수행
		for(int index = 0; index < contents.length(); index++) {
			
			char ch = contents.charAt(index);
			
			// 검색 문자열 별로 검색
			for(FindStr findStrObj: findStrObjs) {
				findStrObj.process(index, ch);
			}
		}
		
		// 검색 결과를 반환하기 위해 int 배열 형태로 변경
		int[] findLocs = new int[findStrObjs.size()];
		for(int index = 0; index < findLocs.length; index++) {
			findLocs[index] = findStrObjs.get(index).getFindLoc();
		}
		
		// 검색 결과 반환
		return findLocs;
	}
	
	/**
	 * find 메소드에서 사용할 검색 정보 클래스 
	 * 
	 * @author jmsohn
	 */
	@Data
	private static class FindStr {
		
		/** 검색해야할 문자열 */
		private String findStr;
		/** 대소문자 구분 여부(true - 구분하지 않음, false - 구분함) */
		private boolean ignoreCase;
		
		/** 최초 일치 위치 */
		private int findLoc;
		/**
		 * 검색 중인 문자열 위치 정보
		 * key - 일치 시작 위치, value - 문자열 내에 현재까지 일치하는 위치
		 */
		private Map<Integer, Integer> pins;
		
		/**
		 * 생성자
		 * 
		 * @param findStr
		 */
		FindStr(String findStr, boolean ignoreCase) throws Exception {
			
			if(findStr == null) {
				throw new NullPointerException("findStr is null");
			}
			
			this.setFindStr(findStr);
			this.setIgnoreCase(ignoreCase);
			this.setFindLoc(-1);	// 못찾은 경우 -1
			this.setPins(new HashMap<Integer, Integer>());
		}
		
		/**
		 * 입력된 문자에 대해 검색 수행
		 * -> 한문자씩 확인 작업 수행
		 * 
		 * @param index 검색 대상 문자열내에 현재 위치
		 * @param ch 입력된 문자
		 */
		void process(int index, char ch) {
			
			// 이미 찾은 경우 더이상 검색을 수행하지 않음
			if(this.getFindLoc() != -1) {
				return;
			}

			// 문자가 일치하지 않는 경우
			// pin에서 삭제할 대상 목록
			Set<Integer> toRemove = new HashSet<Integer>();
			
			// 각 pin 들에 대해 주어진 문자(ch)와 검색 중인 문자(findCh) 일치 여부를 확인
			for(int startIndex: this.getPins().keySet()) {
				
				int findIndex = this.getPins().get(startIndex);
				char findCh = this.getFindStr().charAt(findIndex);
				
				if(StringUtil.isEqualChar(ch, findCh, this.ignoreCase) == true) {
					
					findIndex++;
					
					// 문자열 일치하는 경우
					// -> findLoc 설정 후 종료
					// 문자열 일치하지 않는 경우
					// -> 하나 증가된 findIndex를 startIndex에 설정
					if(findIndex >= this.getFindStr().length()) {
						this.setFindLoc(startIndex);
						return;
					} else {
						this.getPins().put(startIndex, findIndex);
					}
					
				} else {
					
					// 문자가 일치하지 않을 경우 삭제 대상에 추가
					// 여기에서 삭제하면 for 문이 돌고 있는 중에 대상에 변화가 생겨 오류가 발생
					toRemove.add(startIndex);
				}
			}
			
			// pin 목록에서 pin 삭제
			for(Integer key: toRemove) {
				this.getPins().remove(key);
			}
			
			// 최초 문자와 일치하는 경우 새로운 pin 생성
			if(StringUtil.isEqualChar(ch, this.getFindStr().charAt(0), this.ignoreCase) == true) {
				this.getPins().put(index, 1);
			}
		}
		
	}
	
	/**
	 * 문자열 내에 검색할 문자열이 하나라도 있는지 확인하는 메소드
	 * 
	 * @param contents 문자열
	 * @param ignoreCase 대소문자 구분 여부(true - 구분하지 않음, false - 구분함)
	 * @param findStrs 검색할 문자열들
	 * @return 문자열 내에 검색할 문자열이 하나라도 있는지 여부
	 */
	public static boolean containsAny(String contents, boolean ignoreCase, String... findStrs) throws Exception {
		
		int[] indexes = find(contents, ignoreCase, findStrs);
		
		for(int index: indexes) {
			
			// 문자열이 있는 경우 true를 반환
			if(index >= 0) {
				return true;
			}
		}
		
		// 검색된 문자열이 없는 경우 false를 반환
		return false;
	}
	
	/**
	 * 여러 문자열을 구분자(delimiter)를 넣어 이어 붙히는 메소드
	 * 
	 * @param delimiter 구분자
	 * @param strs 문자열들
	 * @return 이어 붙힌 문자열
	 */
	public static String join(String delimiter, String... strs) throws Exception {
		
		// 입력값 검증
		if(delimiter == null) {
			throw new NullPointerException("delimiter is null");
		}
		
		if(strs == null) {
			throw new NullPointerException("strs is null");
		}
		
		// 문자열을 이어 붙히기 위한 StringBuilder 변수
		StringBuilder joinStr = new StringBuilder("");
		
		// 각 문자열들을 이어 붙힘
		for(int index = 0; index < strs.length; index++) {
			
			if(strs[index] == null) {
				throw new NullPointerException("strs array has null element at " + index);
			}
			
			// 문자열을 이어 붙힘
			// 단 마지막 문자열의 뒤에는 구분자(delimiter)를 붙히지 않음
			joinStr.append(strs[index]);
			if(index + 1 != strs.length) {
				joinStr.append(delimiter);
			}
		}
		
		return joinStr.toString();
	}
	
	/**
	 * 여러 객체의 toString()한 문자열을 구분자(delimiter)를 넣어 이어 붙히는 메소드
	 * 
	 * @param delimiter 구분자
	 * @param iter 여러 객체를 담고 있는 iterator
	 * @return 이어 붙힌 문자열
	 */
	public static String join(String delimiter, Iterable<?> iter) throws Exception {
		
		// iterator에서 문자열 목록을 만듦
		ArrayList<String> strs = new ArrayList<>();
		
		iter.forEach(obj -> {
			strs.add(obj.toString());
		});
		
		return join(delimiter, strs.stream().toArray(String[]::new));
	}
	
	/**
	 * 문자열의 길이를 반환하는 메소드<br>
	 * -> 주어진 문자열이 null 일 경우 0을 반환함
	 * 
	 * @param str 문자열
	 * @return 문자열의 길이
	 */
	public static int length(String str) {
		
		if(str == null) {
			return 0;
		}
		
		return str.length();
	}

	/**
	 * 문자열이 비어 있는지 반환하는 메소드<br>
	 * -> 주어진 문자열이 null 일 경우 true를 반환함
	 * 
	 * @param str 문자열
	 * @return 문자열이 비어 있는지 여부
	 */
	public static boolean isEmpty(String str) {
		
		if(str == null) {
			return true;
		}
		
		return str.isEmpty();
	}
	
	/**
	 * 문자열이 공백으로만 이루어져 있는지 반환하는 메소드<br>
	 * -> 주어진 문자열이 null 일 경우 true를 반환함
	 * 
	 * @param str 문자열
	 * @return 문자열이 비어 있는지 여부
	 */
	public static boolean isBlank(String str) {
		
		if(str == null) {
			return true;
		}
		
		return str.isBlank();
	}
	
	/**
	 * int 배열을 구분자(delimiter)를 넣어 문자열로 만드는 메소드
	 * 
	 * @param delimiter int 목록 사이에 추가할 구분자(null 일경우 "")
	 * @param array 문자열로 변환할 int 배열
	 * @return 변환된 문자열
	 */
	public static String join(String delimiter, int... array) {
		
		if(delimiter == null) {
			delimiter = "";
		}
		
		if(array == null) {
			return "";
		}
		
		StringBuilder builder = new StringBuilder();
		
		for(int index = 0; index < array.length; index++) {
			
			if(index != 0) {
				builder.append(delimiter);
			}
			
			builder.append(array[index]);
		}
		
		return builder.toString();
	}
}
