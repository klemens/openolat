package de.unileipzig.xman.exam.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.activemq.util.IOHelper;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
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
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NamedFileMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.user.HomePageConfigManagerImpl;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;

import de.unileipzig.xman.admin.mail.MailManager;
import de.unileipzig.xman.admin.mail.form.MailForm;
import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.appointment.AppointmentManager;
import de.unileipzig.xman.appointment.tables.AppointmentLecturerOralTableModel;
import de.unileipzig.xman.calendar.CalendarManager;
import de.unileipzig.xman.comment.CommentManager;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.esf.form.ESFCommentCreateAndEditForm;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.exam.forms.EditMarkForm;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;
import uk.ac.ed.ph.jqtiplus.internal.util.Pair;

public class ExamLecturerOralController extends BasicController implements ExamController {
	
	private Exam exam;
	private VelocityContainer mainVC;
	private BreadcrumbedStackedPanel stack;
	
	private TableController appointmentTable;
	private AppointmentLecturerOralTableModel appointmentTableModel;

	private CloseableModalController cmc;
	
	private Link refreshTableButton;
	private Link downloadCalendarButton;

	private UserSearchController userSearchController;

	private EditMarkForm editMarkForm;
	private List<Appointment> editMarkFormAppointmentHolder;

	private ESFCommentCreateAndEditForm editCommentForm;
	private List<Appointment> editCommentFormAppointmentHolder;

	private MailForm editMailForm;
	private List<Appointment> editMailFormAppointmentHolder;

	private ExamDetailsController examDetailsController;

	private OLog log = Tracing.createLoggerFor(getClass());

