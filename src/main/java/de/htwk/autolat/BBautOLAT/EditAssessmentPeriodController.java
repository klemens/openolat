package de.htwk.autolat.BBautOLAT;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.ICourse;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;

public class EditAssessmentPeriodController extends BasicController {
	
	private long courseNodeID;
	private BBautOLATCourseNode courseNode;
	private ICourse course;
	private Configuration conf;
	
	private Panel main;
	private VelocityContainer mainvc;
	
	private EditPropertiesForm editPropertiesForm;


	public EditAssessmentPeriodController(UserRequest ureq,
			WindowControl wControl, long courseNodeID, BBautOLATCourseNode courseNode,
			ICourse course) {
		super(ureq, wControl);
		
		this.courseNodeID = courseNodeID;
		this.course = course;
		this.courseNode = courseNode;
		this.conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(course.getResourceableId(), courseNodeID);
		
		main = new Panel("editAssessmentPeriodController");
		mainvc = createVelocityContainer("editAssessmentPeriodController");
		
		editPropertiesForm = new EditPropertiesForm(EditPropertiesForm.NAME, ureq, getWindowControl(), course.getResourceableId(), courseNodeID);
		editPropertiesForm.addControllerListener(this);
		
		mainvc.put("editProperties", editPropertiesForm.getInitialComponent());
		
		if(conf.getTaskInstanceList().size()!=0) {
			editPropertiesForm.setEnable(false);
		}
		
		main.setContent(mainvc);
		putInitialPanel(main);
		
		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void event(UserRequest ureq, Controller ctr, Event evnt) {
		if(ctr == editPropertiesForm) {
			if(evnt.equals(FormEvent.DONE_EVENT)) {
				//createOutput(ureq);
				fireEvent(ureq, new Event("EDITPROPERTIES_OK"));
			}
		}
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}
	
	public void updateForm() {
		if(editPropertiesForm != null) {
			//System.out.println("call update Elements");
			editPropertiesForm.updateElements();
		}
	}

}
