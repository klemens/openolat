package de.htwk.autolat.LivingTaskInstance;

import java.util.Date;

import org.olat.core.CoreSpringFactory;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.TaskInstance.TaskInstance;

/**
 * The Class LivingTaskInstanceManager provides the general method interfaces for the management of
 * living task instances.
 * 
 * <P>
 * Initial Date:  20.11.2009 <br>
 * @author Tom
 */
public abstract class LivingTaskInstanceManager {
	
	/**
	 * Creates a living task instance.
	 * 
	 * @param signature The signature
	 * @param taskText The task text
	 * @param internalTaskText The internal task text provided by the autotool-server
	 * @param sampleSolution The sample solution
	 * @param sampleDocumentation The sample documentation
	 * @param creationDate The creation date
	 * @return The living task instance to be created
	 */
	public abstract LivingTaskInstance createLivingTaskInstance(String signature, String taskText, String internalTaskText, String sampleSolution, String sampleDocumentation, Date creationDate);
	
	/**
	 * Creates and persists a living task instance.
	 * 
	 * @param signature The signature
	 * @param taskText The task text
	 * @param internalTaskText The internal task text provided by the autotool-server
	 * @param sampleSolution The sample solution
	 * @param sampleDocumentation The sample documentation
	 * @param creationDate The creation date
	 * @return The living task instance to be created and persisted
	 */
	public abstract LivingTaskInstance createAndPersistLivingTaskInstance(String signature, String taskText, String internalTaskText, String sampleSolution, String sampleDocumentation, Date creationDate);
	
	/**
	 * Load a living task instance by id.
	 * 
	 * @param ID The iD
	 * @return The living task instance to be loaded
	 */
	public abstract LivingTaskInstance loadLivingTaskInstanceByID(long ID);
	
	/**
	 * Save a living task instance.
	 * 
	 * @param livingTaskInstance The living task instance to save
	 */
	public abstract void saveLivingTaskInstance(LivingTaskInstance livingTaskInstance);
	
	/**
	 * Update a living task instance.
	 * 
	 * @param livingTaskInstance The living task instance to update
	 */
	public abstract void updateLivingTaskInstance(LivingTaskInstance livingTaskInstance);
	
	/**
	 * Delete a living task instance.
	 * 
	 * @param livingTaskInstance The living task instance to delete
	 * @return True, if the deletion process was successful
	 */
	public abstract boolean deleteLivingTaskInstance(LivingTaskInstance livingTaskInstance);
}
