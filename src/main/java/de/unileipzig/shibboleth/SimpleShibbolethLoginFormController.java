package de.unileipzig.shibboleth;

import java.util.Locale;

import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.WebappHelper;
import org.olat.login.auth.AuthenticationController;

public class SimpleShibbolethLoginFormController extends AuthenticationController {
	
	private VelocityContainer mainVC;
	private Link redirectButton;
	
	private static String path;

	public SimpleShibbolethLoginFormController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = new VelocityContainer("simpleShibbolethLoginFormController", getClass(), "loginForm", getTranslator(), this);
		
		redirectButton = LinkFactory.createButton("simpleShibboleth.redirectButton", mainVC, this);
		redirectButton.setPrimary(true);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == redirectButton) {
			DispatcherModule.redirectTo(ureq.getHttpResp(), WebappHelper.getServletContextPath() + "/" + path + "/");
		}
	}

	@Override
	public void changeLocale(Locale newLocale) {
		// We only provide one button, so this is not really relevant
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
	
	public static void setPath(String newPath) {
		path = newPath;
	}

}
