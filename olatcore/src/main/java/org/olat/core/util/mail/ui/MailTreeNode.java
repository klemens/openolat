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

package org.olat.core.util.mail.ui;

import java.util.Locale;

import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailInboxActionExtension;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.MailOutboxActionExtension;
import org.olat.core.util.mail.manager.MailManager;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  24 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailTreeNode extends GenericTreeNode {

	private static final long serialVersionUID = -2356354555358474675L;

	private final Identity identity;
	private final Translator translator;

	private GenericTreeNode inbox;
	private GenericTreeNode outbox;
	
	public MailTreeNode(Identity identity, Locale locale, MailContextResolver resolver) {
		this(null, null, identity, locale, resolver);
	}
	
	public MailTreeNode(String title, Object userObject, Identity identity, Locale locale, MailContextResolver resolver) {
		super(title , userObject);

		this.identity = identity;
		translator = Util.createPackageTranslator(MailModule.class, locale);
		inbox = new GenericTreeNode(translator.translate("mail.inbox"), new MailInboxActionExtension(resolver));
		outbox = new GenericTreeNode(translator.translate("mail.outbox"), new MailOutboxActionExtension(resolver));
		
		addChild(inbox);
		addChild(outbox);
		setDelegate(inbox);
	}

	@Override
	public String getIconCssClass() {
		return "o_co_icon";
	}

	@Override
	public String getIconDecorator1CssClass() {
		if(MailManager.getInstance().hasNewMail(identity)) {
			return "b_mail_new";
		}
		return null;
	}
}
