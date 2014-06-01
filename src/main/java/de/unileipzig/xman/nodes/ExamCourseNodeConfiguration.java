package de.unileipzig.xman.nodes;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
public class ExamCourseNodeConfiguration implements CourseNodeConfiguration{
	@Override
	public String getAlias() {
		return Exam.ORES_TYPE_NAME;
	}

	@Override
	public String getIconCSSClass() {
		return "k_exam_icon";
	}

	@Override
	public CourseNode getInstance() {
		return new ExamCourseNode();
	}

	@Override
	public String getLinkCSSClass() {
		return null;
	}

	@Override
	public String getLinkText(Locale locale) {
		Translator translator = Util.createPackageTranslator(ExamCourseNodeConfiguration.class, locale);
		return translator.translate("ExamCourseNodeConfiguration.title_exam");
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

	@Override
	public List<String> getAlternativeCourseNodes() {
		return Collections.emptyList();
	}
}