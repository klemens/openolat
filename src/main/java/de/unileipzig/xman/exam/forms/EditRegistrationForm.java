package de.unileipzig.xman.exam.forms;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.unileipzig.xman.exam.Exam;

/**
 * 
 * @author
 */
public class EditRegistrationForm extends FormBasicController {

	private DateChooser regStart;
	private DateChooser regEnd;
	private DateChooser signOff;
	private Submit submit;
	Date regStartDate;
	Date regEndDate;
	Date signOffDate;

	/**
	 * creates the editRegistrationForm
	 * 
	 * @param name
	 *            the name of the form
	 * @param translator
	 *            the translator
	 * @param regStartDate
	 *            the start of the registration
	 * @param regEndDate
	 *            the end of the registratio
	 * @param signOffDate
	 *            the end of the unsubscription time
	 */
	public EditRegistrationForm(UserRequest ureq, WindowControl wControl,
			String name, Translator translator, Date regStartDate,
			Date regEndDate, Date signOffDate) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		
		this.regStartDate = regStartDate;
		this.regEndDate = regEndDate;
		this.signOffDate = signOffDate;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		Calendar future = Calendar.getInstance(ureq.getLocale());
		future.setTime(new Date()); future.add(Calendar.MONTH, 1);
		
		Date now = new Date();
		Date inAMonth = future.getTime();
		
		regStart = uifactory.addDateChooser("regStart", "EditRegistrationForm.regStartDate", now, formLayout);
		regStart.setMandatory(true);
		regStart.setDisplaySize(20);
		regStart.setMaxLength(16);
		regStart.setDateChooserTimeEnabled(true);
		if (regStartDate != null)
			regStart.setDate(regStartDate);

		regEnd = uifactory.addDateChooser("regEnd", "EditRegistrationForm.regEndDate", inAMonth, formLayout);
		regEnd.setMandatory(true);
		regEnd.setDisplaySize(20);
		regEnd.setMaxLength(16);
		regEnd.setDateChooserTimeEnabled(true);
		if (regEndDate != null)
			regEnd.setDate(regEndDate);

		signOff = uifactory.addDateChooser("signOff", "EditRegistrationForm.signOffDate", inAMonth, formLayout);
		signOff.setMandatory(true);
		signOff.setExampleKey("EditRegistrationForm.signOffDate.example", null);
		signOff.setDisplaySize(20);
		signOff.setMaxLength(16);
		signOff.setDateChooserTimeEnabled(true);
		if (signOffDate != null)
			signOff.setDate(signOffDate);

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		submit = uifactory.addFormSubmitButton("save", "saveButton", buttonGroupLayout);
	}

	/**
	 * @see Form#validate()
	 */
	public boolean validateFormLogic(UserRequest ureq) {

		boolean validate = false;

		validate = !regStart.isEmpty("EditRegistrationForm.isEmpty")
				&& !regEnd.isEmpty("EditRegistrationForm.isEmpty")
				&& !signOff.isEmpty("EditRegistrationForm.isEmpty");

		if (validate) {

			if (regEnd.getDate().getTime() <= regStart.getDate().getTime()) {

				regEnd.setErrorKey("EditRegistrationForm.dateError", null);
				validate = false;
			}
			if (signOff.getDate().getTime() < regEnd.getDate().getTime()) {

				signOff.setErrorKey("EditRegistrationForm.dateError", null);
				validate = false;
			}
		}

		return validate;
	}

	/**
	 * @return a Date representing the start of the registration period
	 */
	public Date getRegStart() {

		return regStart.getDate();
	}

	/**
	 * @return a Date representing the end of the registration period
	 */
	public Date getRegEnd() {

		return regEnd.getDate();
	}

	/**
	 * @return a Date representing the sign off deadline
	 */
	public Date getSignOff() {

		return signOff.getDate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_VALIDATION_OK);

	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}
}