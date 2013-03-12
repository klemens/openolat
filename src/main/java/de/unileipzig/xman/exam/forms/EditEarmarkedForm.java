package de.unileipzig.xman.exam.forms;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * @author
 */
public class EditEarmarkedForm extends FormBasicController {

	private FormToggle box;
	private Submit submit;
	private Cancel cancel;

	/**
	 * creates the editEarmarkedForm
	 * 
	 * @param name
	 *            the name of the form
	 * @param translator
	 *            the translator
	 * @param earmarked
	 *            true, if the earmarked feature is enabled
	 */
	public EditEarmarkedForm(UserRequest ureq, WindowControl wControl,
			String name, Translator translator, boolean earmarked) {
		super(ureq, wControl);

		box = uifactory.addToggleButton("EditEarmarkedForm.box", null, null,
				null, null);
		if (earmarked) {
			box.toggleOn();
		} else {
			box.toggleOff();
		}

		// submit / cancel keys
		submit = uifactory.addFormSubmitButton("save", "saveButton", null);
		cancel = uifactory.addFormCancelButton("cancel", null, ureq,
				getWindowControl());

	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.olat.core.gui.components.form.Form#validate()
	 */
	public boolean validate() {

		return true;
	}

	/**
	 * @return true, if the earmarked feature was enabeled
	 */
	public boolean getEarmarked() {

		return box.isOn();
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
