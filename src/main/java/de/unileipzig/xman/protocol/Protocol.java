package de.unileipzig.xman.protocol;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.core.util.resource.OresHelper;

import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.exam.Exam;

public interface Protocol extends OLATResourceable, CreateInfo, Persistable, ModifiedInfo {

	// Repository types
	public static String ORES_TYPE_NAME = OresHelper.calculateTypeName(Protocol.class);
	
	/* --------------- setter ------------------*/
	
	/**
	 * @param exam, sets the exam
	 */
	public void setExam(Exam exam);
	
	/**
	 * @param app, sets the appointment
	 */
	public void setAppointment(Appointment app);
	
	/**
	 * @param sets the identity
	 */
	public void setIdentity(Identity identity);
	
	/**
	 * @param earmarked, true if the student only could be added as earmarked
	 */
	public void setEarmarked(boolean earmarked);
	
	/**
	 * @param comments, sets the comments
	 */
	public void setComments(String comments);
	
	/**
	 * @param grade, sets the grade
	 */
	public void setGrade(String grade);
	
	/**
	 * @param clawback, sets the clawback
	 */
	public void setClawback(String clawback);
	
	/**
	 * @param examType, sets the Typ of the Exam. first or second
	 */
	public void setExamType(String examType);
	
	/*---------------- getter ------------------*/
	
	/**
	 * @return the exam
	 */
	public Exam getExam();
	
	/**
	 * @return the appointment
	 */
	public Appointment getAppointment();
	
	/**
	 * @return the identity
	 */
	public Identity getIdentity();
	
	/**
	 * @return true if the student is added as earmarked
	 */
	public boolean getEarmarked();

	/**
	 * @return the comments
	 */
	public String getComments();
	
	/**
	 * @return the grade for this student
	 */
	public String getGrade();
	
	/**
	 * @return the clawback for this Exam
	 */
	public String getClawback();
	
	/**
	 * @return the Typ of the Exam
	 */
	public String getExamType();
}
