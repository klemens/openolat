/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21.ui.components;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;

/**
 * 
 * Initial date: 19.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InteractionResultFormItem extends AssessmentObjectFormItem {
	
	private final InteractionResultComponent component;
	
	public InteractionResultFormItem(String name, Interaction interaction, ResolvedAssessmentItem resolvedAssessmentItem) {
		super(name, null);
		component = new InteractionResultComponent(name + "_cmp", interaction, resolvedAssessmentItem, this);
	}
	
	public ResolvedAssessmentTest getResolvedAssessmentTest() {
		return component.getResolvedAssessmentTest();
	}

	public void setResolvedAssessmentTest(ResolvedAssessmentTest resolvedAssessmentTest) {
		component.setResolvedAssessmentTest(resolvedAssessmentTest);
	}
	
	public TestSessionController getTestSessionController() {
		return component.getTestSessionController();
	}

	public void setTestSessionController(TestSessionController testSessionController) {
		component.setTestSessionController(testSessionController);
	}

	public ItemSessionState getItemSessionState() {
		return component.getItemSessionState();
	}

	public void setItemSessionState(ItemSessionState itemSessionState) {
		component.setItemSessionState(itemSessionState);
	}

	public boolean isShowSolution() {
		return component.isShowSolution();
	}

	public void setShowSolution(boolean showSolution) {
		component.setShowSolution(showSolution);
	}
	
	public boolean isShowCorrectSolution() {
		return component.isShowCorrectSolution();
	}
	
	public void setShowCorrectSolution(boolean showCorrectSolution) {
		component.setShowCorrectSolution(showCorrectSolution);
	}

	@Override
	public InteractionResultComponent getComponent() {
		return component;
	}
	

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void reset() {
		//
	}
}