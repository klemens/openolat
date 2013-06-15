package de.htwk.autolat.Student;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

import de.htwk.autolat.Configuration.Configuration;
import de.htwk.autolat.TaskInstance.TaskInstance;

public interface Student extends Persistable {
	
	public void setIdentity(Identity identity);
	public Identity getIdentity();
	
	public void addTaskInstance(TaskInstance taskInstance);
	public List<TaskInstance> getTaskInstanceList();
	public void setTaskInstanceList(List<TaskInstance> taskInstanceList);
	
	public TaskInstance getTaskInstanceByConfiguration(Configuration conf);
}
