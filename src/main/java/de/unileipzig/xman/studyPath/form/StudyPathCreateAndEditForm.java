package de.unileipzig.xman.studyPath.form;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;

import de.unileipzig.xman.studyPath.StudyPath;

public class StudyPathCreateAndEditForm extends FormBasicController {

	private StudyPath studyPath;
	private TextElement name;
	private Submit submit;
	private Cancel cancel;
	private Translator translator;

	public StudyPathCreateAndEditForm(UserRequest ureq, WindowControl wControl,
			String formName, Translator translator, StudyPath studyPath) {
		super(ureq, wControl);
		this.studyPath = studyPath;
		this.translator = translator;
		this.setTranslator(new PackageTranslator("de.unileipzig.xman.studyPath", ureq.getLocale()));
		initForm(ureq);

	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {

		return !name.isEmpty("CreateAndEditModuleForm.name.empty");
	}

	public String getName() {

		return this.name.getValue();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		name = uifactory.addTextElement("name", "CreateAndEditModuleForm.name",
				100, null, formLayout);
		name.setMandatory(true);
		if (studyPath != null)
			name.setValue(translator.translate(studyPath.getI18nKey()));

		// submit / cancel keys
		submit = uifactory.addFormSubmitButton("save", "blubb", formLayout);
		// cancel = uifactory.addFormCancelButton("cancel", null, ureq,
		// getWindowControl());
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

}
