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

package org.olat.course.nodes.members;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.MembersCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for MembersCourseNodeEditController
 * 
 * <P>
 * Initial Date:  11 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MembersCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
	private static final String[] paneKeys = {};
	
	private final MembersCourseNode courseNode;

	private TabbedPane myTabbedPane;

	
	public MembersCourseNodeEditController(UserRequest ureq, WindowControl wControl, MembersCourseNode courseNode, ICourse course,
			UserCourseEnvironment euce) {
		super(ureq,wControl);
		
		this.courseNode = courseNode;
		
		CourseGroupManager groupMgr = course.getCourseEnvironment().getCourseGroupManager();

	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}
	
	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
