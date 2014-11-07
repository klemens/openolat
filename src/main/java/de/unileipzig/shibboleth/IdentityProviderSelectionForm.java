package de.unileipzig.shibboleth;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

class IdentityProviderSelectionForm extends FormBasicController {
	private SingleSelection ipSelect;
	private String[] identityProviders;

	public IdentityProviderSelectionForm(UserRequest ureq, WindowControl wControl, String id, Translator translator, String[] identityProviders) {
		super(ureq, wControl, id, FormBasicController.LAYOUT_VERTICAL);
		setTranslator(translator);
		this.identityProviders = identityProviders;
		initForm(ureq);
	}

	public String getIdentityProvider() {
		return ipSelect.getSelectedValue();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer selectWrapper = FormLayoutContainer.createInputGroupLayout("selectWrapper", getTranslator(), "<i class='o_icon o_icon-fw o_icon_provider_shibboleth'> </i>", null);
		formLayout.add(selectWrapper);
		ipSelect = uifactory.addDropdownSingleselect("simpleShibboleth.ipSelection", selectWrapper, identityProviders, identityProviders, null);
		ipSelect.setLabel(null, null);
		uifactory.addFormSubmitButton("rightAddOn", "simpleShibboleth.ipSelection.submit", selectWrapper);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
}