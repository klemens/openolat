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
package org.olat.core.gui.components.form.flexible.impl.elements.table;


import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 15.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StaticFlexiCellRenderer implements FlexiCellRenderer {

	private String label;
	private String action;
	private String iconCSS;
	private String linkCSS;
	private String linkTitle; 
	private FlexiCellRenderer labelDelegate;
	
	public StaticFlexiCellRenderer(String label, String action) {
		this(label, action, null, null, null);
	}
	
	public StaticFlexiCellRenderer(String label, String action, String linkCSS, String iconCSS) {
		this(label, action, linkCSS, iconCSS, null);
	}
	
	public StaticFlexiCellRenderer(String label, String action, String linkCSS, String iconCSS, String linkTitle) {
		this.label = label;
		this.action = action;
		this.linkCSS = linkCSS;
		this.iconCSS = iconCSS;
		this.linkTitle = linkTitle;
	}
	
	public StaticFlexiCellRenderer(String action, FlexiCellRenderer labelDelegate) {
		this.labelDelegate = labelDelegate;
		this.action = action;
	}

  /**
   * 
   * @param target
 * @param cellValue
 * @param translator
   */	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		
		String cellAction = getAction();
		if(StringHelper.containsNonWhitespace(cellAction)) {
			FlexiTableElementImpl ftE = source.getFlexiTableElement();
			String id = source.getFormDispatchId();
			Form rootForm = ftE.getRootForm();
			NameValuePair pair = new NameValuePair(cellAction, Integer.toString(row));
			String jsCode = FormJSHelper.getXHRFnCallFor(rootForm, id, 1, pair);
			target.append("<a href=\"javascript:").append(jsCode).append("\"");
			if(StringHelper.containsNonWhitespace(linkTitle)) {
				target.append(" title=\"").append(StringEscapeUtils.escapeHtml(linkTitle)).append("\"");
			}
			if(StringHelper.containsNonWhitespace(linkCSS)) {
				target.append(" class=\"").append(linkCSS).append("\"");
			}
			target.append(" onclick=\"return o2cl();\">");
			if(StringHelper.containsNonWhitespace(iconCSS)) {
				target.append("<i class=\"o_icon ").append(iconCSS).append("\">&nbsp;</i>");
			}
			if(labelDelegate == null) {
				target.append(getLabel());
			} else {
				labelDelegate.render(renderer, target, cellValue, row, source, ubu, translator);
			}
			target.append("</a>");
		} else if(labelDelegate == null) {
			target.append(getLabel());
		} else {
			labelDelegate.render(renderer, target, cellValue, row, source, ubu, translator);
		}
	}
	
	protected String getAction() {
		return action;
	}
	
	protected String getLabel() {
		return label;
	}
}
