package com.redeye.kafexporter.util;

import java.util.ArrayList;
import java.util.List;

/**
 * byte array 처리 관련 utility 클래스
 * 
 * @author jmsohn
 */
public class BytesUtil {
	
	/**
	 * int 값을 byte 목록(8바이트)으로 변환
	 * 
	 * @param value 변환할 int 값
	 * @return 변환된 byte 목록
	 */
	public static byte[] intToBytes(int value) {
		
	    byte[] bytes = new byte[4];
	    
	    for (int i = 0; i < 4; i++) {
	        bytes[3 - i] = (byte) (value >> (i * 8));
	    }
	    
	    return bytes;
	}
	
	/**
	 * long 값을 byte 목록(8바이트)으로 변환
	 * 
	 * @param value 변환할 long 값
	 * @return 변환된 byte 목록
	 */
	public static byte[] longToBytes(long value) {
		
	    byte[] bytes = new byte[8];
	    
	    for (int i = 0; i < 8; i++) {
	        bytes[7 - i] = (byte) (value >> (i * 8));
	    }
	    
	    return bytes;
	}
	
	/**
	 * float 값을 byte 목록(4바이트)으로 변환
	 * 
	 * @param value 변환할 float 값
	 * @return 변환된 byte 목록
	 */
	public static byte[] floatToBytes(float value) {
		
		int intBits = Float.floatToIntBits(value);
	    
		byte[] bytes = new byte[4];
	    for (int i = 0; i < 4; i++) {
	        bytes[3 - i] = (byte) (intBits >> (i * 8));
	    }
	    
	    return bytes;
	}
	
	/**
	 * double 값을 byte 목록(8바이트)으로 변환
	 * 
	 * @param value 변환할 double 값
	 * @return 변환된 byte 목록
	 */
	public static byte[] doubleToBytes(double value) {
		
	    long longBits = Double.doubleToLongBits(value);
	    
	    byte[] bytes = new byte[8];
	    for (int i = 0; i < 8; i++) {
	        bytes[7 - i] = (byte) (longBits >> (i * 8));
	    }
	    
	    return bytes;
	}
	
	/**
	 * 바이트 배열을 비교하여 반환<br>
	 * 두 배열의 길이가 같고 요소도 동일하면 0,<br>
	 * b1의 요소와 b2의 요소를 비교하여 b1이 크면 1, b2가 크면 -1<br>
	 * 요소가 같으나 b1이 더 길 경우 1, b2가 더 길 경우 -1 반환 
	 * 
	 * @param b1 비교할 첫번째 바이트 배열
	 * @param b2 비고할 두번째 바이트 배열
	 * @return 비교 결과
	 */
	public static int compare(byte[] b1, byte[] b2) throws Exception {
		
		// 입력값 검증
		if(b1 == null) {
			throw new NullPointerException("byte array(b1) is null.");
		}
		
		if(b2 == null) {
			throw new NullPointerException("byte array(b2) is null.");
		}
		
		// 비교할 byte index의 종료 위치 변수
		// b1과 b2 중 크기가 작은 쪽까지 비교함
		int end = (b1.length > b2.length)?b2.length:b1.length;
		
		// 바이트 비교
		for(int index = 0; index < end; index++) {
			
			int compare = b1[index] - b2[index];
			
			// 같지 않으면 비교 결과를 반환
			if(compare != 0) {
				return sign(compare);
			}
		}
		
		// end 까지의 byte가 같으면 크기를 비교하여 반환
		if(b1.length == b2.length) {
			return 0;
		} else if(b1.length > b2.length) {
			return 1;
		} else {
			return -1;
		}
	}
	
	/**
	 * 주어진 숫자(double)가 양,음,0 부호 반환<br>
	 * 양수 이면, 1<br>
	 * 음수 이면, -1<br>
	 * 0 이면, 0
	 * 
	 * @param value 검사할 숫자
	 * @return 부호
	 */
	private static int sign(double value) {
		
		int sign = 0;
		
		if(value > 0) {
			sign = 1;
		} else if(value < 0) {
			sign = -1;
		}
		
		return sign;
	}
	
