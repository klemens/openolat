package de.htwk.autolat.TaskConfiguration;

import java.util.Iterator;
import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;

import de.htwk.autolat.Configuration.ConfigurationManagerImpl;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskType.TaskType;

public class TaskConfigurationManagerImpl extends TaskConfigurationManager {
	
	private static final TaskConfigurationManagerImpl INSTANCE = new TaskConfigurationManagerImpl();
	
	private TaskConfigurationManagerImpl() {
		//nothing to do here
	}

	@Override
	public TaskConfiguration createTaskConfiguration(List<TaskInstance> taskInstanceList, String configurationText, String documentationText, String descriptionText, String authorComment, String signature,
			String lastError, boolean isAltered, TaskType taskType) {
		TaskConfiguration conf = new TaskConfigurationImpl();
		conf.setConfigurationText(configurationText);
		conf.setDocumentationText(documentationText);
		conf.setDescriptionText(descriptionText);
		conf.setAuthorComment(authorComment);
		conf.setSignature(signature);
		conf.setLastError(lastError);
		conf.setIsAltered(isAltered);
		conf.setTaskInstanceList(taskInstanceList);
		conf.setTaskType(taskType);
		return conf;
	}
	
	@Override
	public TaskConfiguration createAndPersistTaskConfiguration(List<TaskInstance> taskInstanceList, String configurationText, String documentationText, String descriptionText, String authorComment, String signature,
			String lastError, boolean isAltered, TaskType taskType) {
		
		TaskConfiguration conf = createTaskConfiguration(taskInstanceList, configurationText, documentationText, descriptionText, authorComment, signature, lastError, isAltered,
				taskType);
		saveTaskConfiguration(conf);
		return conf;		
	}

	@Override
	public boolean deleteTaskConfiguration(TaskConfiguration taskConf) {
		try {
			DBFactory.getInstance().deleteObject(taskConf);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public void handleUnusedDuplicates() {
		
		List<TaskConfiguration> taskConfigurations = getAllTaskConfigurations();
		Iterator<TaskConfiguration> taskConfigIt = taskConfigurations.iterator();
		while(taskConfigIt.hasNext()) {
			TaskConfiguration taskConfig = taskConfigIt.next();
			if(ConfigurationManagerImpl.getInstance().getConfigurationByTaskConfiguration(taskConfig)!=null)
				taskConfigIt.remove();
		}
		//eliminate all task configs, which are currently unused and are duplicates, by comparing the configuration text
		Iterator<TaskConfiguration> duplicateIt1 = taskConfigurations.iterator();
		while(duplicateIt1.hasNext()) {
			TaskConfiguration victim = duplicateIt1.next();
			boolean remove = false;
			Iterator<TaskConfiguration> duplicateIt2 = taskConfigurations.iterator();
			while(duplicateIt2.hasNext()) {
				TaskConfiguration survivor = duplicateIt2.next();
				if((!victim.getKey().equals(survivor.getKey())) && (victim.getConfigurationText().equals(survivor.getConfigurationText()))) {
					remove = true;
					break;
				}
			}
			if(remove) { 
				duplicateIt1.remove();
				deleteTaskConfiguration(victim);
			}
		}
	}

	@Override
	public TaskConfiguration loadTaskConfigurationByID(long ID) {
		return (TaskConfiguration)DBFactory.getInstance().loadObject(TaskConfigurationImpl.class, ID);
	}

	@Override
	public void saveTaskConfiguration(TaskConfiguration taskConf) {
		DBFactory.getInstance().saveObject(taskConf);
	}

	@Override
	public void updateTaskConfiguration(TaskConfiguration taskConf) {
		DBFactory.getInstance().updateObject(taskConf);
	}
	
	@Override
	public List<TaskConfiguration> getAllTaskConfigurations() {
		
		String query = "SELECT tc FROM TaskConfigurationImpl AS tc";
		DBQuery dbq = DBFactory.getInstance().createQuery(query);
		List result = dbq.list();
		return result;
	}
	
	@Override
	public List<TaskConfiguration> getAllAlteredTaskConfigurations(TaskConfiguration taskConf) {
		
		List<TaskConfiguration> result = getAllTaskConfigurations();
		Iterator<TaskConfiguration> confIt = result.iterator();
		while(confIt.hasNext()) {
			TaskConfiguration temp = confIt.next();
			if(!temp.getIsAltered()) { 
				confIt.remove();
				continue;
			}
			if (taskConf!=null) {
				if(taskConf.getKey().equals(temp.getKey())) confIt.remove();
				else if(!taskConf.getTaskType().getKey().equals(temp.getTaskType().getKey())) confIt.remove();
			}
		}
		
		return result;
	}
	
	@Override
	public void saveOrUpdateTaskConfiguration(TaskConfiguration taskConfiguration) {
		
		if(taskConfiguration.getKey() == null) {
			saveTaskConfiguration(taskConfiguration);
		}
		else {
			updateTaskConfiguration(taskConfiguration);
		}
	}

	public static TaskConfigurationManagerImpl getInstance() {
		return INSTANCE;
	}
	
}
