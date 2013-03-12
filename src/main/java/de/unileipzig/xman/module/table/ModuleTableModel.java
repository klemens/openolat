package de.unileipzig.xman.module.table;

import java.util.List;
import java.util.Vector;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;

import de.unileipzig.xman.module.Module;


/**
 * 
 * @author
 */
public class ModuleTableModel extends DefaultTableDataModel {

	public static String ENTRY_SELECTED = "module.launch";
	private List<Module> entries = new Vector<Module>();
	private int COLUMN_COUNT = 4;
	private Translator translator;
	private boolean selectable;
	
	/**
	 * default constructor
	 * @param translator - the package translator
	 */
	public ModuleTableModel(Translator translator, List<Module> moduleegories, boolean selectable) {
		super(moduleegories);
		this.entries = moduleegories; 
		this.translator = translator;
		this.selectable = selectable;
	}
	
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public int getRowCount() {
		return entries.size();
	}

	/**
	 * 
	 * @param row
	 * @return
	 */
	public Module getEntryAt(int row){
		return entries.get(row);
	}
	
	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	@Override
	public String getValueAt(int row, int col) {
		Module module = this.getEntryAt(row);
		switch(col) {
			case 0: return module.getName();
			
			case 1: return module.getModuleNumber();
			
			//siehe CommentEntryTableModel
			case 2: String wikitext = Formatter.truncate(module.getDescription(), 256);
					return wikitext;
			
			case 3: return module.getPersonInCharge().getName() + ", " + 
										 module.getPersonInCharge().getUser().getProperty(UserConstants.FIRSTNAME, null) +" "+
										 module.getPersonInCharge().getUser().getProperty(UserConstants.LASTNAME, null);
			
			default: return "";
		}
	}
	
	/**
	 * sets the entries of the table
	 * @param entries - a list of exams
	 */
	public void setEntries(List<Module> entries){
		this.entries = entries;
	}
	
	/**
	 * initializes the table
	 */
	public void setTable(TableController tableCtr) {
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ModuleTableModel.header.name", 0, selectable ? ENTRY_SELECTED : null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ModuleTableModel.header.number", 1, null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ModuleTableModel.header.description", 2, null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ModuleTableModel.header.personInCharge", 3, null, translator.getLocale()));
	}

}
