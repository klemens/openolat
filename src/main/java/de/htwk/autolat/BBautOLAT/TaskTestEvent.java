package de.htwk.autolat.BBautOLAT;

import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;

import de.htwk.autolat.TaskInstance.TaskInstance;

public class TaskTestEvent extends Event {
	private TaskInstance taskInstance;
	

	public TaskTestEvent(TaskInstance taskInstance, String command) {
		super(command);
		this.taskInstance = taskInstance;
		// TODO Auto-generated constructor stub
	}
	
	public TaskInstance getTaskInstance() {
		return taskInstance;
	}

}
