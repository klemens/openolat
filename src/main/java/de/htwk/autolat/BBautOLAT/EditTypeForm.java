package de.htwk.autolat.BBautOLAT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.jdom.JDOMException;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Connector.AutolatConnectorException;
import de.htwk.autolat.Connector.BBautOLATConnector;
import de.htwk.autolat.TaskType.TaskType;
import de.htwk.autolat.TaskType.TaskTypeManagerImpl;

/**
 * A form controller to select from existing task types.
 * @author werjo
 *
 */
public class EditTypeForm extends FormBasicController {
	
	public static final String NAME = "EditTypeForm";
	
	public static final String CONST_NOTCHOSEN = ".notchosen.";
	private Configuration conf;
	
	//GUI
	private SingleSelection selectType;
	private FormSubmit submit;
	
	/**
	 * Constructor
	 * @param name name of the controller.
	 * @param ureq the user request object
	 * @param wControl the window control object
	 * @param courseNodeID the ID of a given course node
	 */
	public EditTypeForm(String name, UserRequest ureq, WindowControl wControl, long courseID, long courseNodeID) {		
		super(ureq, wControl);
		
		conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);			
		
		initForm(flc, this, ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		
		BBautOLATConnector conn = null;
		try {
			conn = new BBautOLATConnector(conf);
		} catch (JDOMException e) {
			showInfo("error.form.editconnection.XMLerror");
		} catch (IOException e) {
			showInfo("error.form.editconnection.IOerror");
		} catch (AutolatConnectorException e) {
			showInfo("error.form.editconnection.Servererror");
		}
		if(conn != null) {
			ArrayList<TaskType> typeList = (ArrayList<TaskType>) conn.getTaskTypes();
			String types[] = new String[typeList.size()+1];
			String keys[] = new String[typeList.size()+1];
			types[0] = translate("label.selectelem.edittype.notypechosen");
			keys[0] = CONST_NOTCHOSEN;
			Iterator<TaskType> it = typeList.iterator();
			int i=0;
			while(it.hasNext()) {
				TaskType temp = it.next();
				i++;
				types[i] = temp.getType();
				keys[i] = String.valueOf(temp.getKey());
			}
		
			selectType = uifactory.addDropdownSingleselect("selectType", "label.form.edittype.typeselect", formLayout,
					keys, types, null);
			selectType.setMandatory(true);
		
			if(conf.getTaskConfiguration()!=null) {
				selectType.select(String.valueOf(conf.getTaskConfiguration().getTaskType().getKey()), true);
			}
		} else {
			String types[] = new String[1];
			String keys[] = new String[1];
			types[0] = translate("label.selectelem.edittype.notypechosen");
			keys[0] = CONST_NOTCHOSEN;
			selectType = uifactory.addDropdownSingleselect("selectType", "label.form.edittype.typeselect", formLayout,
					keys, types, null);
			selectType.setMandatory(true);
		}
		
		submit = uifactory.addFormSubmitButton("submit", "label.form.edittype.submit", formLayout);		
	}
	/**
	 * validate the form
	 * @return true if the form is valid
	 */
	public boolean validate() {
			
			if(selectType.getSelectedKey().equals(CONST_NOTCHOSEN)) {
				selectType.setErrorKey("error.selectelem.edittype.selectatype", null);
				return false;
			}
		
		return true;
	}
	/**
	 * @return returns the selected task type
	 */
	public TaskType getSelectedType() {
		TaskType result = TaskTypeManagerImpl.getInstance().loadTaskTypeByID(Long.parseLong(selectType.getSelectedKey()));
		return result;	
	}


	@Override
	protected void formOK(UserRequest ureq) {
		if(validate()) {
			fireEvent(ureq, FormEvent.DONE_EVENT);
		}
		
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * enable/disable all Form elements
	 * @param enable
	 */
	public void setEnable(boolean enable) {
		submit.setEnabled(enable);
		selectType.setEnabled(enable);
	}
	
}
