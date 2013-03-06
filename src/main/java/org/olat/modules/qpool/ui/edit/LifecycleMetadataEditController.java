/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.qpool.ui.edit;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionPoolService;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.QuestionItemMetadatasController;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LifecycleMetadataEditController extends FormBasicController {
	
	private TextElement versionEl;
	private SingleSelection statusEl;
	
	private QuestionItem item;
	private final QuestionPoolService qpoolService;

	public LifecycleMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QuestionItemMetadatasController.class, ureq.getLocale(), getTranslator()));
		
		this.item = item;
		qpoolService = CoreSpringFactory.getImpl(QuestionPoolService.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("lifecycle");
		
		versionEl = uifactory.addTextElement("lifecycle.version", "lifecycle.version", 50, "", formLayout);
		
		String[] statusTypeKeys = QuestionStatus.valueString();
		String[] statusTypeValues = new String[statusTypeKeys.length];
		for(int i=statusTypeKeys.length; i-->0; ) {
			statusTypeValues[i] = translate("lifecycle.status." + statusTypeKeys[i]);
		}
		statusEl = uifactory.addDropdownSingleselect("lifecycle.status", "lifecycle.status", formLayout,
				statusTypeKeys, statusTypeValues, null);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		versionEl.clearError();
		String version = versionEl.getValue();
		if(!StringHelper.containsNonWhitespace(version)) {
			versionEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		statusEl.clearError();
		if(!statusEl.isOneSelected()) {
			statusEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		return allOk &= super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			if(statusEl.isOneSelected()) {
				itemImpl.setStatus(statusEl.getSelectedKey());
			}
			
			itemImpl.setItemVersion(versionEl.getValue());
		}
		item = qpoolService.updateItem(item);
		fireEvent(ureq, new QItemEdited(item));
	}
}