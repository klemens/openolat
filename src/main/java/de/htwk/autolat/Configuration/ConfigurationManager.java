package de.htwk.autolat.Configuration;

import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;

import de.htwk.autolat.ServerConnection.ServerConnection;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskModule.TaskModule;

public abstract class ConfigurationManager {
	
	public abstract Configuration createConfiguration(TaskConfiguration taskConfiguration, List<TaskModule> taskPlan, List<TaskInstance> taskInstanceList,
				Date beginDate, long courseID, long courseNodeID, Date endDate, List<Integer> scorePoints, ServerConnection serverConnection, String autolatServer);
	
	public abstract Configuration createAndPersistConfiguration (TaskConfiguration taskConfiguration, List<TaskModule> taskPlan,
			List<TaskInstance> taskInstanceList, Date beginDate, long courseID, long courseNodeID, Date endDate, List<Integer> scorePoints,
			ServerConnection serverConnection, String autolatServer);
	
	public abstract Configuration getConfigurationByCourseID(long courseID, long courseNodeID); 
	
	public abstract Configuration loadConfigurationByID(long ID);
		
	public abstract void saveConfiguration(Configuration conf);
	
	public abstract void updateConfiguration(Configuration conf);
	
	public abstract boolean deleteConfiguration(Configuration conf);

	public abstract Configuration getConfigurationByTaskConfiguration(TaskConfiguration taskConfig);
}