	/**
	 * target Byte의 첫 부분과 지정한 접두사 일치 여부 반환<br>
	 * 일치할 경우 : true, 일치하지 않을 경우 : false
	 * 
	 * @param target 확인 대상 byte array
	 * @param prefix 접두사 byte array
	 * @return target Byte의 첫 부분과 지정한 접두사 일치 여부
	 */
	public static boolean startsWith(byte[] target, byte[] prefix) throws Exception {
		
		// 입력값 검증
		if(target == null) {
			throw new NullPointerException("target array is null.");
		}

		if(prefix == null) {
			throw new NullPointerException("suffix array is null.");
		}
		
		// target이 접두사보다 작을 경우에는 항상 false 
		if(prefix.length > target.length) {
			return false;
		}
		
		// 각 Byte array의 index번째에 있는 byte가 같은지 확인
		for(int index = 0; index < prefix.length; index++) {
			if(target[index] != prefix[index]) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * target Byte의 끝 부분과 지정한 접미사 일치 여부 반환<br>
	 * 일치할 경우 : true, 일치하지 않을 경우 : false
	 *
	 * @param target 확인 대상 byte array
	 * @param suffix 접미사 byte array
	 * @return target Byte의 끝 부분과 지정한 접미사 일치 여부
	 */
	public static boolean endsWith(byte[] target, byte[] suffix) throws Exception {

		// 입력값 검증
		if(target == null) {
			throw new NullPointerException("target array is null.");
		}

		if(suffix == null) {
			throw new NullPointerException("suffix array is null.");
		}

		// target이 접미사보다 작을 경우에는 항상 false 
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
	 * 목표 배열(target) 내에 찾을 배열(lookup)이 있는지 여부 반환
	 * 
	 * @param target 목표 배열
	 * @param start target의 검색 시작 지점
	 * @param lookup 찾을 배열
	 * @return 목표 배열 내에 찾을 배열이 있는지 여부
	 */
	public static boolean contains(byte[] target, int start, byte[] lookup) throws Exception {
		return indexOf(target, start, lookup) >= 0;
	}
	
	/**
	 * 목표 배열(target) 내에 찾을 배열(lookup)이 있는지 여부 반환
	 * 
	 * @param target 목표 배열
	 * @param lookup 찾을 배열
	 * @return 목표 배열 내에 찾을 배열이 있는지 여부
	 */
	public static boolean contains(byte[] target, byte[] lookup) throws Exception {
		return contains(target, 0, lookup);
	}
	
	/**
	 * 주어진 target byte array를 split 하는 메소드
  	 *
	 * @param target target byte array
	 * @param splitter 구분자 byte array
	 * @param isSplitterInclude 분할된 문자열에 배열의 구분자를 포함할 것인지 여부
	 * @return 구분자에 의해 분리된 결과 목록
	 */
	public static List<byte[]> split(byte[] target, byte[] splitter, boolean isSplitterInclude) throws Exception {

		// parameter null 체크
		if(target == null) {
			throw new NullPointerException("target array is null.");
		}

		if(splitter == null) {
			throw new NullPointerException("splitter array is null.");
		}

		// 구분자에 의해 분리된 결과
		List<byte[]> splitedTarget = new ArrayList<>();
		
		// split이 발견된 곳의 위치 변수
		int index = -1;
		
		// 검사를 시작할 위치 변수
		int start = 0;
		
		do {
			
			// 목표 배열 내 splitter가 발견된 곳의 위치 확인 
			// 찾지 못할 경우 -1 반환
			index = indexOf(target, start, splitter);
			
			// 시작지점(start)부터 발견된 곳(index)의 배열을 splitedTarget에 추가 
			// index가 -1일 경우 시작 지점부터 끝까지 배열을 추가
			int splitLength = 0;
			if(index >= 0) {
				splitLength = index - start + ((isSplitterInclude)?splitter.length:0);
			} else {
				splitLength = target.length - start;
			}
			
			byte[] splitedBytes = new byte[splitLength];
			
			System.arraycopy(target, start, splitedBytes, 0, splitedBytes.length);
			splitedTarget.add(splitedBytes);
			
			// 다음 시작위치 계산(splitter가 발견된 위치 + splitter의 크기)
			start = index + splitter.length;
			
		} while(index >= 0 && start < target.length);
    	
		// 최종 결과 반환
		return splitedTarget;
	}
	
	/**
	 * 주어진 target byte array를 split 하는 메소드<br>
	 * 구분자가 포함 됐을 경우 분리 함
	 *
	 * @param target target byte array
	 * @param split 구분자 byte array
	 * @return 구분자에 의해 분리된 결과 목록
	 */
	public static List<byte[]> split(byte[] target, byte[] split) throws Exception {
		return split(target, split, false);
	}
	
	/**
	 * 여러 Byte array를 합침<br>
	 * srcs[0]: {1, 2}, srcs[1]: {3}, srcs[2]: {4, 5} -> {1, 2, 3, 4, 5}
	 *
	 * @param targets: 합칠 Byte array
	 * @return 합쳐진 Byte array
	 */
	public static byte[] concat(byte[]... srcs) throws Exception {
		
		// parameter null 체크
		if(srcs == null) {
			throw new NullPointerException("source array is null.");
		}
		
		if(srcs.length == 0) {
			return new byte[0];
		}
		
		// 전체 목록의 크기 계산
		int totalSize = 0;
		for(byte[] src: srcs) {
			
			// 합칠 배열이 null일 경우, skip함
			if(src == null) {
				continue;
			}
			
			totalSize += src.length;
		}

		// 합쳐진 Byte 배열 변수
		// 두 Byte array 크기의 합 만큼의 사이즈 할당
		byte[] concatenatedArray = new byte[totalSize];

		// 배열을 합침
		int dstStart = 0;
		for(byte[] src: srcs) {
			
			// 합칠 배열이 null일 경우, skip함
			if(src == null) {
				continue;
			}

			// concatenatedArray에 src 배열 복사
			System.arraycopy(src, 0, concatenatedArray, dstStart, src.length);
			dstStart += src.length;
		}
		
		return concatenatedArray;
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
		
		if(length == 0) {
			return new byte[0];
		}

		byte[] cutArray = new byte[length];
		System.arraycopy(array, start, cutArray, 0, length);
		
		return cutArray;
	}
	
	/**
	 * 문자열을 byte 배열로 변환<br>
	 * ex) str:"1A03", order: OrderType.ASCEND -> byte[] {26, 3}<br>
	 *     str:"1A03", order: OrderType.DESCEND -> byte[] {3, 26}
	 * 
	 * @param str 문자열
	 * @param order 순서
	 * @return 변환된 byte 배열
	 */
	public static byte[] strToBytes(String str, OrderType order) throws Exception {
		
		// 입력값 검증
		if(str == null) {
			throw new NullPointerException("str is null.");
		}
		
		if(str.length() % 2 != 0) {
			throw new Exception("str must be even.");
		}
		
		// 변환된 byte 배열을 담을 변수
		byte[] bytes = new byte[str.length()/2];
		
		for(int index = 0; index < bytes.length; index++) {
			
			// 순서 설정(order)에 따른 저장 위치 변수
			int orderedIndex = index;
			if(order == OrderType.DESCEND) {
				orderedIndex = bytes.length - 1 - index;
			}
			
			// 상위 니블의 데이터를 가져옴
			byte b1 = getByte(str.charAt(index * 2));
			// 왼쪽으로 4 bit를 이동하여 상위 니블로 만듦
			b1 = (byte)(b1 << 4);
			
			// 하위 니블의 데이터를 가져옴
			byte b2 = getByte(str.charAt(index * 2 + 1));

			// 상위 니블(b1)과 하위니블(b2)를 합쳐서 저장
			bytes[orderedIndex] = (byte)(b1 + b2);
		}
		
		// 변환 결과를 반환
		return bytes;
	}
	
	/**
	 * 문자열을 byte 배열로 변환<br>
	 * ex) str:"1A03", order: OrderType.ASCEND(default) -> byte[] {26, 3}
	 * 
	 * @param str 문자열
	 * @return 변환된 byte 배열
	 */
	public static byte[] strToBytes(String str) throws Exception {
		return strToBytes(str, OrderType.ASCEND);
	}
	
	/**
	 * 주어진 문자에 해당하는 byte를 반환하는 메소드
	 * 
	 * @param ch 문자
	 * @return 문자를 byte로 변환한 결과
	 */
	public static byte getByte(char ch) throws Exception {
		
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
	 * byte 배열을 문자열로 변환<br>
	 * ex) byte[] {26, 3}, order: OrderType.ASCEND -> "1A03"<br>
	 *     byte[] {26, 3}, order: OrderType.DESCEND -> "031A"
	 * 
	 * @param bytes byte 배열
	 * @param order 순서
	 * @return 변환된 문자열
	 */
	public static String bytesToStr(byte[] bytes, OrderType order) throws Exception {
		
		if(bytes == null) {
			throw new NullPointerException("bytes is null.");
		}
		
		StringBuilder builder = new StringBuilder("");
		
		for(int index = 0; index < bytes.length; index++) {
			
			// 순서 설정(order)에 따른 참조 위치 변수
			int orderedIndex = index;
			if(order == OrderType.DESCEND) {
				orderedIndex = bytes.length - 1 - index;
			}
			
			// 문자열에 바이트 추가
			builder.append(String.format("%02X", bytes[orderedIndex]));
		}
		
		return builder.toString();
	}
	
	/**
	 * byte 배열을 문자열로 변환<br>
	 * ex) byte[] {26, 3}, order: OrderType.ASCEND(default) -> "1A03"<br>
	 * 
	 * @param bytes byte 배열
	 * @return 변환된 문자열
	 */
	public static String bytesToStr(byte[] bytes) throws Exception {
		return bytesToStr(bytes, OrderType.ASCEND);
	}
	
	/**
	 * byte 배열 끊어 읽기 객체 생성 
	 * 
	 * @param bytes 읽을 byte 배열
	 * @return byte 배열 끊어 읽기 객체 
	 */
	public static ByteChunkReader buildByteChunkReader(byte[] bytes) throws Exception {
		return new ByteChunkReader(bytes);
	}
	
	/**
	 * Byte 배열을 끊어읽기 위한 Reader 클래스
	 * 
	 * @author jmsohn
	 */
	public static class ByteChunkReader {
		
		/** byte 배열 */
		private byte[] bytes;
		/** 현재까지 읽은 위치 */
		private int pos;
		
		/**
		 * 생성자
		 * 
		 * @param bytes 읽을 byte 배열
		 */
		private ByteChunkReader(byte[] bytes) throws Exception {
			
			if(bytes == null) {
				throw new NullPointerException("bytes is null.");
			}
			
			this.bytes = bytes;
			this.pos = 0;
		}
		
		/**
		 * 주어진 바이트 수(n) 만큼 스킵하여 이동<br>
		 * 주어진 바이트 수에 마이너스(-) 값이 가능하지만,<br>
		 * 만일 최종 위치가 0 미만이면 최종 위치를 0으로 고정함
		 * 
		 * @param n 스킵할 바이트 수
		 */
		public synchronized void skip(int n) {
			
			this.pos += n;
			
			if(this.pos < 0) {
				this.pos = 0;
			}
		}
		
		/**
		 * 바이트 배열을 다 읽었는지 여부 반환<br>
		 * 바이트 배열을 다 읽지 않았을 경우 true 반환
		 * 
		 * @return 바이트 배열을 다 읽었는지 여부
		 */
		public boolean hasRemains() {
			return this.pos < this.bytes.length;
		}
		
		/**
		 * 특정 크기 만큼 읽어 반환
		 * 
		 * @param readSize 읽을 크기
		 * @return 읽은 byte 배열
		 */
		public synchronized byte[] readNByte(int readSize) throws Exception {

			// 입력값 검증
			if(readSize <= 0) {
				throw new IllegalArgumentException("read size must be greater than 0:" + readSize);
			}
			
			if(this.pos + readSize > this.bytes.length) {
				throw new Exception("length is exceeded: position:" + this.pos + ", read size:" + readSize + ", byte array length:" + this.bytes.length);
			}
			
			// byte 배열에서 현재 위치(pos)를 기준으로 readSize 만큼 읽음
			byte[] read = BytesUtil.cut(this.bytes, this.pos, readSize);
			
			// 현재 위치를 readSize 만큼 이동
			this.pos += readSize;
			
			// 결과 반환
			return read;
		}
		
		/**
		 * 끊어 읽을 크기 목록 만큼 읽어 반환
		 * ex) bytes = {0x1,0x2,0x3}, chunkSizes = {2, 1}
		 *     --> {0x1,0x2}, {0x3}을 반환
		 * 
		 * @param chunkSizes 끊어 읽을 크기 목록
		 * @return 끊어 읽은 byte 배열의 배열
		 */
		public List<byte[]> readNByte(int[] chunkSizes) throws Exception {
			
			if(chunkSizes == null) {
				throw new NullPointerException("chunkSizes is null.");
			}
			
			List<byte[]> chunkList = new ArrayList<>();
			
			for(int chunkSize: chunkSizes) {
				chunkList.add(this.readNByte(chunkSize));
			}
			
			return chunkList;
		}
		
		/**
		 * 특정 바이트 패턴이 나오기 전까지의 바이트 배열 반환<br>
		 * - 못찾을 경우 null을 반환함
		 * 
		 * @param pattern 찾을 바이트 패턴
		 * @param isInclude 바이트 패턴 포함 여부 true이면, 바이트 패턴 포함하여 반환
		 * @return 바이트 패턴이 나오기 전까지 바이트 배열
		 */
		public synchronized byte[] readUntilMatch(byte[] pattern, boolean isInclude) throws Exception {
			
			// 입력값 검증
			if(pattern == null) {
				throw new NullPointerException("pattern is null.");
			}
			
			if(pattern.length == 0) {
				throw new IllegalArgumentException("pattern lenth is 0.");
			}
			
			// 패턴의 위치를 찾음
			int patternPos = BytesUtil.indexOf(this.bytes, this.pos, pattern);
			
			// 못찾은 경우, null을 반환
			if(patternPos < 0) {
				return null;
			}
			
			// 복사할 바이트 배열 길이 변수
			int length = patternPos - this.pos;
			
			// 바이트 패턴이 포함되어야 할 경우 패턴 길이를 추가
			if(isInclude == true) {
				length += pattern.length;
			} 
			
			// 복사할 바이트 배열 변수
			byte[] read = new byte[length];
			
			// 패턴까지의 바이트 배열을 복사
			System.arraycopy(this.bytes, this.pos, read, 0, length);
			
			// 다음 읽을 위치로 이동
			this.pos = patternPos + 1; 
					
			// 복사한 바이트 배열을 반환
			return read;
		}
		
		/**
		 * 특정 바이트 패턴이 나오기 전까지의 바이트 배열 반환<br>
		 * - 바이트 패턴은 포함하지 않음<br>
		 * - 못찾을 경우 null을 반환함
		 * 
		 * @param pattern 찾을 바이트 패턴
		 * @return 바이트 패턴이 나오기 전까지 바이트 배열
		 */
		public byte[] readUntilMatch(byte[] pattern) throws Exception {
			return this.readUntilMatch(pattern, false);
		}
		
		/**
		 * 특정 바이트 패턴이 나오기 전까지의 바이트 배열 반환<br>
		 * - 못찾을 경우 null을 반환함
		 * 
		 * @param pattern 찾을 바이트 패턴
		 * @param isInclude 바이트 패턴 포함 여부 true이면, 바이트 패턴 포함하여 반환
		 * @return 바이트 패턴이 나오기 전까지 바이트 배열
		 */
		public byte[] readUntilMatch(byte pattern, boolean isInclude) throws Exception {
			return this.readUntilMatch(new byte[] { pattern }, isInclude);
		}
		
		/**
		 * 특정 바이트 패턴이 나오기 전까지의 바이트 배열 반환<br>
		 * - 바이트 패턴은 포함하지 않음<br>
		 * - 못찾을 경우 null을 반환함
		 * 
		 * @param pattern 찾을 바이트 패턴
		 * @return 바이트 패턴이 나오기 전까지 바이트 배열
		 */
		public byte[] readUntilMatch(byte pattern) throws Exception {
			return this.readUntilMatch(new byte[] { pattern }, false);
		}
	}
}
