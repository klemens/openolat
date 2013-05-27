package de.unileipzig.xman.exam.forms;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

import de.unileipzig.xman.exam.Exam;

/**
 * 
 * @author
 */
public class EditCommentsForm extends FormBasicController {

	Translator translator;
	// TODO: WikiMarkupTextAreaElement wirklich durch Textelement ersetzen oder
	// doch durch RichTextElement? Selbiges in allen anderen Forms mit
	// Kommentarfeldern.
	TextElement commentsWikiArea;
	private Submit submit;
	private Cancel cancel;
	String comments;

	/**
	 * creates the edit comments form
	 * 
	 * @param name
	 *            the name of the form
	 * @param translator
	 *            the translator
	 * @param comments
	 *            the comments
	 */
	public EditCommentsForm(UserRequest ureq, WindowControl wControl,
			String name, Translator translator, String comments) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		
		this.comments = comments;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		commentsWikiArea = uifactory.addTextAreaElement("comments",
				"EditCommentsForm.commentsWikiArea", 1024, 7, 70, true,
				comments, formLayout);

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		submit = uifactory.addFormSubmitButton("save", "saveButton", buttonGroupLayout);
	}

	/**
	 * @see org.olat.core.gui.components.form.Form#validate()
	 */
	public boolean validate() {

		return true;
	}

	/**
	 * @return a String representing the entered comments.
	 */
	public String getComments() {

		return commentsWikiArea.getValue();
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
