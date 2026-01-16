package com.redeye.kafexporter;

import java.lang.instrument.Instrumentation;

import com.redeye.kafexporter.acquisitor.KafkaAcquisitor;
import com.redeye.kafexporter.http.HttpExporter;

/**
 * 
 * 
 * @author jmsohn
 */
public class KafExporter {
	
	/**
	 * 
	 * 
	 * @param args
	 * @param inst
	 */
	public static void premain(String args, Instrumentation inst) {
		
		KafkaAcquisitor.init(inst);
		
		try {
			
			HttpExporter server = new HttpExporter(5551);
			server.start();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
