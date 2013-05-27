package de.unileipzig.xman.exam.forms;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.exam.Exam;

/**
 * 
 * @author
 */
public class CreateAndEditAppointmentForm extends FormBasicController {

	private DateChooser date;
	private TextElement place;
	private IntegerElement duration, count, pause;
	private boolean isOral;
	private Appointment app;
	private Submit submit;
	private Cancel cancel;

	/**
	 * creates the createAndEditAppointmentForm
	 * 
	 * @param name
	 *            the name of the form
	 * @param translator
	 *            the translator
	 * @param isOral
	 *            true if the exam is oral
	 * @param app
	 *            the appointment to edit
	 */
	public CreateAndEditAppointmentForm(UserRequest ureq,
			WindowControl wControl, String name, Translator translator,
			boolean isOral, Appointment app) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));

		this.isOral = isOral;
		this.app = app;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		date = uifactory.addDateChooser("date", "CreateAndEditAppointmentForm.date", new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()), formLayout);
		date.setCustomDateFormat("dd.MM.yyyy HH:mm");
		date.setDateChooserDateFormat("%d.%m.%Y %H:%M");
		date.setMandatory(true);
		date.setMaxLength(16);
		date.setDisplaySize(20);
		date.setDateChooserTimeEnabled(true);

		place = uifactory.addTextElement("place",
				"CreateAndEditAppointmentForm.place", 100, "", formLayout);
		place.setMandatory(true);

		duration = uifactory.addIntegerElement("duration",
				"CreateAndEditAppointmentForm.duration", 0, formLayout);
		duration.setExampleKey("CreateAndEditAppointmentForm.minutes.example",
				null);
		duration.setDisplaySize(5);
		duration.setMaxLength(3);
		duration.setMandatory(true);
		duration.showExample(true);

		if (isOral && (app == null)) {

			pause = uifactory.addIntegerElement("pause",
					"CreateAndEditAppointmentForm.pause", 0, formLayout);
			pause.setExampleKey("CreateAndEditAppointmentForm.minutes.example",
					null);
			pause.setDisplaySize(5);
			pause.setMaxLength(3);
			pause.setMandatory(true);
			duration.showExample(true);

			count = uifactory.addIntegerElement("count",
					"CreateAndEditAppointmentForm.count", 0, formLayout);
			count.setDisplaySize(3);
			count.setMaxLength(2);
			count.setMandatory(true);
		}

		if (app != null) {

			date.setDate(app.getDate());
			place.setValue(app.getPlace());
			duration.setIntValue(app.getDuration());
		}

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		submit = uifactory.addFormSubmitButton("save", "submitKey", buttonGroupLayout);
	}

	/**
	 * @see org.olat.core.gui.components.form.Form#validate()
	 */
	public boolean validate() {

		boolean valid = false;
		valid = !this.date.isEmpty("CreateAndEditAppointmentForm.error")
				&& !this.place.isEmpty("CreateAndEditAppointmentForm.error")
				&& !this.duration.isEmpty("CreateAndEditAppointmentForm.error");
		// TODO: Standardcheck?
		// && this.duration
		// .isInteger("CreateAndEditAppointmentForm.error");

		if (isOral && (app == null)) {
			valid &= !this.count.isEmpty("CreateAndEditAppointmentForm.error")
					&& !this.pause
							.isEmpty("CreateAndEditAppointmentForm.error");
			// && this.count
			// .isInteger("CreateAndEditAppointmentForm.error")
			// && this.pause
			// .isInteger("CreateAndEditAppointmentForm.error");
		}

		// valid = valid
		// && this.date
		// .validDate("CreateAndEditAppointmentForm.wrongDate");

		return valid;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count.getIntValue();
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date.getDate();
	}

	/**
	 * @return the duration
	 */
	public int getDuration() {
		return duration.getIntValue();
	}

	/**
	 * @return the pause
	 */
	public int getPause() {
		return pause.getIntValue();
	}

	/**
	 * @return the place
	 */
	public String getPlace() {
		return place.getValue();
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
