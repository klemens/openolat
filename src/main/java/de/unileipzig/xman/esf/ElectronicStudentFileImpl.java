package de.unileipzig.xman.esf;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

import de.unileipzig.xman.comment.Comment;
import de.unileipzig.xman.comment.CommentEntry;
import de.unileipzig.xman.comment.CommentManager;
import de.unileipzig.xman.protocol.Protocol;

public class ElectronicStudentFileImpl extends PersistentObject implements ElectronicStudentFile {

	private boolean validated;
	private Identity identity;
	private Identity validator;
	private List<Protocol> protocolList;
	private Comment comments;
	private Date lastModified;
	
	ElectronicStudentFileImpl(){}
	
	/**
	 * all fields will be set via the setMethods, so here is nothing to do
	 */
	ElectronicStudentFileImpl(Identity identity){
		
		this.comments = CommentManager.getInstance().createComment();
		CommentManager.getInstance().persistComment(this.comments);
		
		this.identity = identity;
		this.protocolList = new ArrayList<Protocol>();
	}
	
// ############################# Validation ####################################
	
	public boolean getValidated() {
		
		return this.validated;
	}

	public void setValidated(boolean validated) {
		
		this.validated = validated;
	}
	
	// ############################# Identity ####################################
	
	public Identity getIdentity() {
		
		return this.identity;
	}

	public void setIdentity(Identity identity) {
		
		this.identity = identity;
	}
	
	// ############################# Protocols ###################################
	
	public List<Protocol> getProtocolList() {
		
		return this.protocolList;
	}

	public void setProtocolList(List<Protocol> protocolList) {
		
		this.protocolList = protocolList;
	}
	
	public void addProtocol(Protocol proto) {
		
		this.protocolList.add(proto);
	}

	// ########################### Comment ###################################
	
	public void addCommentEntry(CommentEntry commentEntry) {
		
		this.comments.addCommentEntry(commentEntry);
	}

	public void removeCommentEntry(Long commentEntryKey) {
		
		this.comments.removeCommentEntry(commentEntryKey);
	}
	
	public List<CommentEntry> getCommentEntries() {
		
		return comments.getComments();
	}
	
	public Comment getComments() {
		
		return this.comments;
	}
	
	public void setComments(Comment comment) {
		
		this.comments = comment;
	}


	// ########################## OLAT Resourceable ########################
	
	public Long getResourceableId() {
		
		Long id = this.getKey();				
		return id;
	}

	public String getResourceableTypeName() {
		
		return ORES_TYPE_NAME;
	}

	public Identity getValidator() {

		return this.validator;
	}

	public void setValidator(Identity identity) {
		
		this.validator = identity;
	}

	public Date getLastModified() {
		
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		
		this.lastModified = lastModified;
	}
}
