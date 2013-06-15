package de.htwk.autolat.BBautOLAT;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.NodeEvaluation;

import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.Student.StudentManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskModule.TaskModule;
import de.htwk.autolat.TaskModule.TaskModuleTableModel;
import de.htwk.autolat.TaskSolution.TaskSolution;

public class TaskOverviewController extends BasicController
{
	private static final String PACKAGE = Util.getPackageName(TaskOverviewController.class);
	private static final String KEY_VIEW_TASK = "key.view.task";
	private static final String KEY_VIEW_SOLUTION = "key.view.solution";
	private static final String KEY_VIEW_TEST = "key.view.test";
	public static final String KEY_TEST_TASK = "key.test.task"; 
	private static final String KEY_VIEW_PASSED = "key.view.passed";
	private static final String KEY_VIEW_SCORE = "key.view.score";
	
	
	private WindowControl wControl;

	private Panel overviewPanel;
	
	private TableController tableCntr;
	private TaskOverviewTableModel taskOverviewTableModel;
	
	private CMCTaskViewController CMCTaskView;
	//private CMCBestSolutionViewController CMCBestSolView;
	
	private CMCEditPassedController CMCEditPassedView;
	private CMCEditScoreController CMCEditScoreView;
	private long courseNodeID;
	private CloseableModalController CMCDialog;
	
	private VelocityContainer overviewVC;
	private List<Identity> coachedStudents;
	private Long courseID;
	
	
	

	
	public TaskOverviewController(UserRequest ureq, WindowControl wControl, long courseID, CourseEnvironment courseEnv, NodeEvaluation ne, List<Identity> coachedStudents)
	{
		super(ureq, wControl);
		this.wControl = wControl;
		this.coachedStudents = coachedStudents;
		this.courseNodeID = courseID;
		this.courseID = courseEnv.getCourseResourceableId();
		
		PackageTranslator translator = new PackageTranslator(PACKAGE, this.getLocale());
		setTranslator(translator);
		
		overviewPanel = new Panel("OverviewPanel");
		putInitialPanel(overviewPanel);
		
		overviewVC = this.createVelocityContainer("taskOverviewController");
		initTable(ureq, courseEnv, ne, courseID);
		
		overviewPanel.setContent(overviewVC);		
	}

	private void initTable(UserRequest ureq, CourseEnvironment courseEnv, NodeEvaluation ne, long courseID) {
		TableGuiConfiguration tableConfig = new TableGuiConfiguration(); // table configuration
		tableConfig.setTableEmptyMessage(translate("label.controller.taskoverview.empty_table")); // message for empty table
		removeAsListenerAndDispose(tableCntr);
	  tableCntr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator()); // reference on table controller
		listenTo(tableCntr); // focus on table controller
		
		// 2) naming the columns
		tableCntr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.taskoverview.name", 0, null, getLocale()));
		tableCntr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.taskoverview.institute_id", 1, null, getLocale()));
		tableCntr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.taskoverview.viewtask", 2, KEY_VIEW_TASK, getLocale()));
		tableCntr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.taskoverview.testtask", 3, KEY_VIEW_TEST, getLocale()));
		tableCntr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.taskoverview.instance_date", 4, null, getLocale()));
		tableCntr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.taskoverview.solution", 5, null, getLocale()));
		tableCntr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.taskoverview.fails", 6, null, getLocale()));
		tableCntr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.taskoverview.passed", 7, KEY_VIEW_PASSED, getLocale()));
		tableCntr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.taskoverview.points", 8, KEY_VIEW_SCORE, getLocale()));
		tableCntr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.taskoverview.scorepoints", 9, null, getLocale()));
		tableCntr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.taskoverview.livinginstances", 10, null, getLocale()));

		tableCntr.setSortColumn(1, true);
		
