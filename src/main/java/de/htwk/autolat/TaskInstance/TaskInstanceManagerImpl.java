package de.htwk.autolat.TaskInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.JDOMException;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.UserConstants;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.Connector.AutolatConnectorException;
import de.htwk.autolat.Connector.BBautOLATConnector;
import de.htwk.autolat.LivingTaskInstance.LivingTaskInstance;
import de.htwk.autolat.LivingTaskInstance.LivingTaskInstanceManagerImpl;
import de.htwk.autolat.Student.Student;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskModule.TaskModule;
import de.htwk.autolat.TaskResult.TaskResult;
import de.htwk.autolat.TaskResult.TaskResultManagerImpl;
import de.htwk.autolat.TaskSolution.TaskSolution;
import de.htwk.autolat.TaskSolution.TaskSolutionManagerImpl;
import de.htwk.autolat.TaskType.TaskTypeImpl;

public class TaskInstanceManagerImpl extends TaskInstanceManager {
	
	private static final TaskInstanceManagerImpl INSTANCE = new TaskInstanceManagerImpl();
	
	private TaskInstanceManagerImpl() {
		//nothing to do here
	}

	@Override
	public TaskInstance createTaskInstance(List solutions, Configuration conf, int counter, LivingTaskInstance livingInstance,
			TaskResult result, Student student, TaskConfiguration taskConf, TaskModule module) {
		
		TaskInstance instance = new TaskInstanceImpl();
		instance.setConfiguration(conf);
		instance.setLivingTaskInstance(livingInstance);
		instance.setInstanceCounter(counter);
		instance.setResult(result);
		instance.setStudent(student);
		instance.setTaskConfiguration(taskConf);
		instance.setTaskModule(module);
		for(int i = 0; i < (solutions == null ? 0 : solutions.size()); i++) {
			instance.addSolution((TaskSolution)solutions.get(i));
		}
		return instance;
	}
	
	@Override
	public TaskInstance createAndPersistTaskInstance(List solutions, Configuration conf, int counter, LivingTaskInstance livingInstance,
			TaskResult result, Student student, TaskConfiguration taskConf, TaskModule module) {
		TaskInstance instance = createTaskInstance(solutions, conf, counter, livingInstance, result, student, taskConf, module);
		saveTaskInstance(instance);
		return instance;
	}

	@Override
	public boolean deleteTaskInstance(TaskInstance instance) {
		try{
			DBFactory.getInstance().deleteObject(instance);
			return true;
		}catch(Exception e) {
			return false;
		}
	}

	@Override
	public List<TaskInstance> findTaskInstancesByConfiguration(Configuration conf) {
	
		String query = "SELECT instance FROM TaskInstanceImpl AS instance WHERE instance.configuration = :key";
		DBQuery dbq = DBFactory.getInstance().createQuery(query);
		dbq.setLong("key", conf.getKey());
		return (List<TaskInstance>) dbq.list();

	}

	@Override
	public TaskInstance loadTaskInstanceByID(long ID) {
		return (TaskInstance) DBFactory.getInstance().loadObject(TaskInstanceImpl.class, ID);
	}

	@Override
	public void saveTaskInstance(TaskInstance instance) {
		DBFactory.getInstance().saveObject(instance);
	}

	@Override
	public void updateTaskInstance(TaskInstance instance) {
		DBFactory.getInstance().updateObject(instance);
	}
	
	@Override
	public boolean hasSolution(TaskInstance taskInstance) {
		return taskInstance.getSolutions().size() > 0 ? true : false;
	}

