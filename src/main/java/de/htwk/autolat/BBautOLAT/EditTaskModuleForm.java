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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.TaskModule.TaskModule;
import de.htwk.autolat.TaskModule.TaskModuleManagerImpl;

/**
 * Description:<br>
 * TODO: Tom Class Description for EditTaskModuleForm
 * 
 * <P>
 * Initial Date:  21.11.2009 <br>
 * @author Tom
 */
public class EditTaskModuleForm extends FormBasicController {

	public static final String CONST_BEGIN = ".begin.";
	
	public static final String NAME = "EditTaskModuleForm";
	
	private Configuration conf;
	private TaskModule module;
	
	//GUI
	private DateChooser enddate;
	private TextElement duration;
	private TextElement maxcount;
	private SingleSelection position;
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		
		duration = uifactory.addTextElement("duration", "label.form.edittaskmodule.duration", 50, "00:00:00:00", formLayout);
		duration.setExampleKey("example.form.edittaskmodule.duration", null);
		
		enddate = uifactory.addDateChooser("enddate","label.form.edittaskmodule.enddate", "", formLayout);
		enddate.setDateChooserTimeEnabled(true);
		enddate.setDateChooserDateFormat("%d.%m.%Y %H:%M");
		
		maxcount = uifactory.addTextElement("maxcount", "label.form.edittaskmodule.maxcount", 10, "0", formLayout);
		
		List<TaskModule> taskModules = conf.getTaskPlan();
		String keys[] = new String[taskModules.size()+1];
		String labels[] = new String[taskModules.size()+1];
		keys[0] = CONST_BEGIN;
		labels[0] = translate("label.form.edittaskmodule.firstposition");
		Iterator<TaskModule> taskIt = taskModules.iterator();
		int i = 0;
		while(taskIt.hasNext()) {
			i++;
			TaskModule temp = taskIt.next();
			keys[i] = String.valueOf(temp.getKey());
			labels[i] = translate("label.form.edittaskmodule.positionlabel") + " " + i + " (" + String.valueOf(temp.getKey()) + ")";
		}
		
		if(module!=null) {
			
			String altKeys[] = new String[taskModules.size()];
			String altLabels[] = new String[taskModules.size()];
			int m = -1;
			for(int t=0;t<taskModules.size()+1;t++) {
				if(!keys[t].equals(String.valueOf(module.getKey()))) {
					m++;
					altKeys[m] = keys[t];
					altLabels[m] = labels[t];
				}
			}
			keys = altKeys;
			labels = altLabels;
			
		}
		
		position = uifactory.addDropdownSingleselect("position", "label.form.edittaskmodule.position", formLayout,
				keys, labels, null);
		position.select(keys[taskModules.size()+(module!=null ? -1 : 0)], true);
		
		
		if(module != null) {
			duration.setValue(TaskModuleManagerImpl.getInstance().getDurationValue(module.getTaskDuration()));
			enddate.setDate(module.getTaskEndDate());
			maxcount.setValue(String.valueOf(module.getMaxCount()));
			Iterator<TaskModule> posIt = taskModules.iterator();
			int j = 0;
			while(posIt.hasNext()) {
				TaskModule tempObject = posIt.next();
				if(tempObject.getKey().equals(module.getKey())) break;
				j++;
			}
			if(j == 0) 
				position.select(CONST_BEGIN, true);
			else position.select(String.valueOf(taskModules.get(j-1).getKey()), true);
		}
		
