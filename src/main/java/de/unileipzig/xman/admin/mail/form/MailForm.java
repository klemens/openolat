package de.unileipzig.xman.admin.mail.form;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.unileipzig.xman.exam.Exam;

public class MailForm extends FormBasicController {

	private TextElement subjectElem;
	private TextElement bodyElem;

	public MailForm(UserRequest ureq, WindowControl wControl, String name,
			Translator translator) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));

		initForm(ureq);

	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		subjectElem = uifactory.addTextElement("subjectElem",
				"MailForm.subject", 128, "", formLayout);
		subjectElem.setDisplaySize(60);
		subjectElem.setMandatory(true);

		bodyElem = uifactory.addTextAreaElement("bodyelem", "MailForm.body",
				1024, 15, 60, true, "", formLayout);
		bodyElem.setMandatory(true);

		// submit / cancel keys
		uifactory.addFormSubmitButton("save", "MailForm.send", formLayout);
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {

		return !this.subjectElem.isEmpty("MailForm.noSubject")
				&& !this.bodyElem.isEmpty("MailForm.noBody");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_VALIDATION_OK);

	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_FORM_CANCELLED);
	}

	public String getSubject() {

		return this.subjectElem.getValue();
	}

	public String getBody() {

		return this.bodyElem.getValue();
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}
}
