package de.unileipzig.xman.exam.controllers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailTemplateForm;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResourceManager;
import org.olat.user.HomePageConfigManager;
import org.olat.user.HomePageConfigManagerImpl;
import org.olat.user.UserInfoMainController;

import de.unileipzig.xman.admin.mail.MailManager;
import de.unileipzig.xman.admin.mail.form.MailForm;
import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.appointment.AppointmentManager;
import de.unileipzig.xman.appointment.tables.AppointmentTableModel;
import de.unileipzig.xman.calendar.CalendarManager;
import de.unileipzig.xman.comment.CommentEntry;
import de.unileipzig.xman.comment.CommentManager;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.esf.controller.ESFEditController;
import de.unileipzig.xman.esf.form.ESFCommentCreateAndEditForm;
import de.unileipzig.xman.esf.table.ESFTableModel;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.exam.forms.EditMarkForm;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;
import de.unileipzig.xman.protocol.tables.ProtocolTableModel;

/**
 * 
 * @author
 */
public class ExamLaunchController extends MainLayoutBasicController implements
		MainLayoutController {

	private static final String VELOCITY_ROOT = Util
			.getPackageVelocityRoot(Exam.class);

	private VelocityContainer vcMain;
	private Translator translator;
	private Exam exam;
	private ESFCommentCreateAndEditForm commentForm;
	private TableController appTableCtr, myAppTableCtr, earProtoTableCtr,
			regProtoTableCtr, chooseAppCtr;
	private AppointmentTableModel appTableMdl, myAppTableMdl, chooseAppMdl;
	private ProtocolTableModel earProtoTableMdl, regProtoTableMdl;
	private boolean isResourceOwner, isOLATUser;
	private Protocol protocol;
	private Protocol choosenProtocol;
	private OLATResourceable res;
	private EditMarkForm editMarkForm;
	private CloseableModalController cmc;
	private UserSearchController usc;
	private List<Protocol> protoList = null;
	private Identity id;
	private Link addStudent;
	private Link editExam;

	private LayoutMain3ColsController columnLayoutCtr;
	private CommentEntry commentEntry;
	private ESFCommentCreateAndEditForm editCommentForm;
	private CloseableModalController editCommentCtr;
	private CloseableModalController sendMailCtr;
	private MailForm mailForm;

	/**
	 * creates the controller for the exam launcher
	 * 
	 * @param ureq
	 *            the UserRequest
	 * @param wControl
	 *            the window control
	 * @param exam
	 *            the exam
	 * @param isResourceOwner
	 *            true if the user is resourceowner
	 * @param isOLATUser
	 *            true, if the user is a olat user
	 */
	public ExamLaunchController(UserRequest ureq, WindowControl wControl,
			Exam exam, boolean isResourceOwner, boolean isOLATUser) {
		super(ureq, wControl);

		this.exam = exam;
		this.isResourceOwner = isResourceOwner;
		this.isOLATUser = isOLATUser;


		String tmpExamName = ExamDBManager.getInstance().getExamName(this.exam);
		if (!tmpExamName.equals(this.exam.getName())) {
			this.exam.setName(tmpExamName);
			ExamDBManager.getInstance().updateExam(this.exam);
		}

		translator = Util.createPackageTranslator(Exam.class, ureq.getLocale());

		vcMain = new VelocityContainer("examLaunch", VELOCITY_ROOT
				+ "/examLaunch.html", translator, this);

		vcMain.contextPut("examType", translator.translate(this.exam
				.getIsOral() ? "oral" : "written"));

		vcMain.contextPut("regStartDate",
				exam.getRegStartDate() == null ? "n/a" : DateFormat
						.getDateTimeInstance(DateFormat.SHORT,
								DateFormat.SHORT, translator.getLocale())
						.format(exam.getRegStartDate()));

		vcMain.contextPut("regEndDate", exam.getRegEndDate() == null ? "n/a"
				: DateFormat.getDateTimeInstance(DateFormat.SHORT,
						DateFormat.SHORT, translator.getLocale()).format(
						exam.getRegEndDate()));

		vcMain.contextPut("signOffDate", exam.getSignOffDate() == null ? "n/a"
				: DateFormat.getDateTimeInstance(DateFormat.SHORT,
						DateFormat.SHORT, translator.getLocale()).format(
						exam.getSignOffDate()));

		vcMain.contextPut("earmarkedEnabled", translator.translate(exam
				.getEarmarkedEnabled() ? "yes" : "no"));

		String comments = exam.getComments();
		// TODO: Else-Fall war eigentlich Wiki-Markup,
		// Formatter.formatwikimarkup() war um Formatter.truncate() drumrum
		vcMain.contextPut("comments", comments.equals("") ? translator
				.translate("ExamLaunchController.comments.isEmpty") : Formatter
				.truncate(comments, 2048));	


		columnLayoutCtr = new LayoutMain3ColsController(ureq,
				getWindowControl(), null, null, vcMain, "examLaunch");
		listenTo(columnLayoutCtr);// cleanup on dispose
		// add background image to home site
		columnLayoutCtr.addCssClassToMain("o_home");

		if(isResourceOwner) {
		    editExam = LinkFactory.createButtonSmall("ExamLaunchController.link.editExam", vcMain, this);
		    vcMain.put("editExam", editExam);
		    vcMain.contextPut("isResourceOwner", true);
		} else {
			vcMain.contextPut("isResourceOwner", false);
		}
		
		this.setAppointmentTable(ureq);

		this.setProtocolTable(ureq);

		this.putInitialPanel(columnLayoutCtr.getInitialComponent());
	}

	/**
	 * Sets a table for all appointments of the exam. The content of the table
	 * depends on the role of the user. A second table is shown, if the user is
	 * subscribed to the exam.
	 * 
	 * @param ureq
	 *            the user request
	 */
	private void setAppointmentTable(UserRequest ureq) {

		protocol = ProtocolManager.getInstance().findProtocolByIdentityAndExam(
				ureq.getIdentity(), exam);

		String decision = AppointmentTableModel.NO_SELECTION;
		// my appointment table
		vcMain.contextPut("showMyAppointment", false);
		vcMain.contextPut("showAppTable", true);
		if (isOLATUser) {
			if (protocol == null) {
				if (this.canSubscribe())
					decision = AppointmentTableModel.SELECT_SUBSCRIBE;
			}
			// (second) table with own appointment
			else {
				if (!exam.getIsOral())
					vcMain.contextPut("showAppTable", false);
				vcMain.contextPut("showMyAppointment", true);
				vcMain.contextPut("earmarkedAppInfo", protocol.getEarmarked());
				List<Appointment> myAppList = new Vector<Appointment>();
				myAppList.add(protocol.getAppointment());
				myAppTableMdl = new AppointmentTableModel(
						ureq.getLocale(),
						myAppList,
						this.canUnsubscribe() ? AppointmentTableModel.SELECT_UNSUBSCRIBE
								: AppointmentTableModel.NO_SELECTION);
				TableGuiConfiguration myTgc = new TableGuiConfiguration();
				myTgc.setColumnMovingOffered(true);
				myTgc.setDownloadOffered(true);
				myAppTableCtr = new TableController(myTgc, ureq, this
						.getWindowControl(), translator);
				myAppTableMdl.setTable(myAppTableCtr);
				myAppTableCtr.setTableDataModel(myAppTableMdl);

				// NEU
				myAppTableCtr.addControllerListener(this);

				vcMain.put("myAppTable", myAppTableCtr.getInitialComponent());
			}
		}
		// appointment table
		TableGuiConfiguration tgc = new TableGuiConfiguration();
		tgc.setColumnMovingOffered(true);
		tgc.setDownloadOffered(true);
		tgc.setPreferencesOffered(true, "ExamLaunchController.appTable");
		tgc.setTableEmptyMessage(translator
				.translate("ExamEditorController.appointmentTable.empty"));
		appTableCtr = new TableController(tgc, ureq, this.getWindowControl(),
				translator);
		List<Appointment> appList;
		if (isResourceOwner)
			appList = AppointmentManager.getInstance()
					.findAllAppointmentsByExamId(exam.getKey());
		else {
			appList = AppointmentManager.getInstance()
					.findAllAvailableAppointmentsByExamId(exam.getKey());
			if (exam.getIsOral() && appList.size() == 0)
				vcMain.contextPut("showAppTable", false);
		}
		appTableMdl = new AppointmentTableModel(ureq.getLocale(), appList,
				decision);
		appTableMdl.setTable(appTableCtr);
		appTableCtr.setTableDataModel(appTableMdl);
		appTableCtr.setSortColumn(0, true);

		// NEU
		appTableCtr.addControllerListener(this);

		vcMain.put("appTable", appTableCtr.getInitialComponent());
	}

	/**
	 * sets the protocol table
	 * 
	 * @param ureq
	 *            the user request
	 */
	private void setProtocolTable(UserRequest ureq) {

		vcMain.contextPut("showProtocolTable", false);
		if (isResourceOwner) {
			vcMain.contextPut("showProtocolTable", true);
			
			this.addStudent = LinkFactory.createButtonSmall(
					"ExamLaunchController.link.addStudent", vcMain, this);
			vcMain.put("addStudent", addStudent);

			TableGuiConfiguration regTgc = new TableGuiConfiguration();
			regTgc.setMultiSelect(true);
			regTgc.setColumnMovingOffered(true);
			regTgc.setDownloadOffered(true);
			regTgc.setPreferencesOffered(true,
					"ExamLaunchController.regProtoTable");
			regTgc.setTableEmptyMessage(translator
					.translate("ExamLaunchController.regProtoTable.empty"));
			regTgc.setShowAllLinkEnabled(true);
			regProtoTableCtr = new TableController(regTgc, ureq, this
					.getWindowControl(), translator);
			regProtoTableCtr.setMultiSelect(true);
			regProtoTableCtr.addMultiSelectAction(
					"ExamLaunchController.regProtoTable.multiselect.earmark",
					"protoTable.earmark");
			regProtoTableCtr.addMultiSelectAction(
					"ExamLaunchController.protoTable.multiselect.remove",
					"protoTable.remove");
			regProtoTableCtr.addMultiSelectAction(
					"ExamLaunchController.regProtoTable.multiselect.note",
					"protoTable.note");
			// replaced by a button cause is not shown if table is empty
			// regProtoTableCtr.addMultiSelectAction("ExamLaunchController.protoTable.subscribe",
			// "protoTable.subscribe");
			regProtoTableCtr.addMultiSelectAction(
					"ExamLaunchController.protoTable.mailToStudent",
					"protoTable.mailToStudent");
			regProtoTableCtr.addMultiSelectAction(
					"ExamLaunchController.protoTable.editComment",
					"protoTable.editComment");
			List<Protocol> protoList = ProtocolManager.getInstance()
					.findAllProtocolsByExam(exam, false);
			regProtoTableMdl = new ProtocolTableModel(ureq.getLocale(),
					protoList, true, false,
					(ureq.getUserSession().getRoles().isInstitutionalResourceManager() || ureq.getUserSession().getRoles().isOLATAdmin()));
			regProtoTableMdl.setTable(regProtoTableCtr);
			regProtoTableCtr.setTableDataModel(regProtoTableMdl);
			regProtoTableCtr.setSortColumn(0, true);

			// NEU
			regProtoTableCtr.addControllerListener(this);

			vcMain.put("regProtoTable", regProtoTableCtr.getInitialComponent());

			TableGuiConfiguration earTgc = new TableGuiConfiguration();
			earTgc.setMultiSelect(true);
			earTgc.setColumnMovingOffered(true);
			earTgc.setDownloadOffered(true);
			earTgc.setPreferencesOffered(true,
					"ExamLaunchController.earProtoTable");
			earTgc.setTableEmptyMessage(translator
					.translate("ExamLaunchController.earProtoTable.empty"));
			earProtoTableCtr = new TableController(earTgc, ureq, this
					.getWindowControl(), translator);
			earProtoTableCtr.setMultiSelect(true);
			earProtoTableCtr.addMultiSelectAction(
					"ExamLaunchController.earProtoTable.multiselect.register",
					"protoTable.register");
			earProtoTableCtr.addMultiSelectAction(
					"ExamLaunchController.protoTable.multiselect.remove",
					"protoTable.remove");
			earProtoTableCtr.addMultiSelectAction(
					"ExamLaunchController.protoTable.mailToStudent",
					"earTable.mailToStudent");
			earProtoTableCtr.addMultiSelectAction(
					"ExamLaunchController.protoTable.editComment",
					"protoTable.editComment");
			protoList = ProtocolManager.getInstance().findAllProtocolsByExam(
					exam, true);
			earProtoTableMdl = new ProtocolTableModel(ureq.getLocale(),
					protoList, false, false,
					(ureq.getUserSession().getRoles().isInstitutionalResourceManager() || ureq.getUserSession().getRoles().isOLATAdmin()));
			earProtoTableMdl.setTable(earProtoTableCtr);
			earProtoTableCtr.setTableDataModel(earProtoTableMdl);
			earProtoTableCtr.setSortColumn(0, true);

			// NEU
			earProtoTableCtr.addControllerListener(this);

			vcMain.put("earProtoTable", earProtoTableCtr.getInitialComponent());
		}
	}

	/**
	 * @returns true if actual date is within registration period
	 */
	private boolean canSubscribe() {

		if (exam.getRegStartDate() != null && exam.getRegEndDate() != null) {
			Date date = new Date();
			if (date.getTime() > exam.getRegStartDate().getTime()
					&& date.getTime() < exam.getRegEndDate().getTime())
				return true;
			else
				return false;
		} else
			return false;

	}

	/**
	 * @returns true if actual date is before sign off deadline
	 */
	private boolean canUnsubscribe() {

		if (exam.getSignOffDate() != null)
			return (new Date().getTime() < exam.getSignOffDate().getTime());
		else
			return false;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {

		// nothing to do
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {

		// register student manually
		if (source == addStudent) {

			usc = new UserSearchController(ureq, this.getWindowControl(), false);
			usc.addControllerListener(this);
			cmc = new CloseableModalController(this.getWindowControl(),
					translator.translate("close"), usc.getInitialComponent());
			cmc.activate();
		}
	
		if (source == editExam) {
			OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(exam.getResourceableId(), Exam.ORES_TYPE_NAME);
			DTabs dts = (DTabs) Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");

			// Deleting the current tab and creating a new one with the same resource is a dirty hack
			// TODO implement using a StackedController
			DTab dt = dts.getDTab(ores);
			if(dt == null) return;
			dts.removeDTab(ureq, dt);

			DTab dtNew = dts.createDTab(ores, exam.getName());

			ExamEditorController eec = (ExamEditorController) RepositoryHandlerFactory.getInstance().getRepositoryHandler(Exam.ORES_TYPE_NAME)
																		.createEditorController(ores, ureq, this.getWindowControl());
			dtNew.setController(eec);

			dts.addDTab(ureq, dtNew);
			dts.activate(ureq, dtNew, null);
		}

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == editMarkForm) {

			if (event == Form.EVNT_VALIDATION_OK) {

				for (Protocol p : protoList) {
					p.setGrade(editMarkForm.getGrade());
					ProtocolManager.getInstance().updateProtocol(p);
					Locale userLocale = new Locale(p.getIdentity().getUser().getPreferences().getLanguage());
					Translator tmpTranslator = Util.createPackageTranslator(Exam.class, userLocale);
					BusinessControlFactory bcf = BusinessControlFactory.getInstance();
					
					// Email GetMark
					MailManager.getInstance().sendEmail(
						tmpTranslator.translate("ExamLaunchController.GetMark.Subject", new String[] { ExamDBManager.getInstance().getExamName(exam) }),
						tmpTranslator.translate("ExamLaunchController.GetMark.Body",
							new String[] {
								ExamDBManager.getInstance().getExamName(exam),
								this.getRealName(p.getIdentity()),
								DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, tmpTranslator.getLocale()).format(p.getAppointment().getDate()),
								p.getAppointment().getPlace(),
								new Integer(p.getAppointment().getDuration()).toString(),
								p.getExam().getIsOral() ? tmpTranslator.translate("oral") : tmpTranslator.translate("written"),
								bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
							}),
						p.getIdentity()
					);
				}
				cmc.deactivate();
				this.setProtocolTable(ureq);
			}
		}

		if (source == commentForm) {

			if (event == Form.EVNT_VALIDATION_OK) {

				// close modal dialog
				this.getWindowControl().pop();

				// for every protocol the exam office has choosen, add the
				// comment
				for (Protocol proto : protoList) {

					proto.setComments(commentForm.getComment());
					ProtocolManager.getInstance().updateProtocol(proto);
					this.setProtocolTable(ureq);
				}
			}

			if (event == Form.EVNT_FORM_CANCELLED) {

				this.getWindowControl().pop();
			}
		}

		if (source == editCommentForm) {

			if (event == Form.EVNT_VALIDATION_OK) {

				// close modal dialog
				this.getWindowControl().pop();

				// add the new comment to the choosen protocol and update it in
				// the db
				this.choosenProtocol.setComments(editCommentForm.getComment());
				ProtocolManager.getInstance().updateProtocol(choosenProtocol);

				// refresh view
				this.setProtocolTable(ureq);
			}

			if (event == Form.EVNT_FORM_CANCELLED) {

				this.getWindowControl().pop();
			}
		}
		// somebody has written an email
		if (source == mailForm) {

			if (event == Form.EVNT_VALIDATION_OK) {

				String subject = mailForm.getSubject() + ":"
						+ System.getProperty("line.separator");
				String body = mailForm.getBody();

				// send the mail and save the body/subject to the esf
				for (Protocol p : this.protoList) {

					MailManager.getInstance().sendEmail(subject, body,
							ureq.getIdentity());
					this.createCommentForStudent(ureq,
							ElectronicStudentFileManager.getInstance()
									.retrieveESFByIdentity(p.getIdentity()),
							subject + body);
				}
				// release the mailform
				this.getWindowControl().pop();
			}

			if (event == Form.EVNT_FORM_CANCELLED) {

				this.getWindowControl().pop();
			}
		}
		if (source == appTableCtr) {

			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {

				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();

				// subscribe to exam
				if (actionid.equals(AppointmentTableModel.SELECT_SUBSCRIBE)) {

					// register student to the choosen appointment
					this.registerStudent(appTableMdl.getEntryAt(te.getRowId()),
							ureq.getIdentity(), exam.getEarmarkedEnabled());
					this.setAppointmentTable(ureq);

					// add a comment to the esf

					// build the output which should come in the esf
					String examName = this.exam.getName();
					String[] args = { "'" + examName + "'" };

					// set identity and text of the comment
					String commentText = this.translator.translate(
							"ExamLaunchController.studentRegisteredHimself",
							args);

					this.createCommentForStudent(ureq,
							ElectronicStudentFileManager.getInstance()
									.retrieveESFByIdentity(ureq.getIdentity()),
							commentText);

					// for renew the screen, if your are admin the table would
					// not get updated
					this.setProtocolTable(ureq);
					this.setAppointmentTable(ureq);
				}
			}
		}

		if (source == myAppTableCtr) {

			// sign off from exam
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {

				protocol = ProtocolManager.getInstance().findProtocolByIdentityAndExam(ureq.getIdentity(), exam);

				CalendarManager.getInstance().deleteKalendarEventForExam(exam, ureq.getIdentity());
				
				BusinessControlFactory bcf = BusinessControlFactory.getInstance();

				// Email Remove
				MailManager.getInstance().sendEmail(
					translator.translate("ExamLaunchController.Remove.Subject",new String[] { ExamDBManager.getInstance().getExamName(exam) }),
					translator.translate("ExamLaunchController.Remove.Body",
						new String[] {
							ExamDBManager.getInstance().getExamName(exam),
							this.getRealName(protocol.getIdentity()),
							DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, translator.getLocale()).format(protocol.getAppointment().getDate()),
							protocol.getAppointment().getPlace(),
							new Integer(protocol.getAppointment().getDuration()).toString(),
							protocol.getExam().getIsOral() ? translator.translate("oral") : translator.translate("written"),
							bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
						}),
					protocol.getIdentity()
				);
				
				if (exam.getIsOral()) {
					Appointment tempApp = protocol.getAppointment();
					tempApp.setOccupied(false);
					AppointmentManager.getInstance().updateAppointment(tempApp);
					tempApp = null;
				}
				ProtocolManager.getInstance().deleteProtocol(protocol);
				this.setAppointmentTable(ureq);

				// build the output which should come in the esf
				String examName = this.exam.getName();
				String[] args = { "'" + examName + "'" };

				// set identity and text of the comment
				String commentText = this.translator
						.translate(
								"ExamLaunchController.studentDeRegisteredHimself",
								args);

				this
						.createCommentForStudent(ureq,
								ElectronicStudentFileManager.getInstance()
										.retrieveESFByIdentity(
												ureq.getIdentity()),
								commentText);
			}
		}

		if (source == earProtoTableCtr) {

			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {

				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();

				// somebody wants to open an esf
				if (actionid.equals(ProtocolTableModel.ESF_OPEN)) {
					this.launchEsfInNewTab(te, ureq, false);
				}

				if (actionid.equals(ProtocolTableModel.COMMAND_VCARD)) {

					this.openVCard(te, ureq, false);
				}
			}

			if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {

				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals("protoTable.remove")) {

					protoList = (List<Protocol>) earProtoTableMdl.getObjects(tmse.getSelection());
					for (Protocol p : protoList) {
						Locale userLocale = new Locale(p.getIdentity().getUser().getPreferences().getLanguage());
						Translator tmpTranslator = Util.createPackageTranslator(Exam.class, userLocale);
						BusinessControlFactory bcf = BusinessControlFactory.getInstance();
						
						// Email Remove
						MailManager.getInstance().sendEmail(
							tmpTranslator.translate("ExamLaunchController.Remove.Subject", new String[] { ExamDBManager.getInstance().getExamName(exam) }),
							tmpTranslator.translate("ExamLaunchController.Remove.Body",
								new String[] {
									ExamDBManager.getInstance().getExamName(exam),
									this.getRealName(p.getIdentity()),
									DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, tmpTranslator.getLocale()).format(p.getAppointment().getDate()),
									p.getAppointment().getPlace(),
									new Integer(p.getAppointment().getDuration()).toString(),
									p.getExam().getIsOral() ? tmpTranslator.translate("oral") : tmpTranslator.translate("written"),
									bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
								}),
							p.getIdentity()
						);
						
						// update appointment
						if (exam.getIsOral()) {
							Appointment tempApp = p.getAppointment();
							tempApp.setOccupied(false);
							AppointmentManager.getInstance().updateAppointment(
									tempApp);
							tempApp = null;
						}
						ProtocolManager.getInstance().deleteProtocol(p);

						// add a comment to the esf

						// build the output which should come in the esf
						String firstName = ureq.getIdentity().getUser()
								.getProperty(UserConstants.FIRSTNAME,
										ureq.getLocale());
						String lastName = ureq.getIdentity().getUser()
								.getProperty(UserConstants.LASTNAME,
										ureq.getLocale());
						String examName = this.exam.getName();
						String[] args = {
								("'" + lastName + ", " + firstName + "'"),
								"'" + examName + "'" };

						// set identity and text of the comment
						String commentText = this.translator
								.translate(
										"ExamLaunchController.removedFromEarmarkedStudentManually",
										args);

						this.createCommentForStudent(ureq,
										ElectronicStudentFileManager
												.getInstance()
												.retrieveESFByIdentity(
														p.getIdentity()),
										commentText);
					}
				}

				if (tmse.getAction().equals("protoTable.register")) {

					protoList = (ArrayList<Protocol>) earProtoTableMdl
							.getObjects(tmse.getSelection());
					for (Protocol proto : protoList) {
						proto.setEarmarked(false);
						ProtocolManager.getInstance().updateProtocol(proto);
						Locale userLocale = new Locale(proto.getIdentity().getUser().getPreferences().getLanguage());
						Translator tmpTranslator = Util.createPackageTranslator(Exam.class, userLocale);
						BusinessControlFactory bcf = BusinessControlFactory.getInstance();

						// TODO testen ob das hier hinhaut mit der erzeugten
						// email... earmarked -> registered
						Appointment tempApp = proto.getAppointment();

						// Email Register
						MailManager.getInstance().sendEmail(
							tmpTranslator.translate("ExamLaunchController.Register.Subject", new String[] { ExamDBManager.getInstance().getExamName(exam) }),
							tmpTranslator.translate("ExamLaunchController.Register.Body",
								new String[] {
									ExamDBManager.getInstance().getExamName(exam),
									this.getRealName(proto.getIdentity()),
									DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, translator.getLocale()).format(proto.getAppointment().getDate()),
									proto.getAppointment().getPlace(),
									new Integer(proto.getAppointment().getDuration()).toString(),
									proto.getExam().getIsOral() ? translator.translate("oral") : translator.translate("written"),
									bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true),
									translator.translate(proto.getEarmarked() ? "ExamLaunchController.status.earmarked" : "ExamLaunchController.status.registered"),
									this.getSemester(tempApp),
									this.getRealName(exam.getIdentity()),
									proto.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null),
									proto.getIdentity().getUser().getProperty(UserConstants.EMAIL, null),
									proto.getIdentity().getUser().getProperty(UserConstants.STUDYSUBJECT, null)
								}),
							proto.getIdentity()
						);

						// add a comment to the esf

						// build the output which should come in the esf
						String firstName = ureq.getIdentity().getUser()
								.getProperty(UserConstants.FIRSTNAME,
										ureq.getLocale());
						String lastName = ureq.getIdentity().getUser()
								.getProperty(UserConstants.LASTNAME,
										ureq.getLocale());
						String examName = this.exam.getName();
						String[] args = {
								("'" + lastName + ", " + firstName + "'"),
								"'" + examName + "'" };

						// set identity and text of the comment
						String commentText = this.translator
								.translate(
										"ExamLaunchController.registeredFromEarmarkedStudentManually",
										args);

						this.createCommentForStudent(ureq,
								ElectronicStudentFileManager.getInstance()
										.retrieveESFByIdentity(
												proto.getIdentity()),
								commentText);
					}
				}

				if (tmse.getAction().equals("protoTable.addComment")) {

					protoList = (ArrayList<Protocol>) earProtoTableMdl
							.getObjects(tmse.getSelection());

					if (protoList.size() == 1) {
						commentForm = new ESFCommentCreateAndEditForm(ureq,
								getWindowControl(), VELOCITY_ROOT, translator,
								null);
						commentForm.addControllerListener(this);

						// make it a modal dialog
						cmc = new CloseableModalController(getWindowControl(),
								translator.translate("close"), commentForm
										.getInitialComponent());
						this.listenTo(cmc);
						cmc.activate();
					}

					else {

						this
								.getWindowControl()
								.setWarning(
										translator
												.translate("ExamLaunchController.noIdentityChosen"));
					}
				}

				// somebody wants to edit a comment
				if (tmse.getAction().equals("protoTable.editComment")) {

					List<Protocol> protocolList = this.earProtoTableMdl
							.getObjects(tmse.getSelection());

					// you could only edit one comment at a time
					if (protocolList.size() == 1) {

						choosenProtocol = ProtocolManager
								.getInstance()
								.findProtocolByIdentityAndExam(
										protocolList.get(0).getIdentity(), exam);

						// create temporary commentEntry
						this.commentEntry = CommentManager.getInstance()
								.createCommentEntry();
						this.commentEntry.setComment(choosenProtocol
								.getComments());
						editCommentForm = new ESFCommentCreateAndEditForm(ureq,
								getWindowControl(),
								"ESFCommentCreateAndEditForm", this.translator,
								commentEntry);
						editCommentForm.addControllerListener(this);

						editCommentCtr = new CloseableModalController(
								getWindowControl(), translate("close"),
								editCommentForm.getInitialComponent());
						listenTo(editCommentCtr);
						editCommentCtr.activate();
					} else {

						this
								.getWindowControl()
								.setInfo(
										translator
												.translate("ExamLaunchController.pleaseChoseOnlyOneComment"));
					}
				}

				// somebody wants to send an email
				if (tmse.getAction().equals("earTable.mailToStudent")) {

					this.protoList = this.regProtoTableMdl.getObjects(tmse
							.getSelection());

					if (protoList.size() >= 1) {

						mailForm = new MailForm(ureq, getWindowControl(),
								"mailForm", this.translator);
						mailForm.addControllerListener(this);

						sendMailCtr = new CloseableModalController(
								getWindowControl(), translate("close"),
								mailForm.getInitialComponent());
						listenTo(sendMailCtr);
						sendMailCtr.activate();
					} else {

						this
								.getWindowControl()
								.setWarning(
										translator
												.translate("ExamLaunchController.atLeastOneStudent"));
					}
				}

				this.setProtocolTable(ureq);
			}
		}

		// Registered table
		if (source == regProtoTableCtr) {

			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {

				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();

				if (actionid.equals(ProtocolTableModel.COMMAND_VCARD)) {

					this.openVCard(te, ureq, true);
				}

				// somebody wants to open an esf
				if (actionid.equals(ProtocolTableModel.ESF_OPEN)) {

					Protocol protocol = this.regProtoTableMdl.getEntryAt(te
							.getRowId());
					ElectronicStudentFile esf = ElectronicStudentFileManager
							.getInstance().retrieveESFByIdentity(
									protocol.getIdentity());

					this.launchEsfInNewTab(te, ureq, true);
				}

			}

			if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {

				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals("protoTable.remove")) {

					protoList = (List<Protocol>) regProtoTableMdl.getObjects(tmse.getSelection());

					BusinessControlFactory bcf = BusinessControlFactory.getInstance();
					ExamDBManager edb = ExamDBManager.getInstance();
					for (Protocol p : protoList) {
						Locale userLocale = new Locale(p.getIdentity().getUser().getPreferences().getLanguage());
						Translator tmpTranslator = Util.createPackageTranslator(Exam.class, userLocale);

						// Email Remove
						MailManager.getInstance().sendEmail(
							tmpTranslator.translate("ExamLaunchController.Remove.Subject", new String[] { ExamDBManager.getInstance().getExamName(exam) }),
							tmpTranslator.translate("ExamLaunchController.Remove.Body",
								new String[] {
									// exam name, user name, exam date, exam location, exam duration, oral/written, link
									edb.getExamName(exam),
									this.getRealName(p.getIdentity()),
									DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, userLocale).format(p.getAppointment().getDate()),
									p.getAppointment().getPlace(),
									new Integer(p.getAppointment().getDuration()).toString(),
									p.getExam().getIsOral() ? tmpTranslator.translate("oral") : tmpTranslator.translate("written"),
									bcf.getAsURIString(bcf.createCEListFromString(edb.findRepositoryEntryOfExam(exam)), true)
								}),
							p.getIdentity()
						);

						// update appointment
						if (exam.getIsOral()) {
							Appointment tempApp = p.getAppointment();
							tempApp.setOccupied(false);
							AppointmentManager.getInstance().updateAppointment(
									tempApp);
							tempApp = null;
						}
						ProtocolManager.getInstance().deleteProtocol(p);

						// add a comment to the esf

						// build the output which should come in the esf
						String firstName = ureq.getIdentity().getUser()
								.getProperty(UserConstants.FIRSTNAME,
										ureq.getLocale());
						String lastName = ureq.getIdentity().getUser()
								.getProperty(UserConstants.LASTNAME,
										ureq.getLocale());
						String examName = this.exam.getName();
						String[] args = {
								("'" + lastName + ", " + firstName + "'"),
								"'" + examName + "'" };

						// set identity and text of the comment
						String commentText = this.translator.translate(
								"ExamLaunchController.removedStudentManually",
								args);

						this
								.createCommentForStudent(ureq,
										ElectronicStudentFileManager
												.getInstance()
												.retrieveESFByIdentity(
														p.getIdentity()),
										commentText);
					}
				}

				if (tmse.getAction().equals("protoTable.earmark")) {

					protoList = (List<Protocol>) regProtoTableMdl.getObjects(tmse.getSelection());
					for (Protocol p : protoList) {
						p.setEarmarked(true);
						ProtocolManager.getInstance().updateProtocol(p);
						Locale userLocale = new Locale(p.getIdentity().getUser().getPreferences().getLanguage());
						Translator tmpTranslator = Util.createPackageTranslator(Exam.class, userLocale);
						BusinessControlFactory bcf = BusinessControlFactory.getInstance();

						// Email MoveToEarmarked
						MailManager.getInstance().sendEmail(
							tmpTranslator.translate("ExamLaunchController.MoveToEarmarked.Subject", new String[] { ExamDBManager.getInstance().getExamName(exam) }),
							tmpTranslator.translate("ExamLaunchController.MoveToEarmarked.Body",
								new String[] {
									ExamDBManager.getInstance().getExamName(exam),
									this.getRealName(p.getIdentity()),
									DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, userLocale).format(p.getAppointment().getDate()),
									p.getAppointment().getPlace(),
									new Integer(p.getAppointment().getDuration()).toString(),
									p.getExam().getIsOral() ? tmpTranslator.translate("oral") : tmpTranslator.translate("written"),
									bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
								}),
							p.getIdentity()
						);

						// add a comment to the esf

						// build the output which should come in the esf
						String firstName = ureq.getIdentity().getUser()
								.getProperty(UserConstants.FIRSTNAME,
										ureq.getLocale());
						String lastName = ureq.getIdentity().getUser()
								.getProperty(UserConstants.LASTNAME,
										ureq.getLocale());
						String examName = this.exam.getName();
						String[] args = {
								("'" + lastName + ", " + firstName + "'"),
								"'" + examName + "'" };

						// set identity and text of the comment
						String commentText = this.translator
								.translate(
										"ExamLaunchController.earmarkedStudentManually",
										args);

						this
								.createCommentForStudent(ureq,
										ElectronicStudentFileManager
												.getInstance()
												.retrieveESFByIdentity(
														p.getIdentity()),
										commentText);
					}
				}

				if (tmse.getAction().equals("protoTable.note")) {

					protoList = (ArrayList<Protocol>) regProtoTableMdl
							.getObjects(tmse.getSelection());

					if (protoList.size() != 0) {
						editMarkForm = new EditMarkForm(ureq,
								getWindowControl(), "editMarkForm", translator);
						editMarkForm.addControllerListener(this);
						cmc = new CloseableModalController(this
								.getWindowControl(), translator
								.translate("close"), editMarkForm
								.getInitialComponent());
						cmc.activate();
					}
				}

				if (tmse.getAction().equals("protoTable.addComment")) {

					protoList = (ArrayList<Protocol>) regProtoTableMdl
							.getObjects(tmse.getSelection());

					if (protoList.size() == 1) {
						commentForm = new ESFCommentCreateAndEditForm(ureq,
								getWindowControl(), VELOCITY_ROOT, translator,
								null);
						commentForm.addControllerListener(this);

						// make it a modal dialog
						cmc = new CloseableModalController(getWindowControl(),
								translator.translate("close"), commentForm
										.getInitialComponent());
						this.listenTo(cmc);
						cmc.activate();
					} else {

						this
								.getWindowControl()
								.setWarning(
										translator
												.translate("ExamLaunchController.noIdentityChosen"));
					}
				}

				// somebody wants to edit a comment
				if (tmse.getAction().equals("protoTable.editComment")) {

					List<Protocol> protocolList = this.regProtoTableMdl
							.getObjects(tmse.getSelection());

					// you could only edit one comment at a time
					if (protocolList.size() == 1) {

						choosenProtocol = ProtocolManager
								.getInstance()
								.findProtocolByIdentityAndExam(
										protocolList.get(0).getIdentity(), exam);

						// create temporary commentEntry
						this.commentEntry = CommentManager.getInstance()
								.createCommentEntry();
						this.commentEntry.setComment(choosenProtocol
								.getComments());
						editCommentForm = new ESFCommentCreateAndEditForm(ureq,
								getWindowControl(),
								"ESFCommentCreateAndEditForm", this.translator,
								commentEntry);
						editCommentForm.addControllerListener(this);

						editCommentCtr = new CloseableModalController(
								getWindowControl(), translate("close"),
								editCommentForm.getInitialComponent());
						listenTo(editCommentCtr);
						editCommentCtr.activate();
					} else {

						this
								.getWindowControl()
								.setWarning(
										translator
												.translate("ExamLaunchController.pleaseChoseOnlyOneComment"));
					}
				}

				// somebody wants to send an email
				if (tmse.getAction().equals("protoTable.mailToStudent")) {

					this.protoList = this.regProtoTableMdl.getObjects(tmse
							.getSelection());

					if (protoList.size() >= 1) {

						mailForm = new MailForm(ureq, getWindowControl(),
								"mailForm", this.translator);
						mailForm.addControllerListener(this);

						sendMailCtr = new CloseableModalController(
								getWindowControl(), translate("close"),
								mailForm.getInitialComponent());
						listenTo(sendMailCtr);
						sendMailCtr.activate();
					} else {

						this
								.getWindowControl()
								.setWarning(
										translator
												.translate("ExamLaunchController.atLeastOneStudent"));
					}
				}

				this.setProtocolTable(ureq);
			}
		}

		// UserSearchController has found an identity
		if (source == usc) {

			if (event instanceof SingleIdentityChosenEvent) {

				id = ((SingleIdentityChosenEvent) event).getChosenIdentity();
				cmc.deactivate();
				// appointment table
				TableGuiConfiguration tgc = new TableGuiConfiguration();
				tgc.setColumnMovingOffered(true);
				tgc.setDownloadOffered(true);
				tgc.setPreferencesOffered(true,
						"ExamLaunchController.chooseAppTable");
				tgc
						.setTableEmptyMessage(translator
								.translate("ExamEditorController.appointmentTable.empty"));
				chooseAppCtr = new TableController(tgc, ureq, this
						.getWindowControl(), translator);
				List<Appointment> appList;
				appList = AppointmentManager.getInstance()
						.findAllAvailableAppointmentsByExamId(exam.getKey());
				chooseAppMdl = new AppointmentTableModel(ureq.getLocale(),
						appList, AppointmentTableModel.SELECT_SUBSCRIBE);
				chooseAppMdl.setTable(chooseAppCtr);
				chooseAppCtr.setTableDataModel(chooseAppMdl);
				chooseAppCtr.setSortColumn(0, true);

				// NEU
				chooseAppCtr.addControllerListener(this);

				cmc = new CloseableModalController(this.getWindowControl(),
						translator.translate("close"), chooseAppCtr
								.getInitialComponent());
				cmc.activate();
			}
		}

		// register student manually
		if (source == chooseAppCtr) {

			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {

				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				cmc.deactivate();
				// subscribe student to exam manually
				if (actionid.equals(AppointmentTableModel.SELECT_SUBSCRIBE)) {

					ElectronicStudentFile esf = ElectronicStudentFileManager
							.getInstance().retrieveESFByIdentity(id);

					if (esf != null) {

						// register student in the choosen appointment
						Appointment tempApp = chooseAppMdl.getEntryAt(te
								.getRowId());
						this.registerStudent(tempApp, id, false);

						// build the output which should come in the esf
						String firstName = ureq.getIdentity().getUser()
								.getProperty(UserConstants.FIRSTNAME,
										ureq.getLocale());
						String lastName = ureq.getIdentity().getUser()
								.getProperty(UserConstants.LASTNAME,
										ureq.getLocale());
						String examName = this.exam.getName();
						String[] args = {
								("'" + lastName + ", " + firstName + "'"),
								"'" + examName + "'" };

						// set identity and text of the comment
						String commentText = this.translator
								.translate(
										"ExamLaunchController.registeredStudentManually",
										args);

						this.createCommentForStudent(ureq, esf, commentText);
					} else {

						this
								.getWindowControl()
								.setInfo(
										translator
												.translate("ExamLaunchController.studentHasNoESF"));
					}
				}
				this.setProtocolTable(ureq);
			}
		}
	}

	/**
	 * just for lazyness :)
	 */
	private String getRealName(Identity ident) {

		String realName = ident.getUser().getProperty(UserConstants.LASTNAME,
				null)
				+ ", "
				+ ident.getUser().getProperty(UserConstants.FIRSTNAME, null);

		return realName;
	}

	/**
	 * ## temporarily, used at the university of leipzig ## checks, if the user,
	 * who wants to subscribe, has a correct (int) institution identifier
	 * 
	 * @param ident
	 * @return true, if the institution identifier is an integer (university of
	 *         leipzig)
	 */
	private boolean hasInstitutionIdentifier(Identity ident) {

		try {

			new Integer(ident.getUser().getProperty(
					UserConstants.INSTITUTIONALUSERIDENTIFIER, null));
		} catch (NumberFormatException nfe) {

			this
					.getWindowControl()
					.setWarning(
							translator
									.translate("ExamLaunchController.warning.institutionalNumber"));
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param app
	 * @param id
	 */
	private void registerStudent(Appointment app, Identity id,
			boolean isEarmarked) {

		Appointment tempApp = AppointmentManager.getInstance()
				.findAppointmentByID(app.getKey());

		ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance()
				.retrieveESFByIdentity(id);

		if (esf != null) {

			// check if app is available
			if ((tempApp != null) && (!tempApp.getOccupied())) {
				Protocol proto = ProtocolManager.getInstance().createProtocol();
				proto.setIdentity(id);
				proto.setEarmarked(isEarmarked);
				proto.setExam(exam);

				// set appointment to occupied if its an oral exam
				if (exam.getIsOral()) {
					tempApp.setOccupied(true);
					AppointmentManager.getInstance().updateAppointment(tempApp);
				}
				proto.setAppointment(tempApp);
				ProtocolManager.getInstance().saveProtocol(proto);

				// add the protocol to the students esf
				esf.addProtocol(proto);
				ElectronicStudentFileManager.getInstance()
						.updateElectronicStundentFile(esf);

				CalendarManager.getInstance().createKalendarEventForExam(exam,
						id, res);
				
				BusinessControlFactory bcf = BusinessControlFactory.getInstance();
				// Email Register
				// // Email Bodies and Subjects, vars: {0} exam name {1} last
				// name, first name {2} app.date {3} app.place {4} app.duration
				// {5} exam.type {6} exam.url {7} proto.earmarked {8} semester
				// {9} author {10} studserv email {11} email {12} studyPath
				MailManager.getInstance().sendEmail(
					translator.translate("ExamLaunchController.Register.Subject", new String[] { ExamDBManager.getInstance().getExamName(exam) }),
					translator.translate("ExamLaunchController.Register.Body",
						new String[] {
							ExamDBManager.getInstance().getExamName(exam),
							this.getRealName(proto.getIdentity()),
							DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, translator.getLocale()).format(proto.getAppointment().getDate()),
							proto.getAppointment().getPlace(),
							new Integer(proto.getAppointment().getDuration()).toString(),
							proto.getExam().getIsOral() ? translator.translate("oral") : translator.translate("written"),
							bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true),
							translator.translate(proto.getEarmarked() ? "ExamLaunchController.status.earmarked" : "ExamLaunchController.status.registered"),
							this.getSemester(tempApp),
							this.getRealName(exam.getIdentity()),
							proto.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null),
							proto.getIdentity().getUser().getProperty(UserConstants.EMAIL, null),
							proto.getIdentity().getUser().getProperty(UserConstants.STUDYSUBJECT, null)
						}),
					proto.getIdentity()
				);
			} else {
				Tracing.logError("app == null: " + (tempApp == null)
						+ "  tempApp.isOccupied:" + tempApp.getOccupied(),
						ExamLaunchController.class);
				this
						.getWindowControl()
						.setInfo(
								translator
										.translate("ExamLaunchController.info.appNotAvailable"));
			}
		} else {

			this
					.getWindowControl()
					.setInfo(
							translator
									.translate("ExamLaunchController.register.couldNotRegisterNoESF"));
		}

	}

	/**
	 * returns the semester calculate out of the personal appointment of a
	 * student
	 * 
	 * @param appointment
	 *            - the appointment the student has subscribed
	 * @return the semester of the exam: SS 2008 or WS 2008
	 */
	private String getSemester(Appointment appointment) {

		int month = appointment.getDate().getMonth();

		int year = appointment.getDate().getYear() + 1900;

		if (month >= 3 && month <= 8)
			return "SS " + year;
		else
			return "WS " + year + "/" + (year + 1);
	}

	private void launchEsfInNewTab(TableEvent tableEvent, UserRequest ureq,
			boolean regTableMdl) {

		Protocol proto = regTableMdl ? this.regProtoTableMdl
				.getEntryAt(tableEvent.getRowId()) : this.earProtoTableMdl
				.getEntryAt(tableEvent.getRowId());
		ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance()
				.retrieveESFByIdentity(proto.getIdentity());

		OLATResourceable ores = OLATResourceManager.getInstance()
				.findResourceable(esf.getResourceableId(),
						ElectronicStudentFile.ORES_TYPE_NAME);

		// add the esf in a dtab
		DTabs dts = (DTabs) Windows.getWindows(ureq).getWindow(ureq)
				.getAttribute("DTabs");
		DTab dt = dts.getDTab(ores);
		if (dt == null) {
			// does not yet exist -> create and add
			dt = dts.createDTab(ores, esf.getIdentity().getName());

			if (dt == null)
				return;
			ESFEditController esfLaunchCtr = new ESFEditController(ureq, dt
					.getWindowControl(), esf);
			dt.setController(esfLaunchCtr);
			dts.addDTab(ureq, dt);
		}
		dts.activate(ureq, dt, null);
	}

	private void openVCard(TableEvent tableEvent, UserRequest ureq,
			boolean regTableMdl) {
		// get identitiy and open new visiting card controller in new window
		Protocol proto = regTableMdl ? this.regProtoTableMdl
				.getEntryAt(tableEvent.getRowId()) : this.earProtoTableMdl
				.getEntryAt(tableEvent.getRowId());
		Identity identity = proto.getIdentity();

		if (identity != null) {
			HomePageConfigManager hpcm = HomePageConfigManagerImpl
					.getInstance();
			OLATResourceable ores = hpcm.loadConfigFor(identity.getName());

			DTabs dts = (DTabs) Windows.getWindows(ureq).getWindow(ureq)
					.getAttribute("DTabs");
			DTab dt = dts.getDTab(ores);
			if (dt == null) {
				// does not yet exist -> create and add
				dt = dts.createDTab(ores, identity.getName());
				if (dt == null)
					return;
				UserInfoMainController uimc = new UserInfoMainController(ureq,
						dt.getWindowControl(), identity);
				dt.setController(uimc);
				dts.addDTab(ureq, dt);
			}
			dts.activate(ureq, dt, null);
		}
	}

	private void createCommentForStudent(UserRequest ureq,
			ElectronicStudentFile esf, String comment) {

		if (esf == null)
			throw new AssertException(
					"There is no electronic student file for a student.");
		// create comment
		CommentEntry commentEntry = CommentManager.getInstance()
				.createCommentEntry(comment, ureq.getIdentity());

		// add to esf an update the esf
		esf.addCommentEntry(commentEntry);
		ElectronicStudentFileManager.getInstance()
				.updateElectronicStundentFile(esf);
	}
}