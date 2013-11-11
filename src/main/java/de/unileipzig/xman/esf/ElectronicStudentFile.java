package de.unileipzig.xman.esf;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.util.resource.OresHelper;

import de.unileipzig.xman.comment.CommentEntry;
import de.unileipzig.xman.protocol.Protocol;

/**
 * This class represents an electronic version of the students file in the exam office.
 * Every Exam including the grade, the student took place in, is listed here.
 * It is possible to add a comment here.
 * 
 * @author gerb
 */
public interface ElectronicStudentFile extends CreateInfo, ModifiedInfo, Persistable, OLATResourceable {
	
	// Repository types
	public static String ORES_TYPE_NAME = OresHelper.calculateTypeName(ElectronicStudentFileImpl.class);

	
	/* ------------------getter --------------------- */
	
	/**
	 * @return the identity witch belongs to this esf
	 */
	public Identity getIdentity();
	
	/**
	 * @return the list of protocol of the student
	 */
	public List<Protocol> getProtocolList();
	
	/**
	 * @return the comments of this esf
	 */
	public Set<CommentEntry> getComments();
	
	/**
	 * @return the date, when the esf was edited the last time
	 */
	public Date getLastModified();
	
	/* ------------------setter --------------------- */
	
	/**
	 * @return the identity witch belongs to this esf
	 */
	public void setIdentity(Identity identity);
	
	/**
	 * @return the list of protocol of the student
	 */
	public void setProtocolList(List<Protocol> protocolList);
	
	/**
	 * spring setter
	 */
	public void setComments(Set<CommentEntry> comments);
	
	/**
	 * @param the date to set
	 */
	public void setLastModified(Date lastModified);
	
	/* ------------------adder --------------------- */
	
	/**
	 * @param adds a CommentEntry to the list of those
	 */
	public void addCommentEntry(CommentEntry commentEntry);
	
	
	/**
	 * @param proto - the protocol which should be added to this esf
	 */
	public void addProtocol(Protocol proto);
	
	/* ------------------remover --------------------- */
	
	/**	 * 
	 * @param the key of the CommentEntry to delete
	 * @return the deleted CommentEntry
	 */
	public void removeCommentEntries(List<CommentEntry> entries);
	
	
	/**************************************************************/
}
