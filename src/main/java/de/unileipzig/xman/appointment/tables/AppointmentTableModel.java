package de.unileipzig.xman.appointment.tables;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.unileipzig.xman.appointment.Appointment;

/**
 * 
 * @author
 */
public class AppointmentTableModel extends DefaultTableDataModel {

	public static final String SELECT_SUBSCRIBE = "AppointmentTableModel.select.subscribe";
	public static final String SELECT_UNSUBSCRIBE = "AppointmentTableModel.select.unsubscribe";
	public static final String NO_SELECTION = "select.nothing";
	
	private static final String PACKAGE = Util.getPackageName(Appointment.class);
	
	private List<Appointment> entries;
	private int COLUMN_COUNT;
	private Translator translator;
	private String selection;
	
	/**
	 * Constructor
	 * @param translator - the package translator
	 * @param categories - List of appointments
	 * @param selection - the kind of selection (use the static finals SELECT_SUBSCRIBE or SELECT_UNSUBSCRIBE as selection action)
	 */
	public AppointmentTableModel(Locale locale, List<Appointment> appointments, String selection) {
		super(appointments);
		
		this.translator = new PackageTranslator(PACKAGE, locale);
		
		this.selection = selection;
		this.entries = appointments;
		if ( selection.equals(SELECT_SUBSCRIBE) || selection.equals(SELECT_UNSUBSCRIBE) ) COLUMN_COUNT = 4;
		else COLUMN_COUNT = 3;
	}
	
	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		
		return COLUMN_COUNT;
	}
	
	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getRowCount()
	 */
	public int getRowCount() {
		
		return entries.size();
	}

	/**
	 * gets the entry at the specified position
	 * @return the selected appointment
	 */
	public Appointment getEntryAt(int row){
		
		return entries.get(row);
	}
	
	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public String getValueAt(int row, int col) {
		
		Appointment appointment = this.getEntryAt(row);
		switch(col) {
			case 0: return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, translator.getLocale()).format(appointment.getDate());
			case 1: return appointment.getPlace();
			case 2: return new Integer(appointment.getDuration()).toString() + " min";
			case 3: return translator.translate(selection);
			default: return "";
		}
	}
	
	/**
	 * sets the entries of the table
	 * @param entries - a list of appointments
	 */
	public void setEntries(List<Appointment> appointments){
		
		this.entries = appointments;
		this.objects = appointments;
	}
	
	/**
	 * initializes the table
	 */
	public void setTable(TableController tableCtr) {
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentTableModel.header.date", 0, null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentTableModel.header.place", 1, null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentTableModel.header.duration", 2, null, translator.getLocale()));
		if ( COLUMN_COUNT == 4 ) tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentTableModel.header.choose", 3, selection, translator.getLocale()));
	}
}