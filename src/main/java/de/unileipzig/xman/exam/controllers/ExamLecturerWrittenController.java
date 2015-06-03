package de.unileipzig.xman.exam.controllers;

import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.user.HomePageConfigManagerImpl;
import org.olat.user.UserInfoMainController;

import de.unileipzig.xman.admin.mail.MailManager;
import de.unileipzig.xman.admin.mail.form.MailForm;
import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.appointment.AppointmentManager;
import de.unileipzig.xman.appointment.tables.AppointmentLecturerOralTableModel;
import de.unileipzig.xman.comment.CommentManager;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.esf.form.ESFCommentCreateAndEditForm;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.exam.forms.EditMarkForm;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;
import de.unileipzig.xman.protocol.tables.ProtocolLecturerWrittenModel;

public class ExamLecturerWrittenController extends BasicController implements ExamController {
	
	private Exam exam;
	private VelocityContainer mainVC;
	
	private TableController protocolTable;
	private ProtocolLecturerWrittenModel protocolTableModel;

	private CloseableModalController cmc;
	
	private Link refreshTableButton;

	private Link userAddButton;
	private UserSearchController userSearchController;
	private Appointment userSearchControllerAppointmentHolder;

	private EditMarkForm editMarkForm;
	private List<Protocol> editMarkFormProtocolHolder;

	private ESFCommentCreateAndEditForm editCommentForm;
	private List<Protocol> editCommentFormProtocolHolder;

	private MailForm editMailForm;
	private List<Protocol> editMailFormProtocolHolder;

	private ExamDetailsController examDetailsController;

	/**
	 * The exam given MUST be written, otherwise InvalidParameterException is thrown!
	 * 
	 * @param ureq
	 * @param wControl
	 * @param exam The written exam to manage
	 * @throws InvalidParameterException
	 */
	protected ExamLecturerWrittenController(UserRequest ureq, WindowControl wControl, Exam exam) {
		super(ureq, wControl);
		
		if(exam.getIsOral())
			throw new InvalidParameterException("Expected written exam, got oral one");
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		this.exam = exam;
		
		mainVC = new VelocityContainer("examStudentView", Exam.class, "examLecturerWrittenView", getTranslator(), this);

		examDetailsController = new ExamDetailsController(ureq, wControl, getTranslator(), exam, true);
		mainVC.put("examDetails", examDetailsController.getInitialComponent());

		init(ureq);

		putInitialPanel(mainVC);
	}
	
	private void init(UserRequest ureq) {
		List<Appointment> apps = AppointmentManager.getInstance().findAllAppointmentsByExamId(exam.getKey());
		if(apps.size() == 1) {
			mainVC.contextPut("showProtocolTable", true);
			
			mainVC.contextPut("appDate", Formatter.getInstance(ureq.getLocale()).formatDateAndTime(apps.get(0).getDate()));
			mainVC.contextPut("appLocation", apps.get(0).getPlace());
			mainVC.contextPut("appDuration", new Integer(apps.get(0).getDuration()) + " min");
			
			userAddButton = LinkFactory.createButton("ExamLecturerWrittenController.userAddButton", mainVC, this);
			refreshTableButton = LinkFactory.createButton("ExamLecturerWrittenController.refreshTable", mainVC, this);
			
			buildProtocolTable(ureq);
		} else {
			mainVC.contextPut("showProtocolTable", false);
		}
	}
	
