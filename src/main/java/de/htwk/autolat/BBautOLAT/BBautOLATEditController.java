package de.htwk.autolat.BBautOLAT;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * the basic controller to edit an autOLAT node
 * 
 * <P>
 * @author werjo
 *
 */
public class BBautOLATEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
	
	public static final String PANE_KEY_TASKCONFIG = "label.tabpane.taskedit";
	public static final String PANE_KEY_SERVERCONFIG = "label.tabpane.serveredit";
	
	private static final String[] PANE_KEYS = {PANE_KEY_TASKCONFIG, PANE_KEY_SERVERCONFIG};
	
	private TabbedPane tabPane;
	private long courseNodeID;
	private ModuleConfiguration moduleConfiguration;
	
	private EditTaskController editTaskController;
	private EditConnectionController editConnectionController;
	private EditAssessmentPeriodController assessmentPeriodController;
	private EditModuleController moduleController;
	
	/**
	 * 
	 * @param config
	 * @param ureq
	 * @param wControl
	 * @param autOLATCourseNode
	 * @param course
	 * @param euce
	 */
	public BBautOLATEditController(ModuleConfiguration config, UserRequest ureq, WindowControl wControl, BBautOLATCourseNode autOLATCourseNode,
			ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		
		moduleConfiguration = config;
		courseNodeID = Long.valueOf(autOLATCourseNode.getIdent());
		createOutput(ureq, wControl, autOLATCourseNode, course);
	}
	/**
	 * initialize the form elements
	 * 
	 * @param ureq
	 * @param wControl
	 * @param courseNode
	 * @param course
	 */
	private void createOutput(UserRequest ureq, WindowControl wControl, BBautOLATCourseNode courseNode, ICourse course) {
		editTaskController = new EditTaskController(ureq, wControl, courseNodeID, courseNode, course);
		editTaskController.addControllerListener(this);
		
		assessmentPeriodController = new EditAssessmentPeriodController(ureq, wControl, courseNodeID, courseNode, course);
		assessmentPeriodController.addControllerListener(this);
		
		moduleController = new EditModuleController(ureq, wControl, courseNodeID, courseNode, course);
		moduleController.addControllerListener(this);
		
		editConnectionController = new EditConnectionController(ureq, wControl, course.getResourceableId(), courseNodeID);
		editConnectionController.addControllerListener(this);
	}

	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub

	}

	@Override
	protected void event(UserRequest ureq, Component comp, Event evnt) {
	// TODO Auto-generated method stub

	}

	@Override
	protected void event(UserRequest ureq, Controller ctr, Event evnt) {
		
		if(ctr == editConnectionController) {			
			moduleConfiguration.setBooleanEntry("ServerConnectionSet", true);
			editTaskController.createOutput(ureq);
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);			
		}

		if(ctr == editTaskController) {
			if(evnt.getCommand()=="CONNEDIT_OK") {
				moduleConfiguration.setBooleanEntry("ServerConnectionSet", true);
				editTaskController.createOutput(ureq);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
			if(evnt.getCommand()=="EDITTYPE_OK") {
				moduleConfiguration.setBooleanEntry("TaskTypeValid", true);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
			if(evnt.getCommand().equals("IMPORT_DONE")) {
				assessmentPeriodController.updateForm();
				//System.out.println("call updateForm");
			}
			//if(evnt.getCommand()=="EDITPROPERTIES_OK") {
			//	moduleConfiguration.setBooleanEntry("GradingTimeSet", true);
			//	fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			//}
		}
		if(ctr == assessmentPeriodController) {
			if(evnt.getCommand().equals("EDITPROPERTIES_OK")) {
				moduleConfiguration.setBooleanEntry("GradingTimeSet", true);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}

	}
	
	@Override
	public void addTabs(TabbedPane tabPane) {
		this.tabPane = tabPane;		
		tabPane.addTab(translate(PANE_KEY_TASKCONFIG), editTaskController.getInitialComponent());
		tabPane.addTab(translate("label.tabpane.assess"), assessmentPeriodController.getInitialComponent());
		tabPane.addTab(translate("label.tabpane.module"), moduleController.getInitialComponent());
		tabPane.addTab(translate(PANE_KEY_SERVERCONFIG), editConnectionController.getInitialComponent());		
	}

	@Override
	public String[] getPaneKeys() {
		return PANE_KEYS;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return this.tabPane;
	}

}
