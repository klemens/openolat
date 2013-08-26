package de.unileipzig.UserInfoInterceptor;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.olat.user.UserManager;
// HGG 2012-04-17 logging via OLog added
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;


/**
 * Description: A mandatory form called after a user logs in to make
 * ldap-generated users add their first name, last name and institutional
 * identifier (german: "Matrikelnummer")
 * 
 * @author Sascha Vinz
 */
public class UserInfoController extends FormBasicController implements
		SupportsAfterLoginInterceptor {

	private TextElement firstName;
	private TextElement lastName;
	private TextElement ID;
	private Submit submit;        
        private static OLog log = Tracing.createLoggerFor(UserInfoController.class);

	public UserInfoController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		//
		// calls our initForm(formlayout,listener,ureq) with default values.
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose(boolean)
	 * 
	 *      I'll just keep that here. Never change a running system.
	 */
	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		// this method is called if the form has validated
		// which means that all form items are filled without error
		// and all complex business rules validated also to true.
		//
		// the form values are now read out and persisted
		User user = ureq.getIdentity().getUser();
		user.setProperty(UserConstants.FIRSTNAME, firstName.getValue());
		user.setProperty(UserConstants.LASTNAME, lastName.getValue());
		user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, ID.getValue());
		user.setProperty(UserConstants.INSTITUTIONALNAME, "Universität Leipzig");
		UserManager.getInstance().updateUser(user);
		DBFactory.getInstance(true).intermediateCommit();
		fireEvent(ureq, Event.DONE_EVENT);

	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		/*
		 * create a form with a title and 3 input fields to enter the missing
		 * personal data
		 */
		User user = ureq.getIdentity().getUser();
		setFormTitle("text");
		final int defaultDisplaySize = 20;
		firstName = uifactory.addTextElement("firstname",
				"IDForm.firstnameField", 20, user.getProperty(
						UserConstants.FIRSTNAME, null), formLayout);
		firstName.setDisplaySize(defaultDisplaySize);
		firstName.setNotEmptyCheck("IDForm.mustbefilled");
		firstName.setMandatory(true);
		firstName.setEnabled(true);

		lastName = uifactory.addTextElement("lastname", "IDForm.surnameField",
				20, user.getProperty(UserConstants.LASTNAME, null), formLayout);
		lastName.setDisplaySize(defaultDisplaySize);
		lastName.setNotEmptyCheck("IDForm.mustbefilled");
		lastName.setMandatory(true);
		lastName.setEnabled(true);

		ID = uifactory.addTextElement("instID", "IDForm.idField", 10, user
				.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null),
				formLayout);
		ID.setDisplaySize(defaultDisplaySize);
		ID.setNotEmptyCheck("IDForm.mustbefilled");
		ID.setMandatory(true);
		ID.setEnabled(true);

		submit = new FormSubmit("submit", "IDForm.OK");
		formLayout.add(submit);
	}

	/**
	 * A new method of SupportsAfterLoginInterceptor, encapsulates checking if
	 * the call for this is really necessary. The fields are checked for null
	 * and for just being empty strings.
	 */
	public boolean isInterceptionRequired(UserRequest ureq) {
		Roles usersRoles = ureq.getUserSession().getRoles();
		User user = ureq.getIdentity().getUser();
		if (usersRoles.isAuthor() || usersRoles.isGroupManager()
				|| usersRoles.isInstitutionalResourceManager()
				|| usersRoles.isOLATAdmin() || usersRoles.isUserManager()) {
			if (log.isDebug()) logReasonForFailedCall(ureq, true);
			return false;
		} else if ((user.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER,
				null) == null)
				|| (user.getProperty(UserConstants.FIRSTNAME, null) == null)
				|| (user.getProperty(UserConstants.LASTNAME, null) == null)) {
			return true;
		} else if ((user.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER,
				null).isEmpty())
				|| (user.getProperty(UserConstants.FIRSTNAME, null).isEmpty())
				|| (user.getProperty(UserConstants.LASTNAME, null).isEmpty())) {
			return true;
		} else {
			if (log.isDebug()) logReasonForFailedCall(ureq, false);
			return false;
		}
	}

	/**
	 * Internal method to log the reason for not calling the interceptor.
	 */
	private void logReasonForFailedCall(UserRequest ureq,
			boolean itsBecauseOfHisRoles) {
		Roles usersRoles = ureq.getUserSession().getRoles();
		User user = ureq.getIdentity().getUser();
		String reason = "Alle Informationen sind bereits im System.";
		if (itsBecauseOfHisRoles) {
			if (usersRoles.isAuthor()) {
				reason = "Der Nutzer hat u.a. die Rolle Author.";
			} else if (usersRoles.isGroupManager()) {
				reason = "Der Nutzer ist u.a. Lerngruppenmanager.";
			} else if (usersRoles.isInstitutionalResourceManager()) {
				reason = "Der Nutzer ist u.a. Institutionsressourcenverwalter.";
			} else if (usersRoles.isOLATAdmin()) {
				reason = "Der Nutzer ist u.a. Administrator.";
			} else if (usersRoles.isUserManager()) {
				reason = "Der Nutzer ist u.a. Benutzerverwalter.";
			} else {
				reason = "Schwerwiegender Fehler des Programmierers.";
			}
		}
		log.debug("LoginInterceptor: Nutzer "
			  + ureq.getIdentity().getName()
			  + " \nVorname: " + user.getProperty(UserConstants.FIRSTNAME, null)
			  + " \nNachname: "+ user.getProperty(UserConstants.LASTNAME, null)
			  + " \nMatrikelnummer: "
			  + user.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null)
			  + " \nRollen: "  + usersRoles.toString()
			  + " \nwurde nicht zum Nachtragen fehlender Informationen aufgefordert."
			  + " \nGrund: " + reason, null);
	}

}
