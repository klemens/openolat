package de.htwk.autolat.TaskModule;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;

import de.htwk.autolat.Configuration.Configuration;

public class TaskModuleImpl extends PersistentObject implements TaskModule {
	
	private long taskDuration;
	private Date taskEndDate;
	private long maxCount;
	private TaskModule nextModule;
	private Configuration configuration;
	

	public long getTaskDuration() {
		return taskDuration;
	}

	public Date getTaskEndDate() {
		return taskEndDate;
	}

	public void setTaskDuration(long time) {
		this.taskDuration = time;
		
	}

	public void setTaskEndDate(Date endDate) {
		this.taskEndDate = endDate;
		
	}
	public long getMaxCount() {
		return maxCount;
	}

	public TaskModule getNextModule() {
		return nextModule;
	}

	public boolean hasNext() {
		if(nextModule == null)
			return false;
		return true;
	}

	public void setMaxCount(long maxCount) {
		this.maxCount = maxCount;
	}

	public void setNextModule(TaskModule next) {
		this.nextModule = next;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration conf) {
		this.configuration = conf;
	}
	
}