	@Override
	public TaskSolution getBestSolutionInTaskInstance(TaskInstance taskInstance) {
		
		List<TaskSolution> resultList = taskInstance.getSolutions();
		if((taskInstance.getTaskConfiguration().getTaskType().getScoringOrder().equals(TaskTypeImpl.SO_DECREASING)) ||
				(taskInstance.getTaskConfiguration().getTaskType().getScoringOrder().equals(TaskTypeImpl.SO_NONE))) {
			switch(resultList.size()) {
				case 0: return null;
				case 1: return resultList.get(0);
				case 2: return (resultList.get(0).getScore() > resultList.get(1).getScore() ? resultList.get(0) : resultList.get(1));
				case 3: TaskSolution tempSolution = null;
								tempSolution =
								resultList.get(0).getScore() > resultList.get(1).getScore() ?
										resultList.get(0) : resultList.get(1);
							return
								tempSolution.getScore() > resultList.get(2).getScore() ?
										tempSolution : resultList.get(2);										
				default: return null;
			}
		}
		
		else {
			List<TaskSolution> resultList2 = new ArrayList<TaskSolution>();
			for(TaskSolution aSolution : resultList) {
				if(aSolution.getScore()!=0) resultList2.add(aSolution);
			}
			switch(resultList2.size()) {
				case 0: return getLatestSolutionInTaskInstance(taskInstance);
				case 1: return resultList2.get(0);
				case 2: return (resultList2.get(0).getScore() < resultList2.get(1).getScore() ? resultList2.get(0) : resultList2.get(1));
				case 3: TaskSolution tempSolution = null;
								tempSolution = 
								(resultList2.get(0).getScore() < resultList2.get(1).getScore() ?
										resultList2.get(0) : resultList2.get(1));
							return
								(tempSolution.getScore() < resultList2.get(2).getScore() ?
										tempSolution : resultList2.get(2));										
				default: return null;
			}
		}
		
	}
	
	@Override
	public TaskSolution getLatestSolutionInTaskInstance(TaskInstance taskInstance) {
		
		List<TaskSolution> resultList = taskInstance.getSolutions();
		switch(resultList.size()) {
			case 0:	return null;
			case 1: return resultList.get(0);
			case 2: return (resultList.get(0).getSolutionDate().after(resultList.get(1).getSolutionDate()) ? resultList.get(0) : resultList.get(1));
			case 3: TaskSolution tempSolution = null;
							tempSolution =
								(resultList.get(0).getSolutionDate().after(resultList.get(1).getSolutionDate()) ?
										resultList.get(0) : resultList.get(1));
							return
								tempSolution.getSolutionDate().after(resultList.get(2).getSolutionDate()) ?
										tempSolution : resultList.get(2);
			default: return null;
		}
	}
	
	@Override
	public TaskSolution getLatestCorrectSolutionInTaskInstance(TaskInstance taskInstance)
	{
		List<TaskSolution> allResultList = taskInstance.getSolutions();
		ArrayList<TaskSolution> resultList = new ArrayList<TaskSolution>(); 
		for(TaskSolution aSolution : allResultList)
			if(aSolution.getScore() != 0) // a correct solution can have positive or negative score
				resultList.add(aSolution);
		
		switch(resultList.size()) {
			case 0:	return null;
			case 1: return resultList.get(0);
			case 2: return (resultList.get(0).getSolutionDate().after(resultList.get(1).getSolutionDate()) ? resultList.get(0) : resultList.get(1));
			case 3: TaskSolution tempSolution = null;
							tempSolution = 
								(resultList.get(0).getSolutionDate().after(resultList.get(1).getSolutionDate()) ?
										resultList.get(0) : resultList.get(1));
							return
								tempSolution.getSolutionDate().after(resultList.get(2).getSolutionDate()) ?
										tempSolution : resultList.get(2);
			default: return null;
		}		
	}
	
