package de.htwk.autolat.TaskModule;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.htwk.autolat.Configuration.Configuration;

public abstract class TaskModuleManager {	

	public abstract TaskModule createTaskModule(long duration, Date endDate, long maxCount,	TaskModule nextModule,	Configuration conf);
	
	public abstract TaskModule createAndPersistTaskModule(long duration, Date endDate, long maxCount,	TaskModule nextModule,	Configuration conf);
		
	public abstract TaskModule loadTaskModuleByID(long ID);
	
	//TODO welche joins werden benötigt?
	public abstract List findTaskModule();
		
	public abstract void saveTaskModule(TaskModule module);
	
	public abstract void updateTaskModule(TaskModule module);
	
	public abstract boolean deleteTaskModule(TaskModule module);
	
	public abstract String getDurationValue(long duration);
	
	public abstract String getDurationValueWithLabels(long duration, Locale locale);
	
	public abstract long parseDurationValue(String value) throws Exception;
	
	public abstract TaskModule getNextTaskModule(long courseID, long courseNodeID, TaskModule previousModule);
	
	public abstract boolean evaluateTaskModuleConditions (TaskModule taskModule, long counter, Date creationDate);

	public abstract Date getDurationEndDate (TaskModule taskModule, Date creationDate);
	
	public abstract Date getTaskModuleEndDate (TaskModule taskModule, Date creationDate);
	
}

