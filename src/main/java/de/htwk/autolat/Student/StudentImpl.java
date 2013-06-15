package de.htwk.autolat.Student;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.TaskInstance.TaskInstance;

public class StudentImpl extends PersistentObject implements Student {
	private List<TaskInstance> taskInstanceList;
	private Identity identity;
	

	public void addTaskInstance(TaskInstance taskInstance) {
		if(taskInstanceList == null) {
			taskInstanceList = new ArrayList<TaskInstance>();
		}
		taskInstanceList.add(taskInstance);
	}

	public Identity getIdentity() {
		return identity;
	}

	public List<TaskInstance> getTaskInstanceList() {
		return taskInstanceList;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public void setTaskInstanceList(List<TaskInstance> taskInstanceList) {
		this.taskInstanceList = taskInstanceList;
	}

	public TaskInstance getTaskInstanceByConfiguration(Configuration configuration)
	{
		TaskInstance taskInstance = null;
		if(configuration!=null && this.getTaskInstanceList()!=null)
		{
			for(TaskInstance aTaskInstance : this.getTaskInstanceList())
				{
					if(aTaskInstance.getConfiguration().getKey().equals(configuration.getKey()))
						{
							taskInstance = aTaskInstance;
							break;
						}
				}
		}
		return taskInstance;
	}

}
