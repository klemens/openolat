package de.htwk.autolat.BBautOLAT;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

import org.olat.core.gui.translator.Translator;

// import org.olat.core.gui.components.form.flexible.impl.Form;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstance;


public class TaskSeedForm extends FormBasicController
{
	public static final String NAME = "TaskSeedForm";
	protected TextElement seed;
	private Configuration configuration;
	
	public TaskSeedForm(String name, UserRequest ureq, WindowControl wControl, long courseNodeID)
	//(String name, Translator translator, long courseNodeID)
	{	
		super(ureq, wControl);
		initForm(flc, this, ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		// TODO Auto-generated method stub
		seed = uifactory.addTextElement("seed", "label.form.task.enterseed", 150, null, formLayout);
		uifactory.addFormSubmitButton("label.form.task.seed.submit", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_VALIDATION_OK);		
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}

	
}
