package de.unileipzig.xman.esf.site;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.unileipzig.xman.esf.ElectronicStudentFile;

/**
 * Factory class that produces esf sites
 */
public class ElectronicStudentFileSiteDefinition extends AbstractSiteDefinition implements SiteDefinition {
	@Override
	protected SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		if(!ureq.getUserSession().getRoles().isAuthor() &&
				!ureq.getUserSession().getRoles().isInstitutionalResourceManager() &&
				!ureq.getUserSession().getRoles().isOLATAdmin() &&
				!ureq.getUserSession().getRoles().isGuestOnly()) {
			Translator translator = Util.createPackageTranslator(ElectronicStudentFile.class, ureq.getLocale());
			return new ElectronicStudentFileSite(translator.translate("esf.site.title"), this);
		}
		return null;
	}
}
