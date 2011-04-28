/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.resource.accesscontrol;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;

/**
 * 
 * Description:<br>
 * Module for access control of OLAT Resource
 * 
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AccessControlModule extends AbstractOLATModule implements ConfigOnOff {
	
	public static final String AC_ENABLED = "resource.accesscontrol.enabled";
	public static final String AC_HOME_ENABLED = "resource.accesscontrol.home.overview";
	
	public static final String TOKEN_ENABLED = "method.token.enabled";
	public static final String FREE_ENABLED = "method.free.enabled";

	private boolean enabled;
	private boolean freeEnabled;
	private boolean tokenEnabled;
	private boolean homeOverviewEnabled;
	
	private final List<AccessMethodHandler> methodHandlers = new ArrayList<AccessMethodHandler>();
	
	@Override
	public void init() {
		//module enabled/disabled
		String enabledObj = getStringPropertyValue(AC_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String tokenEnabledObj = getStringPropertyValue(TOKEN_ENABLED, true);
		if(StringHelper.containsNonWhitespace(tokenEnabledObj)) {
			tokenEnabled = "true".equals(tokenEnabledObj);
		}
		
		String freeEnabledObj = getStringPropertyValue(FREE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(freeEnabledObj)) {
			freeEnabled = "true".equals(freeEnabledObj);
		}
		
		String homeEnabledObj = getStringPropertyValue(AC_HOME_ENABLED, true);
		if(StringHelper.containsNonWhitespace(homeEnabledObj)) {
			homeOverviewEnabled = "true".equals(homeEnabledObj);
		}

		logInfo("Access control module is enabled: " + Boolean.toString(enabled));
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	@Override
	protected void initDefaultProperties() {
		enabled = getBooleanConfigParameter(AC_ENABLED, true);
		freeEnabled = getBooleanConfigParameter(FREE_ENABLED, true);
		tokenEnabled = getBooleanConfigParameter(TOKEN_ENABLED, true);
		homeOverviewEnabled = getBooleanConfigParameter(AC_HOME_ENABLED, true);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		if(this.enabled != enabled) {
			setStringProperty(AC_ENABLED, Boolean.toString(enabled), true);
		}
	}

	public boolean isTokenEnabled() {
		return tokenEnabled;
	}

	public void setTokenEnabled(boolean tokenEnabled) {
		if(this.tokenEnabled != tokenEnabled) {
			setStringProperty(TOKEN_ENABLED, Boolean.toString(tokenEnabled), true);
		}
	}

	public boolean isFreeEnabled() {
		return freeEnabled;
	}

	public void setFreeEnabled(boolean freeEnabled) {
		if(this.freeEnabled != freeEnabled) {
			setStringProperty(FREE_ENABLED, Boolean.toString(freeEnabled), true);
		}
	}
	
	public boolean isHomeOverviewEnabled() {
		return homeOverviewEnabled;
	}

	public void setHomeOverviewEnabled(boolean homeOverviewEnabled) {
		if(this.homeOverviewEnabled != homeOverviewEnabled) {
			setStringProperty(AC_HOME_ENABLED, Boolean.toString(homeOverviewEnabled), true);
		}
	}

	public List<AccessMethodHandler> getMethodHandlers() {
		return new ArrayList<AccessMethodHandler>(methodHandlers);
	}
	
	public void setMethodHandlers(List<AccessMethodHandler> handlers) {
		if(handlers != null) {
			methodHandlers.addAll(handlers);
		}
	}
	
	public void addMethodHandler(AccessMethodHandler handler) {
		if(handler != null) {
			methodHandlers.add(handler);
		}
	}
	
	public void removeMethodHandler(AccessMethodHandler handler) {
		if(handler != null) {
			methodHandlers.remove(handler);
		}
	}

	public AccessMethodHandler getAccessMethodHandler(String type) {
		for(AccessMethodHandler handler:methodHandlers) {
			if(handler.getType().equals(type)) {
				return handler;
			}
		}
		return null;
	}
}