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

package org.olat.core.util.mail.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailerTest;
import org.olat.core.util.mail.model.DBMail;
import org.olat.core.util.mail.model.DBMailAttachment;
import org.olat.core.util.mail.model.DBMailAttachmentData;
import org.olat.core.util.mail.model.DBMailRecipient;
import org.olat.test.JunitTestHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  24 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailManagerTest extends MailerTest {
	
	private static Identity ident1, ident2, ident3, ident4;
	private static boolean isInitialized = false;
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private MailManager mailManager;
	
	@Before
	public void setUp() {
		super.setup();
		mailModule.setInterSystem(true);
		
		if(!isInitialized) {
			ident1 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
			ident2 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
			ident3 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
			ident4 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
		}
	}
	
	@Test
	public void testSaveMail() {
		List<ContactList> contacts = createContactLists("TO", ident2);
		MailContext context = createMailContext();
		DBMail mail = mailManager.saveDBMessage(context, ident1, null, null, null, null, null, contacts, null, "Test save mail", "Body of test save mail", null, null);
		
		dbInstance.commitAndCloseSession();
		
		DBMail persistedMail = mailManager.getMessageByKey(mail.getKey());
		assertNotNull(persistedMail);
		assertEquals("Test save mail", persistedMail.getSubject());
		assertEquals("Body of test save mail", persistedMail.getBody());
		assertNotNull(persistedMail.getCreationDate());
		assertNotNull(persistedMail.getLastModified());
		assertNotNull(persistedMail.getRecipients());
		assertEquals(1, persistedMail.getRecipients().size());
		assertEquals(ident2.getKey(), persistedMail.getRecipients().get(0).getRecipient().getKey());
		//check context
		assertNotNull(persistedMail.getContext());
		assertNotNull(persistedMail.getContext().getOLATResourceable());
		assertEquals(persistedMail.getContext().getOLATResourceable().getResourceableId(),
				persistedMail.getContext().getOLATResourceable().getResourceableId());
		assertEquals(persistedMail.getContext().getOLATResourceable().getResourceableTypeName(),
				persistedMail.getContext().getOLATResourceable().getResourceableTypeName());
		assertEquals(context.getResSubPath(), persistedMail.getContext().getResSubPath());
		assertEquals(context.getBusinessPath(), persistedMail.getContext().getBusinessPath());
	}
	
	@Test
	public void testOutbox() {
		//create a mail
		List<ContactList> contacts = createContactLists("TO", ident2);
		mailManager.saveDBMessage(null, ident1, null, null, null, null, null, contacts, null, "Test save mail identity 1", "Body of test save mail identity 1", null, null);

		List<ContactList> contacts2 = createContactLists("TO", ident1);
		mailManager.saveDBMessage(null, ident2, null, null, null, null, null, contacts2, null, "Test save mail identity 2", "Body of test save mail identity 2", null, null);

		dbInstance.commitAndCloseSession();
		
		List<DBMail> outbox = mailManager.getOutbox(ident1, 0, -1);
		assertFalse(outbox.isEmpty());
		for(DBMail mail:outbox) {
			assertNotNull(mail.getFrom());
			assertNotNull(mail.getFrom().getRecipient());
			assertEquals(ident1.getKey(), mail.getFrom().getRecipient().getKey());
		}
	}
	
	@Test
	public void testInbox() {
		//create a mail
		List<ContactList> contacts = createContactLists("TO", ident2);
		mailManager.saveDBMessage(null, ident1, null, null, null, null, null, contacts, null, "Test inbox identity 1", "Body of test inbox identity 1", null, null);

		List<ContactList> contacts2 = createContactLists("TO", ident1);
		mailManager.saveDBMessage(null, ident2, null, null, null, null, null, contacts2, null, "Test inbox identity 2", "Body of test inbox identity 2", null, null);

		dbInstance.commitAndCloseSession();

		List<DBMail> inbox = mailManager.getInbox(ident1, null, Boolean.FALSE, null, 0, -1);
		assertFalse(inbox.isEmpty());
		
		dbInstance.commitAndCloseSession();
		
		for(DBMail mailShort:inbox) {
			DBMail mail = mailManager.getMessageByKey(mailShort.getKey());
			assertNotNull(mail.getFrom());
			assertFalse(mail.getRecipients().isEmpty());
			assertEquals(ident1.getKey(), mail.getRecipients().get(0).getRecipient().getKey());
		}
	}
	
	@Test
	public void testRecipient() {
		DBMailRecipient recipient = new DBMailRecipient();
		recipient.setDeleted(Boolean.TRUE);
		recipient.setEmailAddress("contact@frentix.com");
		recipient.setGroup("My group");
		recipient.setMarked(Boolean.TRUE);
		recipient.setRead(Boolean.TRUE);
		recipient.setRecipient(ident1);
		recipient.setVisible(Boolean.TRUE);
		
		dbInstance.saveObject(recipient);
		dbInstance.commitAndCloseSession();

		DBMailRecipient stored = (DBMailRecipient)dbInstance.loadObject(DBMailRecipient.class, recipient.getKey());
		assertEquals(Boolean.TRUE, stored.getDeleted());
		assertEquals("contact@frentix.com", stored.getEmailAddress());
		assertEquals("My group", stored.getGroup());
		assertEquals(Boolean.TRUE, stored.getMarked());
		assertEquals(Boolean.TRUE, stored.getRead());
		assertEquals(Boolean.TRUE, stored.getVisible());
		assertNotNull(stored.getRecipient());
		assertEquals(ident1.getKey(), stored.getRecipient().getKey());
	}
	
	@Test
	public void testAttachment() throws URISyntaxException {
		//save a mail with attachments
		URL portraitUrl = MailManagerTest.class.getResource("portrait.jpg");
		assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		List<File> attachments = Collections.singletonList(portrait);

		List<ContactList> contacts = createContactLists("TO", ident2);
		DBMail mail = mailManager.saveDBMessage(null, ident1, null, null, null, null, null, contacts, null, "Test save mail identity 1", "Body of test save mail identity 1", attachments, null);
		assertNotNull(mail);
		
		dbInstance.commitAndCloseSession();
		
		//retrieve the attachments without data
		List<DBMailAttachment> persistedAttachments = mailManager.getAttachments(mail);
		assertNotNull(persistedAttachments);
		assertFalse(persistedAttachments.isEmpty());
		assertEquals(1, persistedAttachments.size());
		
		DBMailAttachment persistedAttachment = persistedAttachments.get(0);
		assertNotNull(persistedAttachment);
		assertNotNull(persistedAttachment.getMail());
		assertNotNull(persistedAttachment.getMimetype());
		assertNotNull(persistedAttachment.getName());
		assertNotNull(persistedAttachment.getSize());
		
		assertEquals(mail, persistedAttachment.getMail());
		assertTrue("image/jpeg".equals(persistedAttachment.getMimetype()) || "image/jpg".equals(persistedAttachment.getMimetype()));
		assertEquals("portrait.jpg", persistedAttachment.getName());
		assertEquals(new Long(portrait.length()), persistedAttachment.getSize());

		dbInstance.commitAndCloseSession();
		
		//retrieve the attachments with data
		DBMailAttachmentData storedAttachment = mailManager.getAttachmentWithData(persistedAttachment.getKey());
		assertNotNull(storedAttachment);
		assertNotNull(storedAttachment.getMail());
		assertNotNull(storedAttachment.getMimetype());
		assertNotNull(storedAttachment.getName());
		assertNotNull(storedAttachment.getSize());
		assertNotNull(storedAttachment.getDatas());
		
		assertEquals(mail, storedAttachment.getMail());
		assertTrue("image/jpeg".equals(storedAttachment.getMimetype()) || "image/jpg".equals(storedAttachment.getMimetype()));
		assertEquals("portrait.jpg", storedAttachment.getName());
		assertEquals(new Long(portrait.length()), storedAttachment.getSize());
		assertEquals(new Long(portrait.length()), new Long(storedAttachment.getDatas().length));
	}
	
	@Test
	public void testDeleteMail() throws URISyntaxException {
		//save a mail with attachments
		URL portraitUrl = MailManagerTest.class.getResource("portrait.jpg");
		assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		List<File> attachments = Collections.singletonList(portrait);

		List<ContactList> contacts = createContactLists("TO", ident2);
		DBMail mail = mailManager.saveDBMessage(null, ident1, null, null, null, null, null, contacts, null, "Test save mail identity 1", "Body of test save mail identity 1", attachments, null);
		assertNotNull(mail);
		
		dbInstance.commitAndCloseSession();
		
		List<DBMailAttachment> persistedAttachments = mailManager.getAttachments(mail);
		assertNotNull(persistedAttachments);
		assertEquals(1, persistedAttachments.size());
		DBMailAttachment persistedAttachment = persistedAttachments.get(0);

		dbInstance.commitAndCloseSession();
		
		//ident1 as from delete the mail in its outbox
		List<DBMail> outbox = mailManager.getOutbox(ident1, 0, 0);
		
		DBMail outboxMail = null;
		for(DBMail outMail:outbox) {
			if(outMail.equals(mail)) {
				outboxMail = outMail;
				break;
			}
		}
		assertNotNull(outboxMail);
		mailManager.delete(outboxMail, ident1, false);
		
		dbInstance.commitAndCloseSession();
		
		//check if the mail exists for ident2
		DBMail checkMailExist = mailManager.getMessageByKey(mail.getKey());
		assertNotNull(checkMailExist);
		List<DBMailAttachment> checkAttachmentExist = mailManager.getAttachments(checkMailExist);
		assertNotNull(checkAttachmentExist);
		assertEquals(1, checkAttachmentExist.size());
		
		//check if the mail doesn't exist for ident1 anymore
		List<DBMail> outboxDeleted = mailManager.getOutbox(ident1, 0, 0);
		DBMail checkOutboxMailDeleted = null;
		for(DBMail outMail:outboxDeleted) {
			if(outMail.equals(mail)) {
				checkOutboxMailDeleted = outMail;
				break;
			}
		}
		assertNull(checkOutboxMailDeleted);
		
		dbInstance.commitAndCloseSession();
		
		//ident2 as receiver delete the mail in its inbox
		List<DBMail> inbox = mailManager.getInbox(ident2, null, Boolean.FALSE, null, 0, 0);
		DBMail inboxMail = null;
		for(DBMail inMail:inbox) {
			if(inMail.equals(mail)) {
				inboxMail = inMail;
				break;
			}
		}
		assertNotNull(inboxMail);
		mailManager.delete(inboxMail, ident2, false);
		
		dbInstance.commitAndCloseSession();
		
		//check ident2 inbox
		List<DBMail> inboxDeleted = mailManager.getInbox(ident2, null, Boolean.FALSE, null, 0, 0);
		DBMail inboxMailDeleted = null;
		for(DBMail inMail:inboxDeleted) {
			if(inMail.equals(mail)) {
				inboxMailDeleted = inMail;
				break;
			}
		}
		assertNull(inboxMailDeleted);
		
		//check if the mail deleted
		DBMail checkMailDeleted = mailManager.getMessageByKey(mail.getKey());
		assertNull(checkMailDeleted);
		List<DBMailAttachment> checkAttachmentDeleted = mailManager.getAttachments(checkMailExist);
		assertNotNull(checkAttachmentDeleted);
		assertTrue(checkAttachmentDeleted.isEmpty());
	}
	
	@Test
	public void testMetaMail() throws URISyntaxException {
		//save a mail with attachments
		URL portraitUrl = MailManagerTest.class.getResource("portrait.jpg");
		assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		List<File> attachments = Collections.singletonList(portrait);
		
		String metaId = UUID.randomUUID().toString().replace("-", "");
		
		List<ContactList> contact2 = createContactLists("TO", ident2);
		DBMail mail2 = mailManager.saveDBMessage(null, ident1, null, null, null, null, null, contact2, metaId, "Test meta mail 2", "Body of test meta mail 2", attachments, null);
		assertNotNull(mail2);

		List<ContactList> contact3 = createContactLists("TO", ident3);
		DBMail mail3 = mailManager.saveDBMessage(null, ident1, null, null, null, null, null, contact3, metaId, "Test meta mail 3", "Body of test meta mail 3", attachments, null);
		assertNotNull(mail3);
		
		List<ContactList> contact4 = createContactLists("TO", ident4);
		DBMail mail4 = mailManager.saveDBMessage(null, ident1, null, null, null, null, null, contact4, metaId, "Test meta mail 3", "Body of test meta mail 3", attachments, null);
		assertNotNull(mail4);
		
		dbInstance.commitAndCloseSession();
		
		//load the attachments
		List<DBMailAttachment> persistedAttachments = mailManager.getAttachments(mail2);
		assertNotNull(persistedAttachments);
		assertEquals(1, persistedAttachments.size());
		DBMailAttachment persistedAttachment2 = persistedAttachments.get(0);
		
		persistedAttachments = mailManager.getAttachments(mail3);
		assertNotNull(persistedAttachments);
		assertEquals(1, persistedAttachments.size());
		DBMailAttachment persistedAttachment3 = persistedAttachments.get(0);
		
		persistedAttachments = mailManager.getAttachments(mail4);
		assertNotNull(persistedAttachments);
		assertEquals(1, persistedAttachments.size());
		DBMailAttachment persistedAttachment4 = persistedAttachments.get(0);

		dbInstance.commitAndCloseSession();
		
		////////////////////////////////////////////////////////////////
		//ident1 as from delete the mail in its outbox
		////////////////////////////////////////////////////////////////
		List<DBMail> outbox = mailManager.getOutbox(ident1, 0, 0);
		
		DBMail outboxMail = null;
		for(DBMail outMail:outbox) {
			if(outMail.equals(mail2)) {
				outboxMail = outMail;
				break;
			}
		}
		assertNotNull(outboxMail);
		mailManager.delete(outboxMail, ident1, true);
		
		dbInstance.commitAndCloseSession();

		//check if the mail doesn't exist for ident1 anymore
		List<DBMail> outboxDeleted = mailManager.getOutbox(ident1, 0, 0);
		DBMail checkOutboxMailDeleted = null;
		for(DBMail outMail:outboxDeleted) {
			if(outMail.equals(mail2) || outMail.equals(mail3) || outMail.equals(mail4)) {
				checkOutboxMailDeleted = outMail;
				break;
			}
		}
		assertNull(checkOutboxMailDeleted);
		
		dbInstance.commitAndCloseSession();
		
		////////////////////////////////////////////////////////////////
		//check if the mail exists for ident2
		////////////////////////////////////////////////////////////////
		List<DBMail> inbox2= mailManager.getInbox(ident2, null, Boolean.FALSE, null, 0, 0);
		DBMail checkInbox2Mail2Exist = null;
		DBMail checkInbox2Mail3Exist = null;
		DBMail checkInbox2Mail4Exist = null;
		for(DBMail inMail:inbox2) {
			if(inMail.equals(mail2)) {
				checkInbox2Mail2Exist = inMail;
			} else if(inMail.equals(mail3)) {
				checkInbox2Mail3Exist = inMail;
			} else if(inMail.equals(mail4)) {
				checkInbox2Mail4Exist = inMail;
			}
		}
		assertNotNull(checkInbox2Mail2Exist);
		assertNull(checkInbox2Mail3Exist);
		assertNull(checkInbox2Mail4Exist);
		
		dbInstance.commitAndCloseSession();
		
		//ident2 delete the mail
		mailManager.delete(checkInbox2Mail2Exist, ident2, true);
		
		dbInstance.commitAndCloseSession();
		
		//check ident2 inbox
		List<DBMail> inbox2Deleted = mailManager.getInbox(ident2, null, Boolean.FALSE, null, 0, 0);
		DBMail inbox2Mail2Deleted = null;
		for(DBMail inMail:inbox2Deleted) {
			if(inMail.equals(mail2)) {
				inbox2Mail2Deleted = inMail;
				break;
			}
		}
		assertNull(inbox2Mail2Deleted);
		
		
		////////////////////////////////////////////////////////////////
		//check if the mail exists for ident3
		////////////////////////////////////////////////////////////////
		List<DBMail> inbox3= mailManager.getInbox(ident3, null, Boolean.FALSE, null, 0, 0);
		DBMail checkInbox3Mail2Exist = null;
		DBMail checkInbox3Mail3Exist = null;
		DBMail checkInbox3Mail4Exist = null;
		for(DBMail inMail:inbox3) {
			if(inMail.equals(mail2)) {
				checkInbox3Mail2Exist = inMail;
			} else if(inMail.equals(mail3)) {
				checkInbox3Mail3Exist = inMail;
			} else if(inMail.equals(mail4)) {
				checkInbox3Mail4Exist = inMail;
			}
		}
		assertNull(checkInbox3Mail2Exist);
		assertNotNull(checkInbox3Mail3Exist);
		assertNull(checkInbox3Mail4Exist);
		
		dbInstance.commitAndCloseSession();
		
		//ident2 delete the mail
		mailManager.delete(checkInbox3Mail3Exist, ident3, true);
		
		dbInstance.commitAndCloseSession();
		
		//check ident2 inbox
		List<DBMail> inbox3Deleted = mailManager.getInbox(ident3, null, Boolean.FALSE, null, 0, 0);
		DBMail inbox3Mail3Deleted = null;
		for(DBMail inMail:inbox3Deleted) {
			if(inMail.equals(mail3)) {
				inbox3Mail3Deleted = inMail;
				break;
			}
		}
		assertNull(inbox3Mail3Deleted);
		
		////////////////////////////////////////////////////////////////
		//check if the mail exists for ident4
		////////////////////////////////////////////////////////////////
		List<DBMail> inbox4= mailManager.getInbox(ident4, null, Boolean.FALSE, null, 0, 0);
		DBMail checkInbox4Mail2Exist = null;
		DBMail checkInbox4Mail3Exist = null;
		DBMail checkInbox4Mail4Exist = null;
		for(DBMail inMail:inbox4) {
			if(inMail.equals(mail2)) {
				checkInbox4Mail2Exist = inMail;
			} else if(inMail.equals(mail3)) {
				checkInbox4Mail3Exist = inMail;
			} else if(inMail.equals(mail4)) {
				checkInbox4Mail4Exist = inMail;
			}
		}
		assertNull(checkInbox4Mail2Exist);
		assertNull(checkInbox4Mail3Exist);
		assertNotNull(checkInbox4Mail4Exist);
		
		dbInstance.commitAndCloseSession();
		
		//ident2 delete the mail
		mailManager.delete(checkInbox4Mail4Exist, ident4, true);
		
		dbInstance.commitAndCloseSession();
		
		//check ident2 inbox
		List<DBMail> inbox4Deleted = mailManager.getInbox(ident4, null, Boolean.FALSE, null, 0, 0);
		DBMail inbox4Mail4Deleted = null;
		for(DBMail inMail:inbox4Deleted) {
			if(inMail.equals(mail4)) {
				inbox4Mail4Deleted = inMail;
				break;
			}
		}
		assertNull(inbox4Mail4Deleted);
		
		/////////////////////////////////////////////////////////////////////
		//check if all the mails are really deleted
		/////////////////////////////////////////////////////////////////////
		DBMail checkMail2Deleted = mailManager.getMessageByKey(mail2.getKey());
		assertNull(checkMail2Deleted);
		List<DBMailAttachment> checkAttachment2Deleted = mailManager.getAttachments(mail2);
		assertNotNull(checkAttachment2Deleted);
		assertTrue(checkAttachment2Deleted.isEmpty());
		
		DBMail checkMail3Deleted = mailManager.getMessageByKey(mail3.getKey());
		assertNull(checkMail3Deleted);
		List<DBMailAttachment> checkAttachment3Deleted = mailManager.getAttachments(mail3);
		assertNotNull(checkAttachment3Deleted);
		assertTrue(checkAttachment3Deleted.isEmpty());
		
		DBMail checkMail4Deleted = mailManager.getMessageByKey(mail4.getKey());
		assertNull(checkMail4Deleted);
		List<DBMailAttachment> checkAttachment4Deleted = mailManager.getAttachments(mail4);
		assertNotNull(checkAttachment4Deleted);
		assertTrue(checkAttachment4Deleted.isEmpty());
		
		/////////////////////////////////////////////////////////////////////
		//check if all the attachments are really deleted
		/////////////////////////////////////////////////////////////////////
		DBMailAttachmentData deletedAttachment2 = mailManager.getAttachmentWithData(persistedAttachment2.getKey());
		assertNull(deletedAttachment2);

		DBMailAttachmentData deletedAttachment3 = mailManager.getAttachmentWithData(persistedAttachment3.getKey());
		assertNull(deletedAttachment3);

		DBMailAttachmentData deletedAttachment4 = mailManager.getAttachmentWithData(persistedAttachment4.getKey());
		assertNull(deletedAttachment4);
	}
	
	private List<ContactList> createContactLists(String name, Identity... identities) {
		ContactList contacts = new ContactList(name);
		for(Identity identity: identities) {
			contacts.add(identity);
		}
		return Collections.singletonList(contacts);
	}
	
	private MailContext createMailContext() {
		MailContextImpl context = new MailContextImpl();
		context.setResourceableId(25l);
		context.setResourseableTypeName("MAIL-RESOURCE");
		context.setBusinessPath("[Inbox:0]");
		context.setResSubPath("SUB");
		return context;
	}
}
