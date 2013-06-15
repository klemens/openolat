package de.htwk.autolat.TaskConfiguration;

import java.util.List;

import org.olat.core.id.Persistable;

import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskType.TaskType;

public interface TaskConfiguration extends Persistable {

	public void setSignature(String sig);
	public String getSignature();
	
	public void setDocumentationText(String text);
	public String getDocumentationText();
	
	public void addTaskInstance(TaskInstance taskInstance);
	public List<TaskInstance> getTaskInstanceList();
	public void setTaskInstanceList(List<TaskInstance> taskInstanceList);
	
	public void setTaskType(TaskType type);
	public TaskType getTaskType();
	
	public String getConfigurationText();
	public void setConfigurationText(String configurationText);
	
	public void setAuthorComment(String authorComment);
	public String getAuthorComment();
	
	public void setLastError(String lastError);
	public String getLastError();
	
	public void setIsAltered(boolean isAltered);
	public boolean getIsAltered();
	
	public String getDescriptionText();
	public void setDescriptionText(String descriptionText);
}
