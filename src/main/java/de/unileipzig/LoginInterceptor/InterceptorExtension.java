package de.unileipzig.LoginInterceptor;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.login.AfterLoginConfig;
import org.olat.login.AfterLoginInterceptionManager;


/**
 * Entry point for the InterceptorExtension. Provides Methods for Spring to instantiate
 * the Controller according to the xml file.
 * 
 * @author Sascha Vinz
 */

public class InterceptorExtension extends AbstractOLATModule {
	private final AfterLoginInterceptionManager Al;
	private AfterLoginConfig afterLoginConfig;

	public InterceptorExtension(final AfterLoginInterceptionManager Al) {
		this.Al = Al;
	}

	public AfterLoginInterceptionManager getAl() {
		return Al;
	}

	public AfterLoginConfig getAfterLoginConfig() {
		return afterLoginConfig;
	}

	public void setAfterLoginConfig(AfterLoginConfig afterLoginConfig) {
		this.afterLoginConfig = afterLoginConfig;

	}

	@Override
	public void init() {
		if (afterLoginConfig != null) {
			Al.addAfterLoginControllerConfig(afterLoginConfig);
		}

	}

	@Override
	protected void initDefaultProperties() {

	}

	@Override
	protected void initFromChangedProperties() {

	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;

	}
	
	
	
	public String lala(){
		
		return "lala";
	}
}
