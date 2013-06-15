package de.htwk.autolat.LivingTaskInstance;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;

/**
 * The Class LivingTaskInstanceImpl as an implementation for a living task instance.
 * 
 * <P>
 * Initial Date:  20.11.2009 <br>
 * @author Tom
 */
public class LivingTaskInstanceImpl extends PersistentObject implements LivingTaskInstance {

	/** The signature. */
	private String signature;
	
	/** The task text. */
	private String taskText;
	
	/** The internal task text. */
	private String internalTaskText;
	
	/** The creation date. */
	private Date creationDate;
	
	/** The sample solution. */
	private String sampleSolution;
	
	/** The sample documentation. */
	private String sampleDocumentation;

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstance#getSignature()
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstance#getTaskText()
	 */
	public String getTaskText() {
		return taskText;
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstance#setSignature(java.lang.String)
	 */
	public void setSignature(String sig) {
		this.signature = sig;
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstance#setTaskText(java.lang.String)
	 */
	public void setTaskText(String text) {
		this.taskText = text;
	}

	/**
	 * @see org.olat.core.commons.persistence.PersistentObject#getCreationDate()
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstance#setCreationDate(java.util.Date)
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstance#getSampleSolution()
	 */
	public String getSampleSolution() {
		return sampleSolution;
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstance#setSampleSolution(java.lang.String)
	 */
	public void setSampleSolution(String sampleSolution) {
		this.sampleSolution = sampleSolution;
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstance#getSampleDocumentation()
	 */
	public String getSampleDocumentation() {
		return sampleDocumentation;
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstance#setSampleDocumentation(java.lang.String)
	 */
	public void setSampleDocumentation(String sampleDocumentation) {
		this.sampleDocumentation = sampleDocumentation;
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstance#getInternalTaskText()
	 */
	public String getInternalTaskText() {
		return internalTaskText;
	}

	/**
	 * @see de.htwk.autolat.LivingTaskInstance.LivingTaskInstance#setInternalTaskText(java.lang.String)
	 */
	public void setInternalTaskText(String internalTaskText) {
		this.internalTaskText = internalTaskText;
	}

}
