package de.htwk.autolat.TaskSolution;

import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;

public abstract class TaskSolutionManager {	

	public abstract TaskSolution createTaskSolution(String solutionText, String evalText, double score, Date solutionDate);
	
	public abstract TaskSolution createAndPersistTaskSolution(String solutionText, String evalText, double score, Date solutionDate);
		//what SQL joins are need?
	public abstract List findTaskSolutionBy();
	
	public abstract TaskSolution loadTaskSolutionByID(long ID);
		
	public abstract void saveTaskSolution(TaskSolution solution);
	
	public abstract void updateTaskSolution(TaskSolution solution);
	
	public abstract boolean deleteTaskSolution(TaskSolution solution);
}
