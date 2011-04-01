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
package org.olat.core.util.mail;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  24 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http.//www.frentix.com
 */
public class MailModule extends AbstractOLATModule {
	
	private static final String INTERN_MAIL_SYSTEM = "internSystem";
	private static final String RECEIVE_REAL_MAIL_USER_DEFAULT_SETTING = "receiveRealMailUserDefaultSetting";
	
	private boolean internSystem;
	private boolean receiveRealMailUserDefaultSetting;
	
	private WebappHelper webappHelper;
	
	public MailModule() {
		//make Spring happy
	}
	
	/**
	 * [used by Spring]
	 * @param webappHelper
	 */
	public void setWebappHelper(WebappHelper webappHelper) {
		this.webappHelper = webappHelper;
	}
	
	/**
	 * [used by Spring]
	 * @see org.olat.core.configuration.AbstractOLATModule#setPersistedProperties(org.olat.core.configuration.PersistedProperties)
	 */
	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	@Override
	public void init() {
		String internSystemValue = getStringPropertyValue(INTERN_MAIL_SYSTEM, true);
		if(StringHelper.containsNonWhitespace(internSystemValue)) {
			internSystem = "true".equalsIgnoreCase(internSystemValue);
		}
		
		String receiveRealMailUserDefaultSettingValue = getStringPropertyValue(RECEIVE_REAL_MAIL_USER_DEFAULT_SETTING, true);
		if(StringHelper.containsNonWhitespace(receiveRealMailUserDefaultSettingValue)) {
			receiveRealMailUserDefaultSetting = "true".equalsIgnoreCase(receiveRealMailUserDefaultSettingValue);
		}
	}

	@Override
	protected void initDefaultProperties() {
		internSystem = getBooleanConfigParameter(INTERN_MAIL_SYSTEM, false);
		receiveRealMailUserDefaultSetting = getBooleanConfigParameter(RECEIVE_REAL_MAIL_USER_DEFAULT_SETTING, true);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	/**
	 * Used the intern mail system.
	 * @return
	 */
	public boolean isInternSystem() {
		return internSystem;
	}
	
	/**
	 * 
	 * @param internSystem
	 */
	public void setInterSystem(boolean internSystem) {
		this.internSystem = internSystem;
	}
	
	/**
	 * Users can receive real e-mail too. This setting is the default for
	 * users. They can change it in Preferences Panel.
	 * @return
	 */
	public boolean isReceiveRealMailUserDefaultSetting() {
		return receiveRealMailUserDefaultSetting;
	}

	/**
	 * Check if the mail host is configured
	 * @return
	 */
	public boolean isMailHostEnabled() {
		String mailhost = WebappHelper.getMailConfig("mailhost");
		return (StringHelper.containsNonWhitespace(mailhost) && !mailhost.equalsIgnoreCase("disabled"));
	}
}