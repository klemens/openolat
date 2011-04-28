/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.group.ui.edit;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;

/**
 * 
 * Description:<br>
 * Add a check box to the standard access control controller
 * 
 * <P>
 * Initial Date:  26 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
//fxdiff VCRP-1,2: access control of resources
public class BusinessGroupEditAccessController extends FormBasicController {
	
	private FormSubmit saveButton;
	private MultipleSelectionElement openBg;
	private final AccessConfigurationController configController;
	
	private final BusinessGroup businessGroup;
	
	public BusinessGroupEditAccessController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		
		this.businessGroup = businessGroup;
		OLATResource resource = OLATResourceManager.getInstance().findResourceable(businessGroup);
		configController = new AccessConfigurationController(ureq, wControl, resource, businessGroup.getName(), mainForm);
		listenTo(configController);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add(configController.getInitialFormItem());
		
		String[] keys = new String[]{"xx"};
		String[] values = new String[]{translate("chkBox.open")};
		openBg = uifactory.addCheckboxesHorizontal("fieldset.legend.open", formLayout, keys, values, null);
		openBg.setLabel(null, null);
		if(businessGroup.getVisibleToNonMembers() != null && businessGroup.getVisibleToNonMembers().booleanValue()) {
			openBg.select("xx", true);
		}

		final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		formLayout.add(buttonGroupLayout);
		
		saveButton = uifactory.addFormSubmitButton("save", formLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public boolean isVisibleToNonMembers() {
		return (openBg.isMultiselect() && openBg.isSelected(0));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		configController.formOK(ureq);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
