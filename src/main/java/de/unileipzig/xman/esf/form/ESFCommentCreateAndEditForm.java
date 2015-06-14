package de.unileipzig.xman.esf.form;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;

/**
 * 
 * Description:<br>
 * TODO: gerb Class Description for ESFCommentCreateForm
 * 
 * <P>
 * Initial Date: 28.05.2008 <br>
 * 
 * @author gerb
 */
public class ESFCommentCreateAndEditForm extends FormBasicController {

	// the input box for the comment
	private RichTextElement comment;
	private String defaultText;

	/**
	 * 
	 * @param name
	 * @param translator
	 */
	public ESFCommentCreateAndEditForm(UserRequest ureq,
			WindowControl wControl, String name, Translator translator,
			String defaultText) {
		super(ureq, wControl);
		this.setTranslator(new PackageTranslator("de.unileipzig.xman.esf", ureq.getLocale()));
		this.defaultText = defaultText;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		comment = uifactory.addRichTextElementForStringDataMinimalistic("comment", "ESFCommentCreateForm.comment", "", -1, -1, formLayout, getWindowControl());
		comment.setMandatory(true);
		if (!defaultText.isEmpty())
			comment.setValue(defaultText);

		// submit / cancel keys
		uifactory.addFormSubmitButton("save", "save", formLayout);
	}

	/**
	 * 
	 * @see org.olat.core.gui.components.form.Form#validate()
	 */
	public boolean validateFormLogic(UserRequest ureq) {

		return true;
	}

	public String getComment() {

		return this.comment.getValue();
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
