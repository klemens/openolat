package de.unileipzig.xman.esf.site;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteInstance;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;

import de.unileipzig.xman.esf.controller.ESFLaunchController;

/**
 * This represents a permanent tab at the top of the portal that contains the esf of the user
 */
public class ElectronicStudentFileSite extends AbstractSiteInstance implements SiteInstance {
	private NavElement origNavElem;
	private NavElement curNavElem;

	public ElectronicStudentFileSite(String title, SiteDefinition siteDef) {
		super(siteDef);

		origNavElem = new DefaultNavElement(title, title, "o_site_esf");
		curNavElem = new DefaultNavElement(origNavElem);
	}

	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}

	@Override
	protected Controller createController(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		return new ESFLaunchController(ureq, wControl);
	}
}
