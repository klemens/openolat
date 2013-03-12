package de.unileipzig.xman.portal;

import java.util.Map;


import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;

public class ExamPortlet extends AbstractPortlet {

	private static final String PACKAGE_UI = Util.getPackageName(ExamPortlet.class);
	private Controller runCtr;
	
	/**
	 * 
	 * @see org.olat.gui.control.generic.portal.Portlet#createInstance(org.olat.core.gui.control.WindowControl, org.olat.core.gui.UserRequest, java.util.Map)
	 */
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map portletConfig) {
		
		Translator translator = new PackageTranslator(PACKAGE_UI, ureq.getLocale());
		Portlet p = new ExamPortlet();
		p.setConfiguration(portletConfig);
		p.setTranslator(translator);
		return p;
	}

	/**
	 * 
	 * @see org.olat.gui.control.generic.portal.Portlet#disposeRunComponent(boolean)
	 */
	public void disposeRunComponent() {
		
		if (this.runCtr != null) {
			
			this.runCtr.dispose();
			this.runCtr = null;
		}
		
	}

	/**
	 * 
	 * @see org.olat.gui.control.generic.portal.Portlet#getCssClass()
	 */
	public String getCssClass() {
		
		return "o_pt_w_exa";
	}

	/**
	 * 
	 * @see org.olat.gui.control.generic.portal.Portlet#getDescription()
	 */
	public String getDescription() {
		
		return getTranslator().translate("ExamPortlet.description");
	}

	/**
	 * 
	 * @see org.olat.gui.control.generic.portal.Portlet#getInitialRunComponent(org.olat.core.gui.control.WindowControl, org.olat.core.gui.UserRequest)
	 */
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		
		this.runCtr = new ExamPortletRunController(wControl, ureq, getTranslator());
		return this.runCtr.getInitialComponent();
	}

	/**
	 * 
	 * @see org.olat.gui.control.generic.portal.Portlet#getTitle()
	 */
	public String getTitle() {
		
		return this.getTranslator().translate("ExamPortlet.title");
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.Disposable#dispose(boolean)
	 */
	public void dispose() {
		
		this.disposeRunComponent();
	}

}
