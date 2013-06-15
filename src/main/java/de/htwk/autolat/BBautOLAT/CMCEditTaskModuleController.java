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
package de.htwk.autolat.BBautOLAT;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;

import de.htwk.autolat.TaskModule.TaskModule;

/**
 * Description:<br>
 * TODO: Tom Class Description for CMCEditTaskModuleController
 * 
 * <P>
 * Initial Date:  21.11.2009 <br>
 * @author Tom
 */
public class CMCEditTaskModuleController extends BasicController {

	private VelocityContainer mainvc;
	private Panel main;
	
	private EditTaskModuleForm editTaskModuleForm;
	
	/**
	 * @param ureq
	 * @param control
	 */
	public CMCEditTaskModuleController(UserRequest ureq, WindowControl control, long courseID, long courseNodeID, TaskModule taskModule) {
		super(ureq, control);
		
		main = new Panel("editTaskModule");
		mainvc = createVelocityContainer("CMCeditTaskModuleController");
		
		editTaskModuleForm = new EditTaskModuleForm(EditTaskModuleForm.NAME, ureq, control, courseID, courseNodeID, taskModule);		
		editTaskModuleForm.addControllerListener(this);
		mainvc.put("editTaskModuleForm", editTaskModuleForm.getInitialComponent());
		
		main.setContent(mainvc);
		putInitialPanel(main);
	}

	/**
	 * @param ureq
	 * @param control
	 * @param fallBackTranslator
	 */
	public CMCEditTaskModuleController(UserRequest ureq, WindowControl control, Translator fallBackTranslator) {
		super(ureq, control, fallBackTranslator);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub

	}
	
	protected void event(UserRequest ureq, Controller ctr, Event evnt) {
		if(ctr == editTaskModuleForm) {
			if(evnt.equals(FormEvent.DONE_EVENT)) {
				fireEvent(ureq, FormEvent.DONE_EVENT);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub
		
	}

}