	@Override
	public boolean addTaskSolution(TaskInstance taskInstance, TaskSolution taskSolution) {
		taskInstance.incrementSolutions();
		if(taskSolution.getScore() == 0) {
			taskInstance.incrementFailedAttempts();
		}
		
		List<TaskSolution> resultList = taskInstance.getSolutions();
		
			boolean set = false;
			if(getBestSolutionInTaskInstance(taskInstance)!=null) {
				if(((taskInstance.getTaskConfiguration().getTaskType().getScoringOrder().equals(TaskTypeImpl.SO_DECREASING)) 
						|| (taskInstance.getTaskConfiguration().getTaskType().getScoringOrder().equals(TaskTypeImpl.SO_NONE)))						
						&& (getBestSolutionInTaskInstance(taskInstance).getScore()<taskSolution.getScore())) {
					TaskSolutionManagerImpl.getInstance().deleteTaskSolution(getBestSolutionInTaskInstance(taskInstance));
					taskSolution.setTaskInstance(taskInstance);
					resultList.remove(getBestSolutionInTaskInstance(taskInstance));
					set = true;
				}
				else if((taskInstance.getTaskConfiguration().getTaskType().getScoringOrder().equals(TaskTypeImpl.SO_INCREASING)) 						
						&& (((getBestSolutionInTaskInstance(taskInstance).getScore()>taskSolution.getScore()) && (taskSolution.getScore()!=0)) || ((getBestSolutionInTaskInstance(taskInstance).getScore()==0) && (taskSolution.getScore()!=0)))) {
					TaskSolutionManagerImpl.getInstance().deleteTaskSolution(getBestSolutionInTaskInstance(taskInstance));
					taskSolution.setTaskInstance(taskInstance);
					resultList.remove(getBestSolutionInTaskInstance(taskInstance));
					set = true;
				}
				
			}
			if(getLatestSolutionInTaskInstance(taskInstance)!=null) {
				if(getLatestSolutionInTaskInstance(taskInstance).getSolutionDate().before(taskSolution.getSolutionDate())) {
					if(getLatestCorrectSolutionInTaskInstance(taskInstance)!=null) {
						if(getLatestSolutionInTaskInstance(taskInstance).getKey().equals(getLatestCorrectSolutionInTaskInstance(taskInstance).getKey())) {
							if(taskInstance.getSolutions().size()<3) {
								taskSolution.setTaskInstance(taskInstance);
								set = true;
							}
						}
						else {
							
							TaskSolutionManagerImpl.getInstance().deleteTaskSolution(getLatestSolutionInTaskInstance(taskInstance));
							taskSolution.setTaskInstance(taskInstance);
							resultList.remove(getLatestSolutionInTaskInstance(taskInstance));
							set = true;
						}
					}
					else {	

						
						TaskSolutionManagerImpl.getInstance().deleteTaskSolution(getLatestSolutionInTaskInstance(taskInstance));
						taskSolution.setTaskInstance(taskInstance);
						resultList.remove(getLatestSolutionInTaskInstance(taskInstance));
						set = true;
					}
				}
			}
			else {
				taskSolution.setTaskInstance(taskInstance);
				TaskSolutionManagerImpl.getInstance().saveTaskSolution(taskSolution);
				resultList.add(taskSolution);
				taskInstance.setSolutions(resultList);
				updateTaskInstance(taskInstance);
				updateInstanceResult(taskInstance);
				return true;
			}
			if(getLatestCorrectSolutionInTaskInstance(taskInstance)!=null) {
				if(taskSolution.getScore() != 0) // a correct solution can have positive or negative score?
				{
					if(getLatestCorrectSolutionInTaskInstance(taskInstance).getSolutionDate().before(
							taskSolution.getSolutionDate())) {
						
						if(!(getLatestCorrectSolutionInTaskInstance(taskInstance).getKey().equals(getBestSolutionInTaskInstance(taskInstance).getKey()))) {
							
							
							TaskSolutionManagerImpl.getInstance().deleteTaskSolution(getLatestCorrectSolutionInTaskInstance(taskInstance));
							taskSolution.setTaskInstance(taskInstance);
							resultList.remove(getLatestCorrectSolutionInTaskInstance(taskInstance));
							set = true;							
						}
						else {
							if(taskInstance.getSolutions().size()<3) {
								taskSolution.setTaskInstance(taskInstance);
								set =true;
							}
						}
					}							
					
				}
			}
			if(set) {
				TaskSolutionManagerImpl.getInstance().saveTaskSolution(taskSolution);
				resultList.add(taskSolution);
				taskInstance.setSolutions(resultList);
				updateTaskInstance(taskInstance);
				updateInstanceResult(taskInstance);
				return true;
			}
			else {
				updateInstanceResult(taskInstance);
				return false;
			}
	}
	