		uifactory.addFormSubmitButton("submit", "label.form.edittaskmodule.submit", formLayout);
	}
	
	/**
	 * @param name
	 * @param translator
	 */
	public EditTaskModuleForm(String name, UserRequest ureq, WindowControl wControl, long courseID, long couseNodeID, TaskModule taskModule) {
		super(ureq, wControl);
		
		conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, couseNodeID);
		module = taskModule;
		
		initForm(flc, this, ureq);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		long duration = 0;
		long maxcount = 0;
		Date enddate = null;
		String tempKey;
		long selectedKey = 0;
		
		long prevduration = 0;
		Date prevenddate = null;
		
		long nextduration = 0;
		Date nextenddate = null;		
		
		try{
			duration = TaskModuleManagerImpl.getInstance().parseDurationValue(this.duration.getValue());
		} catch(Exception e) {
			this.duration.setErrorKey("error.form.edittaskmodule.illegalcharacters", null);
			return;
		}
		try{
			maxcount = Long.valueOf(this.maxcount.getValue());
		}catch(Exception e) {
			this.maxcount.setErrorKey("error.form.edittaskmodule.illegalcharacters", null);
			return;
		}
		if(this.enddate.getDate()!=null) enddate = this.enddate.getDate();
		tempKey = this.position.getSelectedKey();
		if(!tempKey.equals(CONST_BEGIN)) selectedKey = Long.valueOf(tempKey);
		
		if(maxcount<0) {
			this.maxcount.setErrorKey("error.form.edittaskmodule.negativecount", null);
			return;
		}
		
		if(duration == 0 && enddate==null && maxcount==0) {
			position.setErrorKey("error.form.edittaskmodule.none", null);
			return;
		}
		
		List<TaskModule> taskModules = conf.getTaskPlan();
		List<TaskModule> before = new ArrayList<TaskModule>();
		List<TaskModule> after = new ArrayList<TaskModule>();
		
		if(module!=null) {
		Iterator<TaskModule> delIt = taskModules.iterator();
			while(delIt.hasNext()) {
				TaskModule delObject = delIt.next();
				if(delObject.getKey().equals(module.getKey())) {
					delIt.remove();
					break;
				}
			}
		}
		
		
		int i = -1;
		int pos = -1;
		
		//determine the position of the entry after which the insert shall be made
		if(selectedKey != 0) {
			Iterator<TaskModule> posIt = taskModules.iterator();
			while(posIt.hasNext()) {
				pos++;
				TaskModule posObject = posIt.next();
				if(posObject.getKey().equals(selectedKey)) break;
			}
		}
		
		//determine the other entries
		Iterator<TaskModule> taskIt = taskModules.iterator();
		while(taskIt.hasNext()) {
			i++;
			TaskModule temp = taskIt.next();
			//case 1: modules AFTER the current one
			if(i>pos) {
				nextduration += temp.getTaskDuration();
				if(nextenddate==null) {
					if(temp.getTaskEndDate()!=null) nextenddate = temp.getTaskEndDate();
				}
				after.add(temp);
			}
			//case 2: modules BEFORE the current one
			else {
				prevduration += temp.getTaskDuration();
				if(temp.getTaskEndDate()!=null) prevenddate = temp.getTaskEndDate();
				before.add(temp);
			}
		}
		
		if(prevenddate==null) prevenddate = conf.getBeginDate();
		
		if(nextenddate==null) nextenddate = conf.getEndDate();
		
		//milliseconds!
		//duration *= 1000;
		//prevduration *= 1000;
		//nextduration *= 1000;
		
		//verifying the module dates up to the previous entry
		if(enddate!=null) {
			if(prevenddate.after(enddate)) {
				this.enddate.setErrorKey("error.form.edittaskmodule.shortenddate", null);
				return;
			}
		}
		
		//verifying the module dates up to the next entry
		if(conf.getBeginDate().getTime()+prevduration+duration+nextduration>conf.getEndDate().getTime()) {
			this.duration.setErrorKey("error.form.edittaskmodule.overlongduration", null);
			return;
		}
		if(enddate!=null) {
			if(enddate.after(nextenddate)) {
				this.enddate.setErrorKey("error.form.edittaskmodule.overlongenddate", null);
				return;
			}
		}
		
		//everything is fine, add the module
		if(module==null) {
			module = TaskModuleManagerImpl.getInstance().createAndPersistTaskModule(duration, enddate, maxcount, null, conf);
		}
		else {
			module.setMaxCount(maxcount);
			module.setTaskDuration(duration);
			module.setTaskEndDate(enddate);
			TaskModuleManagerImpl.getInstance().updateTaskModule(module);
		}
		
		List<TaskModule> result = new ArrayList<TaskModule>();
		result.addAll(before);
		result.add(module);
		result.addAll(after);
		conf.setTaskPlan(result);
		ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
		
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}

}
