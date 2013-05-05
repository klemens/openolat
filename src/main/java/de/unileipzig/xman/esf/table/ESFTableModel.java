package de.unileipzig.xman.esf.table;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;

import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.studyPath.StudyPath;

public class ESFTableModel extends DefaultTableDataModel<ElectronicStudentFile> {

	
	private Locale locale;
	private List<ElectronicStudentFile> entries;
	private int COLUMN_COUNT;
	public static final String COMMAND_VALIDATE = "do.validate.esf";
	public static final String COMMAND_INVALIDATE = "do.invalidate.esf";
	public static final String COMMAND_OPEN = "do.open.esf";
	public static final String COMMAND_DELETE = "do.delete.esf";
	public static final String COMMAND_SENDMAIL = "sendmail";
	
	/**
	 * 
	 * @param locale
	 * @param entries
	 * @param showNonValidatedESF
	 */
	public ESFTableModel(Locale locale, List<ElectronicStudentFile> entries) {
		super(entries);
		
		this.locale = locale;
		this.entries = entries;
		this.COLUMN_COUNT = 4;
	}


	public int getColumnCount() {
		
		return this.COLUMN_COUNT;
	}


	public Object getValueAt(int row, int col) {

		ElectronicStudentFile entry = this.getEntryAt(row);
		
		switch(col) {
			case 0: return entry.getIdentity().getName();

			case 1: return entry.getIdentity().getUser().getProperty(UserConstants.LASTNAME, locale) + ", " + 
										 entry.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, locale);

			case 2: return entry.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, locale);

			case 3: return entry.getIdentity().getUser().getProperty(UserConstants.STUDYSUBJECT, locale);

			case 5: return entry.getLastModified();

			default: return "";
		}
	}

	public void setTable(TableController tableCtr) {
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ESFTableModel.username", 0, null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ESFTableModel.name", 1, null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ESFTableModel.institutionaluseridentifier", 2, COMMAND_OPEN, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ESFTableModel.studyPath", 3, null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ESFTableModel.lastModified", 4, null, locale));
	}
	
	public ElectronicStudentFile getEntryAt(int row) {
		
		return entries.get(row);
	}

}
