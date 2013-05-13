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
public class ExamDetailsForm extends FormBasicController {

	private SingleSelection examTypeSwitchElem;
	private TextElement chooseAccountFor;
	
	public static String FIRST_KEY = "ExamDetailsController.first";
	public static String SECOND_KEY = "ExamDetailsController.second";
	
	
	public ExamDetailsForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		
		String[] values = new String[] { translate(FIRST_KEY), translate(SECOND_KEY) };
		String[] keys = new String[] { ExamDetailsForm.FIRST_KEY, ExamDetailsForm.SECOND_KEY };

		examTypeSwitchElem = uifactory.addRadiosVertical("examDetails", "ExamDetailsForm.radioButton", formLayout, keys, values);
		examTypeSwitchElem.select(ExamDetailsForm.FIRST_KEY, true);
						
		chooseAccountFor = uifactory.addTextElement("accountFor", "ExamDetailsForm.accountFor", 50, "", formLayout);
		
		uifactory.addFormSubmitButton("save", "ExamDetailsForm.submit", formLayout);
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
		if(examTypeSwitchElem.getSelectedKey() == ExamDetailsForm.FIRST_KEY) return Exam.ORIGINAL_EXAM;
		else return Exam.REAPEAT_EXAM;
	}
	
	
}