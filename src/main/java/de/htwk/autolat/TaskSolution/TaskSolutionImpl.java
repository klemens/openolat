package de.htwk.autolat.TaskSolution;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;

import de.htwk.autolat.TaskInstance.TaskInstance;

public class TaskSolutionImpl extends PersistentObject implements TaskSolution {
	
	private String evaluationText;
	private double score;
	private String solutionText;
	private Date solutionDate;
	
	private TaskInstance taskInstance;	
	
	public String getEvaluationText() {
		return evaluationText;
	}

	public double getScore() {
		return score;
	}

	public String getSolutionText() {
		return solutionText;
	}

	public void setEvaluationText(String text) {
		this.evaluationText = text;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public void setSolutionText(String text) {
		this.solutionText = text;
	}
	
	public void setSolutionDate(Date date) {
		this.solutionDate = date;
	}

	public Date getSolutionDate() {
		return this.solutionDate;
	}

	public TaskInstance getTaskInstance() {
		return taskInstance;
	}

	public void setTaskInstance(TaskInstance taskInstance) {
		this.taskInstance = taskInstance;
	}
}
