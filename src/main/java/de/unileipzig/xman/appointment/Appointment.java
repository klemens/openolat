package de.unileipzig.xman.appointment;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.util.resource.OresHelper;

import de.unileipzig.xman.exam.Exam;

/**
 * 
 * @author
 */
public interface Appointment extends OLATResourceable, CreateInfo, ModifiedInfo, Persistable {
	
	// Repository types
	public static String ORES_TYPE_NAME = OresHelper.calculateTypeName(Appointment.class);
	
	
	/*------------------ setter ---------------------*/
	
	/**
	 * @param sets the exam to this appoinment
	 */
	public void setExam(Exam exam);
	
	/**
	 * @date the date the exam takes place
	 */
	public void setDate(Date date);
	
	/**
	 * @param sets the duration of the appointment
	 */
	public void setDuration(int duration);
	
	/**
	 * @param the place the appointment takes place
	 */
	public void setPlace(String place);
	
	/**
	 * @param true if the appointment is already occupied
	 */
	public void setOccupied(boolean occupied);
	
	
	/*------------------ getter ---------------------*/
	
	/**
	 * @return the appropriate exam
	 */
	public Exam getExam();
	
	/**
	 * @return the date the appointment takes place
	 */
	public Date getDate();
	
	/**
	 * @return the duration of the appointment
	 */
	public int getDuration();
	
	/**
	 * @return the place the appointment takes place
	 */
	public String getPlace();
	
	/**
	 * @return the status of the appointment, occupied or free
	 */
	public boolean getOccupied();
	
}
