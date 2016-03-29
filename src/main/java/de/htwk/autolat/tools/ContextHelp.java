package de.htwk.autolat.tools;

import java.util.Locale;

import org.olat.admin.user.tools.UserTool;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.help.HelpLinkSPI;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("autolatHelp")
public class ContextHelp implements HelpLinkSPI {
	private final static String PREFIX = "autolat:";

	@Value("${autolat.help.fallback:ooConfluenceLinkHelp}")
	private String fallbackProviderId;
	private HelpLinkSPI fallbackProvider;

	@Override
	public String getURL(Locale locale, String page) {
		if(page.startsWith(PREFIX)) {
			String fragment = page.substring(PREFIX.length());
			return StaticMediaDispatcher.getStaticURI("msg/autolat-help.html#" + fragment);
		} else {
			return getFallbackProvider().getURL(locale, page);
		}
	}

	@Override
	public Component getHelpPageLink(UserRequest ureq, String title, String tooltip, String iconCSS, String elementCSS, String page) {
		if(page.startsWith(PREFIX)) {
			ExternalLink helpLink = new ExternalLink("topnav.help." + page);
			helpLink.setName(title);
			helpLink.setTooltip(tooltip);
			helpLink.setIconLeftCSS(iconCSS);
			helpLink.setElementCssClass(elementCSS);
			helpLink.setTarget("autolathelp");
			helpLink.setUrl(getURL(ureq.getLocale(), page));
			return helpLink;
		} else {
			return getFallbackProvider().getHelpPageLink(ureq, title, tooltip, iconCSS, elementCSS, page);
		}
	}

	@Override
	public UserTool getHelpUserTool(WindowControl wControl) {
		// The main documentation always uses the fallback provider
		return getFallbackProvider().getHelpUserTool(wControl);
	}

	private HelpLinkSPI getFallbackProvider() {
		if(fallbackProvider == null) {
			fallbackProvider = (HelpLinkSPI) CoreSpringFactory.getBean(fallbackProviderId);
		}

		return fallbackProvider;
	}
}
