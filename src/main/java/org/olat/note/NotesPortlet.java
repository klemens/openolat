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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.note;

import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.gui.control.generic.portal.PortletToolController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * Description:<br>
 * Displays the list of most used notes
 * <P>
 * Initial Date:  08.07.2005 <br>
 * @author gnaegi
 */
public class NotesPortlet extends AbstractPortlet {
	private NotesPortletRunController runCtr;
	
	/**
	 * @see org.olat.gui.control.generic.portal.AbstractPortlet#createInstance(org.olat.core.gui.control.WindowControl, org.olat.core.gui.UserRequest, java.util.Map)
	 */
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map<String,String> configuration) {
		Translator translator = Util.createPackageTranslator(NotesPortlet.class, ureq.getLocale());
		Portlet p = new NotesPortlet();
		p.setName(this.getName());
		p.setConfiguration(configuration);
		p.setTranslator(translator);
		return p;
	}
	
	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getTitle()
	 */
	public String getTitle() {
		return getTranslator().translate("notesPortlet.title");
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getDescription()
	 */
	public String getDescription() {
		return getTranslator().translate("notesPortlet.description");
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getInitialRunComponent(org.olat.core.gui.control.WindowControl, org.olat.core.gui.UserRequest)
	 */
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		if(this.runCtr != null) runCtr.dispose();
		this.runCtr = new NotesPortletRunController(wControl, ureq, getTranslator(), this.getName());
		return this.runCtr.getInitialComponent();
	}

	/**
	 * @see org.olat.core.gui.control.Disposable#dispose(boolean)
	 */
	public void dispose() {
		disposeRunComponent();
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getCssClass()
	 */
	public String getCssClass() {
		return "o_portlet_notes";
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#disposeRunComponent(boolean)
	 */
	public void disposeRunComponent() {
		if (this.runCtr != null) {
			this.runCtr.dispose();
			this.runCtr = null;
		}
	}

	public PortletToolController<Note> getTools(UserRequest ureq, WindowControl wControl) {
		if (runCtr == null) {
			this.runCtr = new NotesPortletRunController(wControl, ureq, getTranslator(), this.getName());
		}
	  return runCtr.createSortingTool(ureq, wControl);
	}
}
