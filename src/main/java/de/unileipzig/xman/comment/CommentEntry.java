package de.unileipzig.xman.comment;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Description:<br>
 * The real comment, including the author of the comment, is saved here.
 * 
 * <P>
 * Initial Date:  15.05.2008 <br>
 * @author gerb
 */
public class CommentEntry extends PersistentObject implements ModifiedInfo {

	private Identity author;
	private String comment;
	private Date lastModified;
	
	CommentEntry() {
		
		this.author = null;
		this.comment = "";
	}

	/**
	 * @return the author of this commentEntry
	 */
	public Identity getAuthor() {
		return author;
	}

	/**
	 * @param author - the author
	 */
	public void setAuthor(Identity author) {
		this.author = author;
	}

	/**
	 * @return the comment of this commentEntry
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment - the comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public Date getLastModified() {
		
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		
		this.lastModified = lastModified;
	}
}
