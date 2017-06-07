package de.unileipzig.xman.admin.controller;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;

import de.unileipzig.xman.admin.ExamAdminSite;

public class ExamAdminESFMainController extends BasicController {

	private BreadcrumbedStackedPanel stackPanel;
	private ExamAdminESFController esfController;

	protected ExamAdminESFMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		setTranslator(Util.createPackageTranslator(ExamAdminSite.class, ureq.getLocale()));

		stackPanel = new BreadcrumbedStackedPanel("stack", getTranslator(), this);
		stackPanel.setShowCloseLink(true, false);
		putInitialPanel(stackPanel);

		esfController = new ExamAdminESFController(ureq, getWindowControl(), stackPanel);
		stackPanel.pushController(translate("ExamAdminMainController.menu.esf"), esfController);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
}
