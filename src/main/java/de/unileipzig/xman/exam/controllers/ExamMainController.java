package de.unileipzig.xman.exam.controllers;

import java.util.Calendar;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.author.CatalogSettingsController;
import org.olat.repository.ui.list.RepositoryEntryDetailsController;
import org.olat.resource.OLATResourceManager;

import de.unileipzig.xman.appointment.AppointmentManager;
import de.unileipzig.xman.exam.AlreadyLockedException;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.exam.components.SelectDropdown;
import de.unileipzig.xman.protocol.ProtocolManager;
import de.unileipzig.xman.protocol.archived.ArchivedProtocolManager;

public class ExamMainController extends MainLayoutBasicController implements Activateable2, ExamController {
	
	static public enum View {
		STUDENT,	// normal oo user
		LECTURER, 	// that is able to edit: owner, examoffice or admin
		OTHER 		// guests
	}
	
	private Exam exam;
	private View view;

	private ExamController examController;
	private TooledStackedPanel toolbarStack;
	private Link editorLink;
	private Link catalogLink;
	private Link detailsLink;
	private Link archiveLink;
	private SelectDropdown examType;
	private SelectDropdown publicationStatus;
	private DialogBoxController changeToOralDialog;
	private DialogBoxController changeToWrittenDialog;
	private DialogBoxController archiveDialog;
	private Controller detailsController;
	private boolean inEditor;

