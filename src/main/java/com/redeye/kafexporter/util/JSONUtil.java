package com.redeye.kafexporter.util;


import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 
 * @author jmsohn
 */
public class JSONUtil {
	
	/**
	 * 
	 * 
	 * @param map
	 * @return
	 */
	public static String toJSON(Map<?, ?> map) {
		
		// 입력 값 검증
		if(map == null) {
			return "{}";
		}
		
		// 생성할 json 객체
		StringBuffer json = new StringBuffer("{");
		
		// 첫 번째 항목 여부
		// 중간에 ","를 넣기 위함
		boolean isFirst = true;
		
		for(Object key: map.keySet()) {
			
			// 중간에 "," 추가
			if(isFirst == false) {
				json.append(", ");
			}
			
			//
			json
				.append('"')
				.append(key.toString())
				.append("\": ")
				.append(getJSONValue(map.get(key)));
			
			isFirst = false;
		}
		
		json.append("}");
		
		return json.toString();
	}
	
	/**
	 * 
	 * 
	 * @param list
	 * @return
	 */
	public static String toJSON(List<?> list) {
		
		// 입력 값 검증
		if(list == null) {
			return "[]";
		}
		
		// 생성할 json 객체
		StringBuffer json = new StringBuffer("[");
		
		// 첫 번째 항목 여부
		// 중간에 ","를 넣기 위함
		boolean isFirst = true;
		
		for(Object value: list) {
			
			// 중간에 "," 추가
			if(isFirst == false) {
				json.append(", ");
			}
			
			//
			json.append(getJSONValue(value));
			
			isFirst = false;
		}
		
		json.append("]");
		
		return json.toString();
	}
	
	/**
	 * 
	 * 
	 * @param set
	 * @return
	 */
	public static String toJSON(Set<?> set) {
		
		// 입력 값 검증
		if(set == null) {
			return "[]";
		}
		
		return toJSON(List.of(set.toArray()));
	}
	
	/**
	 * 
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static String getJSONValue(Object obj) {
		
		if(obj == null) {
			return "null";
		}
		
		Class<?> type = obj.getClass();
		
		if(Map.class.isAssignableFrom(type) == true) {
			
			return toJSON((Map)obj);
			
		} else if(List.class.isAssignableFrom(type) == true) {
			
			return toJSON((List)obj);
			
		} else if(Set.class.isAssignableFrom(type) == true) {
			
			return toJSON((Set)obj);
			
		} else {
			
			if(TypeUtil.isPrimitive(type) == true) {
				
				String value = obj.toString();
				
				if(value.equals("NaN") == true) {	// Not a Number 처리
					return "null";
				} else {
					return value;
				}
				
			} else {
				return '"' + obj.toString() + '"';
			}
		}
	}
}
