package de.unileipzig.shibboleth;

import java.util.Locale;

import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.login.auth.AuthenticationController;

public class SimpleShibbolethLoginFormController extends AuthenticationController {
	
	private VelocityContainer mainVC;
	private IdentityProviderSelectionForm ipSelection;

	public SimpleShibbolethLoginFormController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = new VelocityContainer("simpleShibbolethLoginFormController", getClass(), "loginForm", getTranslator(), this);
		
		ipSelection = new IdentityProviderSelectionForm(ureq, wControl, "", getTranslator(), SimpleShibbolethManager.getInstance().getAvailableIdentityProviders().toArray(new String[0]));
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
			String loginUri = SimpleShibbolethManager.getInstance().getLoginUrl(ipSelection.getIdentityProvider());
			DispatcherModule.redirectTo(ureq.getHttpResp(), loginUri);
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
}
