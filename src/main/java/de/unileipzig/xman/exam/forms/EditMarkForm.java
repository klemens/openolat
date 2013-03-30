package de.unileipzig.xman.exam.forms;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.util.Util;

import de.unileipzig.xman.exam.Exam;

/**
 * 
 * @author
 */
public class EditMarkForm extends FormBasicController {

	private TextElement grade;
	private Submit submit;
	private Cancel cancel;

	/**
	 * creates the edit mark form
	 * 
	 * @param name
	 *            the name of the form
	 * @param translator
	 *            the translator
	 */
	public EditMarkForm(UserRequest ureq, WindowControl wControl, String name,
			Translator translator) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		/*
		 * points = new TextElement("EditMarkForm.points", 4);
		 * points.setMandatory(true); points.setExample("EditMarkForm.example");
		 * this.addFormElement("points", points);
		 */

		grade = uifactory.addTextElement("mark", "EditMarkForm.grade", 50, "",
				formLayout);
		grade.setMandatory(true);
		grade.setExampleKey("EditMarkForm.example", null);
		grade.showExample(true);

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		submit = uifactory.addFormSubmitButton("save", "saveButton", buttonGroupLayout);
		cancel = uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}

	/**
	 * @see Form#validate()
	 */
	public boolean validateFormLogic(UserRequest ureq) {

		// !points.isEmpty(translator.translate("EditMarkForm.empty")) &&
		return !grade.isEmpty("EditMarkForm.error");
	}

	/**
	 * @return the to double parsed value of the input
	 */
	public String getGrade() {

		return grade.getValue();
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
