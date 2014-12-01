package de.unileipzig.xman.exam.forms;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

import de.unileipzig.xman.exam.Exam;

public class EditDescriptionForm extends FormBasicController {

	private String comments;

	private RichTextElement descriptionEditor;

	public EditDescriptionForm(UserRequest ureq, WindowControl wControl, String comments) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		
		this.comments = comments;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		descriptionEditor = uifactory.addRichTextElementForStringDataMinimalistic("comments", "EditDescriptionForm.description", comments, 10, -1, formLayout, getWindowControl());

		uifactory.addFormSubmitButton("save", "saveButton", formLayout);
	}

	/**
	 * @return a String representing the entered comments.
	 */
	public String getDescription() {
		return descriptionEditor.getValue();
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
