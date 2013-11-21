package de.unileipzig.shibboleth;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowSettings;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultChiefController;
import org.olat.core.gui.control.Event;
import org.olat.core.util.WebappHelper;

/**
 * This controller can show an error message in a stand alone html page
 * @author Klemens Schölhorn
 */
public class SimpleShibbolethErrorController extends DefaultChiefController {
	/**
	 * @see SimpleShibbolethErrorController
	 * @param ureq
	 * @param message The message to display
	 * @param detail More details (can be null)
	 */
	public SimpleShibbolethErrorController(UserRequest ureq, String message, String detail) {
		Window window = Windows.getWindows(ureq).getWindowManager().createWindowBackOffice("shibbolethError", this, new WindowSettings()).getWindow();

		VelocityContainer mainVC = new VelocityContainer("shibbolethError", SimpleShibbolethErrorController.class, "errorWindow", null, this);
		mainVC.contextPut("cssURL", window.getGuiTheme().getBaseURI() + "layout.css");
		mainVC.contextPut("backLink", WebappHelper.getServletContextPath());
		mainVC.contextPut("message", message);
		if(detail != null) {
			mainVC.contextPut("", detail);
		}

		window.setContentPane(mainVC);
		setWindow(window);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
}
