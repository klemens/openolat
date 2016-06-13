/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.portfolio.sites;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.PortfolioV2Module;

/**
 * 
 * Initial date: 06.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioSiteDef  extends AbstractSiteDefinition implements SiteDefinition {

	@Override
	protected SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		if(ureq.getUserSession().getRoles().isGuestOnly()) {
			return null;
		} else if(StringHelper.containsNonWhitespace(config.getSecurityCallbackBeanId())) {
			return new PortfolioSite(this, ureq.getLocale());
		}
		// only for registered users and invitee but not guests
		return new PortfolioSite(this, ureq.getLocale());
	}
	
	@Override
	public boolean isEnabled() {
		PortfolioV2Module module = CoreSpringFactory.getImpl(PortfolioV2Module.class);
		return module.isEnabled() && super.isEnabled();
	}

	@Override
	public boolean isFeatureEnabled() {
		PortfolioV2Module module = CoreSpringFactory.getImpl(PortfolioV2Module.class);
		return module.isEnabled();
	}
}
