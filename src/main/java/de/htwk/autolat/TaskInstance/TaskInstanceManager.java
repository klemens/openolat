package de.htwk.autolat.TaskInstance;

import java.util.List;

import org.olat.core.CoreSpringFactory;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.LivingTaskInstance.LivingTaskInstance;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskModule.TaskModule;
import de.htwk.autolat.TaskResult.TaskResult;
import de.htwk.autolat.TaskSolution.TaskSolution;


public abstract class TaskInstanceManager {
	
	public abstract TaskInstance createAndPersistTaskInstance(List solutions, Configuration conf, int counter, LivingTaskInstance livingInstance,
			TaskResult result, Student student, TaskConfiguration taskConf, TaskModule module);
	
	public abstract TaskInstance createTaskInstance(List solutions, Configuration conf, int counter, LivingTaskInstance livingInstance, 
			TaskResult result, Student student, TaskConfiguration taskConf, TaskModule module);
	
	public abstract TaskInstance loadTaskInstanceByID(long ID);
	
	public abstract void saveTaskInstance(TaskInstance instance);
	
	public abstract boolean deleteTaskInstance(TaskInstance instance);
	
	public abstract void updateTaskInstance(TaskInstance instance);

	public abstract List<TaskInstance> findTaskInstancesByConfiguration(Configuration conf);
	
	public abstract boolean hasSolution(TaskInstance taskInstance); 
	
	public abstract TaskSolution getBestSolutionInTaskInstance(TaskInstance taskInstance);
	
	public abstract TaskSolution getLatestSolutionInTaskInstance(TaskInstance taskInstance);
	
	public abstract TaskSolution getLatestCorrectSolutionInTaskInstance(TaskInstance taskInstance);
	
	public abstract boolean addTaskSolution(TaskInstance taskInstance, TaskSolution taskSolution);
	
	public abstract void updateInstanceResult(TaskInstance taskInstance);
	
	public abstract void deleteLivingInstancesInTaskInstances(List<TaskInstance> taskInstances, boolean keepResults);

	public abstract void createNewLivingTaskInstancesInTaskInstances(List<TaskInstance> taskInstances, Configuration conf);
}
