package de.htwk.autolat.TaskInstance;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.olat.core.commons.persistence.PersistentObject;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.LivingTaskInstance.LivingTaskInstance;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskModule.TaskModule;
import de.htwk.autolat.TaskResult.TaskResult;
import de.htwk.autolat.TaskSolution.TaskSolution;

public class TaskInstanceImpl extends PersistentObject implements TaskInstance {
	
	private List<TaskSolution> solutions;
	
	private long solutionCounter;
	private long failedAttempts;
	private long livingInstanceCounter;
	private Configuration configuration;
	private long instanceCounter;
	private LivingTaskInstance livingTaskInstance;
	private TaskResult result;
	private Student student;
	private TaskConfiguration taskConfiguration;
	private TaskModule taskModule;
	
	public TaskInstanceImpl() {
		solutionCounter = 0;
		failedAttempts = 0;
		livingInstanceCounter = 0;
	}
	
	public void addSolution(TaskSolution solution) {
		if(solutions == null) {
			solutions = new ArrayList<TaskSolution>();
			solutions.add(solution);
		}
		else {
			solutions.add(solution);
		}
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public long getInstanceCounter() {
		return instanceCounter;
	}

	public LivingTaskInstance getLivingTaskInstance() {
		return livingTaskInstance;
	}

	public TaskResult getResult() {
		return result;
	}

	public List<TaskSolution> getSolutions() {
		return (solutions==null ? new ArrayList<TaskSolution>() : solutions);
	}
	
	public TaskSolution getLastSolution() {
		TaskSolution latestSolution = TaskInstanceManagerImpl.getInstance().getLatestSolutionInTaskInstance(this);
		return latestSolution;
	}
	
	public TaskSolution getBestSolution() {
		// can be shortened if only two solutions are saved for each taskInstance
		/*
		double maxScore = 0;
		TaskSolution bestSolution = solutions.get(0);
		for(TaskSolution sol : solutions)
		{
			if(sol.getScore() >= maxScore)
			{
				maxScore = sol.getScore();
				bestSolution = sol;
			}
		}
		return bestSolution;
		*/
		TaskSolution bestSolution = TaskInstanceManagerImpl.getInstance().getBestSolutionInTaskInstance(this);
		return bestSolution;
	
	}

	public Student getStudent() {
		return student;
	}

	public TaskConfiguration getTaskConfiguration() {
		return taskConfiguration;
	}

	public TaskModule getTaskModule() {
		return taskModule;
	}

	public void setConfiguration(Configuration conf) {
		this.configuration = conf;
	}

	public void setInstanceCounter(long counter) {
		this.instanceCounter = counter;
	}

	public void setLivingTaskInstance(LivingTaskInstance task) {
		this.livingTaskInstance = task;
	}

	public void setResult(TaskResult result) {
		this.result = result;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public void setTaskConfiguration(TaskConfiguration taskConf) {
		this.taskConfiguration = taskConf;
	}

	public void setTaskModule(TaskModule taskModule) {
		this.taskModule = taskModule;
	}

	public void setSolutions(List<TaskSolution> solutions) {
		this.solutions = solutions;
	}

	public void setSolutionCounter(long solutionCounter) {
		this.solutionCounter = solutionCounter;
	}

	public long getSolutionCounter() {
		return solutionCounter;
	}

	public void setFailedAttempts(long failedAttempts) {
		this.failedAttempts = failedAttempts;
	}

	public long getFailedAttempts() {
		return failedAttempts;
	}

	public void incrementFailedAttempts() {
		failedAttempts++;
	}

	public void incrementSolutions() {
		solutionCounter++;
	}

	public long getLivingInstanceCounter() {
		return livingInstanceCounter;
	}

	public void incrementLivingInstanceCounter() {
		livingInstanceCounter++;
	}

	public void setLivingInstanceCounter(long livingInstanceCounter) {
		this.livingInstanceCounter = livingInstanceCounter;
	}

}
