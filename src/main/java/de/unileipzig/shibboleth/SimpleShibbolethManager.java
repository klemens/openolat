package de.unileipzig.shibboleth;

import org.springframework.beans.factory.annotation.Value;

public class SimpleShibbolethManager {
	@Value("${simpleShibboleth.path:shibDefault}")
	private String dispatcherPath;

	protected SimpleShibbolethManager() {
		_beanInstance = this;
	}

	/**
	 * Return the /path/ at which the dispatcher lives
	 */
	public String getDispatcherPath() {
		return dispatcherPath;
	}

	/**
	 * This is only needed because there is currently no way
	 * to inject the manager into the AuthenticationController,
	 * because the factory used to create it (AuthenticationProvider)
	 * does not support injection and is final (only private constructors).
	 */
	static SimpleShibbolethManager _beanInstance;
	public static SimpleShibbolethManager getInstance() {
		return _beanInstance;
	}
}
