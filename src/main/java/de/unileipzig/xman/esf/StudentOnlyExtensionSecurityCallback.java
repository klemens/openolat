package de.unileipzig.xman.esf;

import org.olat.core.extensions.action.ActionExtensionSecurityCallback;
import org.olat.core.gui.UserRequest;

/**
 * Description:<br>
 * hide ESFLaunchController for all uses with more rights
 */
public class StudentOnlyExtensionSecurityCallback implements ActionExtensionSecurityCallback {

	/**
	 * Only show to normal users = students
	 * @see org.olat.core.extensions.action.ActionExtensionSecurityCallback#isAllowedToLaunchActionController(org.olat.core.gui.UserRequest)
	 */
	@Override
	public boolean isAllowedToLaunchActionController(UserRequest ureq) {
		if(ureq.getUserSession().getRoles().isAuthor() ||
			ureq.getUserSession().getRoles().isInstitutionalResourceManager() ||
			ureq.getUserSession().getRoles().isOLATAdmin() ||
			ureq.getUserSession().getRoles().isGuestOnly())
			return false;
		else
			return true;
	}

}
