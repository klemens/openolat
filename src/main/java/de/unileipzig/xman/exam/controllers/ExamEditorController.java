package de.unileipzig.xman.exam.controllers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
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
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
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
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

import de.unileipzig.xman.admin.mail.MailManager;
import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.appointment.AppointmentManager;
import de.unileipzig.xman.appointment.tables.AppointmentTableModel;
import de.unileipzig.xman.calendar.CalendarManager;
import de.unileipzig.xman.comment.CommentManager;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.exam.ExamHandler;
import de.unileipzig.xman.exam.AlreadyLockedException;
import de.unileipzig.xman.exam.forms.CreateAndEditAppointmentForm;
import de.unileipzig.xman.exam.forms.EditDescriptionForm;
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
	private static final String ACTION_DELETE_AUTHOR = "examEditor.deleteAuthor";

	private Panel mainPanel;
	private VelocityContainer vcMain, vcApp;
	private LayoutMain3ColsController layoutCtr;
	private Controller userSearchController;

	private LockResult lockResult;
	private Exam exam;
	private Appointment app;
	private TabbedPane tabbedPane;
	private EditDescriptionForm editDescriptionForm;
	private EditRegistrationForm editRegForm;
	private CreateAndEditAppointmentForm createAppForm, editAppForm;
	private TableController appTableCtr;
	private TableController authorTableCtr;
	private AppointmentTableModel appTableMdl;
	private Link addAppLink, addAuthorLink;
	private CloseableModalController cmc;
	private RepositoryEntry repoEntry;
	
	@Autowired
	private RepositoryService repositoryService;

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

		editDescriptionForm = new EditDescriptionForm(ureq, this.getWindowControl(), exam.getName(), exam.getComments());
		editDescriptionForm.addControllerListener(this);
		tabbedPane.addTab(translate("ExamEditorController.tabbedPane.comments"),
				editDescriptionForm.getInitialComponent());

		editRegForm = new EditRegistrationForm(ureq, this.getWindowControl(), exam);
		editRegForm.addControllerListener(this);
		tabbedPane.addTab(translate("ExamEditorController.tabbedPane.registration"),
				editRegForm.getInitialComponent());

		vcApp = new VelocityContainer("appPage", VELOCITY_ROOT + "/tabApp.html", getTranslator(), this);
		addAppLink = LinkFactory.createButtonSmall("ExamEditorController.link.addAppointment", vcApp, this);
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
		tgc.setDownloadOffered(true);
		tgc.setTableEmptyMessage(translate("ExamEditorController.appointmentTable.empty"));
		appTableCtr = new TableController(tgc, ureq, getWindowControl(), getTranslator());
		appTableCtr.setMultiSelect(true);
		appTableCtr.addMultiSelectAction("ExamEditorController.appointmentTable.edit", "appTable.edit");
		if (exam.getIsOral())
			appTableCtr.addMultiSelectAction("ExamEditorController.appointmentTable.del", "appTable.del");
		List<Appointment> appList = AppointmentManager.getInstance().findAllAppointmentsByExamId(exam.getKey());
		appTableMdl = new AppointmentTableModel(getTranslator(), appList, exam.getIsOral());
		appTableMdl.setTable(appTableCtr);
		appTableCtr.setTableDataModel(appTableMdl);
		appTableCtr.setSortColumn(0, true);
		appTableCtr.addControllerListener(this);

		vcApp.put("appointmentTable", appTableCtr.getInitialComponent());
		tabbedPane.addTab(translate("ExamEditorController.tabbedPane.appointments"), vcApp);


		TableGuiConfiguration authorTableConfig = new TableGuiConfiguration();
		authorTableConfig.setDisplayTableHeader(false);
		authorTableConfig.setDisplayRowCount(false);
		authorTableConfig.setPageingEnabled(false);
		authorTableConfig.setDownloadOffered(false);
		authorTableConfig.setSortingEnabled(false);

		removeAsListenerAndDispose(authorTableCtr);
		authorTableCtr = new TableController(authorTableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(authorTableCtr);

		List<Identity> authors = repositoryService.getMembers(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam), GroupRoles.owner.name());
		authorTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ExamEditorController.authorTable.name", 0, null, ureq.getLocale()));
		authorTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ExamEditorController.authorTable.action", 1, ACTION_DELETE_AUTHOR, ureq.getLocale(), DefaultColumnDescriptor.ALIGNMENT_RIGHT));
		authorTableCtr.setTableDataModel(new DefaultTableDataModel<Identity>(authors) {
			@Override
			public int getColumnCount() { return 1; }
			@Override
			public Object getValueAt(int row, int col) {
				Identity id = getObject(row);
				if(col == 0) {
					User user = id.getUser();
					return user.getFirstName() + " " + user.getLastName();
				} else {
					if(id.equals(ureq.getIdentity())) {
						// Authors should not remove themselves
						return "";
					} else {
						return translate("ExamEditorController.authorTable.actionDelete");
					}
				}
			}
		});

		VelocityContainer vcAuthors = new VelocityContainer("authorTable", Exam.class, "tabAuthors", getTranslator(), this);
		vcAuthors.put("ExamEditorController.authorTable", authorTableCtr.getInitialComponent());
		addAuthorLink = LinkFactory.createButton("ExamEditorController.link.addAuthor", vcAuthors, this);
		tabbedPane.addTab(translate("ExamEditorController.tabbedPane.authors"), vcAuthors);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		removeAsListenerAndDispose(authorTableCtr);
		removeAsListenerAndDispose(userSearchController);
		if(lockResult != null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockResult);
		}
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
		} else if(source == addAuthorLink) {
			removeAsListenerAndDispose(userSearchController);
			userSearchController = new UserSearchController(ureq, getWindowControl(), false, false, false);
			listenTo(userSearchController);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchController.getInitialComponent());
			cmc.activate();
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
										getName(p.getIdentity()),
										DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, tmpTranslator.getLocale()).format(tempApp.getDate()),
										tempApp.getPlace(),
										new Integer(tempApp.getDuration()).toString(),
										p.getExam().getIsOral() ? tmpTranslator.translate("oral") : tmpTranslator.translate("written"),
										bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
									}),
								p.getIdentity()
							);
							
							ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(p.getIdentity());

							// add a comment to the esf
							String commentText = translate("ExamEditorController.appointmentRemoved", new String[] { exam.getName(), getName(ureq.getIdentity()) });
							CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());

							ProtocolManager.getInstance().deleteProtocol(p);
						}
						tempApp = AppointmentManager.getInstance().findAppointmentByID(tempApp.getKey());
						AppointmentManager.getInstance().deleteAppointment(tempApp);
					}

					appTableMdl.setObjects(AppointmentManager.getInstance()
							.findAllAppointmentsByExamId(exam.getKey()));
					appTableCtr.modelChanged();
				}
			}
		} else if (source == editDescriptionForm) {
			if (event == Form.EVNT_VALIDATION_OK) {
				if(!editDescriptionForm.getName().equals(exam.getName())) {
					showInfo("ExamEditorController.nameChange");
				}

				exam = ExamDBManager.getInstance().findExamByID(exam.getKey());
				exam.setComments(editDescriptionForm.getDescription());
				exam.setName(editDescriptionForm.getName());

				RepositoryEntry re = ExamDBManager.getInstance().findRepositoryEntryOfExam(exam);
				re.setDisplayname(exam.getName());
				re.setDescription(exam.getComments());

				DBFactory.getInstance().commitAndCloseSession();
			}
		} else if (source == editRegForm) {

			if (event == Form.EVNT_VALIDATION_OK) {

				exam = ExamDBManager.getInstance().findExamByID(exam.getKey());
				exam.setRegStartDate(editRegForm.getRegStart());
				exam.setRegEndDate(editRegForm.getRegEnd());
				exam.setSignOffDate(editRegForm.getSignOff());

				exam.setEarmarkedEnabled(editRegForm.getEarmarked());
				exam.setIsMultiSubscription(editRegForm.getMultiSubscription());

				this.createTabbedPane(ureq);
				tabbedPane.setSelectedPane(1);
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
				appTableMdl.setObjects(AppointmentManager.getInstance()
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
				appTableMdl.setObjects(AppointmentManager.getInstance()
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
								getName(p.getIdentity()),
								DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, tmpTranslator.getLocale()).format(p.getAppointment().getDate()),
								p.getAppointment().getPlace(),
								new Integer(p.getAppointment().getDuration()).toString(),
								p.getExam().getIsOral() ? tmpTranslator.translate("oral") : tmpTranslator.translate("written"),
								bcf.getAsURIString(bcf.createCEListFromString(ExamDBManager.getInstance().findRepositoryEntryOfExam(exam)), true)
							}),
						p.getIdentity()
					);

					ElectronicStudentFile esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(p.getIdentity());
					// add a comment to the esf
					String commentText = translate("ExamEditorController.appointmentChanged", new String[] { exam.getName(), getName(ureq.getIdentity()) });
					CommentManager.getInstance().createCommentInEsf(esf, commentText, ureq.getIdentity());
				}
			}
		} else if(source == authorTableCtr) {
			TableEvent tableEvent = (TableEvent) event;
			if(tableEvent.getActionId().equals(ACTION_DELETE_AUTHOR)) {
				Identity author = (Identity) authorTableCtr.getSortedObjectAt(tableEvent.getRowId());

				RepositoryEntry re = ExamDBManager.getInstance().findRepositoryEntryOfExam(exam);
				if(repositoryService.hasRole(author, re, GroupRoles.owner.name())) {
					repositoryService.removeRole(author, re, GroupRoles.owner.name());
				}

				int authorIndex = tabbedPane.getSelectedPane();
				createTabbedPane(ureq);
				tabbedPane.setSelectedPane(authorIndex);
			}
		} else if(source == userSearchController) {
			if(event instanceof SingleIdentityChosenEvent) {
				cmc.deactivate();
				Identity newAuthor = ((SingleIdentityChosenEvent) event).getChosenIdentity();

				RepositoryEntry re = ExamDBManager.getInstance().findRepositoryEntryOfExam(exam);
				if(!repositoryService.hasRole(newAuthor, re, GroupRoles.owner.name())) {
					repositoryService.addRole(newAuthor, re, GroupRoles.owner.name());
				}

				int authorIndex = tabbedPane.getSelectedPane();
				createTabbedPane(ureq);
				tabbedPane.setSelectedPane(authorIndex);
			}
		}
	}

	protected String getName(Identity id) {
		return id.getUser().getProperty(UserConstants.FIRSTNAME, null) + " " + id.getUser().getProperty(UserConstants.LASTNAME, null);
	}
}
