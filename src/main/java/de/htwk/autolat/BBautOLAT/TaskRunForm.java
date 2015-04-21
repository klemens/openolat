package de.htwk.autolat.BBautOLAT;

import java.text.DateFormat;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;

//import org.olat.core.gui.components.form.Form;
//import org.olat.core.gui.formelements.StaticHTMLTextElement;
//import org.olat.core.gui.formelements.TextAreaElement;
//import org.olat.core.gui.translator.Translator;

import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.LivingTaskInstance.LivingTaskInstanceManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskModule.TaskModuleManagerImpl;

/**
 * This class is a very simple form where the students can enter and submit
 * their solutions. The TaskRunForm is called by and included in the
 * TaskInstanceTestController and the TaskInstanceRunController. 
 */
public class TaskRunForm extends FormBasicController
{
	
	/** The Constant NAME. */
	public static final String NAME = "TaskRunForm";
	
	private final String RED_HEX = "#FF0000";
	private final String GREEN_HEX = "#009900";
	
	private boolean showEvaluationPeriod = true;
	private boolean hasTaskModule;
	private String remarkText;
	private TaskInstance taskInstance;
	
	protected TextElement solution;
	private StaticTextElement doc;
	protected TextElement remark;
	protected TextElement counter;
	protected TextElement timerestriction;
	protected FormSubmit submit;
	
