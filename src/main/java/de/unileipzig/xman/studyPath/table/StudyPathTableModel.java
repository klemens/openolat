package de.unileipzig.xman.studyPath.table;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;

import de.unileipzig.xman.studyPath.StudyPath;


public class StudyPathTableModel extends DefaultTableDataModel {

	private Locale locale;
	private Translator translator;
	
	public static String ENTRY_SELECTED = "studyPath.edit";
	
	public StudyPathTableModel(Locale locale, List<StudyPath> entries, Translator translator) {
		super(entries);
		
		this.locale = locale;
		this.translator = translator;
	}

	public int getColumnCount() {

		return 3;
	}

	public int getRowCount() {

		return this.objects.size();
	}

	public Object getValueAt(int row, int col) {
		
		StudyPath sp = this.getEntryAt(row);
		
		switch(col) {
			case 0: return sp.getName(); 
			
			case 1: return sp.getCreationDate();
			
			case 2: return sp.getLastModified();
			
			default: return "N/A";
		}
		
	}
	
	public StudyPath getEntryAt(int row){
		
		return (StudyPath)this.objects.get(row);
	}

	public void setTable(TableController tableCtr) {
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("StudyPathTableModel.header.name", 0, ENTRY_SELECTED, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("StudyPathTableModel.header.lastModified", 1, null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("StudyPathTableModel.header.creationDate", 2, null, locale));
	}
}
