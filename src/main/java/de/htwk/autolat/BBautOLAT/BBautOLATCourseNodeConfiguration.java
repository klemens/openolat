package de.htwk.autolat.BBautOLAT;

import java.util.List;
import java.util.Locale;

import org.olat.core.extensions.ExtensionResource;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;

public class BBautOLATCourseNodeConfiguration extends AbstractCourseNodeConfiguration implements CourseNodeConfiguration {

	private static final String PACKAGE = Util.getPackageName(BBautOLATCourseNodeConfiguration.class);

	private BBautOLATCourseNodeConfiguration() {
		super();
	}
	
	public CourseNode getInstance() {
		return new BBautOLATCourseNode();
	}

	public String getLinkText(Locale locale) {
		PackageTranslator translator = new PackageTranslator(PACKAGE, locale);
		return translator.translate("title_autolat");
	}

	public String getIconCSSClass() {
		return "o_iqsurv_icon";
	}

	public String getLinkCSSClass() {
		return null;
	}

	public String getAlias() {
		return BBautOLATCourseNode.TYPE;
	}

	public String getName() {
		return getAlias();
	}

	public List getExtensionResources() {
		return null;
	}

	public ExtensionResource getExtensionCSS() {
		return null;
	}

	public void setup() {
		//nothing to do
	}

	public void tearDown() {
		// nothing to do
	}

	@Override
	public boolean isEnabled() {
		
		return true;
	}

}
