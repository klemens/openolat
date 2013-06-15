package de.htwk.autolat.TaskType;

import java.util.List;

import org.olat.core.CoreSpringFactory;

public abstract class TaskTypeManager {

	public abstract TaskType createTaskType(String taskType, String scoringOrder);
	//need?
	public abstract TaskType createAndPersistTaskType(String taskType, String scoringOrder);

	public abstract TaskType loadTaskTypeByID(long ID);
	
	public abstract TaskType findTaskTypeByType(String type);
	
	public abstract void saveTaskType(TaskType type);
	
	public abstract void updateTaskType(TaskType type);
	
	public abstract boolean deleteTaskType(TaskType type);
	
	public abstract void updateTaskTypeScoringOrder(TaskType taskType, String scoringOrder);
	
	public abstract void saveOrUpdateTaskType(TaskType taskType);
}
