package de.unileipzig.xman.exam.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.CatalogManager;

import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;

/**
 * This controller displays details about an exam in a two column table.
 *
 * This includes the name and type of the exam, the registration- and
 * unregistration-phase (including status icons and remaining days) and
 * the status of earmarking and multi-subscription.
 */
public class ExamDetailsController extends BasicController implements ExamController {
	Exam exam;
	private boolean showWarnings;

	private VelocityContainer baseVC;

	private static final String STATUS_NOT_YET_STARTED = "notYetStarted";
	private static final String STATUS_STARTED = "started";
	private static final String STATUS_ENDED = "ended";

	public ExamDetailsController(final UserRequest ureq, WindowControl wControl, final Translator translator, Exam exam, boolean showWarnings) {
		super(ureq, wControl);

		setTranslator(translator);
		this.exam = exam;
		this.showWarnings = showWarnings;

		baseVC = new VelocityContainer("examDetails", Exam.class, "examDetailsView", translator, this);

		init();
		updateTime(new Date());

		putInitialPanel(baseVC);
	}

	private void init() {
		baseVC.contextPut("examName", exam.getName());
		baseVC.contextPut("examType", translate(exam.getIsOral() ? "oral" : "written"));
		baseVC.contextPut("examTypeCssClass", exam.getIsOral() ? "o_icon_exam_oral" : "o_icon_exam_written");
		baseVC.contextPut("earmarkedEnabled", exam.getEarmarkedEnabled());
		baseVC.contextPut("multiSubscriptionEnabled", exam.getIsMultiSubscription());

		if(exam.getComments().isEmpty()) {
			baseVC.contextPut("showExamDescription", false);
		} else {
			baseVC.contextPut("showExamDescription", true);
			baseVC.contextPut("examDescription", exam.getComments());
		}

		if(ExamDBManager.getInstance().isClosed(exam)) {
			showWarnings = false;
			baseVC.contextPut("showClosedInfo", true);
		}

		if(showWarnings) {
			RepositoryEntry re = ExamDBManager.getInstance().findRepositoryEntryOfExam(exam);

			// private warning
			baseVC.contextPut("showPrivateWarning", re.getAccess() < RepositoryEntry.ACC_USERS);

			// catalog warning
			CatalogManager cm = CoreSpringFactory.getImpl(CatalogManager.class);
			baseVC.contextPut("showCatalogWarning", cm.getCatalogEntriesReferencing(re).size() == 0);
		} else {
			baseVC.contextPut("showPrivateWarning", false);
			baseVC.contextPut("showCatalogWarning", false);

		}

		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm", getLocale());
		baseVC.contextPut("registationPhase", translate("ExamDetailsController.registration", new String[] { format.format(exam.getRegStartDate()), format.format(exam.getRegEndDate())}));
		baseVC.contextPut("unregistrationPhase", translate("ExamDetailsController.unregistration", format.format(exam.getSignOffDate())));
	}

	/**
	 * This function calculates the status of registration and unregistration
	 * based on the current time. You can call this whenever the site is shown.
	 */
	public void updateTime(Date now) {
		// Determine the correct color for the status icons
		// and calculate the remaining days to display
		String registrationStatus, unregistrationStatus;
		String registrationMessage = "";
		String unregistrationMessage = "";
		if(now.before(exam.getRegStartDate())) {
			registrationStatus = STATUS_NOT_YET_STARTED;
			registrationMessage = timespanToString(now, exam.getRegStartDate(), "ExamDetailsController.timespan.start.");
			unregistrationStatus = STATUS_NOT_YET_STARTED;
		} else if(now.before(exam.getRegEndDate())) {
			registrationStatus = STATUS_STARTED;
			registrationMessage = timespanToString(now, exam.getRegEndDate(), "ExamDetailsController.timespan.end.");
			unregistrationStatus = STATUS_NOT_YET_STARTED;
		} else if(now.before(exam.getSignOffDate())) {
			registrationStatus = STATUS_ENDED;
			unregistrationStatus = STATUS_STARTED;
			unregistrationMessage = timespanToString(now, exam.getSignOffDate(), "ExamDetailsController.timespan.end.");
		} else {
			registrationStatus = STATUS_ENDED;
			unregistrationStatus = STATUS_ENDED;
		}
		baseVC.contextPut("registrationStatus", registrationStatus);
		baseVC.contextPut("unregistrationStatus", unregistrationStatus);
		baseVC.contextPut("registrationMessage", registrationMessage);
		baseVC.contextPut("unregistrationMessage", unregistrationMessage);
	}

	/**
	 * Update the exam. This makes the controller redraw the view
	 */
	public void updateExam(UserRequest ureq, Exam exam) {
		this.exam = exam;
		init();
		updateTime(new Date());
	}

	/**
	 * Calculates the difference between the given two dates and returns
	 * a text version of it where only the most significant of date, hour
	 * or minute is included.
	 * It uses the default translator and creates the key from the prefix
	 * plus one of hour, day, days, hours, minutes; all but the first two
	 * get the amount of time as a parameter.
	 */
	private String timespanToString(Date start, Date end, String prefix) {
		long diff = end.getTime() - start.getTime();

		// diff contains difference in milliseconds
		long minutes = diff / (60 * 1000) % 60;
		long hours = diff / (60 * 60 * 1000) % 24;
		long days = diff / (24 * 60 * 60 * 1000);

		if(days == 1) {
			return translate(prefix + "day");
		} else if(days > 1) {
			return translate(prefix + "days", String.valueOf(days));
		} else if(hours == 1) {
			return translate(prefix + "hour");
		} else if(hours > 1) {
			return translate(prefix + "hours", String.valueOf(hours));
		} else {
			return translate(prefix + "minutes", String.valueOf(minutes));
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// no events
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
}
