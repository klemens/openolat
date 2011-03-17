/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.course.nodes.members;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.MembersCourseNode;

public class MembersCourseNodeConfiguration extends AbstractCourseNodeConfiguration implements CourseNodeConfiguration {
	
	private MembersCourseNodeConfiguration() {
		super();
	}

	@Override
	public String getAlias() {
		return "cmembers";
	}

	@Override
	public CourseNode getInstance() {
		return new MembersCourseNode();
	}

	@Override
	public String getLinkText(Locale locale) {
		Translator fallback = Util.createPackageTranslator(CourseNodeConfiguration.class, locale);
		Translator translator = Util.createPackageTranslator(MembersCourseNodeConfiguration.class, locale, fallback);
		return translator.translate("title_info");
	}

	@Override
	public String getIconCSSClass() {
		return "o_cmembers_icon";
	}

	@Override
	public String getLinkCSSClass() {
		return null;
	}
}