	private void buildProtocolTable(UserRequest ureq) {
		removeAsListenerAndDispose(protocolTable);
		
		protocolTableModel = new ProtocolLecturerWrittenModel(exam, ureq.getLocale());
		
		TableGuiConfiguration tableGuiConfiguration = new TableGuiConfiguration();
		tableGuiConfiguration.setDownloadOffered(true);
		tableGuiConfiguration.setTableEmptyMessage(translate("ExamLecturerWrittenController.protocolTable.empty"));
		tableGuiConfiguration.setMultiSelect(true);
		tableGuiConfiguration.setPreferencesOffered(true, "ExamLecturerWrittenController.appointmentTable");
		protocolTable = new TableController(tableGuiConfiguration, ureq, getWindowControl(), getTranslator());
		
		protocolTableModel.createColumns(protocolTable);
		protocolTable.setTableDataModel(protocolTableModel);
		protocolTable.setSortColumn(protocolTableModel.getColumnCount(), false); // sort by last, zerobased,  +1 for multiselect
		
		listenTo(protocolTable);
		
		mainVC.put("protocolTable", protocolTable.getInitialComponent());
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		// check that exam was not closed in the meantime
		if(ExamDBManager.getInstance().isClosed(exam)) {
			showInfo("ExamMainController.info.closed");
			return;
		}
		
		if(source == protocolTable) {
			if(event instanceof TableEvent) {
				TableEvent tableEvent = (TableEvent) event;
				
				/**
				 * open vcard of selected user
				 */
				if(tableEvent.getActionId().equals(AppointmentLecturerOralTableModel.ACTION_USER)) {
					Protocol p = protocolTableModel.getObject(tableEvent.getRowId());
					
					OLATResourceable ores = HomePageConfigManagerImpl.getInstance().loadConfigFor(p.getIdentity().getName());

					DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDTabs();
					DTab dt = dts.getDTab(ores);
					if (dt == null) {
						// does not yet exist
						dt = dts.createDTab(ores, null, p.getIdentity().getName());
						if (dt == null) return;
						UserInfoMainController uimc = new UserInfoMainController(ureq, dt.getWindowControl(), p.getIdentity());
						dt.setController(uimc);
						dts.addDTab(ureq, dt);
					}
					dts.activate(ureq, dt, null);
				}
			} else if(event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent tableEvent = (TableMultiSelectEvent) event;
				
				/**
				 * create form to edit result (grade)
				 */
				if(tableEvent.getAction().equals(AppointmentLecturerOralTableModel.ACTION_MULTI_EDIT_RESULT)) {
					editMarkFormProtocolHolder = protocolTableModel.getObjects(tableEvent.getSelection());
					
					if(editMarkFormProtocolHolder.isEmpty()) {
						showInfo("ExamLecturerWrittenController.info.selectOneProtocol");
						return;
					}
					
					removeAsListenerAndDispose(editMarkForm);
					editMarkForm = new EditMarkForm(ureq, getWindowControl(), "editMarkForm", getTranslator());
					listenTo(editMarkForm);
					
					cmc = new CloseableModalController(this.getWindowControl(), translate("close"), editMarkForm.getInitialComponent());
					cmc.activate();
			
				/**
				 * create form to add comments of edit if chosen only one
				 */
				} else if(tableEvent.getAction().equals(AppointmentLecturerOralTableModel.ACTION_MULTI_EDIT_COMMENT)) {
					editCommentFormProtocolHolder = protocolTableModel.getObjects(tableEvent.getSelection());
					String defaultText = "";
					
					if(editCommentFormProtocolHolder.isEmpty()) {
						showInfo("ExamLecturerWrittenController.info.selectOneProtocol");
						return;
					}
					
					if(editCommentFormProtocolHolder.size() == 1) {
							defaultText = editCommentFormProtocolHolder.get(0).getComments();
					}
					
					removeAsListenerAndDispose(editCommentForm);
					editCommentForm = new ESFCommentCreateAndEditForm(ureq, getWindowControl(), "editCommentForm", getTranslator(), defaultText);
					listenTo(editCommentForm);
					
					cmc = new CloseableModalController(this.getWindowControl(), translate("close"), editCommentForm.getInitialComponent());
					cmc.activate();
				
				/**
				 * create form to send emails to students
				 */
				} else if(tableEvent.getAction().equals(AppointmentLecturerOralTableModel.ACTION_MULTI_MAIL)) {
					editMailFormProtocolHolder = protocolTableModel.getObjects(tableEvent.getSelection());
					
					if(editMailFormProtocolHolder.isEmpty()) {
						showInfo("ExamLecturerWrittenController.info.selectOneProtocol");
						return;
					}
					
					ArrayList<String> recipients = new ArrayList<String>();
					for(Protocol proto : editMailFormProtocolHolder) {
						recipients.add(getName(proto.getIdentity()));
					}
					
					removeAsListenerAndDispose(editMailForm);
					String from = ureq.getIdentity().getUser().getProperty(UserConstants.EMAIL, null);
					String to = String.join(", ", recipients);
					editMailForm = new MailForm(ureq, getWindowControl(), from, to, "[" + exam.getName() + "] ");
					listenTo(editMailForm);
					
					cmc = new CloseableModalController(this.getWindowControl(), translate("close"), editMailForm.getInitialComponent());
					cmc.activate();
			
				/**
				 * change status of selected students to earmarked
				 */
				} else if(tableEvent.getAction().equals(AppointmentLecturerOralTableModel.ACTION_MULTI_EARMARK)) {
					List<Protocol> protos = protocolTableModel.getObjects(tableEvent.getSelection());
					
					for(Protocol proto : protos) {
						proto = ProtocolManager.getInstance().findProtocolByID(proto.getKey());
						proto.setEarmarked(true);
						ProtocolManager.getInstance().updateProtocol(proto);
						
						Translator userTranslator = Util.createPackageTranslator(Exam.class, new Locale(proto.getIdentity().getUser().getPreferences().getLanguage()));
						BusinessControlFactory bcf = BusinessControlFactory.getInstance();

						// Email MoveToEarmarked
						MailManager.getInstance().sendEmail(
							userTranslator.translate("Mail.MoveToEarmarked.Subject", new String[] { exam.getName() }),
							userTranslator.translate("Mail.MoveToEarmarked.Body",
								new String[] {
									exam.getName(),
									getName(proto.getIdentity()),
									DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, userTranslator.getLocale()).format(proto.getAppointment().getDate()),
									proto.getAppointment().getPlace(),
									new Integer(proto.getAppointment().getDuration()).toString(),
									userTranslator.translate("written"),
									bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
								}),
							proto.getIdentity()
						);
						
						// load esf
						ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(proto.getIdentity());

						// add a comment to the esf
						String commentText = translate("ExamLecturerWrittenController.earmarkedStudentManually", new String[] { getName(ureq.getIdentity()), exam.getName() });
						CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());
						
						// save changed esf
						ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);
					}
					
					// update view
					protocolTableModel.update();
					protocolTable.modelChanged();
				
				/**
				 * change status of selected users to registered
				 */
				} else if(tableEvent.getAction().equals(AppointmentLecturerOralTableModel.ACTION_MULTI_REGISTER)) {
					List<Protocol> protos = protocolTableModel.getObjects(tableEvent.getSelection());
					
					for(Protocol proto : protos) {
						proto = ProtocolManager.getInstance().findProtocolByID(proto.getKey());
						proto.setEarmarked(false);
						ProtocolManager.getInstance().updateProtocol(proto);
						
						Translator userTranslator = Util.createPackageTranslator(Exam.class, new Locale(proto.getIdentity().getUser().getPreferences().getLanguage()));
						BusinessControlFactory bcf = BusinessControlFactory.getInstance();

						// calculate semester
						String semester;
						Calendar cal = Calendar.getInstance();
						cal.setTime(proto.getAppointment().getDate());
						if (cal.get(Calendar.MONTH) >= 3 && cal.get(Calendar.MONTH) <= 8)
							semester = "SS " + cal.get(Calendar.YEAR);
						else
							semester = "WS " + cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.YEAR) + 1);
						
