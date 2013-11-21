package de.unileipzig.xman.exam.controllers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.modal.ButtonClickedEvent;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Util;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResourceManager;

import de.unileipzig.xman.admin.mail.MailManager;
import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.appointment.AppointmentManager;
import de.unileipzig.xman.appointment.tables.AppointmentTableModel;
import de.unileipzig.xman.calendar.CalendarManager;
import de.unileipzig.xman.catalog.controller.ExamCatalogController;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.exam.ExamHandler;
import de.unileipzig.xman.exam.AlreadyLockedException;
import de.unileipzig.xman.exam.forms.CreateAndEditAppointmentForm;
import de.unileipzig.xman.exam.forms.EditCommentsForm;
import de.unileipzig.xman.exam.forms.EditEarmarkedForm;
import de.unileipzig.xman.exam.forms.EditMultiSubscriptionForm;
import de.unileipzig.xman.exam.forms.EditRegistrationForm;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;
import de.unileipzig.xman.protocol.archived.ArchivedProtocol;
import de.unileipzig.xman.protocol.archived.ArchivedProtocolManager;

/**
 * 
 * @author
 */
public class ExamEditorController extends BasicController {

	private static final String VELOCITY_ROOT = Util
			.getPackageVelocityRoot(Exam.class);

	private static final String EXAM_EDITOR_LOCK = "examEditor.lock";
	private static final String CMD_TOOLS_CLOSE_EXAM_YES_NO = "toolCtr.closeExamYesNo";

	private Panel mainPanel;
	private VelocityContainer vcMain, vcApp;
	private LayoutMain3ColsController layoutCtr;

	private LockResult lockResult;
	private Exam exam;
	private Appointment app;
	private TabbedPane tabbedPane;
	private EditCommentsForm editCommentsForm;
	private EditRegistrationForm editRegForm;
	private EditEarmarkedForm editEarmarkedForm;
	private EditMultiSubscriptionForm editMultiSubscriptionForm;
	private CreateAndEditAppointmentForm createAppForm, editAppForm;
	private TableController appTableCtr;
	private AppointmentTableModel appTableMdl;
	private Link addAppLink;
	private CloseableModalController cmc;
	private RepositoryEntry repoEntry;
	
	private Link archiveExamLink;
	private DialogBoxController archiveExamOkCancelDialog;

