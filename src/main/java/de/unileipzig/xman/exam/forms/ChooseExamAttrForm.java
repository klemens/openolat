package de.unileipzig.xman.exam.forms;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.unileipzig.xman.exam.Exam;

/**
 * form to create a new exam
 * 
 * @author iggy
 */
public class ChooseExamAttrForm extends FormBasicController {

	private SingleSelection examTypeSwitchElem;
	private Submit submit;

	/**
	 * default constructor
	 * 
	 * @param name
	 *            name of the form
	 * @param translator
	 *            package translator
	 */
	public ChooseExamAttrForm(UserRequest ureq, WindowControl wControl,
			String name, Translator translator) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		// type of exam (written / oral)
		String[] values = new String[] { translate("written"), translate("oral") };
		String[] keys = new String[] { Exam.EXAM_TYPE_WRITTEN,
				Exam.EXAM_TYPE_ORAL };

		examTypeSwitchElem = uifactory.addRadiosVertical("examType",
				"ChooseExamAttrForm.radioButton", formLayout, keys, values);
		examTypeSwitchElem.select(Exam.EXAM_TYPE_WRITTEN, true);

		// submit / cancel keys
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		submit = uifactory.addFormSubmitButton("save", "submitKey", buttonGroupLayout);
	}

	/**
	 * @see Form#validate()
	 */
	public boolean validate() {
		return true;
	}

	/**
	 * @return the selected value (Exam.EXAM_TYPE_WRITTEN or
	 *         Exam.EXAM_TYPE_ORAL)
	 */
	public String getType() {

		return examTypeSwitchElem.getKey(examTypeSwitchElem.getSelected());
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