	@Override
	public void updateInstanceResult(TaskInstance taskInstance) {
		
		TaskResult givenResult = taskInstance.getResult();		
		TaskSolution bestSolution = getBestSolutionInTaskInstance(taskInstance);
		
		if(bestSolution!=null) {
			if(bestSolution.getScore() != 0) {
				if(givenResult!=null) {
					if((taskInstance.getTaskConfiguration().getTaskType().getScoringOrder().equals(TaskTypeImpl.SO_DECREASING)) ||
							(taskInstance.getTaskConfiguration().getTaskType().getScoringOrder().equals(TaskTypeImpl.SO_NONE)) &&
							(givenResult.getMaxScore() < bestSolution.getScore()) || (taskInstance.getTaskConfiguration().getTaskType().getScoringOrder().equals(TaskTypeImpl.SO_INCREASING) 
									&& (givenResult.getMaxScore() > bestSolution.getScore()))) {
						givenResult.setHasPassed(true);
						givenResult.setMaxScore(bestSolution.getScore());
						givenResult.setMaxScoreSolutionText(bestSolution.getSolutionText());
						givenResult.setMaxScoreTaskText(taskInstance.getLivingTaskInstance().getTaskText());
						TaskResultManagerImpl.getInstance().updateTaskResult(givenResult);
					}
				}
				else {
					TaskResult result = TaskResultManagerImpl.getInstance().createAndPersistTaskResult(bestSolution.getSolutionDate(), bestSolution.getScore(), 
							taskInstance.getLivingTaskInstance().getTaskText(), bestSolution.getSolutionText(), true);
					taskInstance.setResult(result);
					updateTaskInstance(taskInstance);
				}
			}
		}
	}
	
	@Override
	public void deleteLivingInstancesInTaskInstances(List<TaskInstance> taskInstances, boolean keepResults) {
		
		Iterator<TaskInstance> taskInstanceIt = taskInstances.iterator();
		while(taskInstanceIt.hasNext()) {
			TaskInstance taskInstance = taskInstanceIt.next();
			//delete the living instance
			LivingTaskInstance livingInstance = taskInstance.getLivingTaskInstance();
			taskInstance.setLivingTaskInstance(null);
			//reduce the number of living instances
			if(taskInstance.getLivingInstanceCounter()>0) taskInstance.setLivingInstanceCounter(taskInstance.getLivingInstanceCounter()-1);
			this.updateTaskInstance(taskInstance);
			LivingTaskInstanceManagerImpl.getInstance().deleteLivingTaskInstance(livingInstance);
			//handle the solutions
			taskInstance.setFailedAttempts(0);
			taskInstance.setSolutionCounter(0);
			List<TaskSolution> solutions = taskInstance.getSolutions();
			taskInstance.setSolutions(null);
			this.updateTaskInstance(taskInstance);
			for(TaskSolution solution : solutions) {
				TaskSolutionManagerImpl.getInstance().deleteTaskSolution(solution);
			}
			//handle the result objects
			if(!keepResults) {
				TaskResult result = taskInstance.getResult();
				taskInstance.setResult(null);
				this.updateTaskInstance(taskInstance);
				TaskResultManagerImpl.getInstance().deleteTaskResult(result);
			}
		}
	}
	
	@Override
	public void createNewLivingTaskInstancesInTaskInstances(List<TaskInstance> taskInstances, Configuration conf) {
		
		BBautOLATConnector conn = null;
		try {
			conn = new BBautOLATConnector(conf);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (AutolatConnectorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		Iterator<TaskInstance> taskIt = taskInstances.iterator();
		while(taskIt.hasNext()) {
			TaskInstance instance = taskIt.next();
			// try to get a registration number
			String seed = instance.getStudent().getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null);
			
			if(seed == null)
			{
				// quick hack to generate a fake registration number from the user name
				String name = instance.getStudent().getIdentity().getKey().toString();
				int namenumber = 1;
				int prime[] = {2, 3, 5, 7, 11, 13, 17, 19};		
				for(int i = 0; i < name.length() && i < 8; i++)
				{
					namenumber *= java.lang.Math.pow(prime[i], name.charAt(i) % 4);
					namenumber = namenumber % 1000000 + 1; 
				}
				seed = Integer.toString(namenumber);
				//end of hack
			}
			LivingTaskInstance livingInstance = conn.getLivingTaskInstance(conf.getTaskConfiguration(), seed);
			LivingTaskInstanceManagerImpl.getInstance().saveLivingTaskInstance(livingInstance);
			instance.setLivingTaskInstance(livingInstance);
			this.updateTaskInstance(instance);
		}
	}
	
	public static TaskInstanceManagerImpl getInstance() {
		return INSTANCE;
	}

}
