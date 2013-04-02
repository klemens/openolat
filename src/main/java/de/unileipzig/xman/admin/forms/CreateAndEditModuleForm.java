package de.unileipzig.xman.admin.forms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityManager;
import org.olat.core.util.Util;

import de.unileipzig.xman.admin.ExamAdminSite;
import de.unileipzig.xman.admin.ExamAdminSiteDef;
import de.unileipzig.xman.module.Module;
import de.unileipzig.xman.module.ModuleManager;

/**
 * 
 * @author blutz
 */
public class CreateAndEditModuleForm extends FormBasicController {

	private TextElement name, number;
	private TextElement personInCharge;
	private TextElement description;
	private Module module;
	private Identity identity;
	private Submit submit;
	private Cancel cancel;

	public CreateAndEditModuleForm(UserRequest ureq, WindowControl wControl,
			String formName, Module module) {
		super(ureq, wControl);
		this.module = module;
	//	this.setTranslator(Util.createPackageTranslator("de.unileipzig.xman.admin", ureq.getLocale()));
		this.setTranslator(new PackageTranslator("de.unileipzig.xman.admin", ureq.getLocale()));
		initForm(ureq);
	}

	
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {

		name = uifactory.addTextElement("name", "CreateAndEditModuleForm.name",
				50, null, formLayout);
		name.setMandatory(true);
		if (module != null)
			name.setValue(this.module.getName());

		number = uifactory.addTextElement("number",
				"CreateAndEditModuleForm.number", 50, null, formLayout);
		number.setMandatory(false);
		if (module != null)
			number.setValue(module.getModuleNumber());

		description = uifactory.addTextAreaElement(
				"CreateAndEditModuleForm.description", 5, 50, null, formLayout);
		description.setMandatory(true);
		if (module != null)
			description.setValue(this.module.getDescription());

		personInCharge = uifactory
				.addTextElement("personInCharge",
						"CreateAndEditModuleForm.personInCharge", 50,
						"Loginname", formLayout);
		personInCharge.setMandatory(true);
		if (module != null)
			personInCharge.setValue(module.getPersonInCharge().getName());

		// submit / cancel keys
		submit = uifactory.addFormSubmitButton("save", "submitKey", formLayout);
//		cancel = uifactory.addFormCancelButton("cancel", null, ureq,
//				getWindowControl());

	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {

		boolean valid = !name.isEmpty("CreateAndEditModuleForm.name.isEmpty")
				&& !description
						.isEmpty("CreateAndEditModuleForm.description.isEmpty")
				&& !personInCharge
						.isEmpty("CreateAndEditModuleForm.personInCharge.isEmpty");
		// && !number.isEmpty("CreateAndEditModuleForm.number.isEmpty");

		IdentityManager im = (IdentityManager) CoreSpringFactory
				.getBean("core.id.IdentityManager");
		identity = im.findIdentityByName(personInCharge.getValue());

		if (!number.getValue().equals("")) {

			if (!this.isValidModuleNumber()) {
				valid = false;
				number.setErrorKey("CreateAndEditModuleForm.number.isEmpty",
						null);
			}
		}

		if (identity == null) {

			valid = false;
			personInCharge.setErrorKey(
					"CreateAndEditModuleForm.personInCharge.noIdentityFound",
					null);
		}

		if (module == null) {
			if (ModuleManager.getInstance().findModuleByName(name.getValue()) != null) {
				valid = false;
				name.setErrorKey(
						"CreateAndEditModuleForm.name.error.duplicate", null);
			}
		}
		if (module != null && !module.getName().equals(name.getValue())) {
			if (ModuleManager.getInstance().findModuleByName(name.getValue()) != null) {
				valid = false;
				name.setErrorKey(
						"CreateAndEditModuleForm.name.error.duplicate", null);
			}
		}
		return valid;
	}

	/**
	 * @return the value of the field name
	 */
	public String getName() {

		return name.getValue();
	}

	/**
	 * @return the value of the field description
	 */
	public String getDescription() {

		return description.getValue();
	}

	public Identity getPersonInCharge() {

		return identity;
	}

	public String getModuleNumber() {

		return number.getValue();
	}

	public boolean isValidModuleNumber() {

		String address = this.number.getValue();

		Pattern p = Pattern.compile("[0-9]{2}-[0-9]{3}-[0-9]{4}");
		Matcher m = p.matcher(address);
		return m.matches();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_VALIDATION_OK);

	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Form.EVNT_FORM_CANCELLED);
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}
}
