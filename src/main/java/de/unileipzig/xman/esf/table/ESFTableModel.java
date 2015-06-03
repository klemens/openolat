package de.unileipzig.xman.esf.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.id.UserConstants;

import de.unileipzig.xman.esf.ElectronicStudentFile;

public class ESFTableModel extends DefaultTableDataModel<ElectronicStudentFile> {
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
		
		setLocale(locale);
		this.COLUMN_COUNT = 4;
	}


	public int getColumnCount() {
		
		return this.COLUMN_COUNT;
	}


	public Object getValueAt(int row, int col) {

		ElectronicStudentFile entry = getObject(row);
		
		switch(col) {
			case 0: return entry.getIdentity().getName();

			case 1: return entry.getIdentity().getUser().getProperty(UserConstants.LASTNAME, getLocale()) + ", " + 
										 entry.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, getLocale());

			case 2: return entry.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, getLocale());

			case 3: return entry.getIdentity().getUser().getProperty(UserConstants.STUDYSUBJECT, getLocale());

			case 5: return entry.getLastModified();

			default: return "";
		}
	}

	public void setTable(TableController tableCtr) {
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ESFTableModel.username", 0, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ESFTableModel.name", 1, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ESFTableModel.institutionaluseridentifier", 2, COMMAND_OPEN, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ESFTableModel.studyPath", 3, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ESFTableModel.lastModified", 4, null, getLocale()));
	}

	@Override
	public ESFTableModel createCopyWithEmptyList() {
		return new ESFTableModel(getLocale(), new ArrayList<ElectronicStudentFile>());
	}
}
