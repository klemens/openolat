package de.unileipzig.xman.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.unileipzig.xman.admin.controller.ExamAdminMainController;

import java.util.*;

/**
 * 
 * @author
 */
public class ExamAdminSite implements SiteInstance {
	
	// refer to the definitions in org.olat
	private static final String PACKAGE = Util.getPackageName(ExamAdminSite.class);
	
	private NavElement origNavElem;
	private NavElement curNavElem;
	
	/**
	 * creates the exam office tab
	 * @param loc the locale
	 */
	public ExamAdminSite(Locale loc){
		
		Translator trans = new PackageTranslator(PACKAGE, loc);
		origNavElem = new DefaultNavElement(trans.translate("ExamAdminSite.topNav.examAdmin"), trans.translate("ExamAdminSite.topNav.examAdmin.alt"), "o_xman_examAdmin");
		curNavElem = new DefaultNavElement(origNavElem);
	}

	/**
	 * creates the maincontroller for the exam office tab
	 * @param ureq - the UserRequest
	 * @param wControl - the windowcontrol
	 */
	public MainLayoutController createController(UserRequest ureq, WindowControl wControl) {
		
		MainLayoutController c = new ExamAdminMainController(ureq, wControl);
		return c;
	}

	/**
	 * @return the current navigation element 
	 */
	public NavElement getNavElement() {
		
		return curNavElem;
	}

	/**
	 * //TODO
	 */
	public boolean isKeepState() {

		return true;
	}

	/**
	 * resets the current navigation element
	 */
	public void reset() {
		
		curNavElem = new DefaultNavElement(origNavElem);
	}
}