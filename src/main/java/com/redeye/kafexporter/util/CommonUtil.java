package com.redeye.kafexporter.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.InflaterInputStream;

/**
 * 공통 Util
 *
 * @author gunha, jmsohn, Lee Hyeonji
 * @version 0.1
 * @since 2023-02-01 오후 2:45
 */
public class CommonUtil {
	
	/**
	 * List 형 객체를 배열 형태로 변환하여 반환<br>
	 * List 형 객체가 null 일 경우, 빈 배열이 반환됨(null 반환 아님)
	 * 
	 * @param array 변환할 List 객체
	 * @param type List 객체 요소의 타입
	 * @return 변환된 배열 객체
	 */
	public static <T> T[] toArray(List<T> arrayList, Class<T> type) throws Exception {
		
		// 리스트의 요소 타입이 정의 되지 않은 경우 예외 발생
		if(type == null) {
			throw new Exception("type is null.");
		}
		
		// 생성할 배열 크기 변수
		int arrayLength = 0;
		
		// 리스트가 null일 경우, 빈 배열 생성
		if(arrayList != null) {
			arrayLength = arrayList.size();
		}
		
		// 배열 객체 생성
		// new T[];는 안됨
		@SuppressWarnings("unchecked")
		T[] array = (T[])Array.newInstance(type, arrayLength);
		
		// 배열 복사
		for(int index = 0; index < arrayLength; index++) {
			array[index] = arrayList.get(index);
		}
		
		// 배열 반환
		return array;
	}

