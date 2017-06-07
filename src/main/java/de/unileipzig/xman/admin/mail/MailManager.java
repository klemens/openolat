package de.unileipzig.xman.admin.mail;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailBundle;
import org.springframework.beans.factory.annotation.Autowired;

public class MailManager {
	@Autowired
	org.olat.core.util.mail.MailManager mailer;
	
	public static MailManager getInstance() {
		return CoreSpringFactory.getImpl(MailManager.class);
	}

	/**
	 * This method sends an email with the given subject and body to the choosen identity.
	 * @param body the body of the email
	 * @param subject the subject of the email
	 * @param from sender
	 * @param to recipient
	 * @return true if the mail was successfully delivered to the recipients
	 */
	public boolean sendEmail(String subject, String body, Identity to) {
		return sendEmail(subject, body, null, to, null);
	}

	/**
	 * This method sends an email with the given subject and body to the choosen identity.
	 * @param body the body of the email
	 * @param subject the subject of the email
	 * @param from sender
	 * @param to recipient
	 * @return true if the mail was successfully delivered to the recipients
	 */
	public boolean sendEmail(String subject, String body, Identity from, Identity to) {
		return sendEmail(subject, body, from, to, null);
	}

	/**
	 * This method sends an email with the given subject and body to the choosen identity.
	 * @param body the body of the email
	 * @param subject the subject of the email
	 * @param from sender
	 * @param to recipient
	 * @param cc carbon copy
	 * @return true if the mail was successfully delivered to the recipients
	 */
	public boolean sendEmail(String subject, String body, Identity from, Identity to, Identity cc) {
		if(to == null && cc == null) {
			throw new IllegalArgumentException("To and Cc both null");
		}

		MailBundle mail = new MailBundle();
		if(from != null) {
			mail.setFromId(from);
		}
		if(to != null) {
			mail.setToId(to);
		}
		if(cc != null) {
			mail.setToId(cc);
		}
		mail.setContent(subject, body);
		
		return mailer.sendExternMessage(mail, null, false).isSuccessful();
	}
}