						// Email Register
						MailManager.getInstance().sendEmail(
							userTranslator.translate("Mail.Register.Subject", new String[] { exam.getName() }),
							userTranslator.translate("Mail.Register.Body",
								new String[] {
									exam.getName(),
									getName(proto.getIdentity()),
									DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, userTranslator.getLocale()).format(proto.getAppointment().getDate()),
									proto.getAppointment().getPlace(),
									new Integer(proto.getAppointment().getDuration()).toString(),
									userTranslator.translate("written"),
									bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true),
									userTranslator.translate(proto.getEarmarked() ? "ExamLecturerWrittenController.status.earmarked" : "ExamLecturerWrittenController.status.registered"),
									semester,
									proto.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null),
									proto.getIdentity().getUser().getProperty(UserConstants.EMAIL, null),
									proto.getStudyPath()
								}),
							proto.getIdentity()
						);
						
						// load esf
						ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(proto.getIdentity());

						// add a comment to the esf
						String commentText = translate("ExamLecturerWrittenController.registeredFromEarmarkedStudentManually", new String[] { getName(ureq.getIdentity()), exam.getName() });
						CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());
						
						// save changed esf
						ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);
					}
					
					// update view
					protocolTableModel.update();
					protocolTable.modelChanged();
				
				/**
				 *  remove selected users from exam
				 */
				} else if(tableEvent.getAction().equals(AppointmentLecturerOralTableModel.ACTION_MULTI_UNREGISTER)) {
					List<Protocol> protos = protocolTableModel.getObjects(tableEvent.getSelection());
					
					for(Protocol proto : protos) {
						proto = ProtocolManager.getInstance().findProtocolByID(proto.getKey());
						
						Translator userTranslator = Util.createPackageTranslator(Exam.class, new Locale(proto.getIdentity().getUser().getPreferences().getLanguage()));
						BusinessControlFactory bcf = BusinessControlFactory.getInstance();

						// Email Remove
						MailManager.getInstance().sendEmail(
							userTranslator.translate("Mail.Remove.Subject", new String[] { exam.getName() }),
							userTranslator.translate("Mail.Remove.Body",
								new String[] {
									// exam name, user name, exam date, exam location, exam duration, oral/written, link
									exam.getName(),
									getName(proto.getIdentity()),
									DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, userTranslator.getLocale()).format(proto.getAppointment().getDate()),
									proto.getAppointment().getPlace(),
									new Integer(proto.getAppointment().getDuration()).toString(),
									userTranslator.translate("written"),
									bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
								}),
							proto.getIdentity()
						);
						
						// update appointment
						Appointment tempApp = AppointmentManager.getInstance().findAppointmentByID(proto.getAppointment().getKey());
						tempApp.setOccupied(false);
						AppointmentManager.getInstance().updateAppointment(tempApp);
						
						// delete protocol
						ProtocolManager.getInstance().deleteProtocol(proto);
						
						// load esf
						ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(proto.getIdentity());
						
						// add a comment to the esf
						String commentText = translate("ExamLecturerWrittenController.removedStudentManually", new String[] { getName(ureq.getIdentity()), exam.getName() });
						CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());
						
						// save changed esf
						ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);
					}
					
					// update view
					protocolTableModel.update();
					protocolTable.modelChanged();
				}
			}
		
		/**
		 * subscribe student to exam manually
		 */
		} else if(source == userSearchController) {
			if(event instanceof SingleIdentityChosenEvent) {
				// close modal
				cmc.deactivate();
				
				SingleIdentityChosenEvent searchEvent = (SingleIdentityChosenEvent) event;				
				ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(searchEvent.getChosenIdentity());
				
				assert(userSearchControllerAppointmentHolder != null);
				
				if (esf != null) {
					if(!ProtocolManager.getInstance().isIdentitySubscribedToExam(searchEvent.getChosenIdentity(), exam)) {
						if(ProtocolManager.getInstance().registerStudent(userSearchControllerAppointmentHolder, esf, getTranslator(), false, "")) {
							// create comment in esf
							String commentText = translate("ExamLecturerOralController.registeredStudentManually", new String[] { getName(ureq.getIdentity()), exam.getName()});
							CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());
							
							// safe changed esf and appointment
							ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);
							AppointmentManager.getInstance().updateAppointment(userSearchControllerAppointmentHolder);
							
							// update view
							protocolTableModel.update();
							protocolTable.modelChanged();
						}
					} else {
						showError("ExamLecturerWrittenController.error.alreadyRegistered");
					}
				} else {
					showError("ExamLecturerWrittenController.error.studentHasNoESF");
				}
				
				userSearchControllerAppointmentHolder = null;
			}
			
		/**
		 * process edit marks form
		 */
		} else if(source == editMarkForm) {
			if(event == Form.EVNT_VALIDATION_OK) {
				cmc.deactivate();
				
				for (Protocol proto : editMarkFormProtocolHolder) {
					proto = ProtocolManager.getInstance().findProtocolByID(proto.getKey());
					proto.setGrade(editMarkForm.getGrade());
					ProtocolManager.getInstance().updateProtocol(proto);
					
					Translator userTranslator = Util.createPackageTranslator(Exam.class, new Locale(proto.getIdentity().getUser().getPreferences().getLanguage()));
					BusinessControlFactory bcf = BusinessControlFactory.getInstance();
					
					// Email GetMark
					MailManager.getInstance().sendEmail(
						userTranslator.translate("Mail.GetMark.Subject", new String[] { exam.getName() }),
						userTranslator.translate("Mail.GetMark.Body",
							new String[] {
								exam.getName(),
								getName(proto.getIdentity()),
								DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, userTranslator.getLocale()).format(proto.getAppointment().getDate()),
								proto.getAppointment().getPlace(),
								new Integer(proto.getAppointment().getDuration()).toString(),
								userTranslator.translate("written"),
								bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
							}),
						proto.getIdentity()
					);

					// add a comment in esf
					ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(proto.getIdentity());
					String commentText = translate("ExamLecturerOralController.gotResult", new String[] { exam.getName(), proto.getGrade()});
					CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());
				}
				
				editMarkFormProtocolHolder = null;

				// update view
				protocolTableModel.update();
				protocolTable.modelChanged();
			}
		
		/**
		 * process edit comments form
		 */
		} else if(source == editCommentForm) {
			if(event == Form.EVNT_VALIDATION_OK) {
				cmc.deactivate();
				
				for (Protocol proto : editCommentFormProtocolHolder) {
					proto = ProtocolManager.getInstance().findProtocolByID(proto.getKey());
					proto.setComments(editCommentForm.getComment());
					ProtocolManager.getInstance().updateProtocol(proto);
				}
				
				editCommentFormProtocolHolder = null;

				// update view
				protocolTableModel.update();
				protocolTable.modelChanged();
			}

		/**
		 * send mails to students and save them in their esf
		 */
		} else if(source == editMailForm) {
			if(event == Form.EVNT_VALIDATION_OK) {
				cmc.deactivate();
				
				String subject = editMailForm.getSubject();
				String body = editMailForm.getBody();
			
				for (Protocol proto : editMailFormProtocolHolder) {
					MailManager.getInstance().sendEmail(subject, body, ureq.getIdentity(), proto.getIdentity());
					
					// load esf
					ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(proto.getIdentity());
					
					// create comment in esf
					CommentManager.getInstance().createCommentInEsf(esf, "E-Mail: " + subject + "\n\n" + body, ureq.getIdentity());
					
					// save changed esf
					ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);
				}

				if(editMailForm.getCopyToSender()) {
					MailManager.getInstance().sendEmail(subject, body, null, null, ureq.getIdentity());
				}

				editMailFormProtocolHolder = null;
			}
		}
	}

	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// check that exam was not closed in the meantime
		if(ExamDBManager.getInstance().isClosed(exam)) {
			showInfo("ExamMainController.info.closed");
			return;
		}

		if(source == refreshTableButton) {
			// update view
			protocolTableModel.update();
			protocolTable.modelChanged();
		} else if(source == userAddButton) {
			// Guaranteed to work because we checked exactly that in constructor
			userSearchControllerAppointmentHolder = AppointmentManager.getInstance().findAllAppointmentsByExamId(exam.getKey()).get(0);
			
			removeAsListenerAndDispose(userSearchController);
			userSearchController = new UserSearchController(ureq, getWindowControl(), false, false);
			listenTo(userSearchController);
			
			cmc = new CloseableModalController(this.getWindowControl(), translate("close"), userSearchController.getInitialComponent());
			cmc.activate();
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
		removeAsListenerAndDispose(protocolTable);
		removeAsListenerAndDispose(userSearchController);
		removeAsListenerAndDispose(editCommentForm);
		removeAsListenerAndDispose(editMailForm);
		removeAsListenerAndDispose(editMarkForm);
		examDetailsController.dispose();
	}
	
	protected String getName(Identity id) {
		return id.getUser().getProperty(UserConstants.FIRSTNAME, null) + " " + id.getUser().getProperty(UserConstants.LASTNAME, null);
	}

}
