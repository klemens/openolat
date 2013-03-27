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

import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.module.Module;
import de.unileipzig.xman.module.ModuleManager;

/**
 * form to create a new exam
 * 
 * @author iggy
 */
public class ChooseExamAttrForm extends FormBasicController {

	private SingleSelection examTypeSwitchElem;
	private SingleSelection module;
	private Submit submit;
	private Cancel cancel;

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

		// module, exam belongs to
		List<Module> moduleList = ModuleManager.getInstance().findAllModules();
		values = new String[moduleList.size()];
		keys = new String[moduleList.size()];
		for (int i = 0; i < moduleList.size(); i++) {
			values[i] = moduleList.get(i).getName();
			keys[i] = new Integer(i).toString();
		}

		module = uifactory.addDropdownSingleselect("module",
				"ChooseExamAttrForm.module", formLayout, keys, values, null);
		module.setMandatory(true);

		// submit / cancel keys
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		submit = uifactory.addFormSubmitButton("save", "submitKey", buttonGroupLayout);
		cancel = uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
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

	/**
	 * @return the selected value (the name of the chosen module)
	 */
	public String getModule() {

		return module.getValue(module.getSelected());
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
