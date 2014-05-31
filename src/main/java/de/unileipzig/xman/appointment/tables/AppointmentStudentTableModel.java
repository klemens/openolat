package de.unileipzig.xman.appointment.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.appointment.AppointmentManager;
import de.unileipzig.xman.esf.ElectronicStudentFile;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.exam.ExamDBManager;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;

public class AppointmentStudentTableModel extends DefaultTableDataModel<Appointment> {

	static public String ACTION_SUBSCRIBE = "subscribe";
	static public String ACTION_UNSUBSCRIBE = "unsubscribe";
	
	private int columnCount;
	
	private Exam exam;
	private ElectronicStudentFile esf;
	Translator translator;
	List<Protocol> userProtocols;
	
	private boolean subscribedToExam;
	private boolean showSubscription;
	private boolean showUnsubscription;

	public AppointmentStudentTableModel(Exam exam, ElectronicStudentFile esf, Locale locale) {
		super(new ArrayList<Appointment>());
		
		setLocale(locale);
		this.translator = Util.createPackageTranslator(Exam.class, getLocale());
		this.exam = exam;
		this.esf = esf;

		showSubscription = ExamDBManager.getInstance().canSubscribe(exam);
		showUnsubscription = ExamDBManager.getInstance().canUnsubscribe(exam);

		update();
	}
	
	public void update() {
		userProtocols = ProtocolManager.getInstance().findAllProtocolsByIdentityAndExam(esf.getIdentity(), exam);
		subscribedToExam = ProtocolManager.getInstance().isIdentitySubscribedToExam(esf.getIdentity(), exam);
		
		setObjects(AppointmentManager.getInstance().findAllAppointmentsByExamId(exam.getKey()));
	}

	@Override
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Appointment app = getObject(row);
		
		switch(col) {
			case 0: return app.getDate();
			case 1: return app.getPlace();
			case 2: return new Integer(app.getDuration()) + " min";
			case 3:	if(!app.getOccupied()) {
						if(exam.getIsOral() && !exam.getIsMultiSubscription()) {
							return subscribedToExam ? "" : translator.translate("AppointmentStudentTableModel.subscribe");
						} else {
							return isSubscribedToAppointment(app) ? "" : translator.translate("AppointmentStudentTableModel.subscribe");
						}
					} else {
						return "";
					}
			case 4: return isSubscribedToAppointment(app) ? translator.translate("AppointmentStudentTableModel.unsubscribe") : "";
			case 5:	if(isSubscribedToAppointment(app)) {
						if(getProtocol(app).getEarmarked())
							return translator.translate("AppointmentStudentTableModel.status.earmarked");
						else
							return translator.translate("AppointmentStudentTableModel.status.subscribed");
					} else {
						if(app.getOccupied())
							return translator.translate("AppointmentStudentTableModel.status.unavailable");
						else
							return translator.translate("AppointmentStudentTableModel.status.unsubscribed");
					}
		}
		
		return null;
	}

	public void createColumns(TableController tableController) {
		columnCount = 4;

		tableController.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentStudentTableModel.header.date", 0, null, getLocale())); // locale needed for date formatting
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentStudentTableModel.header.location", 1, null, null));
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentStudentTableModel.header.duration", 2, null, null));

		if(showSubscription) {
			tableController.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentStudentTableModel.header.subscribe", 3, ACTION_SUBSCRIBE, null));
			++columnCount;
		}
		if(showUnsubscription) {
			tableController.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentStudentTableModel.header.unsubscribe", 4, ACTION_UNSUBSCRIBE, null));
			++columnCount;
		}

		DefaultColumnDescriptor status = new DefaultColumnDescriptor("AppointmentStudentTableModel.header.status", 5, null, null);
		status.setEscapeHtml(EscapeMode.none);
		tableController.addColumnDescriptor(status);
	}
	
	private boolean isSubscribedToAppointment(Appointment appointment) {
		for(Protocol p : userProtocols) {
			if(p.getAppointment().equalsByPersistableKey(appointment))
				return true;
		}
		return false;
	}
	
	private Protocol getProtocol(Appointment appointment) {
		for(Protocol p : userProtocols) {
			if(p.getAppointment().equalsByPersistableKey(appointment))
				return p;
		}
		return null;
	}

}
