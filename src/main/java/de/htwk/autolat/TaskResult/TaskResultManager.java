package de.htwk.autolat.TaskResult;

import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;

import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskSolution.TaskSolution;

public abstract class TaskResultManager {
	
	public abstract TaskResult createTaskResult(Date maxScoreDate, double maxScore, String maxScoreTaskText, String taskSolution, boolean hasPassed);

	public abstract TaskResult createAndPersistTaskResult(Date maxScoreDate, double maxScore, String maxScoreTaskText, String taskSolution,
			boolean hasPassed);
	
	public abstract TaskResult loadTaskResultByID(long ID);
	
	public abstract void saveTaskResult(TaskResult result);
	
	public abstract boolean deleteTaskResult(TaskResult result);
	
	public abstract void updateTaskResult(TaskResult result);
	//TODO
	public abstract List findTaskResult();
	
}
