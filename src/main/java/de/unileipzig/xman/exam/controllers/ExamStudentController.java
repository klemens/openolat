package de.unileipzig.xman.exam.controllers;

import java.text.DateFormat;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;

import de.unileipzig.xman.admin.mail.MailManager;
import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.appointment.AppointmentManager;
import de.unileipzig.xman.appointment.tables.AppointmentStudentTableModel;
import de.unileipzig.xman.calendar.CalendarManager;
import de.unileipzig.xman.comment.CommentEntry;
import de.unileipzig.xman.comment.CommentManager;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;

public class ExamStudentController extends BasicController {
	
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(Exam.class);
	
	private Exam exam;
	private ElectronicStudentFile esf;
	private VelocityContainer baseVC;
	private VelocityContainer mainVC;
	
	private TableController subscriptionTable;
	private AppointmentStudentTableModel subscriptionTableModel;
	
	private ExamDetailsController examDetailsControler;
	private CloseableModalController examDetailsControlerModal;

	public ExamStudentController(UserRequest ureq, WindowControl wControl, Exam exam) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		this.exam = exam;
		
		init(ureq, wControl);
	}
	
	private void init(UserRequest ureq, WindowControl wControl) {
		esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(ureq.getIdentity());
		
		baseVC = new VelocityContainer("examLaunch", VELOCITY_ROOT + "/examBase.html", getTranslator(), this);
		
		baseVC.contextPut("examType", translate(exam.getIsOral() ? "oral" : "written"));
		baseVC.contextPut("regStartDate", exam.getRegStartDate() == null ? "n/a" : Formatter.getInstance(ureq.getLocale()).formatDateAndTime(exam.getRegStartDate()));
		baseVC.contextPut("regEndDate", exam.getRegEndDate() == null ? "n/a" : Formatter.getInstance(ureq.getLocale()).formatDateAndTime(exam.getRegEndDate()));
		baseVC.contextPut("signOffDate", exam.getSignOffDate() == null ? "n/a" : Formatter.getInstance(ureq.getLocale()).formatDateAndTime(exam.getSignOffDate()));
		baseVC.contextPut("earmarkedEnabled", translate(exam.getEarmarkedEnabled() ? "yes" : "no"));
		baseVC.contextPut("multiSubscriptionEnabled", translate(exam.getIsMultiSubscription() ? "yes" : "no"));
		String comments = exam.getComments();
		baseVC.contextPut("comments", comments.isEmpty() ? translate("examBase_html.comments.isEmpty") : comments);
		
		mainVC = new VelocityContainer("examStudentView", VELOCITY_ROOT + "/examStudentView.html", getTranslator(), this);
		baseVC.put("anyForm", mainVC);
		
		if(esf != null) {
			mainVC.contextPut("showSubscriptionTable", true);
			buildAppointmentTable(ureq, wControl);
		} else {
			mainVC.contextPut("showSubscriptionTable", false);
		}
		
		putInitialPanel(baseVC);
	}
	
	private void buildAppointmentTable(UserRequest ureq, WindowControl wControl) {
		removeAsListenerAndDispose(subscriptionTable);
		
		subscriptionTableModel = new AppointmentStudentTableModel(exam, esf, ureq.getLocale());
		
		TableGuiConfiguration tableGuiConfiguration = new TableGuiConfiguration();
		tableGuiConfiguration.setColumnMovingOffered(false);
		tableGuiConfiguration.setDownloadOffered(false);
		tableGuiConfiguration.setTableEmptyMessage(translate("ExamEditorController.appointmentTable.empty"));
		subscriptionTable = new TableController(tableGuiConfiguration, ureq, wControl, getTranslator());
		
		subscriptionTableModel.createColumns(subscriptionTable);
		subscriptionTable.setTableDataModel(subscriptionTableModel);
		subscriptionTable.setSortColumn(0, true);
		
		listenTo(subscriptionTable);
		
		mainVC.put("subscriptionTable", subscriptionTable.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// check that exam was not closed in the meantime
		if(ExamDBManager.getInstance().isClosed(exam)) {
			showInfo("ExamMainController.info.closed");
			return;
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		// check that exam was not closed in the meantime
		if(ExamDBManager.getInstance().isClosed(exam)) {
			showInfo("ExamMainController.info.closed");
			return;
		}
		
		// User clicked subscribe or unsubscribe
		if(source == subscriptionTable) {
			TableEvent tableEvent = (TableEvent) event;
			
			if(tableEvent.getActionId().equals(AppointmentStudentTableModel.ACTION_SUBSCRIBE)) {
				removeAsListenerAndDispose(examDetailsControler);
				
				//Ask for exam type and accountFor
				examDetailsControler = new ExamDetailsController(ureq, this.getWindowControl());
				examDetailsControler.setAppointment(subscriptionTableModel.getObject(tableEvent.getRowId()));
				
				listenTo(examDetailsControler);

				examDetailsControlerModal = new CloseableModalController(getWindowControl(), translate("close"), examDetailsControler.getInitialComponent());
				examDetailsControlerModal.activate();
			} else if(tableEvent.getActionId().equals(AppointmentStudentTableModel.ACTION_UNSUBSCRIBE)) {
				Protocol protocol = ProtocolManager.getInstance().findProtocolByIdentityAndAppointment(ureq.getIdentity(), subscriptionTableModel.getObject(tableEvent.getRowId()));

				// TODO CalendarManager.getInstance().deleteKalendarEventForExam(exam, ureq.getIdentity());
				
				// Email Remove
				BusinessControlFactory bcf = BusinessControlFactory.getInstance();
				MailManager.getInstance().sendEmail(
					translate("Mail.Remove.Subject",new String[] { ExamDBManager.getInstance().getExamName(exam) }),
					translate("Mail.Remove.Body",
						new String[] {
							ExamDBManager.getInstance().getExamName(exam),
							protocol.getIdentity().getUser().getProperty(UserConstants.LASTNAME, null) + ", " + protocol.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, null),
							DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getLocale()).format(protocol.getAppointment().getDate()),
							protocol.getAppointment().getPlace(),
							new Integer(protocol.getAppointment().getDuration()).toString(),
							protocol.getExam().getIsOral() ? translate("oral") : translate("written"),
							bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
						}),
					protocol.getIdentity()
				);
				
				if (exam.getIsOral()) {
					Appointment tempApp = AppointmentManager.getInstance().findAppointmentByID(protocol.getAppointment().getKey());
					tempApp.setOccupied(false);
					AppointmentManager.getInstance().updateAppointment(tempApp);
					tempApp = null;
				}
				
				// delete protocol
				ProtocolManager.getInstance().deleteProtocol(protocol);

				// reload esf
				esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(esf.getIdentity());
				
				// create comment
				String commentText = translate("ExamStudentController.studentDeRegisteredHimself", new String[] { "'" + exam.getName() + "'" });
				CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());
				
				// save changed esf
				ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);

				// update view
				subscriptionTableModel.update();
				subscriptionTable.modelChanged();
			}
		} else if(source == examDetailsControler) {
			Appointment appointment = AppointmentManager.getInstance().findAppointmentByID(examDetailsControler.getAppointment().getKey());
			
			// subscribe to exam
			if (event == Event.DONE_EVENT) {
				examDetailsControlerModal.deactivate();
				examDetailsControlerModal.dispose();
				examDetailsControlerModal = null;
				
				String examType = examDetailsControler.getChooseExamType() == Exam.ORIGINAL_EXAM ? translate("ExamDetailsController.first") : translate("ExamDetailsController.second");
				String accountFor = examDetailsControler.getAccountFor();
				String comment;
                if(accountFor.isEmpty())
                	comment = examType;
                else
                	comment = examType + ": " + accountFor;
                
                // reload esf
                esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(esf.getIdentity());
                
				// register student to the chosen appointment
				if(ProtocolManager.getInstance().registerStudent(appointment, esf, getTranslator(), exam.getEarmarkedEnabled(), comment)) {
					// create comment
					String commentText = translate("ExamStudentController.studentRegisteredHimself", new String[] { "'" + exam.getName() + "'" });
					CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());
					
					// save changed esf and appointment
					ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);
					AppointmentManager.getInstance().updateAppointment(appointment);
				} else {
					getWindowControl().setInfo(translate("ExamStudentController.info.appNotAvailable"));
				}
				
				// update view
				subscriptionTableModel.update();
				subscriptionTable.modelChanged();
			}
		}
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(subscriptionTable);
		removeAsListenerAndDispose(examDetailsControler);
		if(examDetailsControlerModal != null) {
			examDetailsControlerModal.dispose();
			examDetailsControlerModal = null;
		}
	}

}
