package de.unileipzig.xman.comment.table;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;

import de.unileipzig.xman.comment.CommentEntry;

/**
 * 
 * Description:<br>
 * The TableDataModel for the CommentEntrys
 * 
 * <P>
 * Initial Date:  22.05.2008 <br>
 * @author gerb
 */
public class CommentEntryTableModel extends DefaultTableDataModel<CommentEntry> {

	private List<CommentEntry> entries;
	private int COLUMN_COUNT = 3;
	private Locale locale;
	
	/**
	 * Constructs a new table model according to the locale and the entries
	 * 
	 * @param locale - the user's local
	 * @param entries - a list of commentEntry's
	 */
	public CommentEntryTableModel(Locale locale, List<CommentEntry> entries) {
		super(entries);
		
		this.locale = locale;
		this.entries = entries;
	}

	/**
	 * Returns the count of the colomns of this table
	 * 
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		
		return this.COLUMN_COUNT;
	}

	/**
	 * Returns the value of the cell row x col of this table
	 * 
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		
		CommentEntry entry = this.getEntryAt(row);
		
		switch(col) {
			
			case 0: return  entry.getCreationDate();
			
			case 1:
				User user = entry.getAuthor().getUser();
				return user.getProperty(UserConstants.FIRSTNAME, null) + " " + user.getProperty(UserConstants.LASTNAME, null);

			/**
			 * Old code: Formatter.formatWikiMarkup(entry.getComment());
			 * 
			 * Decription of formatWikiMarkup:
			 * renders wiki markup like _italic_ to XHTML see also www.radeox.org
			 * @Deprecated The wiki markup area is no longer supported. In the legacy form infrastructure it's still there, but it won't be available in the new flexi forms. In
			 *             flexi forms use the RichTextElement instead. tested during migration and expanded to prevent radeox failures
			 */
			case 2: return  entry.getComment();
			
			default: return "";
		}
	}

	/**
	 * @param row - the number of the row 
	 * @return the CommentEntry at the "row"-position
	 */
	private CommentEntry getEntryAt(int row) {
		
		return entries.get(row);
	}
	
	/**
	 * Adds the header of the columns of this model
	 * 
	 * @param tableCtr - the tableController for this model
	 */
	public void setTable(TableController tableCtr) {
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("CommentEntryTableModel.header.creationDate", 0, null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("CommentEntryTableModel.header.author", 1, null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("CommentEntryTableModel.header.comment", 2, null, locale));
	}
}
