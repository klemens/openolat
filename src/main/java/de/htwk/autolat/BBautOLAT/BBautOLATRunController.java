package de.htwk.autolat.BBautOLAT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Connector.BBautOLATConnector;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.Student.StudentManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskResult.TaskResult;

public class BBautOLATRunController extends BasicController{

	private BBautOLATCourseNode courseNode;
	
	private long courseNodeID;
	private ModuleConfiguration configuration;
	
	private Translator translator;
	private TabbedPane tabPane;
	private Panel content;
	private VelocityContainer vCon; 
	private VelocityContainer pane1, pane2, pane3;
	private Controller myOverview, myTIRunCtr, myTITestCtr;
	
	private Controller taskInstanceRunController;
	private TaskOverviewController taskOverviewController;
	private TaskInstanceTestController taskInstanceTestController;
	
	private static final String PACKAGE = Util.getPackageName(BBautOLATRunController.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(PACKAGE);

	private List<Identity> coaches;
	private List<Identity> participants;
	private Identity userID;

	private UserCourseEnvironment euce;
	private boolean isPreview;

	private boolean isCoach;
	private boolean isParticipant;

	private Long courseID;
	
	public BBautOLATRunController (WindowControl wControl, UserRequest ureq, 
			BBautOLATCourseNode autOLATCourseNode, UserCourseEnvironment euce, NodeEvaluation ne, boolean isPreview)				
	{
		
		super(ureq, wControl);
		
		this.courseNode = autOLATCourseNode;
		this.configuration = courseNode.getModuleConfiguration();
		this.euce = euce;
		this.isPreview = isPreview;
				
		courseID = euce.getCourseEnvironment().getCourseResourceableId();
		courseNodeID = Long.valueOf(autOLATCourseNode.getIdent());
				
		userID = euce.getIdentityEnvironment().getIdentity();		
		isCoach = euce.getCourseEnvironment().getCourseGroupManager().isIdentityCourseCoach(userID);
				
		if(isPreview)
		{
			// This is actually a very very simple approach because actually the preview groups
			// should be checked with the PreviewCourseGroupManager, but this turned out to be
			// rather complex. (There seems to be no prior case where this type of functionality
			// has been used.)
			if(!isCoach) isParticipant = true;
		}
		else
		{
			//old isParticipant = euce.getCourseEnvironment().getCourseGroupManager().getParticipantsFromLearningGroup(null).contains(userID);
			isParticipant = euce.getCourseEnvironment().getCourseGroupManager().getParticipantsFromBusinessGroups().contains(getIdentity());
		}
						
		Roles roles = euce.getIdentityEnvironment().getRoles();
							
		translator = new PackageTranslator(PACKAGE, ureq.getLocale());
			
		vCon = this.createVelocityContainer("runController");
						
		tabPane = new TabbedPane("tabPane", ureq.getLocale());
		tabPane.addListener(this);
												
		addTabs(ureq, euce.getCourseEnvironment(), ne);
		
		vCon.contextPut("taskShortTitle", autOLATCourseNode.getShortTitle());
		vCon.put("tabPane", tabPane);
		//content = putInitialPanel(tabPane);
		this.putInitialPanel(vCon);
	}
	
	protected void addTabs(UserRequest ureq, CourseEnvironment courseEnv, NodeEvaluation ne)
	{
		// add the overview panel for coaches
		if(isCoach)
		{			
			taskOverviewController = new TaskOverviewController(ureq, getWindowControl(), courseNodeID, courseEnv, ne, getCoachedStudents());
			taskOverviewController.addControllerListener(this);			
			tabPane.addTab(translate("label.tabpane.taskoverview"), taskOverviewController.getInitialComponent());						
		}
		
		// add the test panel for everyone but participants 
		if(!isParticipant || isCoach)
		{
			taskInstanceTestController = new TaskInstanceTestController
				.Builder(ureq, getWindowControl(), courseID, courseNodeID)
				.showSeedInputField()
				.showRandomSeedButton()
				.showTaskText()
				.showOptions()								
				.showSolutionForm()
				//.showSolutionText()
				.build();
			
			//taskInstanceTestController = new TaskInstanceTestController(ureq, getWindowControl(), courseNodeID, ne);
			taskInstanceTestController.addControllerListener(this);
			tabPane.addTab(translate("label.tabpane.tasktest"), taskInstanceTestController.getInitialComponent());
		}

		// add the panel to solve tasks and submit solutions 
		// users that are coaches and participants should be able to get scores as well?
		if(isParticipant)
		{
			taskInstanceRunController = new TaskInstanceRunController(ureq, getWindowControl(), courseNodeID, euce.getCourseEnvironment(), isPreview);
			taskInstanceRunController.addControllerListener(this);
			tabPane.addTab(translate("label.tabpane.taskedit"), taskInstanceRunController.getInitialComponent());			
		}
		
		if(isParticipant && !isCoach)
		{
			Student student = StudentManagerImpl.getInstance().getStudentByIdentity(userID);
			
			if(student != null)			
			{
				Configuration courseConfig = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
				TaskInstance taskInstance = student.getTaskInstanceByConfiguration(courseConfig);				
				if(taskInstance != null)				
				{										
					TaskResult taskResult = taskInstance.getResult(); 
					if(taskResult != null && taskResult.getHasPassed())
					{
						taskInstanceTestController = new TaskInstanceTestController(ureq, getWindowControl(), courseID, courseNodeID);
						taskInstanceTestController.addControllerListener(this);
						tabPane.addTab(translate("label.tabpane.taskteststudent"), taskInstanceTestController.getInitialComponent());
					}
				}
			}			
		}
	}

	// fireEvent(ureq, new TaskTestEvent(taskInstance, KEY_TEST_TASK));
	// abfangen mit Event.getCommand().equals(KEY...)
	
	@Override
	protected void doDispose()
	{
		//TODO Auto-generated method stub
	}

	@Override
	protected void event(UserRequest ureq, Component component, Event event)
	{
		//nothing to do here
	}
	
	protected void event(UserRequest ureq, Controller source, Event event)
	{
		if(source == taskInstanceRunController)
		{			
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		
		if(source == taskOverviewController)
		{
			if(event.getCommand().equals(TaskOverviewController.KEY_TEST_TASK))
			{					
				// cast necessary?
				TaskTestEvent tte = (TaskTestEvent) event;
				taskInstanceTestController.setLivingTaskInstance(tte.getTaskInstance().getLivingTaskInstance(), ureq);				
				tabPane.setSelectedPane(1);
			}			
		}
	}
	
	/*
	protected boolean userIsParticipant()
	{
		boolean isParticipant = false;
		CourseGroupManager cgm = euce.getCourseEnvironment().getCourseGroupManager();
		
		// get all groups for which this node (i.e. task) is visible
		String groupstring = courseNode.getPreConditionVisibility().getEasyModeGroupAccess();
		if(groupstring!=null) {
			for(String group : groupstring.split(",\\s?"))
				if(cgm.getParticipantsFromLearningGroup(group).contains(userID))
					isParticipant= true;
		}
		else {
			if(cgm.getParticipantsFromArea(null).contains(userID))
				isParticipant = true;
		}
		return isParticipant;
	}
	*/
	
	protected List<Identity> getCoachedStudents()
	{	
		if(isPreview)
		{
			return new ArrayList<Identity>();
		}
		
		List<String> grouplist = new ArrayList<String>();
		CourseGroupManager cgm = euce.getCourseEnvironment().getCourseGroupManager();
		
		// this list will hold all users that are displayed in the overview
		participants = new ArrayList<Identity>();
						
		// get all groups from which this node (i.e. task) is visible
		String groupstring = courseNode.getPreConditionVisibility().getEasyModeGroupAccess();
		if(groupstring!=null)
			grouplist = Arrays.asList(groupstring.split(",\\s?"));
		
		// get all groups where user is coach (= 'owned groups'? (hopefully))
		//old		List<BusinessGroup> ownedGroups = cgm.getOwnedLearningGroupsFromAllContexts(userID);						

		cgm.getOwnedBusinessGroups(getIdentity());
		List<BusinessGroup> ownedGroups = cgm.getOwnedBusinessGroups(getIdentity());						
		for(BusinessGroup group : ownedGroups)
		{
			// if the node has visibility restrictions then only add users that
			// are in a group from which the node is visible
			if(groupstring!=null)
			{
				if(grouplist.contains(group.getName())) {
					// old participants.addAll(cgm.getParticipantsFromLearningGroup(group.getName()));
					participants.addAll(cgm.getParticipantsFromBusinessGroups());
				}
			}
			// if there are no visibility restrictions then just add the users
			else {
				// old participants.addAll(cgm.getParticipantsFromLearningGroup(group.getName()));
				participants.addAll(cgm.getParticipantsFromBusinessGroups());
			} 
		}				
		
		// remove duplicates (not necessary is learnings groups are disjunct)
		HashSet<Identity> tempSet = new HashSet<Identity>(participants);
	  participants.clear();
	  participants.addAll(tempSet);
		
		return participants;
	}

}
