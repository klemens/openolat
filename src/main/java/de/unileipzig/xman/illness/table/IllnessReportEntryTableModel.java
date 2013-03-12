package de.unileipzig.xman.illness.table;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;

import de.unileipzig.xman.illness.IllnessReportEntry;

/**
 * 
 * Description:<br>
 * The TableDataModel for the IllnessReportEntries
 * 
 * <P>
 * Initial Date:  22.05.2008 <br>
 * @author gerb
 */
public class IllnessReportEntryTableModel extends DefaultTableDataModel {

	private List<IllnessReportEntry> entries;
	private int COLUMN_COUNT = 2;
	private Locale locale;
	
	/**
	 * Constructs a new table model according to the locale and the entries
	 * 
	 * @param locale - the user's local
	 * @param entries - a list of commentEntry's
	 */
	public IllnessReportEntryTableModel(Locale locale, List<IllnessReportEntry> entries) {
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
	 * Returns the value of the cell: row x col of this table
	 * 
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		
		IllnessReportEntry entry = this.getEntryAt(row);
		
		switch(col) {
			case 0: return  DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(entry.getFromDate());
		
			case 1: return  DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(entry.getToDate());
			
			default: return "";
		}
	}

	/**
	 * @param row - the number of the row 
	 * @return the IllnessReportEntry at the "row"-position
	 */
	private IllnessReportEntry getEntryAt(int row) {
		
		return entries.get(row);
	}
	
	/**
	 * Adds the header of the columns of this model
	 * 
	 * @param tableCtr - the tableController for this model
	 */
	public void setTable(TableController tableCtr) {
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("IllnessReportEntryTableModel.header.start", 0, null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("IllnessReportEntryTableModel.header.end", 1, null, locale));
	}
}

