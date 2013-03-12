package de.unileipzig.xman.home;

import java.util.Locale;

import org.olat.core.extensions.Extension;
import org.olat.core.extensions.ExtensionElement;
import org.olat.core.extensions.action.ActionExtension;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.extensions.helpers.ExtensionElements;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.home.HomeMainController;

import de.unileipzig.xman.esf.controller.ESFLaunchController;

/**
 * Extension which adds a new new menu entry to the HomeMainController.
 * 
 * @author gerb
 */
		// -----------------------alt:  implements Extension ... eventuell auch die Methoden entfernen wenns geht
public class MyValidationExtension{

	private ExtensionElements elements = new ExtensionElements();
	
	/**
	 * Default constructor.
	 * Add new entries to the ExtensionElements here.
	 */
	public MyValidationExtension() {
		
		this.elements.putExtensionElement(HomeMainController.class.getName(), this.homeMainControllerEntry("myValidation"));
	}
	
	/**
	 * Creates new ExtensionElements for this extension.
	 * 
	 * @param extName - the identifier of the extension
	 * @return the created extension element or null if no such extension element is available
	 */
	private ExtensionElement homeMainControllerEntry(String extName) {
		
		ActionExtension actionExt = null;
		
		// menu entry "my exams"
		if ( extName.equals("myValidation")) {
		
			actionExt = new GenericActionExtension() {
				
				/*
				 * @see org.olat.core.extensions.action.ActionExtension#getDescription(java.util.Locale)
				 */
				@Override
				public String getDescription(Locale locale) {
					
					Translator translator = Util.createPackageTranslator(MyValidationExtension.class, locale);
					return translator.translate("s");
				}
				
				/*
				 * @see org.olat.core.extensions.action.ActionExtension#getActionText(java.util.Locale)
				 */
				@Override
				public String getActionText(Locale locale) {
					
					Translator translator = Util.createPackageTranslator(MyValidationExtension.class, locale);
					return translator.translate("myValidationExtension.action");
				}
				
				/*
				 * @see org.olat.core.extensions.action.ActionExtension#createController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl, java.lang.Object)
				 */
				@Override
				public Controller createController(UserRequest ureq, WindowControl wControl, Object obj) {
					
					return new ESFLaunchController(ureq, wControl);
				}
			};
		}
		
		return actionExt;
	}

	/**
	 * @return the extension element for the specified extension point (e.g. this.getClass.getName())
	 */
	public ExtensionElement getExtensionFor(String extensionPoint) {
		
		return this.elements.getExtensionElement(extensionPoint);
	}
}
