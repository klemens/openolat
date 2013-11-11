package de.unileipzig.xman.comment;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;

import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.esf.ElectronicStudentFileManager;

public class CommentManager {

	private static CommentManager INSTANCE = null;
	
	private CommentManager() {
		// singleton
	}
	
	static { INSTANCE = new CommentManager(); }
	
	/**
	 * @return Singleton.
	 */
	public static CommentManager getInstance() { return INSTANCE; }
	
	/**
	 * Convenience method for creating a comment
	 * @param esf Valid esf
	 * @param text Has to be size() >= 1
	 */
	public void createCommentInEsa(ElectronicStudentFile esf, String text, Identity author) {
		// create comment
		CommentEntry commentEntry = new CommentEntry(author, text);

		// add comment and update the esf
		esf = ElectronicStudentFileManager.getInstance().retrieveESFByIdentity(esf.getIdentity());
		esf.addCommentEntry(commentEntry);		
		ElectronicStudentFileManager.getInstance().updateElectronicStundentFile(esf);
	}
	
	/**
	 * Convenience method for creating a comment
	 * @param esf Valid esf
	 * @param text Has to be size() >= 1
	 */
	public void updateCommentInEsa(CommentEntry comment, String newText) {
		// load for update
		CommentEntry newComment = (CommentEntry) DBFactory.getInstance().loadObject(comment, true);
		
		newComment.setComment(newText);
		
		DBFactory.getInstance().updateObject(newComment);
	}
}
