package de.unileipzig.xman.admin;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteInstance;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StateSite;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.util.logging.activity.LoggingResourceable;

import de.unileipzig.xman.admin.controller.ExamAdminMainController;

public class ExamAdminSite extends AbstractSiteInstance {
	private NavElement origNavElem;
	private NavElement curNavElem;

	public ExamAdminSite(Locale locale, SiteDefinition siteDef) {
		super(siteDef);
		
		Translator translator = Util.createPackageTranslator(ExamAdminSite.class, locale);
		origNavElem = new DefaultNavElement(translator.translate("ExamAdminSite.topNav.examAdmin"), translator.translate("ExamAdminSite.topNav.examAdmin.alt"), "o_xman_examAdmin");
		curNavElem = new DefaultNavElement(origNavElem);
	}

	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}

	@Override
	protected Controller createController(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ExamAdminSite.class, 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, ores, new StateSite(this), wControl, true);

		return new ExamAdminMainController(ureq, bwControl);
	}
}
