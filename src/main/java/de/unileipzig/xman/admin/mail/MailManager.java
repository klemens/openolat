package de.unileipzig.xman.admin.mail;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;

/**
 * 
 * @author
 */
public class MailManager {
		
	private static MailManager INSTANCE = null;

	private MailManager() {
		// singleton
	}
	
	static { INSTANCE = new MailManager(); }
	
	/**
	 * @return Singleton.
	 */
	public static MailManager getInstance() { return INSTANCE; }

	/**
	 * this methode creates a new email template to the given body and subject.
	 * 
	 * @param body the body of the email
	 * @param subject the subject of the mail
	 * @return the mailTemplate
	 */
	public MailTemplate createEmail(String body, String subject, final Map<String, String> variables) {
		
		MailTemplate mailTemplate = new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				
//				String[] keys = (String[]) variables.keySet().toArray();
//				
//				for (int i = 0; i < variables.size(); i++ ) {
//					
//					context.put(keys[i], variables.get(i));
//				}
			}
		};
		return mailTemplate;
	}
	
	/**
	 * This method sends an email with the given subject and body to the choosen identity.
	 * for the translator use this: {1} exam name {2} first name {3} app.date {4} app.place {5} app.duration {6} exam.typ  
	 * @param body the body of the email
	 * @param subject the subject of the email
	 * @param identityList the list of identities
	 * @return true if the mail was successfully delivered to the recipients
	 */
	public boolean sendEmail(String subject, String body, Identity identity) {
		
		MailerResult result = null;
		if ( identity != null) {
			result = MailerWithTemplate.getInstance().sendMail(null,identity, null, null,
					this.createEmail(body, subject, new HashMap<String,String>()), null);
		}		
		// unused at the moment
		if ( result.getReturnCode() == MailerResult.OK ) {
			return true;
		} 
		else return false;
	}
	
}