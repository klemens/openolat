package de.unileipzig.xman.appointment;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.ModifiedInfo;

import de.unileipzig.xman.exam.Exam;

/**
 * 
 * @author
 */
public class AppointmentImpl extends PersistentObject implements Appointment{

	private Exam exam;
	private Date date;
	private int duration;
	private String place;
	private boolean occupied;
	private Date lastModified;
	
	/*
	 * standard constructor
	 */
	public AppointmentImpl() {
		
		occupied = false;
	}
	
	
	/* ------------------- getter -------------------- */
	
	/* 
	 * @see de.xman.appointment.Appointment#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/* 
	 * @see de.xman.appointment.Appointment#getDuration()
	 */
	public int getDuration() {
		return duration;
	}

	/* 
	 * @see de.xman.appointment.Appointment#getExam()
	 */
	public Exam getExam() {
		return exam;
	}

	/* 
	 * @see de.xman.appointment.Appointment#getOccupied()
	 */
	public boolean getOccupied() {
		return occupied;
	}

	/* 
	 * @see de.xman.appointment.Appointment#getPlace()
	 */
	public String getPlace() {
		return place;
	}
	
	/**
	 * @return the key of the appointment
	 */
	public Long getResourceableId() {
		
		return this.getKey();
	}

	/**
	 * @return the resourceableTypeName
	 */
	public String getResourceableTypeName() {
		
		return ORES_TYPE_NAME;
	}

	/* ------------------- setter -------------------- */
	
	/* 
	 * @see de.xman.appointment.Appointment#setDate(java.util.Date)
	 */
	public void setDate(Date date) {
		
		this.date = date;
	}

	/* 
	 * @see de.xman.appointment.Appointment#setDuration(int)
	 */
	public void setDuration(int duration) {
		
		this.duration = duration;
	}

	/* 
	 * @see de.xman.appointment.Appointment#setExam(de.xman.exam.Exam)
	 */
	public void setExam(Exam exam) {
	
		this.exam = exam;
	}

	/* 
	 * @see de.xman.appointment.Appointment#setOccupied(boolean)
	 */
	public void setOccupied(boolean occupied) {
		
		this.occupied = occupied; 
	}

	/* 
	 * @see de.xman.appointment.Appointment#setPlace(java.lang.String)
	 */
	public void setPlace(String place) {
		
		this.place = place;
	}

	public Date getLastModified() {
		
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		
		this.lastModified = lastModified;
	}
	
}
