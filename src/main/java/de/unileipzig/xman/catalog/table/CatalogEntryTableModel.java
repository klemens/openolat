package de.unileipzig.xman.catalog.table;

import java.util.List;
import java.util.Locale;

import org.olat.catalog.CatalogEntry;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.Translator;


public class CatalogEntryTableModel extends DefaultTableDataModel {

	public static final String COMMAND_DOWN = "load.children.of.node";
	public static final String COMMAND_UP = "show parents of node";
	
	private Locale locale;
	private Translator translator;
	
	public CatalogEntryTableModel(Locale locale, List<CatalogEntry> entries, Translator translator) {
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
		
		CatalogEntry ce = this.getEntryAt(row);
		switch(col) {
		
			case 0: return ce.getName();
			
			case 1: return ce.getDescription() == null || ce.getDescription().equals("") ? translator.translate("CatalogEntryTableModel.noDescription") : ce.getDescription();
			
			case 2: return ce.getParent().getName();
			
			default: return "N/A";
		}
	}
	
	public CatalogEntry getEntryAt(int row){
		
		return (CatalogEntry)this.objects.get(row);
	}

	public void setTable(TableController tableCtr) {
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("CatalogEntryTableModel.header.name", 0, COMMAND_DOWN, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("CatalogEntryTableModel.header.description", 1, null, locale));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("CatalogEntryTableModel.header.parentName", 2, COMMAND_UP, locale));
	}
}
