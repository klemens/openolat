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
public class MailOutboxActionExtension implements ActionExtension {
	
	private MailContextResolver resolver;
	
	public MailOutboxActionExtension() {
		//
	}
	
	public MailOutboxActionExtension(MailContextResolver resolver) {
		this.resolver = resolver;
	}
	
	
	@Override
	public String getDescription(Locale locale) {
		Translator translator = Util.createPackageTranslator(MailOutboxActionExtension.class, locale);
		return translator.translate("mail.outbox");
	}

	@Override
	public String getActionText(Locale locale) {
		Translator translator = Util.createPackageTranslator(MailOutboxActionExtension.class, locale);
		return translator.translate("mail.outbox.alt");
	}

	@Override
	public Controller createController(UserRequest ureq, WindowControl wControl, Object arg) {
		return MailUIFactory.createOutboxController(ureq, wControl, resolver);
	}
}
