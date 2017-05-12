package de.unileipzig.xman.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;

/**
 * This class represents the factory for the top menu sites.
 * It's bean it loaded automatically by openolat.
 */
public class ExamAdminSiteDef extends AbstractSiteDefinition implements SiteDefinition {
	@Override
	protected SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		if ( ureq.getUserSession().getRoles().isInstitutionalResourceManager() ||  ureq.getUserSession().getRoles().isOLATAdmin()) {
			// only open for olat-usermanagers
			return new ExamAdminSite(ureq.getLocale(), this);
		} 
		return null;
	}

	@Override
	public int getOrder() {
		return 100;
	}
}
