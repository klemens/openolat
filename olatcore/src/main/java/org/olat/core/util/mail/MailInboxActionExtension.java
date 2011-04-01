package org.olat.core.util.mail;

import java.util.Locale;

import org.olat.core.extensions.action.ActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ui.MailContextResolver;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  25 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailInboxActionExtension implements ActionExtension {
	
	private MailContextResolver resolver;
	
	public MailInboxActionExtension() {
		//
	}
	
	public MailInboxActionExtension(MailContextResolver resolver) {
		this.resolver = resolver;
	}
	
	@Override
	public String getDescription(Locale locale) {
		Translator translator = Util.createPackageTranslator(MailInboxActionExtension.class, locale);
		return translator.translate("mail.inbox");
	}

	@Override
	public String getActionText(Locale locale) {
		Translator translator = Util.createPackageTranslator(MailInboxActionExtension.class, locale);
		return translator.translate("mail.inbox.alt");
	}

	@Override
	public Controller createController(UserRequest ureq, WindowControl wControl, Object arg) {
		return MailUIFactory.createInboxController(ureq, wControl, resolver);
	}
}
