package de.unileipzig.UserInfoInterceptor;

import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.olat.user.UserManager;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * A form to request the name of the user if missing.
 * Also supports request of student numbers in different ways.
 */
public class UserInfoController extends FormBasicController implements SupportsAfterLoginInterceptor {
	private static boolean enabled = false;
	private static String studentNumberStatus = "ignore";
	private static String studentNumberCheck = "";
	
	private static OLog log = Tracing.createLoggerFor(UserInfoController.class);

	private TextElement firstNameField;
	private TextElement lastNameField;
	private TextElement emailField;
	private TextElement studentNumberField;

	public UserInfoController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		User user = ureq.getIdentity().getUser();
		
		String firstName = user.getProperty(UserConstants.FIRSTNAME, null);
		String lastName = user.getProperty(UserConstants.LASTNAME, null);
		String email = user.getProperty(UserConstants.EMAIL, null);
		String studentNumber = user.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
		if(firstName == null) firstName = "";
		if(lastName == null) lastName = "";
		if(email == null) email = "";
		if(studentNumber == null) studentNumber = "";

		firstNameField = uifactory.addTextElement("firstname", "UserInfoController.firstName", 40, firstName, formLayout);
		firstNameField.setNotEmptyCheck("UserInfoController.emptyFirstName");
		firstNameField.setMandatory(true);

		lastNameField = uifactory.addTextElement("lastname", "UserInfoController.lastName", 40, lastName, formLayout);
		lastNameField.setNotEmptyCheck("UserInfoController.emptyLastName");
		lastNameField.setMandatory(true);

		emailField = uifactory.addTextElement("email", "UserInfoController.email", 100, email, formLayout);
		emailField.setRegexMatchCheck(".+@.+", "UserInfoController.emailWrong");
		emailField.setMandatory(true);

		// show student number input only for students
		if(isStudent(ureq) && !"ignore".equals(studentNumberStatus)) {
			studentNumberField = uifactory.addTextElement("studentnumber", "UserInfoController.studentNumber", 10, studentNumber, formLayout);
			if("require".equals(studentNumberStatus)) {
				studentNumberField.setNotEmptyCheck("UserInfoController.emptyStudentNumber");
				studentNumberField.setMandatory(true);
			}
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", "UserInfoController.submit", buttonLayout);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		// validate student number (only for students)
		if(isStudent(ureq) && !"ignore".equals(studentNumberStatus)) {
			String studentNumber = studentNumberField.getValue().trim();
			
			if(!studentNumberCheck.isEmpty() && !studentNumber.isEmpty()) {
				try {
					if(!studentNumber.matches(studentNumberCheck)) {
						studentNumberField.setErrorKey("UserInfoController.studentNumberWrong", null);
						return false;
					}
				} catch(PatternSyntaxException e) {
					log.error("The configured regex '" + studentNumberCheck + "' is syntactically wrong", e);
				}
			}
		}
		
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// get fresh user instance (prevent hibernate issues)
		User user = UserManager.getInstance().loadUserByKey(ureq.getIdentity().getUser().getKey());
		
		user.setProperty(UserConstants.FIRSTNAME, firstNameField.getValue().trim());
		user.setProperty(UserConstants.LASTNAME, lastNameField.getValue().trim());
		user.setProperty(UserConstants.EMAIL, emailField.getValue().trim());
		if(isStudent(ureq) && !"ignore".equals(studentNumberStatus)) {
			user.setProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, studentNumberField.getValue().trim());
		}
		
		UserManager.getInstance().updateUser(user);
		
		// signal completion
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	/**
	 * Checks if the logged in user is a student by checking if he is not a
	 * admin, guest, author, resource manager, user manager or group manager
	 */
	private boolean isStudent(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		if(!roles.isGuestOnly() && !roles.isOLATAdmin() && !roles.isAuthor() && !roles.isUserManager() &&
				!roles.isInstitutionalResourceManager() && !roles.isGroupManager()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Requests interception if the name is missing or - if configured via
	 * userInfoInterceptor.studentNumber - if the student number is missing
	 * and the user in not admin or author
	 */
	@Override
	public boolean isUserInteractionRequired(UserRequest ureq) {
		if(!enabled) {
			return false;
		}
		
		User user = ureq.getIdentity().getUser();

		// do not ask guests
		if(ureq.getUserSession().getRoles().isGuestOnly()) {
			return false;
		}
		
		// name missing
		String firstName = user.getProperty(UserConstants.FIRSTNAME, null);
		String lastName = user.getProperty(UserConstants.LASTNAME, null);
		if(firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
			return true;
		}

		// email missing
		String email = user.getProperty(UserConstants.EMAIL, null);
		if(email == null || email.isEmpty()) {
			return true;
		}

		// student number missing (only for students)
		String identifier = user.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
		if(isStudent(ureq) && "require".equals(studentNumberStatus) && (identifier == null || identifier.isEmpty())) {
			return true;
		}
		
		return false;
	}
	
	@Override
	protected void doDispose() {
		// nothing to dispose
	}
	
	/**
	 * Spring setter to configure the interceptor
	 * @param enabled Enable of disable the interceptor
	 * @param studentNumberStatus One of ignore, ask and require
	 * @param studentNumberCheck regex to check the supplied student number
	 */
	public static void setSettings(boolean enabled, String studentNumberStatus, String studentNumberCheck) {
		UserInfoController.enabled = enabled;
		UserInfoController.studentNumberCheck = studentNumberCheck;
		
		if(Arrays.asList("ignore", "ask", "require").contains(studentNumberStatus)) {
			UserInfoController.studentNumberStatus = studentNumberStatus;
		} else {
			log.error("Given student number configuration '" + studentNumberStatus + "' is invalid. (valid options: ignore, ask, require)");
		}
	}

}
