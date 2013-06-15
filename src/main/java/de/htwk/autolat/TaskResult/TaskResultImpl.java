package de.htwk.autolat.TaskResult;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Persistable;

import de.htwk.autolat.TaskInstance.TaskInstance;
import de.htwk.autolat.TaskSolution.TaskSolution;

public class TaskResultImpl extends PersistentObject implements TaskResult {
	
	private Date maxScoreDate;
	private double maxScore;
	private String maxScoreTaskText;
	private String maxScoreSolutionText;
	private boolean hasPassed;
	
	
	public double getMaxScore() {
		return maxScore;
	}

	public String getMaxScoreSolutionText() {
		return maxScoreSolutionText;
	}

	public String getMaxScoreTaskText() {
		return maxScoreTaskText;
	}

	public boolean getHasPassed() {
		return hasPassed;
	}

	public void setMaxScore(double score) {
		this.maxScore = score;
		
	}

	public void setMaxScoreSolutionText(String solutionText) {
		this.maxScoreSolutionText = solutionText;
	}

	public void setMaxScoreTaskText(String taskText) {
		this.maxScoreTaskText = taskText;
	}

	public void setHasPassed(boolean passed) {
		this.hasPassed = passed;
	}

	public void setMaxScoreDate(Date maxScoreDate) {
		this.maxScoreDate = maxScoreDate;
	}

	public Date getMaxScoreDate() {
		return maxScoreDate;
	}

}