	/**
	 * The Controller that manages the display and the edit of an exam
	 * 
	 * Pass one of View.STUDENT, View.LECTURER, View.OTHER to specify the view
	 * 
	 * @see View
	 * @param ureq
	 * @param wControl
	 * @param exam The exam to edit or display
	 * @param view The view that should be presented to the user
	 * @throws AlreadyLockedException 
	 */
	public ExamMainController(UserRequest ureq, WindowControl wControl, Exam exam, View view) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, getLocale()));
		this.exam = exam;
		this.view = view;
		
		toolbarStack = new TooledStackedPanel("examStackPanel", getTranslator(), this);
		init(ureq);
		putInitialPanel(toolbarStack);
	}
	
	public ExamMainController(UserRequest ureq, WindowControl wControl, Exam exam, View view, boolean launchEditor) throws AlreadyLockedException {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, getLocale()));
		this.exam = exam;
		this.view = view;
		
		toolbarStack = new TooledStackedPanel("examStackPanel", getTranslator(), this);
		init(ureq);
		putInitialPanel(toolbarStack);
		
		if(launchEditor) {
			pushEditor(ureq); // can throw AlreadyLockedException
		}
	}
	
	/**
	 * Init method so we can throw an exception from only one constructor
	 */
	private void init(UserRequest ureq) {
		toolbarStack.setShowCloseLink(true, true);
		
		// initialize exam registration dates
		if(exam.getRegStartDate() == null) {
			Calendar date = Calendar.getInstance();
			date.add(Calendar.DAY_OF_MONTH, 1);
			date.set(Calendar.HOUR_OF_DAY, 0);
			date.set(Calendar.MINUTE, 0);
			date.set(Calendar.SECOND, 0);
			exam.setRegStartDate(date.getTime());

			date.add(Calendar.MONTH, 1);
			date.set(Calendar.HOUR_OF_DAY, 23);
			date.set(Calendar.MINUTE, 59);
			date.set(Calendar.SECOND, 59);
			exam.setRegEndDate(date.getTime());
			exam.setSignOffDate(date.getTime());
		}
		
		if(view == View.STUDENT) {
			examController = new ExamStudentController(ureq, getWindowControl(), exam);
			toolbarStack.rootController(exam.getName(), examController);
		} else if(view == View.LECTURER) {
			if(exam.getIsOral()) {
				examController = new ExamLecturerOralController(ureq, getWindowControl(), exam);
			} else {
				examController = new ExamLecturerWrittenController(ureq, getWindowControl(), exam);
			}
			toolbarStack.setInvisibleCrumb(0); // Show the toolbar also on the top level
			toolbarStack.rootController(exam.getName(), examController);
			buildToolbar();
		} else if(view == View.OTHER) {
			getWindowControl().setError("Don't have access!!");
			return;
		}
	}
	
	private void buildToolbar() {
		toolbarStack.removeAllTools();

		editorLink = LinkFactory.createToolLink("editor", translate("ExamMainController.tool.editExam"), this, "o_icon_courseeditor");
		toolbarStack.addTool(editorLink);

		catalogLink = LinkFactory.createToolLink("catalog", translate("ExamMainController.tool.catalog"), this, "o_icon_catalog");
		toolbarStack.addTool(catalogLink);

		detailsLink = LinkFactory.createToolLink("details", translate("ExamMainController.tool.info"), this, "o_icon_details");
		toolbarStack.addTool(detailsLink);

		examType = new SelectDropdown("examType",
				new String[] {"written", "oral"},
				new String[] {"ExamMainController.tool.examType.written", "ExamMainController.tool.examType.oral"},
				new String[] {"o_icon_exam_written", "o_icon_exam_oral"}, getTranslator());
		if(exam.getIsOral()) {
			examType.select("oral");
		}
		examType.addListener(this);
		toolbarStack.addTool(examType, Align.left);

		publicationStatus = new SelectDropdown("publicationStatus",
				new String[] {"private", "public"},
				new String[] {"ExamMainController.tool.publicationStatus.private", "ExamMainController.tool.publicationStatus.public"},
				new String[] {"o_icon_exam_private", "o_icon_exam_public"}, getTranslator());
		publicationStatus.addListener(this);
		RepositoryEntry re = ExamDBManager.getInstance().findRepositoryEntryOfExam(exam);
		if(re.getAccess() >= RepositoryEntry.ACC_USERS) {
			publicationStatus.select("public");
		}
		toolbarStack.addTool(publicationStatus, Align.left);

		archiveLink = LinkFactory.createToolLink("archive", translate("ExamMainController.tool.archive"), this, "o_icon_exam_archive");
		toolbarStack.addTool(archiveLink, Align.right);

		if(ExamDBManager.getInstance().isClosed(exam)) {
			editorLink.setEnabled(false);
			archiveLink.setEnabled(false);
			examType.setEnabled(false);
		}
	}

	private void pushEditor(UserRequest ureq) throws AlreadyLockedException {
		if(ExamDBManager.getInstance().isClosed(exam)) {
			showInfo("ExamMainController.info.closed");
			return;
		}
		if(inEditor) {
			return;
		}
		
		OLATResourceable res = OLATResourceManager.getInstance().findResourceable(exam.getResourceableId(), exam.getResourceableTypeName());
		toolbarStack.pushController(translate("examEditor_html.header"), new ExamEditorController(ureq, getWindowControl(), res));
		inEditor = true;
	}

	private void pushDetails(UserRequest ureq) {
		RepositoryEntry re = ExamDBManager.getInstance().findRepositoryEntryOfExam(exam);
		detailsController = new RepositoryEntryDetailsController(ureq, getWindowControl(), re, true);
		listenTo(detailsController);
		toolbarStack.pushController(translate("ExamMainController.stack.infopage"), detailsController);
	}

	private void pushCatalog(UserRequest ureq) {
		RepositoryEntry re = ExamDBManager.getInstance().findRepositoryEntryOfExam(exam);
		CatalogSettingsController catalogController = new CatalogSettingsController(ureq, getWindowControl(), toolbarStack, re);
		catalogController.initToolbar();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		 if(source == toolbarStack) {
			if(event instanceof PopEvent) {
				PopEvent popEvent = (PopEvent) event;
				if(popEvent.getController() instanceof ExamEditorController) {
					inEditor = false;
				}
				// reload exam
				exam = ExamDBManager.getInstance().findExamByID(exam.getKey());
				updateExam(ureq, exam);
			} else if(event == Event.CLOSE_EVENT) {
				// close the tab we are in (without providing a previous history point)
				getWindowControl().getWindowBackOffice().getWindow().getDTabs()
					.closeDTab(ureq, ExamDBManager.getInstance().findRepositoryEntryOfExam(exam).getOlatResource(), null);
			}
		} else if (source == editorLink) {
			try {
				pushEditor(ureq);
			} catch(AlreadyLockedException e) {
				getWindowControl().setInfo(translate("ExamEditorController.alreadyLocked", new String[] { e.getName() }));
			}
		} else if(source == catalogLink) {
			pushCatalog(ureq);
		} else if(source == detailsLink) {
			pushDetails(ureq);
		} else if(source == examType) {
			if(ExamDBManager.getInstance().isClosed(exam)) {
				showInfo("ExamMainController.info.closed");
				return;
			}
			if(ProtocolManager.getInstance().findAllProtocolsByExam(exam).size() > 0) {
				showError("ExamMainController.error.studentsSubscribed");
				return;
			}

			String newType = event.getCommand();
			if(newType.equals("oral")) {
				if(exam.getIsOral()) {
					return;
				}
				changeToOralDialog = activateOkCancelDialog(ureq, translate("ExamMainController.dialog.examType.title"), translate("ExamMainController.dialog.examType.oral"), changeToOralDialog);
			} else if(newType.equals("written")) {
				if(!exam.getIsOral()) {
					return;
				}
				changeToWrittenDialog = activateOkCancelDialog(ureq, translate("ExamMainController.dialog.examType.title"), translate("ExamMainController.dialog.examType.written"), changeToWrittenDialog);
			}
		} else if(source == archiveLink) {
			if(ExamDBManager.getInstance().isClosed(exam)) {
				return;
			}

			archiveDialog = activateOkCancelDialog(ureq, translate("ExamMainController.dialog.archiveExam.title"), translate("ExamMainController.dialog.archiveExam"), archiveDialog);
		} else if(source == publicationStatus) {
			String access = event.getCommand();
			RepositoryEntry re = ExamDBManager.getInstance().findRepositoryEntryOfExam(exam);

			if("private".equals(access)) {
				RepositoryManager.getInstance().setAccess(re, RepositoryEntry.ACC_OWNERS, false);
			} else if("public".equals(access)) {
				RepositoryManager.getInstance().setAccess(re, RepositoryEntry.ACC_USERS, false);
			}

			updateExam(ureq, exam);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == changeToOralDialog) {
			if(DialogBoxUIFactory.isOkEvent(event)) {
				changeExamType(ureq, true);
			}
		} else if(source == changeToWrittenDialog) {
			if(DialogBoxUIFactory.isOkEvent(event)) {
				changeExamType(ureq, false);
			}
		} else if(source == archiveDialog) {
			if(DialogBoxUIFactory.isOkEvent(event)) {
				// close exam
				ExamDBManager.getInstance().close(exam);
				// archive the protocols of the exam
				ArchivedProtocolManager.getInstance().archiveProtocols(exam);

				updateExam(ureq, exam);
			}
		} else if(source == detailsController) {
			if(event == Event.DONE_EVENT) {
				toolbarStack.popUpToRootController(ureq);
				removeAsListenerAndDispose(detailsController);
				detailsController = null;
			}
		}
	}

	@Override
	public void updateExam(UserRequest ureq, Exam newExam) {
		if(exam.getIsOral() != newExam.getIsOral() || exam.getName() != newExam.getName()) {
			exam = newExam;

			// total rebuild necessary (controllers are disposed during stack unwind)
			init(ureq);
		} else {
			exam = newExam;
			if(examController != null) {
				examController.updateExam(ureq, exam);
			}
			if(view == View.LECTURER) {
				buildToolbar();
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && entries.size() > 0) {
			String action = entries.get(0).getOLATResourceable().getResourceableTypeName();

			if("Editor".equals(action)) {
				try {
					pushEditor(ureq);
				} catch(AlreadyLockedException e) {
					getWindowControl().setInfo(translate("ExamEditorController.alreadyLocked", new String[] { e.getName() }));
				}
			} else if("Catalog".equals(action)) {
				pushCatalog(ureq);
			} else if("Infos".equals(action)) {
				pushDetails(ureq);
			}
		}
	}

	private void changeExamType(UserRequest ureq, boolean oral) {
		if(ProtocolManager.getInstance().findAllProtocolsByExam(exam).size() > 0) {
			showError("ExamMainController.error.studentsSubscribed");
			return;
		}

		AppointmentManager.getInstance().deleteAllAppointmentsByExam(exam);
		Exam newExam = ExamDBManager.getInstance().findExamByID(exam.getKey());
		newExam.setIsOral(oral);
		if(!oral) {
			newExam.setIsMultiSubscription(false);
		}
		updateExam(ureq, newExam);
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(changeToOralDialog);
		removeAsListenerAndDispose(changeToWrittenDialog);
		removeAsListenerAndDispose(archiveDialog);
		removeAsListenerAndDispose(detailsController);
		if(inEditor) {
			toolbarStack.popContent(); // disposes the editor controller and thus releases the lock
		}
	}
}