	/**
	 * Instantiates a new task run form.
	 *
	 * @param name the name
	 * @param translator the translator
	 * @param courseNodeID the course node id
	 * @param taskInstance the task instance
	 * @param showEvaluationPeriod determines whether the evaluation period should be displayed 
	 */
	//public TaskRunForm(String name, Translator translator, long courseNodeID, TaskInstance taskInstance, boolean showEvaluationPeriod)
	public TaskRunForm(String name, UserRequest ureq, WindowControl wControl, long courseID, long courseNodeID, TaskInstance taskInstance, boolean showEvaluationPeriod)
	{	
		//super(name, translator);
		super(ureq, wControl);
	
		this.showEvaluationPeriod = showEvaluationPeriod;		
		this.taskInstance = taskInstance;
		this.hasTaskModule = false;
						
		Configuration configuration = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
		
		// create the (maybe colored) date strings for the remark
		String colorCodedBegin = translate("label.form.task.begindatenotset");
		String colorCodedEnd = translate("label.form.task.enddatenotset");
		
		if(configuration.getBeginDate()!=null && configuration.getEndDate()!=null) {
			colorCodedBegin = DateFormat.getInstance().format(configuration.getBeginDate());
			colorCodedEnd = DateFormat.getInstance().format(configuration.getEndDate());;
				
			if(configuration.getBeginDate().after(new Date()))
				colorCodedBegin = colorString(colorCodedBegin, RED_HEX);
			else
				colorCodedBegin = colorString(colorCodedBegin, GREEN_HEX);
		
			if(configuration.getEndDate().before(new Date()))
				colorCodedEnd = colorString(colorCodedEnd, RED_HEX);
			else
				colorCodedEnd = colorString(colorCodedEnd, GREEN_HEX);
		}
		
		if(taskInstance!=null)
				hasTaskModule = taskInstance.getTaskModule() == null ? false : true;
		
		remarkText = new String(
			"<p>" + translate("label.form.task.duration", new String[]{colorCodedBegin, colorCodedEnd}) + "</p>");
		
		if(hasTaskModule)
			remarkText = remarkText.concat(
					"<p>" + translate("label.form.task.module.yes") + "</p>"					
			);
					
		// add form elements				
		//addFormElement("solution", new TextAreaElement("label.form.task.entersolution", 8,4));
		//setSubmitKey("label.form.task.submit");
		
		/* autolat8
		if(showEvaluationPeriod)
		{
			addFormElement("remark", new StaticHTMLTextElement(
					"label.form.task.remark",			
					remarkText,				
					500)
			);
			if(taskInstance!=null)
			{
				addFormElement("counter", new StaticHTMLTextElement("label.form.task.counter",
						String.valueOf(taskInstance.getInstanceCounter()) +
						(hasTaskModule && (taskInstance.getTaskModule().getMaxCount() != 0) ? " / " + taskInstance.getTaskModule().getMaxCount() : ""),
						20)					
				);			
				if(hasTaskModule)
				{
					Date taskModuleEndDate = TaskModuleManagerImpl.getInstance().getTaskModuleEndDate(
							taskInstance.getTaskModule(),
							taskInstance.getLivingTaskInstance().getCreationDate());
					if(taskModuleEndDate != null)
						addFormElement("timerestriction", new StaticHTMLTextElement("label.form.task.module.timerestriction",
								translate("label.form.task.module.enddate", 
										new String[]{DateFormat.getInstance().format(taskModuleEndDate)}),
										100)
						);
				}
			}
		}
		*/
		initForm(flc, this, ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.Form#validate()
	 */
	/* autolat-7
	@Override
	public boolean validate()
	{
		return true;
	}
	*/

	/**
	 * A very simple method to enclose a string in a HTML tags in order to
	 * change the font color to the color with colorCode.
	 *
	 * @param s the string
	 * @param colorCode the HTML color-code, in formal #RRGGBB
	 * @return the string s enclosed in html font tag
	 */
	private String colorString(String s, String colorCode)
	{
		return new String("<font style=\"color:" + colorCode + ";\">" + s + "</font>");
	}
	

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) 
	{
		//uifactory.addTextElement("scorepoints", "label.form.editproperties.scorepoints", 150,
			//"", formLayout);
		//solution = uifactory.addTextElement(
		//		"solution", "label.form.task.entersolution", 1024, "", formLayout);
		
		solution = uifactory.addTextAreaElement("solution", "label.form.task.entersolution", 10000, 8, 4, false, "", formLayout);
		doc = uifactory.addStaticTextElement("documentation", "label.form.task.documentation", "", formLayout);
		submit = uifactory.addFormSubmitButton("submit", "label.form.task.submit", formLayout);
		
		if(showEvaluationPeriod)
		{	
			remark = uifactory.addRichTextElementForStringDataMinimalistic("remark", "label.form.task.remark",
					remarkText, -1, -1, formLayout, getWindowControl());
			remark.setEnabled(false);

			if(taskInstance!=null)
			{
				counter = uifactory.addRichTextElementForStringDataMinimalistic("counter", "label.form.task.counter", String.valueOf(taskInstance.getInstanceCounter()) +
						(hasTaskModule && (taskInstance.getTaskModule().getMaxCount() != 0) ? " / " + taskInstance.getTaskModule().getMaxCount() : ""), -1,
						-1, formLayout, getWindowControl());
				counter.setEnabled(false);			
				if(hasTaskModule)
				{
					Date taskModuleEndDate = TaskModuleManagerImpl.getInstance().getTaskModuleEndDate(
							taskInstance.getTaskModule(),
							taskInstance.getLivingTaskInstance().getCreationDate());
					if(taskModuleEndDate != null) {
						timerestriction = uifactory.addRichTextElementForStringDataMinimalistic("timerestriction", "label.form.task.module.timerestriction",
								new String(DateFormat.getInstance().format(taskModuleEndDate)), -1, -1, formLayout, getWindowControl());
						timerestriction.setEnabled(false);
					}
				}
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// TODO Auto-generated method stub		
		fireEvent(ureq, Form.EVNT_VALIDATION_OK);
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}

	public void setDisplayOnly(boolean status) {
		solution.setEnabled(!status);
		submit.setEnabled(!status);
		
	}

	/**
	 * Set the documentation that is displayed below the solution entry box.
	 * @param xmlDocumentation XML-Representation of the documentation, eg:
	 *     "<?xml version='1.0' ?> <Beside ><Text >[</Text ><Link href="*link*" >Statement</Link ><Text >]</Text></Beside>"
	 */
	public void setDocumentation(String xmlDocumentation) {
		String documentation = LivingTaskInstanceManagerImpl.getInstance().parseDocumentation(xmlDocumentation);
		doc.setValue(documentation);
	}
}
