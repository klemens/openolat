package de.unileipzig.xman.exam.forms;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

import de.unileipzig.xman.exam.Exam;

public class EditRegistrationForm extends FormBasicController {

	Exam exam;

	private DateChooser regStartEdit;
	private DateChooser regEndEdit;
	private DateChooser signOffEdit;
	private MultipleSelectionElement booleanAttributes;

	private static final String EARMARKED = "earmarked";
	private static final String MULTI_SUBSCRIPTION = "multiSubscription";

	public EditRegistrationForm(UserRequest ureq, WindowControl wControl, Exam exam) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		
		this.exam = exam;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		regStartEdit = uifactory.addDateChooser("regStart", "EditRegistrationForm.regStartDate", exam.getRegStartDate(), formLayout);
		regStartEdit.setMandatory(true);
		regStartEdit.setDateChooserTimeEnabled(true);

		regEndEdit = uifactory.addDateChooser("regEnd", "EditRegistrationForm.regEndDate", exam.getRegEndDate(), formLayout);
		regEndEdit.setMandatory(true);
		regEndEdit.setDateChooserTimeEnabled(true);

		signOffEdit = uifactory.addDateChooser("signOff", "EditRegistrationForm.signOffDate", exam.getSignOffDate(), formLayout);
		signOffEdit.setMandatory(true);
		signOffEdit.setExampleKey("EditRegistrationForm.signOffDate.example", null);
		signOffEdit.setDateChooserTimeEnabled(true);

		booleanAttributes = uifactory.addCheckboxesVertical("boolOptions", null, formLayout, new String[] {MULTI_SUBSCRIPTION, EARMARKED},
				new String[] {translate("EditRegistrationForm.multiSubscriptionButton"), translate("EditRegistrationForm.earmarkedButton")}, 1);
		booleanAttributes.select(EARMARKED, exam.getEarmarkedEnabled());
		booleanAttributes.select(MULTI_SUBSCRIPTION, exam.getIsMultiSubscription());
		// only configurable for oral exams
		booleanAttributes.setEnabled(MULTI_SUBSCRIPTION, exam.getIsOral());
		booleanAttributes.setExampleKey("EditRegistrationForm.earmarked.example", null);

		uifactory.addFormSubmitButton("save", "saveButton", formLayout);
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean result = !regStartEdit.isEmpty("EditRegistrationForm.isEmpty")
						&& !regEndEdit.isEmpty("EditRegistrationForm.isEmpty")
						&& !signOffEdit.isEmpty("EditRegistrationForm.isEmpty");

		if (result) {
			if (regEndEdit.getDate().getTime() <= regStartEdit.getDate().getTime()) {
				regEndEdit.setErrorKey("EditRegistrationForm.dateError", null);
				result = false;
			}
			if (signOffEdit.getDate().getTime() < regEndEdit.getDate().getTime()) {
				signOffEdit.setErrorKey("EditRegistrationForm.dateError", null);
				result = false;
			}
		}

		return result;
	}

	/**
	 * @return a Date representing the start of the registration period
	 */
	public Date getRegStart() {
		return regStartEdit.getDate();
	}

	/**
	 * @return a Date representing the end of the registration period
	 */
	public Date getRegEnd() {
		return regEndEdit.getDate();
	}

	/**
	 * @return a Date representing the sign off deadline
	 */
	public Date getSignOff() {
		return signOffEdit.getDate();
	}

	/**
	 * @return true, if the multiSubscription feature was enabled
	 */
	public boolean getMultiSubscription() {
		return booleanAttributes.getSelectedKeys().contains(MULTI_SUBSCRIPTION);
	}

	/**
	 * @return true, if the multiSubscription feature was enabled
	 */
	public boolean getEarmarked() {
		return booleanAttributes.getSelectedKeys().contains(EARMARKED);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_VALIDATION_OK);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
}