package de.unileipzig.xman.appointment;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.Tracing;

import de.unileipzig.xman.appointment.AppointmentImpl;
import de.unileipzig.xman.exam.Exam;
import de.unileipzig.xman.protocol.Protocol;

/**
 * 
 * @author gerb
 */
public class AppointmentManager {
	
	/**
	 * for singleton
	 */
	private static AppointmentManager INSTANCE = new AppointmentManager();
	
	/**
	 * for singleton
	 */
	private AppointmentManager() {
		// singleton
	}
	
	/**
	 * for singleton
	 * @return the one and only instance of this manager
	 */
	public static AppointmentManager getInstance(){
		return INSTANCE;
	}
	
	
	
	/*----------------- appointment -------------------*/
	
	/**
	 * creates a new appointment
	 * @return a new appointmentImpl object
	 */
	public Appointment createAppointment(){
		
		return new AppointmentImpl();
	}
	
	/**
	 * persists it in the database and returns it.
	 * @return the new created appointment
	 */
	public void saveAppointment(Appointment app){
	
		DBFactory.getInstance().saveObject(app);
		Tracing.createLoggerFor(AppointmentManager.class).info("New appointment with the id " + app.getKey() + " was created.");
	}
	
	/**
	 * Deletes the given appointment.
	 * @param appointment, which is to be deleted
	 */
	public void deleteAppointment(Appointment app) {
		
		if ( app != null ) DBFactory.getInstance().deleteObject(app);
	}
	
	/**
	 * deletes all appointments for the given exam
	 * @param exam the exam which will be delete
	 */
	public void deleteAllAppointmentsByExam(Exam exam) {
		if ( exam != null ) {
			List<Appointment> appList = AppointmentManager.getInstance().findAllAvailableAppointmentsByExam(exam);
			for ( Appointment app : appList ) {
				AppointmentManager.getInstance().deleteAppointment(app);
			}
		}
	}
	
	/**
	 * Updates an existing appointment
	 * @param app - the appointment, which is to be updated
	 */
	public void updateAppointment(Appointment app){
		
		DBFactory.getInstance().updateObject(app);
	}
	
	/**
	 * finds an appointment by the given id
	 * @param id the id of the appointment
	 * @return the specified appointment or null if there is no appointment with this id
	 */
	public Appointment findAppointmentByID(Long id) {
		return DBFactory.getInstance().getCurrentEntityManager().find(AppointmentImpl.class, id);
	}
	
	/**
	 * finds all appointments to the specified exam
	 * @param examId the id of the exam
	 * @return all appointments which belong to the exam(Id), or empty list of there were no appointments
	 */
	public List<Appointment> findAllAppointmentsByExam(Exam exam) {
		String query = "from de.unileipzig.xman.appointment.AppointmentImpl as app where app.exam = :exam";
		return DBFactory.getInstance().getCurrentEntityManager()
			.createQuery(query, Appointment.class)
			.setParameter("exam", exam)
			.getResultList();
	}
	
	/**
	 * returns all free appointments to a given exam
	 * @param examId the id of the exam
	 * @return a list of available appointments, or null if there were no appointments 
	 */
	public List<Appointment> findAllAvailableAppointmentsByExam(Exam exam) {
		List<Appointment> tempList = AppointmentManager.getInstance().findAllAppointmentsByExam(exam);
		List<Appointment> appList = new Vector<Appointment>();
		for ( Appointment app : tempList ) {
			if ( !app.getOccupied() ) {
				appList.add(app);
			}
		}
		return appList;
	}
	
	/**
	 * persists a whole set of appointments
	 * @param exam all appointments belong to
	 * @param startDate of the first appointment
	 * @param place of all appointments
	 * @param count: number of appointements
	 * @param minutes: duration of one appointment
	 * @param breakMinutes: beetween two appointments
	 */
	public void saveAppointmentsByWizard(Exam exam, Date startDate, String place, int count, long minutes, long breakMinutes) {
		Appointment app;
		Date date = startDate;
		for( int i = 0; i < count; i++ ) {
			app = new AppointmentImpl();
			app.setDuration((int)minutes);
			app.setDate(date);
			app.setExam(exam);
			app.setPlace(place);
			this.saveAppointment(app);
			date = new Date( date.getTime() + ( minutes * 60 * 1000) + ( breakMinutes * 60 * 1000 ));
		}
	}
}
