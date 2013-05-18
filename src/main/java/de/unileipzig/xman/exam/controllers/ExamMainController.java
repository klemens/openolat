package de.unileipzig.xman.exam.controllers;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerImpl;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResourceManager;

import de.unileipzig.xman.exam.Exam;

public class ExamMainController extends MainLayoutBasicController {
	
	static public enum View {
		STUDENT,	// normal oo user
		LECTURER, 	// that is able to edit: owner, examoffice or admin
		OTHER 		// guests
	}
	
	static private String TOOL_EDIT_EXAM = "edit";
	
	private Exam exam;
	private View view;
	private StackedController cstack;
	private ToolController toolController;

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
	 */
	public ExamMainController(UserRequest ureq, WindowControl wControl, Exam exam, View view) {
		this(ureq, wControl, exam, view, false);
	}
	
	public ExamMainController(UserRequest ureq, WindowControl wControl, Exam exam, View view, boolean launchEditor) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(Exam.class, getLocale()));
		
		this.exam = exam;
		this.view = view;
		this.cstack = new StackedControllerImpl(getWindowControl(), getTranslator(), "examStack");
		
		VelocityContainer mainVC = new VelocityContainer("examMain", Util.getPackageVelocityRoot(Exam.class) + "/examMain.html", getTranslator(), this);
		mainVC.put("stackedController", cstack.getInitialComponent());
		putInitialPanel(cstack.getInitialComponent());
		
		// TODO Split LaunchController into StudentController and LecturerController
		if(view == View.STUDENT) {
			cstack.pushController(exam.getName(), new ExamLaunchController(ureq, wControl, exam, false, true));
		} else if(view == View.LECTURER) {
			Controller examController = new ExamLaunchController(ureq, wControl, exam, true, false);
			buildToolController();
			cstack.pushController(exam.getName(), new LayoutMain3ColsController(ureq, getWindowControl(), null,
														toolController.getInitialComponent(), examController.getInitialComponent(), "examMain"));
		} else if(view == View.OTHER) {
			getWindowControl().setError("Don't have access!!");
			return;
		}
		
		if(launchEditor) {
			pushEditor(ureq);
		}
	}
	
	private void buildToolController() {
		if(toolController != null) {
			removeAsListenerAndDispose(toolController);
			toolController = null;
		}
		
		toolController = ToolFactory.createToolController(getWindowControl());
		listenTo(toolController);
		
		toolController.addHeader("Prüfung");
		toolController.addLink(TOOL_EDIT_EXAM, "bearbeiten");
	}
	
	private void pushEditor(UserRequest ureq) {
		OLATResourceable res = OLATResourceManager.getInstance().findResourceable(exam.getResourceableId(), exam.getResourceableTypeName());
		cstack.pushController("Prüfungseditor", new ExamEditorController(ureq, getWindowControl(), res));
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == toolController) {
			if(event.getCommand().equals(TOOL_EDIT_EXAM)) {
				pushEditor(ureq);
			}
		}
	}

	@Override
	protected void doDispose() {
	}
}
