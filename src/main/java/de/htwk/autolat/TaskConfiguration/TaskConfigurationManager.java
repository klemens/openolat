package de.htwk.autolat.TaskConfiguration;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;

import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskType.TaskType;

public abstract class TaskConfigurationManager {
	
	public abstract TaskConfiguration createTaskConfiguration(List<TaskInstance> taskInstanceList, String configurationText, String documentationText, String descriptionText,
			String authorComment, String signature, String lastError, boolean isAltered, TaskType taskType);
	
	public abstract TaskConfiguration createAndPersistTaskConfiguration(List<TaskInstance> taskInstanceList, String configurationText, String documentationText, String descriptionText,
			String authorComment, String signature, String lastError, boolean isAltered, TaskType taskType);
	
	public abstract TaskConfiguration loadTaskConfigurationByID(long ID);
		
	public abstract void saveTaskConfiguration(TaskConfiguration taskConf);
	
	public abstract void updateTaskConfiguration(TaskConfiguration taskConf);
	
	public abstract boolean deleteTaskConfiguration(TaskConfiguration taskConf);

	public abstract void handleUnusedDuplicates();
	
	public abstract List<TaskConfiguration> getAllTaskConfigurations();
	
	public abstract List<TaskConfiguration> getAllAlteredTaskConfigurations(TaskConfiguration taskConf);

	public abstract void saveOrUpdateTaskConfiguration(TaskConfiguration taskConfiguration);
}
