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
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/
package org.olat.core.gui.components.tree;

import org.olat.core.gui.control.Event;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for TreeDropEvent
 * 
 * <P>
 * Initial Date:  23 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class TreeDropEvent extends Event {

	private final String droppedNodeId;
	private final String targetNodeId;
	private final boolean asChild;

	/**
	 * 
	 * @param command
	 * @param nodeId
	 */
	public TreeDropEvent(String command, String droppedNodeId, String targetNodeId, boolean asChild) {
		super(command);
		this.droppedNodeId = droppedNodeId;
		this.targetNodeId = targetNodeId;
		this.asChild = asChild;
	}

	/**
	 * @return the dropped nodeId
	 */
	public String getDroppedNodeId() {
		return droppedNodeId;
	}
	
	/**
	 * @return The targeted node id
	 */
	public String getTargetNodeId() {
		return targetNodeId;
	}
	
	
	public boolean isAsChild() {
		return asChild;
	}

	@Override
	public String toString() {
		return "TreeDropEvent:{cmd:"+getCommand()+", droppedNodeId:"+droppedNodeId+", targetNodeId:"+targetNodeId+"}";
	}
}