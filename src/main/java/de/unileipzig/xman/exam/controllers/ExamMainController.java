package de.unileipzig.xman.exam.controllers;

import java.util.Calendar;

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResourceManager;

import de.unileipzig.xman.exam.AlreadyLockedException;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;

public class ExamMainController extends MainLayoutBasicController {
	
	static public enum View {
		STUDENT,	// normal oo user
		LECTURER, 	// that is able to edit: owner, examoffice or admin
		OTHER 		// guests
	}
	
	static private String TOOL_EDIT_EXAM = "edit";
	static private String TOOL_OPEN_COURSECONFIG = "courseconfig";
	
	private Exam exam;
	private View view;
	private TooledStackedPanel toolbarStack;
	private ToolController toolController;
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
		
		init(ureq);
	}
	
	public ExamMainController(UserRequest ureq, WindowControl wControl, Exam exam, View view, boolean launchEditor) throws AlreadyLockedException {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, getLocale()));
		this.exam = exam;
		this.view = view;
		
		init(ureq);
		
		if(launchEditor) {
			pushEditor(ureq); // can throw AlreadyLockedException
		}
	}
	
	/**
	 * Init method so we can throw an exception from only one constructor
	 */
	private void init(UserRequest ureq) {
		toolbarStack = new TooledStackedPanel("examStackPanel", getTranslator(), this);
		putInitialPanel(toolbarStack);
		
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
		
		String name = exam.getName() + " (" + (exam.getIsOral() ? translate("oral") : translate("written")) + ")";
		if(view == View.STUDENT) {
			Controller examController = new ExamStudentController(ureq, getWindowControl(), exam);
			toolbarStack.pushController(name, new LayoutMain3ColsController(ureq, getWindowControl(), null, null, examController.getInitialComponent(), "examMain"));
		} else if(view == View.LECTURER) {
			Controller examController;
			if(exam.getIsOral()) {
				examController = new ExamLecturerOralController(ureq, getWindowControl(), toolbarStack, exam);
			} else {
				examController = new ExamLecturerWrittenController(ureq, getWindowControl(), toolbarStack, exam);
			}
			buildToolController();
			toolbarStack.pushController(name, new LayoutMain3ColsController(ureq, getWindowControl(), null,
														toolController.getInitialComponent(), examController.getInitialComponent(), "examMain"));
		} else if(view == View.OTHER) {
			getWindowControl().setError("Don't have access!!");
			return;
		}
	}
	
	private void buildToolController() {
		if(toolController != null) {
			removeAsListenerAndDispose(toolController);
			toolController = null;
		}
		
		toolController = ToolFactory.createToolController(getWindowControl());
		listenTo(toolController);
		
		toolController.addHeader(translate("ExamMainController.tool.header"));
		toolController.addLink(TOOL_EDIT_EXAM, translate("ExamMainController.tool.editExam"));

		toolController.addHeader(translate("ExamMainController.tool.header.general"));
		toolController.addLink(TOOL_OPEN_COURSECONFIG, translate("ExamMainController.tool.courseconfig"));
	}
	
	private void pushEditor(UserRequest ureq) throws AlreadyLockedException {
		if(ExamDBManager.getInstance().isClosed(exam)) {
			showInfo("ExamMainController.info.closed");
			return;
		}
		
		OLATResourceable res = OLATResourceManager.getInstance().findResourceable(exam.getResourceableId(), exam.getResourceableTypeName());
		toolbarStack.pushController(translate("examEditor_html.header"), new ExamEditorController(ureq, getWindowControl(), res));
		inEditor = true;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		 if(source == toolbarStack) {
			if(event instanceof PopEvent) {
				PopEvent popEvent = (PopEvent) event;
				if(popEvent.getController() instanceof ExamEditorController) {
					inEditor = false;
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == toolController) {
			if(event.getCommand().equals(TOOL_EDIT_EXAM)) {
				try {
					pushEditor(ureq);
				} catch(AlreadyLockedException e) {
					getWindowControl().setInfo(translate("ExamEditorController.alreadyLocked", new String[] { e.getName() }));
				}
			} else if(event.getCommand().equals(TOOL_OPEN_COURSECONFIG)) {
				OLATResourceable ores = OLATResourceManager.getInstance().findResourceable(exam.getResourceableId(), Exam.ORES_TYPE_NAME);
				Long reKey = RepositoryManager.getInstance().lookupRepositoryEntryKey(ores, true);
				
				String businessPath = "[RepositorySite:0][RepositoryEntry:" + reKey + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			}
		}
	}

	@Override
	protected void doDispose() {
		if(inEditor) {
			toolbarStack.popContent(); // disposes the editor controller and thus releases the lock
		}
	}
}
