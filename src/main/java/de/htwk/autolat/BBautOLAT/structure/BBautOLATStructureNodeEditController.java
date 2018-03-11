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

import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.st.EditScoreCalculationEasyForm;
import org.olat.course.run.scoring.ScoreCalculator;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;

/**
 * Edit controller for the autolat structure node with support for
 * changing accessability and score calculation
 * 
 * The normal openolat structure node additionally supports specifying
 * the displayed content (static or some sort of children overview).
 * However, the autolat structure node always displays the score and
 * highscore lists.
 */
public class BBautOLATStructureNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	private static final String PANE_TAB_ST_SCORECALCULATION = "pane.tab.st_scorecalculation";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	
	private static final String[] paneKeys = { PANE_TAB_ST_SCORECALCULATION, PANE_TAB_ACCESSIBILITY };
	private BBautOLATStructureNode stNode;
	private EditScoreCalculationExpertForm scoreExpertForm;
	private EditScoreCalculationEasyForm scoreEasyForm;
	private List<CourseNode> assessableChildren;
	
	private VelocityContainer score;
	private Link activateEasyModeButton;
	private Link activateExpertModeButton;

	private ConditionEditController accessibilityCondContr;

	private UserCourseEnvironment euce;
	private TabbedPane myTabbedPane;

	public BBautOLATStructureNodeEditController(UserRequest ureq, WindowControl wControl, BBautOLATStructureNode bBautOLATStructureNode, VFSContainer courseFolderContainer,
			CourseGroupManager groupMgr, CourseEditorTreeModel editorModel, UserCourseEnvironment euce) {
		super(ureq, wControl);

		this.stNode = bBautOLATStructureNode;
		this.euce = euce;

		Translator translator = Util.createPackageTranslator(BBautOLATStructureNodeEditController.class, Condition.class, ureq.getLocale());
		setTranslator(translator);
				
		score = this.createVelocityContainer("scoreedit");
		activateEasyModeButton = LinkFactory.createButtonSmall("cmd.activate.easyMode", score, this);
		activateExpertModeButton = LinkFactory.createButtonSmall("cmd.activate.expertMode", score, this);
				
		// Find assessable children nodes
		assessableChildren = AssessmentHelper.getAssessableNodes(editorModel, bBautOLATStructureNode);

		// Accessibility precondition
		Condition accessCondition = bBautOLATStructureNode.getPreConditionAccess();
		accessibilityCondContr = new ConditionEditController(ureq, getWindowControl(), euce, accessCondition, assessableChildren);
		this.listenTo(accessibilityCondContr);

		ScoreCalculator scoreCalc = bBautOLATStructureNode.getScoreCalculator();
		if (scoreCalc != null) {
			if (scoreCalc.isExpertMode() && scoreCalc.getPassedExpression() == null && scoreCalc.getScoreExpression() == null) {
				scoreCalc = null;
			} else if (!scoreCalc.isExpertMode() && scoreCalc.getPassedExpressionFromEasyModeConfiguration() == null
					&& scoreCalc.getScoreExpressionFromEasyModeConfiguration() == null) {
				scoreCalc = null;
			}
		}

		if (assessableChildren.size() == 0 && scoreCalc == null) {
			// show only the no assessable children message, if no previous score
			// config exists.
			score.contextPut("noAssessableChildren", Boolean.TRUE);
		} else {
			score.contextPut("noAssessableChildren", Boolean.FALSE);
		}

		// Init score calculator form
		if (scoreCalc != null && scoreCalc.isExpertMode()) {
			initScoreExpertForm(ureq);
		} else {
			initScoreEasyForm(ureq);
		}
	}

	/**
	 * Initialize an easy mode score calculator form and push it to the score
	 * velocity container
	 */
	private void initScoreEasyForm(UserRequest ureq) {
		removeAsListenerAndDispose(scoreEasyForm);
		scoreEasyForm = new EditScoreCalculationEasyForm(ureq, getWindowControl(), stNode.getScoreCalculator(), assessableChildren);
		listenTo(scoreEasyForm);
		score.put("scoreForm", scoreEasyForm.getInitialComponent());
		score.contextPut("isExpertMode", Boolean.FALSE);
	}

	/**
	 * Initialize an expert mode score calculator form and push it to the score
	 * velocity container
	 */
	private void initScoreExpertForm(UserRequest ureq) {
		removeAsListenerAndDispose(scoreExpertForm);
		scoreExpertForm = new EditScoreCalculationExpertForm(ureq, getWindowControl(), stNode.getScoreCalculator(), euce, assessableChildren);
		listenTo(scoreExpertForm);
		scoreExpertForm.setScoreCalculator(stNode.getScoreCalculator());
		score.put("scoreForm", scoreExpertForm.getInitialComponent());
		score.contextPut("isExpertMode", Boolean.TRUE);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == activateEasyModeButton) {
			initScoreEasyForm(ureq);
		} else if (source == activateExpertModeButton){
			initScoreExpertForm(ureq);
		}
	}
	
	/**
	 * 
	 * @param nodeDescriptions
	 * @return the warning message if any, null otherwise
	 */
	
	private String getWarningMessage(List<String> nodeDescriptions) {
		if(nodeDescriptions.size()>0) {			
			String invalidNodeTitles = "";
			Iterator<String> titleIterator = nodeDescriptions.iterator();
			while(titleIterator.hasNext()) {
				if(!invalidNodeTitles.equals("")) {
					invalidNodeTitles += "; ";
				}
				invalidNodeTitles += titleIterator.next();
			}	
			return translate("scform.error.configuration") + ": " + invalidNodeTitles;
		}
		return null;
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessibilityCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessibilityCondContr.getCondition();
				stNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == scoreEasyForm) {
			
			if (event == Event.DONE_EVENT) {	
				//show warning if the score might be wrong because of the invalid nodes used for calculation
				List<String> testElemWithNoResource = scoreEasyForm.getInvalidNodeDescriptions();
				String msg = getWarningMessage(testElemWithNoResource);
				if(msg!=null) {								
					showWarning(msg);
				}

				ScoreCalculator sc = scoreEasyForm.getScoreCalulator();
				/*
				 * OLAT-1144 bug fix if Calculation Score -> NO and Calculate passing
				 * score -> NO we get a ScoreCalculator == NULL !
				 */
				if (sc != null) {
					sc.setPassedExpression(sc.getPassedExpressionFromEasyModeConfiguration());
					sc.setScoreExpression(sc.getScoreExpressionFromEasyModeConfiguration());
				}
				// ..setScoreCalculator(sc) can handle NULL values!
				stNode.setScoreCalculator(sc);
				initScoreEasyForm(ureq); // reload form, remove deleted nodes
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			} else if (event == Event.CANCELLED_EVENT) { // reload form
				initScoreEasyForm(ureq);
			}
		} else if (source == scoreExpertForm) {
			if (event == Event.DONE_EVENT) {
        //show warning if the score might be wrong because of the invalid nodes used for calculation
				List<String> testElemWithNoResource = scoreExpertForm.getInvalidNodeDescriptions();
				String msg = getWarningMessage(testElemWithNoResource);
				if(msg!=null) {								
					getWindowControl().setWarning(msg);
				}
				
				ScoreCalculator sc = scoreExpertForm.getScoreCalulator();
				/*
				 * OLAT-1144 bug fix if a ScoreCalculator == NULL !
				 */
				if (sc != null) {
					sc.clearEasyMode();
				}
				// ..setScoreCalculator(sc) can handle NULL values!
				stNode.setScoreCalculator(sc);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			} else if (event == Event.CANCELLED_EVENT) { // reload form
				initScoreExpertForm(ureq);
			}
		}
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
		tabbedPane.addTab(translate(PANE_TAB_ST_SCORECALCULATION), score);
	}

	@Override
	protected void doDispose() {
    //child controllers registered with listenTo() get disposed in BasicController
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

}