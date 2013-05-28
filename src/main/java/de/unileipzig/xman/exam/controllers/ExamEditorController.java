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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
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
import de.unileipzig.xman.exam.forms.CreateAndEditAppointmentForm;
import de.unileipzig.xman.exam.forms.EditCommentsForm;
import de.unileipzig.xman.exam.forms.EditEarmarkedForm;
import de.unileipzig.xman.exam.forms.EditRegistrationForm;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;

/**
 * 
 * @author
 */
public class ExamEditorController extends DefaultController implements
		GenericEventListener {

	private static final String PACKAGE = Util.getPackageName(Exam.class);
	private static final String VELOCITY_ROOT = Util
			.getPackageVelocityRoot(Exam.class);

	private static final String CMD_TOOLS_OPEN_EXAM = "toolCtr.openExam";
	private static final String CMD_TOOLS_CLOSE_EDITOR = "toolCtr.close";
	private static final String EXAM_EDITOR_LOCK = "examEditor.lock";
	private static final String CMD_TOOLS_CLOSE_EXAM_YES_NO = "toolCtr.closeExamYesNo";

	private Panel mainPanel;
	private VelocityContainer vcMain, vcApp;
	private Translator translator;
	private LayoutMain3ColsController layoutCtr;
	private ToolController toolCtr;
	private MenuTree menuTree;

	private LockResult lockResult;
	private Exam exam;
	private Appointment app;
	private TabbedPane tabbedPane;
	private EditCommentsForm editCommentsForm;
	private EditRegistrationForm editRegForm;
	private EditEarmarkedForm editEarmarkedForm;
	private CreateAndEditAppointmentForm createAppForm, editAppForm;
	private TableController appTableCtr;
	private AppointmentTableModel appTableMdl;
	private Link addAppLink;
	private CloseableModalController cmc;
	private RepositoryEntry repoEntry;

	/**
	 * creates the controller for the exam editor
	 * 
	 * @param ureq
	 *            the UserRequest
	 * @param wControl
	 *            the window control
	 * @param res
	 *            the olat resourceable for the exam
	 */
	public ExamEditorController(UserRequest ureq, WindowControl wControl,
			OLATResourceable res) {
		super(wControl);

		this.exam = ExamDBManager.getInstance().findExamByID(
				res.getResourceableId());
		repoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(res,
				true);

		translator = new PackageTranslator(PACKAGE, ureq.getLocale());

		// try to acquire edit lock for this course.
		// --------------------------------getInstance hinzugefügt
		lockResult = CoordinatorManager.getInstance().getCoordinator()
				.getLocker().acquireLock(res, ureq.getIdentity(),
						EXAM_EDITOR_LOCK);

		if (lockResult.isSuccess()) {

			vcMain = new VelocityContainer("examEditor", VELOCITY_ROOT
					+ "/examEditor.html", translator, this);

			this.createTabbedPane(ureq);

			toolCtr = ToolFactory.createToolController(wControl);
			toolCtr.addControllerListener(this);
			toolCtr.addHeader(translator
					.translate("ExamEditorController.toolCtr.header"));
			toolCtr.addLink(CMD_TOOLS_OPEN_EXAM, translator
					.translate("ExamEditorController.toolCtr.openExam"));
			toolCtr.addLink(CMD_TOOLS_CLOSE_EDITOR, translator
					.translate("ExamEditorController.toolCtr.close"), null,
					"o_tb_close");

			menuTree = new MenuTree("examEditorMenu");
			menuTree.setTreeModel(this.buildTreeModel(repoEntry
					.getDisplayname()));

			mainPanel = new Panel("examEditorPanel");
			mainPanel.setContent(vcMain);

			layoutCtr = new LayoutMain3ColsController(ureq, wControl, menuTree,
					toolCtr.getInitialComponent(), mainPanel,
					"examEditorCtrLayoutKey");

			this.setInitialComponent(layoutCtr.getInitialComponent());
			// --------------------------------getInstance hinzugefügt
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
					.registerFor(this, ureq.getIdentity(), res);
		} else {

			this.getWindowControl().setInfo(
					translator.translate("ExamEditorController.alreadyLocked",
							new String[] { lockResult.getOwner().getName() }));
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
				"editCommentsForm", translator, exam.getComments());
		editCommentsForm.addControllerListener(this);
		tabbedPane.addTab(translator
				.translate("ExamEditorController.tabbedPane.comments"),
				editCommentsForm.getInitialComponent());

		editRegForm = new EditRegistrationForm(ureq, this.getWindowControl(),
				"editRegistrationForm", translator, exam.getRegStartDate(),
				exam.getRegEndDate(), exam.getSignOffDate());
		editRegForm.addControllerListener(this);
		tabbedPane.addTab(translator
				.translate("ExamEditorController.tabbedPane.registration"),
				editRegForm.getInitialComponent());

		if (exam.getRegEndDate() != null) {
			vcApp = new VelocityContainer("appPage", VELOCITY_ROOT
					+ "/tabApp.html", translator, this);
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
			tgc.setTableEmptyMessage(translator
					.translate("ExamEditorController.appointmentTable.empty"));
			appTableCtr = new TableController(tgc, ureq, this
					.getWindowControl(), translator);
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
			tabbedPane.addTab(translator
					.translate("ExamEditorController.tabbedPane.appointments"),
					vcApp);
		}

		editEarmarkedForm = new EditEarmarkedForm(ureq,
				this.getWindowControl(), "editEarmarkedForm", translator, exam
						.getEarmarkedEnabled());
		editEarmarkedForm.addControllerListener(this);
		tabbedPane.addTab(translator
				.translate("ExamEditorController.tabbedPane.earmarked"),
				editEarmarkedForm.getInitialComponent());

		ExamCatalogController ecc = new ExamCatalogController(ureq, this
				.getWindowControl(), exam);
		tabbedPane.addTab(
				translator.translate("ExamCatalogController.catalog"), ecc
						.getInitialComponent());
	}

	/**
	 * builds the treemodel
	 * 
	 * @param examName
	 * @return the generic tree model
	 */
	private GenericTreeModel buildTreeModel(String examName) {

		GenericTreeModel tm = new GenericTreeModel();
		GenericTreeNode gtn = new GenericTreeNode();
		gtn.setTitle(examName);
		gtn.setUserObject("menuRoot");
		gtn.setAltText(examName);
		gtn.setAccessible(false);
		tm.setRootNode(gtn);

		GenericTreeNode gtn1 = new GenericTreeNode();
		gtn1.setTitle(translator.translate(exam.getIsOral() ? "oral"
				: "written"));
		gtn1.setUserObject("menuKind");
		gtn1.setAccessible(false);
		gtn.addChild(gtn1);

		return tm;
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
		if (source == menuTree) {
			// nothing to do here
		} else if (source == addAppLink) {
			VelocityContainer vcEditApp = new VelocityContainer(
					"createAppPage", VELOCITY_ROOT + "/createApp.html",
					translator, this);
			createAppForm = new CreateAndEditAppointmentForm(ureq, this
					.getWindowControl(), "createAppForm", translator, exam
					.getIsOral(), null);
			createAppForm.addControllerListener(this);
			vcEditApp.put("createAppForm", createAppForm.getInitialComponent());
			cmc = new CloseableModalController(this.getWindowControl(),
					translator.translate("close"), vcEditApp);
			cmc.activate();
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == toolCtr) {
			// close editor
			if (event.getCommand().equals(CMD_TOOLS_CLOSE_EDITOR)) {
				// --------------------------------getInstance hinzugefügt
				if (lockResult.isSuccess())
					CoordinatorManager.getInstance().getCoordinator()
							.getLocker().releaseLock(lockResult);
				this.fireEvent(ureq, Event.DONE_EVENT);
			}
			// show preview
			if (event.getCommand().equals(CMD_TOOLS_OPEN_EXAM)) {
				OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(exam.getResourceableId(), Exam.ORES_TYPE_NAME);
				DTabs dts = (DTabs) Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");

				// Deleting the current tab and creating a new one with the same resource is a dirty hack
				// TODO implement using a StackedController
				DTab dt = dts.getDTab(ores);
				if(dt == null) return;
				dts.removeDTab(ureq, dt);

				DTab dtNew = dts.createDTab(ores, exam.getName());

				ExamLaunchController examLaunchCtr = (ExamLaunchController) RepositoryHandlerFactory.getInstance().getRepositoryHandler(Exam.ORES_TYPE_NAME)
																			.createLaunchController(ores, ureq, this.getWindowControl());
				dtNew.setController(examLaunchCtr);

				dts.addDTab(ureq, dtNew);
				dts.activate(ureq, dtNew, null);
			}
		} else if (source == appTableCtr) {
			if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals("appTable.edit")) {
					List<Appointment> appList = (ArrayList<Appointment>) appTableMdl
							.getObjects(tmse.getSelection());
					if (appList.size() == 1) {
						app = appList.get(0);
						editAppForm = new CreateAndEditAppointmentForm(ureq,
								this.getWindowControl(), "editAppForm",
								translator, exam.getIsOral(), app);
						editAppForm.addControllerListener(this);
						cmc = new CloseableModalController(this
								.getWindowControl(), translator
								.translate("close"), editAppForm
								.getInitialComponent());
						cmc.activate();
					} else {
						this
								.getWindowControl()
								.setInfo(
										translator
												.translate("ExamEditorController.appointmentTable.noSingleChoose"));
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
							tempApp = p.getAppointment();
							CalendarManager.getInstance().deleteKalendarEventForExam(exam,p.getIdentity());
							Locale userLocale = new Locale(p.getIdentity().getUser().getPreferences().getLanguage());
							Translator tmpTranslator = new PackageTranslator(PACKAGE, userLocale);
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
						AppointmentManager.getInstance().deleteAppointment(
								tempApp);
					}

					appTableMdl.setEntries(AppointmentManager.getInstance()
							.findAllAppointmentsByExamId(exam.getKey()));
					appTableCtr.modelChanged();
				}
			}
		} else if (source == editCommentsForm) {

			if (event == Form.EVNT_VALIDATION_OK) {

				exam.setComments(editCommentsForm.getComments());
				ExamDBManager.getInstance().updateExam(exam);
			}
		} else if (source == editRegForm) {

			if (event == Form.EVNT_VALIDATION_OK) {

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
				exam.setEarmarkedEnabled(editEarmarkedForm.getEarmarked());
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
					Translator tmpTranslator = new PackageTranslator(PACKAGE, userLocale);
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
		}
	}

	public void event(Event event) {

		// nothing to catch
	}
}