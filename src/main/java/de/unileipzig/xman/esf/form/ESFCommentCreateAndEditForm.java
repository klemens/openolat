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
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;

import de.unileipzig.xman.comment.CommentEntry;

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
	private TextElement comment;
	private Submit submit;
	private Cancel cancel;
	private CommentEntry entry;

	/**
	 * 
	 * @param name
	 * @param translator
	 */
	public ESFCommentCreateAndEditForm(UserRequest ureq,
			WindowControl wControl, String name, Translator translator,
			CommentEntry entry) {
		super(ureq, wControl);
		this.entry=entry;
		this.setTranslator(new PackageTranslator("de.unileipzig.xman.esf", ureq.getLocale()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		comment = uifactory.addTextAreaElement("comment",
				"ESFCommentCreateForm.comment", 1024, 6, 4, true, "", formLayout);
		comment.setMandatory(true);
		if (entry != null)
			comment.setValue(entry.getComment());

		// submit / cancel keys
		submit = uifactory.addFormSubmitButton("save", "save", formLayout);
		// cancel = uifactory.addFormCancelButton("cancel", null, ureq,
		// getWindowControl());

	}

	/**
	 * 
	 * @see org.olat.core.gui.components.form.Form#validate()
	 */
	public boolean validateFormLogic(UserRequest ureq) {

		return !comment.isEmpty("ESFCommentCreateForm.isEmpty");
	}

	public String getComment() {

		return this.comment.getValue();
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
