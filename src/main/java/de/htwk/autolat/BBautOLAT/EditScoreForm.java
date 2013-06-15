package de.htwk.autolat.BBautOLAT;


import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.Student.StudentManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskInstance.TaskInstanceManagerImpl;
import de.htwk.autolat.TaskResult.TaskResult;
import de.htwk.autolat.TaskResult.TaskResultManagerImpl;

public class EditScoreForm extends FormBasicController {
	
	public static final String NAME = "EditScoreForm";
	
	private TaskResult taskResult;
	private long courseNodeID;
	private Student student;

	private TextElement editScore;

	private long courseID;

	public EditScoreForm(String name, UserRequest ureq, WindowControl wControl, TaskResult taskResult, Student student, long courseID, long courseNodeID) {
		super(ureq, wControl);
		
		this.taskResult = taskResult;
		this.courseNodeID = courseNodeID;
		this.courseID = courseID;
		this.student = student;
		
		initForm(flc, this, ureq);	
	}
	
	public boolean validate() {
		TaskResult result;
		if(taskResult == null) {
			taskResult = TaskResultManagerImpl.getInstance().createAndPersistTaskResult(new Date(System.currentTimeMillis()), 0.0, null, null, false);
			Student stud = StudentManagerImpl.getInstance().loadStudentByID(student.getKey());
			Configuration conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
			TaskInstance newTaskInstance = stud.getTaskInstanceByConfiguration(conf);
			
			if(newTaskInstance == null) {
				newTaskInstance = TaskInstanceManagerImpl.getInstance().createAndPersistTaskInstance(null, conf, 0, null, taskResult, stud, null, null);
				stud.addTaskInstance(newTaskInstance);
				StudentManagerImpl.getInstance().updateStudent(stud);
			}
			else {
				newTaskInstance.setResult(taskResult);
				TaskInstanceManagerImpl.getInstance().updateTaskInstance(newTaskInstance);
			}
		}
		String value = editScore.getValue();
		double newScore;
		result = TaskResultManagerImpl.getInstance().loadTaskResultByID(taskResult.getKey());

		try {
			newScore = Double.valueOf(value);
			result.setMaxScore(newScore);
			TaskResultManagerImpl.getInstance().updateTaskResult(result);
			return true;
		} catch (Exception e) {
			editScore.setErrorKey("error.selectelem.editscore.nodouble", null);
			return false;
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		/*
		if(taskResult != null)
			addFormElement("editScore", new TextElement("label.form.editscore.editscore", String.valueOf(taskResult.getMaxScore()), 20));
		else
			addFormElement("editScore", new TextElement("label.form.editscore.editscore", String.valueOf(0.0), 20));

		setSubmitKey("label.form.editscore.submit");
		*/

		String start = String.valueOf(0.0);
		
		if(taskResult != null)
			start = String.valueOf(taskResult.getMaxScore());						

		editScore = uifactory.addTextElement("editScore", "label.form.editscore.editscore", 20, start, formLayout);
		uifactory.addFormSubmitButton("label.form.editscore.submit", formLayout);
		
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(validate())
			fireEvent(ureq, Form.EVNT_VALIDATION_OK);		
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}
	
}
