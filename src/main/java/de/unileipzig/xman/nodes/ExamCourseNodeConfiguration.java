package de.unileipzig.xman.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.extensions.ClasspathExtensionResource;
import org.olat.core.extensions.ExtensionResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;

import de.unileipzig.xman.exam.Exam;

/**
 * 
 * holds some specifications for an extension-node at the course-editor for our exam
 * 
 * @author blutz
 *
 */
		// ------------------------------------------alt: OLATExtension 
public class ExamCourseNodeConfiguration implements CourseNodeConfiguration{
		
	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getAlias()
	 */
	public String getAlias() {
		
		return Exam.ORES_TYPE_NAME;
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getIconCSSClass()
	 */
	public String getIconCSSClass() {
		
		return "k_exam_icon";
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getInstance()
	 */
	public CourseNode getInstance() {
		
		return new ExamCourseNode();
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getLinkCSSClass()
	 */
	public String getLinkCSSClass() {
		
		return null;
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getLinkText(Locale)
	 */
	public String getLinkText(Locale locale) {
		
		Translator translator = Util.createPackageTranslator(ExamCourseNodeConfiguration.class, locale);
		return translator.translate("ExamCourseNodeConfiguration.title_exam");
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#getExtensionCSS()
	 */
	public ExtensionResource getExtensionCSS() {
		
		return new ClasspathExtensionResource(ExamCourseNodeConfiguration.class, "raw/style.css");
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#getExtensionResources()
	 */
	public List<Object> getExtensionResources() {
		
		List<Object> resources = new ArrayList<Object>();
		resources.add(new ClasspathExtensionResource(ExamCourseNodeConfiguration.class, "raw/Exam.gif"));
		return resources;
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#getName()
	 */
	public String getName() {
		
		return this.getAlias();
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#setup()
	 */
	public void setup() {
		
		// nothing
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#tearDown()
	 */
	public void tearDown() {
			
		// nothing
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}
}