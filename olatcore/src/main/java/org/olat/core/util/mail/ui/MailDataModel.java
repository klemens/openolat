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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.model.DBMailImpl;
import org.olat.core.util.mail.model.DBMailRecipient;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  28 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailDataModel implements TableDataModel {
	
	private List<DBMailImpl> mails;
	private List<DBMailImpl> filteredMails;
	private final Identity identity;
	private final Formatter formatter;
	private final Translator translator;
	private final Map<String,String> bpToContexts;
	
	public MailDataModel(List<DBMailImpl> mails, Map<String,String> bpToContexts, Identity identity,
			Translator translator, Formatter formatter) {
		this.mails = mails;
		this.bpToContexts = bpToContexts;
		this.identity = identity;
		this.formatter = formatter;
		this.translator = translator;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return filteredMails == null ? mails.size() : filteredMails.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		DBMailImpl mail = filteredMails == null ? mails.get(row) : filteredMails.get(row);
		Columns[] cols = Columns.values();
		if(col < cols.length) {
			switch(cols[col]) {
				case read: {
					for(DBMailRecipient recipient:mail.getRecipients()) {
						if(recipient != null && recipient.getRecipient() != null && recipient.getRecipient().equalsByPersistableKey(identity)) {
							return recipient.getRead();
						}
					}
					return Boolean.FALSE;
				}
				case marked: {
					for(DBMailRecipient recipient:mail.getRecipients()) {
						if(recipient != null && recipient.getRecipient() != null && recipient.getRecipient().equalsByPersistableKey(identity)) {
							return recipient.getMarked();
						}
					}
					return Boolean.FALSE;
				}
				case context: {
					String businessPath = mail.getContext().getBusinessPath();
					if(StringHelper.containsNonWhitespace(businessPath)) {
						String contextName = bpToContexts.get(businessPath);
						if(StringHelper.containsNonWhitespace(businessPath)) {
							return new ContextPair(contextName, businessPath);
						}
					}
					return null;
				}
				case subject: return mail.getSubject();
				case receivedDate:
				case sendDate: {
					return mail.getCreationDate();
				}
				case from: {
					DBMailRecipient from = mail.getFrom();
					if(from != null ) {
						if(from.getRecipient() != null) {
							return from.getRecipient();
						} else if (StringHelper.containsNonWhitespace(from.getGroup())) {
							return from.getGroup();
						} else if (StringHelper.containsNonWhitespace(from.getEmailAddress())) {
							return from.getEmailAddress();
						}
					}
					return "-";
				}
				case recipients: {
					if(StringHelper.containsNonWhitespace(mail.getMetaId())) {
						return translator.translate("mail.from.miscellaneous");
					}
					
					StringBuilder sb = new StringBuilder();
					for(DBMailRecipient recipient:mail.getRecipients()) {
						if(recipient.getGroup() != null) {
							if(sb.length() > 0) sb.append(", ");
							sb.append(recipient.getGroup());
						}
					}
					return sb.toString();
				}
			}
		}
		return mail;
	}

	@Override
	public Object getObject(int row) {
		return filteredMails == null ? mails.get(row) : filteredMails.get(row);
	}

	@Override
	public void setObjects(List objects) {
		mails = objects;
		filteredMails = null;
	}
	
	public void replace(DBMailImpl mail) {
		int index = mails.indexOf(mail);
		if(index >= 0 && index < mails.size()) {
			mails.set(index, mail);
		}
	}
	
	public void filter(MailContextShortName filter) {
		if(filter == null || !StringHelper.containsNonWhitespace(filter.getBusinessPath())) {
			filteredMails = null;
		} else {
			filteredMails = new ArrayList<DBMailImpl>();
			for(DBMailImpl mail:mails) {
				if(filter.getBusinessPath().equals(mail.getContext().getBusinessPath())) {
					filteredMails.add(mail);
				}
			}
		}
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new MailDataModel(Collections.<DBMailImpl>emptyList(), bpToContexts, identity, translator, formatter);
	}
	
	public enum Columns {
		read("mail.read.header"),
		marked("mail.marked.header"),
		context("mail.context"),
		subject("mail.subject"),
		sendDate("mail.sendDate"),
		receivedDate("mail.receivedDate"),
		from("mail.from"),
		recipients("mail.recipients");
		
		
		private final String i18nKey;
		
		private Columns(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
	
	public class ContextPair {
		private final String name;
		private final String businessPath;
		
		public ContextPair(String name, String businessPath) {
			this.name = name;
			this.businessPath = businessPath;
		}
		
		public String getName() {
			return name;
		}
		public String getBusinessPath() {
			return businessPath;
		}
		
		
		
	}
}
