package de.htwk.autolat.BBautOLAT;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
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
import de.htwk.autolat.TaskResult.TaskResultImpl;
import de.htwk.autolat.TaskResult.TaskResultManagerImpl;

public class EditPassedForm extends FormBasicController {
	
	public static final String NAME = "EditPassedForm";
	
	public static final String CONST_NOTCHOSEN = ".notchosen.";
	public static final String CONST_YES = ".yes.";
	public static final String CONST_NO = ".no.";
	
	private SingleSelection selectPassed;
	private TaskResult taskResult;
	private Student student;
	private long courseNodeID;

	private long courseID;

	public EditPassedForm(String name, UserRequest ureq, WindowControl wControl, TaskResult taskResult, Student student, long courseID, long courseNodeID) {
		super(ureq, wControl);
		this.taskResult = taskResult;
		this.student = student;
		this.courseNodeID = courseNodeID;
		this.courseID = courseNodeID;

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
		result = TaskResultManagerImpl.getInstance().loadTaskResultByID(taskResult.getKey());

		if(selectPassed.getSelectedKey().equals(CONST_YES)) {
			result.setHasPassed(true);
			TaskResultManagerImpl.getInstance().updateTaskResult(result);
			return true;
		}
		else if(selectPassed.getSelectedKey().equals(CONST_NO)) {
			result.setHasPassed(false);
			TaskResultManagerImpl.getInstance().updateTaskResult(result);
			return true;
		}
		else {
			selectPassed.setErrorKey("error.selectelem.editpassed.novaluechosen", null);			
			return false;
		}
				
		/*
		if(getSingleSelectionElement("selectPassed").getSelectedKey().equals(CONST_YES)) {
			result.setHasPassed(true);
			TaskResultManagerImpl.getInstance().updateTaskResult(result);
			return true;
		}
		else if(getSingleSelectionElement("selectPassed").getSelectedKey().equals(CONST_NO)) {
			result.setHasPassed(false);
			TaskResultManagerImpl.getInstance().updateTaskResult(result);
			return true;
		}
		else {
			getSingleSelectionElement("selectPassed").setErrorKey("error.selectelem.editpassed.novaluechosen");
			return false;
		}
		
		//return false;
		 */
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		String[] keys = {CONST_NOTCHOSEN, CONST_YES, CONST_NO};
		String[] values = {translate("label.selectelem.editpassed.novaluechosen"), translate("label.selectelem.editpassed.yes"),
				translate("label.selectelem.editpassed.no")};
		
		selectPassed = uifactory.addDropdownSingleselect("label.form.editpassed.selectpassed", formLayout, keys, values, null);
		uifactory.addFormSubmitButton("label.form.editpassed.submit", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(validate())
		{
			fireEvent(ureq, Form.EVNT_VALIDATION_OK);
		}
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	} 

}
