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

public class ExamStudentController extends BasicController implements ExamController {
	
	private Exam exam;
	private ElectronicStudentFile esf;
	private VelocityContainer mainVC;
	
	private TableController subscriptionTable;
	private AppointmentStudentTableModel subscriptionTableModel;
	
	private ExamStudentRegistrationDetailsController examStudentRegistrationDetailsControler;
	private CloseableModalController examStudentRegistrationDetailsControlerModal;
	private ExamDetailsController examDetailsController;

	public ExamStudentController(UserRequest ureq, WindowControl wControl, Exam exam) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		this.exam = exam;
		
		mainVC = new VelocityContainer("examStudentView", Exam.class, "examStudentView", getTranslator(), this);

		examDetailsController = new ExamDetailsController(ureq, wControl, getTranslator(), exam, false);
		mainVC.put("examDetails", examDetailsController.getInitialComponent());

		init(ureq);

		putInitialPanel(mainVC);
	}
	
	private void init(UserRequest ureq) {
		esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(ureq.getIdentity());
		
		if(esf != null) {
			mainVC.contextPut("showSubscriptionTable", true);
			buildAppointmentTable(ureq);
		} else {
			mainVC.contextPut("showSubscriptionTable", false);
		}
	}
	
	private void buildAppointmentTable(UserRequest ureq) {
		removeAsListenerAndDispose(subscriptionTable);
		
		subscriptionTableModel = new AppointmentStudentTableModel(exam, esf, ureq.getLocale());
		
		TableGuiConfiguration tableGuiConfiguration = new TableGuiConfiguration();
		tableGuiConfiguration.setDownloadOffered(false);
		tableGuiConfiguration.setTableEmptyMessage(translate("ExamEditorController.appointmentTable.empty"));
		subscriptionTable = new TableController(tableGuiConfiguration, ureq, getWindowControl(), getTranslator());
		
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
				removeAsListenerAndDispose(examStudentRegistrationDetailsControler);
				
				//Ask for exam type and accountFor
				examStudentRegistrationDetailsControler = new ExamStudentRegistrationDetailsController(ureq, this.getWindowControl());
				examStudentRegistrationDetailsControler.setAppointment(subscriptionTableModel.getObject(tableEvent.getRowId()));
				
				listenTo(examStudentRegistrationDetailsControler);

				examStudentRegistrationDetailsControlerModal = new CloseableModalController(getWindowControl(), translate("close"), examStudentRegistrationDetailsControler.getInitialComponent());
				examStudentRegistrationDetailsControlerModal.activate();
			} else if(tableEvent.getActionId().equals(AppointmentStudentTableModel.ACTION_UNSUBSCRIBE)) {
				if(!ExamDBManager.getInstance().canUnsubscribe(exam)) {
					showError("ExamStudentController.info.unsubscriptionPeriodOver");
					return;
				}

				Protocol protocol = ProtocolManager.getInstance().findProtocolByIdentityAndAppointment(ureq.getIdentity(), subscriptionTableModel.getObject(tableEvent.getRowId()));
				
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
		} else if(source == examStudentRegistrationDetailsControler) {
			// subscribe to exam
			if (event == Event.DONE_EVENT) {
				examStudentRegistrationDetailsControlerModal.deactivate();
				examStudentRegistrationDetailsControlerModal.dispose();
				examStudentRegistrationDetailsControlerModal = null;

				if(!ExamDBManager.getInstance().canSubscribe(exam)) {
					showError("ExamStudentController.info.subscriptionPeriodOver");
					return;
				}

				String examType = examStudentRegistrationDetailsControler.getChooseExamType() == Exam.ORIGINAL_EXAM ? translate("ExamStudentRegistrationDetailsForm.first") : translate("ExamStudentRegistrationDetailsForm.second");
				String accountFor = examStudentRegistrationDetailsControler.getAccountFor();
				String comment;
                if(accountFor.isEmpty())
                	comment = examType;
                else
                	comment = examType + ": " + accountFor;
                
                // reload appointment and esf
                Appointment appointment = AppointmentManager.getInstance().findAppointmentByID(examStudentRegistrationDetailsControler.getAppointment().getKey());
                esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(esf.getIdentity());
                
				// register student to the chosen appointment
                if(!appointment.getOccupied()) {
					if(exam.getEarmarkedEnabled()) {
						ProtocolManager.getInstance().earmarkStudent(appointment, esf, comment);
					} else {
						ProtocolManager.getInstance().registerStudent(appointment, esf, comment);

						// create comment
						String commentText = translate("ExamStudentController.studentRegisteredHimself", new String[] { "'" + exam.getName() + "'" });
						CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());
					}

					// save changed esf and appointment
					ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);
					AppointmentManager.getInstance().updateAppointment(appointment);
				} else {
					showInfo("ExamStudentController.info.appNotAvailable");
				}
				
				// update view
				subscriptionTableModel.update();
				subscriptionTable.modelChanged();
			}
		}
	}

	@Override
	public void updateExam(UserRequest ureq, Exam newExam) {
		this.exam = newExam;
		init(ureq);
		examDetailsController.updateExam(ureq, newExam);
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(subscriptionTable);
		removeAsListenerAndDispose(examStudentRegistrationDetailsControler);
		if(examStudentRegistrationDetailsControlerModal != null) {
			examStudentRegistrationDetailsControlerModal.dispose();
			examStudentRegistrationDetailsControlerModal = null;
		}
		examDetailsController.dispose();
	}

}