    /**
     * get milliseconds from LocalDateTime
	 *
     * @param localDateTime: 메시지 생성시간, LocalDateTime.now()의 값
     * @return Type Long
     */
    public static Long localDateToLong(LocalDateTime localDateTime) {
        return ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

	/**
	 * get LocalDateTime from Str
	 *
	 * @param datetimeStr: LocalDateTime 으로 변환할 문자열 (시간대 정보 포함된 상태)
	 * @return Asia/Seoul 시간대의 LocalDateTime 객체
	 */
	public static LocalDateTime strToLocalDate(String datetimeStr) {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.appendPattern("yyyy-MM-dd HH:mm:ss")  // 기본 패턴
				.optionalStart()
				.appendFraction(ChronoField.MILLI_OF_SECOND, 1, 9, true) // 밀리초 표시
				.optionalEnd()
				.appendOffset("+HH:mm", "Z")  // 시간대 표시
				.toFormatter();
		LocalDateTime localDateTime = LocalDateTime.parse(datetimeStr, formatter);

		// 시간대 정보 설정
		ZoneId zoneId = ZoneId.of("Asia/Seoul");
		return ZonedDateTime.of(localDateTime, zoneId).toLocalDateTime();
	}
	
	/**
	 * 나노 시간으로 표현된 두 시간 문자열 사이의 시간차이를 반환
	 * 
	 * @param p1 첫번째 시간 문자열
	 * @param p2 두번째 시간 문자열
	 * @return 시간 차이
	 */
	public static long getIntervalNS(String p1, String p2) throws Exception {
		
		Instant instant1 = Instant.parse(p1);
		long p1Sec = instant1.getEpochSecond();
		int p1Nano = instant1.getNano(); 
		
		Instant instant2 = Instant.parse(p2);
		long p2Sec = instant2.getEpochSecond();
		int p2Nano = instant2.getNano(); 
		
		long interval = (p1Sec - p2Sec) * 1000000000L + (p1Nano - p2Nano);
		
		return interval;
	}

	/**
	 * target Byte의 끝 부분과
	 * 지정한 접미사의 Byte가 일치하는지 확인
	 *
	 * @param target 확인할 target
	 * @param suffix 접미사
	 * @return Byte 일치 여부
	 */
    public static boolean endsWithBytes(byte[] target, byte[] suffix) throws Exception {

		// parameter null 체크
    	if(target == null) {
    		throw new NullPointerException("target array is null.");
    	}
    	
    	if(suffix == null) {
    		throw new NullPointerException("suffix array is null.");
    	}

		// target의 길이가 접미사보다 짧으면 항상 false
		// -> 접미사의 일부만 포함되어 있을 경우
    	if(suffix.length > target.length) {
    		return false;
    	}

		// 비교를 시작할 target의 끝 부분 위치
    	int start = target.length - suffix.length;

		// 각 Byte array의 index번째에 있는 byte가 같은지 확인
    	for(int index = 0; index < suffix.length; index++) {
    		if(target[start + index] != suffix[index]) {
    			return false;
    		}
    	}
    	
    	return true;

    }

	/**
	 * 두 Byte array를 합침
	 *
	 * @param first,second 합칠 Byte array
	 * @return 합쳐진 Byte array
	 */
    public static byte[] concatBytes(byte[] first, byte[] second) throws Exception {

		// parameter null 체크
    	if(first == null) {
    		throw new NullPointerException("first array is null.");
    	}
    	
    	if(second == null) {
    		throw new NullPointerException("second array is null.");
    	}

		// 합쳐진 Byte array
		// 두 Byte array 크기의 합 만큼의 사이즈 할당
    	byte[] concatenatedArray = new byte[first.length + second.length];

		// array를 복사함
		// concatenatedArary에 first 복사 후 second 복사해서 붙임
    	System.arraycopy(first, 0, concatenatedArray, 0, first.length);
    	System.arraycopy(second, 0, concatenatedArray, first.length, second.length);
    	
    	return concatenatedArray;
    }

	/**
	 * Byte를 split 해주는 메소드.<br>
	 * target byte와 구분자의 byte를 비교하여<br>
	 * 구분자가 포함 됐을 경우 분리
	 *
	 * @param target target byte
	 * @param split 구분자 byte
	 * @return 구분자에 의해 분리된 결과
	 */
    public static ArrayList<byte[]> splitBytes(byte[] target, byte[] split) throws Exception {

		// parameter null 체크
    	if(target == null) {
    		throw new NullPointerException("target array is null.");
    	}
    	
    	if(split == null) {
    		throw new NullPointerException("split array is null.");
    	}

		// 구분자에 의해 분리된 결과
    	ArrayList<byte[]> splitedTarget = new ArrayList<>();

		// 구분자와 동일하지 않아 분리된 byte
    	byte[] pieceBuffer = new byte[target.length];
    	int pieceBufferPos = 0;

		// 구분자와 동일한 byte를 저장해둠
		// -> 구분자의 문자가 일부 포함되어 있을 경우를 위함
    	byte[] splitBuffer = new byte[split.length];
    	int splitBufferPos = 0;

		// 동일한지 상태를 체크할 변수.
		// 구분자와 동일할 경우 1, 아닐 경우 0
    	int status = 0;
		// 구분자 위치
    	int splitPos = 0;
    	
    	for(int index = 0; index < target.length; index++) {
    		
    		byte cur = target[index];

			/* target과 구분자의 byte가 동일할 경우
			 * - splitBuffer에 저장
			 * - 다음 비교를 위해 target과 구분자의 위치를 한칸 이동
			 * - 만약 구분자가 모두 포함되어 있을 경우 splitTarget에 추가 */
    		if(cur == split[splitPos]) {
    			
    			status = 1; // 상태를 1로 바꿈

				// splitBuffer에 현재 byte 저장 후 한칸 이동
    			splitBuffer[splitBufferPos++] = cur;

				// 구분자 한칸 이동
    			splitPos++;

				// 구분자가 모두 포함되어 있을 경우
				// -> 지금까지 분리된 byte(pieceBuffer)를 splitTarget에 추가
    			if(splitPos >= split.length) {

					// pieceBuffer 복사 후 추가
    				byte[] piece = new byte[pieceBufferPos];
    				System.arraycopy(pieceBuffer, 0, piece, 0, pieceBufferPos);
    				
    				if(pieceBufferPos != 0) {
    					splitedTarget.add(piece);
    				}

					// 분리에 필요한 변수 초기화
    				splitBufferPos = 0;
    				pieceBufferPos = 0;
    				splitPos = 0;
    				status = 0;
    			}
    			
    		} else {
				/* target과 구분자의 byte가 동일하지 않을 경우
				 * - pieceBuffer에 byte를 추가
				 * - 상태가 1일 경우 구분자에 일부 포함된 byte가 있었던 것이므로
				 *   splitBuffer를 pieceBuffer에 추가 */
    			
    			if(status == 1) {
					// splitBuffer를 복사해 pieceBuffer에 추가함
    				System.arraycopy(splitBuffer, 0, pieceBuffer, pieceBufferPos, splitBufferPos);

					// pieceBuffer의 위치를 splitBuffer 만큼 이동
    				pieceBufferPos += splitBufferPos;
    				splitBufferPos = 0;
    			}

				// 상태는 그대로 0
    			status = 0;
    			splitPos = 0;

				// pieceBuffer에 현재 byte를 추가하고
				// pieceBufferPos 위치 한칸 이동
    			pieceBuffer[pieceBufferPos++] = cur;
    		}
    		
    	}
    	
    	// 종료 처리
    	if(pieceBufferPos != 0 || splitBufferPos != 0) {

			// 현재까지 분리된 Byte를 복사
	    	byte[] piece = new byte[pieceBufferPos + splitBufferPos];
	    	System.arraycopy(pieceBuffer, 0, piece, 0, pieceBufferPos);
	    	
	    	if(splitBufferPos != 0) {
	    		System.arraycopy(splitBuffer, 0, piece, pieceBufferPos, splitBufferPos);
	    	}

			//splitedTarget에 추가
    		splitedTarget.add(piece);
    	}
    	
    	return splitedTarget;
    	
    }
    
	/**
	 * 객체의 타입에 따라 int 값으로 반환
	 * 
	 * @param obj int로 변환할 객체
	 * @return 변환된 int 값
	 */
	public static int toInteger(Object obj) throws Exception {
		
		// 입력값 검증
		if(obj == null) {
			throw new NullPointerException("obj is null");
		}
		
		// 타입에 따라 integer 형태로 만들어 반환
		return toInteger(obj, -1);
	}
    
	/**
	 * 객체의 타입에 따라 int 값으로 반환
	 * 
	 * @param obj int로 변환할 객체
	 * @param defaultValue obj가 null일 경우 반환되는 값
	 * @return 변환된 int 값
	 */
	public static int toInteger(Object obj, int defaultValue) throws Exception {
		
		// 입력값 검증
		if(obj == null) {
			return defaultValue;
		}
		
		// 타입에 따라 integer 형태로 만들어 반환
		if(obj instanceof Integer) {
			return (Integer)obj;
		} else if(obj instanceof String) {
			return Integer.parseInt(obj.toString());
		} else {
			throw new Exception("Unexpected type:" + obj.getClass());
		}
	}
	
	/**
	 * 객체의 타입에 따라 long 값으로 반환
	 * 
	 * @param obj long로 변환할 객체
	 * @return 변환된 long 값
	 */
	public static long toLong(Object obj) throws Exception {
		
		// 입력값 검증
		if(obj == null) {
			throw new NullPointerException("obj is null");
		}
		
		// 타입에 따라 double 형태로 만들어 반환
		return toLong(obj, -1);
	}
    
	/**
	 * 객체의 타입에 따라 long 값으로 반환
	 * 
	 * @param obj long로 변환할 객체
	 * @param defaultValue obj가 null일 경우 반환되는 값
	 * @return 변환된 long 값
	 */
	public static long toLong(Object obj, long defaultValue) throws Exception {
		
		// 입력값 검증
		if(obj == null) {
			return defaultValue;
		}
		
		// 타입에 따라 long 형태로 만들어 반환
		if(obj instanceof Long) {
			return (Long)obj;
		} else if(obj instanceof String) {
			return Long.parseLong(obj.toString());
		} else {
			throw new Exception("Unexpected type:" + obj.getClass());
		}
	}
	
	/**
	 * 객체의 타입에 따라 double 값으로 반환
	 * 
	 * @param obj double로 변환할 객체
	 * @return 변환된 double 값
	 */
	public static double toDouble(Object obj) throws Exception {
		
		// 입력값 검증
		if(obj == null) {
			throw new NullPointerException("obj is null");
		}
		
		// 타입에 따라 double 형태로 만들어 반환
		return toDouble(obj, -1);
	}
    
	/**
	 * 객체의 타입에 따라 double 값으로 반환
	 * 
	 * @param obj double로 변환할 객체
	 * @param defaultValue obj가 null일 경우 반환되는 값
	 * @return 변환된 double 값
	 */
	public static double toDouble(Object obj, long defaultValue) throws Exception {
		
		// 입력값 검증
		if(obj == null) {
			return defaultValue;
		}
		
		// 타입에 따라 double 형태로 만들어 반환
		if(obj instanceof Double) {
			return (Double)obj;
		} else if(obj instanceof String) {
			return Double.parseDouble(obj.toString());
		} else {
			throw new Exception("Unexpected type:" + obj.getClass());
		}
	}

	
	/**
	 * 객체의 타입에 따라 boolean 값으로 반환
	 * 
	 * @param obj boolean로 변환할 객체
	 * @return 변환된 boolean 값
	 */
	public static boolean toBoolean(Object obj) throws Exception {
		
		// 입력값 검증
		if(obj == null) {
			throw new NullPointerException("obj is null");
		}
		
		// 타입에 따라 boolean 형태로 만들어 반환
		return toBoolean(obj, false);
	}
	
	/**
	 * 객체의 타입에 따라 boolean 값으로 반환
	 * 
	 * @param obj boolean로 변환할 객체
	 * @param defaultValue obj가 null일 경우 반환되는 값
	 * @return 변환된 boolean 값
	 */
	public static boolean toBoolean(Object obj, boolean defaultValue) throws Exception {
		
		// 입력값 검증
		if(obj == null) {
			return defaultValue;
		}
		
		// 타입에 따라 boolean 형태로 만들어 반환
		if(obj instanceof Boolean) {
			return (Boolean)obj;
		} else if(obj instanceof String) {
			return Boolean.parseBoolean(obj.toString());
		} else {
			throw new Exception("Unexpected type:" + obj.getClass());
		}
	}
	
	/**
	 * 문자열을 byte 배열로 변환<br>
	 * ex) "1A03" -> byte[] {26, 3}
	 * 
	 * @param str 문자열
	 * @return 변환된 byte 배열
	 */
	public static byte[] strToBytes(String str) throws Exception {
		
		// 입력값 검증
		if(str == null) {
			throw new NullPointerException("str is null");
		}
		
		if(str.length() % 2 != 0) {
			throw new Exception("str must be even");
		}
		
		// 변환된 byte 배열을 담을 변수
		byte[] bytes = new byte[str.length()/2];
		
		for(int index = 0; index < bytes.length; index++) {
			
			// 상위 니블의 데이터를 가져옴
			byte b1 = getByte(str.charAt(index * 2));
			// 왼쪽으로 4 bit를 이동하여 상위 니블로 만듦
			b1 = (byte)(b1 << 4);
			
			// 하위 니블의 데이터를 가져옴
			byte b2 = getByte(str.charAt(index * 2 + 1));

			// 상위 니블(b1)과 하위니블(b2)를 합쳐서 저장
			bytes[index] = (byte)(b1 + b2);
		}
		
		// 변환 결과를 반환
		return bytes;
	}
	
	/**
	 * 주어진 문자에 해당하는 byte를 반환하는 메소드
	 * 
	 * @param ch 문자
	 * @return 문자를 byte로 변환한 결과
	 */
	private static byte getByte(char ch) throws Exception {
		
		if(ch >= '0' && ch <= '9') {
			return (byte)(ch - '0');
		} else if(ch >= 'a' && ch <= 'z') {
			return (byte)(ch - 'a' + 10);
		} else if(ch >= 'A' && ch <= 'Z') {
			return (byte)(ch - 'A' + 10);
		} else {
			throw new Exception("Unexpected char:" + ch); 
		}
	}
	
	/**
	 * byte 배열을 문자열로 변환
	 * ex) byte[] {26, 3} -> "1A03" 
	 * 
	 * @param bytes byte 배열
	 * @return 변환된 문자열
	 */
	public static String bytesToStr(byte[] bytes) throws Exception {
		
		if(bytes == null) {
			throw new NullPointerException("bytes is null");
		}
		
		StringBuilder builder = new StringBuilder("");
		
		for(int index = 0; index < bytes.length; index++) {
			builder.append(String.format("%02X", bytes[index]));
		}
		
		return builder.toString();
	}
	
	/**
	 * Integer List를 int 배열로 변환함 
	 * 
	 * @param arrayList Integer 타입의 List
	 * @return 변환된 int 배열
	 */
	public static int[] toIntArray(List<Integer> arrayList) {
		
		if(arrayList == null) {
			return new int[0];
		}
		
		int[] intArray = new int[arrayList.size()];
		for(int index = 0; index < intArray.length; index++) {
			intArray[index] = arrayList.get(index);
		}
		
		return intArray;
		
	}
    
	/**
	 * 파일 내용을 읽어들임 
	 * 
	 * @param path 파일의 경로
	 * @return 파일에서 읽어들인 내용
	 */
	public static String readFile(File inputfile) throws FileNotFoundException, IOException {
		
		File file = inputfile;
		
		try (Reader in = new FileReader(file)) {
			
			StringBuffer sb = new StringBuffer("");
			
			int read = in.read();
			while(read != -1) {
				char ch = (char)read;
				sb.append(ch);
				read = in.read(); 
			}
			
			return sb.toString();
		} 
	}
	
	/**
	 * bit로 표현된 map에 설정된 index 목록을 반환
	 * ex) bitMap: 0b1101, size:3 -> 1, 3
	 * 
	 * @param bitMap bit로 표현된 맵 
	 * @param size bit 표현된 맵의 크기
	 * @return index 목록
	 */
	public static ArrayList<Integer> bitmapToIndices(int bitMap, int size) {
		
		// index 목록 변수
		ArrayList<Integer> indices = new ArrayList<>();
		
		// 사이즈 만큼 루프 수행
		for(int index = 0; index < size; index++) {
			
			// 가장 끝에 bit가 1인지 확인
			if((bitMap & 1) == 1) {
				indices.add(index + 1);
			}

			// 오른쪽으로 한칸시프트
			bitMap = bitMap >>> 1;
		}
		
		return indices;
	}
	
	/**
	 * byte 배열의 특정 위치를 잘라서 반환
	 * 
	 * @param array 대상 byte 배열
	 * @param start 자르기 시작 지점
	 * @param length 자를 크기
	 * @return 잘라진 배열
	 */
	public static byte[] cut(byte[] array, int start, int length) throws Exception {
		
		if(array == null) {
			throw new NullPointerException("array is null.");
		}
		
		if(start < 0 || start >= array.length) {
			throw new IllegalArgumentException("start is invalid:" + start);
		}
		
		if(length < 1) {
			throw new IllegalArgumentException("length is invalid:" + start);
		}

		byte[] cutArray = new byte[length];
		System.arraycopy(array, start, cutArray, 0, length);
		
		return cutArray;
	}
    
	/**
	 * 목표 배열(target) 내에 찾을 배열(lookup)의 첫번째 일치하는 시작 위치를 반환<br>
	 * 만일 찾지 못하면 -1을 반환함
	 * 
	 * @param target 목표 배열
	 * @param start target의 검색 시작 지점
	 * @param lookup 찾을 배열
	 * @return 목표 배열 내에 첫번째 일치하는 위치
	 */
	public static int indexOf(byte[] target, int start, byte[] lookup) throws Exception {
		
		// 목표 배열의 크기(target.length - start)가 찾을 배열의 크기(lookup.length)
		// 보다 작은 경우에는 -1을 반환
		if(target == null || lookup == null || target.length - start < lookup.length) {
			return -1;
		}
		
		// 상태 변수 - 0:배열내 불일치 상태, 1:찾을 배열과 일치 중인 상태
		int status = 0;
		// 목표 배열내 검색 중인 위치 변수
		int pos = start;
		// 목표 배열내 찾을 배열과 최초로 일치하는 위치 저장용 변수
		int savePos = -1;
		// 찾을 배열내 위치 변수
		int lookupPos = 0;
		
		// 검색 위치가 목표 배열의 크기 보다 작을 경우 수행
		while(pos < target.length) {
			
			if(target[pos] == lookup[lookupPos]) {
				
				// 최초로 일치하는 경우
				// 현재 위치를 savePos에 저장
				if(status == 0) {
					savePos = pos;
					status = 1;
				}
				
				// 목표 배열내 검색 위치와 찾을 배열내 검색 위치를
				// 다음 byte로 이동
				pos++;
				lookupPos++;
				
				// 만일 찾을 배열의 모든 문자를 검색 완료하였으면
				// 일치 시작 위치(savePos)를 반환
				if(lookupPos >= lookup.length) {
					return savePos;
				}
				
			} else {
				
				if(status == 1) {
					
					// 기존 savePos로 이동하게 되면 다시 매치되기 때문에 +1 문자부터 검사하도록함
					pos = savePos + 1;
					
					// 초기화
					savePos = -1;
					lookupPos = 0;
					status = 0;
					
				} else if(status == 0) {
					
					// 검색 위치를 하나 증가하여 다음 byte를 비교
					pos++;
					
				} else {
					throw new Exception("Unexpected status:" + status);
				}
				
			}
		}
		
		// 목표 배열을 모두 확인하였으나
		// 찾지 못함
		return -1;
	}
	
	/**
	 * 목표 배열(target) 내에 찾을 배열(lookup)의 첫번째 일치하는 위치를 반환<br>
	 * 만일 찾지 못하면 -1을 반환함
	 * 
	 * @param target 목표 배열
	 * @param lookup 찾을 배열
	 * @return 목표 배열 내에 첫번째 일치하는 위치
	 */
	public static int indexOf(byte[] target, byte[] lookup) throws Exception {
		return indexOf(target, 0, lookup);
	}

	/**
	 * state 압축 해제 <br>
	 *
	 * <ol>
	 * <li> Base64 디코딩 </li>
	 * <li> 압축 해제 </li>
	 * </ol>
	 *
	 * @param compressedState 압축된 상태정보
	 * @throws IOException 입출력 오류
	 * @return byteArray
	 */
	public static byte[] decompressState(byte[] compressedState) throws IOException {

		// 입력값 검증
		if (compressedState == null || compressedState.length == 0) {
			throw new IllegalArgumentException("CompressedState byte array cannot be null or empty");
		}

		// Base64 디코딩
		byte[] decodedBytes = Base64.getDecoder().decode(compressedState);

		// 압축 해제하는데 사용되는 InflaterInputStream 객체 생성
		try (InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(decodedBytes))) {
			// 압축 해제된 데이터를 담는 곳
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024];
			int length;
			// 압축 해제된 데이터 읽고, outputStream 에 쓰기
			while ((length = inflaterInputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, length);
			}

			// 바이트배열로 반환
			return outputStream.toByteArray();
		}
	}

	/**
	 * get OffsetDateTime from Long
	 *
	 * @param time 타임 long값
	 * @throws IllegalArgumentException 표준 시간대를 잘못 설정할 경우, OffsetDateTime 변환 실패할 경우
	 * @return offsetDateTime
	 */
	public static OffsetDateTime longToOffsetDateTime(long time) {

		// 추후 환경변수로 추출
		String zoneIdStr = "Asia/Seoul";

		ZoneId zone;
		try {
			// 주어진 시간대 문자열 체크
			zone = ZoneId.of(zoneIdStr);
		} catch (Exception e) {
			String msg = String.format(
					"Invalid zoneIdStr: %s. Available zoneIdStr are: %s",
					zoneIdStr,
					String.join(", ", ZoneId.getAvailableZoneIds()) // 올바른 ZoneIdStr을 msg로 보여줌
			);
			throw new IllegalArgumentException(msg, e);
		}

		// OffsetDateTime으로 변환
		try {
			return Instant.ofEpochMilli(time).atZone(zone).toOffsetDateTime();
		} catch (Exception e) {
			throw new RuntimeException("Failed to convert time to OffsetDateTime", e);
		}
	}

	/**
	 * get Long from OffsetDateTime
	 *
	 * @param time 타임 OffsetDateTime값
	 * @throws RuntimeException
	 * @return long값 / null값이면 0L 반환
	 */
	public static long offsetDateTimeToLong(OffsetDateTime time) {

		if (time == null) {
			return 0L;
		}

		try {
			// epoch millisecond로 변환
			return time.toInstant().toEpochMilli();
		} catch (Exception e) {
			throw new RuntimeException("Failed to convert OffsetDateTime to long", e);
		}
	}
}
