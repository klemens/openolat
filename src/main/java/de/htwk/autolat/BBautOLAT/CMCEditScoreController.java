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

import de.htwk.autolat.Student.Student;
import de.htwk.autolat.TaskInstance.TaskInstance;

public class CMCEditScoreController extends BasicController {
	
	private VelocityContainer mainVC;
	private Panel main;
	private TaskInstance taskInstance;
	private Student student;
	private long courseNodeID;
	
	private EditScoreForm editScoreForm;
	private WindowControl wControl;
	private long courseID;

	protected CMCEditScoreController(UserRequest ureq, WindowControl wControl, TaskInstance taskInstance, Student student, long courseID, long courseNodeID) {
		super(ureq, wControl);
		this.taskInstance = taskInstance;
		this.student = student;
		this.courseID = courseID;
		this.courseNodeID = courseNodeID;
		this.wControl = wControl;
		
		main = new Panel("viewEditScore");
		mainVC = createVelocityContainer("CMCEditScoreController");
		createOutput(ureq);
		main = this.putInitialPanel(mainVC);
	}

	private void createOutput(UserRequest ureq) {
		if(taskInstance != null)
			editScoreForm = new EditScoreForm(EditScoreForm.NAME, ureq, wControl, taskInstance.getResult(), student, courseID, courseNodeID);			
		if(taskInstance == null) 
			editScoreForm = new EditScoreForm(EditScoreForm.NAME, ureq, wControl, null, student, courseID, courseNodeID);
			
		editScoreForm.addControllerListener(this);

		mainVC.put("editScoreForm", editScoreForm.getInitialComponent());
		main.setContent(mainVC);		
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void event(UserRequest arg0, Component arg1, Event arg2) {
		// TODO Auto-generated method stub
		
	}
	
	protected void event(UserRequest ureq, Controller ctr, Event evt) {
		if(ctr == editScoreForm) {
			if(evt.equals(Form.EVNT_VALIDATION_OK)) {
				fireEvent(ureq, FormEvent.DONE_EVENT);
			}
		}
	}

}
