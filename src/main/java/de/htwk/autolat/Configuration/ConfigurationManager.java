package de.htwk.autolat.Configuration;

import java.util.Date;
import java.util.List;

import de.htwk.autolat.ServerConnection.ServerConnection;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskModule.TaskModule;

public interface ConfigurationManager {
	
	public Configuration createConfiguration(TaskConfiguration taskConfiguration, List<TaskModule> taskPlan, List<TaskInstance> taskInstanceList,
				Date beginDate, long courseID, long courseNodeID, Date endDate, List<Integer> scorePoints, ServerConnection serverConnection, String autolatServer);
	
	public Configuration createAndPersistConfiguration (TaskConfiguration taskConfiguration, List<TaskModule> taskPlan,
			List<TaskInstance> taskInstanceList, Date beginDate, long courseID, long courseNodeID, Date endDate, List<Integer> scorePoints,
			ServerConnection serverConnection, String autolatServer);
	
	public Configuration getConfigurationByCourseID(long courseID, long courseNodeID); 
	
	public Configuration loadConfigurationByID(long ID);
		
	public void saveConfiguration(Configuration conf);
	
	public void updateConfiguration(Configuration conf);
	
	public boolean deleteConfiguration(Configuration conf);

	public Configuration getConfigurationByTaskConfiguration(TaskConfiguration taskConfig);
}
