package de.htwk.autolat.BBautOLAT;

import java.io.IOException;

import org.jdom.JDOMException;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Connector.AutolatConnectorException;
import de.htwk.autolat.Connector.BBautOLATConnector;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskConfiguration.TaskConfigurationManagerImpl;
import de.htwk.autolat.tools.XMLParser.OutputObject;
import de.htwk.autolat.tools.XMLParser.XMLParser;
/**
 * Form to configure the autotool task. 
 * 
 * <P>
 * @author werjo
 *
 */
public class EditTaskForm extends FormBasicController {
	
	public static final String NAME = "EditTaskForm";
	
	private boolean isConfTextChanged = false;
	
	private long courseID;
	private long courseNodeID;
	private Configuration conf;
	private TaskConfiguration taskConfiguration;
	private TaskConfiguration newVerifiedTask;
	
	private XMLParser parser; 
	private String documentationTextAsHTML;
	
	private boolean valid = false;
	
	//GUI
	private TextElement configuration;
	private TextElement doc;
	private TextElement description;
	private TextElement comment;
	private FormSubmit submit; 
	/**
	 * constructor, to initialize the form. <br><br>
	 * 
	 * @param name a name for the controller
	 * @param ureq the user request object
	 * @param wControl the window control object
	 * @param courseNodeID the id of the course node to determine the configuration
	 * @param taskConf a given task configuration, mainly used in the form to choose from given task configurations.
	 */
	public EditTaskForm(String name, UserRequest ureq, WindowControl wControl, long courseID, long courseNodeID, TaskConfiguration taskConf) {
		super(ureq, wControl);
		
		this.courseNodeID = courseNodeID;
		this.courseID = courseID;
		conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
		taskConfiguration = taskConf;
		
		parser = new XMLParser();		
		initForm(flc, this, ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		configuration = uifactory.addTextAreaElement("configuration", "label.form.edittask.configuration",
				30000, 8, 4, true, "", formLayout);
		configuration.setMandatory(true);
		if((conf.getTaskConfiguration() == null) && (taskConfiguration == null)) {	
			configuration.setValue(translate("message.form.edittask.chosetypefirst"));
		}
		
		if((conf.getTaskConfiguration() != null) && (taskConfiguration != null)) {
			try {
				OutputObject parseResult = parser.parseString(taskConfiguration.getDocumentationText());
				documentationTextAsHTML = parseResult.toString();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			doc = uifactory.addRichTextElementForStringDataMinimalistic("documentation", "label.form.edittask.documentation",
					documentationTextAsHTML, -1, -1, false, formLayout, ureq.getUserSession(), getWindowControl());
			
			//doc = uifactory.addTextElement("documentation", "label.form.edittask.documentation",
			//		30000, documentationTextAsHTML, formLayout);
			doc.setEnabled(false);
			description = uifactory.addTextAreaElement("description", "label.form.edittask.description",
					30000, 4, 4, true, "", formLayout);
			comment = uifactory.addTextAreaElement("comment", "label.form.edittask.comment",
					30000, 4, 4, true, "", formLayout);
			
			configuration.setValue(taskConfiguration.getConfigurationText());
			description.setValue(taskConfiguration.getDescriptionText());
			comment.setValue(taskConfiguration.getAuthorComment());
			
		}
		
		if((conf.getTaskConfiguration() != null) && (taskConfiguration == null)) {
			
			try {
				OutputObject parseResult = parser.parseString(conf.getTaskConfiguration().getDocumentationText());
				documentationTextAsHTML = parseResult.toString();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			doc = uifactory.addRichTextElementForStringDataMinimalistic("documentation", "label.form.edittask.documentation",
					documentationTextAsHTML, -1, -1, false, formLayout, ureq.getUserSession(), getWindowControl());
			
			//doc = uifactory.addTextElement("documentation", "label.form.edittask.documentation",
			//		30000, documentationTextAsHTML, formLayout);
			doc.setEnabled(false);
			
			description = uifactory.addTextAreaElement("description", "label.form.edittask.description",
					30000, 4, 4, true, "", formLayout);
			comment = uifactory.addTextAreaElement("comment", "label.form.edittask.comment",
					30000, 4, 4, true, "", formLayout);
			
			configuration.setValue(conf.getTaskConfiguration().getConfigurationText());
			description.setValue(conf.getTaskConfiguration().getDescriptionText());
			comment.setValue(conf.getTaskConfiguration().getAuthorComment());
		}
		
		submit = uifactory.addFormSubmitButton("submit", formLayout);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		
		TaskConfiguration taskConfig = conf.getTaskConfiguration();
		
		String compareText = taskConfig.getConfigurationText();
		
		taskConfig.setConfigurationText(configuration.getValue());
		taskConfig.setAuthorComment(comment.getValue());
		

		BBautOLATConnector connector;
		try {
			connector = new BBautOLATConnector(conf);
		} catch (JDOMException e) {
			showInfo("error.form.editconnection.XMLerror");
			return;
		} catch (IOException e) {
			showInfo("error.form.editconnection.IOerror");
			return;
		} catch (AutolatConnectorException e) {
			showInfo("error.form.editconnection.Servererror");
			return;
		}
		
		TaskConfiguration standardConf = connector.getTaskConfiguration(conf.getTaskConfiguration().getTaskType());
		
		if(!standardConf.getConfigurationText().equals(taskConfig.getConfigurationText())) {
			taskConfig.setIsAltered(true);
		}
		
		if(taskConfig.getConfigurationText().equals(compareText)) 
			this.isConfTextChanged = false;
		else
			this.isConfTextChanged = true;
		
		boolean result = connector.verifyTaskConfiguration(taskConfig);
		if(result) {
			connector = null;
			valid = true;
			newVerifiedTask = taskConfig;
			fireEvent(ureq, FormEvent.DONE_EVENT);
		}
		else {
			String parseErrorAsHTML;
			try {
				parser = new XMLParser();
				OutputObject parseResult = parser.parseString(taskConfig.getLastError());
				parseErrorAsHTML = parseResult.toString();
			} catch (Exception e) {
				// should not happen
				parseErrorAsHTML = "???";
			}
			String[] params = {"<br>", parseErrorAsHTML};
			configuration.setErrorKey("error.form.edittask.invalidtaskconfiguration", params);
			connector = null;
			valid = false;
		}
	}


	/**
	 * persist the task configuration if the form is valid.
	 */
	public boolean persistCurrentTaskConfiguration() {
		
		if(this.valid) {
			conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
			
			BBautOLATConnector connector;
			try {
				connector = new BBautOLATConnector(conf);
			} catch (JDOMException e) {
				showInfo("error.form.editconnection.XMLerror");
				return false;
			} catch (IOException e) {
				showInfo("error.form.editconnection.IOerror");
				return false;
			} catch (AutolatConnectorException e) {
				showInfo("error.form.editconnection.Servererror");
				return false;
			}
			
			TaskConfiguration standardConf = connector.getTaskConfiguration(conf.getTaskConfiguration().getTaskType());
			TaskConfiguration taskConfig = conf.getTaskConfiguration();
			
			//update task configuration entries
			taskConfig.setConfigurationText(configuration.getValue());
			taskConfig.setDescriptionText(description.getValue());
			taskConfig.setAuthorComment(comment.getValue());
			taskConfig.setLastError("");
			
			//verify task conf again
			boolean result = connector.verifyTaskConfiguration(taskConfig);
			if(!result) {
				showInfo("error.form.edittask.persist");
				return false;
			}
			
			//set the isAltered-flag
			if(!standardConf.getConfigurationText().equals(taskConfig.getConfigurationText()))
				taskConfig.setIsAltered(true);
			
			//persist the task configuration
			TaskConfigurationManagerImpl.getInstance().updateTaskConfiguration(taskConfig);
			conf.setTaskConfiguration(taskConfig);
			
			//persist the configuration
			ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
			
			connector = null;
			return true;
		}
		return false;
		
	}
	/**
	 * changes the access status of all form elements.
	 * 
	 * @param status true if elements should be enabled
	 */
	public void setElementAccessStatus(boolean status) {
		
		if(comment != null)
			comment.setEnabled(status);
		if(description != null)
			description.setEnabled(status);
		if(configuration != null)
			configuration.setEnabled(status);
		if(submit != null) {
			submit.setEnabled(status);
		}
		//setAllFormElements(!status);
		//setDisplayOnly(!status);
		
		//if(!status) {
		//	configuration.setValue(translate("message.form.edittask.chosetypefirst"));
		//}
	}
	
	/**
	 * updates the value of the configuration text field from the database
	 */
	public void updateView() {
		configuration.setValue(conf.getTaskConfiguration().getConfigurationText());
	}
	/**
	 * sets the boolean value 
	 * @param isConfTextChanged
	 */
	public void setConfTextChanged(boolean isConfTextChanged) {
		this.isConfTextChanged = isConfTextChanged;
	}
	/**
	 * returns the boolean value
	 * @return isConfTextChanged
	 */
	public boolean isConfTextChanged() {
		return isConfTextChanged;
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}
	
}
