package de.htwk.autolat.TaskConfiguration;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.PersistentObject;

import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskType.TaskType;

public class TaskConfigurationImpl extends PersistentObject implements TaskConfiguration {

	private List<TaskInstance> taskInstanceList;
	private String configurationText;
	private String documentationText;
	private String descriptionText;
	private String authorComment;
	private String signature;
	private String lastError;
	private boolean isAltered;
	private TaskType taskType;
	
	public void addTaskInstance(TaskInstance taskInstance) {
		if(taskInstanceList == null) {
			taskInstanceList = new ArrayList<TaskInstance>();
		}
		taskInstanceList.add(taskInstance);
	}

	public String getDocumentationText() {
		return documentationText;
	}

	public String getSignature() {
		return signature;
	}

	public List<TaskInstance> getTaskInstanceList() {
		return taskInstanceList;
	}

	public TaskType getTaskType() {
		return taskType;
	}

	public void setDocumentationText(String text) {
		this.documentationText = text;
	}

	public void setSignature(String sig) {
		this.signature = sig;
	}

	public void setTaskType(TaskType type) {
		this.taskType = type;
	}

	public void setTaskInstanceList(List<TaskInstance> taskInstanceList) {
		this.taskInstanceList = taskInstanceList;
	}

	public String getConfigurationText() {
		return configurationText;
	}

	public void setConfigurationText(String configurationText) {
		this.configurationText = configurationText;
	}

	public void setAuthorComment(String authorComment) {
		this.authorComment = authorComment;
	}

	public String getAuthorComment() {
		return authorComment;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}

	public String getLastError() {
		return lastError;
	}

	public void setIsAltered(boolean isAltered) {
		this.isAltered = isAltered;
	}

	public boolean getIsAltered() {
		return isAltered;
	}
	
	public String getDescriptionText() {
		return descriptionText;
	}

	public void setDescriptionText(String descriptionText) {
		this.descriptionText = descriptionText;
	}

}
