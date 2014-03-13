/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package de.htwk.autolat.BBautOLAT.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.IUserActivityLogger;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.ObjectivesHelper;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.PeekViewWrapperController;
import org.olat.course.nodes.st.STCourseNodeConfiguration;
import org.olat.course.nodes.st.STCourseNodeEditController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.util.logging.activity.LoggingResourceable;

import de.htwk.autolat.BBautOLAT.BBautOLATCourseNode;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.TaskInstance.TaskInstance;

/**
 * Description:<br>
 * variant of the structure node to aggregate autOLAT task results
 * 
 * @author Joerg
 */
public class BBautOLATStructureNodeRunController extends BasicController {

	private static final String PACKAGE = Util.getPackageName(BBautOLATStructureNodeRunController.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(BBautOLATStructureNodeRunController.class);

	private static final String ACTIVITY_GOTO_NODE = "GOTO_NODE";
	private static final String GOTO_NID = "GOTTO_NID: ";

	private VelocityContainer myContent;
	private TableController tableCntr;
	private TableController tableCntrPoints;
	
	private HighScoreTableModel tableModel;
	private PointOverviewTableModel pointModel;
	private TopListTableModel topListTableModel;
	
	private CourseEnvironment courseEnv;
	private UserCourseEnvironment uCourseEnv;
	private ScoreEvaluation se;
	private NodeEvaluation ne;
	private UserRequest ureq;
	private Panel main;
	
	private List<Identity> participants;
	private Identity userID;
	
	

	private BBautOLATStructureNode courseNode;
	
	private ModuleConfiguration config;
	private WindowControl wCntr;
	
	/**
	 * @param ureq
	 * @param userCourseEnv
	 * @param stCourseNode
	 * @param se
	 * @param ne
	 */
	public BBautOLATStructureNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, BBautOLATStructureNode bBautOLATStructureNode, ScoreEvaluation se,
			NodeEvaluation ne) {
		super(ureq, wControl);
		PackageTranslator trans = new PackageTranslator(PACKAGE, ureq.getLocale());
		
		this.uCourseEnv = userCourseEnv;
		this.courseEnv = userCourseEnv.getCourseEnvironment();
		this.courseNode = bBautOLATStructureNode;
		this.userID = ureq.getIdentity();
		this.se = se;
		this.ne = ne;
		this.ureq = ureq;
		main = new Panel("highScorePanel");
		putInitialPanel(main);
		config = bBautOLATStructureNode.getModuleConfiguration();
		
		
		
		myContent = new VelocityContainer("olatmodstrun", VELOCITY_ROOT + "/run.html", trans, this);
		createOutput();
		main.setContent(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == myContent) {

			// must be the id of a course node
			fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, event.getCommand()));
		}
	}
	
	private void createOutput() { 
		
		List<CourseNode> children = new ArrayList<CourseNode>();
		List<BBautOLATCourseNode> autotoolTask = new ArrayList<BBautOLATCourseNode>();
		
		// build up a overview of all visible children (direct children only, no
		// grandchildren)
		// autotooltasks need for the Highscoretable
		/*String displayType = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW);
		String peekviewChildNodesConfig = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_CHILD_NODES, null);
		List<String> peekviewChildNodes =  (peekviewChildNodesConfig == null ? new ArrayList<String>() : Arrays.asList(peekviewChildNodesConfig.split(",")));
		int chdCnt = ne.getChildCount();
		for (int i = 0; i < chdCnt; i++) {
			NodeEvaluation neChd = ne.getNodeEvaluationChildAt(i);
			if (neChd.isVisible()) {
				// Build and add child generic or specific peek view
				CourseNode child = neChd.getCourseNode();
				Controller childViewController = null;
				Controller childPeekViewController = null;
				if (displayType.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW)) {
					if (peekviewChildNodes.size() == 0) {
						// Special case: no child nodes configured. This is the case when
						// the node has been configured before it had any children. We just
						// use the first children as they appear in the list
						if (i < STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES) {
							childPeekViewController = child.createPeekViewRunController(ureq, wCntr, uCourseEnv, neChd);
						} else {
							// Stop, we already reached the max count
							break;
						}
					} else {
						// Only add configured children
						if (peekviewChildNodes.contains(child.getIdent())) {
							childPeekViewController = child.createPeekViewRunController(ureq, wCntr, uCourseEnv, neChd);
						} else {
							// Skip this child - not configured
							continue;
						}
					}
				}
				// Add child to list		
				children.add(child);
				if(child.getType().equals(BBautOLATCourseNode.TYPE)) {
					autotoolTask.add((BBautOLATCourseNode)child);
				}
				childViewController = new PeekViewWrapperController(ureq, wCntr, child, childPeekViewController);
				listenTo(childViewController); // auto-dispose controller
				myContent.put("childView_" + child.getIdent(), childViewController.getInitialComponent());
			}
		}*/
				
		
		int chdCnt = ne.getChildCount();
		for (int i = 0; i < chdCnt; i++) {
			NodeEvaluation neChd = ne.getNodeEvaluationChildAt(i);
			if (neChd.isVisible()) {
				CourseNode child = neChd.getCourseNode();
				children.add(child);
				if(child.getType().equals(BBautOLATCourseNode.TYPE)) {
					autotoolTask.add((BBautOLATCourseNode)child);
				}
			}
		}
		
		if(userIsCoach()) {
			
			//List<Identity> students = getCoachedStudents();
			
			TableGuiConfiguration tableConfig = new TableGuiConfiguration(); // table configuration
			tableConfig.setTableEmptyMessage(translate("label.controller.structure.empty_table")); // message for empty table
			removeAsListenerAndDispose(tableCntr);
			tableCntr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator()); // reference on table controller
			listenTo(tableCntr);
			
			
		  //create table columns
			tableCntr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.highscore.name", 0, null, getLocale()));
			tableCntr.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.highscore.instid", 1, null, getLocale()));

			int i = 0;
			for(i = 0; i < autotoolTask.size(); i++) {
				DefaultColumnDescriptor defCD; 
				if(i < 30) {
					defCD = new DefaultColumnDescriptor("task."+i, i+3, null, getLocale());
				}
				else {
					defCD = new DefaultColumnDescriptor("task.nn", i+3, null, getLocale());
				}
				
				defCD.setEscapeHtml(EscapeMode.none);
				tableCntr.addColumnDescriptor(defCD);


			}
			DefaultColumnDescriptor sum = new DefaultColumnDescriptor("label.controller.highscore.sum", i+3+1, null, getLocale());
			sum.setEscapeHtml(EscapeMode.none);
			tableCntr.addColumnDescriptor(sum);
			tableCntr.setSortColumn(autotoolTask.size()+2, false);
			
			tableModel = new HighScoreTableModel(uCourseEnv.getCourseEnvironment().getCourseResourceableId(), getCoachedStudents(), autotoolTask);
			tableCntr.setTableDataModel(tableModel);
			tableModel.setObjects(getCoachedStudents());
			myContent.contextPut("isCoach", true);
			myContent.put("highscoreList", tableCntr.getInitialComponent());
			
			TableGuiConfiguration tableConfigPoints = new TableGuiConfiguration(); // table configuration
			tableConfigPoints.setTableEmptyMessage(translate("label.controller.structure.empty_table")); // message for empty table
			removeAsListenerAndDispose(tableCntrPoints);
			tableCntrPoints = new TableController(tableConfigPoints, ureq, getWindowControl(), getTranslator()); // reference on table controller
			listenTo(tableCntrPoints);
			
			
		  //create table columns
			tableCntrPoints.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.highscore.name", 0, null, getLocale()));
			tableCntrPoints.addColumnDescriptor(new DefaultColumnDescriptor("label.controller.highscore.instid", 1, null, getLocale()));

			for(i = 0; i < autotoolTask.size(); i++) {
				DefaultColumnDescriptor defCD; 
				if(i < 30) {
					defCD = new DefaultColumnDescriptor("task."+i, i+3, null, getLocale());
				}
				else {
					defCD = new DefaultColumnDescriptor("task.nn", i+3, null, getLocale());
				}
				
				defCD.setEscapeHtml(EscapeMode.none);
				tableCntrPoints.addColumnDescriptor(defCD);


			}
			sum = new DefaultColumnDescriptor("label.controller.highscore.sum", i+3+1, null, getLocale());
			sum.setEscapeHtml(EscapeMode.none);
			tableCntrPoints.addColumnDescriptor(sum);
			tableCntrPoints.setSortColumn(autotoolTask.size()+2, false);
			
			pointModel = new PointOverviewTableModel(uCourseEnv.getCourseEnvironment().getCourseResourceableId(), getCoachedStudents(), autotoolTask);
			tableCntrPoints.setTableDataModel(pointModel);
			pointModel.setObjects(getCoachedStudents());
			myContent.contextPut("isCoach", true);
			myContent.put("pointList", tableCntrPoints.getInitialComponent());
			
			myContent.contextPut("children", children);
			myContent.contextPut("nodeFactory", CourseNodeFactory.getInstance());
			
			// push title and learning objectives, only visible on intro page
			
			myContent.contextPut("menuTitle", courseNode.getShortTitle());
			myContent.contextPut("displayTitle", courseNode.getLongTitle());
			myContent.contextPut("hasScore", new Boolean(courseNode.hasScoreConfigured()));
			myContent.contextPut("hasPassed", new Boolean(courseNode.hasPassedConfigured()));

			if (se != null) {
				Float score = se.getScore();
				Boolean passed = se.getPassed();
				if (score != null) {
					myContent.contextPut("scoreScore", AssessmentHelper.getRoundedScore(score));
				}
				if (passed != null) {
					myContent.contextPut("scorePassed", passed);
					myContent.contextPut("hasPassedValue", Boolean.TRUE);
				} else {
					myContent.contextPut("hasPassedValue", Boolean.FALSE);
				}
			}

			// Adding learning objectives
			String learningObj = courseNode.getLearningObjectives();
			if (learningObj != null) {
				Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq);
				myContent.put("learningObjectives", learningObjectives);
				myContent.contextPut("hasObjectives", learningObj); // dummy value, just
																														// an exists operator
			}
			
		}
		
		else {
			
			//List<Identity> students = getCoachedStudents();
			
			TableGuiConfiguration tableConfig = new TableGuiConfiguration(); // table configuration
			tableConfig.setTableEmptyMessage(translate("label.controller.structure.empty_table")); // message for empty table
			removeAsListenerAndDispose(tableCntr);
		  tableCntr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator()); // reference on table controller
			listenTo(tableCntr);
			
			//add columns
			int i = 0;
			for(i = 0; i < autotoolTask.size(); i++) {
				DefaultColumnDescriptor defCD; 
				if(i < 30) {
					defCD = new DefaultColumnDescriptor("task."+i, i, null, getLocale());
				}
				else {
					defCD = new DefaultColumnDescriptor("task.nn", i, null, getLocale());
				}
				
				defCD.setEscapeHtml(EscapeMode.none);
				tableCntr.addColumnDescriptor(defCD);


			}
			DefaultColumnDescriptor sum = new DefaultColumnDescriptor("label.controller.highscore.sum", i+1, null, getLocale());
			sum.setEscapeHtml(EscapeMode.none);
			tableCntr.addColumnDescriptor(sum);
			tableCntr.setSortColumn(autotoolTask.size(), false);
			
			topListTableModel = new TopListTableModel(uCourseEnv.getCourseEnvironment().getCourseResourceableId(), getStudents(), userID, autotoolTask);
			tableCntr.setTableDataModel(topListTableModel);
			topListTableModel.setObjects(getStudents());
			myContent.put("topList", tableCntr.getInitialComponent());
			

			myContent.contextPut("isCoach", false);
			myContent.contextPut("children", children);
			myContent.contextPut("nodeFactory", CourseNodeFactory.getInstance());
			
			// push title and learning objectives, only visible on intro page
			
			myContent.contextPut("menuTitle", courseNode.getShortTitle());
			myContent.contextPut("displayTitle", courseNode.getLongTitle());
			myContent.contextPut("hasScore", new Boolean(courseNode.hasScoreConfigured()));
			myContent.contextPut("hasPassed", new Boolean(courseNode.hasPassedConfigured()));

			if (se != null) {
				Float score = se.getScore();
				Boolean passed = se.getPassed();
				if (score != null) {
					myContent.contextPut("scoreScore", AssessmentHelper.getRoundedScore(score));
				}
				if (passed != null) {
					myContent.contextPut("scorePassed", passed);
					myContent.contextPut("hasPassedValue", Boolean.TRUE);
				} else {
					myContent.contextPut("hasPassedValue", Boolean.FALSE);
				}
			}

			// Adding learning objectives
			String learningObj = courseNode.getLearningObjectives();
			if (learningObj != null) {
				Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq);
				myContent.put("learningObjectives", learningObjectives);
				myContent.contextPut("hasObjectives", learningObj); // dummy value, just
																														// an exists operator
			}
		}
		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
	// nothing to do yet
	}
	
	protected boolean userIsCoach() 
	{
		boolean isCoach = false;
		CourseGroupManager cgm = courseEnv.getCourseGroupManager();
			
		// get all groups from which this node (i.e. task) is visible
		String groupstring = courseNode.getPreConditionVisibility().getEasyModeGroupAccess();
		if(groupstring!=null) {			
			for(String group : groupstring.split(",\\s?"))
			{				
				// old if(cgm.getCoachesFromLearningGroup(group).contains(userID))
				if(cgm.getCoachesFromBusinessGroups().contains(getIdentity()))
					isCoach = true;
			}
		}
		else {
			try {
				// old if(cgm.getCoachesFromArea(null).contains(userID))
				if(cgm.getCoachesFromAreas().contains(getIdentity()))
					isCoach = true;
			} catch (AssertException ae) {
				//unsupportet in preview so return false
				return false;
			}
		}
		return isCoach;
	}
	
	/*protected boolean userIsParticipant()
	{
		boolean isParticipant = false;
		CourseGroupManager cgm = courseEnv.getCourseGroupManager();
		
		// get all groups from which this node (i.e. task) is visible
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
	}*/
	
	protected List<Identity> getCoachedStudents()
	{	
		List<String> grouplist = new ArrayList<String>();
		CourseGroupManager cgm = courseEnv.getCourseGroupManager();
		
		// this list will hold all users that are displayed in the overview
		participants = new ArrayList<Identity>();
						
		// get all groups from which this node (i.e. task) is visible
		String groupstring = courseNode.getPreConditionVisibility().getEasyModeGroupAccess();
		if(groupstring!=null)
			grouplist = Arrays.asList(groupstring.split(",\\s?"));
		
		// get all groups where user is coach (= 'owned groups'? (hopefully))
		List<BusinessGroup> ownedGroups = new ArrayList<BusinessGroup>();
		try {
			// old ownedGroups = cgm.getOwnedLearningGroupsFromAllContexts(userID);	
			ownedGroups = cgm.getOwnedBusinessGroups(getIdentity());	
		} catch(AssertException ae) {
			//Unsupported in preview so return a empty list or the actually participan list
			return participants;
		}
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
		
		HashSet<Identity> tempSet = new HashSet<Identity>(participants);
	  participants.clear();
	  participants.addAll(tempSet);
		
		return participants;
	}
	
	protected List<Identity> getStudents() {
		
		List<String> grouplist = new ArrayList<String>();
		participants = new ArrayList<Identity>();
		CourseGroupManager cgm = courseEnv.getCourseGroupManager();

		// get all groups from which this node (i.e. task) is visible
		String groupstring = courseNode.getPreConditionVisibility().getEasyModeGroupAccess();
		
		if(groupstring!=null) {
			grouplist = Arrays.asList(groupstring.split(",\\s?"));
		}
		try {
			for(String group : grouplist) {
				// old participants.addAll(cgm.getParticipantsFromLearningGroup(group));
				participants.addAll(cgm.getParticipantsFromBusinessGroups());
			}
			
			// old participants.addAll(cgm.getParticipantsFromArea(null));
			participants.addAll(cgm.getParticipantsFromAreas());
		} catch(AssertException ae) {
			//functions are unsupported in preview
		}
		
		// remove duplicates (not necessary is learnings groups are disjunct)
		HashSet<Identity> tempSet = new HashSet<Identity>(participants);
	  participants.clear();
	  participants.addAll(tempSet);
		
	  //System.out.println("Students" + participants.size());
		
		return participants;
	}
	
}