	/**
	 * creates the controller for the exam editor
	 * 
	 * @param ureq
	 *            the UserRequest
	 * @param wControl
	 *            the window control
	 * @param res
	 *            the olat resourceable for the exam
	 * @throws AlreadyLockedException When the editor is already locked by another user, contains name as message
	 */
	public ExamEditorController(UserRequest ureq, WindowControl wControl,
			OLATResourceable res) throws AlreadyLockedException {
		super(ureq, wControl);

		this.exam = ExamDBManager.getInstance().findExamByID(
				res.getResourceableId());
		repoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(res,
				true);

		setTranslator(Util.createPackageTranslator(Exam.class, ureq.getLocale()));

		// try to acquire edit lock for this course.
		lockResult = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(res, ureq.getIdentity(), EXAM_EDITOR_LOCK);

		if (lockResult.isSuccess()) {

			vcMain = new VelocityContainer("examEditor", VELOCITY_ROOT
					+ "/examEditor.html", getTranslator(), this);

			this.createTabbedPane(ureq);

			mainPanel = new Panel("examEditorPanel");
			mainPanel.setContent(vcMain);
			
			archiveExamLink = LinkFactory.createButton("ExamEditorController.link.archiveExam", vcMain, this);

			layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, mainPanel, "examEditorCtrLayoutKey");

			putInitialPanel(layoutCtr.getInitialComponent());
		} else {
			// Throw exception with user that currently holds the lock
			User user = lockResult.getOwner().getUser();
			throw new AlreadyLockedException(user.getProperty(UserConstants.FIRSTNAME, null) + " " + user.getProperty(UserConstants.LASTNAME, null));
		}
	}

	/**
	 * creates the tabbed pane
	 * 
	 * @param ureq
	 *            the user request
	 */
	private void createTabbedPane(UserRequest ureq) {

		tabbedPane = new TabbedPane("examEditorTabbedPane", ureq.getLocale());
		tabbedPane.addListener(this);
		vcMain.put("tabbedPane", tabbedPane);

		editCommentsForm = new EditCommentsForm(ureq, this.getWindowControl(),
				"editCommentsForm", getTranslator(), exam.getComments());
		editCommentsForm.addControllerListener(this);
		tabbedPane.addTab(translate("ExamEditorController.tabbedPane.comments"),
				editCommentsForm.getInitialComponent());

		editRegForm = new EditRegistrationForm(ureq, this.getWindowControl(),
				"editRegistrationForm", getTranslator(), exam.getRegStartDate(),
				exam.getRegEndDate(), exam.getSignOffDate());
		editRegForm.addControllerListener(this);
		tabbedPane.addTab(translate("ExamEditorController.tabbedPane.registration"),
				editRegForm.getInitialComponent());

		if (exam.getRegEndDate() != null) {
			vcApp = new VelocityContainer("appPage", VELOCITY_ROOT
					+ "/tabApp.html", getTranslator(), this);
			addAppLink = LinkFactory.createButtonSmall(
					"ExamEditorController.link.addAppointment", vcApp, this);
			boolean enableAppLink = true;
			if (!exam.getIsOral()) {
				if (AppointmentManager.getInstance()
						.findAllAppointmentsByExamId(exam.getKey()).size() != 0) {
					enableAppLink = false;
				}
			}
			vcApp.contextPut("enableAppLink", enableAppLink);
			TableGuiConfiguration tgc = new TableGuiConfiguration();
			tgc.setMultiSelect(true);
			tgc.setColumnMovingOffered(true);
			tgc.setDownloadOffered(true);
			tgc.setTableEmptyMessage(translate("ExamEditorController.appointmentTable.empty"));
			appTableCtr = new TableController(tgc, ureq, this
					.getWindowControl(), getTranslator());
			appTableCtr.setMultiSelect(true);
			appTableCtr.addMultiSelectAction(
					"ExamEditorController.appointmentTable.edit",
					"appTable.edit");
			if (exam.getIsOral())
				appTableCtr.addMultiSelectAction(
						"ExamEditorController.appointmentTable.del",
						"appTable.del");
			List<Appointment> appList = AppointmentManager.getInstance()
					.findAllAppointmentsByExamId(exam.getKey());
			appTableMdl = new AppointmentTableModel(ureq.getLocale(), appList,
					AppointmentTableModel.NO_SELECTION);
			appTableMdl.setTable(appTableCtr);
			appTableCtr.setTableDataModel(appTableMdl);
			appTableCtr.setSortColumn(0, true);

			// NEU
			appTableCtr.addControllerListener(this);

			vcApp.put("appointmentTable", appTableCtr.getInitialComponent());
			tabbedPane.addTab(translate("ExamEditorController.tabbedPane.appointments"),
					vcApp);
		}

		editEarmarkedForm = new EditEarmarkedForm(ureq,
				this.getWindowControl(), "editEarmarkedForm", getTranslator(), exam
						.getEarmarkedEnabled());
		editEarmarkedForm.addControllerListener(this);
		tabbedPane.addTab(translate("ExamEditorController.tabbedPane.earmarked"),
				editEarmarkedForm.getInitialComponent());
		
		if(exam.getIsOral()) { // only oral exams can have multiSubscription
			editMultiSubscriptionForm = new EditMultiSubscriptionForm(ureq, getWindowControl(), exam.getIsMultiSubscription());
			editMultiSubscriptionForm.addControllerListener(this);
			tabbedPane.addTab(translate("EditMultiSubscriptionForm.name"), editMultiSubscriptionForm.getInitialComponent());
		}

		ExamCatalogController ecc = new ExamCatalogController(ureq, this
				.getWindowControl(), exam);
		tabbedPane.addTab(translate("ExamCatalogController.catalog"), ecc
						.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if(lockResult != null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockResult);
		}
		removeAsListenerAndDispose(archiveExamOkCancelDialog);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == addAppLink) {
			VelocityContainer vcEditApp = new VelocityContainer(
					"createAppPage", VELOCITY_ROOT + "/createApp.html",
					getTranslator(), this);
			createAppForm = new CreateAndEditAppointmentForm(ureq, this
					.getWindowControl(), "createAppForm", getTranslator(), exam
					.getIsOral(), null);
			createAppForm.addControllerListener(this);
			vcEditApp.put("createAppForm", createAppForm.getInitialComponent());
			cmc = new CloseableModalController(this.getWindowControl(),
					translate("close"), vcEditApp);
			cmc.activate();
		} else if(source == archiveExamLink) {
			archiveExamOkCancelDialog = activateOkCancelDialog(ureq, translate("ExamEditorController.link.archiveExam"), translate("ExamEditorController.link.archiveExam.warning"), archiveExamOkCancelDialog);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == appTableCtr) {
			if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals("appTable.edit")) {
					List<Appointment> appList = (ArrayList<Appointment>) appTableMdl
							.getObjects(tmse.getSelection());
					if (appList.size() == 1) {
						app = appList.get(0);
						editAppForm = new CreateAndEditAppointmentForm(ureq,
								this.getWindowControl(), "editAppForm",
								getTranslator(), exam.getIsOral(), app);
						editAppForm.addControllerListener(this);
						cmc = new CloseableModalController(this
								.getWindowControl(), translate("close"), editAppForm
								.getInitialComponent());
						cmc.activate();
					} else {
						this
								.getWindowControl()
								.setInfo(translate("ExamEditorController.appointmentTable.noSingleChoose"));
					}
				}
				// delete appointments (and belonging protocols and calendar
				// events)
				else if (tmse.getAction().equals("appTable.del")) {

					List<Appointment> appList = (List<Appointment>) appTableMdl.getObjects(tmse.getSelection());
					for (Appointment tempApp : appList) {
						List<Protocol> protoList = ProtocolManager
								.getInstance().findAllProtocolsByAppointment(
										tempApp);
						for (Protocol p : protoList) {
							p = ProtocolManager.getInstance().findProtocolByID(p.getKey());
							
							CalendarManager.getInstance().deleteKalendarEventForExam(exam,p.getIdentity());
							Locale userLocale = new Locale(p.getIdentity().getUser().getPreferences().getLanguage());
							Translator tmpTranslator = Util.createPackageTranslator(Exam.class, userLocale);
							BusinessControlFactory bcf = BusinessControlFactory.getInstance();
							
							// Email DeleteAppointment
							MailManager.getInstance().sendEmail(
								tmpTranslator.translate("ExamEditorController.DeleteAppointment.Subject", new String[] { ExamDBManager.getInstance().getExamName(exam) }),
								tmpTranslator.translate("ExamEditorController.DeleteAppointment.Body",
									new String[] {
										ExamDBManager.getInstance().getExamName(exam),
										p.getIdentity().getUser().getProperty(UserConstants.LASTNAME, ureq.getLocale()) + ", " + p.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, ureq.getLocale()),
										DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, tmpTranslator.getLocale()).format(tempApp.getDate()),
										tempApp.getPlace(),
										new Integer(tempApp.getDuration()).toString(),
										p.getExam().getIsOral() ? tmpTranslator.translate("oral") : tmpTranslator.translate("written"),
										bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
									}),
								p.getIdentity()
							);
							
							ProtocolManager.getInstance().deleteProtocol(p);
						}
						tempApp = AppointmentManager.getInstance().findAppointmentByID(tempApp.getKey());
						AppointmentManager.getInstance().deleteAppointment(tempApp);
					}

					appTableMdl.setEntries(AppointmentManager.getInstance()
							.findAllAppointmentsByExamId(exam.getKey()));
					appTableCtr.modelChanged();
				}
			}
		} else if (source == editCommentsForm) {

			if (event == Form.EVNT_VALIDATION_OK) {

				exam = ExamDBManager.getInstance().findExamByID(exam.getKey());
				exam.setComments(editCommentsForm.getComments());
				ExamDBManager.getInstance().updateExam(exam);
			}
		} else if (source == editRegForm) {

			if (event == Form.EVNT_VALIDATION_OK) {

				exam = ExamDBManager.getInstance().findExamByID(exam.getKey());
				exam.setRegStartDate(editRegForm.getRegStart());
				exam.setRegEndDate(editRegForm.getRegEnd());
				exam.setSignOffDate(editRegForm.getSignOff());
				ExamDBManager.getInstance().updateExam(exam);
				this.createTabbedPane(ureq);
				tabbedPane.setSelectedPane(1);
			}
		} else if (source == editEarmarkedForm) {
			
			if (event == Form.EVNT_VALIDATION_OK) {
				
				// TODO: hier müssen entweder die vorgemerkten studenten
				// gelöscht oder in die registrierten liste übernommen werden
				exam = ExamDBManager.getInstance().findExamByID(exam.getKey());
				exam.setEarmarkedEnabled(editEarmarkedForm.getEarmarked());
				ExamDBManager.getInstance().updateExam(exam);
			}
		} else if (source == editMultiSubscriptionForm) {
			if (event == Form.EVNT_VALIDATION_OK) {
				exam = ExamDBManager.getInstance().findExamByID(exam.getKey());
				exam.setIsMultiSubscription(editMultiSubscriptionForm.getMultiSubscription());
				ExamDBManager.getInstance().updateExam(exam);
			}
		} else if (source == createAppForm) {

			if (event == Form.EVNT_VALIDATION_OK) {

				cmc.deactivate();
				// written exam
				if (!exam.getIsOral()) {

					app = AppointmentManager.getInstance().createAppointment();
					app.setDuration(createAppForm.getDuration());
					app.setPlace(createAppForm.getPlace());
					app.setDate(createAppForm.getDate());
					app.setExam(exam);
					app.setOccupied(false);
					AppointmentManager.getInstance().saveAppointment(app);
					vcApp.contextPut("enableAppLink", false);

				} else {
					AppointmentManager.getInstance().saveAppointmentsByWizard(
							exam, createAppForm.getDate(),
							createAppForm.getPlace(), createAppForm.getCount(),
							createAppForm.getDuration(),
							createAppForm.getPause());
				}
				appTableMdl.setEntries(AppointmentManager.getInstance()
						.findAllAppointmentsByExamId(exam.getKey()));
				appTableCtr.modelChanged();
			}
		} else if (source == editAppForm) {

			if (event == Form.EVNT_VALIDATION_OK) {

				cmc.deactivate();
				app = AppointmentManager.getInstance().findAppointmentByID(app.getKey());
				app.setDate(editAppForm.getDate());
				app.setPlace(editAppForm.getPlace());
				app.setDuration(editAppForm.getDuration());
				AppointmentManager.getInstance().updateAppointment(app);
				appTableMdl.setEntries(AppointmentManager.getInstance()
						.findAllAppointmentsByExamId(exam.getKey()));
				appTableCtr.modelChanged();
				List<Protocol> protoList = ProtocolManager.getInstance()
						.findAllProtocolsByAppointment(app);
				for (Protocol p : protoList) {
					Locale userLocale = new Locale(p.getIdentity().getUser().getPreferences().getLanguage());
					Translator tmpTranslator = Util.createPackageTranslator(Exam.class, userLocale);
					BusinessControlFactory bcf = BusinessControlFactory.getInstance();

					// Email UpdateAppointment
					MailManager.getInstance().sendEmail(
						tmpTranslator.translate("ExamEditorController.UpdateAppointment.Subject", new String[] { ExamDBManager.getInstance().getExamName(exam) }),
						tmpTranslator.translate("ExamEditorController.UpdateAppointment.Body",
							new String[] {
								ExamDBManager.getInstance().getExamName(exam),
								p.getIdentity().getUser().getProperty(UserConstants.LASTNAME, ureq.getLocale()) + ", " + p.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, ureq.getLocale()),
								DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, tmpTranslator.getLocale()).format(p.getAppointment().getDate()),
								p.getAppointment().getPlace(),
								new Integer(p.getAppointment().getDuration()).toString(),
								p.getExam().getIsOral() ? tmpTranslator.translate("oral") : tmpTranslator.translate("written"),
								bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
							}),
						p.getIdentity()
					);
				}
			}
		} else if(source == archiveExamOkCancelDialog) {
            if(DialogBoxUIFactory.isOkEvent(event)) {
            	// close exam
				ExamDBManager.getInstance().close(exam);
				
				// archive the protocols of the exam
				for(Protocol protocol : ProtocolManager.getInstance().findAllProtocolsByExam(exam)) {
					Appointment appointment = protocol.getAppointment();
					
					ArchivedProtocol archivedProtocol = new ArchivedProtocol();
					archivedProtocol.setIdentifier(protocol.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null));
					archivedProtocol.setName(exam.getName());
					archivedProtocol.setDate(appointment.getDate());
					archivedProtocol.setLocation(appointment.getPlace() != null ? appointment.getPlace() : "");
					archivedProtocol.setComment(protocol.getComments() != null ? protocol.getComments() : "");
					archivedProtocol.setResult(protocol.getGrade() != null ? protocol.getGrade() : "");
					archivedProtocol.setStudyPath(protocol.getIdentity().getUser().getProperty(UserConstants.STUDYSUBJECT, null));
					
					ArchivedProtocolManager.getInstance().save(archivedProtocol);
				}
				
				// and close tab, because the editor does not make sense anymore
				OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(exam.getResourceableId(), Exam.ORES_TYPE_NAME);
				DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDTabs();
				DTab dt = dts.getDTab(ores);
				if(dt == null) return;
				dts.removeDTab(ureq, dt);
            }
		}
	}
}
