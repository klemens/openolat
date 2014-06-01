package de.unileipzig.xman.exam.forms;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.util.Util;
import de.unileipzig.xman.exam.Exam;

/**
 * Form for the details, asked when writing into Exam
 * 
 * @author robert seidler
 *
 */
public class ExamStudentRegistrationDetailsForm extends FormBasicController {

	private SingleSelection examTypeSwitchElem;
	private TextElement chooseAccountFor;
	
	public static String FIRST_KEY = "ExamStudentRegistrationDetailsForm.first";
	public static String SECOND_KEY = "ExamStudentRegistrationDetailsForm.second";
	
	
	public ExamStudentRegistrationDetailsForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		
		String[] values = new String[] { translate(FIRST_KEY), translate(SECOND_KEY) };
		String[] keys = new String[] { ExamStudentRegistrationDetailsForm.FIRST_KEY, ExamStudentRegistrationDetailsForm.SECOND_KEY };

		examTypeSwitchElem = uifactory.addRadiosVertical("examDetails", "ExamStudentRegistrationDetailsForm.radioButton", formLayout, keys, values);
		examTypeSwitchElem.select(ExamStudentRegistrationDetailsForm.FIRST_KEY, true);
						
		chooseAccountFor = uifactory.addTextElement("accountFor", "ExamStudentRegistrationDetailsForm.accountFor", 50, "", formLayout);
		
		uifactory.addFormSubmitButton("save", "ExamStudentRegistrationDetailsForm.submit", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_VALIDATION_OK);		
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}
	
	public String getAccountFor(){
		return chooseAccountFor.getValue();
	}
	
	public int getExamTypeSwitchElem(){
		if(examTypeSwitchElem.getSelectedKey() == ExamStudentRegistrationDetailsForm.FIRST_KEY) return Exam.ORIGINAL_EXAM;
		else return Exam.REAPEAT_EXAM;
	}
	
	
}