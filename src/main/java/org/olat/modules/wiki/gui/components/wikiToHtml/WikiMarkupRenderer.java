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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.modules.wiki.gui.components.wikiToHtml;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

import org.jamwiki.parser.AbstractParser;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.JFlexParser;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.Formatter;

/**
 * Description:<br>
 * render part of the component, where the html output surrounding the
 * transformed wiki syntax gets added
 * <P>
 * Initial Date: May 17, 2006 <br>
 * 
 * @author guido
 */
public class WikiMarkupRenderer implements ComponentRenderer {

	protected WikiMarkupRenderer() {
	//
	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		WikiMarkupComponent wikiComp = (WikiMarkupComponent) source;
		
		AJAXFlags flags = renderer.getGlobalSettings().getAjaxFlags();
		boolean iframePostEnabled = flags.isIframePostEnabled();
		
		//ParserInput parserInput = wikiComp.getParserInput();
		
		
		
		ParserInput input = new ParserInput();
    input.setWikiUser(null);
    input.setAllowSectionEdit(false);
    input.setDepth(10);
    input.setContext("");
    //input.setTableOfContents(null);
    input.setLocale(new Locale("en"));
    //input.setVirtualWiki(Long.toString(wikiComp.getOres().getResourceableId()));
    input.setTopicName("dummy");
    input.setUserIpAddress("0.0.0.0");
    OlatWikiDataHandler dataHandler = new OlatWikiDataHandler(wikiComp.getOres(), wikiComp.getImageBaseUri());
    input.setDataHandler(dataHandler);
    
    StringOutput out = new StringOutput(100);
		ubu.buildURI(out, null , null, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		String uri = out.toString();
		
		ParserDocument parsedDoc = null;

		
		
		String uniqueId = "o_wiki"+wikiComp.getDispatchID();
		try {
			uri = URLDecoder.decode(uri, "utf-8");
			input.setVirtualWiki(uri.substring(1, uri.length()-1));
			if (iframePostEnabled) {
				StringOutput so = new StringOutput();
				ubu.appendTarget(so);
				input.setURLTarget(so.toString());
			}
			sb.append("<div style=\"min-height:"+ wikiComp.getMinHeight() +"px\" id=\"");
			sb.append(uniqueId);
			sb.append("\">");
		
			AbstractParser parser = new JFlexParser(input);
			parsedDoc = parser.parseHTML(wikiComp.getWikiContent());
		} catch (UnsupportedEncodingException e) {
			//encoding utf-8 should be ok
		} catch (Exception e) {
			throw new OLATRuntimeException(this.getClass(), "error while rendering wiki page with content:"+ wikiComp.getWikiContent(), e);
		}
		// Use global js math formatter for latex formulas
		sb.append(Formatter.formatLatexFormulas(parsedDoc.getContent()));
		sb.append("</div>");
		//set targets of media, image and external links to target "_blank" 
		sb.append("<script type=\"text/javascript\">/* <![CDATA[ */ ");
		sb.append("changeAnchorTargets('").append(uniqueId).append("');");
		sb.append("/* ]]> */</script>");
	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderingState)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
		//
	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.RenderingState)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
	//
	}

}
