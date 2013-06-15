package de.htwk.autolat.TaskModule;

import java.util.Date;
import org.olat.core.id.Persistable;

import de.htwk.autolat.Configuration.Configuration;
/**
 * 
 * Description:<br>
 * TODO: Joerg Class Description for TaskModule
 * 
 * <P>
 * Initial Date:  27.10.2009 <br>
 * @author Joerg
 */
public interface TaskModule extends Persistable {
	/**
	 * Sets an end date for the random living tasks
	 * @param endDate
	 */
	public void setTaskEndDate(Date endDate);
	/**
	 * 
	 * @return end date for a living task
	 */
	public Date getTaskEndDate();
	/**
	 * Sets the duration time for the random living tasks
	 * @param time 
	 */
	public void setTaskDuration(long time);
	/**
	 * 
	 * @return duration for a living task
	 */
	public long getTaskDuration();
	
	public void setNextModule(TaskModule next);
	
	public TaskModule getNextModule();
	
	public boolean hasNext();
	
	public void setMaxCount(long maxCount);
	
	public long getMaxCount();
	
	public void setConfiguration(Configuration conf);
	
	public Configuration getConfiguration();
}
