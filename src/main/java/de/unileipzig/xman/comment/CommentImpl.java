package de.unileipzig.xman.comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Description:<br>
 * This class represents a set of CommentEntrys.
 * Only authorized personal should add informations.
 * 
 * <P>
 * Initial Date:  15.05.2008 <br>
 * @author gerb
 */
public class CommentImpl extends PersistentObject implements Comment {
	
	private List<CommentEntry> comments;
	private Date lastModified;

	CommentImpl() {
		
		this.comments = new ArrayList<CommentEntry>();
	}
	
	/**
	 * @see de.xman.comment.Comment#addCommentEntry(de.xman.comment.CommentEntry)
	 */
	public void addCommentEntry(CommentEntry commentEntry) {
		
		this.comments.add(commentEntry);
	}

	/**
	 * @see de.xman.comment.Comment#removeCommentEntry(java.lang.Long)
	 */
	public void removeCommentEntry(Long key) {
		
		CommentEntry commentEntry = CommentManager.getInstance().retrieveCommentEntryByKey(key);
		this.comments.remove(commentEntry);
	}
	
	/**
	 * @see de.xman.comment.Comment#getComments()
	 */
	public List<CommentEntry> getComments() {
		return comments;
	}

	/**
	 * @param comments - the List of comments to be set
	 */
	public void setComments(List<CommentEntry> comments) {
		this.comments = comments;
	}
	
	public Date getLastModified() {
		
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		
		this.lastModified = lastModified;
	}
}
