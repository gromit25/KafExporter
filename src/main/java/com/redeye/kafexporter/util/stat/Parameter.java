package com.redeye.kafexporter.util.stat;

import java.util.List;

import lombok.Getter;

/**
 * 실시간 모수(Parameter) 계산 클래스
 * 
 * @author jmsohn
 */
public class Parameter {
	
	/** 데이터의 개수 */
	@Getter
	private int count;
	
	/** 합계 */
	@Getter
	private double sum;
	/** 제곱합(표준편차 계산용) */
	@Getter
	private double squaredSum;
	/** 세제곱 합(왜도 계산용) */
	@Getter
	private double cubedSum;
	/** 네제곱 합(첨도 계산용) */
	@Getter
	private double fourthPoweredSum;
	
	/** 평균 값 */
	private double mean;
	/** 분산 */
	private double variance;
	/** 왜도 */
	private double skewness;
	/** 첨도 */
	private double kurtosis;
	
	/** 최소 값 */
	private double min;
	/** 최대 값 */
	private double max;

	
	/**
	 * 생성자
	 */
	public Parameter() {
		this.reset();
	}
	
	/**
	 * 생성자
	 *  
	 * @param sum 합계
	 * @param squaredSum 제곱합(표준편차 계산용)
	 * @param cubedSum 세제곱 합(왜도 계산용)
	 * @param fourthPoweredSum 네제곱 합(첨도 계산용)
	 * @param count 데이터의 개수
	 */
	public Parameter(double sum, double squaredSum, double cubedSum, double fourthPoweredSum, int count) throws Exception {
		
		// 입력값 검증
		if(count <= 0) {
			throw new IllegalArgumentException("count must be greater than 0:" + count); 
		}
		
		//---- 데이터 개수 설정
		this.count = count;
		
		//---- 모수 계산을 위한 설정
		this.sum = sum;
		this.squaredSum = squaredSum;
		this.cubedSum = cubedSum;
		this.fourthPoweredSum = fourthPoweredSum;
		
		//---- 설정된 값으로 모수를 계산
		this.calParameter();
	}

	/**
	 * 모수 값 초기화
	 */
	public synchronized void reset() {

		this.count = 0;
		
		this.sum = 0.0;
		this.squaredSum = 0.0;
		this.cubedSum = 0.0;
		this.fourthPoweredSum = 0.0;
		
		this.mean = 0.0;
		this.variance = 0.0;
		this.skewness = 0.0;
		this.kurtosis = 0.0;
		
		this.min = Double.MAX_VALUE;
		this.max = Double.MIN_VALUE;
	}
	
	/**
	 * 데이터 배열 추가
	 * 
	 * @param values 추가할 데이터 배열
	 */
	public void addAll(double... values) {
		for(double value: values) {
			this.add(value);
		}
	}
	
	/**
	 * 데이터 배열 추가
	 * 
	 * @param values 추가할 데이터 배열
	 */
	public void addAll(List<Double> values) {
		for(Double value: values) {
			this.add(value);
		}
	}
	
	/**
	 * 데이터 추가
	 * 
	 * @param value 추가할 데이터
	 * @param squaredValue 추가할 데이터의 제곱값
	 */
	public synchronized void add(double value) {
		
		//----
		double squaredValue = value * value;
		double cubedValue = squaredValue * value;
		double fourthPoweredValue = cubedValue * value;
		
		//---- 데이터 초기화
		this.count++;
		
		//---- 모수 계산을 위한 값 설정
		this.sum += value;
		this.squaredSum += squaredValue;
		this.cubedSum += cubedValue;
		this.fourthPoweredSum += fourthPoweredValue;
		
		//---- 설정된 값으로 모수를 계산
		this.calParameter();
		
		//---- 최소값, 최대값 설정
		if(this.min > value) {
			this.min = value;
		}
		
		if(this.max < value) {
			this.max = value;
		}
	}
	
	/**
	 * 객체에 설정된 값으로 모수 계산
	 */
	private void calParameter() {
		
		//---- 데이터 개수 초기화
		double n = (double)this.count;
		if(n == 0) return;	// 데이터 개수가 0이면 계산하지 않고 반환
		
		//---- 모수 계산 수행
		this.mean = this.sum/n;
		double squaredMean = this.mean * this.mean;
		double cubedMean = squaredMean * this.mean;
		double fourthPoweredMean = cubedMean * this.mean;
		
		this.variance = (this.squaredSum/n) - squaredMean;

		// 분산이 0 이면, 첨도와 왜도 0으로 설정
		// 아닐 경우 첨도 및 왜도 계산
		if(this.variance == 0) {
			 
			this.skewness = 0;
			this.kurtosis = 0;
			
		} else {
			
			this.skewness =
				(
					(this.cubedSum/n)
					- cubedMean
					- (3 * this.mean * this.variance)
				)
				/ Math.pow(this.variance, 3.0/2.0);
			this.kurtosis =
				(
					(this.fourthPoweredSum/n)
					- (4 * this.mean * this.cubedSum / n)
					+ (6 * squaredMean * this.variance)
					+ (3 * fourthPoweredMean)
				)
				/ (this.variance * this.variance) - 3;
		}
	}
	
	/**
	 * 평균 반환
	 * 
	 * @return 평균
	 */
	public double getMean() {
		
		if(this.count == 0) {
			return Double.NaN;
		}
		
		return this.mean;
	}
	
	/**
	 * 분산 반환
	 * 
	 * @return 분산
	 */
	public double getVariance() {
		
		if(this.count == 0) {
			return Double.NaN;
		}
		
		return this.variance;
	}
	
	/**
	 * 표준 편차 반환
	 * 
	 * @return 표준 편차
	 */
	public double getStd() {
		
		if(this.count == 0) {
			return Double.NaN;
		}

		return Math.sqrt(this.variance);
	}

	/**
	 * 왜도 반환
	 * 
	 * @return 왜도
	 */
	public double getSkewness() {
		
		if(this.count == 0) {
			return Double.NaN;
		}
		
		return this.skewness;
	}
	
	/**
	 * 첨도 반환
	 * 
	 * @return 첨도
	 */
	public double getKurtosis() {
		
		if(this.count == 0) {
			return Double.NaN;
		}
		
		return this.kurtosis;
	}
	
	/**
	 * 최소 값 반환
	 * 
	 * @return 최소 값
	 */
	public double getMin() {
		
		if(this.count == 0) {
			return Double.NaN;
		}
		
		return this.min;
	}

	/**
	 * 최대 값 반환
	 * 
	 * @return 최대 값
	 */
	public double getMax() {
		
		if(this.count == 0) {
			return Double.NaN;
		}
		
		return this.max;
	}
	
	/**
	 * 현재 객체를 json 문자열 형태로 반환
	 */
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder("");
		
		builder
			.append("{").append("\r\n")
			.append("\"count\":").append(this.count).append("\r\n")
			.append("\"sum\":").append(this.sum).append("\r\n")
			.append("\"mean\":").append(this.getMean()).append("\r\n")
			.append("\"variance\":").append(this.getVariance()).append("\r\n")
			.append("\"std\":").append(this.getStd()).append("\r\n")
			.append("\"skewness\":").append(this.getSkewness()).append("\r\n")
			.append("\"kurtosis\":").append(this.getKurtosis()).append("\r\n")
			.append("\"min\":").append(this.getMin()).append("\r\n")
			.append("\"max\":").append(this.getMax()).append("\r\n")
			.append("}");
		
		return builder.toString();
	}
}
