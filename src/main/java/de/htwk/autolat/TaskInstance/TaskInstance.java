package de.htwk.autolat.TaskInstance;

import java.util.List;

import org.olat.core.id.Persistable;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.LivingTaskInstance.LivingTaskInstance;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskModule.TaskModule;
import de.htwk.autolat.TaskResult.TaskResult;
import de.htwk.autolat.TaskSolution.TaskSolution;

public interface TaskInstance extends Persistable {
	
	public void setTaskModule(TaskModule taskModule);
	public TaskModule getTaskModule();
	
	public void setConfiguration(Configuration conf);
	public Configuration getConfiguration();
	
	public void setTaskConfiguration(TaskConfiguration taskConf);
	public TaskConfiguration getTaskConfiguration();
	
	public void setStudent(Student student);
	public Student getStudent();
	
	public void setLivingTaskInstance(LivingTaskInstance task);
	public LivingTaskInstance getLivingTaskInstance();
	
	public void setResult(TaskResult result);
	public TaskResult getResult();
	
	public void addSolution(TaskSolution solution);
	public List<TaskSolution> getSolutions();
	public TaskSolution getLastSolution();
	public TaskSolution getBestSolution();
	public void setSolutions(List<TaskSolution> solutions);
	
	public void setInstanceCounter(long counter);
	public long getInstanceCounter();
	
	public void setSolutionCounter(long solutionCounter);
	public long getSolutionCounter();
	public void incrementSolutions();

	public void setFailedAttempts(long failedAttempts);
	public long getFailedAttempts();
	public void incrementFailedAttempts();
	
	public void setLivingInstanceCounter(long livingInstanceCounter);
	public void incrementLivingInstanceCounter();
	public long getLivingInstanceCounter();
}
