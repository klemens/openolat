package de.unileipzig.xman.nodes;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

import de.unileipzig.xman.exam.ExamHandler;

public class ExamCourseNodeRunController extends DefaultController {

	private Panel main;
		
	private CourseEnvironment courseEnv;
	private Controller examLaunchCtr;
	
	private ExamHandler exha = new ExamHandler();
	
	private ModuleConfiguration config;
	
	
	/**
	 * 
	 * @param wControl
	 * @param ureq
	 * @param wikiCourseNode
	 * @param cenv
	 */
	public ExamCourseNodeRunController(WindowControl wControl, UserRequest ureq, ExamCourseNode examCourseNode, CourseEnvironment cenv, NodeEvaluation ne) {
		super(wControl);
		this.courseEnv = cenv;
		this.config = examCourseNode.getModuleConfiguration();
		
		main = new Panel("examrunmain");
		
	//  wird anscheinend nicht mehr benötigt
	//	setUserActivityLogger(courseEnv.getAuditManager().getUserActivityLogger(examCourseNode, false, true));

		RepositoryEntry re = ExamCourseNodeEditController.getExamRepoReference(config, true);
		
		examLaunchCtr = exha.createLaunchController(re, ureq, wControl);
		
		
		main.setContent(examLaunchCtr.getInitialComponent());
		setInitialComponent(main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//no events yet
	}
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if(examLaunchCtr != null){
			examLaunchCtr.dispose();
			examLaunchCtr = null;
		}
	}

}
