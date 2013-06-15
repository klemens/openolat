package de.htwk.autolat.BBautOLAT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
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
import org.olat.course.ICourse;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.TaskModule.TaskModule;
import de.htwk.autolat.TaskModule.TaskModuleTableModel;
/**
 * This Controller is used to generate and edit task modules to structure autotool tasks. 
 * 
 * <P>
 * @author werjo
 *
 */
public class EditModuleController extends BasicController {
	
	private static final String KEY_EDIT = "key.edit";
	private static final String KEY_DELETE = "key.delete";
	
	private long courseNodeID;
	private Configuration conf;
	
	private Panel main;
	private VelocityContainer mainvc;
	
	private TableController taskModulesListCtr;
	private TaskModuleTableModel taskModuleTableModel;
	
	private CMCEditTaskModuleController CMCEditTaskModuleInlay;
	private CloseableModalController CMCEditTaskModuleCtr;
	
	private Link createTaskModule;
	private ICourse course;
	
	/**
	 * Constructor
	 * 
	 * @param ureq
	 * @param wControl
	 * @param courseNodeID
	 * @param courseNode
	 * @param course
	 */
	public EditModuleController(UserRequest ureq, WindowControl wControl, long courseNodeID, BBautOLATCourseNode courseNode,
			ICourse course) {
		super(ureq, wControl);
		
		this.course = course;
		this.courseNodeID = courseNodeID;
		this.conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(course.getResourceableId(), courseNodeID);
		
		main = new Panel("editModuleController");
		mainvc = createVelocityContainer("editModuleController");
		
		initializeTaskModulesListCtr(ureq);
		updateTaskModulesListCtr(conf.getTaskPlan());
		
		createTaskModule = LinkFactory.createButton("label.controller.edittask.createtaskmodule", 
				mainvc, this);
		


		mainvc.put("createTaskModule", createTaskModule);
		
		main.setContent(mainvc);
		putInitialPanel(main);
		
		
	}

	@Override
	protected void event(UserRequest ureq, Component comp, Event evnt) {
		if(comp == createTaskModule) {			
			Configuration configuration = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(course.getResourceableId(), courseNodeID);
			//the dates for begin and end of the working period are needed, so the button don't work when they are null
			//it is also not functional when task configuration is null
			if(configuration.getTaskConfiguration()==null) {
				showInfo("label.info.modulebutton.task");
				return;
			}
			if(configuration.getBeginDate()==null || configuration.getEndDate()==null) {
				showInfo("label.info.modulebutton.date");
				return;
			}
			
			CMCEditTaskModuleInlay = new CMCEditTaskModuleController(ureq, getWindowControl(), course.getResourceableId(), courseNodeID, null);
			CMCEditTaskModuleInlay.addControllerListener(this);
			CMCEditTaskModuleCtr = new CloseableModalController(getWindowControl(), 
					translate("label.controller.cmcedittaskmodule.close"), CMCEditTaskModuleInlay.getInitialComponent());
			CMCEditTaskModuleCtr.addControllerListener(this);
			CMCEditTaskModuleCtr.activate();
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller ctr, Event evnt) {
		// TODO Auto-generated method stub
		if(ctr == CMCEditTaskModuleInlay) {
			CMCEditTaskModuleCtr.deactivate();
			showInfo("info.form.edittaskmodule.taskmodulecreated");
			conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(course.getResourceableId(), courseNodeID);
			updateTaskModulesListCtr(conf.getTaskPlan());
		}
		
		if(ctr == taskModulesListCtr) {
			if(evnt.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) evnt;
				int rowid = te.getRowId();
				if(te.getActionId().equals(KEY_DELETE)) {
					List<TaskModule> result = conf.getTaskPlan();
					result.remove(taskModuleTableModel.getTaskModuleAtRow(rowid));
					conf.setTaskPlan(result);
					ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
					updateTaskModulesListCtr(conf.getTaskPlan());
				}
				if(te.getActionId().equals(KEY_EDIT)) {
					TaskModule module = taskModuleTableModel.getTaskModuleAtRow(rowid);
					CMCEditTaskModuleInlay = new CMCEditTaskModuleController(ureq, getWindowControl(), course.getResourceableId(), courseNodeID, module);
					CMCEditTaskModuleInlay.addControllerListener(this);
					CMCEditTaskModuleCtr = new CloseableModalController(getWindowControl(), 
							translate("label.controller.cmcedittaskmodule.close"), CMCEditTaskModuleInlay.getInitialComponent());
					CMCEditTaskModuleCtr.addControllerListener(this);
					CMCEditTaskModuleCtr.activate();
				}
			}
		}	
	}
	/**
	 * Initialize the table controller to display generated task modules.
	 * @param ureq
	 */
	private void initializeTaskModulesListCtr(UserRequest ureq) {
		// 1) initialize listing controller
		TableGuiConfiguration tableConfig = new TableGuiConfiguration(); // table configuration
		tableConfig.setTableEmptyMessage(translate("label.controller.edittask.notaskmodules")); // message for empty table
		removeAsListenerAndDispose(taskModulesListCtr);
		taskModulesListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator()); // reference on table controller
		listenTo(taskModulesListCtr); // focus on table controller

		// 2) naming the columns
		taskModulesListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.edittask.moduleid", 0, null, getLocale()));
		taskModulesListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.edittask.position", 1, null, getLocale()));
		taskModulesListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.edittask.duration", 2, null, getLocale()));
		taskModulesListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.edittask.enddate", 3, null, getLocale()));
		taskModulesListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.edittask.maxcount", 4, null, getLocale()));
		if(conf.getTaskInstanceList().size()==0) { //edit and delete only if no instances are generated
			taskModulesListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.edittask.edit", 5, KEY_EDIT, getLocale()));
			taskModulesListCtr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.edittask.delete", 6, KEY_DELETE, getLocale()));
		}
		taskModulesListCtr.setSortColumn(1, true);
		
		// 3) initialize the model
		taskModuleTableModel = new TaskModuleTableModel(new ArrayList(), getLocale());
		taskModulesListCtr.setTableDataModel(taskModuleTableModel);
		mainvc.put("taskModulesList", taskModulesListCtr.getInitialComponent());
	}
	/**
	 * Update table model with new data list.
	 * 
	 * @param inputList
	 */
	private void updateTaskModulesListCtr(List inputList) {
		List wrapped = new ArrayList();
		List taskModules = inputList;
		Iterator iter = taskModules.iterator();
		while (iter.hasNext()) {   // running through all taskModules
			TaskModule taskModule = (TaskModule) iter.next();
			wrapped.add(wrapTaskModule(taskModule));
		}
		taskModuleTableModel.setEntries(wrapped);
		
		taskModulesListCtr.modelChanged();   // update is needed in the model
	}
	
	/**
	 * helper method to wrap a task module into an array of objects.
	 * 
	 * @param taskModule
	 * @return
	 */
	private Object wrapTaskModule(TaskModule taskModule) {

		return new Object[] {taskModule};
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}

}
