package de.htwk.autolat.LivingTaskInstance;

import java.util.Date;

import org.olat.core.id.Persistable;

import de.htwk.autolat.TaskInstance.TaskInstance;

/**
 * The Interface LivingTaskInstance as a general interface for a living task instance.
 * 
 * <P>
 * Initial Date:  20.11.2009 <br>
 * @author Tom
 */
public interface LivingTaskInstance extends Persistable{
	
	/**
	 * Sets the signature.
	 * 
	 * @param sig The new signature
	 */
	public void setSignature(String sig);
	
	/**
	 * Gets the signature.
	 * 
	 * @return The signature
	 */
	public String getSignature();
	
	/**
	 * Sets the task text.
	 * 
	 * @param text The new task text
	 */
	public void setTaskText(String text);
	
	/**
	 * Gets the task text.
	 * 
	 * @return The task text
	 */
	public String getTaskText();
	
	/**
	 * Gets the creation date.
	 * 
	 * @return The creation date
	 */
	public Date getCreationDate();
	
	/**
	 * Sets the creation date.
	 * 
	 * @param creationDate The new creation date
	 */
	public void setCreationDate(Date creationDate);
	
	/**
	 * Gets the sample solution.
	 * 
	 * @return The sample solution
	 */
	public String getSampleSolution();
	
	/**
	 * Sets the sample solution.
	 * 
	 * @param sampleSolution The new sample solution
	 */
	public void setSampleSolution(String sampleSolution);

	/**
	 * Gets the sample documentation.
	 * 
	 * @return The sample documentation
	 */
	public String getSampleDocumentation();
	
	/**
	 * Sets the sample documentation.
	 * 
	 * @param sampleDocumentation The new sample documentation
	 */
	public void setSampleDocumentation(String sampleDocumentation);
	
	/**
	 * Gets the internal task text.
	 * 
	 * @return The internal task text
	 */
	public String getInternalTaskText();
	
	/**
	 * Sets the internal task text.
	 * 
	 * @param internalTaskText The new internal task text
	 */
	public void setInternalTaskText(String internalTaskText);
	
}
