package de.unileipzig.xman.protocol;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

import de.unileipzig.xman.appointment.Appointment;
import de.unileipzig.xman.exam.Exam;

public class ProtocolImpl extends PersistentObject implements Protocol {

	private Exam exam;
	private Appointment app;
	private Identity identity;
	private boolean earmarked;
	private String comments;
	private String grade;
	private Date lastModified;
	private String clawback;
	private String examType;
	
	/**
	 * constructor
	 */
	public ProtocolImpl(){}
	
	/* -------------- getter -------------- */
	
	/* 
	 * @see org.olat.core.id.OLATResourceable#getResourceableId()
	 */
	public Long getResourceableId() {
		
		return this.getKey();
	}
	
	/* 
	 * @see de.xman.protocol.Protocol#getAppointment()
	 */
	public Appointment getAppointment() {
	
		return this.app;
	}

	/* 
	 * @see de.xman.protocol.Protocol#getComments()
	 */
	public String getComments() {
		
		return this.comments;
	}

	/* 
	 * @see de.xman.protocol.Protocol#getEarmarked()
	 */
	public boolean getEarmarked() {
		
		return this.earmarked;
	}

	/* 
	 * @see de.xman.protocol.Protocol#getExam()
	 */
	public Exam getExam() {
		
		return this.exam;
	}

	/* 
	 * @see de.xman.protocol.Protocol#getGrade()
	 */
	public String getGrade() {
		
		return this.grade;
	}

	/* 
	 * @see de.xman.protocol.Protocol#getIdentity()
	 */
	public Identity getIdentity() {
		
		return this.identity;
	}

	/* --------------- setter ---------------- */
	
	/* 
	 * @see de.xman.protocol.Protocol#setAppointment(de.xman.appointment.Appointment)
	 */
	public void setAppointment(Appointment app) {
		
		this.app = app;
	}

	/* 
	 * @see de.xman.protocol.Protocol#setComments(java.lang.String)
	 */
	public void setComments(String comments) {
	
		this.comments = comments;
	}

	/* 
	 * @see de.xman.protocol.Protocol#setEarmarked(boolean)
	 */
	public void setEarmarked(boolean earmarked) {
		
		this.earmarked = earmarked;
	}

	/* 
	 * @see de.xman.protocol.Protocol#setExam(de.xman.exam.Exam)
	 */
	public void setExam(Exam exam) {
		
		this.exam = exam;
	}

	/* 
	 * @see de.xman.protocol.Protocol#setGrade(double)
	 */
	public void setGrade(String grade) {
		
		this.grade = grade;
	}

	/* 
	 * @see de.xman.protocol.Protocol#setIdentity(org.olat.core.id.Identity)
	 */
	public void setIdentity(Identity identity) {

		this.identity = identity;
	}
	
	public Date getLastModified() {
		
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		
		this.lastModified = lastModified;
	}

	public String getResourceableTypeName() {
	
		return Protocol.ORES_TYPE_NAME;
	}

	@Override
	public void setClawback(String clawback) {
		this.clawback = clawback;		
	}

	@Override
	public void setExamType(String examType) {
		this.examType = examType;
	}

	@Override
	public String getClawback() {
		return clawback;
	}

	@Override
	public String getExamType() {
		// TODO Auto-generated method stub
		return examType;
	}
	
}
