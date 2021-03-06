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
package org.olat.modules.portfolio.ui.editor.event;

import org.olat.core.gui.control.Event;
import org.olat.modules.portfolio.ui.editor.PageEditorController.EditorFragment;
import org.olat.modules.portfolio.ui.editor.PageElementHandler;
import org.olat.modules.portfolio.ui.editor.PageElementTarget;

/**
 * 
 * Initial date: 04.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddElementEvent extends Event {

	private static final long serialVersionUID = 7073690795238863754L;

	private final PageElementTarget target;
	private final PageElementHandler handler;
	private final EditorFragment referenceFragment;
	
	public AddElementEvent(EditorFragment referenceFragment, PageElementHandler handler, PageElementTarget target) {
		super("pf-add-above-element");
		this.target = target;
		this.handler = handler;
		this.referenceFragment = referenceFragment;
	}

	public PageElementTarget getTarget() {
		return target;
	}

	public PageElementHandler getHandler() {
		return handler;
	}

	public EditorFragment getReferenceFragment() {
		return referenceFragment;
	}
}
