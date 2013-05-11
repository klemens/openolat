package de.unileipzig.xman.exam.forms;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.exam.Exam;

public class ExamDetailsForm extends FormBasicController {

	public ExamDetailsForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}
	
	
}