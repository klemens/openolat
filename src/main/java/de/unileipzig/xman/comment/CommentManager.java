package de.unileipzig.xman.comment;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;

public class CommentManager {

	private static CommentManager INSTANCE = null;
	private OLog log = Tracing.createLoggerFor(CommentManager.class);
	
	private CommentManager() {
		// singleton
	}
	
	static { INSTANCE = new CommentManager(); }
	
	/**
	 * @return Singleton.
	 */
	public static CommentManager getInstance() { return INSTANCE; }

	/**
	 * @return a new CommentEntry object
	 */
	public CommentEntry createCommentEntry() {
		
		return new CommentEntry();
	}
	
	/**
	 * Creates a comment entry with the given text and identity.
	 * if the text is null or text.equals("") == true or the
	 * identity is null a runtimeexception is thrown.
	 * 
	 * @param commentText - the comment may not be null
	 * @param identity - the identity who wrote the comment, may not be null
	 * @return the commentEntry
	 */
	public CommentEntry createCommentEntry(String commentText, Identity identity) {
		
		// create commentEntry and set it
		CommentEntry commentEntry = this.createCommentEntry();
		if ( commentText.equals("") || commentText == null || identity == null ) 
			throw new AssertException("Either commentText or identity was null or text was empty", null);
		commentEntry.setComment(commentText);
		commentEntry.setAuthor(identity);
		
		return commentEntry;
	}
	
	/**
	 * Convenience method for creating a comment
	 * @param esf Valid esf
	 * @param text Has to be size() >= 1
	 */
	public void createCommentInEsa(ElectronicStudentFile esf, String text, Identity author) {
		// create comment
		CommentEntry commentEntry = createCommentEntry(text, author);

		// add comment and update the esf
		esf.addCommentEntry(commentEntry);
		ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);
	}
	
	/**
	 * @return a new Comment object
	 */
	public Comment createComment() {
		
		return new CommentImpl();
	}
	
	/**
	 * persists a given CommentEntry in the database 
	 * @param the CommentEntry to be persisted
	 */
	public void persistCommentEntry(CommentEntry entry) {
		
		DBFactory.getInstance().saveObject(entry);
		log.info("A new CommentEntry with the key: " + entry.getKey() + " was persisted");
	}
	
	/**
	 * persists a given Comment in the database
	 * @param comment
	 */
	public void persistComment(Comment comment) {
		
		DBFactory.getInstance().saveObject(comment);
		log.info("A new Comment with the key: " + comment.getKey() + " was persisted");
	}
	
	/**
	 * retrieves a CommentEntry, identified by the given key
	 * 
	 * @param key - the key of the CommentEntry
	 * @return the CommentEntry or null if none was found
	 */
	public CommentEntry retrieveCommentEntryByKey(Long key) {
		
		String query = "from de.unileipzig.xman.comment.CommentEntry as ce where ce.key = :key";
		DBQuery dbquery = DBFactory.getInstance().createQuery(query);
		dbquery.setString("key", key.toString());
		List<Object> commentEntryList = dbquery.list();
		if ( commentEntryList.size() > 0 ) return (CommentEntry) commentEntryList.get(0);
		else {
			log.warn("No CommentEntry for the given key: " + key.toString() + " could be found");
			return null;
		}
	}
	
	/**
	 * retrieves a Comment, identified by the given key
	 * 
	 * @param key - the key of the Comment
	 * @return the Comment or null if none was found
	 */
	public Comment retrieveCommentByKey(Long key) {
	
		String query = "from de.unileipzig.xman.comment.Comment as c where c.key = :key";
		DBQuery dbquery = DBFactory.getInstance().createQuery(query);
		dbquery.setString("key", key.toString());
		List<Object> commentList = dbquery.list();
		if ( commentList.size() > 0 ) return (Comment) commentList.get(0);
		else {
			log.warn("No Comment for the given key: " + key.toString() + " could be found");
			return null;
		}
	}
	
	/**
 * Removes the CommentEntry identified by the given key from the local database.
	 * 
	 * @param key - The key of the CommentEntry to be deleted
	 */
	public void removeCommentEntryByKey(Long key) {
		
		CommentEntry entry = this.retrieveCommentEntryByKey(key);
		if ( entry != null ) {
			
			long entryKey = entry.getKey();
			DBFactory.getInstance().deleteObject(entry);
			log.info("The CommentEntry with the key: " + entryKey + "was deleted!");
		}
		else {
			log.warn("The CommentEntry with the key: " + key + "could not be deleted, cause there was no such CommentEntry in the local database");
		}
	}
	
	/**
	 * Removes the Comment identified by the given key from the local database.
	 * 
	 * @param key - The key of the Comment to be deleted
	 */
	public void removeCommentByKey(Long key){
		
		Comment comment = this.retrieveCommentByKey(key);
		if ( comment != null ) {
			
			long entryKey = comment.getKey();
			DBFactory.getInstance().deleteObject(comment);
			log.info("The Comment with the key: " + entryKey + "was deleted!");
		}
		else {
			log.warn("The Comment with the key: " + key + "could not be deleted, cause there was no such Comment in the local database");
		}
	}
	
	/**
	 * Updates the given CommentEntry in the local database
	 * 
	 * @param commentEntry - the CommentEntry to be updated
	 */
	public void updateCommentEntry(CommentEntry commentEntry) {
		
		DBFactory.getInstance().updateObject(commentEntry);
	}
	
	/**
	 * Updates the given Comment in the local database
	 * 
	 * @param comment - to be updated
	 */
	public void updateComment(Comment comment) {
		
		DBFactory.getInstance().updateObject(comment);
	}
}
