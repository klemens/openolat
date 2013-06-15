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
package de.htwk.autolat.BBautOLAT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.JDOMException;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.Connector.AutolatConnectorException;
import de.htwk.autolat.Connector.BBautOLATConnector;
import de.htwk.autolat.Connector.types.TaskTree;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskConfiguration.TaskConfigurationManagerImpl;
import de.htwk.autolat.TaskType.TaskType;
import de.htwk.autolat.TaskType.TaskTypeManagerImpl;

/**
 * Description:<br>
 * CMC controller to select a task type to configure a auto tool task configuration.
 * 
 * <P>
 * Initial Date:  04.01.2010 <br>
 * @author Tom
 */
public class CMCTaskTypeSelectController extends BasicController {

	private long courseNodeID;
	
	private MenuTree typeTree;
	private GenericTreeModel treeModel;
	
	private Panel main;
	private VelocityContainer mainvc;

	private long courseID;
	
	/**
	 * @param ureq the user reqeust object
	 * @param control the window control object
	 * @param courseNodeID course node id to determine the configuration
	 */
	public CMCTaskTypeSelectController(UserRequest ureq, WindowControl control, long courseID, long courseNodeID) {
		super(ureq, control);
		
		this.courseNodeID = courseNodeID;
		this.courseID = courseID;
		
		mainvc = this.createVelocityContainer("CMCTaskTypeSelectController");

		createTypeTree();
	
		main = this.putInitialPanel(mainvc);
	}

	/**
	 * build a tree of task types from the database
	 * @throws AutolatConnectorException 
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	private void createTypeTree() { 
		
		List<TaskTree> tree = new ArrayList<TaskTree>();
		BBautOLATConnector conn = null;
		try {
			conn = new BBautOLATConnector(ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID));
			
		} catch (JDOMException e) {
			showInfo("error.form.editconnection.XMLerror");
			return;
		} catch (IOException e) {
			showInfo("error.form.editconnection.IOerror");
			return;
		} catch (AutolatConnectorException e) {
			showInfo("error.form.editconnection.Servererror");
			return;
		}

		tree = conn.getTaskTree();
		treeModel = new GenericTreeModel();
		//typeTree = new MenuTree("typeTree", this);
		typeTree = new MenuTree("typeTree");
		//create the nodes for the tree model, starting with head, going depth-first
		
		String headText = getTranslator().translate("label.controller.cmctasktypeselect.headnode");
		if(ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID).getTaskConfiguration()!=null) {
			headText = headText + ", " + getTranslator().translate("label.controller.cmctasktypeselect.headnodetype") + " " +
			conn.getTaskTypeHierarchyBreadcrumb(ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID).getTaskConfiguration().getTaskType());
		}
		TreeNode head = new GenericTreeNode(headText, null);
		Iterator<TaskTree> treeIt = tree.iterator();
		while(treeIt.hasNext()) {
			TaskTree childTypes = treeIt.next();
			TreeNode temp = new GenericTreeNode((childTypes.isCategory() ? childTypes.getCategory().getCategoryName() : childTypes.getTask().getTaskName()),
					(childTypes.isCategory() ? null : TaskTypeManagerImpl.getInstance().findTaskTypeByType(childTypes.getTask().getTaskName())));
			if(childTypes.isCategory()) addChildTypes(temp, childTypes);
			head.addChild(temp);
		}
		//setting the head with all children as the tree
		treeModel.setRootNode(head);
		typeTree.setTreeModel(treeModel);
		typeTree.addListener(this);
		mainvc.put("typeTree", typeTree);
	}
	/**
	 * 
	 * @param node
	 * @param children
	 */
	private void addChildTypes(TreeNode node, TaskTree children) {
		
		List<TaskTree> subChildren = children.getCategory().getSubTrees();
		Iterator<TaskTree> subIt = subChildren.iterator();
		while(subIt.hasNext()) {
			TaskTree subTree = subIt.next();
			TreeNode temp = new GenericTreeNode((subTree.isCategory() ? subTree.getCategory().getCategoryName() : subTree.getTask().getTaskName()),
					(subTree.isCategory() ? null : TaskTypeManagerImpl.getInstance().findTaskTypeByType(subTree.getTask().getTaskName())));
			if(subTree.isCategory()) addChildTypes(temp, subTree);
			node.addChild(temp);
		}
		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing to to
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component comp, Event evnt) {
	
		if(comp == typeTree && evnt.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
			TreeNode selectedNode = typeTree.getSelectedNode();
			Object typeObject = null;
			if(selectedNode != null) {
				typeObject = selectedNode.getUserObject();
			}
			if(typeObject!=null) {
				TaskType taskType = (TaskType) typeObject;
				Configuration conf = ConfigurationManagerImpl.getInstance().getConfigurationByCourseID(courseID, courseNodeID);
				
				//save the old task config if it was altered by the user, 
				//in this case: do a check up for eliminating unused duplicates in the db
				//otherwise delete the standard task config
				if(conf.getTaskConfiguration()!=null) {
					TaskConfiguration previousConf = conf.getTaskConfiguration();
					conf.setTaskConfiguration(null);
					ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
					if(!previousConf.getIsAltered()) {
						TaskConfigurationManagerImpl.getInstance().deleteTaskConfiguration(previousConf);
					}
					else TaskConfigurationManagerImpl.getInstance().handleUnusedDuplicates();
				}
				
				BBautOLATConnector conn;
				try {
					conn = new BBautOLATConnector(conf);
				} catch (JDOMException e) {
					showInfo("error.form.editconnection.XMLerror");
					return;
				} catch (IOException e) {
					showInfo("error.form.editconnection.IOerror");
					return;
				} catch (AutolatConnectorException e) {
					showInfo("error.form.editconnection.Servererror");
					return;
				}
				TaskConfiguration taskConfiguration = conn.getTaskConfiguration(taskType);
				TaskConfigurationManagerImpl.getInstance().saveTaskConfiguration(taskConfiguration);			
				conf.setTaskConfiguration(taskConfiguration);
				ConfigurationManagerImpl.getInstance().updateConfiguration(conf);
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller ctr, Event evnt) {
	
	}

}
