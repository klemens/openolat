package de.unileipzig.xman.admin.mail.form;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.util.Util;

import de.unileipzig.xman.exam.Exam;

/**
 * Needed because the internal ContactFormController does not
 * allow access to the subject or body the user entered.
 */
public class MailForm extends FormBasicController {
	private String recipient;
	private TextElement subjectElem;
	private TextElement bodyElem;
	private MultipleSelectionElement copyToSender;
	private String initialSubject;
	private String sender;

	public MailForm(UserRequest ureq, WindowControl wControl, String sender, String recipient, String initialSubject) {
		super(ureq, wControl);
		
		this.recipient = recipient;
		this.initialSubject = initialSubject;
		this.sender = sender;
		setTranslator(Util.createPackageTranslator(Exam.class, getLocale()));

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("recipientsElem", "MailForm.recipient", recipient, formLayout);
		uifactory.addStaticTextElement("senderElem", "MailForm.sender", sender, formLayout);

		subjectElem = uifactory.addTextElement("subjectElem", "MailForm.subject", -1, "", formLayout);
		subjectElem.preventValueTrim(true);
		subjectElem.setValue(initialSubject);
		subjectElem.setDisplaySize(60);
		subjectElem.setMandatory(true);

		bodyElem = uifactory.addTextAreaElement("bodyelem", "MailForm.body", -1, 15, 60, true, "", formLayout);
		bodyElem.setMandatory(true);

		copyToSender = uifactory.addCheckboxesHorizontal("copyToSender", null, formLayout, new String[] {"copyToSender"}, new String[] {translate("MailForm.copyToSender")});

		uifactory.addFormSubmitButton("send", "MailForm.send", formLayout);
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		return !subjectElem.isEmpty("MailForm.noSubject") && !bodyElem.isEmpty("MailForm.noBody");
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
		return subjectElem.getValue();
	}

	public String getBody() {
		return bodyElem.getValue();
	}

	public boolean getCopyToSender() {
		return copyToSender.isSelected(0);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
}
