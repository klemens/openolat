package de.htwk.autolat.BBautOLAT;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;

import de.htwk.autolat.Student.Student;
import de.htwk.autolat.TaskInstance.TaskInstance;

public class CMCEditPassedController extends BasicController {
	
	private VelocityContainer mainVC;
	
	private TaskInstance taskInstance;
	private Student student;
	private long courseNodeID;
	
	private EditPassedForm editPassedForm;
	private WindowControl wControl;
	private long courseID;
	
	protected CMCEditPassedController(UserRequest ureq, WindowControl wControl, Translator translator, TaskInstance taskInstance, Student student, long courseID, long courseNodeID) {
		super(ureq, wControl, translator);
		this.taskInstance = taskInstance;
		this.student = student;
		this.courseID = courseID;
		this.courseNodeID = courseNodeID;
		this.wControl = wControl;
		mainVC = createVelocityContainer("CMCEditPassedController");
		createOutput(ureq);
		putInitialPanel(mainVC);
	}

	private void createOutput(UserRequest ureq) {
		//(Form)
		if( taskInstance != null)
			//editPassedForm = new EditPassedForm(EditPassedForm.NAME, getTranslator(), taskInstance.getResult(), student, courseID);
			editPassedForm = new EditPassedForm(EditPassedForm.NAME, ureq, wControl, taskInstance.getResult(), student, courseID, courseNodeID);
			editPassedForm.addControllerListener(this);
		if( taskInstance == null) {
			//editPassedForm = new EditPassedForm(EditPassedForm.NAME, getTranslator(), null, student, courseID);
			editPassedForm = new EditPassedForm(EditPassedForm.NAME, ureq, wControl, null, student, courseID, courseNodeID);
			editPassedForm.addControllerListener(this);
		}
		mainVC.put("editPassedForm", editPassedForm.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub		
	}

	@Override
	protected void event(UserRequest uReq, Component comp, Event evt) {			
	}
	
	@Override
	protected void event(UserRequest ureq, Controller ctr, Event evt) {	
		if(ctr == editPassedForm) {
			if(evt.equals(Form.EVNT_VALIDATION_OK)) {
				fireEvent(ureq, FormEvent.DONE_EVENT);
			}
		}
	}

}
