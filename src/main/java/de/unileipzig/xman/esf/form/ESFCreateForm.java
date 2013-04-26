package de.unileipzig.xman.esf.form;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;

import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.studyPath.StudyPath;
import de.unileipzig.xman.studyPath.StudyPathManager;

public class ESFCreateForm extends FormBasicController {

	private TextElement lastName;
	private TextElement firstName;
	private IntegerElement institutionalNumber;
	private TextElement emailAddress;
	private SingleSelection studyPath;
	private Locale loc;
	private Translator translator;
	private String userStudyPath;
	private Identity identity;
	private Submit submit;
	private Cancel cancel;

	/**
	 * 
	 * @param name
	 * @param translator
	 */
	public ESFCreateForm(UserRequest ureq, WindowControl wControl, String name,
			Translator translator, Identity identity) {
		super(ureq, wControl);
		this.loc = I18nManager.getInstance().getLocaleOrDefault(null);
		this.translator = translator;
		this.identity = identity;
		this.setTranslator(new PackageTranslator("de.unileipzig.xman.esf", ureq.getLocale()));
		initForm(ureq);

	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {

		this.lastName = uifactory.addTextElement("lastName",
				"ESFCreateForm.lastName", 50, identity.getUser().getProperty(
						UserConstants.LASTNAME, translator.getLocale()),
				formLayout);
		this.lastName.setMandatory(true);

		this.firstName = uifactory.addTextElement("firstName",
				"ESFCreateForm.firstName", 50, identity.getUser().getProperty(
						UserConstants.FIRSTNAME, translator.getLocale()),
				formLayout);
		this.firstName.setMandatory(true);

		Integer instiNumber = 0;
		try {
			instiNumber = Integer.parseInt(identity.getUser().getProperty(
					UserConstants.INSTITUTIONALUSERIDENTIFIER,
					translator.getLocale()));
		} catch (Exception e) {
			// TODO: handle exception
		}
		this.institutionalNumber = uifactory.addIntegerElement(
				"institutionalNumber", "ESFCreateForm.institutionalNumber",
				instiNumber, formLayout);
		this.institutionalNumber.setMandatory(true);
		// student may not change his/her matrikel number
		if (!this.institutionalNumber.getValue().equals(""))
			this.institutionalNumber.setEnabled(false);

		this.emailAddress = uifactory.addTextElement("emailAddress",
				"ESFCreateForm.emailAddress", 50, identity.getUser()
						.getProperty(UserConstants.INSTITUTIONALEMAIL,
								translator.getLocale()), formLayout);
		this.emailAddress.setMandatory(true);

		this.studyPath = uifactory.addDropdownSingleselect("studyPath",
				"ESFCreateForm.studyPath", formLayout, StudyPathManager
						.getInstance().createKeysArray(), StudyPathManager
						.getInstance().translateKeyArray(loc), null);
		this.studyPath.setMandatory(true);

		// select the studyPath of the User
		userStudyPath = identity.getUser().getProperty(UserConstants.STUDYSUBJECT,
				loc);
		if (!(userStudyPath == null)) {
			if (!userStudyPath.isEmpty()) {
				this.studyPath.select(userStudyPath, true);
			}
		}

		// submit / cancel keys
		submit = uifactory.addFormSubmitButton("save",
				"ESFCreateController.esfCreateForm.submit", formLayout);
		// cancel = uifactory.addFormCancelButton("cancel", null, ureq,
		// getWindowControl());

	}

	/**
	 * 
	 * @see org.olat.core.gui.components.form.Form#validate()
	 */
	public boolean validateFormLogic(UserRequest ureq) {

		boolean valid = !this.emailAddress.isEmpty("ESFCreateForm.empty")
				&& !this.firstName.isEmpty("ESFCreateForm.empty")
				&& !this.institutionalNumber.isEmpty("ESFCreateForm.empty")
				&& !this.lastName.isEmpty("ESFCreateForm.empty");

		// TODO Wird dieser Check standardmäßig ausgeführt?
		// valid = valid
		// && this.institutionalNumber.
		// .isInteger("ESFCreateForm.noInteger");

		ElectronicStudentFile file = ElectronicStudentFileManager.getInstance()
				.retrieveESFByIdentity(this.identity);

		// no esf yet, student wants to create one (no modification)
		if (file == null) {

			// number already taken
			// if (institutionalNumber.isInteger("ESFCreateForm.noInteger")) {
			if (!ElectronicStudentFileManager
					.getInstance()
					.isMatrikelNumberAvailable(
							new Integer(this.institutionalNumber.getIntValue()))) {

				valid = valid && false;
				this.institutionalNumber.setErrorKey(
						"ESFCreateFrom.duplicateESF", null);
			}
			// }
		}

		if (!this.isValidEmailAddress()) {

			this.emailAddress.setErrorKey("ESFCreateForm.noValidEmailAddress",
					null);
			valid = valid && false;
		}

		String choosenStudyPath = this.getStudyPath();
		Translator trans = Util.createPackageTranslator(StudyPath.class, loc);

		// the user has chosen the default studypath, this is not allowed
		if (trans.translate(choosenStudyPath).equals(
				trans.translate(StudyPathManager.DEFAULT_STUDY_PATH))) {

			this.studyPath.setErrorKey("ESFCreateForm.defaultStudyPathChoosen",
					null);
			valid = valid && false;
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

		Pattern p = Pattern
				.compile("[a-z]{3}[0-9]{2}[a-z]{3}@studserv.uni-leipzig.de");
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
		// TODO Auto-generated method stub

	}
}