		// 3) initialize the model
		//taskOverviewTableModel = new TaskOverviewTableModel(courseEnv.getCourseGroupManager().getParticipantsFromArea(null), getLocale(), courseEnv, ne, courseID);
		taskOverviewTableModel = new TaskOverviewTableModel(coachedStudents, getLocale(), courseEnv, ne, courseID);
		tableCntr.setTableDataModel(taskOverviewTableModel);
		//taskOverviewTableModel.setObjects(courseEnv.getCourseGroupManager().getParticipantsFromArea(null));
		taskOverviewTableModel.setObjects(coachedStudents);
		overviewVC.put("taskOverviewList", tableCntr.getInitialComponent());
		
		
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub		
	}

	@Override
	protected void event(UserRequest ureq, Component comp, Event evnt) {
	}
	
	protected void event(UserRequest ureq, Controller ctr, Event evnt) {
		if(ctr == CMCEditPassedView) {
			if(evnt.equals(FormEvent.DONE_EVENT)) {	
				CMCDialog.deactivate();
				tableCntr.modelChanged();
			}
		}
		if(ctr == CMCEditScoreView) {
			if(evnt.equals(FormEvent.DONE_EVENT)) {
				CMCDialog.deactivate();
				tableCntr.modelChanged();				
			}
		}
		if(ctr == tableCntr) {
			if(evnt.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) evnt;
				int rowid = te.getRowId();
/*				if(te.getActionId().equals(KEY_VIEW_SOLUTION)) {
					
					TaskSolution taskSolution= taskOverviewTableModel.getBestSolutionAtRow(rowid);
					TaskInstance taskInstance = taskOverviewTableModel.getTaskInstanceAtRow(rowid);
					if(taskSolution != null && taskInstance != null) {
						CMCBestSolView = new CMCBestSolutionViewController(ureq, getWindowControl(), taskSolution, taskInstance);
						CMCBestSolView.addControllerListener(this);
						CMCDialog = new CloseableModalController(getWindowControl(), 
								translate("label.controller.cmcbestsolutionviewcontroller.close"), CMCBestSolView.getInitialComponent());
						CMCDialog.addControllerListener(this);
						CMCDialog.activate();
					}
				}*/
				if(te.getActionId().equals(KEY_VIEW_TASK)) {
					TaskSolution taskSolution= taskOverviewTableModel.getBestSolutionAtRow(rowid);
					TaskInstance taskInstance = taskOverviewTableModel.getTaskInstanceAtRow(rowid);
					if(taskInstance != null) {						
						CMCTaskView = new CMCTaskViewController(ureq, getWindowControl(), taskInstance, taskSolution, courseID, courseNodeID);
						CMCTaskView.addControllerListener(this);
						CMCDialog = new CloseableModalController(getWindowControl(), 
								translate("label.controller.cmctaskviewcontroller.close"), CMCTaskView.getInitialComponent());
						CMCDialog.addControllerListener(this);
						CMCDialog.activate();						
						
					}
				}
				if(te.getActionId().equals(KEY_VIEW_TEST)) {
					TaskInstance taskInstance = taskOverviewTableModel.getTaskInstanceAtRow(rowid);
					if(taskInstance != null) {
						fireEvent(ureq, new TaskTestEvent(taskInstance, KEY_TEST_TASK));
					}
				}
				if(te.getActionId().equals(KEY_VIEW_PASSED)) {
					TaskInstance taskInstance = taskOverviewTableModel.getTaskInstanceAtRow(rowid);
					Student student = StudentManagerImpl.getInstance().getStudentByIdentity(taskOverviewTableModel.getIdentityByRow(rowid));
					if(student != null) {						
						CMCEditPassedView = new CMCEditPassedController(ureq, getWindowControl(), getTranslator(), taskInstance, student, courseID, courseNodeID );
						CMCEditPassedView.addControllerListener(this);
						CMCDialog = new CloseableModalController(getWindowControl(),
								translate("label.controller.cmceditpassedcontroller.close"), CMCEditPassedView.getInitialComponent());
						CMCDialog.addControllerListener(this);
						CMCDialog.activate();						
					}
				}
				if(te.getActionId().equals(KEY_VIEW_SCORE)) {
					TaskInstance taskInstance = taskOverviewTableModel.getTaskInstanceAtRow(rowid);
					Student student = StudentManagerImpl.getInstance().getStudentByIdentity(taskOverviewTableModel.getIdentityByRow(rowid));
					if(student != null) {						
						CMCEditScoreView = new CMCEditScoreController(ureq, getWindowControl(), taskInstance, student, courseID, courseNodeID);
						CMCEditScoreView.addControllerListener(this);
						CMCDialog = new CloseableModalController(getWindowControl(),
								translate("label.controller.cmceditscorecontroller.close"), CMCEditScoreView.getInitialComponent());
						CMCDialog.addControllerListener(this);
						CMCDialog.activate();						
					}
				}
			}
		}
	}
}
