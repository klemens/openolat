package de.unileipzig.xman.esf;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;

import de.unileipzig.xman.comment.CommentEntry;
import de.unileipzig.xman.protocol.Protocol;

public class ElectronicStudentFileImpl extends PersistentObject implements ElectronicStudentFile {

	private Identity identity;
	private List<Protocol> protocolList;
	private Set<CommentEntry> comments;
	private Date lastModified;
	
	ElectronicStudentFileImpl() {
		
	}
	
	/**
	 * all fields will be set via the setMethods, so here is nothing to do
	 */
	ElectronicStudentFileImpl(Identity identity){
		comments = new HashSet<CommentEntry>();
		protocolList = new ArrayList<Protocol>();
		lastModified = new Date();
		
		this.identity = identity;
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
		comments.add(commentEntry);
	}

	public void removeCommentEntries(List<CommentEntry> entries) {
		comments.remove(entries);
		Iterator<CommentEntry> it = comments.iterator();
		while(it.hasNext()) {
			CommentEntry c1 = it.next();
			for(CommentEntry c2 : entries) {
				if(c2.equalsByPersistableKey(c1)) {
					it.remove();
					break;
				}
			}
		}
	}
	
	@Override
	public Set<CommentEntry> getComments() {
		return comments;
	}
	
	@Override
	public void setComments(Set<CommentEntry> comments) {
		this.comments = comments;
	}
	

	// ########################## OLAT Resourceable ########################
	
	public Long getResourceableId() {
		
		Long id = this.getKey();				
		return id;
	}

	public String getResourceableTypeName() {
		
		return ORES_TYPE_NAME;
	}

	public Date getLastModified() {
		
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		
		this.lastModified = lastModified;
	}
}
