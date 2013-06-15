package de.htwk.autolat.TaskResult;

import java.util.*;

import org.olat.core.commons.persistence.DBFactory;

public class TaskResultManagerImpl extends TaskResultManager {
	
	private static final TaskResultManagerImpl INSTANCE = new TaskResultManagerImpl();
	
	private TaskResultManagerImpl() {
		//nothing to do here
	}

	@Override
	public TaskResult createTaskResult(Date maxScoreDate, double maxScore, String maxScoreTaskText, String taskSolution,
			boolean hasPassed) {

		TaskResult result = new TaskResultImpl();
		result.setMaxScoreDate(maxScoreDate);
		result.setMaxScore(maxScore);
		result.setMaxScoreSolutionText(taskSolution);
		result.setMaxScoreTaskText(maxScoreTaskText);
		result.setHasPassed(hasPassed);
		return result;
	}
	
	@Override
	public TaskResult createAndPersistTaskResult(Date maxScoreDate, double maxScore, String maxScoreTaskText, String taskSolution,
			boolean hasPassed) {
		
		TaskResult result = createTaskResult(maxScoreDate, maxScore, maxScoreTaskText, taskSolution, hasPassed);
		saveTaskResult(result);
		return result;
	}

	@Override
	public boolean deleteTaskResult(TaskResult result) {
		try{
			DBFactory.getInstance().deleteObject(result);
			return true;
		}catch(Exception e) {
			return false;
		}
	}

	@Override
	public List findTaskResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveTaskResult(TaskResult result) {
		DBFactory.getInstance().saveObject(result);
	}

	@Override
	public void updateTaskResult(TaskResult result) {
		DBFactory.getInstance().updateObject(result);
	}

	@Override
	public TaskResult loadTaskResultByID(long ID) {
		return (TaskResult)DBFactory.getInstance().loadObject(TaskResultImpl.class, ID);
	}
	
	public static TaskResultManagerImpl getInstance() {
		return INSTANCE;
	}

}
