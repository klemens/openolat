package de.unileipzig.xman.admin.form;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;



public class ChooseStudyPathXMLFile extends FormBasicController{

	private FileElement fileElement;
	private Submit submit;
	
	public ChooseStudyPathXMLFile(UserRequest ureq, WindowControl wControl, Translator translator) {
		super(ureq, wControl);
		this.setTranslator(translator);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		fileElement = uifactory.addFileElement("file", "ChooseStudyPathXMLFile.file", formLayout);
		fileElement.setEnabled(true);
		
		submit = uifactory.addFormSubmitButton("submitKey", "ChooseStudyPathXMLFile.submit", formLayout);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_VALIDATION_OK);
	}

	@Override
	protected void doDispose() {
				
	}
	
	public File getFile(){
		return fileElement.getUploadFile();
	}
	
}