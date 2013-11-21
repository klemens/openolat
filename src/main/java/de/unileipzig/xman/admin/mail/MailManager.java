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
	 * @param identity recipient
	 * @return true if the mail was successfully delivered to the recipients
	 */
	public boolean sendEmail(String subject, String body, Identity identity) {
		MailBundle mail = new MailBundle();
		mail.setToId(identity);
		mail.setContent(subject, body);
		
		return mailer.sendExternMessage(mail, null).isSuccessful();
	}
}