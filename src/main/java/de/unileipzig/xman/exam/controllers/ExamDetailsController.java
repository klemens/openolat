package de.unileipzig.xman.exam.controllers;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.forms.ExamDetailsForm;

/**
 * The Controller for the details, asked when writing into an Exam
 * 
 * @author robert seidler
 *
 */
public class ExamDetailsController extends MainLayoutBasicController implements MainLayoutController {


	private VelocityContainer vcAttr;
	private ExamDetailsForm examDetailsForm;
	private Translator translator;
	private String accountFor;
	private int examType;
	private Appointment ap;
	
	
	/**
	 * Create the Controller for the Details Form
	 * @param ureq
	 * @param wControl
	 */
	public ExamDetailsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		translator = Util.createPackageTranslator(Exam.class, ureq.getLocale());

		setTranslator(translator);

		vcAttr = new VelocityContainer("vcAttr", Util.getPackageVelocityRoot(Exam.class)
				+ "/chooseAttr.html", translator, this);

		examDetailsForm = new ExamDetailsForm(ureq, wControl);
		examDetailsForm.addControllerListener(this);

		vcAttr.put("chooseExamAttrForm", examDetailsForm.getInitialComponent());

		this.putInitialPanel(vcAttr);
		
	}
	
	public String getAccountFor(){
		return accountFor;	
	}
	
	public int getChooseExamType(){
		return examType;
	}
	

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		// TODO Auto-generated method stub
		if(source == examDetailsForm){			
			if(event == Form.EVNT_VALIDATION_OK){
				accountFor = examDetailsForm.getAccountFor();
				examType = examDetailsForm.getExamTypeSwitchElem();
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub
		
	}

	public Appointment getAppointment() {
		return ap;
	}

	public void setAppointment(Appointment ap) {
		this.ap = ap;
	}
	
	
}