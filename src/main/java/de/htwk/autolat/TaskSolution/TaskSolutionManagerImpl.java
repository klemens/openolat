package de.htwk.autolat.TaskSolution;

import java.util.List;
import java.util.Date;

import org.olat.core.commons.persistence.DBFactory;

public class TaskSolutionManagerImpl extends TaskSolutionManager {
	
	private static final TaskSolutionManagerImpl INSTANCE = new TaskSolutionManagerImpl();
	
	private TaskSolutionManagerImpl() {
		//nothing to do here
	}

	@Override
	public TaskSolution createAndPersistTaskSolution(String solutionText, String evalText, double score, Date solutionDate) {
		TaskSolution sol = createTaskSolution(solutionText, evalText, score, solutionDate);
		saveTaskSolution(sol);
		return sol;	
	}

	@Override
	public TaskSolution createTaskSolution(String solutionText, String evalText, double score, Date solutionDate) {
		TaskSolution sol = new TaskSolutionImpl();
		sol.setEvaluationText(evalText);
		sol.setScore(score);
		sol.setSolutionText(solutionText);
		sol.setSolutionDate(solutionDate);
		return sol;
	}

	@Override
	public boolean deleteTaskSolution(TaskSolution solution) {
		try{
			DBFactory.getInstance().deleteObject(solution);
			return true;
		}catch (Exception e) {
			return false;

		}
	}

	@Override
	public List findTaskSolutionBy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TaskSolution loadTaskSolutionByID(long ID) {
		return (TaskSolution) DBFactory.getInstance().loadObject(TaskSolutionImpl.class, ID);
	}

	@Override
	public void saveTaskSolution(TaskSolution solution) {
		DBFactory.getInstance().saveObject(solution);
	}

	@Override
	public void updateTaskSolution(TaskSolution solution) {
		DBFactory.getInstance().updateObject(solution);
	}
	
	public static TaskSolutionManagerImpl getInstance() {
		return INSTANCE;
	}

}
