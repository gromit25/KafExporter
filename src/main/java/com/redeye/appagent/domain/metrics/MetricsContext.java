package com.redeye.appagent.domain.metrics;

import java.lang.instrument.Instrumentation;
import java.util.List;

import com.redeye.appagent.Context;
import com.redeye.appagent.loader.APILoader;

/**
 * 
 * 
 * @author jmsohn
 */
public class MetricsContext implements Context {

	@Override
	public void init() {
	}

	@Override
	public void addTransformer(Instrumentation inst) {
	}

	@Override
	public List<Object> getWebControllerList() {
		return List.of();
	}

	@Override
	public List<APILoader> getAPILoaderList() {
		return List.of();
	}
}
