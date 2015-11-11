package de.unileipzig.xman.appointment.tables;

import java.text.DateFormat;
import java.util.List;

import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.Translator;

import de.unileipzig.xman.appointment.Appointment;

public class AppointmentTableModel extends DefaultTableDataModel<Appointment> {
	private boolean examIsOral;
	private Translator translator;
	
	/**
	 * Constructor
	 * @param translator - the package translator
	 * @param categories - List of appointments
	 * @param examIsOral - the kind of exam
	 */
	public AppointmentTableModel(Translator translator, List<Appointment> appointments, boolean examIsOral) {
		super(appointments);
		
		this.translator = translator;
		this.examIsOral = examIsOral;
	}
	
	@Override
	public int getColumnCount() {
		return examIsOral ? 4 : 3;
	}

	@Override
	public String getValueAt(int row, int col) {
		Appointment appointment = getObject(row);

		switch(col) {
			case 0: return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, translator.getLocale()).format(appointment.getDate());
			case 1: return appointment.getPlace();
			case 2: return new Integer(appointment.getDuration()).toString() + " min";
			case 3: return appointment.getOccupied() ? translator.translate("AppointmentTableModel.status.occupied") : translator.translate("AppointmentTableModel.status.free");
		}

		return "";
	}

	/**
	 * initializes the table
	 */
	public void setTable(TableController tableCtr) {
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentTableModel.header.date", 0, null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentTableModel.header.place", 1, null, translator.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentTableModel.header.duration", 2, null, translator.getLocale()));
		if(examIsOral) {
			DefaultColumnDescriptor status = new DefaultColumnDescriptor("AppointmentTableModel.header.status", 3, null, translator.getLocale());
			status.setEscapeHtml(EscapeMode.none);
			tableCtr.addColumnDescriptor(status);
		}
	}
}
