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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package de.htwk.autolat.BBautOLAT.structure;

import java.util.List;
import java.util.Locale;

import org.olat.core.extensions.ExtensionResource;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.util.Util;
import org.olat.course.nodes.AbstractCourseNodeConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
/**
 * 
 * Description:<br>
 * TODO: guido Class Description for STCourseNodeConfiguration
 * 
 */
public class BBautOLATStructureNodeConfiguration extends AbstractCourseNodeConfiguration implements CourseNodeConfiguration {
	transient public static int MAX_PEEKVIEW_CHILD_NODES = 10; // default 10
	private static final String PACKAGE = Util.getPackageName(BBautOLATStructureNode.class);

	private BBautOLATStructureNodeConfiguration() {
		super();
	}
	
	public CourseNode getInstance() {
		return new BBautOLATStructureNode();
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getLinkText(java.util.Locale)
	 */
	public String getLinkText(Locale locale) {
		PackageTranslator translator = new PackageTranslator(PACKAGE, locale);
		return translator.translate("title_autolatst");
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getCSSClass()
	 */
	public String getIconCSSClass() {
		return "o_st_icon";
	}

	/**
	 * @see org.olat.course.nodes.CourseNodeConfiguration#getLinkCSSClass()
	 */
	public String getLinkCSSClass() {
		return null;
	}

	public String getAlias() {
		return "autolatst";
	}

	//
	// OLATExtension interface implementations.
	//

	public String getName() {
		return getAlias();
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#getExtensionResources()
	 */
	public List getExtensionResources() {
		// no ressources, part of main css
		return null;
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#getExtensionCSS()
	 */
	public ExtensionResource getExtensionCSS() {
		// no ressources, part of main css
		return null;
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#setURLBuilder(org.olat.core.gui.render.URLBuilder)
	 */
	public void setExtensionResourcesBaseURI(String ubi) {
	// no need for the URLBuilder
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#setup()
	 */
	public void setup() {
	// nothing to do here
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#tearDown()
	 */
	public void tearDown() {
	// nothing to do here
	}

	@Override
	public boolean isEnabled() {
		
		return true;
	}


}
