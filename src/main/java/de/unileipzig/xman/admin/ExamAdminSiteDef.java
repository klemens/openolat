package de.unileipzig.xman.admin;

import java.util.List;

import org.olat.core.extensions.ExtensionResource;
// import org.olat.core.extensions.OLATExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;

/**
 * 
 * @author
 */
public class ExamAdminSiteDef implements SiteDefinition { // alt: implements also OLATExtension
	
	/**
	 * constructor
	 */
	public ExamAdminSiteDef(){
		super();
	}

	/**
	 * creates the new site definition
	 * @param ureq the UserRequest
	 * @param arg1 the window control
	 */
	public SiteInstance createSite(UserRequest ureq, WindowControl arg1) {
		SiteInstance si = null;
		if ( ureq.getUserSession().getRoles().isInstitutionalResourceManager() ||  ureq.getUserSession().getRoles().isOLATAdmin()) {
			// only open for olat-usermanagers
			si = new ExamAdminSite(ureq.getLocale());
		} 
		return si;
	}

	/**
	 * nothing to do here
	 * @return null
	 */
	public ExtensionResource getExtensionCSS() {
		return null;
	}

	/**
	 * nothing to do here
	 * @return null
	 */
	public List getExtensionResources() {
		return null;
	}

	/**
	 * gets the name
	 * @return "examAdminSite" (String)
	 */
	public String getName() {
		return "examAdminSite";
	}

	/**
	 * nothing to do here
	 */
	public void setup() {}

	/**
	 * nothing to do here
	 */
	public void tearDown() {}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 1;
	}

}
