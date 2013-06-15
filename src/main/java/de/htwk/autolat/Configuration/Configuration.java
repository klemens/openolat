package de.htwk.autolat.Configuration;

import java.util.Date;
import java.util.List;
import org.olat.core.id.Persistable;

import de.htwk.autolat.ServerConnection.ServerConnection;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskModule.TaskModule;

public interface Configuration extends Persistable {
	
	public void addTaskModule(TaskModule taskModule);
	
	public List<TaskModule> getTaskPlan();
	
	public void setCourseID(long courseID);
	
	public long getCourseID();

	public void setCourseNodeID(long courseNodeID);
	
	public long getCourseNodeID();
		
	public void addScorePoints( int scorePoints);
	
	public List<Integer> getScorePoints();
	
	public void setBeginDate(Date beginDate);
	
	public Date getBeginDate();
	
	public void setEndDate(Date endDate);
	
	public Date getEndDate();
	
	public void addTaskInstance(TaskInstance taskInstance);
	
	public List<TaskInstance> getTaskInstanceList();
	
	public void setTaskInstanceList(List<TaskInstance> taskInstanceList);
	
	public void setServerConnection(ServerConnection serverConn);
	
	public String getAutolatServer();
	
	public void setAutolatServer(String autolatserver);
	
	public ServerConnection getServerConnection();
	
	public void setTaskConfiguration(TaskConfiguration taskConf);
	
	public TaskConfiguration getTaskConfiguration();
	
	public void setScorePoints(List<Integer> scorePoints);
	
	public void setTaskPlan(List<TaskModule> taskPlan);

}
