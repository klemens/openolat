package de.unileipzig.shibboleth;

import java.util.Locale;

import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.WebappHelper;
import org.olat.login.auth.AuthenticationController;

public class SimpleShibbolethLoginFormController extends AuthenticationController {
	
	private VelocityContainer mainVC;
	private IdentityProviderSelectionForm ipSelection;
	
	private static String path;

	public SimpleShibbolethLoginFormController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = new VelocityContainer("simpleShibbolethLoginFormController", getClass(), "loginForm", getTranslator(), this);
		
		ipSelection = new IdentityProviderSelectionForm(ureq, wControl, "", getTranslator(), new String[]{ "Universität" });
		listenTo(ipSelection);
		mainVC.put("simpleShibboleth.ipSelection", ipSelection.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == ipSelection && event == Event.DONE_EVENT) {
			DispatcherModule.redirectTo(ureq.getHttpResp(), WebappHelper.getServletContextPath() + "/" + path + "/");
		}
	}

	@Override
	public void changeLocale(Locale newLocale) {
		setLocale(newLocale, true);
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(ipSelection);
	}
	
	public static void setPath(String newPath) {
		path = newPath;
	}
}
