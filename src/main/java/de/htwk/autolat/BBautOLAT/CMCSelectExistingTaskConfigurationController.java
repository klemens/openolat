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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;

import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskConfiguration.TaskConfigurationManagerImpl;
import de.htwk.autolat.TaskType.TaskType;

/**
 * Description:<br>
 * TODO: Tom Class Description for CMCSelectExistingTaskConfigurationController
 * 
 * <P>
 * Initial Date:  05.01.2010 <br>
 * @author Tom
 */
public class CMCSelectExistingTaskConfigurationController extends BasicController {

	private VelocityContainer mainvc;
	
	protected EditTaskForm editTaskForm;
	
	private Link backward;
	private Link forward;
	
	private long courseNodeID;
	private int position;
	private List<TaskConfiguration> taskConfigList;
	private TaskConfiguration conf;
	private long courseID;
	
	/**
	 * @param ureq
	 * @param control
	 */
	public CMCSelectExistingTaskConfigurationController(UserRequest ureq, WindowControl control, long courseID, long courseNodeID) {
		super(ureq, control);
		
		this.courseID = courseID;
		this.courseNodeID = courseNodeID;
		this.conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID).getTaskConfiguration();
		taskConfigList = TaskConfigurationManagerImpl.getInstance().getAllAlteredTaskConfigurations(conf);
		position = 0;
		
		createOutput(ureq, control);		
		putInitialPanel(mainvc);
	}

	private void createOutput(UserRequest ureq, WindowControl wControl) {
		
		mainvc = createVelocityContainer("CMCSelectExistingTaskConfigurationController");
		
		if(taskConfigList.size() == 0) {
			mainvc.contextPut("notempty", false);
		}
		else {
			mainvc.contextPut("notempty", true);
			mainvc.contextPut("status", new String((position+1)+"/"+taskConfigList.size()));
			
			backward = LinkFactory.createButton("label.controller.cmcexistingtc.backward", mainvc, this);
			mainvc.put("backward", backward);
			
			forward = LinkFactory.createButton("label.controller.cmcexistingtc.forward", mainvc, this);
			mainvc.put("forward", forward);
			
			editTaskForm = new EditTaskForm(EditTaskForm.NAME, ureq, wControl, courseID, courseNodeID, taskConfigList.get(position));
			editTaskForm.addControllerListener(this);
			mainvc.put("editTaskForm", editTaskForm.getInitialComponent());
		}
		
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component comp, Event evnt) {
	
		if(comp == backward) {
			if(position>0) position--;
			createOutput(ureq, getWindowControl());
		}

		if(comp == forward) {
			if(position<taskConfigList.size()-1) position++;
			createOutput(ureq, getWindowControl());
		}

	}
	
	@Override
	protected void event(UserRequest ureq, Controller ctr, Event evnt) {
		if(ctr == editTaskForm) {
			if(evnt.equals(FormEvent.DONE_EVENT)) {
				fireEvent(ureq, FormEvent.DONE_EVENT);
			}
		}
	}

}
