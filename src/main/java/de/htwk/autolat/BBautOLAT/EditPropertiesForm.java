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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;

/**
 * Description:<br>
 * Form to set the dates and scorepoints
 * 
 * <P>
 * Initial Date:  21.05.2011 <br>
 * @author Joerg
 */
public class EditPropertiesForm extends FormBasicController {
	
	public static final String NAME = "EditPropertiesForm";
	
	private long courseNodeID;	
	//GUI
	private DateChooser beginDate, endDate;
	private TextElement scorepoints;
	private FormSubmit submit;

	private long courseID;
	
	public EditPropertiesForm(String name, UserRequest ureq, WindowControl wControl, long courseID, long courseNodeID) {
		super(ureq, wControl);
		
		this.courseNodeID = courseNodeID;
		this.courseID = courseID;
		initForm(flc, this, ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		
		beginDate = uifactory.addDateChooser("beginDate", "label.form.editproperties.begindate", null, formLayout);
		beginDate.setDateChooserTimeEnabled(true);
		beginDate.setMandatory(true);
		beginDate.setNotEmptyCheck("error.form.editproperties.setdate");
		
		endDate = uifactory.addDateChooser("endDate", "label.form.editproperties.enddate", null, formLayout);
		endDate.setDateChooserTimeEnabled(true);
		endDate.setMandatory(true);
		endDate.setNotEmptyCheck("error.form.editproperties.setdate");
		
		scorepoints = uifactory.addTextElement("scorepoints", "label.form.editproperties.scorepoints", 150,
				"", formLayout);
		scorepoints.setExampleKey("example.form.editproperties.scorepoints", null);
		
		Configuration conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
		
		if(conf.getBeginDate()!=null) {
			beginDate.setDate(conf.getBeginDate());
		}
		if(conf.getEndDate()!=null) {
			endDate.setDate(conf.getEndDate());
		}
		if(conf.getScorePoints()!=null) {
			String value = "";
			Iterator<Integer> scoreIterator = conf.getScorePoints().iterator();
			while(scoreIterator.hasNext()) {
				value+=scoreIterator.next();
				if(scoreIterator.hasNext()) value+=", ";
			}
			scorepoints.setValue(value);
		}
		
		submit = uifactory.addFormSubmitButton("submit", formLayout);
		
	}

	@Override
	protected void formOK(UserRequest ureq) {

		Date begindate =  beginDate.getDate();
		Date enddate = endDate.getDate();
		String scorepointsString = scorepoints.getValue();
		ArrayList<Integer> scorePoints = new ArrayList<Integer>();
		
		if(enddate.getTime() - begindate.getTime() < 0) {
			endDate.setErrorKey("error.form.editproperties.nointerval", null);
			return;
		}
		/*if(begindate.before(new Date())) {
			beginDate.setErrorKey("error.form.editproperties.datebeforetoday");
			return false;
		}*/
		if(enddate.before(new Date())) {
			endDate.setErrorKey("error.form.editproperties.datebeforetoday", null);
			return;
		}
		if(scorepointsString != "") {
			char[] values = scorepointsString.toCharArray();
			String tempvalue = "";
			for(char c : values) {
				if(c!=',') tempvalue+=String.valueOf(c);
				else {
					try {
						tempvalue = tempvalue.trim();
						scorePoints.add(Integer.parseInt(tempvalue));
						tempvalue = "";
					} catch(Exception e) {
						scorepoints.setErrorKey("error.form.editproperties.illegalcharacters", null);
						return;
					}
				}
			}
			if(tempvalue!="") {
				try {
					tempvalue = tempvalue.trim();
					scorePoints.add(Integer.parseInt(tempvalue));
					tempvalue = "";
				} catch (Exception e) {
					scorepoints.setErrorKey("error.form.editproperties.illegalcharacters", null);
					return;
				}
			}
		}
		Configuration conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
		
		conf.setBeginDate(begindate);
		conf.setEndDate(enddate);
		conf.setScorePoints(scorePoints);
		ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}
	
	public void setEnable(boolean isEnabled) {
		beginDate.setEnabled(isEnabled);
		endDate.setEnabled(isEnabled);
		scorepoints.setEnabled(isEnabled);
		submit.setEnabled(isEnabled);
		
	}
	
	public void updateElements() {
		Configuration conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
		//update scorepoints
		List<Integer> scorepoints = conf.getScorePoints();
		String scoreString = "";
		if(scorepoints.size() > 0) {
			scoreString += scorepoints.get(0);
			for(int i = 1; i < scorepoints.size(); i++) {
				scoreString += ", ";
				scoreString += scorepoints.get(i);
			}
		}
		this.scorepoints.setValue(scoreString);
		
		//update Dates
		this.beginDate.setDate(conf.getBeginDate());
		this.endDate.setDate(conf.getEndDate());
	}

}
