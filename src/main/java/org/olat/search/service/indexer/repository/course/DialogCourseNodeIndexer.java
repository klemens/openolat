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

package org.olat.search.service.indexer.repository.course;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSLeafFilter;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.modules.dialog.DialogElement;
import org.olat.modules.dialog.DialogElementsController;
import org.olat.modules.dialog.DialogElementsPropertyManager;
import org.olat.modules.dialog.DialogPropertyElements;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.Status;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.CourseNodeDocument;
import org.olat.search.service.document.ForumMessageDocument;
import org.olat.search.service.document.file.DocumentAccessException;
import org.olat.search.service.document.file.FileDocumentFactory;
import org.olat.search.service.indexer.DefaultIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Indexer for dialog course-node.
 * @author Christian Guretzki
 */
public class DialogCourseNodeIndexer extends DefaultIndexer implements CourseNodeIndexer {
	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public final static String TYPE_MESSAGE = "type.course.node.dialog.forum.message";
	public final static String TYPE_FILE    = "type.course.node.dialog.file";

	private final static String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.DialogCourseNode";
	
	@Override
	public void doIndex(SearchResourceContext searchResourceContext, Object parentObject, OlatFullIndexer indexer)
			throws IOException, InterruptedException {
		//
	}

	@Override
	public void doIndex(SearchResourceContext repositoryResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter) throws IOException,InterruptedException  {
		SearchResourceContext courseNodeResourceContext = createSearchResourceContext(repositoryResourceContext, courseNode, null);
		Document document = CourseNodeDocument.createDocument(courseNodeResourceContext, courseNode);
		indexWriter.addDocument(document);
		
		CoursePropertyManager coursePropMgr = course.getCourseEnvironment().getCoursePropertyManager();
		DialogElementsPropertyManager dialogElmsMgr = DialogElementsPropertyManager.getInstance();
		DialogPropertyElements elements = dialogElmsMgr.findDialogElements(coursePropMgr, courseNode);
		List<DialogElement> list = new ArrayList<DialogElement>();
		if (elements != null) list = elements.getDialogPropertyElements();
		// loop over all dialog elements
		for (Iterator<DialogElement> iter = list.iterator(); iter.hasNext();) {
			DialogElement element = iter.next();
			element.getAuthor();
			element.getDate();
			Forum forum = ForumManager.getInstance().loadForum(element.getForumKey());
			// do IndexForum
			doIndexAllMessages(courseNodeResourceContext, forum, indexWriter );
			// do Index File
			doIndexFile(element.getFilename(), element.getForumKey(), courseNodeResourceContext, indexWriter);
		}
	}

	/**
	 * Index a file of dialog-module.
	 * @param filename
	 * @param forumKey
	 * @param leafResourceContext
	 * @param indexWriter
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void doIndexFile(String filename, Long forumKey, SearchResourceContext leafResourceContext, OlatFullIndexer indexWriter) throws IOException,InterruptedException {
		OlatRootFolderImpl forumContainer = DialogElementsController.getForumContainer(forumKey);
		VFSLeaf leaf = (VFSLeaf) forumContainer.getItems(new VFSLeafFilter()).get(0);
		if (isLogDebugEnabled()) logDebug("Analyse VFSLeaf=" + leaf.getName());
		try {
			if (CoreSpringFactory.getImpl(FileDocumentFactory.class).isFileSupported(leaf)) {
				leafResourceContext.setFilePath(filename);
				leafResourceContext.setDocumentType(TYPE_FILE);
				
				Document document = CoreSpringFactory.getImpl(FileDocumentFactory.class).createDocument(leafResourceContext, leaf);
				indexWriter.addDocument(document);
			} else {
				if (isLogDebugEnabled()) logDebug("Documenttype not supported. file=" + leaf.getName());
			}
		} catch (DocumentAccessException e) {
			if (isLogDebugEnabled()) logDebug("Can not access document." + e.getMessage());
		} catch (IOException ioEx) {
			logWarn("IOException: Can not index leaf=" + leaf.getName(), ioEx);
		} catch (InterruptedException iex) {
			throw new InterruptedException(iex.getMessage());
		} catch (Exception ex) {
			logWarn("Exception: Can not index leaf=" + leaf.getName(), ex);
		}
	}

	private void doIndexAllMessages(SearchResourceContext parentResourceContext, Forum forum, OlatFullIndexer indexWriter) throws IOException,InterruptedException {
		// loop over all messages of a forum
		List<Message> messages = ForumManager.getInstance().getMessagesByForum(forum);
		for(Message message:messages){
			SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
			searchResourceContext.setBusinessControlFor(message);
			searchResourceContext.setDocumentType(TYPE_MESSAGE);
			Document document = ForumMessageDocument.createDocument(searchResourceContext, message);
		  indexWriter.addDocument(document);
		}
	}

	@Override
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}
	
	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles)  {
		ContextEntry ce = businessControl.popLauncherContextEntry();
		OLATResourceable ores = ce.getOLATResourceable();
		if(isLogDebugEnabled()) logDebug("OLATResourceable=" + ores);
		if ( (ores != null) && (ores.getResourceableTypeName().startsWith("path=")) ) {
			// => it is a file element, typeName format: 'path=/test1/test2/readme.txt'
			return true;
		} else if ((ores != null) && ores.getResourceableTypeName().equals( OresHelper.calculateTypeName(Message.class) ) ) {
			// it is message => check message access
			Long resourceableId = ores.getResourceableId();
			Message message = ForumManager.getInstance().loadMessage(resourceableId);
			Message threadtop = message.getThreadtop();
			if(threadtop==null) {
				threadtop = message;
			}
			boolean isMessageHidden = Status.getStatus(threadtop.getStatusCode()).isHidden(); 
			//assumes that if is owner then is moderator so it is allowed to see the hidden forum threads		
			//TODO: (LD) fix this!!! - the contextEntry is not the right context for this check
			boolean isOwner = BaseSecurityManager.getInstance().isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_ACCESS,  contextEntry.getOLATResourceable());
			if(isMessageHidden && !isOwner) {
				return false;
			}		
			return true;
		} else {
			logWarn("In DialogCourseNode unkown OLATResourceable=" + ores, null);
			return false;
		}
	}
}
