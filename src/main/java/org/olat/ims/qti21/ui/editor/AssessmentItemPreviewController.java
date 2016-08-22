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
package org.olat.ims.qti21.ui.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.manager.audit.DefaultAssessmentSessionAuditLogger;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.ui.AssessmentItemDisplayController;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 20.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemPreviewController extends BasicController {
	
	
	private AssessmentItemDisplayController displayCtrl;
	
	private final VelocityContainer mainVC;
	private final AssessmentSessionAuditLogger candidateAuditLogger = new PreviewAuditLogger();
	

	private AssessmentItemPreviewController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentTestDisplayController.class, getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("assessment_item_preview");
		mainVC.contextPut("outcomes", new ArrayList<>());
		mainVC.contextPut("responses", new ArrayList<>());
		putInitialPanel(mainVC);
	}
	
	public AssessmentItemPreviewController(UserRequest ureq, WindowControl wControl,
			ResolvedAssessmentItem resolvedAssessmentItem, File rootDirectory, File itemFile) {
		this(ureq, wControl);

		displayCtrl = new AssessmentItemDisplayController(ureq, getWindowControl(),
				resolvedAssessmentItem, rootDirectory, itemFile, candidateAuditLogger);
		listenTo(displayCtrl);
		mainVC.put("display", displayCtrl.getInitialComponent());
	}
	
	public AssessmentItemPreviewController(UserRequest ureq, WindowControl wControl,
			ResolvedAssessmentItem resolvedAssessmentItem, AssessmentItemRef itemRef,
			RepositoryEntry testEntry, AssessmentEntry assessmentEntry,
			File rootDirectory, File itemFile) {
		this(ureq, wControl);

		displayCtrl = new AssessmentItemDisplayController(ureq, getWindowControl(),
				testEntry, assessmentEntry, true, resolvedAssessmentItem, itemRef,
				rootDirectory, itemFile, candidateAuditLogger);
		listenTo(displayCtrl);
		mainVC.put("display", displayCtrl.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public class PreviewAuditLogger extends DefaultAssessmentSessionAuditLogger {

		@Override
		public void logCandidateEvent(CandidateEvent candidateEvent, Map<Identifier, AssessmentResponse> candidateResponseMap) {
			List<IdentifierToStringuifiedValue> responses = new ArrayList<>();
			for (Map.Entry<Identifier, AssessmentResponse> responseEntry:candidateResponseMap.entrySet()) {
				Identifier identifier = responseEntry.getKey();
				String stringuifiedValue = responseEntry.getValue().getStringuifiedResponse();
				responses.add(new IdentifierToStringuifiedValue(identifier.toString(), stringuifiedValue));
			}
			Collections.sort(responses);
			mainVC.contextPut("responses", responses);
			mainVC.setDirty(true);
		}

		@Override
		public void logCandidateOutcomes(AssessmentTestSession candidateSession, Map<Identifier, String> outcomeMap) {
			List<IdentifierToStringuifiedValue> outcomes = new ArrayList<>();
			for (Map.Entry<Identifier, String> responseEntry:outcomeMap.entrySet()) {
				Identifier identifier = responseEntry.getKey();
				String stringuifiedValue = responseEntry.getValue();
				outcomes.add(new IdentifierToStringuifiedValue(identifier.toString(), stringuifiedValue));
			}
			Collections.sort(outcomes);
			mainVC.contextPut("outcomes", outcomes);
			mainVC.setDirty(true);
		}
	}
	
	public class IdentifierToStringuifiedValue implements Comparable<IdentifierToStringuifiedValue> {
		
		private String identifier;
		private String stringuifiedValue;
		
		public IdentifierToStringuifiedValue(String identifier, String stringuifiedValue) {
			this.identifier = identifier;
			this.stringuifiedValue = stringuifiedValue;
		}
		
		public String getIdentifier() {
			return identifier;
		}

		public String getStringuifiedValue() {
			return stringuifiedValue;
		}

		@Override
		public int compareTo(IdentifierToStringuifiedValue o) {
			return identifier.compareToIgnoreCase(o.getIdentifier());
		}	
	}
}