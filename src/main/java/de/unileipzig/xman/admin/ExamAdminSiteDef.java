package de.unileipzig.xman.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;

/**
 * This class represents the factory for the top menu sites.
 * It's bean it loaded automatically by openolat.
 */
public class ExamAdminSiteDef implements SiteDefinition {
	public SiteInstance createSite(UserRequest ureq, WindowControl arg1) {
		SiteInstance si = null;
		if ( ureq.getUserSession().getRoles().isInstitutionalResourceManager() ||  ureq.getUserSession().getRoles().isOLATAdmin()) {
			// only open for olat-usermanagers
			si = new ExamAdminSite(ureq.getLocale());
		} 
		return si;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public int getOrder() {
		return 100;
	}
}
