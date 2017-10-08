package de.unileipzig.xman.appointment.tables;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.appointment.AppointmentManager;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.protocol.Protocol;
import de.unileipzig.xman.protocol.ProtocolManager;

public class AppointmentLecturerOralTableModel extends DefaultTableDataModel<Appointment> {
	
	public static String ACTION_USER = "user";
	public static String ACTION_MULTI_REGISTER = "multi.register";
	public static String ACTION_MULTI_EARMARK = "multi.earmark";
	public static String ACTION_MULTI_UNREGISTER = "multi.unregister";
	public static String ACTION_MULTI_EDIT_RESULT = "multi.edit.result";
	public static String ACTION_MULTI_EDIT_COMMENT = "multi.edit.comment";
	public static String ACTION_MULTI_MAIL = "multi.mail";
	
	private int columnCount;
	
	private Translator translator;
	private Exam exam;
	
	private List<Protocol> examProtocols;

	public AppointmentLecturerOralTableModel(Exam exam, Locale locale) {
		super(new ArrayList<Appointment>());

		setLocale(locale);
		this.translator = Util.createPackageTranslator(Exam.class, getLocale());
		this.exam = exam;
	}
	
	public void update() {
		setObjects(AppointmentManager.getInstance().findAllAppointmentsByExamId(exam.getKey()));
		
		examProtocols = ProtocolManager.getInstance().findAllProtocolsByExam(exam);
	}

	@Override
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Appointment app = getObject(row);
		Protocol protocol = findProtocol(app);

		if(protocol == null) {
			if(col == 3) {
				return translator.translate("AppointmentLecturerOralTableModel.action.add");
			} else if(col >= 4 && col <= 7) {
				return "";
			}
		}

		switch(col) {
			case 0: return app.getDate();
			case 1: return app.getPlace();
			case 2: return new Integer(app.getDuration()) + " min";
			case 3: return StringHelper.escapeHtml(protocol.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, null) + " " + protocol.getIdentity().getUser().getProperty(UserConstants.LASTNAME, null));
			case 4: return protocol.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
			case 5: return protocol.getStudyPath();
			case 6: return protocol.getGrade();
			case 7: return protocol.getComments();
			case 8:	if(protocol != null) {
						if(protocol.getEarmarked())
							return translator.translate("AppointmentLecturerOralTableModel.status.earmarked");
						else
							return translator.translate("AppointmentLecturerOralTableModel.status.registered");
					} else {
						return translator.translate("AppointmentLecturerOralTableModel.status.free");
					}
		}
		
		return null;
	}
	
	public void createColumns(TableController tableController) {
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentLecturerOralTableModel.header.date", 0, null, getLocale()));
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentLecturerOralTableModel.header.location", 1, null, getLocale()));
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentLecturerOralTableModel.header.duration", 2, null, getLocale()));
		DefaultColumnDescriptor user = new DefaultColumnDescriptor("AppointmentLecturerOralTableModel.header.name", 3, ACTION_USER, getLocale());
		user.setEscapeHtml(EscapeMode.none); // Escaped manually in getValueAt
		tableController.addColumnDescriptor(user);
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentLecturerOralTableModel.header.matrikel", 4, null, getLocale()));
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentLecturerOralTableModel.header.studypath", 5, null, getLocale()));
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("AppointmentLecturerOralTableModel.header.result", 6, null, getLocale()));
		DefaultColumnDescriptor comment = new DefaultColumnDescriptor("AppointmentLecturerOralTableModel.header.comment", 7, null, getLocale());
		comment.setEscapeHtml(EscapeMode.antisamy);
		tableController.addColumnDescriptor(comment);
		DefaultColumnDescriptor status = new DefaultColumnDescriptor("AppointmentLecturerOralTableModel.header.status", 8, null, getLocale()) {
			/**
			 * Sort order: free, registered, earmarked
			 */
			@Override
			public int compareTo(final int rowa, final int rowb) {
				Protocol protocolA = findProtocol(getObject(rowa));
				Protocol protocolB = findProtocol(getObject(rowb));
				
				int scoreA = (protocolA == null ? 0 : 1);
				if(protocolA != null)
					scoreA += (protocolA.getEarmarked() ? 1 : 0);
				
				int scoreB = (protocolB == null ? 0 : 1);
				if(protocolB != null)
					scoreB += (protocolB.getEarmarked() ? 1 : 0);
				
				// Order by date (inverse) if equal
				if(scoreA == scoreB)
					return getObject(rowb).getDate().compareTo(getObject(rowa).getDate());
				
				return scoreA - scoreB;
			}
		};
		status.setEscapeHtml(EscapeMode.none);
		tableController.addColumnDescriptor(status);
		
		columnCount = 9;
		
		tableController.addMultiSelectAction("AppointmentLecturerOralTableModel.multi.register", ACTION_MULTI_REGISTER);
		tableController.addMultiSelectAction("AppointmentLecturerOralTableModel.multi.unregister", ACTION_MULTI_UNREGISTER);
		tableController.addMultiSelectAction("AppointmentLecturerOralTableModel.multi.edit.result", ACTION_MULTI_EDIT_RESULT);
		tableController.addMultiSelectAction("AppointmentLecturerOralTableModel.multi.edit.comment", ACTION_MULTI_EDIT_COMMENT);
		tableController.addMultiSelectAction("AppointmentLecturerOralTableModel.multi.mail", ACTION_MULTI_MAIL);
		tableController.addMultiSelectAction("AppointmentLecturerOralTableModel.multi.earmark", ACTION_MULTI_EARMARK);
	}
	
	/**
	 * Get the Protocol to the given Appointment, throws 
	 * @param appointment
	 */
	public Protocol getProtocol(Appointment appointment) {
		Protocol proto = findProtocol(appointment);
		if(proto == null)
			throw new InvalidParameterException("Expected Appointment with associated Protocol");
		return proto;
	}
	
	/**
	 * Checks if the given Appointment has a Protocol
	 * @param appointment
	 */
	public boolean existsProtocol(Appointment appointment) {
		return findProtocol(appointment) != null;
	}

	public boolean hasEarmarkedProtocol() {
		return examProtocols.stream()
			.anyMatch(p -> p.getEarmarked());
	}

	/**
	 * simulates get function of Map
	 * (we cannot use Map because same "physical" objects are contained in different java objects an comparators are not properly overloaded)
	 * TODO implement proper comparators
	 */
	private Protocol findProtocol(Appointment appointment) {
		for(Protocol p : examProtocols) {
			if(p.getAppointment().equalsByPersistableKey(appointment))
				return p;
		}
		return null;
	}

}
