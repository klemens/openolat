package de.unileipzig.xman.esf.form;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;

import de.unileipzig.xman.illness.IllnessReportEntry;

/**
 * 
 * Description:<br>
 * TODO: gerb Class Description for ESFIllnessReportCreateForm
 * 
 * <P>
 * Initial Date: 29.05.2008 <br>
 * 
 * @author gerb
 */
public class ESFIllnessReportCreateAndEditForm extends FormBasicController {

	private DateChooser toDate;
	private DateChooser fromDate;
	private Submit submit;
	private Cancel cancel;
	private IllnessReportEntry entry;

	/**
	 * 
	 * @param name
	 * @param translator
	 */
	public ESFIllnessReportCreateAndEditForm(UserRequest ureq,
			WindowControl wControl, String name, Translator translator,
			IllnessReportEntry entry) {
		super(ureq, wControl);
		this.entry=entry;
		this.setTranslator(new PackageTranslator("de.unileipzig.xman.esf", ureq.getLocale()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		fromDate = uifactory.addDateChooser("fromDate",
				"ESFIllnessReportCreateForm.fromDate", SimpleDateFormat
						.getInstance().format(new Date()), formLayout);
		fromDate.setMandatory(true);
		fromDate.setDisplaySize(20);
		fromDate.setMaxLength(16);
		fromDate.setDateChooserTimeEnabled(true);
		fromDate.setDateChooserDateFormat("%d.%m.%Y");
		if (entry != null)
			fromDate.setDate(entry.getFromDate());

		toDate = uifactory.addDateChooser("toDate",
				"ESFIllnessReportCreateForm.toDate", SimpleDateFormat
						.getInstance().format(new Date()), formLayout);
		toDate.setMandatory(true);
		toDate.setDisplaySize(20);
		toDate.setMaxLength(16);
		toDate.setDateChooserTimeEnabled(true);
		toDate.setDateChooserDateFormat("%d.%m.%Y");
		if (entry != null)
			toDate.setDate(entry.getToDate());

		// submit / cancel keys
		submit = uifactory.addFormSubmitButton("save", "save", formLayout);
		// cancel = uifactory.addFormCancelButton("cancel", null, ureq,
		// getWindowControl());

	}

	/**
	 * 
	 * @see org.olat.core.gui.components.form.Form#validate()
	 */
	public boolean validateFormLogic(UserRequest ureq) {

		boolean valid = false;
		boolean validDate = false;

		// both dates should not be empty
		valid = !toDate.isEmpty("ESFIllnessReportCreateForm.dateEmpty")
				&& !fromDate.isEmpty("ESFIllnessReportCreateForm.dateEmpty");

		// should be a valid date, not something like 32.13.2008 and they
		// shouldn't be empty

		// TODO: Automatische Validierung?
		// validDate = valid
		// && toDate.
		// .validDate("ESFIllnessReportCreateForm.isNonValidDate")
		// && fromDate
		// .validDate("ESFIllnessReportCreateForm.isNonValidDate");

		// both dates are correct
		// if (validDate) {
		// start Date has to be before the end date
		if (fromDate.getDate().getTime() <= toDate.getDate().getTime()) {

			// all dates are correct -> validation ok
			validDate = validDate && true;
		} else {

			// end date is before start date -> validation not ok
			validDate = validDate && false;
			this.toDate.setErrorKey(
					"ESFIllnessReportCreateFrom.fromBeforeToDate", null);
		}
		// }

		return validDate;
	}

	public Date getToDate() {

		return this.toDate.getDate();
	}

	public Date getFromDate() {

		return this.fromDate.getDate();
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