	/**
	 * The exam given MUST be oral, otherwise InvalidParameterException is thrown!
	 * 
	 * @param ureq
	 * @param wControl
	 * @param exam The oral exam to manage
	 * @throws InvalidParameterException
	 */
	protected ExamLecturerOralController(UserRequest ureq, WindowControl wControl, Exam exam, BreadcrumbedStackedPanel stack) {
		super(ureq, wControl);
		
		if(!exam.getIsOral())
			throw new InvalidParameterException("Expected oral exam, got written one");
		
		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));
		this.exam = exam;
		this.stack = stack;
		
		mainVC = new VelocityContainer("examStudentView", Exam.class, "examLecturerOralView", getTranslator(), this);

		examDetailsController = new ExamDetailsController(ureq, wControl, getTranslator(), exam, true);
		mainVC.put("examDetails", examDetailsController.getInitialComponent());

		init(ureq);

		putInitialPanel(mainVC);
	}
	
	private void init(UserRequest ureq) {
		if(AppointmentManager.getInstance().findAllAppointmentsByExamId(exam.getKey()).size() > 0) {
			mainVC.contextPut("showAppointmentTable", true);
			
			refreshTableButton = LinkFactory.createButton("ExamLecturerWrittenController.refreshTable", mainVC, this);
			downloadCalendarButton = LinkFactory.createButton("ExamLecturerOralController.downloadCalendar", mainVC, this);
			
			buildAppointmentTable(ureq);
		} else {
			mainVC.contextPut("showAppointmentTable", false);
		}
	}
	
	private void buildAppointmentTable(UserRequest ureq) {
		removeAsListenerAndDispose(appointmentTable);
		
		appointmentTableModel = new AppointmentLecturerOralTableModel(exam, ureq.getLocale());
		
		TableGuiConfiguration tableGuiConfiguration = new TableGuiConfiguration();
		tableGuiConfiguration.setDownloadOffered(true);
		tableGuiConfiguration.setTableEmptyMessage(translate("ExamEditorController.appointmentTable.empty"));
		tableGuiConfiguration.setMultiSelect(true);
		tableGuiConfiguration.setPreferencesOffered(true, "ExamLecturerOralController.appointmentTable");
		appointmentTable = new TableController(tableGuiConfiguration, ureq, getWindowControl(), getTranslator());
		
		appointmentTableModel.createColumns(appointmentTable);
		appointmentTable.setTableDataModel(appointmentTableModel);
		appointmentTable.setSortColumn(appointmentTableModel.getColumnCount(), false); // sort by last, zerobased,  +1 for multiselect
		
		listenTo(appointmentTable);
		
		mainVC.put("appointmentTable", appointmentTable.getInitialComponent());

		// Load the table entries
		updateAppointmentTable();
	}

	private void updateAppointmentTable() {
		appointmentTableModel.update();
		appointmentTable.modelChanged();
		mainVC.contextPut("showEarmarkedNote", appointmentTableModel.hasEarmarkedProtocol());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		// check that exam was not closed in the meantime
		if(ExamDBManager.getInstance().isClosed(exam)) {
			showInfo("ExamMainController.info.closed");
			return;
		}
		
		if(source == appointmentTable) {
			if(event instanceof TableEvent) {
				TableEvent tableEvent = (TableEvent) event;
				
				/**
				 * Add new user to the exam or open the vcard of already existing ones
				 */
				if(tableEvent.getActionId().equals(AppointmentLecturerOralTableModel.ACTION_USER)) {
					Appointment appointment = appointmentTableModel.getObject(tableEvent.getRowId());

					if(appointmentTableModel.existsProtocol(appointment)) {
						// There is already a user registered -> open their vcard
						Protocol p = appointmentTableModel.getProtocol(appointment);

						UserInfoMainController uimc = new UserInfoMainController(ureq, getWindowControl(), p.getIdentity(), false, false);
						stack.pushController(UserManager.getInstance().getUserDisplayName(p.getIdentity()), uimc);
					} else {
						// No user registered, open user search dialog to select one to register
						removeAsListenerAndDispose(userSearchController);
						userSearchController = new UserSearchController(ureq, getWindowControl(), false, false, false);
						userSearchController.setUserObject(appointment.getKey());
						listenTo(userSearchController);

						cmc = new CloseableModalController(this.getWindowControl(), translate("close"), userSearchController.getInitialComponent());
						cmc.activate();
					}
				}
			} else if(event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent tableEvent = (TableMultiSelectEvent) event;
				
				/**
				 * create form to edit result (grade)
				 */
				if(tableEvent.getAction().equals(AppointmentLecturerOralTableModel.ACTION_MULTI_EDIT_RESULT)) {
					editMarkFormAppointmentHolder = filterAppointmentsByType(appointmentTableModel.getObjects(tableEvent.getSelection()), true);
					
					if(editMarkFormAppointmentHolder.isEmpty()) {
						showInfo("ExamLecturerOralController.info.selectOneWithProtocol");
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
					editCommentFormAppointmentHolder = filterAppointmentsByType(appointmentTableModel.getObjects(tableEvent.getSelection()), true);
					String defaultText = "";
					
					if(editCommentFormAppointmentHolder.isEmpty()) {
						showInfo("ExamLecturerOralController.info.selectOneWithProtocol");
						return;
					}
					
					if(tableEvent.getSelection().cardinality() == 1) {
						if(appointmentTableModel.existsProtocol(editCommentFormAppointmentHolder.get(0)))
							defaultText = appointmentTableModel.getProtocol(editCommentFormAppointmentHolder.get(0)).getComments();
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
					editMailFormAppointmentHolder = filterAppointmentsByType(appointmentTableModel.getObjects(tableEvent.getSelection()), true);
					
					if(editMailFormAppointmentHolder.isEmpty()) {
						showInfo("ExamLecturerOralController.info.selectOneWithProtocol");
						return;
					}
					
					ArrayList<String> recipients = new ArrayList<String>();
					for(Appointment app : editMailFormAppointmentHolder) {
						recipients.add(getName(appointmentTableModel.getProtocol(app).getIdentity()));
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
					List<Protocol> protocols = appointmentTableModel.getObjects(tableEvent.getSelection()).stream()
						.filter(appointmentTableModel::existsProtocol)
						.map(appointmentTableModel::getProtocol)
						.filter(p -> !p.getEarmarked())
						.collect(Collectors.toList());

					if(protocols.size() == 0) {
						showInfo("ExamLecturerOralController.info.selectOneRegisteredProtocol");
					}

					for(Protocol protocol : protocols) {
						Protocol proto = ProtocolManager.getInstance().findProtocolByID(protocol.getKey());
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
									userTranslator.translate("oral"),
									bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
								}),
							proto.getIdentity()
						);

						// load esf
						ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(proto.getIdentity());

						// add a comment to the esf
						String commentText = translate("ExamLecturerOralController.earmarkedStudentManually", new String[] { getName(ureq.getIdentity()), exam.getName() });
						CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());

						// needed, because loading the same esf multiple times causes hibernate to fail (stale exception) the transaction
						// TODO: there might be a better way to avoid the exception
						DBFactory.getInstance().intermediateCommit();
					}
					
					// update view
					updateAppointmentTable();
				
				/**
				 * change status of selected users to registered
				 */
				} else if(tableEvent.getAction().equals(AppointmentLecturerOralTableModel.ACTION_MULTI_REGISTER)) {
					List<Protocol> protocols = appointmentTableModel.getObjects(tableEvent.getSelection()).stream()
						.filter(appointmentTableModel::existsProtocol)
						.map(appointmentTableModel::getProtocol)
						.filter(p -> p.getEarmarked())
						.collect(Collectors.toList());

					if(protocols.size() == 0) {
						showInfo("ExamLecturerOralController.info.selectOneEarmarkedProtocol");
					}

					for(Protocol protocol : protocols) {
						Protocol proto = ProtocolManager.getInstance().findProtocolByID(protocol.getKey());
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
									userTranslator.translate("oral"),
									bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true),
									userTranslator.translate(proto.getEarmarked() ? "ExamLecturerOralController.status.earmarked" : "ExamLecturerOralController.status.registered"),
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
						String commentText = translate("ExamLecturerOralController.registeredFromEarmarkedStudentManually", new String[] { getName(ureq.getIdentity()), exam.getName() });
						CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());

						// TODO: see first occurrence
						DBFactory.getInstance().intermediateCommit();
					}
					
					// update view
					updateAppointmentTable();
				
				/**
				 *  remove selected users from exam
				 */
				} else if(tableEvent.getAction().equals(AppointmentLecturerOralTableModel.ACTION_MULTI_UNREGISTER)) {
					List<Appointment> apps = appointmentTableModel.getObjects(tableEvent.getSelection());
					
					for(Appointment app : apps) {
						if(appointmentTableModel.existsProtocol(app)) {
							Protocol proto = ProtocolManager.getInstance().findProtocolByID(appointmentTableModel.getProtocol(app).getKey());
							
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
										userTranslator.translate("oral"),
										bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
									}),
								proto.getIdentity()
							);
							
							// update appointment
							Appointment tempApp = AppointmentManager.getInstance().findAppointmentByID(proto.getAppointment().getKey());
							tempApp.setOccupied(false);
							AppointmentManager.getInstance().updateAppointment(tempApp);
							tempApp = null;
							
							// delete protocol
							ProtocolManager.getInstance().deleteProtocol(proto);
							
							// load esf
							ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(proto.getIdentity());
							
							// add a comment to the esf
							String commentText = translate("ExamLecturerOralController.removedStudentManually", new String[] { getName(ureq.getIdentity()), exam.getName() });
							CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());
							
							// TODO: see first occurrence
							DBFactory.getInstance().intermediateCommit();
						}
					}
					
					// update view
					updateAppointmentTable();

				/**
				 * swap protocols of the two selected appointments (move if only one is occupied)
				 */
				} else if(tableEvent.getAction().equals(AppointmentLecturerOralTableModel.ACTION_MULTI_SWAP)) {
					List<Appointment> selectedAppointments = appointmentTableModel.getObjects(tableEvent.getSelection());

					if(selectedAppointments.size() != 2) {
						showInfo("ExamLecturerOralController.error.selectTwoForEarmarkedSwap");
						return;
					}

					// refresh stale appointments and load protocols
					List<Pair<Appointment, Optional<Protocol>>> protocols = selectedAppointments.stream()
						.map(app -> AppointmentManager.getInstance().findAppointmentByID(app.getKey()))
						.map(app -> new Pair<>(app, ProtocolManager.getInstance().findAllProtocolsByAppointment(app).stream().findAny()))
						.collect(Collectors.toList());

					// check that at least one appointment is occupied and none has a registered (instead of earmarked) protocol
					long occupied = protocols.stream()
						.map(Pair::getSecond)
						.filter(Optional::isPresent)
						.count();
					long registered = protocols.stream()
						.map(Pair::getSecond)
						.filter(Optional::isPresent)
						.filter(p -> !p.get().getEarmarked())
						.count();
					if(occupied < 1 || registered > 0) {
						showError("ExamLecturerOralController.error.selectTwoForEarmarkedSwap");
						// update view, status could have changed
						updateAppointmentTable();
						return;
					}

					// extract two-item-list into local variables; the order does not matter,
					// because all following operations are symmetrical
					Appointment appointmentA = protocols.get(0).getFirst();
					Appointment appointmentB = protocols.get(1).getFirst();
					Optional<Protocol> protocolA = protocols.get(0).getSecond();
					Optional<Protocol> protocolB = protocols.get(1).getSecond();

					// swap the appointments assigned to the protocols, if the latter exist (at least one does)
					protocolA.ifPresent(p -> p.setAppointment(appointmentB));
					protocolB.ifPresent(p -> p.setAppointment(appointmentA));

					// update the occupied status based on the other protocols existence; this is a noop
					// if both protocols are present, otherwise the occupied flags are swapped as well
					appointmentA.setOccupied(protocolB.isPresent());
					appointmentB.setOccupied(protocolA.isPresent());

					// send emails and add comments to the esf
					protocolA.ifPresent(p -> notifyUpdatedAppointment(ureq, p, appointmentA, appointmentB));
					protocolB.ifPresent(p -> notifyUpdatedAppointment(ureq, p, appointmentB, appointmentA));

					// update view
					updateAppointmentTable();
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
				
				Appointment appointment = AppointmentManager.getInstance().findAppointmentByID((Long) userSearchController.getUserObject());
				
				if(appointment.getOccupied()) {
					showError("ExamLecturerOralController.error.appNotAvailable");
					// update view
					updateAppointmentTable();
					return;
				}
				
				if (esf != null) {
					if(ProtocolManager.getInstance().registerStudent(appointment, esf, getTranslator(), false, "")) {
						// create comment in esf
						String commentText = translate("ExamLecturerOralController.registeredStudentManually", new String[] { getName(ureq.getIdentity()), exam.getName()});
						CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());
						
						//save updated esf and appointment
						ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);
						AppointmentManager.getInstance().updateAppointment(appointment);
						
						// update view
						updateAppointmentTable();
					}
				} else {
					showError("ExamLecturerOralController.error.studentHasNoESF");
				}
			}
		
		/**
		 * process edit marks form
		 */
		} else if(source == editMarkForm) {
			if(event == Form.EVNT_VALIDATION_OK) {
				cmc.deactivate();
				
				for (Appointment app : editMarkFormAppointmentHolder) {
					if(appointmentTableModel.existsProtocol(app)) {
						Protocol proto = ProtocolManager.getInstance().findProtocolByID(appointmentTableModel.getProtocol(app).getKey());
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
									userTranslator.translate("oral"),
									bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
								}),
							proto.getIdentity()
						);

						// add a comment in esf
						ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(proto.getIdentity());
						String commentText = translate("ExamLecturerOralController.gotResult", new String[] { exam.getName(), proto.getGrade()});
						CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());
					}
				}
				
				editMarkFormAppointmentHolder = null;

				// update view
				updateAppointmentTable();
			}
		
		/**
		 * process edit comments form
		 */
		} else if(source == editCommentForm) {
			if(event == Form.EVNT_VALIDATION_OK) {
				cmc.deactivate();
				
				for (Appointment app : editCommentFormAppointmentHolder) {
					if(appointmentTableModel.existsProtocol(app)) {
						Protocol proto = ProtocolManager.getInstance().findProtocolByID(appointmentTableModel.getProtocol(app).getKey());
						proto.setComments(editCommentForm.getComment());
						ProtocolManager.getInstance().updateProtocol(proto);
					}
				}
				
				editCommentFormAppointmentHolder = null;

				// update view
				updateAppointmentTable();
			}
		
		/**
		 * send mails to students and save them in their esf
		 */
		} else if(source == editMailForm) {
			if(event == Form.EVNT_VALIDATION_OK) {
				cmc.deactivate();
				
				String subject = editMailForm.getSubject();
				String body = editMailForm.getBody();
			
				for (Appointment app : editMailFormAppointmentHolder) {
					if(appointmentTableModel.existsProtocol(app)) {
						Protocol proto = appointmentTableModel.getProtocol(app);
						
						MailManager.getInstance().sendEmail(subject, body, ureq.getIdentity(), proto.getIdentity());
						
						// load esf
						ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(proto.getIdentity());
						
						// create comment in esf
						CommentManager.getInstance().createCommentInEsf(esf, "<p><em>E-Mail</em>: " + subject + "</p>\n" + body, ureq.getIdentity());
						
						// TODO: see first occurrence
						DBFactory.getInstance().intermediateCommit();
					}
				}

				if(editMailForm.getCopyToSender()) {
					MailManager.getInstance().sendEmail(subject, body, null, null, ureq.getIdentity());
				}
				
				editMailFormAppointmentHolder = null;
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
			updateAppointmentTable();
		} else if(source == downloadCalendarButton) {
			File tmpFile = new File(WebappHelper.getTmpDir(), "xman-calendar-export-" + CodeHelper.getUniqueID());

			OutputStream out = null;
			try {
				out = new FileOutputStream(tmpFile);
				CalendarManager.exportProtocolsIcal(exam, out, getLocale());

				String fileName = translate("ExamLecturerOralController.downloadCalendarFileName", IOHelper.toFileSystemSafeName(exam.getName()));
				MediaResource mr = new NamedFileMediaResource(tmpFile, fileName, "Calendar Export", true);
				ureq.getDispatchResult().setResultingMediaResource(mr);
			} catch(IOException e) {
				log.error("error while creating tmp file for calendar export", e);
				showError("ExamLecturerOralController.downloadCalendarError");
			} finally {
				if(out != null) {
					try {
						out.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
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
		removeAsListenerAndDispose(appointmentTable);
		removeAsListenerAndDispose(userSearchController);
		removeAsListenerAndDispose(editMarkForm);
		removeAsListenerAndDispose(editCommentForm);
		removeAsListenerAndDispose(editMailForm);
		examDetailsController.dispose();
	}
	
	protected String getName(Identity id) {
		return id.getUser().getProperty(UserConstants.FIRSTNAME, null) + " " + id.getUser().getProperty(UserConstants.LASTNAME, null);
	}
	
	protected List<Appointment> filterAppointmentsByType(List<Appointment> oldList, boolean withProtocol) {
		List<Appointment> filteredList = new ArrayList<Appointment>();
		
		for(Appointment app : oldList) {
			if(withProtocol == appointmentTableModel.existsProtocol(app))
				filteredList.add(app);
		}
		
		return filteredList;
	}

	protected void notifyUpdatedAppointment(UserRequest ureq, Protocol protocol, Appointment oldAppointment, Appointment newAppointment) {
		BusinessControlFactory bcf = BusinessControlFactory.getInstance();
		Translator userTranslator = Util.createPackageTranslator(Exam.class, new Locale(protocol.getIdentity().getUser().getPreferences().getLanguage()));
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, userTranslator.getLocale());

		// {0}: exam name, {1} exam link, {2}: new date, {3}: new place, {4}: new duration, {5}: old date, {6}: old place, {7}: old duration
		String[] params = new String[] {
			exam.getName(),
			bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true),
			dateFormat.format(newAppointment.getDate()),
			newAppointment.getPlace(),
			String.valueOf(newAppointment.getDuration()),
			dateFormat.format(oldAppointment.getDate()),
			oldAppointment.getPlace(),
			String.valueOf(oldAppointment.getDuration()),
		};

		String subject = userTranslator.translate("ExamLecturerOralController.changeEarmarkedAppointment.subject", params);
		String body = userTranslator.translate("ExamLecturerOralController.changeEarmarkedAppointment.body", params);
		MailManager.getInstance().sendEmail(subject, body, ureq.getIdentity(), protocol.getIdentity());

		String comment = userTranslator.translate("ExamLecturerOralController.changeEarmarkedAppointment.comment", params);
		ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(protocol.getIdentity());
		CommentManager.getInstance().createCommentInEsf(esf, comment, ureq.getIdentity());
	}
}
