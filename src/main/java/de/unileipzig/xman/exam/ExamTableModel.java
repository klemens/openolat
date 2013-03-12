package de.unileipzig.xman.exam;

import java.util.ArrayList;
import java.util.List;

import org.olat.ControllerFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.IconedTypeCellRenderer;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.repository.RepositoryEntry;

public class ExamTableModel implements TableDataModel {

	/**
	 * Identifies a table selection event (outer-left column)
	 */
	public static final String TABLE_ACTION_SELECT_LINK = "rtbSelectLink";
	
	/**
	 * Identifies a table launch event (if clicked on an item in the name column).
	 */
	public static final String TABLE_ACTION_SELECT_ENTRY = "rtbSelectEntry";
	
	private List entries = new ArrayList(0);
	private Translator translator;
	
	private static int COLUMN_COUNT = 3;
	
	public ExamTableModel(Translator translator) {
		this.translator = translator;
	}
	
	private class CourseIconRenderer extends IconedTypeCellRenderer {
		protected String getAltText(Object val) {
			 return ControllerFactory.translateResourceableTypeName((String)val, translator.getLocale());
		}
		
		protected String getIconPath(Object val) { 
			return "raw/images/restypes/" + val.toString() + ".gif"; 
		}
	}
	
	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getRowCount()
	 */
	public int getRowCount() {
		return entries.size();
	}

	/**
	 * @param row
	 * @return the entry of the row
	 */
	public RepositoryEntry getEntryAt(int row){
		return (RepositoryEntry)entries.get(row);
	}
	
	/**
	 * 
	 */
	public Object getValueAt(int row, int col) {
		// gibt den wert der zelle zurueck
		RepositoryEntry repoEntry = this.getEntryAt(row);
		switch(col) {
			//case 0: if( exam.getIsOral() ) return translator.translate("ExamTableModel.oralExam");
			//		else return translator.translate("ExamTableModel.writtenExam"); // Type "s" for written "m" for oral
			
			//case 1: return exam.getName();
			
			case 2: return "n/a";
			
			case 5: {
				switch (repoEntry.getAccess()) {
					case RepositoryEntry.ACC_OWNERS: return translator.translate("ExamTableModel.access.owner");
					case RepositoryEntry.ACC_OWNERS_AUTHORS: return translator.translate("ExamTableModel.access.author");
					case RepositoryEntry.ACC_USERS: return translator.translate("ExamTableModel.access.user");
					case RepositoryEntry.ACC_USERS_GUESTS: return translator.translate("ExamTableModel.access.guest");
				}
			}
			
			default: return "ERROR";
		}
	}
	
	/**
	 * sets the entries of the table
	 * @param entries - a list of exams
	 */
	public void setEntries(List entries){
		this.entries = entries;
	}	
	
	/**
	 * initializes the table
	 */
	public void setTable(TableController tableCtr, String selectButtonLabel, boolean enableDirectLaunch) {
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("ExamTableModel.type", 0, null, 
				translator.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new CourseIconRenderer()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ExamTableModel.name", 1, enableDirectLaunch ? TABLE_ACTION_SELECT_ENTRY : null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ExamTableModel.type", 2, null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ExamTableModel.appointment", 3, null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ExamTableModel.author", 4, null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ExamTableModel.access", 5, null, translator.getLocale()));
	}

	
	// ----------- neu
	@Override
	public Object getObject(int row) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setObjects(List objects) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object createCopyWithEmptyList() {
		// TODO Auto-generated method stub
		return null;
	}
}