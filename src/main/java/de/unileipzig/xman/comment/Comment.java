package de.unileipzig.xman.comment;

import java.util.List;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 * 
 * Description:<br>
 * TODO: gerb Class Description for Comment
 * 
 * <P>
 * Initial Date:  15.05.2008 <br>
 * @author gerb
 */
public interface Comment extends ModifiedInfo, CreateInfo, Persistable {
	
	public void addCommentEntry(CommentEntry commentEntry);
	
	public void removeCommentEntries(List<CommentEntry> entries);
	
	public List<CommentEntry> getComments();
	
}
