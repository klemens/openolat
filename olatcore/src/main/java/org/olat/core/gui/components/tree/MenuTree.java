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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.components.tree;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeHelper;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class MenuTree extends Component {
	private static final ComponentRenderer RENDERER = new MenuTreeRenderer();

	/**
	 * Comment for <code>NODE_IDENT</code>
	 */
	public static final String NODE_IDENT = "nidle";
	
	/**
	 * Comment for <code>NODE_IDENT</code>
	 */
	public static final String COMMAND_TREENODE = "ctntr";

	/**
	 * Comment for <code>NODE_IDENT</code>
	 */
	public static final String TREENODE_OPEN = "open";
	
	/**
	 * Comment for <code>NODE_IDENT</code>
	 */
	public static final String TREENODE_CLOSE = "close";
	
	/**
	 * event fired when a treenode was clicked (all leaf nodes)
	 */
	public static final String COMMAND_TREENODE_CLICKED = "ctncl";
	
	/**
	 * event fired when a treenode was expanded (all nodes except leafs)
	 */
	public static final String COMMAND_TREENODE_EXPANDED = "ctnex";

	private TreeModel treeModel;
	private String selectedNodeId = null;
	private Set<String> openNodeIds = new HashSet<String>();
	private boolean expandServerOnly = true; // default is serverside menu
	private boolean expandSelectedNode = true;
	
	// for recording / visual marking purposes
	TreeNode markingTreeNode; 
	
	private boolean dirtyForUser = false;

	/**
	 * @param name
	 */
	public MenuTree(String name) {
		super(name);
	}
	
	/**
	 * @param name
	 * @param eventListener
	 */
	public MenuTree(String name, Controller eventListener) {
		super(name);
		addListener(eventListener);
	}

	/**
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	protected void doDispatchRequest(UserRequest ureq) {
		if (GUIInterna.isLoadPerformanceMode()) {
			String compPath = ureq.getParameter("en");
			TreeNode selTreeNode = TreeHelper.resolveTreeNode(compPath, getTreeModel());
			selectedNodeId = selTreeNode.getIdent();
		}
		
		String nodeId = ureq.getParameter(NODE_IDENT);
		String openClose = ureq.getParameter(COMMAND_TREENODE);
		if(!StringHelper.containsNonWhitespace(openClose)) {
			selectedNodeId = nodeId;
		}
		handleClick(ureq, openClose, nodeId);
	}
	
	/**
	 * this is true when the user expanded a treenode to view its children.
	 * it is false when the user clicked on a node with an action 
	 */
	@Override
	public boolean isDirtyForUser() {
		return dirtyForUser;
	}
	
	@Override
	public void setDirty(boolean dirty) {
		super.setDirty(dirty);
		if (!dirty) {
			// clear the userdirty flag also
			dirtyForUser = false;
		}
	}
	
	// -- recorder methods
	
	/**
	 * @param selTreeNode
	 */
	private void handleClick(UserRequest ureq, String cmd, String selNodeId) {
		TreeNode selTreeNode = treeModel.getNodeById(selNodeId);
		
		// could be if upon click, an error occured -> timestamp check does not apply, but the tree model was regenerated (as in course)
		if (selTreeNode == null) return;
				
		if (!selTreeNode.isAccessible()){
			TreeEvent te = new TreeEvent(COMMAND_TREENODE_EXPANDED, selNodeId);
			dirtyForUser = true;
			super.setDirty(true);
			
			fireEvent(ureq, te);
			return;
		}

		TreeNode deleg = selTreeNode.getDelegate();
		boolean changeSelectedNodeId = false;
		if (deleg != null) {
			changeSelectedNodeId = updateOpenedNode(selTreeNode, selNodeId, cmd);
			selNodeId = deleg.getIdent();
			selTreeNode = deleg;
		}
		
		String subCmd = null;
		if(TREENODE_CLOSE.equals(cmd)) {
			subCmd = TreeEvent.COMMAND_TREENODE_CLOSE;
		} else if (TREENODE_OPEN.equals(cmd)) {
			subCmd = TreeEvent.COMMAND_TREENODE_OPEN;
		}
		changeSelectedNodeId |= updateOpenedNode(selTreeNode, selNodeId, cmd);

		TreeEvent te = new TreeEvent(COMMAND_TREENODE_CLICKED, subCmd, selNodeId);
		if (selTreeNode.getChildCount() > 0) {
			dirtyForUser = true;
		} // else dirtyForUser is false, since we clicked a node (which only results in the node beeing marked in a visual style)
		super.setDirty(true);
		fireEvent(ureq, te);
	}
	
	private boolean updateOpenedNode(TreeNode treeNode, String nodeId, String cmd) {
		if(TREENODE_CLOSE.equals(cmd)) {
			removeTreeNodeFromOpenList(treeNode, nodeId);
			if(selectedNodeId != null && isChildOf(treeNode, selectedNodeId)) {
				clearSelection();
				setSelectedNodeId(nodeId);
				return true;
			}
		} else if (TREENODE_OPEN.equals(cmd)) {
			openNodeIds.add(nodeId);
			if(treeNode.getUserObject() instanceof String) {
				openNodeIds.add((String)treeNode.getUserObject());
			}
		} else if (cmd == null) {
			openNodeIds.add(nodeId);
			if(treeNode.getUserObject() instanceof String) {
				openNodeIds.add((String)treeNode.getUserObject());
			}
		}
		return false;
	}
	
	private void removeTreeNodeFromOpenList(TreeNode treeNode, String nodeId) {
		openNodeIds.remove(nodeId);
		openNodeIds.remove(treeNode.getUserObject());
		
		for(int i=treeNode.getChildCount(); i-->0; ) {
			TreeNode child = (TreeNode)treeNode.getChildAt(i);
			String childId = child.getIdent();
			TreeNode deleg = child.getDelegate();
			if (deleg != null) {
				childId = deleg.getIdent();
				child = deleg;
			}
			removeTreeNodeFromOpenList(child, childId);
		}
	}
	
	private boolean isChildOf(INode treeNode, String childId) {
		int childCount = treeNode.getChildCount();
		for(int i=0; i<childCount; i++) {
			INode childNode = treeNode.getChildAt(i);
			if(childNode.getIdent().equals(childId) ||
					(childNode instanceof TreeNode && childId.equals(((TreeNode)childNode).getUserObject())) ||
					(isChildOf(childNode, childId))) {
				return true;
			}
		}
		return false;
	}
	
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
	}

	/**
	 * @return the selected node
	 */
	public TreeNode getSelectedNode() {
		return (selectedNodeId == null ? null : treeModel.getNodeById(selectedNodeId));
	}

	/**
	 * @return the selected node's id
	 */
	public String getSelectedNodeId() {
		return selectedNodeId;
	}

	/**
	 * @param nodeId
	 */
	public void setSelectedNodeId(String nodeId) {
		selectedNodeId = nodeId;
		setDirty(true);
	}

	public Collection<String> getOpenNodeIds() {
		return openNodeIds;
	}

	public void setOpenNodeIds(Collection<String> openNodeIds) {
		if(openNodeIds == null) {
			this.openNodeIds.clear();
		} else {
			this.openNodeIds = new HashSet<String>(openNodeIds);
		}
		setDirty(true);
	}

	/**
	 * 
	 */
	public void clearSelection() {
		selectedNodeId = null;
	}

	/**
	 * @return MutableTreeModel
	 */
	public TreeModel getTreeModel() {
		return treeModel;
	}

	/**
	 * Sets the treeModel.
	 * 
	 * @param treeModel The treeModel to set
	 */
	public void setTreeModel(TreeModel treeModel) {
		this.treeModel = treeModel;
		selectedNodeId = null; // clear selection if a new model is set
		dirtyForUser = true;
		super.setDirty(true);
	}

	/**
	 * @return Returns the expandServerOnly.
	 */
	public boolean isExpandServerOnly() {
		return expandServerOnly;
	}

	/**
	 * @param expandServerOnly The expandServerOnly to set.
	 */
	public void setExpandServerOnly(boolean expandServerOnly) {
		this.expandServerOnly = expandServerOnly;
	}
	
	/**
	 * Expand the selected node to view its children
	 * @return
	 */
	public boolean isExpandSelectedNode() {
		return expandSelectedNode;
	}

	public void setExpandSelectedNode(boolean expandSelectedNode) {
		this.expandSelectedNode = expandSelectedNode;
	}

	/**
	 * @param nodeForum
	 */
	public void setSelectedNode(TreeNode node) {
		String nId = node.getIdent();
		setSelectedNodeId(nId);
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}