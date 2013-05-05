package de.unileipzig.xman.esf.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;

import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.studyPath.StudyPathManager;

public class ESFCreateForm extends FormBasicController {

	private TextElement lastName;
	private TextElement firstName;
	private IntegerElement institutionalNumber;
	private TextElement emailAddress;
	private SingleSelection studyPath;
	private String userStudyPath;
	private User user;

	/**
	 * 
	 * @param name
	 * @param translator
	 */
	public ESFCreateForm(UserRequest ureq, WindowControl wControl, String name, Translator translator, User user) {
		super(ureq, wControl);
		this.user = user;
		setTranslator(Util.createPackageTranslator(ElectronicStudentFile.class, ureq.getLocale()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		lastName = uifactory.addTextElement("lastName", "ESFCreateForm.lastName", 50, user.getProperty(UserConstants.LASTNAME, null), formLayout);
		lastName.setMandatory(true);

		firstName = uifactory.addTextElement("firstName", "ESFCreateForm.firstName", 50, user.getProperty(UserConstants.FIRSTNAME, null), formLayout);
		firstName.setMandatory(true);

		Integer instiNumber = 0;
		try {
			instiNumber = Integer.parseInt(user.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null));
		} catch (NumberFormatException e) {
			// keep 0 as standard
		}
		institutionalNumber = uifactory.addIntegerElement("institutionalNumber", "ESFCreateForm.institutionalNumber", instiNumber, formLayout);
		institutionalNumber.setMandatory(true);
		if(instiNumber > 0) {
			// student may not change already set institutional number
			institutionalNumber.setEnabled(false);
		}

		emailAddress = uifactory.addTextElement("emailAddress", "ESFCreateForm.emailAddress", 50, user.getProperty(UserConstants.INSTITUTIONALEMAIL, null), formLayout);
		emailAddress.setMandatory(true);
		// initialize email with user@studserv.uni-leipzig.de
		if(emailAddress.isEmpty())
			emailAddress.setValue(ureq.getIdentity().getName() + "@studserv.uni-leipzig.de");
		if(!emailAddress.isEmpty()) {
			// student may not change already set mail
			emailAddress.setEnabled(false);
		}
		
		String[] studyPaths = StudyPathManager.getInstance().getAllStudyPathsAsString();
		studyPath = uifactory.addDropdownSingleselect("studyPath", "ESFCreateForm.studyPath", formLayout, studyPaths, studyPaths, null);
		studyPath.setMandatory(true);

		// select the studyPath of the User
		userStudyPath = user.getProperty(UserConstants.STUDYSUBJECT, null);
		if (userStudyPath != null && !userStudyPath.isEmpty() && Arrays.asList(studyPaths).contains(userStudyPath)) {
			studyPath.select(userStudyPath, true);
		}

		// submit key
		uifactory.addFormSubmitButton("save", "ESFCreateController.esfCreateForm.submit", formLayout);
	}

	/**
	 * 
	 * @see org.olat.core.gui.components.form.Form#validate()
	 */
	public boolean validateFormLogic(UserRequest ureq) {
		boolean valid = !emailAddress.isEmpty("ESFCreateForm.empty")
						&& !firstName.isEmpty("ESFCreateForm.empty")
						&& !institutionalNumber.isEmpty("ESFCreateForm.empty")
						&& !lastName.isEmpty("ESFCreateForm.empty");

		// Check if valid institutional number given
		// TODO could be more extensive
		if(institutionalNumber.getIntValue() <= 0) {
			institutionalNumber.setErrorKey("ESFCreateForm.empty", null);
			valid = false;
		}
		
		// no esf yet, student wants to create one (no modification)
		if (null == ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(ureq.getIdentity())) {
			// number already taken
			if (!ElectronicStudentFileManager.getInstance().isMatrikelNumberAvailable(this.institutionalNumber.getIntValue())) {
				institutionalNumber.setErrorKey("ESFCreateFrom.duplicateESF", null);
				valid = false;
			}
		}

		if (!isValidEmailAddress()) {
			emailAddress.setErrorKey("ESFCreateForm.noValidEmailAddress", null);
			valid = false;
		}

		// the user has chosen the default studypath, this is not allowed
		if (studyPath.getSelectedKey().equals(StudyPathManager.DEFAULT_STUDY_PATH)) {
			studyPath.setErrorKey("ESFCreateForm.defaultStudyPathChoosen", null);
			valid = false;
		}

		return valid;
	}

	public String getLastName() {

		return this.lastName.getValue();
	}

	public String getFirstName() {

		return this.firstName.getValue();
	}

	public String getInstitutionalIdentifier() {

		return this.institutionalNumber.getValue();
	}

	public String getInstitutionalEmail() {

		return this.emailAddress.getValue();
	}

	public String getStudyPath() {

		return this.studyPath.getSelectedKey();
	}

	public boolean isValidEmailAddress() {
		String address = this.getInstitutionalEmail();

		Pattern p = Pattern.compile("[a-z]{3}[0-9]{2}[a-z]{3}@studserv.uni-leipzig.de");
		Matcher m = p.matcher(address);
		return m.matches();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_VALIDATION_OK);

	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_FORM_CANCELLED);
	}

	@Override
	protected void doDispose() {
		// nothing to do here
	}
}
