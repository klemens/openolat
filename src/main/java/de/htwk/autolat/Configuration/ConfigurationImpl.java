package de.htwk.autolat.Configuration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Persistable;

import de.htwk.autolat.ServerConnection.ServerConnection;
import de.htwk.autolat.TaskConfiguration.TaskConfiguration;
import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskModule.TaskModule;

public class ConfigurationImpl extends PersistentObject implements Configuration {
	
	private TaskConfiguration taskConfiguration;
	private List<TaskModule> taskPlan;
	private List<TaskInstance> taskInstanceList;
	private Date beginDate;
	private long courseID;
	private long courseNodeID;
	private Date endDate;
	private List<Integer> scorePoints;
	private ServerConnection serverConnection;
	private String autolatServer;
	
	public void addScorePoints(int scorePoints) {
		if(this.scorePoints == null) {
			this.scorePoints = new ArrayList<Integer>();
		}
		this.scorePoints.add(scorePoints);
	}
	public void addTaskInstance(TaskInstance taskInstance) {
		if(taskInstanceList == null) {
			taskInstanceList = new ArrayList<TaskInstance>();
		}
		taskInstanceList.add(taskInstance);
	}
	public void addTaskModule(TaskModule taskModule) {
		if(taskPlan == null) {
			taskPlan = new ArrayList<TaskModule>();
		}
		taskPlan.add(taskModule);
	}
	public Date getBeginDate() {
		return beginDate;
	}
	public long getCourseID() {
		return courseID;
	}
	public long getCourseNodeID() {
		return courseNodeID;
	}
	public Date getEndDate() {
		return endDate;
	}
	public List<Integer> getScorePoints() {
		return scorePoints;
	}
	public ServerConnection getServerConnection() {
		return serverConnection;
	}
	public TaskConfiguration getTaskConfiguration() {
		return taskConfiguration;
	}
	public List<TaskModule> getTaskPlan() {
		return (taskPlan==null ? new ArrayList<TaskModule>() : taskPlan);
	}
	public List<TaskInstance> getTaskInstanceList() {
		return (taskInstanceList==null ? new ArrayList<TaskInstance>() : taskInstanceList);
	}
	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}
	public void setCourseID(long courseID) {
		this.courseID = courseID;
	}
	public void setCourseNodeID(long courseNodeID) {
		this.courseNodeID = courseNodeID;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public void setServerConnection(ServerConnection serverConn) {
		this.serverConnection = serverConn;
	}
	public void setTaskConfiguration(TaskConfiguration taskConf) {
		this.taskConfiguration = taskConf;
	}

	public void setTaskPlan(List<TaskModule> taskPlan) {
		this.taskPlan = taskPlan;
	}
	public void setScorePoints(List<Integer> scorePoints) {
		this.scorePoints = scorePoints;
	}
	public void setTaskInstanceList(List<TaskInstance> taskInstanceList) {
		this.taskInstanceList = taskInstanceList;
	}
	public String getAutolatServer() {
		return autolatServer;
	}
	public void setAutolatServer(String autolatServer) {
		this.autolatServer = autolatServer;
	}
}
